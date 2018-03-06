package com.huofu.module.i5wei.promotion.entity;

import java.util.List;
/**
 * Create By Suliang on 2017/1/24
 */
@SuppressWarnings("all")
public class StorePromotionGratisTemp {
	public static String NOT_OPENED_LIST="not_opened_list";
	public static String NOT_BEGIN_LIST="not_begin_list";
	public static String DOING_LIST="doing_list";
	public static String PAUSED_LIST="paused_list";
	public static String ENDED_LIST="ended_list";
	private List<StorePromotionGratis> notOpenedList;
	private List<StorePromotionGratis> notBeginList;
	private List<StorePromotionGratis> doingList;
	private List<StorePromotionGratis> pausedList;
	private List<StorePromotionGratis> endedList;
	public List<StorePromotionGratis> getNotOpenedList() {
		return notOpenedList;
	}
	public void setNotOpenedList(List<StorePromotionGratis> notOpenedList) {
		this.notOpenedList = notOpenedList;
	}
	public List<StorePromotionGratis> getNotBeginList() {
		return notBeginList;
	}
	public void setNotBeginList(List<StorePromotionGratis> notBeginList) {
		this.notBeginList = notBeginList;
	}
	public List<StorePromotionGratis> getDoingList() {
		return doingList;
	}
	public void setDoingList(List<StorePromotionGratis> doingList) {
		this.doingList = doingList;
	}
	public List<StorePromotionGratis> getPausedList() {
		return pausedList;
	}
	public void setPausedList(List<StorePromotionGratis> pausedList) {
		this.pausedList = pausedList;
	}
	public List<StorePromotionGratis> getEndedList() {
		return endedList;
	}
	public void setEndedList(List<StorePromotionGratis> endedList) {
		this.endedList = endedList;
	}
	
  

}
