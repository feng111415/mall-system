package com.ruoyi.mall.web.controller;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.content.service.IMallHomeContentService;

@Anonymous
@RestController
@RequestMapping("/api/mall/homepage")
public class MallHomeContentPortalController
{
    private final IMallHomeContentService service;

    public MallHomeContentPortalController(IMallHomeContentService service) { this.service = service; }

    @GetMapping
    public AjaxResult homepage() { return AjaxResult.success(service.selectPublishedHomepage(LocalDateTime.now())); }
}
