package com.huofu.module.i5wei.promotion.util;

import huofucore.facade.i5wei.promotion.StorePromotionStatusEnum;
import huofuhelper.util.DataUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动工具类
 * Created by akwei on 10/26/16.
 */
public class StorePromotionUtil {

    /**
     * 支持的取餐方式
     */
    public static List<Integer> getTakeModes(String takeModeStr) {
        List<Integer> takeModes = new ArrayList<Integer>();
        if (DataUtil.isEmpty(takeModeStr)) {
            return takeModes;
        }
        String[] values = takeModeStr.split(",");
        for (String value : values) {
            try {
                int takeMode = Integer.valueOf(value);
                takeModes.add(takeMode);
            } catch (NumberFormatException e) {
                // 不能识别的取餐方式忽略
            }
        }
        return takeModes;
    }

    public static String buildTakeMode(List<Integer> takeModes) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Integer takeMode : takeModes) {
            sb.append(takeMode);
            if (i < takeModes.size() - 1) {
                sb.append(',');
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * 活动状态：未开始、进行中、已暂停、已结束
     */
    public static int getStatus(boolean paused, long time, long beginTime, long endTime) {
	    if (isEnded(time, endTime)) {
		    return StorePromotionStatusEnum.ENDED.getValue();
        }
        if (paused) {
            return StorePromotionStatusEnum.PAUSED.getValue();
        }
	    if (isNotBegin(time, beginTime)) {
		    return StorePromotionStatusEnum.NOT_BEGIN.getValue();
        }
	    if (isDoing(time, beginTime, endTime)) {
		    return StorePromotionStatusEnum.DOING.getValue();
        }
        return StorePromotionStatusEnum.NOT_BEGIN.getValue();
    }
    
    /**
     * 活动状态：未开启、未开始、进行中、已暂停、已结束
     */
    public static int getStatus4Gratis(boolean paused, long time, long beginTime, long endTime) {
    	if(isNotOpened(time, beginTime, paused)){
        	return  StorePromotionStatusEnum.NOT_OPENED.getValue();
        }
	    else if (isEnded(time, endTime)) {
		    return StorePromotionStatusEnum.ENDED.getValue();
        }
        else if (isPaused(time, beginTime, paused)) {
            return StorePromotionStatusEnum.PAUSED.getValue();
        }
	    else if (isDoing(time, beginTime, endTime)) {
		    return StorePromotionStatusEnum.DOING.getValue();
        }
        return StorePromotionStatusEnum.NOT_BEGIN.getValue();
    }

	public static boolean isNotOpened(long now, long beginTime,boolean paused) {
		return beginTime>now&&paused;
	}

    public static boolean isPaused(long now, long beginTime,boolean paused) {
        return beginTime<=now&&paused;
    }
	public static boolean isNotBegin(long now, long beginTime) {
        return now < beginTime;
    }

    public static boolean isDoing(long now, long beginTime, long endTime) {
        return beginTime <= now && now <= endTime;
    }

    public static boolean isEnded(long now, long endTime) {
        return endTime < now;
    }
}
