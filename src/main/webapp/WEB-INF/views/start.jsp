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
        <!-- 功能内容开始 -->
        <div class="main-con">
            <form action="${url}" method="post" id="autoForm" name="autoForm" OnSubmit="true">
                <tr>
                    <td>业务代号:</td>
                    <td >
                        <input type="text" name="order_id"  >
                    </td>
                    <td>商户客户号:</td>
                    <td >
                        <input  type="text" name="order_date"  >
                    </td>
                    <td>版本号:</td>
                    <td>
                        <input  type="text" name="trans_amt"  >
                    </td>
                </tr>
                <a href="javascript:void(0);" id="form_submit" class="sel-btn" onclick="check_3();"><strong class="sel-btn-in">页面方式提交</strong></a>
                <!--<input type="submit" value="查询">-->
            </form>
        </div>
    </div>
</div>
</body>
    <script>
        function check_3(){
            document.autoForm.submit();
        }
</script>

</html>
