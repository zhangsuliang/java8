package com.huofu.module.i5wei.table.service;

import huofucore.facade.pay.payment.RefundDetailDTO;

import java.util.List;

/**
 * 桌台记录支付状态数据详情
 * @author licheng7
 * 2016年5月16日 下午3:38:21
 */
public class TableRecordPayStatusResult {

	/**
	 * 子订单总价合计（子订单order_price之和）
	 */
	long subOrderPriceTotalAmount;
	
	/**
	 * 已支付子订单总价合计（已支付子订单order_price之和）
	 */
	long subOrderPricePayAmount;
	
	/**
	 * 子订单打包费合计（子订单package_fee之和）
	 */
	long subOrderPackageFeeTotalAmount;
	
	/**
	 * 已支付子订单打包费合计（已支付子订单package_fee之和）
	 */
	long subOrderPackageFeePayAmount;
	
	/**
	 * 子订单外送费合计（子订单delivery_fee之和）
	 */
	long subOrderDeliveryFeeTotalAmount;
	
	/**
	 * 已支付子订单外送费合计（已支付子订单delivery_fee之和）
	 */
	long subOrderDeliveryFeePayAmount;
	
	/**
	 * 子订单实付合计（子订单actual_price之和）
	 */
	long subOrderActualPriceTotalAmount;
	
	/**
	 * 子订单已付金额（所有支付完成子订单actual_price之和）
	 */
	long subOrderActualPricePayAmount;
	
	/**
	 * 退菜金额合计
	 */
	long refundChargeItemPriceTotalAmount;
	
	/**
	 * 主订单原价
	 */
	long masterOrderPriceTotalAmount;
	
	/**
	 * 已支付主订单原价
	 */
	long masterOrderPricePayAmount;
	
	/**
	 * 主订单实付
	 */
	long masterActualPriceTotalAmount;
	
	/**
	 * 已支付主订单实付
	 */
	long masterActualPricePayAmount;
	
	/**
	 * 退款结果
	 */
	List<RefundDetailDTO> refundResultList;
	
	/**
	 * 桌台将记录已经退款金额
	 */
	long tableRecordRefundAmount;
	
	/**
	 * 桌台已经收取的台位费
	 */
	long paidTableFee;
	
	/**
	 * 子订单中已经计算的台位费
	 */
	long tableFee;
	
	long tableRecordSettleRefundAmount;
	
	long tableRecordUnSettleRefundAmount;
	
	/**
	 * 子订单折后价（享受完各种折扣之后的金额，不含外送费）合计
	 */
	long subOrderFavorablePriceTotalAmount;
	
	/**
	 * 子订单整单减免金额合计
	 */
	long subOrderTotalDerateTotalAmount;
	
	/**
	 * 子订单整单折扣打折额度合计
	 */
	long subOrderTotalRebatePriceTotalAmount;
	
	/**
	 * 桌台支付详情
	 */
	List<TableRecordPayDetailResult> payDetailResulttList;

	public long getSubOrderPriceTotalAmount() {
		return subOrderPriceTotalAmount;
	}

	public void setSubOrderPriceTotalAmount(long subOrderPriceTotalAmount) {
		this.subOrderPriceTotalAmount = subOrderPriceTotalAmount;
	}

	public long getSubOrderPricePayAmount() {
		return subOrderPricePayAmount;
	}

	public void setSubOrderPricePayAmount(long subOrderPricePayAmount) {
		this.subOrderPricePayAmount = subOrderPricePayAmount;
	}

	public long getSubOrderPackageFeeTotalAmount() {
		return subOrderPackageFeeTotalAmount;
	}

	public void setSubOrderPackageFeeTotalAmount(long subOrderPackageFeeTotalAmount) {
		this.subOrderPackageFeeTotalAmount = subOrderPackageFeeTotalAmount;
	}

	public long getSubOrderPackageFeePayAmount() {
		return subOrderPackageFeePayAmount;
	}

	public void setSubOrderPackageFeePayAmount(long subOrderPackageFeePayAmount) {
		this.subOrderPackageFeePayAmount = subOrderPackageFeePayAmount;
	}

	public long getSubOrderDeliveryFeeTotalAmount() {
		return subOrderDeliveryFeeTotalAmount;
	}

	public void setSubOrderDeliveryFeeTotalAmount(
			long subOrderDeliveryFeeTotalAmount) {
		this.subOrderDeliveryFeeTotalAmount = subOrderDeliveryFeeTotalAmount;
	}

	public long getSubOrderDeliveryFeePayAmount() {
		return subOrderDeliveryFeePayAmount;
	}

	public void setSubOrderDeliveryFeePayAmount(long subOrderDeliveryFeePayAmount) {
		this.subOrderDeliveryFeePayAmount = subOrderDeliveryFeePayAmount;
	}

	public long getSubOrderActualPriceTotalAmount() {
		return subOrderActualPriceTotalAmount;
	}

	public void setSubOrderActualPriceTotalAmount(
			long subOrderActualPriceTotalAmount) {
		this.subOrderActualPriceTotalAmount = subOrderActualPriceTotalAmount;
	}

	public long getSubOrderActualPricePayAmount() {
		return subOrderActualPricePayAmount;
	}

	public void setSubOrderActualPricePayAmount(long subOrderActualPricePayAmount) {
		this.subOrderActualPricePayAmount = subOrderActualPricePayAmount;
	}

	public long getRefundChargeItemPriceTotalAmount() {
		return refundChargeItemPriceTotalAmount;
	}

	public void setRefundChargeItemPriceTotalAmount(
			long refundChargeItemPriceTotalAmount) {
		this.refundChargeItemPriceTotalAmount = refundChargeItemPriceTotalAmount;
	}

	public long getMasterOrderPriceTotalAmount() {
		return masterOrderPriceTotalAmount;
	}

	public void setMasterOrderPriceTotalAmount(long masterOrderPriceTotalAmount) {
		this.masterOrderPriceTotalAmount = masterOrderPriceTotalAmount;
	}

	public long getMasterOrderPricePayAmount() {
		return masterOrderPricePayAmount;
	}

	public void setMasterOrderPricePayAmount(long masterOrderPricePayAmount) {
		this.masterOrderPricePayAmount = masterOrderPricePayAmount;
	}

	public long getMasterActualPriceTotalAmount() {
		return masterActualPriceTotalAmount;
	}

	public void setMasterActualPriceTotalAmount(long masterActualPriceTotalAmount) {
		this.masterActualPriceTotalAmount = masterActualPriceTotalAmount;
	}

	public long getMasterActualPricePayAmount() {
		return masterActualPricePayAmount;
	}

	public void setMasterActualPricePayAmount(long masterActualPricePayAmount) {
		this.masterActualPricePayAmount = masterActualPricePayAmount;
	}

	public long getTableRecordRefundAmount() {
		return tableRecordRefundAmount;
	}

	public void setTableRecordRefundAmount(long tableRecordRefundAmount) {
		this.tableRecordRefundAmount = tableRecordRefundAmount;
	}

	public List<RefundDetailDTO> getRefundResultList() {
		return refundResultList;
	}

	public void setRefundResultList(List<RefundDetailDTO> refundResultList) {
		this.refundResultList = refundResultList;
	}

	public long getPaidTableFee() {
		return paidTableFee;
	}

	public void setPaidTableFee(long paidTableFee) {
		this.paidTableFee = paidTableFee;
	}

	public long getTableFee() {
		return tableFee;
	}

	public void setTableFee(long tableFee) {
		this.tableFee = tableFee;
	}

	public List<TableRecordPayDetailResult> getPayDetailResulttList() {
		return payDetailResulttList;
	}

	public void setPayDetailResulttList(
			List<TableRecordPayDetailResult> payDetailResulttList) {
		this.payDetailResulttList = payDetailResulttList;
	}

	public long getTableRecordSettleRefundAmount() {
		return tableRecordSettleRefundAmount;
	}

	public void setTableRecordSettleRefundAmount(long tableRecordSettleRefundAmount) {
		this.tableRecordSettleRefundAmount = tableRecordSettleRefundAmount;
	}

	public long getTableRecordUnSettleRefundAmount() {
		return tableRecordUnSettleRefundAmount;
	}

	public void setTableRecordUnSettleRefundAmount(
			long tableRecordUnSettleRefundAmount) {
		this.tableRecordUnSettleRefundAmount = tableRecordUnSettleRefundAmount;
	}

	public long getSubOrderFavorablePriceTotalAmount() {
		return subOrderFavorablePriceTotalAmount;
	}

	public void setSubOrderFavorablePriceTotalAmount(
			long subOrderFavorablePriceTotalAmount) {
		this.subOrderFavorablePriceTotalAmount = subOrderFavorablePriceTotalAmount;
	}

	public long getSubOrderTotalDerateTotalAmount() {
		return subOrderTotalDerateTotalAmount;
	}

	public void setSubOrderTotalDerateTotalAmount(
			long subOrderTotalDerateTotalAmount) {
		this.subOrderTotalDerateTotalAmount = subOrderTotalDerateTotalAmount;
	}

	public long getSubOrderTotalRebatePriceTotalAmount() {
		return subOrderTotalRebatePriceTotalAmount;
	}

	public void setSubOrderTotalRebatePriceTotalAmount(
			long subOrderTotalRebatePriceTotalAmount) {
		this.subOrderTotalRebatePriceTotalAmount = subOrderTotalRebatePriceTotalAmount;
	}
}
