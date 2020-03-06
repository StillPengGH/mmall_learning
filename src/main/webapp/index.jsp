<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>上传文件</title>
</head>
<body>
<h2>普通图片上传</h2>
<form action="/manage/product/upload.do" name="form1" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="点击上传"/>
</form>

<h2>富文本图片上传</h2>
<form action="/manage/product/richtext_img_upload.do" name="form2" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="点击上传"/>
</form>
</body>
</html>
