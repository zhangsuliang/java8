package com.huofu.module.i5wei.table.service;

import huofuhelper.util.cache.WengerCache;
import huofuhelper.util.http.*;
import huofuhelper.util.json.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by jiajin.nervous on 16/5/31.
 */
@Service
public class StoreTableQrcodeService {

    private static final Log log = LogFactory.getLog(StoreTableQrcodeService.class);

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("sysconfig");

    private static String baseUrl;

    @Autowired
    private HttpClientFactory httpClientFactory;

    public String getTableQrcode(int merchantId,long storeId,long tableId){
        Object qrcodeUrl= null;
        try{
            String url = this.getTableQrcodeUrl();
            HttpClient httpClient = httpClientFactory.getHttpClient();
            url = url + "/" + merchantId + "/" + storeId + "/" + tableId;
            HttpResp resp = HttpClient4Util.doPost(httpClient, url, null, "UTF-8");
            String msg = resp.getText("UTF-8");
            if (msg == null) {
                log.warn("msg is null !");
                return "";
            }
            Map returnMsg = (Map) JsonUtil.parse(msg, Map.class);
            qrcodeUrl = returnMsg.get("qrcode");
            if(qrcodeUrl == null){
                log.warn("qrcodeUrl is null !");
                return "";
            }
        }catch (Exception e){
            log.warn("fail to get table qrcode_url ！");
            return "";
        }
        return qrcodeUrl.toString();
    }


    /**
     * 获取桌台记录二维码
     * @param merchantId
     * @param storeId
     * @param tableRecordId
     * @return qrcode_url 二维码地址,qrcode_link 扫描二维码进入的原始地址
     */
    public Map<String,String> getTableRecordQrcode(int merchantId,long storeId,long tableRecordId){
        Map<String,String> msgMap = new HashMap<>();
        try{
            String url = this.getTableRecordQrcodeUrl();
            HttpClient httpClient = httpClientFactory.getHttpClient();
            HttpParameters parameters = new HttpParameters();
            parameters.addRequestParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 200);
            parameters.addRequestParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);
            url = url + "/" + merchantId + "/" + storeId + "/" + tableRecordId;
            HttpResp resp = HttpClient4Util.doPost(httpClient, url, parameters, "UTF-8");
            String msg = resp.getText("UTF-8");
            if (msg == null) {
                log.warn("msg is null !");
                return msgMap;
            }
            msgMap = JsonUtil.parse(msg, Map.class);
            if (msgMap == null) {
                return new HashMap<>();
            }
        }catch (Exception e){
            log.warn("fail to get tableRecord qrcode_url ！");
            return msgMap;
        }
        return msgMap;
    }

    public Map<String,Object> tableQrcodeDownload(int merchantId,long storeId,long expire){
        try{
            String url = this.getTableQrcodeDownloadUrl();
            HttpClient httpClient = httpClientFactory.getHttpClient();
            HttpParameters params = new HttpParameters();
            params.add("merchant_id", merchantId+"");
            params.add("store_id", storeId+"");
            if(expire > 0){
                params.add("expire", expire+"");
            }
            HttpResp resp = HttpClient4Util.doPost(httpClient, url, params, "UTF-8");
            String msg = resp.getText("UTF-8");
            if (msg == null) {
                log.warn("msg is null !");
                return null;
            }
            Map<String,Object> returnMsg = (Map<String,Object>) JsonUtil.parse(msg, Map.class);
            return returnMsg;
        }catch (Exception e){
            log.warn("request getTablePassword failed ！");
            return null;
        }

    }
    
    private String getTableQrcodeUrl(){
        String requestUrl = null;
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = resourceBundle.getString("wechat.notify.server");
        }
        requestUrl = baseUrl + "/qrcode/table";
        return requestUrl;
    }

    private String getTableRecordQrcodeUrl(){
        String requestUrl = null;
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = resourceBundle.getString("wechat.notify.server");
        }
        requestUrl = baseUrl + "/qrcode/table_record";
        return requestUrl;
    }

    private String getTableQrcodeDownloadUrl(){
        String requestUrl = null;
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = resourceBundle.getString("wechat.notify.server");
        }
        requestUrl = baseUrl + "/weixin/catering/table/api/getTablePassword";
        return requestUrl;
    }
}