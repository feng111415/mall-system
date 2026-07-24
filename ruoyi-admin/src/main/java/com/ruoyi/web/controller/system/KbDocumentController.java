package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.KbDocument;
import com.ruoyi.system.service.IKbDocumentService;

@RestController
@RequestMapping("/knowledge/document")
public class KbDocumentController extends BaseController
{
    @Autowired
    private IKbDocumentService kbDocumentService;

    @GetMapping("/list")
    public TableDataInfo list(KbDocument kbDocument)
    {
        startPage();
        List<KbDocument> list = kbDocumentService.selectKbDocumentList(kbDocument);
        return getDataTable(list);
    }

    @GetMapping("/{documentId}")
    public AjaxResult getInfo(@PathVariable Long documentId)
    {
        return success(kbDocumentService.selectKbDocumentById(documentId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody KbDocument kbDocument)
    {
        return toAjax(kbDocumentService.insertKbDocument(kbDocument));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody KbDocument kbDocument)
    {
        return toAjax(kbDocumentService.updateKbDocument(kbDocument));
    }

    @DeleteMapping("/{documentIds}")
    public AjaxResult remove(@PathVariable Long[] documentIds)
    {
        return toAjax(kbDocumentService.deleteKbDocumentByIds(documentIds));
    }
}