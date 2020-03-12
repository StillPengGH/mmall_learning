package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 收货地址管理
 * @author Still
 * @version 1.0
 * @date 2020/3/12 14:43
 */
@Controller
@RequestMapping("/shipping")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    /**
     * 增加收货地址
     * @param session
     * @param shipping SpringMVC的对象绑定
     * @return
     */
    @RequestMapping(value = "/add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> add(HttpSession session, Shipping shipping){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.addShipping(user.getId(),shipping);
    }

    /**
     * 删除收货地址
     * @param session
     * @param shippingId 地址ID
     * @return
     */
    @RequestMapping(value = "/del.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> del(HttpSession session,Integer shippingId){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.delShipping(user.getId(),shippingId);
    }

    /**
     * 修改收货地址
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "/update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> update(HttpSession session,Shipping shipping){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.updateShipping(user.getId(),shipping);
    }

    /**
     * 根据shippingId查询地址详情
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "/detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Shipping> detail(HttpSession session,Integer shippingId){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.detailShipping(shippingId);
    }

    /**
     * 收货地址列表（分页）
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.listShipping(user.getId(),pageNum,pageSize);
    }
}
