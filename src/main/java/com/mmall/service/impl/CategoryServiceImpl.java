package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<String> addCategory(Integer parentId,String categoryName){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); // 是否可用，默认true

        int rowCount = categoryMapper.insert(category);
        if(rowCount>0){
            return ServerResponse.createByErrorMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse<String> updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return ServerResponse.createBySuccessMessage("更新品类成功");
        }
        return ServerResponse.createByErrorMessage("更新品类失败");
    }

    @Override
    public  ServerResponse<List<Category>> getChildrenByParentId(Integer categoryId){
        List<Category> list = categoryMapper.selectChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(list)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(list);
    }

    @Override
    public  ServerResponse<List<Integer>> getDeepChildrenByParentId(Integer categoryId){
        // 使用Guava提供的Sets方法创建Set
        Set<Category> categorySet = Sets.newHashSet();
        // 调用递归方法
        findChildCategory(categorySet,categoryId);

        // 包装返回对象
        List<Integer> categoryList = Lists.newArrayList();
        if(categoryId != null){
            for(Category c : categorySet){
                categoryList.add(c.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    // 递归算法，算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId){
        // 根据categoryId查询Category
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        // 查找categoryId的子节点，递归算法一定要有一个退出条件
        List<Category> categoryList = categoryMapper.selectChildrenByParentId(categoryId);
        // 递归查询，递归次数为categoryList的size，即退出递归条件
        for(Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
