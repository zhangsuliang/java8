package com.huofu.module.i5wei.servlet;

import huofuhelper.util.DateUtil;
import huofuhelper.util.SpringUtil;
import huofuhelper.util.json.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;

public class StoreInventoryDateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public StoreInventoryDateServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StoreChargeItemService storeChargeItemService = (StoreChargeItemService) SpringUtil.instance().getBean("storeChargeItemService");
        StoreInventoryService storeInventoryService = (StoreInventoryService) SpringUtil.instance().getBean("storeInventoryService");
        String merchant_id = request.getParameter("merchant_id");
        String store_id = request.getParameter("store_id");
        String repast_date = request.getParameter("repast_date");
        String time_bucket_id = request.getParameter("time_bucket_id");
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
        int merchantId = Integer.valueOf(merchant_id);
        long storeId = Long.valueOf(store_id);
        long repastDate = DateUtil.getBeginTime(repast_date, DateUtil.format, null);
        long timeBucketId = Long.valueOf(time_bucket_id);
        //销售中的产品
        List<StoreProduct> productsInSell = storeChargeItemService.getStoreProductsForDate(merchantId, storeId, repastDate, timeBucketId, false, false);
        List<StoreInventoryDate> resultInventoryDateList = storeInventoryService.getInventoryDate(merchantId, storeId, repastDate, timeBucketId, productsInSell, true);
        out.print(JsonUtil.build(resultInventoryDateList));
    }

}
