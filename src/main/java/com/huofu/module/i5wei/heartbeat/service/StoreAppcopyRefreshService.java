package com.huofu.module.i5wei.heartbeat.service;

import huofuhelper.util.cache.WengerCache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.base.I5weiCachePrefix;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;

/**
 * 签到取号
 * 
 * @author chenkai
 * @since 2016-08-10
 */
@Service
public class StoreAppcopyRefreshService {
	
	private static final Log log = LogFactory.getLog(StoreAppcopyRefreshService.class);

	private static boolean ignoreNamespace = false;
	
	private static int timeoutSecond = 60 * 15;

	@Autowired
	private WengerCache wengerCache;
	
	@Autowired
    private Store5weiSettingService store5weiSettingService;

	private String getSignInKey(int merchantId, long storeId) {
		String key = I5weiCachePrefix.REFRESH_SIGN_IN_APPCOPY + "_" + merchantId + "_" + storeId;
		return key;
	}
	
	public boolean refreshSignInAppcopy(int merchantId, long storeId, long appcopyId, boolean enable) {
		String key = this.getSignInKey(merchantId, storeId);
		Map<Long, Long> appcopyIds = this.getSignInAppcopys(merchantId, storeId);
		// 更新刷新时间
		if (enable) {
			appcopyIds.put(appcopyId, System.currentTimeMillis());
		} else {
			appcopyIds.remove(appcopyId);
		}
		try{
			wengerCache.set(key, appcopyIds, timeoutSecond, ignoreNamespace);
		}catch(Throwable e){
			log.error("refreshSignInAppcopy merchantId=" + merchantId + ",storeId=" + storeId + ",appcopyId=" + appcopyId, e);
		}
		return appcopyIds.isEmpty() ? false : true;
	}
	
	public Map<Long, Long> getSignInAppcopys(int merchantId, long storeId) {
		String key = this.getSignInKey(merchantId, storeId);
		Map<Long, Long> appcopyIds = null;
		try{
			appcopyIds = wengerCache.get(key, ignoreNamespace);
		}catch(Throwable e){
			log.error("refreshSignInAppcopy merchantId=" + merchantId + ",storeId=" + storeId, e);
		}
		if (appcopyIds == null) {
			return new HashMap<Long, Long>();
		}
		long currentTime = System.currentTimeMillis();
		Iterator<Map.Entry<Long, Long>> it = appcopyIds.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, Long> entry = it.next();
			long refreshTime = entry.getValue();
			// 过滤掉刷新时间已经过期的AppCopy
			if ((refreshTime + timeoutSecond * 1000) < currentTime) {
				it.remove();
			}
		}
		return appcopyIds;
	}

}
