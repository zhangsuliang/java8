<%--
  Created by IntelliJ IDEA.
  User: zhouweidong
  Date: 2017/12/4
  Time: 下午12:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>模拟器--汇付天下</title>
    <!-- css样式 -->
    <link type="text/css" rel="stylesheet" href="${rc.contextPath}/styles/index.css" media="all" />
    <style>
        .t_area{
            width:300px;
            overflow-y:visible
        }
    </style>
    <!-- js -->
    <script type="text/javascript" src="${rc.contextPath}/scripts/jquery.js"></script>
    <script type="text/javascript" src="${rc.contextPath}/scripts/index.js"></script>
    <script language="javascript" type="text/javascript" src="${rc.contextPath}/scripts/scriptQuerySdkBindedInfo.js"></script>
</head>
<!--页面一级菜单-->
<body>
<div id="wrap">
    <div id="main">
        <div class="ie6-out">
            <div class="ie6-in">
                    <div class="bms-top fix">
                        <h1 class="l">
                            <a target="_blank" href="http://www.chinapnr.com">
                                <img src="${rc.contextPath}/images/logo.png" class="vm" alt="汇付天下" />
                            </a>
                        </h1>
                    </div>
                    <div class="bms-header">
                        <div class="wrapper-in fix">
                            <h2 class="bms-title">模拟器</h2>
                        </div>
                    </div>
                </div>
        </div>
    </div>
    <!-- 一级菜单结束 -->

    <!-- 二级菜单开始 -->

    <div class="wraper">
        <div class="left-area">
            <div class="left-nav-area">
                <div class="left-nav-top"></div>
                <ul class="left-nav">

                    <li class="left-nav-item">
                        <dl>
                            <dt><b class="nav-ico"></b>NPAY</dt>
                            <dd><a href="${rc.contextPath}/npay/common/stru">普通接口</a></dd>
                        </dl>
                    </li>

                </ul>
            </div>
            <span id="lBtn" class="arrow-btn arrow-btn-l"> <u class="arrow-btn-left"></u>
      </span>
        </div>
        <!-- 二级菜单结束 -->



        <!-- 功能内容开始 -->
        <div class="main-con">
            <form action="${url}" method="post" id="autoForm" name="autoForm" OnSubmit="true">
                <tr>
                    <td>业务代号:</td>
                    <td >
                        <input class="fund-input" type="text" name="cmd_id" id="cmd_id" value="${cmdId}">
                    </td>
                    <td>商户客户号:</td>
                    <td >
                        <input class="fund-input" type="text" name="mer_cust_id" id="mer_cust_id" value="${merCustId}">
                    </td>
                    <td>版本号:</td>
                    <td>
                        <input class="fund-input" type="text" name="version" id="version" value="${version}">
                    </td>
                    <td>SIGN:</td>
                    <td>
                        <input class="fund-input" type="text" name="check_value" id="check_value" value="${sign}">
                    </td>

                </tr>
                <a href="javascript:void(0);" id="form_submit" class="sel-btn" onclick="check_3();"><strong class="sel-btn-in">页面方式提交</strong></a>

            </form>
        </div>
        <!-- 功能内容结束 -->



    </div>
</div>
<!---页尾-->
<div class="push"></div>
</div>
<div class="ie6-out">
    <div class="ie6-in">
        <div id="min-width">
            <div class="main footer_color footer_margin">
                <div class="footer">
                    汇付天下有限公司版权所有  Copyright©2012 ChinaPnR All Right Reserved
                    <br>
                    <a href="http://www.chinapnr.com/intro/" target="_blank" class="link">关于汇付天下</a>
                    <a href="http://www.chinapnr.com/service/" target="_blank"  class="link">客户服务</a>
                    <a href="http://www.chinapnr.com/security/" target="_blank"  class="link">账户安全</a>
                    <a href="http://www.chinapnr.com/qa/" target="_blank"  class="link">帮助中心</a>
                    <a href="http://weibo.com/1938206822" target="_blank"  class="link">联系我们</a>
                    <p class="allpic">
                        客服电话： <b>400 820 5623</b>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>
</body>

<script>
    window.onload=function(){
        document.getElementById("autoForm").submit();
    }

    function check_3(){
        document.autoForm.submit();
    }
</script>

</html>
