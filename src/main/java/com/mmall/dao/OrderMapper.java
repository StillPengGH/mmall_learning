package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByOrderNoUserId(@Param("orderNo") Long orderNo,
                                @Param("userId") Integer userId);

    Order selectByOrderNo(Long orderNo);

    List<Order> selectOrderListByUserId(Integer userId);

    List<Order> selectOrderList();

    // 定时关单：查询未支付状态、创建时间小于指定时间的订单列表
    List<Order> selectOrderStatusByCreateTime(@Param("status") Integer status,
                                              @Param("date") String date);

    // 定时关单：根据orderId将订单设置为关闭状态
    int closeOrderByOrderId(Integer id);
}