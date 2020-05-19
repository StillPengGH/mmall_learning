package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/user")
public class UserManageController {
    @Autowired
    private IUserService iUserService;

    /**
     * 后台用户登录（管理员）
     */
    @RequestMapping(value = "/login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(HttpServletResponse httpServletResponse,
                                      HttpSession session,
                                      String username,
                                      String password){
        ServerResponse<User> response = iUserService.login(username, password);
        if(response.isSuccess()){
            User user = response.getData();
            if(user.getRole() == Const.Role.ROLE_ADMIN){
                // 一期：说明是管理员登录，将user存入session
                // session.setAttribute(Const.CURRENT_USER,user);
                // 二期：在cookie中记录登录token（登录时的JSESSIONID）
                CookieUtil.writeLoginToken(httpServletResponse, session.getId());
                // 将登录信息保存到Redis中
                RedisPoolUtil.setEx(session.getId(), // sessionID
                        JsonUtil.obj2String(response.getData()), // 对User进行序列化
                        Const.RedisCacheExTime.REDIS_SESSION_EX_TIME); // 过期时间
                return response;
            }else{
                return ServerResponse.createByErrorMessage("不是管理员，无法登录");
            }
        }
        return response;
    }
}
