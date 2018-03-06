package com.huofu.module.i5wei.setting.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.Lists;
import com.huofu.module.i5wei.setting.dao.StoreDefaultTagMessageDAO;
import com.huofu.module.i5wei.setting.dao.StoreTagMessageDAO;
import com.huofu.module.i5wei.setting.entity.StoreDefaultTagMessage;
import com.huofu.module.i5wei.setting.entity.StoreTagMessage;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageParam;
import huofucore.facade.merchant.store.StoreDTO;
import huofuhelper.util.bean.BeanUtil;

@Service
public class StoreTagMessageService {
	
	@Autowired
	private StoreTagMessageDAO storeTagMessageDAO;
	
	@Autowired
	private StoreDefaultTagMessageDAO storeDefaultTagMessageDAO;

	public List<StoreTagMessage> getStoreTagMessages(int merchantId, long storeId, StoreTagMessageEnum storeTagMessageEnum) {
		return this.storeTagMessageDAO.getStoreTagMessages(merchantId, storeId, storeTagMessageEnum, true);
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreTagMessage saveStoreTagMessage(int merchantId, long storeId, StoreTagMessageParam param) {
		long currentTime = System.currentTimeMillis();
		if(param.getTagId() > 0){//修改
			StoreTagMessage storeTagMessage = this.storeTagMessageDAO.getById(merchantId, storeId, param.getTagId());
			BeanUtil.copy(param, storeTagMessage);
			storeTagMessage.setUpdateTime(currentTime);
			storeTagMessage.update();
			return storeTagMessage;
		}else{//添加
			StoreTagMessage storeTagMessage = BeanUtil.copy(param, StoreTagMessage.class);
			int count = this.storeTagMessageDAO.countStoreTagMessage(merchantId, storeId, param.getTagType());
			storeTagMessage.setTagSort(count);
			storeTagMessage.setCreateTime(currentTime);
			storeTagMessage.setUpdateTime(currentTime);
			storeTagMessage.create();
			return storeTagMessage;
		}
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public List<StoreTagMessage> updateStoreTagSort(int merchantId, long storeId, List<Long> tagIds) {
		if(tagIds != null && !tagIds.isEmpty()){
			this.storeTagMessageDAO.updateStoreTagSort(tagIds);
			return this.storeTagMessageDAO.getInIds(tagIds, true);
		}
		return new ArrayList<StoreTagMessage>();
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public int deleteStoreTagMessage(int merchantId, long storeId, long tagId) {
		return this.storeTagMessageDAO.deleteStoreTagMessage(merchantId, storeId, tagId);
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void initStoreTagMessages(int merchantId, long storeId, StoreTagMessageEnum storeTagMessageEnum) {
		List<StoreDefaultTagMessage> storeDefaultTagMessages = this.storeDefaultTagMessageDAO.getStoreDefaultTagMessages(storeTagMessageEnum, false);
		if(storeDefaultTagMessages == null || storeDefaultTagMessages.isEmpty()){
			return;
		}
		List<StoreTagMessage> storeTagMessages = new ArrayList<StoreTagMessage>();
		long currentTime = System.currentTimeMillis();
		for (StoreDefaultTagMessage storeDefaultTagMessage : storeDefaultTagMessages) {
			StoreTagMessage storeTagMessage = new StoreTagMessage();
			storeTagMessage.setMerchantId(merchantId);
			storeTagMessage.setStoreId(storeId);
			storeTagMessage.setTagMessage(storeDefaultTagMessage.getTagMessage());
			storeTagMessage.setTagType(storeDefaultTagMessage.getTagType());
			storeTagMessage.setTagSort(0);
			storeTagMessage.setCreateTime(currentTime);
			storeTagMessage.setUpdateTime(currentTime);
			storeTagMessages.add(storeTagMessage);
		}
		List<List<StoreTagMessage>> lists = Lists.partition(storeTagMessages, 50);
		for (List<StoreTagMessage> list : lists) {
			this.storeTagMessageDAO.batchInsert(list);
		}
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void initAllStoreTagMessageDTOs(List<StoreDTO> storeDTOs, StoreTagMessageEnum storeTagMessageEnum) {
		for (StoreDTO storeDTO : storeDTOs) {
			this.initStoreTagMessages(storeDTO.getMerchantId(), storeDTO.getStoreId(), storeTagMessageEnum);
		}
	}
}
