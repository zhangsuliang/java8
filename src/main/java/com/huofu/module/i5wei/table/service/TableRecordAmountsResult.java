package com.huofu.module.i5wei.table.service;

/**
 * 桌台记录各项金额
 * @author licheng7
 * 2016年6月4日 下午4:27:11
 */
public class TableRecordAmountsResult {
	/**
	 * 合计
	 */
	private long totalAmount = 0L;
	/**
	 * 整单折扣
	 */
	private double discountPro = 100;
	/**
	 * 已经享受的折扣金额总计
	 */
	private long alreadyDiscountAmount = 0L;
	/**
	 * 整单减免金额（服务员设置的整单减免金额和桌台记录已经享受到的折扣金额取最大值）
	 */
	private long discountAmount = 0L;
	/**
	 * 应付金额
	 */
	private long payAbleAmount = 0L;
	/**
	 * 已付金额
	 */
	private long paidAmount = 0L;
	/**
	 * 等待结账的金额（waitSettleAmount>0表示用户还需要支付；waitSettleAmount=0表示已经付清；waitSettleAmount<0表示应该给用户退款）
	 */
	private long waitSettleAmount = 0L;
	/**
	 * 整单折扣折合金额
	 */
	private long discountProAmount = 0L;
	
	public long getTotalAmount() {
		return totalAmount;
	}
	
	public void setTotalAmount(long totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	public double getDiscountPro() {
		return discountPro;
	}
	
	public void setDiscountPro(double discountPro) {
		this.discountPro = discountPro;
	}
	
	public long getAlreadyDiscountAmount() {
		return alreadyDiscountAmount;
	}
	
	public void setAlreadyDiscountAmount(long alreadyDiscountAmount) {
		this.alreadyDiscountAmount = alreadyDiscountAmount;
	}
	
	public long getDiscountAmount() {
		return discountAmount;
	}
	
	public void setDiscountAmount(long discountAmount) {
		this.discountAmount = discountAmount;
	}
	
	public long getPayAbleAmount() {
		return payAbleAmount;
	}
	
	public void setPayAbleAmount(long payAbleAmount) {
		this.payAbleAmount = payAbleAmount;
	}
	
	public long getPaidAmount() {
		return paidAmount;
	}
	
	public void setPaidAmount(long paidAmount) {
		this.paidAmount = paidAmount;
	}
	
	public long getWaitSettleAmount() {
		return waitSettleAmount;
	}
	
	public void setWaitSettleAmount(long waitSettleAmount) {
		this.waitSettleAmount = waitSettleAmount;
	}

	public long getDiscountProAmount() {
		return discountProAmount;
	}

	public void setDiscountProAmount(long discountProAmount) {
		this.discountProAmount = discountProAmount;
	}
}
