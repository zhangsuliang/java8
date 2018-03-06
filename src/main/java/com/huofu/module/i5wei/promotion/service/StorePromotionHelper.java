package com.huofu.module.i5wei.promotion.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;

import huofucore.facade.config.client.ClientTypeEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StorePromotionHelper {

    static List<Long> filterDuplicateId(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        if (ids.isEmpty()) {
            return ids;
        }
        Set<Long> idSet = Sets.newHashSet();
        List<Long> _list = Lists.newArrayList();
        for (Long id : ids) {
            if (idSet.contains(id)) {
                continue;
            }
            _list.add(id);
            idSet.add(id);
        }
        return _list;
    }

    static List<Integer> filterDuplicateWeekDay(List<Integer> weekDays) {
        Set<Integer> weekDaySet = Sets.newHashSet();
        weekDaySet.addAll(weekDays);
        List<Integer> _weekDays = Lists.newArrayList();
        _weekDays.addAll(weekDaySet);
        Collections.sort(_weekDays);
        return _weekDays;
    }

	public static boolean isWechatVisist(int clientType) {
		if (clientType == ClientTypeEnum.MOBILEWEB.getValue()) {
            return true;
        }
        if (clientType == ClientTypeEnum.WECHAT.getValue()) {
            return true;
        }
        return false;
    }


}
