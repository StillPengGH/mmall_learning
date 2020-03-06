package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/5 14:20
 */
public interface IFileService {

    String upload(MultipartFile file,String path);
}
