package com.huofu.module.i5wei.menu.entity;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.service.TimeBucketMenuCal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 6/29/15.
 */
public class StoreTimeBucketUtil {

    public static void sortTimeBuckets(List<StoreTimeBucket> storeTimeBuckets) {
        if (storeTimeBuckets == null) {
            return;
        }
        Collections.sort(storeTimeBuckets, (o1, o2) -> {
            if (o1.getStartTime() < o2.getStartTime()) {
                return -1;
            }
            if (o1.getStartTime() > o2.getStartTime()) {
                return 1;
            }
            if (o1.getEndTime() < o2.getEndTime()) {
                return -1;
            }
            if (o1.getEndTime() > o2.getEndTime()) {
                return 1;
            }
            return 0;
        });
    }
    
    /**
     * 按营业时段的外送开始时间升序排序
     * @param storeTimeBuckets
     */
    public static void soreTimeBucketsByDeliveryEndTime(List<StoreTimeBucket> storeTimeBuckets){
    	if (storeTimeBuckets == null) {
            return;
        }
    	Collections.sort(storeTimeBuckets, new Comparator<StoreTimeBucket>() {
			@Override
			public int compare(StoreTimeBucket o1, StoreTimeBucket o2) {
				if(o1.getDeliveryStartTimeForBiz() > o2.getDeliveryStartTimeForBiz()){
					return 1;
				}
				if(o1.getDeliveryStartTimeForBiz() < o2.getDeliveryStartTimeForBiz()){
					return -1;
				}
				if(o1.getDeliveryEndTimeForBiz() > o2.getDeliveryEndTimeForBiz()){
					return 1;
				}
				if(o1.getDeliveryEndTimeForBiz() < o2.getDeliveryEndTimeForBiz()){
					return -1;
				}
				return 0;
			}
		});
    }

    public static void sortTimeBucketMenuCals(List<TimeBucketMenuCal>
                                                      timeBucketMenuCals) {
        if (timeBucketMenuCals == null) {
            return;
        }
        Collections.sort(timeBucketMenuCals, (o1, o2) -> {
            if (o1.getStoreTimeBucket() == null || o2.getStoreTimeBucket() == null) {
                return 0;
            }
            if (o1.getStoreTimeBucket().getStartTime() < o2.getStoreTimeBucket()
                    .getStartTime()) {
                return -1;
            }
            if (o1.getStoreTimeBucket().getStartTime() > o2.getStoreTimeBucket().getStartTime()) {
                return 1;
            }
            return 0;
        });
    }

    public static Map<Long, StoreTimeBucket> buildMap(List<StoreTimeBucket> storeTimeBuckets) {
        Map<Long, StoreTimeBucket> map = Maps.newHashMap();
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            map.put(storeTimeBucket.getTimeBucketId(), storeTimeBucket);
        }
        return map;
    }

}
