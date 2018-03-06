package com.huofu.module.i5wei.table.dao;

import java.util.List;

import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.table.dbrouter.StoreTableDbRouter;
import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordRefundDbRouter;
import com.huofu.module.i5wei.table.entity.StoreTableRecordRefund;


/**
 * 桌台退菜记录
 * @author licheng7
 * 2016年4月27日 上午9:31:23
 */
@Repository
public class StoreTableRecordRefundDAO extends AbsQueryDAO<StoreTableRecordRefund> {

	private void addDbRouteInfo(int merchantId, long storeId) {
        StoreTableRecordRefundDbRouter.addInfo(merchantId, storeId);
    }
	
	/**
	 * 根据桌台记录id查询出退菜记录列表
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @return
	 */
	public List<StoreTableRecordRefund> getStoreTableRecordRefundsByTableRecordId (
			int merchantId, long storeId, long tableRecordId) {
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StoreTableRecordRefund.class, 
				" where merchant_id=? and store_id=? and table_record_id=?", 
					new Object[]{merchantId, storeId, tableRecordId});
	}
	
	public void update (StoreTableRecordRefund storeTableRecordRefund) {
		this.addDbRouteInfo(storeTableRecordRefund.getMerchantId(), storeTableRecordRefund.getStoreId());
		super.update(storeTableRecordRefund);
	}
}
