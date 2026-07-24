package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.KbDocument;
import com.ruoyi.system.mapper.KbDocumentMapper;
import com.ruoyi.system.service.IKbDocumentService;

@Service
public class KbDocumentServiceImpl implements IKbDocumentService
{
    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @Override
    public KbDocument selectKbDocumentById(Long documentId)
    {
        return kbDocumentMapper.selectKbDocumentById(documentId);
    }

    @Override
    public List<KbDocument> selectKbDocumentList(KbDocument kbDocument)
    {
        return kbDocumentMapper.selectKbDocumentList(kbDocument);
    }

    @Override
    public int insertKbDocument(KbDocument kbDocument)
    {
        kbDocument.setCreateTime(DateUtils.getNowDate());
        return kbDocumentMapper.insertKbDocument(kbDocument);
    }

    @Override
    public int updateKbDocument(KbDocument kbDocument)
    {
        kbDocument.setUpdateTime(DateUtils.getNowDate());
        return kbDocumentMapper.updateKbDocument(kbDocument);
    }

    @Override
    public int deleteKbDocumentById(Long documentId)
    {
        return kbDocumentMapper.deleteKbDocumentById(documentId);
    }

    @Override
    public int deleteKbDocumentByIds(Long[] documentIds)
    {
        return kbDocumentMapper.deleteKbDocumentByIds(documentIds);
    }
}