package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Still
 * @version 1.0
 * @date 2020/3/5 14:20
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    // 用于打印日志
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 上传图片
     * @param multipartFile 上传文件(SpringMVC提供的)
     * @param path 上传路径
     * @return 返回上传后的图片路径
     */
    @Override
    public String upload(MultipartFile multipartFile, String path){
        // 获取上传文件的原始文件名
        String fileName = multipartFile.getOriginalFilename();
        // 获取文件的扩展名
        String fileExtName = fileName.substring(fileName.lastIndexOf(".")+1);
        // 创建上传后的文件名（不重复）
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtName;
        // 打印上传文件日志，其中{}为占位符和后面的参数意义对应(logback规定)
        logger.info("开始上传文件，上传的文件名：{},上传的路径：{},新文件名：{}",
                fileName,path,uploadFileName);
        // 声明上传路径的file
        File fileDir = new File(path);
        // 判断是否存这个目录,不存在就要创建目录，即webapp下的upload文件夹
        if(!fileDir.exists()){
            // 赋予可写权限
            fileDir.setWritable(true);
            // 创建目录upload
            fileDir.mkdirs();
        }
        // 创建文件
        File targetFile = new File(path,uploadFileName);
        try {
            // 上传文件
            multipartFile.transferTo(targetFile);
            // 将targetFile上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 上传到FTP服务器，删除本地upload下的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传异常",e);
            return null;
        }
        // 返回上传文件的文件名
        return targetFile.getName();
    }
}
