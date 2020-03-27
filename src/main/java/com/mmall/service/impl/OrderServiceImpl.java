package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/13 10:53
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    public static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        // 根据userID获取当前用户购物车里被勾选产品集合list
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        // 根据勾选的购物车List，装配List<OrderItem>
        ServerResponse serverResponse = this.getOrderItemList(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        // 在serverResponse获取orderItemList
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        // 根据orderItemList获取订单总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        // 生成订单
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        // 将生成的订单号循环的set到orderItem
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        // 将List<OrderItem>批量插入数据库
        orderItemMapper.batchInsert(orderItemList);

        // 生成订单成功后，减少产品库存
        this.reduceProductStock(orderItemList);

        // 清空购物车
        this.cleanCart(cartList);

        // 装配返给前端的数据OrderVo
        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<String> cancelOrder(Integer userId, Long orderNo) {
        // 查询该用户下是否存在该订单
        Order order = orderMapper.selectByOrderNoUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }
        // 如果订单不是未支付状态
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        }
        // 订单改为已取消状态
        Order updateOrder = new Order();
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        updateOrder.setId(order.getId());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();

        // 获取购物车中选中的数据集合
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);

        // 获取orderItem
        ServerResponse serverResponse = this.getOrderItemList(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            // 计算总价
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            // 根据orderItem组装orderItemVo
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }

        // 组装OrderProductVo
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse detailOrder(Integer userId,Long orderNo){
        Order order = orderMapper.selectByOrderNoUserId(orderNo,userId);
        if(order == null){
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
        // 根据order和orderItemList装配OrderVo
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> listOrder(Integer userId,int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectOrderListByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if( userId == null){
                // todo 管理员登录不需要传userId
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    // 装配OrderVo
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemListVo(orderItemVoList);
        return orderVo;
    }

    // 装配OrderItemVo
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    // 装备shippingVo
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    // 清空购物车
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    // 减少产品的库存
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    // 生成订单
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        // 生成订单号
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        // 设置订单状态为“未付款”状态
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        // 设置邮寄费，包邮设置为0
        order.setPostage(0);
        // 支付方式：在线支付
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        // 订单总价
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    // 生成订单号
    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    // 获取订单产品的总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            totalPrice = BigDecimalUtil.add(
                    totalPrice.doubleValue(),
                    orderItem.getTotalPrice().doubleValue()
            );
        }
        return totalPrice;
    }

    // 装配orderItemList
    private ServerResponse getOrderItemList(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车中勾选商品为空");
        }
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();
            // 根据productId获取Product
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            // 判断当前产品是否是售卖状态
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "不在线售卖状态");
            }
            // 校验购买该产品的数量是否大于产品的库存
            if (cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),
                    cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    @Override
    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        // 返回结果
        Map<String, String> resultMap = Maps.newHashMap();
        // 根据userId和OrderNo获取订单信息
        Order order = orderMapper.selectByOrderNoUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有该订单信息");
        }
        // 将orderNo放入resultMap
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // 组装请求数据，请求支付宝，获得相应二维码
        // 请求数据1：订单号(必填)，商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // 请求数据2：订单标题(必填) 粗略描述用户的支付目的。
        String subject = new StringBuffer()
                .append("HappyMMall扫码支付，订单号：")
                .append(outTradeNo).toString();

        // 请求数据3：订单总金额(必填)，单位为元，不能超过1亿元
        String totalAmount = order.getPayment().toString();

        // 请求数据4：订单不可打折金额(可选) ，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 请求数据5：卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 请求数据6：订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuffer().append("订单")
                .append(outTradeNo).append("购买商品同")
                .append(totalAmount)
                .append("元").toString();

        // 请求数据7：商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // 请求数据8： 商户门店编号(必填)，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 请求数据9：业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 请求数据10：支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 请求数据11：商品明细列表，需填写购买商品详细信息
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
        for (OrderItem orderItem : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分，数据库中数据要乘以100）、数量。
            GoodsDetail goods1 = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods1);
        }

        // 创建扫码支付请求builder，即将我们上面的数据设置为请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))
                .setGoodsDetailList(goodsDetailList);


        /**
         * 创建当面付服务
         * 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，
         *  如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");
        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        // 通过支付服务进行支付宝下单
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                // 打印响应信息
                dumpResponse(response);

                // 如果下单成功，生成二维码，保存到服务器
                // 判断本地是否存在upload目录，没有就创建一个
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 二维码本地路径，upload/qr-订单号.png（%s）会被后面的response.getOutTradeNo替换。
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                // 获取二维码，将二维码保存到本地upload文件夹下
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                // 将本地二维码图片上传到ftp服务器上
                String qrFileName = String.format("/qr-%s.png", response.getOutTradeNo());
                File targetFile = new File(path + qrFileName);
                try {
                    // 上传ftp服务器
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常");
                }
                logger.info("qrPath:" + qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");
            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    @Override
    public ServerResponse aliCallBack(Map<String, String> params) {
        // 获取订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        // 获取交易状态
        String tradeStatus = params.get("trade_status");
        // 获取交易号
        String tradeNo = params.get("trade_no");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("非商城订单，回调忽略");
        }
        // 如果订单状态大于20（已支付），证明是重复调用，不向下执行
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        // 判断交易状态(如果交易成功)
        if (Const.AlipayCallBack.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            // 更改order表中的订单状态为已支付
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            // 更改order表中的付款时间
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 组装PayInfo对象
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId()); // 订单支付的用户
        payInfo.setOrderNo(order.getOrderNo()); // 订单编号
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode()); // 支付平台
        payInfo.setPlatformNumber(tradeNo); // 交易号
        payInfo.setPlatformStatus(tradeStatus); // 交易状态
        // 插入数据库
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        // 如果订单状态大于已支付，就返回true
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    // ========backend================
    @Override
    public ServerResponse<PageInfo> manageOrderList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectOrderList();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<OrderVo> manageOrderDetail(long orderNo){
        // 根据orderNo获取Order
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            // 根据orderNo获取OrderItem集合
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            // 根据order和orderItemList装配orderVo
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    @Override
    public ServerResponse<PageInfo> manageOrderSearch(long orderNo,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    @Override
    public ServerResponse<String> manageSendGoods(long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            // 判断订单是不是已付款状态
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccessMessage("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }
}
