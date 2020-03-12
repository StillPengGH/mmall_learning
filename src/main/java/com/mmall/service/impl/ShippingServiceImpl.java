package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/12 14:44
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse addShipping(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int resCount = shippingMapper.insert(shipping);
        if(resCount > 0){
            Map returnMap = Maps.newHashMap();
            returnMap.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",returnMap);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    @Override
    public ServerResponse<String> delShipping(Integer userId,Integer shippingId){
        int resCount = shippingMapper.deleteByUserIdShippingId(userId,shippingId);
        if(resCount>0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServerResponse<String> updateShipping(Integer userId,Shipping shipping){
        shipping.setUserId(userId);
        int resCount = shippingMapper.updateByUserIdShipping(shipping);
        if(resCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> detailShipping(Integer shippingId){
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无改地址");
        }
        return ServerResponse.createBySuccess(shipping);
    }

    @Override
    public ServerResponse<PageInfo> listShipping(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> resList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(resList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
