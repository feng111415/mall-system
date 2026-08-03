package com.ruoyi.mall.aftersale.item;

import com.ruoyi.mall.aftersale.item.web.MallItemAfterSaleAdminController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MallItemAfterSaleAdminControllerPermissionTest
{
    @Test
    void separatesReviewAndRefundPermissions() throws NoSuchMethodException
    {
        PreAuthorize reviewPermission = MallItemAfterSaleAdminController.class
                .getMethod("approve", Long.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize refundPermission = MallItemAfterSaleAdminController.class
                .getMethod("refund", Long.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("@ss.hasPermi('mall:after-sale:audit')", reviewPermission.value());
        assertEquals("@ss.hasPermi('mall:after-sale:refund')", refundPermission.value());
    }
}
