package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;
import net.sf.jsqlparser.schema.Server;

import java.util.Map;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/13 10:53
 */
public interface IOrderService {
    ServerResponse createOrder(Integer userId,Integer shippingId);
    ServerResponse<String> cancelOrder(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse detailOrder(Integer userId,Long OrderNo);
    ServerResponse<PageInfo> listOrder(Integer userId,int pageNum,int pageSize);
    ServerResponse pay(Long orderNo,Integer userId,String path);
    ServerResponse aliCallBack(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    // backend 后台
    ServerResponse<PageInfo> manageOrderList(int pageNum,int pageSize);
    ServerResponse<OrderVo> manageOrderDetail(long orderNo);
    ServerResponse<PageInfo> manageOrderSearch(long orderNo,int pageNum,int pageSize);
    ServerResponse<String> manageSendGoods(long orderNo);

    // 定时关闭订单，hour个小时内没有付款的订单，进行关闭
    void closeOrder(int hour);
}
