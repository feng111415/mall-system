package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.KbDocument;

public interface IKbDocumentService
{
    KbDocument selectKbDocumentById(Long documentId);

    List<KbDocument> selectKbDocumentList(KbDocument kbDocument);

    int insertKbDocument(KbDocument kbDocument);

    int updateKbDocument(KbDocument kbDocument);

    int deleteKbDocumentById(Long documentId);

    int deleteKbDocumentByIds(Long[] documentIds);
}