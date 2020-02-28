package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IUserService iUserService;

    /**
     * 新增或修改产品
     */
    @RequestMapping(value="/save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse save(HttpSession session, Product product){
        // 判断登录状态
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        // 判断是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 新增或修改产品
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 修改产品售卖状态
     */
    @RequestMapping(value="/save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session,Integer productId,Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        // 判断是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 修改产品售卖状态该
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取产品详细信息
     */
    @RequestMapping(value="/save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getDetail(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        // 判断是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 获取产品详情
            return iProductService.manageProductDetail(productId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
}
