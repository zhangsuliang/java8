package com.huofu.module.i5wei.base;

public class BatchResult<T> {
	
	public static int success = 0;
	
	/**
	 * 错误码，当errorCode=0表示已处理成功
	 */
	private int errorCode;
	/**
	 * 业务对象
	 */
	private T obj;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

	public boolean isSuccess() {
		if (errorCode == 0) {
			return true;
		}
		return false;
	}

}
