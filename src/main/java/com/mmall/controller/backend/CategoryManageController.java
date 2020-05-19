package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加品类
     * @param httpServletRequest
     * @param categoryName 品类名称
     * @param parentId 品类的父id
     */
    @RequestMapping(value="/add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpServletRequest httpServletRequest, String categoryName,
                                              @RequestParam(value = "parentId",defaultValue = "0") int parentId){
        // 判断是否登录
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        // 判断登录用户是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.addCategory(parentId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限。");
        }
    }

    /**
     * 更新品类名称
     * @param httpServletRequest
     * @param categoryName 品类名称
     * @param categoryId 品列id
     */
    @RequestMapping(value="/set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpServletRequest httpServletRequest,String categoryName,int categoryId){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        // 判断登录用户是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限。");
        }
    }

    /**
     * 通过categoryId获取子类（一级/平级）
     */
    @RequestMapping(value="/get_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenByParentId(HttpServletRequest httpServletRequest,
                                                                @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        // 判断登录用户是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.getChildrenByParentId(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限。");
        }
    }

    /**
     * 通过category获取所有子类（多级）
     */
    @RequestMapping(value="/get_deep_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getDeepChildrenByParentId(HttpServletRequest httpServletRequest,
                                                    @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        // 判断登录用户是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.getDeepChildrenByParentId(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限。");
        }
    }
}
