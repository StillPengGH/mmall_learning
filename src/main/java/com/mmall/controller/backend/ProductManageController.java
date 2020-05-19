package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IFileService iFileService;

    /**
     * 新增或修改产品
     */
    @RequestMapping(value = "/save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse save(HttpServletRequest httpServletRequest, Product product) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 新增或修改产品
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 修改产品售卖状态
     */
    @RequestMapping(value = "/set_sale_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpServletRequest httpServletRequest, Integer productId, Integer status) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 修改产品售卖状态该
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取产品详细信息
     */
    @RequestMapping(value = "/detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getDetail(HttpServletRequest httpServletRequest, Integer productId) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 获取产品详情
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取产品列表（分页）
     *
     * @param httpServletRequest
     * @param pageNum  第几页 默认1
     * @param pageSize 一页多少条 默认10
     * @return SeverResponse<PageInfo>
     */
    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getList(HttpServletRequest httpServletRequest,
                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 获取产品列表
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 产品搜索
     *
     * @param httpServletRequest
     * @param productName 产品名称
     * @param productId   产品ID
     * @param pageNum     第几页
     * @param pageSize    一页多少条
     * @return ServerResponse<PageInfo>
     */
    @RequestMapping(value = "/search.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getSearch(HttpServletRequest httpServletRequest, String productName, Integer productId,
                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 获取产品列表
            return iProductService.productSearch(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 图片上传
     *
     * @param file    MultipartFile是springMVC的文件上传类
     * @param request 根据servlet上下文，创建相对路径
     */
    @RequestMapping(value = "/upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpServletRequest httpServletRequest,
                                 @RequestParam(value = "upload_file", required = false) MultipartFile file,
                                 HttpServletRequest request) {
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(userLoginToken)) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录");
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // tomcat 相对路径：webapp/upload，我们将上传的图片存储在这个文件夹下
            String path = request.getSession().getServletContext().getRealPath("upload");
            // 上传文件
            String returnUrl = iFileService.upload(file, path);
            // 包装返回信息
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", returnUrl);
            fileMap.put("url", PropertiesUtil.getProperty("ftp.server.http.prefix") + returnUrl);
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 富文本中图片上传（Simditor为例）
     *
     * @param file
     * @param request
     * @return Map
     */
    @RequestMapping(value = "/richtext_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response) {
        Map resMap = Maps.newHashMap();
        // 判断登录状态
        // 从Cookie获取loginToken,通过loginToken获取redis中用户信息字符串，并反序列化为User对象
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            resMap.put("success", false);
            resMap.put("msg", "请登录管理员");
            return resMap;
        }
        String userJsonStr = RedisPoolUtil.get(userLoginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            resMap.put("success", false);
            resMap.put("msg", "请登录管理员");
            return resMap;
        }
        // 判断是否是管理员登录
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // tomcat 相对路径：webapp/upload，我们将上传的图片存储在这个文件夹下
            String path = request.getSession().getServletContext().getRealPath("upload");
            // 上传文件
            String returnUrl = iFileService.upload(file, path);
            if (StringUtils.isBlank(returnUrl)) {
                resMap.put("success", false);
                resMap.put("msg", "上传失败");
                return resMap;
            }
            // 成功
            resMap.put("success", true);
            resMap.put("msg", "上传成功");
            resMap.put("file_path", PropertiesUtil.getProperty("ftp.server.http.prefix") + returnUrl);
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resMap;
        } else {
            resMap.put("success", false);
            resMap.put("msg", "无权限操作");
            return resMap;
        }
    }
}
