package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class KbDocument extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long documentId;
    private Long categoryId;
    private String categoryName;
    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    private String documentName;
    private String fileName;
    private String filePath;
    private String fileType;
    private String status;
    public Long getDocumentId() {
        return documentId;
    }
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public String getDocumentName() {
        return documentName;
    }
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
   
}
