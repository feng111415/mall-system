package com.ruoyi.mall.review.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.member.service.MallMemberTokenService;
import com.ruoyi.mall.review.domain.dto.MallProductReviewRequest;
import com.ruoyi.mall.review.service.MallProductReviewImageStorageService;
import com.ruoyi.mall.review.service.MallProductReviewService;

@Anonymous
@RestController
@RequestMapping("/api/mall/reviews")
public class MallProductReviewPortalController
{
    private final MallProductReviewService service;
    private final MallProductReviewImageStorageService imageStorage;
    private final MallMemberTokenService tokenService;

    public MallProductReviewPortalController(MallProductReviewService service,
            MallProductReviewImageStorageService imageStorage, MallMemberTokenService tokenService)
    {
        this.service = service; this.imageStorage = imageStorage; this.tokenService = tokenService;
    }

    @GetMapping("/products/{spuId}")
    public AjaxResult productReviews(@PathVariable Long spuId,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset)
    { return AjaxResult.success(service.publishedProduct(spuId, limit, offset)); }

    @GetMapping("/orders/{orderId}/items")
    public AjaxResult orderItems(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long orderId)
    { return AjaxResult.success(service.memberOrderItems(tokenService.requireMemberId(authorization), orderId)); }

    @PostMapping
    public AjaxResult submit(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallProductReviewRequest request)
    { return AjaxResult.success("评价已提交，审核通过后展示", service.submit(tokenService.requireMemberId(authorization), request)); }

    @PostMapping("/images")
    public AjaxResult uploadImage(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestPart("file") MultipartFile file)
    { return AjaxResult.success("图片上传成功", imageStorage.store(tokenService.requireMemberId(authorization), file)); }
}
