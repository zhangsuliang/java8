<%@page pageEncoding="utf-8" import="java.util.*"   %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>

<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<%
    String appid=request.getParameter("component_appid");
    String preAuthCode=request.getParameter("pre_auth_code");
    String redirectUrl=request.getParameter("redirect_uri");
%>
component_appid：${component_appid}<br>
pre_auth_code：${pre_auth_code} <br>
redirect_uri： ${redirect_uri}<br>
 <h2>
    <a href="https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid=${component_appid}&pre_auth_code=${pre_auth_code}&redirect_uri=${redirect_uri}">Grant</a>
 </h2>
</body>
</html>
