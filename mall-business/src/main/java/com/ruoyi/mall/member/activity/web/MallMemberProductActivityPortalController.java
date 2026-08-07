package com.ruoyi.mall.member.activity.web;

import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.member.activity.domain.dto.MallBrowseHistoryDeleteRequest;
import com.ruoyi.mall.member.activity.service.MallMemberProductActivityService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@Anonymous
@RestController
@RequestMapping("/api/mall/member/product-activity")
public class MallMemberProductActivityPortalController
{
    private final MallMemberProductActivityService service;
    private final MallMemberTokenService tokenService;

    public MallMemberProductActivityPortalController(MallMemberProductActivityService service,
            MallMemberTokenService tokenService)
    { this.service = service; this.tokenService = tokenService; }

    @GetMapping("/summary")
    public AjaxResult summary(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.summary(tokenService.requireMemberId(authorization))); }

    @GetMapping("/favorites")
    public AjaxResult favorites(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.favorites(tokenService.requireMemberId(authorization))); }

    @GetMapping("/favorites/{spuId}/state")
    public AjaxResult favoriteState(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long spuId)
    { return AjaxResult.success(Map.of("favorited", service.isFavorite(tokenService.requireMemberId(authorization), spuId))); }

    @PostMapping("/favorites/{spuId}")
    public AjaxResult addFavorite(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long spuId)
    { return AjaxResult.success("收藏成功", service.addFavorite(tokenService.requireMemberId(authorization), spuId)); }

    @DeleteMapping("/favorites/{spuId}")
    public AjaxResult removeFavorite(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long spuId)
    { service.removeFavorite(tokenService.requireMemberId(authorization), spuId); return AjaxResult.success("已取消收藏"); }

    @GetMapping("/history")
    public AjaxResult history(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.history(tokenService.requireMemberId(authorization))); }

    @PostMapping("/history/{spuId}")
    public AjaxResult recordHistory(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long spuId)
    { return AjaxResult.success(service.recordHistory(tokenService.requireMemberId(authorization), spuId)); }

    @DeleteMapping("/history/{spuId}")
    public AjaxResult removeHistory(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long spuId)
    { service.removeHistory(tokenService.requireMemberId(authorization), spuId); return AjaxResult.success("浏览记录已删除"); }

    @PostMapping("/history/batch-delete")
    public AjaxResult removeHistoryBatch(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallBrowseHistoryDeleteRequest request)
    { return AjaxResult.success("浏览记录已删除", service.removeHistoryBatch(tokenService.requireMemberId(authorization), request.getSpuIds())); }

    @DeleteMapping("/history")
    public AjaxResult clearHistory(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success("浏览足迹已清空", service.clearHistory(tokenService.requireMemberId(authorization))); }
}
