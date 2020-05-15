package com.mmall.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * 文件服务器工具类
 * @author Still
 * @version 1.0
 * @date 2020/3/5 15:06
 */
@Slf4j
public class FTPUtil {
    // 从mmall.properties里获取ftp相关信息
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    // 创建构造器
    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    // 创建对外开放的上传文件方法，返回值boolean，代表上传是否成功
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 22, ftpUser, ftpPass);
        log.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img",fileList);
        log.info("开始连接ftp服务器,结束上传，上传结果:{}");
        return result;
    }

    // 上传到ftp服务器的具体方法封装
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        // 创建file输入流
        FileInputStream fileInputStream = null;
        // 连接FTP服务器
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
                // 更改工作目录
                ftpClient.changeWorkingDirectory(remotePath);
                // 设置缓冲区
                ftpClient.setBufferSize(1024);
                // 设置编码
                ftpClient.setControlEncoding("UTF-8");
                // 将上传的文件类型设置为一个二进制的类型
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 设置本地被动模式
                ftpClient.enterLocalPassiveMode();
                // 上传FTP
                for (File fileItem : fileList) {
                    fileInputStream = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fileInputStream);
                }
            } catch (IOException e) {
                log.error("上传文件异常", e);
                uploaded = false;
                e.printStackTrace();
            } finally {
                // 关闭流
                fileInputStream.close();
                // 关闭FTP服务器连接
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    private boolean connectServer(String ip, int port, String user, String pwd) {
        boolean isConSuccess = false;
        ftpClient = new FTPClient();
        try {
            // 连接FTP
            ftpClient.connect(ip);
            // 登录
            isConSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            log.error("连接ftp服务器异常", e);
        }
        return isConSuccess;
    }


    // 声明字段：ip、端口、用户名、密码、ftp客户端
    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
