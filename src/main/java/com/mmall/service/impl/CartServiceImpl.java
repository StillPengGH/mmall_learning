package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/11 9:59
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 判断当前用户在购物车里是否已经有了当前产品
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart == null) {
            // 购物车里没有此产品
            Cart newCart = new Cart();
            newCart.setProductId(productId);
            newCart.setUserId(userId);
            newCart.setQuantity(count);
            newCart.setChecked(Const.Cart.CHECKED); // 默认为选中状态
            // 添加到购物车
            cartMapper.insert(newCart);
        } else {
            // 购物车里存在该产品，更改数量
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            // 更新购物车
            cartMapper.updateByPrimaryKeySelective(cart);
        }

        // 获取购物车Vo数据进行返回
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 判断购物车里是否存在该商品
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        // 返回购物车数据
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        // 使用Guava的Splitter方法对productIds进行处理
        List<String> productIdList = Splitter.on(",").splitToList(productIds);

        if (CollectionUtils.isEmpty(productIdList)) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 删除购物车中商品
        cartMapper.deleteByUserIdProductIds(userId, productIdList);
        // 返回CartVo数据
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoAndLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse<CartVo> selectOrUnSelectAll(Integer userId, Integer checked) {
        if (checked == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 更新全选或反选状态
        cartMapper.updateSelectOfUnSelectAll(userId, checked);
        // 获取数据
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        if (checked == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.updateSelectOfUnSelect(userId, productId, checked);
        // 获取数据
        return this.list(userId);
    }

    @Override
    public ServerResponse<Integer> selectCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    // 装配CartVo数据
    private CartVo getCartVoAndLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        // 购物车所有商品总价
        BigDecimal cartTotalPrice = new BigDecimal("0");

        // 根据用户id查询该用户所有购物车中数据
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                // 根据cartItem中的productId查询产品信息
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubTitle(product.getSubtitle());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    // 判断产品库存（产品表中）是否大于购物车该产品的数量（购物车表中）
                    int buyLimitCount = 0; // 购买数量限制
                    if (product.getStock() >= cartItem.getQuantity()) {
                        // 库存大于购买量
                        buyLimitCount = cartItem.getQuantity();
                        // 提示前端可以符合购买限制条件，即购买数没有超过库存
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        // 库存小于购买数量,将购买数量赋值为库存数量
                        buyLimitCount = product.getStock();
                        // 提示前端不符合购买条件数量，即超过了库存
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        // 将购物车中此商品的购买数量更改为有效库存
                        Cart updateCart = new Cart();
                        updateCart.setId(cartItem.getId());
                        updateCart.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(updateCart);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    // 计算单个产品的总价：产品价格*数量
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(
                            product.getPrice().doubleValue(),
                            cartProductVo.getQuantity().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                // 如果当前产品是被选中的状态，添加到整个购物车的总价
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(
                            cartTotalPrice.doubleValue(),
                            cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImgHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    // 判断userId的购物车是否是全选状态
    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartAllCheckedStatusByUserId(userId) == 0;
    }

}
