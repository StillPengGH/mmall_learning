package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 通用响应信息类
 *
 * @param <T>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    // 响应状态码
    private int status;
    // 响应信息
    private String msg;
    // 响应数据
    private T data;

    // 定义私有构造器
    private ServerResponse(int status) {
        this.status = status;
    }

    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    // 方法一：判断是否响应成功，成功返回true，失败false
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    // 方法二：开放get方法
    public int getStatus() {
        return status;
    }

    public String getMsg() { return msg; }

    public T getData() {
        return data;
    }

    // 创建获取成功的响应对象方法，状态码为0，即ResponseCode.SUCCESS.getCode()
    // 方法三：调用第一个构造器，返回一个响应对象（只有status属性）
    public static <T> ServerResponse<T> createBySuccess() {
        // 这里调用的就是私有构造器的第一个，参数为status
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    // 方法四：调用第四个构造器，返回一个响应对象（包含status和msg）
    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    // 方法五：调用第二个构造器，返回一个响应对象（包含status和data）
    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    // 方法六：调用第三个构造器，返回一个响应对象（包含status、msg、data）
    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    // 方法七：创建错误响应信息对象（默认返回：1,ERROR）
    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),
                ResponseCode.ERROR.getDesc());
    }

    // 方法八：创建错误响应信息对象（自定义错误信息）
    public static <T> ServerResponse<T> createByErrorMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), msg);
    }

    // 方法九：创建错误响应信息对象（自定义错误码和错误信息）
    public static <T> ServerResponse<T> createByErrorMessage(int errorCode, String errorMsg) {
        return new ServerResponse<T>(errorCode, errorMsg);
    }
}