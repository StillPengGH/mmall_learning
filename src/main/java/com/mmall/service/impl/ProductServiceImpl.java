package com.mmall.service.impl;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        if (product != null) {
            // 设置主图，将子图以逗号分隔，取第一个为图设置为主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImages = product.getSubImages().split(",");
                if (subImages.length > 0) {
                    product.setMainImage(subImages[0]);
                }
            }
            // 判断新增 or 更新
            if (product.getId() != null) {
                // 更新操作
                int updateCount = productMapper.updateByPrimaryKey(product);
                if (updateCount > 0) {
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }
                return ServerResponse.createBySuccessMessage("更新产品失败");
            } else {
                // 新增操作
                int insertCount = productMapper.insert(product);
                if (insertCount > 0) {
                    return ServerResponse.createBySuccessMessage("新增产品成功");
                }
                return ServerResponse.createBySuccessMessage("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数错误");
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        // 判断参数
        if (productId == null || status == null) {
            // 非法参数
            return ServerResponse.createByErrorMessage(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 选择性更新数据
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int updateCount = productMapper.updateByPrimaryKeySelective(product);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("更新售卖状态成功");
        }
        return ServerResponse.createByErrorMessage("更新售卖状态失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品下架或已删除");
        }
        // 装配ProductDetailVo
        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    // 装配ProductDetailVo
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        // 来自Product
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubTitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        // imageHost，从配置文件获取，配置和代码分离
        productDetailVo.setImageHost(PropertiesUtil.getProperty(
                "ftp.server.http.prefix",
                "http://img.happymmall.com/"));
        // 设置parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0); // 设置根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        // 设置createTime和updateTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }
}
