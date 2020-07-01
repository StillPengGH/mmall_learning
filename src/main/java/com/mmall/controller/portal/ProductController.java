package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
     *
     * @param productId 产品ID
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        return iProductService.productDetail(productId);
    }

    /**
     * 获取产品详情RESTful
     *
     * @param productId 产品ID
     */
    @RequestMapping(value = "/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetailRESTful(@PathVariable Integer productId) {
        return iProductService.productDetail(productId);
    }

    /**
     * 获取产品列表（搜索）
     *
     * @param keyword    搜索关键字 （required=false非必传）
     * @param categoryId 产品类型（required=false非必传）
     * @param pageNum    第几页（默认值：1）
     * @param pageSize   页大小（默认值：10）
     * @param orderBy    排序（默认值：空字符串）
     */
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                                   @RequestParam(value = "orderBy", defaultValue = "") String orderBy) {
        return iProductService.getListByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }

    /**
     * 获取产品列表（搜索）RESTful
     * http://localhost:8080/product/手机/100012/1/10/price_asc
     */
    @RequestMapping(value = "/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> getProductListRESTful(@PathVariable(value = "keyword") String keyword,
                                                          @PathVariable(value = "categoryId") Integer categoryId,
                                                          @PathVariable(value = "pageNum") Integer pageNum,
                                                          @PathVariable(value = "pageSize") Integer pageSize,
                                                          @PathVariable(value = "orderBy") String orderBy) {
        if(pageNum == null) pageNum = 1;
        if(pageSize == null) pageSize = 10;
        if(StringUtils.isBlank(orderBy))orderBy = "price_asc";
        return iProductService.getListByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }

    @RequestMapping(value = "/keyword/{keyword}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> getProductListRESTful(@PathVariable(value = "keyword") String keyword,
                                                          @PathVariable(value = "pageNum") Integer pageNum,
                                                          @PathVariable(value = "pageSize") Integer pageSize,
                                                          @PathVariable(value = "orderBy") String orderBy) {
        if(pageNum == null) pageNum = 1;
        if(pageSize == null) pageSize = 10;
        if(StringUtils.isBlank(orderBy))orderBy = "price_asc";
        return iProductService.getListByKeywordCategory(keyword, null, pageNum, pageSize, orderBy);
    }

    @RequestMapping(value = "/category/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> getProductListRESTful(@PathVariable(value = "categoryId") Integer categoryId,
                                                          @PathVariable(value = "pageNum") Integer pageNum,
                                                          @PathVariable(value = "pageSize") Integer pageSize,
                                                          @PathVariable(value = "orderBy") String orderBy) {
        if(pageNum == null) pageNum = 1;
        if(pageSize == null) pageSize = 10;
        if(StringUtils.isBlank(orderBy))orderBy = "price_asc";
        return iProductService.getListByKeywordCategory("", categoryId, pageNum, pageSize, orderBy);
    }
}
