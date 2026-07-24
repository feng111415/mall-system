package com.ruoyi.mall.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 商城模块健康检查接口。
 *
 * <p>该接口只返回模块自身的就绪信息，不执行数据库或第三方服务探测，
 * 避免健康检查请求对业务系统产生副作用。</p>
 */
@RestController
@RequestMapping("/api/mall/health")
@Anonymous
public class MallHealthController
{
    /**
     * 返回商城模块的基础就绪状态。
     *
     * @return 若依统一响应对象
     */
    @GetMapping
    public AjaxResult health()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("module", "mall-business");
        data.put("status", "UP");
        data.put("version", "0.1.0");
        return AjaxResult.success("商城模块运行正常", data);
    }
}
