package com.ruoyi.mall.cart.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.cart.domain.MallCartResult;
import com.ruoyi.mall.cart.domain.dto.MallCartAddRequest;
import com.ruoyi.mall.cart.mapper.MallCartMapper;
import com.ruoyi.mall.cart.service.IMallCartService;

@Service
public class MallCartServiceImpl implements IMallCartService
{
    private static final int MAX_QUANTITY = 99;
    private final MallCartMapper mapper;

    public MallCartServiceImpl(MallCartMapper mapper) { this.mapper = mapper; }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallCartResult selectCart(Long memberId)
    {
        requireMember(memberId);
        return buildResult(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallCartResult add(Long memberId, MallCartAddRequest request)
    {
        requireMember(memberId);
        if (request == null || request.getSkuId() == null) throw new ServiceException("SKU 不能为空");
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        validateQuantity(quantity);
        MallCartItem sku = requirePurchasableSku(request.getSkuId());
        MallCartItem existing = mapper.selectItemForUpdate(memberId, request.getSkuId());
        int newQuantity = quantity;
        if (existing != null)
        {
            newQuantity = existing.getQuantity() + quantity;
            ensureStock(sku, newQuantity);
            if (mapper.updateQuantity(memberId, request.getSkuId(), newQuantity) != 1)
                throw new ServiceException("购物车更新失败，请重试");
        }
        else
        {
            ensureStock(sku, newQuantity);
            MallCartItem item = new MallCartItem();
            item.setMemberId(memberId); item.setSkuId(request.getSkuId()); item.setQuantity(newQuantity); item.setSelectedFlag("1");
            if (mapper.insertItemIgnore(item) == 0)
            {
                MallCartItem concurrent = mapper.selectItemForUpdate(memberId, request.getSkuId());
                if (concurrent == null) throw new ServiceException("购物车更新失败，请重试");
                newQuantity = concurrent.getQuantity() + quantity;
                ensureStock(sku, newQuantity);
                if (mapper.updateQuantity(memberId, request.getSkuId(), newQuantity) != 1)
                    throw new ServiceException("购物车更新失败，请重试");
            }
        }
        return buildResult(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallCartResult updateQuantity(Long memberId, Long skuId, int quantity)
    {
        requireMember(memberId); validateQuantity(quantity);
        MallCartItem sku = requirePurchasableSku(skuId);
        ensureStock(sku, quantity);
        if (mapper.updateQuantity(memberId, skuId, quantity) != 1) throw new ServiceException("购物车商品不存在");
        return buildResult(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallCartResult updateSelected(Long memberId, Long skuId, boolean selected)
    {
        requireMember(memberId);
        if (selected) ensureStock(requirePurchasableSku(skuId), 1);
        if (mapper.updateSelected(memberId, skuId, selected ? "1" : "0") != 1)
            throw new ServiceException("购物车商品不存在");
        return buildResult(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallCartResult remove(Long memberId, Long skuId)
    {
        requireMember(memberId);
        if (skuId == null || mapper.deleteItem(memberId, skuId) != 1) throw new ServiceException("购物车商品不存在");
        return buildResult(memberId);
    }

    private MallCartResult buildResult(Long memberId)
    {
        List<MallCartItem> items = mapper.selectItems(memberId);
        BigDecimal total = BigDecimal.ZERO;
        int totalCount = 0;
        boolean canCheckout = false;
        for (MallCartItem item : items)
        {
            boolean valid = isValid(item);
            boolean shortage = valid && item.getQuantity() > item.getAvailableStock();
            item.setValid(valid); item.setStockShortage(shortage);
            item.setLineAmount(item.getPrice() == null ? BigDecimal.ZERO
                    : item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            totalCount += item.getQuantity();
            if (!valid && "1".equals(item.getSelectedFlag()))
            {
                mapper.updateSelected(memberId, item.getSkuId(), "0");
                item.setSelectedFlag("0");
            }
            if ("1".equals(item.getSelectedFlag()) && valid)
            {
                total = total.add(item.getLineAmount());
                canCheckout = canCheckout || !shortage;
                if (shortage) canCheckout = false;
            }
        }
        MallCartResult result = new MallCartResult();
        result.setItems(items); result.setTotalCount(totalCount); result.setTotalPrice(total);
        result.setCanCheckout(canCheckout && items.stream().noneMatch(x -> "1".equals(x.getSelectedFlag())
                && (!Boolean.TRUE.equals(x.getValid()) || Boolean.TRUE.equals(x.getStockShortage()))));
        return result;
    }

    private MallCartItem requirePurchasableSku(Long skuId)
    {
        if (skuId == null || skuId <= 0) throw new ServiceException("SKU 无效");
        MallCartItem sku = mapper.selectSkuSnapshot(skuId);
        if (sku == null || !isValid(sku)) throw new ServiceException("商品已下架或已失效");
        return sku;
    }

    private void ensureStock(MallCartItem sku, int quantity)
    {
        if (quantity > MAX_QUANTITY) throw new ServiceException("单个 SKU 最多购买 " + MAX_QUANTITY + " 件");
        if (sku.getAvailableStock() == null || sku.getAvailableStock() < quantity)
            throw new ServiceException("库存不足，仅剩 " + Math.max(0, sku.getAvailableStock() == null ? 0 : sku.getAvailableStock()) + " 件");
    }

    private static boolean isValid(MallCartItem item)
    {
        return item != null && "1".equals(item.getSkuStatus()) && "0".equals(item.getSkuDelFlag())
                && "1".equals(item.getSpuPublishStatus()) && "0".equals(item.getSpuDelFlag())
                && item.getAvailableStock() != null && item.getAvailableStock() > 0;
    }

    private static void validateQuantity(int quantity)
    {
        if (quantity <= 0) throw new ServiceException("购物车数量必须大于 0");
        if (quantity > MAX_QUANTITY) throw new ServiceException("单个 SKU 最多购买 " + MAX_QUANTITY + " 件");
    }

    private static void requireMember(Long memberId) { if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效"); }
}
