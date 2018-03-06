package com.huofu.module.i5wei.order.dao;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordRefundDbRouter;

/**
 * 退菜详情
 * @author licheng7
 * 2016年4月27日 上午9:32:00
 */
@Repository
public class StoreOrderRefundItemDAO extends AbsQueryDAO<StoreOrderRefundItem> {

	private void addDbRouteInfo(int merchantId, long storeId) {
        StoreTableRecordRefundDbRouter.addInfo(merchantId, storeId);
    }
	
	@Override
    public void create(StoreOrderRefundItem storeOrderRefundItem) {
        this.addDbRouteInfo(storeOrderRefundItem.getMerchantId(), storeOrderRefundItem.getStoreId());
        super.create(storeOrderRefundItem);
    }
	
	@Override
    public List<StoreOrderRefundItem> batchCreate(List<StoreOrderRefundItem> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void replace(StoreOrderRefundItem storeOrderRefundItem) {
        this.addDbRouteInfo(storeOrderRefundItem.getMerchantId(), storeOrderRefundItem.getStoreId());
        super.replace(storeOrderRefundItem);
    }
    
    @Override
    public void update(StoreOrderRefundItem storeOrderRefundItem, StoreOrderRefundItem snapshot) {
        this.addDbRouteInfo(storeOrderRefundItem.getMerchantId(), storeOrderRefundItem.getStoreId());
        super.update(storeOrderRefundItem, snapshot);
    }
	
	public void update(StoreOrderRefundItem storeOrderRefundItem) {
		this.addDbRouteInfo(storeOrderRefundItem.getMerchantId(), storeOrderRefundItem.getStoreId());
		super.update(storeOrderRefundItem);
	}
	
	/**
	 * 根据tableRecordId获取退菜记录列表
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @return
	 */
	public List<StoreOrderRefundItem> getStoreOrderRefundItems (int merchantId, long storeId, long tableRecordId) {
		
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StoreOrderRefundItem.class, 
			" where merchant_id=? and store_id=? and table_record_id=?", 
				new Object[]{merchantId, storeId, tableRecordId});
	}
	
	/**
	 * 根据chargeItemId获取退菜记录列表
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param chargeItemId
	 * @return
	 */
	public List<StoreOrderRefundItem> getStoreOrderRefundItemsByChargeItemId (int merchantId, long storeId, long tableRecordId, long chargeItemId) {
		
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StoreOrderRefundItem.class, 
			" where merchant_id=? and store_id=? and table_record_id=? and charge_item_id=?", 
				new Object[]{merchantId, storeId, tableRecordId, chargeItemId});
	}
	
	/**
	 * 根据chargeItemId获取退菜记录列表
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param chargeItemId
	 * @return
	 */
	public List<StoreOrderRefundItem> getStoreOrderRefundItemsByChargeItemId (
			int merchantId, long storeId, long tableRecordId, long chargeItemId, String orderId, boolean packed, boolean forUpdate) {
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where merchant_id=? and store_id=? and table_record_id=? and charge_item_id=? and order_id=? and packed=?";
		if (forUpdate) {
			sql = sql + " for update";
		}
		return this.query.list(StoreOrderRefundItem.class, sql, 
				new Object[]{merchantId, storeId, tableRecordId, chargeItemId, orderId, packed});
	}
	
	public List<StoreOrderRefundItem> getStoreOrderRefundItemsByChargeItemId (
			int merchantId, long storeId, long tableRecordId, long chargeItemId, String orderId, boolean forUpdate) {
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where merchant_id=? and store_id=? and table_record_id=? and charge_item_id=? and order_id=?";
		if (forUpdate) {
			sql = sql + " for update";
		}
		return this.query.list(StoreOrderRefundItem.class, sql, 
				new Object[]{merchantId, storeId, tableRecordId, chargeItemId, orderId});
	}
	
	public List<StoreOrderRefundItem> getStoreOrderRefundItemsByOrderIds (int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
		if (orderIds == null || orderIds.isEmpty()) {
			return new ArrayList<StoreOrderRefundItem>();
		}
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.listInValues(StoreOrderRefundItem.class, " where store_id=? ", "order_id", new Object[]{storeId}, orderIds.toArray());
	}
	
}
