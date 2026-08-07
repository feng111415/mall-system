package com.ruoyi.mall.member.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.message.domain.MallMemberMessage;
import com.ruoyi.mall.member.message.domain.MallMemberMessageSummary;
import com.ruoyi.mall.member.message.mapper.MallMemberMessageMapper;
import com.ruoyi.mall.member.message.service.MallMemberMessageService;

class MallMemberMessageServiceTest
{
    @Mock private MallMemberMessageMapper mapper;
    private MallMemberMessageService service;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); service = new MallMemberMessageService(mapper); }

    @Test
    void summaryAlwaysReturnsFourBusinessConversationsAndUnreadCounts()
    {
        MallMemberMessageSummary order = new MallMemberMessageSummary();
        order.setCategory("ORDER"); order.setUnreadCount(2); order.setTotalCount(3);
        order.setLatestTitle("订单已创建");
        when(mapper.selectSummary(7L)).thenReturn(List.of(order));

        List<MallMemberMessageSummary> result = service.summary(7L);

        assertEquals(4, result.size());
        assertEquals("ORDER", result.get(0).getCategory());
        assertEquals(2, result.get(0).getUnreadCount());
        assertEquals(0, result.get(1).getUnreadCount());
    }

    @Test
    void memberIsolationIsPassedToEveryRead()
    {
        service.list(7L, "ORDER");
        verify(mapper).selectList(7L, "ORDER");
        verify(mapper, never()).selectList(eq(8L), any());
    }

    @Test
    void markReadIsIdempotentAndOnlyMarksOwnedMessage()
    {
        MallMemberMessage message = message(11L);
        when(mapper.selectById(7L, 11L)).thenReturn(message);
        when(mapper.markRead(7L, 11L)).thenReturn(1);

        MallMemberMessage result = service.markRead(7L, 11L);

        assertEquals("1", result.getReadFlag());
        verify(mapper).markRead(7L, 11L);
    }

    @Test
    void rejectsInvalidCategoryAndUnsafeActionPath()
    {
        assertThrows(ServiceException.class, () -> service.list(7L, "MIXED"));
        assertThrows(ServiceException.class, () -> service.publish(7L, "ORDER", "订单", "摘要", null,
                "ORDER", 1L, "M1", "javascript:alert(1)"));
        verify(mapper, never()).insert(any());
    }

    @Test
    void publishesCoreMessageWithUnreadFlag()
    {
        when(mapper.insert(any())).thenAnswer(invocation -> {
            MallMemberMessage value = invocation.getArgument(0);
            value.setMessageId(99L);
            return 1;
        });

        service.publish(7L, "LOGISTICS", "已发货", "包裹已出库", "物流详情", "ORDER", 10L,
                "M10", "/orders/10/logistics");

        org.mockito.ArgumentCaptor<MallMemberMessage> captor = org.mockito.ArgumentCaptor.forClass(MallMemberMessage.class);
        verify(mapper).insert(captor.capture());
        assertEquals("0", captor.getValue().getReadFlag());
        assertEquals("LOGISTICS", captor.getValue().getCategory());
    }

    @Test
    void allowsAccountPrivacyActionPath()
    {
        when(mapper.insert(any())).thenReturn(1);

        service.publish(7L, "ACCOUNT", "登录手机号已更换", "手机号更换成功", "安全提醒",
                "ACCOUNT", 7L, null, "/account/privacy");

        org.mockito.ArgumentCaptor<MallMemberMessage> captor = org.mockito.ArgumentCaptor.forClass(MallMemberMessage.class);
        verify(mapper).insert(captor.capture());
        assertEquals("/account/privacy", captor.getValue().getActionPath());
    }

    @Test
    void markAllReadWithoutCategoryMarksEveryBusinessConversation()
    {
        when(mapper.markAllRead(7L, null)).thenReturn(4);

        assertEquals(4, service.markAllRead(7L, null));
        verify(mapper).markAllRead(7L, null);
    }

    private MallMemberMessage message(Long id)
    {
        MallMemberMessage value = new MallMemberMessage();
        value.setMessageId(id); value.setMemberId(7L); value.setCategory("ORDER");
        value.setTitle("订单已创建"); value.setReadFlag("0");
        return value;
    }
}
