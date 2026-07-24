package com.ruoyi.web.controller.ticket;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.ticket.domain.TicketConfig;
import com.ruoyi.ticket.service.ITicketConfigService;

/**
 * 票务配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/config")
public class TicketConfigController extends BaseController
{
    @Autowired
    private ITicketConfigService ticketConfigService;

    @PreAuthorize("@ss.hasPermi('ticket:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketConfig ticketConfig)
    {
        startPage();
        List<TicketConfig> list = ticketConfigService.selectTicketConfigList(ticketConfig);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:config:export')")
    @Log(title = "票务配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketConfig ticketConfig)
    {
        List<TicketConfig> list = ticketConfigService.selectTicketConfigList(ticketConfig);
        ExcelUtil<TicketConfig> util = new ExcelUtil<TicketConfig>(TicketConfig.class);
        util.exportExcel(response, list, "票务配置数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(ticketConfigService.selectTicketConfigById(configId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:config:add')")
    @Log(title = "票务配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketConfig ticketConfig)
    {
        ticketConfig.setCreateBy(getUsername());
        return toAjax(ticketConfigService.insertTicketConfig(ticketConfig));
    }

    @PreAuthorize("@ss.hasPermi('ticket:config:edit')")
    @Log(title = "票务配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketConfig ticketConfig)
    {
        ticketConfig.setUpdateBy(getUsername());
        return toAjax(ticketConfigService.updateTicketConfig(ticketConfig));
    }

    @PreAuthorize("@ss.hasPermi('ticket:config:remove')")
    @Log(title = "票务配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(ticketConfigService.deleteTicketConfigByIds(configIds));
    }
}
