package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/12 14:43
 */
public interface IShippingService {
    ServerResponse addShipping(Integer userId, Shipping shipping);
    ServerResponse<String> delShipping(Integer userId, Integer shippingId);
    ServerResponse<String> updateShipping(Integer userId,Shipping shipping);
    ServerResponse<Shipping> detailShipping(Integer shippingId);
    ServerResponse<PageInfo> listShipping(Integer userId,int pageNum,int PageSize);
}
