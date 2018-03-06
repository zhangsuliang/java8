package com.huofu.module.i5wei.order.service;

public class StoreOrderPackageFeeResult {

	private long packageFee;//订单打包费
	private boolean producePackageFee;//是否会产生打包费

	public long getPackageFee() {
		return packageFee;
	}

	public void setPackageFee(long packageFee) {
		this.packageFee = packageFee;
	}

	public boolean isProducePackageFee() {
		return producePackageFee;
	}

	public void setProducePackageFee(boolean producePackageFee) {
		this.producePackageFee = producePackageFee;
	}

}
