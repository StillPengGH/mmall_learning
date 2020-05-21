package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

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
     * @param httpServletRequest
     * @param shipping SpringMVC的对象绑定
     * @return
     */
    @RequestMapping(value = "/add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> add(HttpServletRequest httpServletRequest, Shipping shipping){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.addShipping(user.getId(),shipping);
    }

    /**
     * 删除收货地址
     * @param httpServletRequest
     * @param shippingId 地址ID
     * @return
     */
    @RequestMapping(value = "/del.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> del(HttpServletRequest httpServletRequest,Integer shippingId){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.delShipping(user.getId(),shippingId);
    }

    /**
     * 修改收货地址
     * @param httpServletRequest
     * @param shipping
     * @return
     */
    @RequestMapping(value = "/update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> update(HttpServletRequest httpServletRequest,Shipping shipping){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.updateShipping(user.getId(),shipping);
    }

    /**
     * 根据shippingId查询地址详情
     * @param httpServletRequest
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "/detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Shipping> detail(HttpServletRequest httpServletRequest,Integer shippingId){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.detailShipping(shippingId);
    }

    /**
     * 收货地址列表（分页）
     * @param httpServletRequest
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpServletRequest httpServletRequest,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.listShipping(user.getId(),pageNum,pageSize);
    }
}
