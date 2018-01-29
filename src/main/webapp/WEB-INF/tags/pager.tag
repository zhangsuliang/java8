<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ attribute name="curIndex" type="java.lang.Long" required="true"%>
<%@ attribute name="pageSize" type="java.lang.Long" required="true"%>
<%@ attribute name="pagerRange" type="java.lang.Long" required="true"%>
<%@ attribute name="totalPage" type="java.lang.Long" required="true"%>
<%@ attribute name="formId" type="java.lang.String" required="true"%>
<%@ attribute name="rowCount" type="java.lang.Long" required="false"%>
<%
	long begin = Math.max(1, curIndex - pagerRange/2);
	long end = Math.min(begin + (pagerRange-1),totalPage);
	
	request.setAttribute("p_begin", begin);
	request.setAttribute("p_end", end);
%>
<style><!--分页样式-->  
.pager { font: 12px Arial, Helvetica, sans-serif;}  
.pager a {padding: 1px 6px; border: solid 1px #ddd; background: #fff; text-decoration: none;margin-right:2px;line-height:30px;vertical-align:middle;}  
.pager .active a{color:red;border:none;}  
.pager a:visited {padding: 1px 6px; border: solid 1px #ddd; background: #fff; text-decoration: none;}  
.pager a:hover {color: #fff; background: #ffa501;border-color:#ffa501;text-decoration: none;}  
.pager .input_li{padding: 1px 6px;}  
.disabled a{color:#ccc!important;}
</style> 
<script><!--分页跳转脚本-->  
function gotoPage(pageIndex){ 
	var action = "";
    var queryForm = document.getElementById('${empty(formId)?"queryForm":formId}');  
    if(queryForm!=null)
    	action = queryForm.action;
    else
    	action = window.location.pathname;
    var pageSize = document.getElementById("p_pageSizeSelect").value;  
    if(action.indexOf("?")>0)action +="&";
    else action +="?";
    action += "pageIndex=" + pageIndex + "&pageSize=" + pageSize;  
    if(queryForm!=null){
      queryForm.action = action;  
      queryForm.submit(); 
    }else{
    	window.location.href=action;
    }
}  
  
function gotoPageByBtn(){  
    var pageIndex = document.getElementById("p_pageIndex").value;  
    var pageIndexInt = parseInt(pageIndex);  
    var totalPage = ${totalPage};  
      
    if(pageIndexInt>0 && pageIndexInt<totalPage){  
        gotoPage(pageIndex);  
    }  
    else{  
        alert("输入页数超出范围!");  
    }  
}
function reloadPg(){
	var curIndex=${curIndex};
	gotoPage(curIndex);
}
</script> 




	<table class="pager" style="float:right;">
	<tr>
		 <% if (curIndex!=1){%>
               	<td><a href="javascript:gotoPage(1)">首页</a></td>
                <td><a href="javascript:gotoPage(<%=curIndex-1%>)">上一页</a></td>
         <%}else{%>
                <td class="disabled"><a href="#">首页</a></td>
                <td class="disabled"><a href="#">上一页</a></td>
         <%}%>
 
		<c:forEach var="i" begin="${p_begin}" end="${p_end}">
            <c:choose>
                <c:when test="${i == curIndex}">
                    <td class="active"><a href="#">${i}</a></td>
                </c:when>
                <c:otherwise>
                    <td><a href="javascript:gotoPage(${i})">${i}</a></td>
                </c:otherwise>
            </c:choose>
        </c:forEach>

	  	 <% if (curIndex!=totalPage){%>
               	<td><a href="javascript:gotoPage(<%=curIndex+1%>)">下一页</a></td>
                <td><a href="javascript:gotoPage(<%=totalPage%>)">末页</a></td>
         <%}else{%>
                <td class="disabled"><a href="#">下一页</a></td>
                <td class="disabled"><a href="#">末页</a></td>
         <%}%>
         <td><a>共${totalPage}页${!empty(rowCount)?",":""}${!empty(rowCount)?rowCount:""}${!empty(rowCount)?"条":""}</a></td>
         <td class="input_li">跳转到:<input type="text" id="p_pageIndex" size="2" value="<c:out value="${pageIndex}"/>"/>页 <input type="button" id="gotoBtn" onclick="gotoPageByBtn()" value="GO"/></td>
		 <td class="input_li"> 每页:
		 <select id="p_pageSizeSelect" onchange="gotoPage(<%=curIndex%>)">
		 	<option value="10" <c:if test="${pageSize==10}">selected</c:if>>10条</option>
		 	<option value="15" <c:if test="${pageSize==15}">selected</c:if>>15条</option>
		 	<option value="20" <c:if test="${pageSize==20}">selected</c:if>>20条</option>
		 	<option value="30" <c:if test="${pageSize==30}">selected</c:if>>30条</option>
		 	<option value="50" <c:if test="${pageSize==50}">selected</c:if>>50条</option>
		 	<option value="100" <c:if test="${pageSize==100}">selected</c:if>>100条</option>
		 </select>
		 </td>
	</tr>
	</table>