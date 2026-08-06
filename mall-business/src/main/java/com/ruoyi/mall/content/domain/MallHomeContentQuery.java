package com.ruoyi.mall.content.domain;

import java.io.Serializable;

public class MallHomeContentQuery implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String contentType;
    private String status;
    private String keyword;

    public String getContentType() { return contentType; }
    public void setContentType(String value) { contentType = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
}
