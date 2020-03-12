package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Cart;
import com.mmall.vo.CartVo;
import net.sf.jsqlparser.schema.Server;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/11 9:58
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> deleteProduct(Integer userId,String productIds);

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> selectOrUnSelectAll(Integer userId,Integer checked);

    ServerResponse<CartVo> selectOrUnSelect(Integer userId,Integer productId,Integer checked);

    ServerResponse<Integer> selectCartProductCount(Integer userId);
}
