package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
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
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password,
                                      HttpSession session,
                                      HttpServletResponse httpServletResponse,
                                      HttpServletRequest httpServletRequest) {
        ServerResponse<User> response = iUserService.login(username, password);
        // 登录成功
        if (response.isSuccess()) {
            // 在cookie中记录登录token（登录时的JSESSIONID）
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            // 将登录信息保存到Redis中
            RedisPoolUtil.setEx(session.getId(), // sessionID
                    JsonUtil.obj2String(response.getData()), // 对User进行序列化
                    Const.RedisCacheExTime.REDIS_SESSION_EX_TIME); // 过期时间
        }
        return response;
    }

    /**
     * 注销
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        // 将session缓存中的当前用户信息清空即可
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 注册
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 验证用户名和邮箱
     *
     * @param str  验证的内容
     * @param type 判断是用户名还是邮箱
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    /**
     * 获取用户信息
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }

    /**
     * 忘记密码，通过username获取忘记密码提示问题
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 检查找回密码答案是否正确
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 重置密码，根据获取的token和用户名
     *
     * @param username    用户名
     * @param passwordNew 新密码
     * @param forgetToken 找回密码答案时，存入缓存的token
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登录状态下，重置密码
     *
     * @param session     session
     * @param passwordOld 老密码
     * @param passwordNew 新密码
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        // 判断是否是登录状态
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        // 登录状态
        return iUserService.resetPassword(user, passwordOld, passwordNew);
    }

    /**
     * 更新用户信息
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user) {
        // 判断用户是否登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        // 获取当前用户的id和用户名，存入user中,更新的为当前登录用户的信息
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        // 调用Service层的更新用户信息方法
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            // 响应的User里没有username，需要set到data中
            response.getData().setUsername(currentUser.getUsername());
            // 将更新后的User放入session中
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 获取当前用户详细信息
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session) {
        // 判断用户是否登录，需要强制登录。返回状态码应该为10
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage(
                    ResponseCode.NEED_LOGIN.getCode(),
                    "未登录，需要强行登录status=10");
        }
        // 登录状态，获取详细信息
        return iUserService.getInformation(currentUser.getId());
    }
}
