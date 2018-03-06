package com.huofu.module.i5wei.wechat;

import huofuhelper.util.NumberUtil;
import huofuhelper.util.http.HttpClient4Util;
import huofuhelper.util.http.HttpClientFactory;
import huofuhelper.util.http.HttpParameters;
import huofuhelper.util.http.HttpResp;
import huofuhelper.util.json.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Service
public class WechatQrcodeService {
	
	private static final Log log = LogFactory.getLog(WechatQrcodeService.class);
	
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("sysconfig");
	
	private static String baseUrl;
	
	@Autowired
	private HttpClientFactory httpClientFactory;
	
	private String getInvoiceQrcodeUrl() {
		String requestUrl = null;
		if (baseUrl == null || baseUrl.isEmpty()) {
			baseUrl = resourceBundle.getString("wechat.notify.server");
		}
		requestUrl = baseUrl + "/qrcode/invoice";
        return requestUrl;
    }
	
	public Map<String, String> getInvoiceQrcode(int merchantId, long storeId, String orderId, boolean flowGz) {
		Map<String, String> returnMsg = new HashMap<String, String>();
		String url = this.getInvoiceQrcodeUrl();
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", String.valueOf(merchantId));
		params.add("store_id", String.valueOf(storeId));
		params.add("order_id", orderId);
		params.add("flow_gz", String.valueOf(NumberUtil.bool2Int(flowGz)));
		for (int i = 0; i < 3; i++) {
			String msg = null;
			try {
				HttpResp resp = HttpClient4Util.doPost(httpClient, url, params, "UTF-8");
				msg = resp.getText("UTF-8");
				if (msg != null) {
					returnMsg = (Map<String, String>) JsonUtil.parse(msg, Map.class);
				}
				break;
			} catch (Exception e) {
				log.error("fail to request (" + url + ") for getting invoice qrcode_url, and the response is " + msg, e);
			}
		}
		return returnMsg;
	}
	
	
}
