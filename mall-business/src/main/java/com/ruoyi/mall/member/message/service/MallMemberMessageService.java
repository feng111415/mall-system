package com.ruoyi.mall.member.message.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.member.message.domain.MallMemberMessage;
import com.ruoyi.mall.member.message.domain.MallMemberMessageSummary;
import com.ruoyi.mall.member.message.mapper.MallMemberMessageMapper;

@Service
public class MallMemberMessageService implements MemberMessagePort
{
    private static final Map<String, String[]> CATEGORIES = new LinkedHashMap<>();
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("ORDER", "LOGISTICS", "AFTER_SALE", "ACCOUNT");
    static
    {
        CATEGORIES.put("ORDER", new String[] { "订单消息", "下单、支付和订单状态变化" });
        CATEGORIES.put("LOGISTICS", new String[] { "物流提醒", "发货、运输和签收节点" });
        CATEGORIES.put("AFTER_SALE", new String[] { "售后进度", "申请、审核和退款处理" });
        CATEGORIES.put("ACCOUNT", new String[] { "账户安全", "新设备登录和安全提醒" });
    }

    private final MallMemberMessageMapper mapper;

    public MallMemberMessageService(MallMemberMessageMapper mapper) { this.mapper = mapper; }

    public List<MallMemberMessageSummary> summary(Long memberId)
    {
        requireMember(memberId);
        Map<String, MallMemberMessageSummary> values = new LinkedHashMap<>();
        CATEGORIES.forEach((category, text) -> values.put(category,
                new MallMemberMessageSummary(category, text[0], text[1])));
        for (MallMemberMessageSummary value : mapper.selectSummary(memberId))
        {
            MallMemberMessageSummary target = values.get(value.getCategory());
            if (target == null) continue;
            target.setUnreadCount(value.getUnreadCount() == null ? 0 : value.getUnreadCount());
            target.setTotalCount(value.getTotalCount() == null ? 0 : value.getTotalCount());
            target.setLatestTitle(value.getLatestTitle()); target.setLatestSummary(value.getLatestSummary());
            target.setLatestTime(value.getLatestTime());
        }
        return new ArrayList<>(values.values());
    }

    public List<MallMemberMessage> list(Long memberId, String category)
    {
        requireMember(memberId);
        String normalized = normalizeCategory(category, true);
        return mapper.selectList(memberId, normalized);
    }

    public MallMemberMessage detail(Long memberId, Long messageId)
    {
        requireMember(memberId);
        if (messageId == null || messageId <= 0) throw new ServiceException("消息参数无效");
        MallMemberMessage value = mapper.selectById(memberId, messageId);
        if (value == null) throw new ServiceException("消息不存在");
        return value;
    }

    @Transactional
    public MallMemberMessage markRead(Long memberId, Long messageId)
    {
        MallMemberMessage value = detail(memberId, messageId);
        mapper.markRead(memberId, messageId);
        value.setReadFlag("1");
        return value;
    }

    @Transactional
    public int markAllRead(Long memberId, String category)
    {
        requireMember(memberId);
        return mapper.markAllRead(memberId, normalizeCategory(category, true));
    }

    @Override
    public void publish(Long memberId, String category, String title, String summary, String content,
            String businessType, Long businessId, String businessNo, String actionPath)
    {
        requireMember(memberId);
        String normalizedCategory = normalizeCategory(category, false);
        if (StringUtils.isBlank(title)) throw new ServiceException("消息标题不能为空");
        if (!isSafeActionPath(actionPath)) throw new ServiceException("消息跳转地址不在允许范围内");
        MallMemberMessage message = new MallMemberMessage();
        message.setMemberId(memberId); message.setCategory(normalizedCategory);
        message.setTitle(trim(title, 120)); message.setSummary(trim(summary, 255));
        message.setContent(trim(content, 2000)); message.setBusinessType(trim(businessType, 40));
        message.setBusinessId(businessId); message.setBusinessNo(trim(businessNo, 80));
        message.setActionPath(trim(actionPath, 200));
        message.setReadFlag("0");
        if (mapper.insert(message) != 1) throw new ServiceException("会员消息保存失败");
    }

    private String normalizeCategory(String value, boolean nullable)
    {
        if (StringUtils.isBlank(value))
        {
            if (nullable) return null;
            throw new ServiceException("消息分类不能为空");
        }
        String normalized = value.trim().toUpperCase();
        if (!ALLOWED_CATEGORIES.contains(normalized)) throw new ServiceException("消息分类无效");
        return normalized;
    }

    private boolean isSafeActionPath(String value)
    {
        if (StringUtils.isBlank(value)) return true;
        String path = value.trim();
        return (path.matches("^/(orders|after-sales)(/\\d+)?(/logistics)?$")
                || path.matches("^/account(/privacy)?$"))
                && !path.contains("//") && !path.contains("..") && !path.contains("?") && !path.contains("#");
    }

    private String trim(String value, int max)
    {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.length() > max ? normalized.substring(0, max) : normalized;
    }

    private void requireMember(Long memberId)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
    }
}
