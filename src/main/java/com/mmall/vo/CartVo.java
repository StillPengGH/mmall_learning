package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车的Vo
 * @author Still
 * @version 1.0
 * @date 2020/3/11 11:18
 */
public class CartVo {
    // 购物车中单个产品的Vo
    private List<CartProductVo> cartProductVoList;
    // 是否全选
    private Boolean allChecked;
    // 总价
    private BigDecimal cartTotalPrice;
    // 图片Host
    private String imgHost;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public String getImgHost() {
        return imgHost;
    }

    public void setImgHost(String imgHost) {
        this.imgHost = imgHost;
    }
}
