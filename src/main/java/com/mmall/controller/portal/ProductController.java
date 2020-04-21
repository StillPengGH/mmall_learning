package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/10 9:01
 */

@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /**
     * 获取产品详情
     * @param productId 产品ID
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        return iProductService.productDetail(productId);
    }

    /**
     * 获取产品列表（搜索）
     * @param keyword 搜索关键字 （required=false非必传）
     * @param categoryId 产品类型（required=false非必传）
     * @param pageNum 第几页（默认值：1）
     * @param pageSize 页大小（默认值：10）
     * @param orderBy 排序（默认值：空字符串）
     */
    @RequestMapping(value = "list.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(@RequestParam(value = "keyword",required = false) String keyword,
                                                   @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                                   @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                   @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                   @RequestParam(value = "orderBy",defaultValue = "") String orderBy){
        return iProductService.getListByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
