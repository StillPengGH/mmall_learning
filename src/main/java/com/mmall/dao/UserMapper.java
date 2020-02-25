package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    // 检查是否存在指定用户名的用户
    int checkUsername(String username);

    // 检查邮箱是否存在
    int checkEmail(String email);

    // 登录，通过用户名和密码查询该用户信息
    User selectLogin(@Param("username") String username,@Param("password") String password);

    // 通过username查询找回密码问题
    String selectQuestionByUsername(String username);

    // 检查找回密码答案是否正确
    int checkAnswer(@Param("username") String username,
                    @Param("question") String question,
                    @Param("answer") String answer);

    // 重置密码
    int resetPasswordByUsername(@Param("username")String username,@Param("passwordNew")String passwordNew);

    // 根据用户id查询旧密码是否正确
    int checkPasswordOld(@Param("userId")Integer userId,@Param("passwordOld")String passwordOld);

    // 检查email除了自己以外，其他人是否使用
    int checkEmailByUserId(@Param("email")String email,@Param("userId")Integer userId);
}