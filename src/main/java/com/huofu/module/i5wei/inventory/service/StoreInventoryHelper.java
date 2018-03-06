package com.huofu.module.i5wei.inventory.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.inventory.entity.StoreInventoryWeek;

@Component
public class StoreInventoryHelper {

	public void sortInvWeekList(List<StoreInventoryWeek> invWeekList) {
		ComparatorStoreInventoryWeek comparator = new ComparatorStoreInventoryWeek();
		Collections.sort(invWeekList, comparator);
	}
	
	public class ComparatorStoreInventoryWeek implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreInventoryWeek week0 = (StoreInventoryWeek) arg0;
			StoreInventoryWeek week1 = (StoreInventoryWeek) arg1;
			int flag = Integer.valueOf(week0.getWeekDay()).compareTo(week1.getWeekDay());
			if (flag == 0) {
				if(week0.getStoreTimeBucket()==null||week1.getStoreTimeBucket()==null){
					return flag; 
				}
				return Integer.valueOf(week0.getStoreTimeBucket().getStartTime()).compareTo(week1.getStoreTimeBucket().getStartTime());
			} else {
				return flag;
			}
		}

	}
	
}
