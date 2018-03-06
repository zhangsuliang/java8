package com.huofu.module.i5wei.servlet;

import huofuhelper.util.DateUtil;
import huofuhelper.util.SpringUtil;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huofu.module.i5wei.order.service.StoreOrderService;

public class StoreSerialNumberServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public StoreSerialNumberServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StoreOrderService storeOrderService = (StoreOrderService) SpringUtil.instance().getBean("storeOrderService");
		String merchant_id = request.getParameter("merchant_id");
		String store_id = request.getParameter("store_id");
		String repast_date = request.getParameter("repast_date");
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
		int merchantId = Integer.valueOf(merchant_id); 
		long storeId = Long.valueOf(store_id); 
		long repastDate = DateUtil.getBeginTime(repast_date, DateUtil.format, null);
		int serialNumber = storeOrderService.getTakeSerialNumber(merchantId, storeId, repastDate);
		out.print(serialNumber);
	}

}
