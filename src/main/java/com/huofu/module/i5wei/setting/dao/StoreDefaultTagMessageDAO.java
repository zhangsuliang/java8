package com.huofu.module.i5wei.setting.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.setting.entity.StoreDefaultTagMessage;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;
import huofuhelper.util.AbsQueryDAO;

@Repository
public class StoreDefaultTagMessageDAO extends AbsQueryDAO<StoreDefaultTagMessage> {
	
	public List<StoreDefaultTagMessage> getStoreDefaultTagMessages(StoreTagMessageEnum storeTagMessageEnum, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		String sql = null;
		List<Object> params = new ArrayList<Object>();
		if(storeTagMessageEnum != null){
			sql = "where tag_type = ?";
			params.add(storeTagMessageEnum.getValue());
		}
		return this.query.list2(StoreDefaultTagMessage.class, sql, params);
	}
}
