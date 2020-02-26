package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse<String> addCategory(Integer parentId, String categoryName);

    ServerResponse<String> updateCategoryName(Integer categoryId,String categoryName);

    ServerResponse<List<Category>> getChildrenByParentId(Integer categoryId);

    ServerResponse<List<Integer>> getDeepChildrenByParentId(Integer categoryId);
}
