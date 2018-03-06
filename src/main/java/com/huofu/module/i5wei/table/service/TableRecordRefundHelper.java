package com.huofu.module.i5wei.table.service;

import huofucore.facade.i5wei.order.RefundOrderItemParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.menu.dao.StoreChargeItemPriceDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderRefundItemDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.table.entity.StoreTableRecordRefund;

/**
 * 退菜记录帮助类
 * @author licheng7
 * 2016年5月22日 上午9:23:20
 */
@Component
public class TableRecordRefundHelper {
	
	@Autowired
	private StoreOrderItemDAO storeOrderItemDAO;
	@Autowired
	private StoreOrderRefundItemDAO storeOrderRefundItemDAO;
	@Autowired
	private StoreChargeItemPriceDAO storeChargeItemPriceDAO;

	private static final Log log = LogFactory.getLog(TableRecordRefundHelper.class);
	
	/**
	 * 创建退菜记录
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param staffId
	 * @param clientType
	 * @param refundOrderItemParam
	 * @param storeChargeItem
	 * @return
	 */
	public StoreTableRecordRefund createStoreTableRecordRefund (int merchantId, long storeId, long tableRecordId, long staffId,
			int clientType, RefundOrderItemParam refundOrderItemParam, String chargeItemName, String unit) {
		StoreTableRecordRefund storeTableRecordRefund = new StoreTableRecordRefund();
		storeTableRecordRefund.setMerchantId(merchantId);	
		storeTableRecordRefund.setStoreId(storeId);
		storeTableRecordRefund.setTableRecordId(tableRecordId);	
		storeTableRecordRefund.setStaffId(staffId);
		storeTableRecordRefund.setClientType(clientType);
		storeTableRecordRefund.setRefundPrice(refundOrderItemParam.getRefundAmount());
		storeTableRecordRefund.setChargeItemId(refundOrderItemParam.getChargeItemId());
		storeTableRecordRefund.setChargeItemName(chargeItemName);
		storeTableRecordRefund.setPacked(refundOrderItemParam.isPacked());
		storeTableRecordRefund.setAmount(refundOrderItemParam.getRefundNum());
		storeTableRecordRefund.setUnit(unit);
		storeTableRecordRefund.setRefundReason(refundOrderItemParam.getRefundReason());
		storeTableRecordRefund.setRestoreInventory(refundOrderItemParam.isRecoveryStock());
		storeTableRecordRefund.setCreateTime(System.currentTimeMillis());
		storeTableRecordRefund.setUpdateTime(System.currentTimeMillis());
		storeTableRecordRefund.create();
		return storeTableRecordRefund;
	}
	
	/**
	 * 创建退菜项目记录
	 * @param storeOrder
	 * @param refundOrderItemParam
	 * @param storeOrderRefundItemMap
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param staffId
	 * @param storeChargeItem
	 * @param storeOrderRefundItemList
	 * @param chargeItemNumInRefundItem
	 */
	public StoreOrderRefundItem createRefundItemRecord (
		StoreOrder storeOrder, RefundOrderItemParam refundOrderItemParam, int merchantId, long storeId, long tableRecordId, 
			long staffId, long tableRecordRefundId, long refundItemPrice, double refundedItemNum) {
		// 根据订单id查询订单包含的收费项目
		List<StoreOrderItem> storeOrderItems = storeOrderItemDAO.getStoreOrderItemByOrderId(merchantId, storeId, storeOrder.getOrderId(), false);
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			if (storeOrderItem.getChargeItemId() == refundOrderItemParam.getChargeItemId()) {
				List<StoreOrderRefundItem> storeOrderRefundItems = storeOrderRefundItemDAO.getStoreOrderRefundItemsByChargeItemId(
						merchantId, storeId, tableRecordId, storeOrderItem.getChargeItemId(), storeOrder.getOrderId(), refundOrderItemParam.isPacked(), true);
				// 已退数量
				double refundItemAmount = 0L;
				if (storeOrderRefundItems != null && !storeOrderRefundItems.isEmpty()) {
					for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems) {
						refundItemAmount += storeOrderRefundItem.getAmount();
					}
				}
				// 可退数量
				double allowRefundItemNum = 0;
				if (refundOrderItemParam.isPacked()) {
					allowRefundItemNum = storeOrderItem.getPackedAmount() - refundItemAmount;
				} else {
					allowRefundItemNum = storeOrderItem.getAmount() - storeOrderItem.getPackedAmount() - refundItemAmount;
				}
				// 创建退菜记录
				StoreOrderRefundItem storeOrderRefundItem = new StoreOrderRefundItem();
				if (allowRefundItemNum > 0) {
					if (allowRefundItemNum >= refundOrderItemParam.getRefundNum() - refundedItemNum) {
						storeOrderRefundItem.setAmount(refundOrderItemParam.getRefundNum() - refundedItemNum);
					} else  {
						storeOrderRefundItem.setAmount(allowRefundItemNum);
					}
				} else {
					return null;
				}
				storeOrderRefundItem.setTableRecordId(tableRecordId);
				storeOrderRefundItem.setMerchantId(merchantId);
				storeOrderRefundItem.setStoreId(storeId);
				storeOrderRefundItem.setChargeItemId(refundOrderItemParam.getChargeItemId());
				storeOrderRefundItem.setRepastDate(storeOrder.getRepastDate());
				storeOrderRefundItem.setTimeBucketId(storeOrder.getTimeBucketId());
				storeOrderRefundItem.setChargeItemName(storeOrderItem.getChargeItemName());
				storeOrderRefundItem.setUnit(storeOrderItem.getUnit());
				storeOrderRefundItem.setRestoreInventory(refundOrderItemParam.isRecoveryStock());
				storeOrderRefundItem.setRefundReason(refundOrderItemParam.getRefundReason());
				storeOrderRefundItem.setStaffId(staffId);
				storeOrderRefundItem.setPacked(refundOrderItemParam.isPacked());
				storeOrderRefundItem.setOrderId(storeOrder.getOrderId());
				storeOrderRefundItem.setPrice(refundItemPrice);
				BigDecimal bDRefundAmount = new BigDecimal(String.valueOf(refundOrderItemParam.getRefundAmount())); // 退菜金额
				double refundNum = refundOrderItemParam.getRefundNum() - refundedItemNum;
				BigDecimal bDRefundNum = new BigDecimal(String.valueOf(refundNum)); // 退菜数量
				storeOrderRefundItem.setRefundPrice(bDRefundAmount.divide(bDRefundNum, 2, RoundingMode.HALF_UP).longValue()); // 取平均值
				storeOrderRefundItem.setCreateTime(System.currentTimeMillis());
				storeOrderRefundItem.setUpdateTime(System.currentTimeMillis());
				storeOrderRefundItem.setTableRecordRefundId(tableRecordRefundId);
				storeOrderRefundItem.create();
				return storeOrderRefundItem;
			}
		}
		return null;
	}
}
