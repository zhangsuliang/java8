<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="./css/style.css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>
<table border="1" cellpadding="2" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111">
			<tr>
				<td>
					<font color=red>*</font>商户号
				</td>

				<td>
                     ${MerId }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>订单号
				</td>

				<td>
                   ${MerOrderNo }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>商户日期
				</td>

				<td>
                    ${TranDate }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>商户时间
				</td>

				<td>
                    ${TranTime }
                </td>
			</tr>
			
			<tr>
				<td>
					<font color=red>*</font>交易类型
				</td>

				<td>
                    ${TranType }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>业务类型
				</td>

				<td>
                    ${BusiType }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>交易币种
				</td>

				<td>
                    ${CurryNo }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>订单金额
				</td>

				<td>
                    ${OrderAmt }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>订单状态
				</td>

				<td>
                    ${OrderStatus }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>版本号
				</td>

				<td>
                     ${Version }
                </td>
			</tr>
			<tr>
				<td>
					分账类型
				</td>

				<td>
                    ${SplitType }
                </td>
			</tr>
			<tr>
				<td>
					分账金额拆分方式
				</td>

				<td>
                    ${SplitMethod }
                </td>
			</tr>
			
			<tr>
				<td>
					分账公式
				</td>

				<td>
                    ${MerSplitMsg }
                </td>
			</tr>
			
			<tr>
				<td>
					<font color=red>*</font>支付机构号
				</td>

				<td>
                    ${BankInstNo }
                </td>
			</tr>
			<tr>
				<td>
					商品信息描述
				</td>

				<td>
                    ${CommodityMsg }
                </td>
			</tr>
			<tr>
				<td>
					商户私有域
				</td>

				<td>
                   ${MerResv }
                </td>
			</tr>
			<tr>
				<td>
					交易扩展域
				</td>

				<td>
                   ${TranReserved }
                </td>
			</tr>
			<tr>
				<td>
					支付超时时间
				</td>

				<td>
                   ${PayTimeOut }
                </td>
			</tr>
			<tr>
				<td>
					商户时间戳
				</td>

				<td>
                   ${TimeStamp }
                </td>
			</tr>
			<tr>
				<td>
					持卡人IP信息
				</td>

				<td>
                   ${RemoteAddr }
                </td>
			</tr>
			<tr>
				<td>
					收单流水号
				</td>

				<td>
                    ${AcqSeqId }
                </td>
			</tr>
			<tr>
				<td>
					收单日期
				</td>

				<td>
                    ${AcqDate }
                </td>
			</tr>
			<tr>
				<td>
					交易完成日期
				</td>

				<td>
                   ${CompleteDate }
                </td>
			</tr>
			<tr>
				<td>
					交易完成时间
				</td>

				<td>
                   ${CompleteTime }
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>ChinaPay数字签名
				</td>

				<td width="800">
                     <pre>${Signature }</pre>
                </td>
			</tr>
			<tr>
				<td>
					<font color=red>*</font>签名验证信息
				</td>

				<td width="800">
                     <pre>${VERIFY_KEY }</pre>
                </td>
			</tr>
		</table>	

	<hr>
</body>
</html>