package com.huofu.module.i5wei.setting.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.presell.StorePresellSettingDTO;
import huofucore.facade.i5wei.presell.StorePresellSettingModeEnum;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;

import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.setting.dao.StorePresellSettingDAO;
import com.huofu.module.i5wei.setting.entity.StorePresellSetting;

@Service
public class StorePresellService {

	@Autowired
	private StorePresellSettingDAO storePresellSettingDao;
	
	public StorePresellSetting getStorePresellSetting(int merchantId, long storeId){
		return storePresellSettingDao.getById(merchantId, storeId, true);
	}
	
	public long getStorePresellTimeMillis(int merchantId, long storeId){
		StorePresellSetting storePresellSetting = storePresellSettingDao.getById(merchantId, storeId,true,true);
		long now = System.currentTimeMillis();
		if (storePresellSetting == null) {
			return DateUtil.getEndTime(now, null);
		}
		int preMode = storePresellSetting.getPreMode();
		if (preMode == StorePresellSettingModeEnum.DAY.getValue()) {
			int predays = storePresellSetting.getPreDays();
			MutableDateTime mdt = new MutableDateTime(now);
			mdt.addDays(predays);
			mdt.setHourOfDay(23);
			mdt.setMinuteOfHour(59);
			mdt.setSecondOfMinute(59);
			mdt.setMillisOfSecond(999);
			return mdt.getMillis();
			
		} else if (preMode == StorePresellSettingModeEnum.WEEK.getValue()) {
			int perWeekDay = storePresellSetting.getPreWeekDay();
			MutableDateTime mdt = new MutableDateTime(now);
	        mdt.setDayOfWeek(perWeekDay);
			mdt.addWeeks(1);
			mdt.setHourOfDay(23);
			mdt.setMinuteOfHour(59);
			mdt.setSecondOfMinute(59);
			mdt.setMillisOfSecond(999);
			return mdt.getMillis();
		}
		return DateUtil.getEndTime(now, null);
	}
	
	
	public void updateStorePresellSetting(StorePresellSettingDTO storePresellSettingDTO) throws T5weiException{
		if(storePresellSettingDTO==null){
			throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(),"store input params incomplete");
		}
		int merchantId = storePresellSettingDTO.getMerchantId();
		long storeId = storePresellSettingDTO.getStoreId();
		boolean enabled = storePresellSettingDTO.isEnabled();
		StorePresellSettingModeEnum preMode = storePresellSettingDTO.getPreMode();
		StorePresellSetting storePresellSetting = storePresellSettingDao.getById(merchantId, storeId, false, true);
		if (storePresellSetting == null) {
			//预售设置为空，直接创建预售设置
			storePresellSetting = BeanUtil.copy(storePresellSettingDTO, StorePresellSetting.class);
			if(enabled){
				if (preMode == null || preMode.getValue() == StorePresellSettingModeEnum.UNKNOWN.getValue()) {
					storePresellSetting.setEnabled(true);
					storePresellSetting.setPreMode(StorePresellSettingModeEnum.DAY.getValue());
					storePresellSetting.setPreDays(1);
					storePresellSetting.setPreWeekDay(1);
				}
			}
			storePresellSettingDao.create(storePresellSetting);
			return;
		}
		if(!enabled){
			//预售设置关闭，直接更新返回
			storePresellSetting.setEnabled(enabled);
			storePresellSettingDao.update(storePresellSetting);
			return;
		}
		if(preMode==null){
			throw new T5weiException(T5weiErrorCodeType.STORE_PRESELL_MODE_IS_NULL.getValue(),"store presell_mode is null");
		}
		storePresellSetting.setEnabled(true);
		if(preMode.getValue()==StorePresellSettingModeEnum.DAY.getValue()){
			int preDays = storePresellSettingDTO.getPreDays();
			if (preDays > 0) {
				storePresellSetting.setPreDays(preDays);
			}
			storePresellSetting.setPreMode(preMode.getValue());
		}else if(preMode.getValue()==StorePresellSettingModeEnum.WEEK.getValue()){
			int preWeekDay = storePresellSettingDTO.getPreWeekDay();
			if (preWeekDay > 0) {
				storePresellSetting.setPreWeekDay(preWeekDay);
			}
			storePresellSetting.setPreMode(preMode.getValue());
		}
		storePresellSettingDao.update(storePresellSetting);
	}
	
}
