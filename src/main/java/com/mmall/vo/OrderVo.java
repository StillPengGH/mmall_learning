package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单VO
 * @author Still
 * @version 1.0
 * @date 2020/3/26 15:46
 */
public class OrderVo {
    private Long orderNo;
    private BigDecimal payment; // 支付总额
    private Integer paymentType; // 支付方式
    private String paymentTypeDesc; // 支付方式描述
    private Integer postage; // 邮费
    private Integer status; // 订单状态
    private String statusDesc; // 状态描述
    private String paymentTime; // 支付时间
    private String sendTime; // 发货时间
    private String endTime; // 结束时间
    private String closeTime; // 交易关闭时间
    private String createTime; // 创建时间
    private List<OrderItemVo> orderItemListVo; // 订单明细Vo集合
    private String imageHost; // 图片host
    private Integer shippingId; // 收货地址id
    private String receiverName; // 收货人
    private ShippingVo shippingVo; // 收货地址vo

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentTypeDesc() {
        return paymentTypeDesc;
    }

    public void setPaymentTypeDesc(String paymentTypeDesc) {
        this.paymentTypeDesc = paymentTypeDesc;
    }

    public Integer getPostage() {
        return postage;
    }

    public void setPostage(Integer postage) {
        this.postage = postage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<OrderItemVo> getOrderItemListVo() {
        return orderItemListVo;
    }

    public void setOrderItemListVo(List<OrderItemVo> orderItemListVo) {
        this.orderItemListVo = orderItemListVo;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public Integer getShippingId() {
        return shippingId;
    }

    public void setShippingId(Integer shippingId) {
        this.shippingId = shippingId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public ShippingVo getShippingVo() {
        return shippingVo;
    }

    public void setShippingVo(ShippingVo shippingVo) {
        this.shippingVo = shippingVo;
    }
}
