package com.ruoyi.mall.cart.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.cart.domain.MallCartItem;

public interface MallCartMapper
{
    List<MallCartItem> selectItems(Long memberId);
    MallCartItem selectItemForUpdate(@Param("memberId") Long memberId, @Param("skuId") Long skuId);
    MallCartItem selectSkuSnapshot(Long skuId);
    int insertItemIgnore(MallCartItem item);
    int updateQuantity(@Param("memberId") Long memberId, @Param("skuId") Long skuId, @Param("quantity") int quantity);
    int updateSelected(@Param("memberId") Long memberId, @Param("skuId") Long skuId, @Param("selectedFlag") String selectedFlag);
    int deleteItem(@Param("memberId") Long memberId, @Param("skuId") Long skuId);
}
