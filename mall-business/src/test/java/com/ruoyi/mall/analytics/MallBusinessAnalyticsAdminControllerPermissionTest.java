package com.ruoyi.mall.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.mall.analytics.web.MallBusinessAnalyticsAdminController;

class MallBusinessAnalyticsAdminControllerPermissionTest
{
    @Test
    void endpointRequiresAnalyticsPermission() throws Exception
    {
        Method method = MallBusinessAnalyticsAdminController.class.getMethod("analytics", Integer.class);
        assertEquals("@ss.hasPermi('mall:analytics:list')", method.getAnnotation(PreAuthorize.class).value());
    }
}
