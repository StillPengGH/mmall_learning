package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        // 检查用户名是否存在
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        // 将明文密码进行MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        // 通过用户名密码查询用户，将查询的User对象放入相应对象的data里。
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        // 如果存在用户，将user中的密码设置为空，并将user放入响应对象中
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServerResponse<String> register(User user){
        // 检查用户名是否存在
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        // 设置用户权限（普通用户）
        user.setRole(Const.Role.ROLE_CUSTOMER);
        // 对密码进行MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str,String type){
        // 如果type不为空
        if(StringUtils.isNotBlank(type)){
            // 如果type等于username，判断用户名是否存在
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            // 如果type等于email，判断邮箱是否存在
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username){
        // 判断用户名是否存在
        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        // 通过username查询question
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题为空");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
            // 说明问题及问题的答案是正确的,创建token，并存入本地缓存
            String forgetToken = UUID.randomUUID().toString(); // 创建不重复字符串

            // 改造前：将生成的token放入缓存，key值为token_（前缀）+ username （本地缓存）
            // TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);

            // 改造后（远程数据服务Redis存储）
            RedisPoolUtil.setEx(Const.TOKEN_PREFIX+username,forgetToken,60*60*12);
            // 将token返回给前端
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        // 判断token是否为空
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token不能为空");
        }
        // 判断用户是否存在
        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        // 改造前： 获取本地缓存中的该用户的token
        //String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        // 改造后：从redis中读取forgetToken
        String token = RedisPoolUtil.get(Const.TOKEN_PREFIX+username);

        // 如果token不存在，无效或过期
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或已过期");
        }
        // 使用forgetToken和缓存中的进行对比，为true证明相等
        if(StringUtils.equals(forgetToken,token)){
            // 对新密码进行md5加密，并更新数据库
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.resetPasswordByUsername(username,md5Password);
            if(resultCount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(User user,String passwordOld,String passwordNew){
        // 查询旧密码是否正确
        int resultCount = userMapper.checkPasswordOld(user.getId(),MD5Util.MD5EncodeUtf8(passwordOld));
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        // 修改密码
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        // 根据主键选择性的更新字段
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user){
        // 不更新用户名
        // 检查要更新的email是否在数据库中已经存在（除了自己以外其他用户）
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServerResponse.createByErrorMessage("email已存在,请更换");
        }
        // 包装需要更新的User
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        // 调用选择性更新方法
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount>0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(int userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        // 将password设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 判断是否是管理员登录（后台backend）
     */
    @Override
    public ServerResponse checkAdminRole(User user){
        if(user !=null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
