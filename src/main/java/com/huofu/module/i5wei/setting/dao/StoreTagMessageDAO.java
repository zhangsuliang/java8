package com.huofu.module.i5wei.setting.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.setting.entity.StoreTagMessage;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;
import huofuhelper.util.AbsQueryDAO;

@Repository
public class StoreTagMessageDAO extends AbsQueryDAO<StoreTagMessage> {
	
	public StoreTagMessage getById(int merchantId, long storeId, long tagId) {
		return this.query.objById(StoreTagMessage.class, tagId);
	}
	
	public List<StoreTagMessage> getInIds(List<Long> tagIds, boolean enableSlave) {
		if(tagIds == null || tagIds.isEmpty()){
			return new ArrayList<StoreTagMessage>();
		}
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		return this.query.listInValues2(StoreTagMessage.class, null, "tag_id", "order by tag_sort asc", null, tagIds);
	}
	
	public List<StoreTagMessage> getStoreTagMessages(int merchantId, long storeId, StoreTagMessageEnum storeTagMessageEnum, boolean enableSlave){
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("where store_id = ? ");
		params.add(storeId);
		
		if(storeTagMessageEnum != null){
			sql.append(" and tag_type = ?");
			params.add(storeTagMessageEnum.getValue());
		}
		sql.append(" order by tag_sort asc");
		return this.query.list2(StoreTagMessage.class, sql.toString(), params);
	}
	
	public int countStoreTagMessage(int merchantId, long storeId, int tagType) {
		List<Object> params = new ArrayList<Object>();
		params.add(storeId);
		params.add(tagType);
		return this.query.count2(StoreTagMessage.class, "where store_id = ? and tag_type = ? ", params);
	}
	
	public void updateStoreTagSort(List<Long> tagIds){
		if(tagIds == null || tagIds.isEmpty()){
			return;
		}
		List<Object[]> valuesList = new ArrayList<Object[]>();
		long updateTime = System.currentTimeMillis();
		for(int i = 0; i< tagIds.size(); i++){
			Object[] params = new Object[]{i, updateTime, tagIds.get(i)};
			valuesList.add(params);
		}
		query.batchUpdate(StoreTagMessage.class, "set tag_sort = ? , update_time = ? where tag_id = ?", valuesList);
	}

	public int deleteStoreTagMessage(int merchantId, long storeId, long tagId) {
		return this.query.deleteById(StoreTagMessage.class, new Object[]{tagId});
	}

	public void batchInsert(List<StoreTagMessage> storeTagMessages) {
		if(storeTagMessages == null || storeTagMessages.isEmpty()){
			return;
		}
		this.query.batchInsert(storeTagMessages);
	}

}
