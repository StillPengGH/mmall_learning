package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/13 10:53
 */
public interface IOrderService {
    ServerResponse pay(Long orderNo,Integer userId,String path);
    ServerResponse aliCallBack(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
}
