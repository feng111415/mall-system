package com.ruoyi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.UserException;
import com.ruoyi.framework.web.exception.GlobalExceptionHandler;
import com.ruoyi.framework.web.response.HttpStatusResponseBodyAdvice;
import org.junit.jupiter.api.Test;

class HttpStatusResponseContractTest
{
    @Test
    void mapsLegacyAjaxErrorCodeToHttpStatus()
    {
        assertEquals(401, HttpStatusResponseBodyAdvice.resolveStatus(AjaxResult.error(401, "登录已失效")));
        assertEquals(403, HttpStatusResponseBodyAdvice.resolveStatus(AjaxResult.error(403, "没有权限")));
        assertEquals(404, HttpStatusResponseBodyAdvice.resolveStatus(AjaxResult.error(404, "不存在")));
        assertEquals(500, HttpStatusResponseBodyAdvice.resolveStatus(AjaxResult.error("失败")));
    }

    @Test
    void leavesSuccessAndLegacyWarningAsHttp200()
    {
        assertNull(HttpStatusResponseBodyAdvice.resolveStatus(AjaxResult.success()));
        assertNull(HttpStatusResponseBodyAdvice.resolveStatus(AjaxResult.warn("提示")));
        assertNull(HttpStatusResponseBodyAdvice.resolveStatus("plain text"));
    }

    @Test
    void serviceErrorsDefaultToBadRequestButPreserveExplicitNotFound()
    {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        assertEquals(400, handler.handleServiceException(new ServiceException("参数无效"), null).get("code"));
        assertEquals(404, handler.handleServiceException(new ServiceException("订单不存在", 404), null).get("code"));
    }

    @Test
    void userInputErrorsReturnBadRequestInsteadOfInternalServerError()
    {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        UserException error = new UserException("ignored", null)
        {
            private static final long serialVersionUID = 1L;

            @Override
            public String getMessage()
            {
                return "验证码已失效";
            }
        };

        AjaxResult result = handler.handleUserException(error);
        assertEquals(400, result.get("code"));
        assertEquals("验证码已失效", result.get("msg"));
    }
}
