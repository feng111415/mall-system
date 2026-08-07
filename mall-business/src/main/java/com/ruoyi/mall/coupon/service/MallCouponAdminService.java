package com.ruoyi.mall.coupon.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.coupon.domain.MallCouponTemplate;
import com.ruoyi.mall.coupon.mapper.MallCouponMapper;

@Service
public class MallCouponAdminService
{
    private final MallCouponMapper mapper;
    private final MallCouponService couponService;

    public MallCouponAdminService(MallCouponMapper mapper, MallCouponService couponService)
    {
        this.mapper = mapper; this.couponService = couponService;
    }

    public List<MallCouponTemplate> list(String keyword, String status)
    {
        return mapper.selectAdminTemplates(keyword == null ? null : keyword.trim(), status);
    }

    public MallCouponTemplate detail(Long couponId)
    {
        MallCouponTemplate value = mapper.selectAdminTemplate(couponId);
        if (value == null) throw new ServiceException("优惠券不存在");
        value.setScopeTargetIds(mapper.selectScopeTargetIds(couponId));
        return value;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCouponTemplate save(MallCouponTemplate value, String operatorId)
    {
        validate(value);
        boolean created = value.getCouponId() == null;
        if (value.getCouponId() == null)
        {
            value.setStatus("DRAFT"); value.setCreateBy(operatorId);
            if (mapper.insertTemplate(value) != 1) throw new ServiceException("优惠券保存失败");
        }
        else
        {
            value.setUpdateBy(operatorId);
            if (mapper.updateTemplate(value) != 1) throw new ServiceException("仅草稿优惠券允许编辑");
            mapper.deleteScopes(value.getCouponId());
        }
        if (!"ALL".equals(value.getScopeType()))
            for (Long targetId : value.getScopeTargetIds()) mapper.insertScope(value.getCouponId(), targetId);
        mapper.insertAudit(value.getCouponId(), created ? "CREATE" : "UPDATE", operatorId, value.getCouponName());
        return detail(value.getCouponId());
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCouponTemplate changeStatus(Long couponId, String status, String operatorId)
    {
        if (!("PUBLISHED".equals(status) || "PAUSED".equals(status))) throw new ServiceException("优惠券状态无效");
        MallCouponTemplate current = detail(couponId);
        if ("PUBLISHED".equals(status) && current.getTotalQuantity() <= (current.getClaimedQuantity() == null ? 0 : current.getClaimedQuantity()))
            throw new ServiceException("优惠券已无可发放数量");
        if (mapper.updateTemplateStatus(couponId, status, operatorId) != 1)
            throw new ServiceException("优惠券状态已变化，请刷新后重试");
        mapper.insertAudit(couponId, "PUBLISHED".equals(status) ? "PUBLISH" : "PAUSE", operatorId, status);
        return detail(couponId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void issue(Long couponId, Long memberId, String operatorId)
    {
        couponService.claim(memberId, couponId);
        mapper.insertAudit(couponId, "ISSUE", operatorId, "memberId=" + memberId);
    }

    private void validate(MallCouponTemplate value)
    {
        if (value == null || value.getCouponName() == null || value.getCouponName().trim().isEmpty())
            throw new ServiceException("优惠券名称不能为空");
        if (!("ALL".equals(value.getScopeType()) || "CATEGORY".equals(value.getScopeType()) || "PRODUCT".equals(value.getScopeType())))
            throw new ServiceException("优惠券适用范围无效");
        if (value.getThresholdAmount() == null || value.getDiscountAmount() == null
                || value.getThresholdAmount().compareTo(BigDecimal.ZERO) < 0
                || value.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0
                || value.getDiscountAmount().compareTo(value.getThresholdAmount()) > 0)
            throw new ServiceException("优惠券金额规则无效");
        if (value.getTotalQuantity() == null || value.getTotalQuantity() <= 0)
            throw new ServiceException("发行数量必须大于0");
        if (value.getValidFrom() == null || value.getValidTo() == null || !value.getValidTo().isAfter(value.getValidFrom()))
            throw new ServiceException("有效期必须完整且结束时间晚于开始时间");
        if (!"ALL".equals(value.getScopeType()) && (value.getScopeTargetIds() == null || value.getScopeTargetIds().isEmpty()))
            throw new ServiceException("指定范围优惠券至少选择一个分类或商品");
    }
}
