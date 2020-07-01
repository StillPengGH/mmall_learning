package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    // 查询产品列表
    List<Product> selectList();

    // 根据name和id搜索产品
    List<Product> selectByNameAndId(@Param(value = "productName") String productName,
                                    @Param(value = "productId") Integer productId);

    List<Product> selectByNameAndCategoryIds(@Param(value = "keyword") String keyword,
                                             @Param(value = "categoryIdList") List<Integer> categoryIdList);

    // 根据产品Id获取产品库存，为了订单中的产品被删除，库存为null所以用Integer，int无法为null
    Integer selectStockById(Integer id);
}