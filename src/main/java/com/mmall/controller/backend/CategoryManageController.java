package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
     * @param session HttpSession
     * @param categoryName 品类名称
     * @param parentId 品类的父id
     */
    @RequestMapping(value="/add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, String categoryName,
                                              @RequestParam(value = "parentId",defaultValue = "0") int parentId){
        // 判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
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
     * @param session HttpSession
     * @param categoryName 品类名称
     * @param categoryId 品列id
     */
    @RequestMapping(value="/set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session,String categoryName,int categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse<List<Category>> getChildrenByParentId(HttpSession session,
                                                                @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse getDeepChildrenByParentId(HttpSession session,
                                                    @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
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
