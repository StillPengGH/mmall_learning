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
    List<Product> selectByNameAndId(@Param(value="productName") String productName,
                                    @Param(value="productId") Integer productId);
}