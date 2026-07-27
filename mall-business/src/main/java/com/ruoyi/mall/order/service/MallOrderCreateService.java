package com.ruoyi.mall.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.cart.domain.MallCartResult;
import com.ruoyi.mall.cart.service.IMallCartService;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.domain.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallPaymentStatus;
import com.ruoyi.mall.order.domain.dto.MallCreateOrderRequest;
import com.ruoyi.mall.order.mapper.MallOrderMapper;

@Service
public class MallOrderCreateService
{
    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int MAX_ORDER_NO_ATTEMPTS = 3;
    private final MallOrderMapper mapper;
    private final IMallCartService cartService;
    private final MallMemberAuthService memberService;
    private final InventoryPort inventoryPort;

    /** Constructor kept for isolated unit tests that do not exercise stock reservation. */
    public MallOrderCreateService(MallOrderMapper mapper, IMallCartService cartService,
            MallMemberAuthService memberService)
    {
        this(mapper, cartService, memberService, null);
    }

    @Autowired
    public MallOrderCreateService(MallOrderMapper mapper, IMallCartService cartService,
            MallMemberAuthService memberService, InventoryPort inventoryPort)
    {
        this.mapper = mapper;
        this.cartService = cartService;
        this.memberService = memberService;
        this.inventoryPort = inventoryPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallOrder create(Long memberId, MallCreateOrderRequest request)
    {
        validateRequest(memberId, request);
        MallOrder existing = mapper.selectByIdempotencyKey(memberId, request.getIdempotencyKey());
        if (existing != null) return existing;

        MallCartResult cart = cartService.selectCart(memberId);
        List<MallCartItem> selectedItems = validateCart(cart);
        MallMemberAddress address = findAddress(memberId, request.getAddressId());
        MallOrder order = buildOrder(memberId, request, address, selectedItems);

        for (int attempt = 0; attempt < MAX_ORDER_NO_ATTEMPTS; attempt++)
        {
            order.setOrderNo(generateOrderNo());
            if (mapper.insertOrderIgnore(order) == 1)
            {
                saveItemsAndLog(order, selectedItems, request);
                return order;
            }
            existing = mapper.selectByIdempotencyKey(memberId, request.getIdempotencyKey());
            if (existing != null) return existing;
        }
        throw new ServiceException("订单创建失败，请重试");
    }

    private void saveItemsAndLog(MallOrder order, List<MallCartItem> selectedItems,
            MallCreateOrderRequest request)
    {
        for (MallCartItem cartItem : selectedItems)
        {
            MallOrderItem item = new MallOrderItem();
            item.setOrderId(order.getOrderId());
            item.setSkuId(cartItem.getSkuId()); item.setSkuCode(cartItem.getSkuCode());
            item.setProductName(cartItem.getProductName()); item.setSkuName(cartItem.getSkuName());
            item.setProductImage(cartItem.getProductImage()); item.setUnitPrice(cartItem.getPrice());
            item.setQuantity(cartItem.getQuantity()); item.setLineAmount(cartItem.getLineAmount());
            if (inventoryPort != null) inventoryPort.reserve(order.getOrderNo(), cartItem.getSkuId(), cartItem.getQuantity());
            if (mapper.insertItem(item) != 1) throw new ServiceException("订单商品保存失败，请重试");
        }
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setToStatus(order.getStatus()); log.setOperatorType("MEMBER");
        log.setOperatorId(String.valueOf(order.getMemberId())); log.setRemark("会员创建订单");
        log.setRequestId(request.getIdempotencyKey());
        if (mapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败，请重试");
    }

    private MallOrder buildOrder(Long memberId, MallCreateOrderRequest request,
            MallMemberAddress address, List<MallCartItem> selectedItems)
    {
        BigDecimal productAmount = selectedItems.stream().map(item -> item.getLineAmount() == null
                ? BigDecimal.ZERO : item.getLineAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        MallOrder order = new MallOrder();
        order.setMemberId(memberId); order.setStatus(MallOrderStatus.PENDING_PAYMENT.name());
        order.setPaymentStatus(MallPaymentStatus.UNPAID.name()); order.setIdempotencyKey(request.getIdempotencyKey());
        order.setProductAmount(productAmount); order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO); order.setPayableAmount(productAmount);
        order.setReceiverName(address.getReceiverName()); order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvince()); order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict()); order.setReceiverDetailAddress(address.getDetailAddress());
        order.setRemark(request.getRemark()); order.setVersion(0); order.setCreateTime(LocalDateTime.now());
        return order;
    }

    private List<MallCartItem> validateCart(MallCartResult cart)
    {
        if (cart == null || !Boolean.TRUE.equals(cart.getCanCheckout()) || cart.getItems() == null)
            throw new ServiceException("购物车商品已变化，请返回购物车重新确认");
        List<MallCartItem> selected = cart.getItems().stream()
                .filter(item -> "1".equals(item.getSelectedFlag()))
                .sorted(Comparator.comparing(MallCartItem::getSkuId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        if (selected.isEmpty()) throw new ServiceException("请至少选择一件商品");
        if (selected.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getValid())
                || Boolean.TRUE.equals(item.getStockShortage()) || item.getSkuId() == null || item.getSkuId() <= 0
                || item.getQuantity() == null || item.getQuantity() <= 0))
            throw new ServiceException("商品状态或库存已变化，请返回购物车重新确认");
        return selected;
    }

    private MallMemberAddress findAddress(Long memberId, Long addressId)
    {
        if (addressId == null) throw new ServiceException("请选择收货地址");
        List<MallMemberAddress> addresses = memberService.addresses(memberId);
        if (addresses != null)
        {
            for (MallMemberAddress address : addresses)
            {
                if (addressId.equals(address.getAddressId())) return address;
            }
        }
        throw new ServiceException("收货地址不存在");
    }

    private void validateRequest(Long memberId, MallCreateOrderRequest request)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
        if (request == null || request.getIdempotencyKey() == null
                || request.getIdempotencyKey().trim().isEmpty()) throw new ServiceException("幂等键不能为空");
        if (request.getIdempotencyKey().length() > 80) throw new ServiceException("幂等键长度不能超过80");
    }

    private String generateOrderNo()
    {
        return "M" + ORDER_NO_TIME.format(LocalDateTime.now())
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
