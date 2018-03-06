package com.huofu.module.i5wei.printer.JsonMap;

import huofuhelper.util.NumberUtil;

import java.util.HashMap;
import java.util.Map;

import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;

public class StoreOrderRefundItemJsonMap {
	
	public static Map<String, Object> toMap(StoreOrderRefundItem refundItem) {
		Map<String, Object> printMessage = new HashMap<String, Object>();
		printMessage.put("charge_item_id", refundItem.getChargeItemId());
		printMessage.put("charge_item_name", refundItem.getChargeItemName());
		printMessage.put("price", refundItem.getPrice());
		printMessage.put("amount", refundItem.getAmount());
		printMessage.put("unit", refundItem.getUnit());
		printMessage.put("packed", NumberUtil.bool2Int(refundItem.isPacked()));
		printMessage.put("restore_inventory", NumberUtil.bool2Int(refundItem.isRestoreInventory()));
		printMessage.put("create_time",refundItem.getCreateTime());
		printMessage.put("refund_price", refundItem.getRefundPrice());
		if (refundItem.getTableRecordId() > 0) {
			printMessage.put("refund_reason", refundItem.getRefundReason());
		}
		return printMessage;
	}
	
	public static Map<String, Object> toMap(StoreOrderRefundItem refundItem, StoreOrderItem storeOrderItem) {
		Map<String, Object> printMessage = new HashMap<String, Object>();
		printMessage.put("charge_item_id", refundItem.getChargeItemId());
		printMessage.put("charge_item_name", refundItem.getChargeItemName());
		printMessage.put("price", refundItem.getPrice());
		printMessage.put("amount", refundItem.getAmount());
		printMessage.put("unit", refundItem.getUnit());
		printMessage.put("packed", NumberUtil.bool2Int(refundItem.isPacked()));
		printMessage.put("create_time",refundItem.getCreateTime());
		printMessage.put("restore_inventory", NumberUtil.bool2Int(refundItem.isRestoreInventory()));
		printMessage.put("refund_price", refundItem.getRefundPrice());
		if (refundItem.getTableRecordId() > 0) {
			printMessage.put("refund_reason", refundItem.getRefundReason());
		}
		double orderAmount = 0D;
		double orderRefundAmount = 0D;
		if(refundItem.isPacked()){
			orderAmount = storeOrderItem.getPackedAmount();
			orderRefundAmount = storeOrderItem.getRefundChargeItemNumPacked();
		}else{
			orderAmount = NumberUtil.sub(storeOrderItem.getAmount(), storeOrderItem.getPackedAmount());
			orderRefundAmount = storeOrderItem.getRefundChargeItemNumUnPacked();
		}
		printMessage.put("order_amount", orderAmount);
		printMessage.put("order_refund_amount", orderRefundAmount);
		printMessage.put("subitems", StoreOrderSubitemJsonMap.toMapList(storeOrderItem.getStoreOrderSubitems()));
		return printMessage;
	}

}
