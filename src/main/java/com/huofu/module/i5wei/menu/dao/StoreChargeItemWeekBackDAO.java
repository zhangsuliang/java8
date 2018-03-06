package com.huofu.module.i5wei.menu.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemWeek;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemWeekBack;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

@Repository
public class StoreChargeItemWeekBackDAO extends AbsQueryDAO<StoreChargeItemWeekBack> {
	private void addDbRouteInfo(int merchantId, long storeId) {
		BaseStoreDbRouter.addInfo(merchantId, storeId);
	}

	/**
	 * 批量插入
	 */
	public List<StoreChargeItemWeekBack> batchCreate(List<StoreChargeItemWeekBack> list) {
		if (list == null || list.isEmpty()) {
			return list;
		}
		int merchantId = list.get(0).getMerchantId();
		long storeId = list.get(0).getStoreId();
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.batchInsert(list);
	}
	
	/**
	 * 查询指定营业时段的周期设置
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @param enableSlave
	 * @return
	 */
	public List<StoreChargeItemWeekBack> getStoreChargeItemWeekBacks(int merchantId, long storeId, long timeBucketId, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StoreChargeItemWeekBack.class, "where store_id=? and time_bucket_id=?", new Object[]{storeId,timeBucketId});
	}
	
	public int getStoreChargeItemWeekBackCount(int merchantId, long storeId, long timeBucketId, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.count(StoreChargeItemWeekBack.class, "where store_id=? and time_bucket_id=?", new Object[]{storeId,timeBucketId});
	}
	
	public List<StoreChargeItemWeekBack> getStoreChargeItemWeekBackPage(int merchantId, long storeId, long timeBucketId, int begin, int size, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.mysqlList(StoreChargeItemWeekBack.class, "where store_id=? and time_bucket_id=?", begin, size, new Object[]{storeId,timeBucketId});
	}

	/**
	 * 删除备份表中的周期设置
	 * @param merchantId
	 * @param storeId
	 * @param chargeItemWeekBackIds
	 */
	public void batchDeleteByTimeBucketId(int merchantId, long storeId, long timeBucketId) {
		List<Object> values = new ArrayList<Object>();
		values.add(storeId);
		values.add(timeBucketId);
		this.addDbRouteInfo(merchantId, storeId);
		this.query.delete2(StoreChargeItemWeekBack.class, "where store_id=? and time_bucket_id=?", values);
	}

	/**
	 * 修改周期设置备份表中的已生效的收费项的周期设置，结束时间为前一天的最后一秒钟
	 * @param merchantId
	 * @param storeId
	 * @param chargeItemId
	 * @param beginTime
	 * @param now
	 * @param newEndTime
	 * @return
	 */
	public int updateEndTimeForValid(int merchantId, long storeId, long chargeItemId, long beginTime, long now, long newEndTime) {
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.update(StoreChargeItemWeekBack.class,
				"set end_time=? where store_id=? and charge_item_id=? and begin_time<? and end_time>=?",
				new Object[] { newEndTime, storeId, chargeItemId, beginTime, now });
	}

	/**
	 * 删除周期设置备份表中的未生效的收费项的周期设置
	 * @param merchantId
	 * @param storeId
	 * @param chargeItemId
	 * @param beginTime
	 * @return
	 */
	public int deleteForFuture(int merchantId, long storeId, long chargeItemId, long beginTime) {
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.delete(StoreChargeItemWeekBack.class, "where store_id=? and charge_item_id=? and begin_time>=?", new Object[] { storeId, chargeItemId, beginTime });
	}
}
