package com.ruoyi.mall.member.web;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.service.IMallMemberService;
import com.ruoyi.mall.member.service.MallMemberAccountQueryService;

/** 若依后台会员管理接口，仅接受后台管理员 Token。 */
@RestController
@RequestMapping("/mall/member")
public class MallMemberAdminController extends BaseController
{
    private final IMallMemberService memberService;
    private final MallMemberAccountQueryService accountQueryService;

    public MallMemberAdminController(IMallMemberService memberService, MallMemberAccountQueryService accountQueryService)
    {
        this.memberService = memberService;
        this.accountQueryService = accountQueryService;
    }

    @PreAuthorize("@ss.hasPermi('mall:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallMember member)
    {
        startPage();
        List<MallMember> list = memberService.selectList(member);
        list.forEach(this::maskPhone);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:member:query')")
    @GetMapping("/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId)
    {
        MallMember member = memberService.selectById(memberId);
        return success(member == null ? null : maskPhone(member));
    }

    @PreAuthorize("@ss.hasPermi('mall:member:query')")
    @GetMapping("/{memberId}/account-lifecycle")
    public AjaxResult accountLifecycle(@PathVariable Long memberId)
    {
        return success(accountQueryService.overviewForAdmin(memberId));
    }

    @PreAuthorize("@ss.hasPermi('mall:member:edit')")
    @Log(title = "商城会员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody MallMember member)
    {
        member.setUpdateBy(getUsername());
        return toAjax(memberService.update(member));
    }

    @PreAuthorize("@ss.hasPermi('mall:member:remove')")
    @Log(title = "商城会员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberIds}")
    public AjaxResult remove(@PathVariable Long[] memberIds)
    {
        return toAjax(memberService.deleteByIds(memberIds));
    }

    private MallMember maskPhone(MallMember member)
    {
        String phone = member.getPhone();
        member.setPhone(phone != null && phone.length() == 11
                ? phone.substring(0, 3) + "****" + phone.substring(7) : "");
        return member;
    }
}
