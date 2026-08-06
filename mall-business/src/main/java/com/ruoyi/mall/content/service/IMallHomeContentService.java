package com.ruoyi.mall.content.service;

import java.time.LocalDateTime;
import java.util.List;
import com.ruoyi.mall.content.domain.MallHomeContent;
import com.ruoyi.mall.content.domain.MallHomeContentQuery;
import com.ruoyi.mall.content.domain.MallHomepageView;

public interface IMallHomeContentService
{
    MallHomepageView selectPublishedHomepage(LocalDateTime now);
    List<MallHomeContent> selectAdminContentList(MallHomeContentQuery query);
    MallHomeContent selectContentById(Long contentId);
    int saveContent(MallHomeContent content);
    int disableContent(Long contentId, String updateBy);
}
