package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 购物车模块
 *
 * @author Still
 * @version 1.0
 * @date 2020/3/11 9:54
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * 添加购物车
     *
     * @param httpServletRequest
     * @param productId 产品ID
     * @param count     产品数量
     * @return
     */
    @RequestMapping(value = "/add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> addCart(HttpServletRequest httpServletRequest, Integer productId, Integer count) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        // 添加到购物车，并获取购物车数据
        return iCartService.add(user.getId(), productId, count);
    }

    /**
     * 更新购物车（更新购物车中指定产品的数量）
     *
     * @param session
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping(value = "/update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> updateCart(HttpServletRequest httpServletRequest, Integer productId, Integer count) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(), productId, count);
    }

    /**
     * 删除购物车中商品（可多选）
     *
     * @param session
     * @param productIds 规定多个产品id用逗号分隔
     * @return
     */
    @RequestMapping(value = "/delete_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpServletRequest httpServletRequest, String productIds) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(), productIds);
    }

    /**
     * 查询用户购物车数据
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<CartVo> list(HttpServletRequest httpServletRequest) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    /**
     * 全选
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/select_all.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpServletRequest httpServletRequest) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelectAll(user.getId(), Const.Cart.CHECKED);
    }

    /**
     * 全部不选
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/un_select_all.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpServletRequest httpServletRequest) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelectAll(user.getId(), Const.Cart.UN_CHECKED);
    }

    /**
     * 指定商品单独选
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/select.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<CartVo> select(HttpServletRequest httpServletRequest, Integer productId) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.CHECKED);
    }

    /**
     * 指定商品单独不选
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/un_select.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpServletRequest httpServletRequest, Integer productId) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.UN_CHECKED);
    }

    /**
     * 查询用户购物车的产品数量
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/get_cart_product_count.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest httpServletRequest){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            // 如果为空 返回0即可
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.selectCartProductCount(user.getId());
    }
}
