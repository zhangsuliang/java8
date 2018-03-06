package com.huofu.module.i5wei.servlet;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.inventory.StoreInventoryDateAmountUpdateParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.SpringUtil;
import huofuhelper.util.json.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;

public class StoreInventoryDateUpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public StoreInventoryDateUpdateServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StoreInventoryService storeInventoryService = (StoreInventoryService) SpringUtil.instance().getBean("storeInventoryService");
		String merchant_id = request.getParameter("merchant_id");
		String store_id = request.getParameter("store_id");
		String repast_date = request.getParameter("repast_date");
		String time_bucket_id = request.getParameter("time_bucket_id");
		String product_id = request.getParameter("product_id");
		String amount_str = request.getParameter("amount");
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		if (merchant_id == null || merchant_id.isEmpty()) {
			out.print("merchant_id is null");
			return;
		}
		if (store_id == null || store_id.isEmpty()) {
			out.print("store_id is null");
			return;
		}
		if (repast_date == null || repast_date.isEmpty()) {
			out.print("repast_date is null");
			return;
		}
		if (time_bucket_id == null || time_bucket_id.isEmpty()) {
			out.print("time_bucket_id is null");
			return;
		}
		if (product_id == null || product_id.isEmpty()) {
			out.print("product_id is null");
			return;
		}
		if (amount_str == null || amount_str.isEmpty()) {
			out.print("amount is null");
			return;
		}
		int merchantId = Integer.valueOf(merchant_id); 
		long storeId = Long.valueOf(store_id); 
		long repastDate = DateUtil.getBeginTime(repast_date, DateUtil.format, null);
		long timeBucketId = Long.valueOf(time_bucket_id);
		long productId = Long.valueOf(product_id);
		double amount = Double.valueOf(amount_str);
		StoreInventoryDateAmountUpdateParam storeInventoryDateAmountUpdateParam = new StoreInventoryDateAmountUpdateParam();
		storeInventoryDateAmountUpdateParam.setMerchantId(merchantId);
		storeInventoryDateAmountUpdateParam.setStoreId(storeId);
		storeInventoryDateAmountUpdateParam.setRepastDate(repastDate);
		storeInventoryDateAmountUpdateParam.setTimeBucketId(timeBucketId);
		storeInventoryDateAmountUpdateParam.setProductId(productId);
		storeInventoryDateAmountUpdateParam.setAmount(amount);
		StoreInventoryDate storeInventoryDate = null;
		try {
			storeInventoryDate = storeInventoryService.updateInventoryDateAmount(storeInventoryDateAmountUpdateParam);
		} catch (T5weiException e) {
			out.print(e.getMessage());
			return;
		}
		out.print(JsonUtil.build(storeInventoryDate));
	}

}
