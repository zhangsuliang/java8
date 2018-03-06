package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.dao.StoreTvMenuDAO;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucketUtil;
import com.huofu.module.i5wei.menu.entity.StoreTvMenu;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.QueryStoreTvMenus4SaveParam;
import huofucore.facade.i5wei.menu.QueryStoreTvMenusParam;
import huofucore.facade.i5wei.menu.StoreTvMenuParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.PageResult;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class StoreTVMenuService {

    @Autowired
    private StoreTvMenuDAO storeTvMenuDAO;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    public StoreTvMenu save(StoreTvMenuParam param) {
        if (param.getUseDate() > 1000) {
            param.setUseDate(DateUtil.getBeginTime(param.getUseDate(), null));
        }
        StoreTvMenu storeTvMenu = this.storeTvMenuDAO.getById(param.getMerchantId(), param.getStoreId(), param.getTimeBucketId(), param.getUseDate(), false, false);
        if (storeTvMenu == null) {
            storeTvMenu = new StoreTvMenu();
            BeanUtil.copy(param, storeTvMenu);
            storeTvMenu.init4Create();
            storeTvMenu.create();
        } else {
            storeTvMenu.snapshot();
            BeanUtil.copy(param, storeTvMenu);
            storeTvMenu.setUpdateTime(System.currentTimeMillis());
            storeTvMenu.update();
        }
        if (storeTvMenu.getTimeBucketId() > 0) {
            try {
                StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucket(param.getMerchantId(), param.getStoreId(), param.getTimeBucketId());
                storeTvMenu.setStoreTimeBucket(storeTimeBucket);
            } catch (T5weiException e) {
            }
        }
        return storeTvMenu;
    }

    public void deleteStoreTvMenu(int merchantId, long storeId, long timeBucketId, long useDate) throws T5weiException {
        long _useDate = useDate;
        if (_useDate > 1000) {
            _useDate = DateUtil.getBeginTime(useDate, null);
        }
        this.storeTvMenuDAO.deleteById(merchantId, storeId, timeBucketId, _useDate);
    }

    public StoreTvMenu getStoreTvMenu(int merchantId, long storeId, long timeBucketId, long useDate) throws T5weiException {
        long dateTime = 0;
        if (useDate > 1000) {
            dateTime = DateUtil.getBeginTime(useDate, null);
        }
        return this.storeTvMenuDAO.loadById(merchantId, storeId, timeBucketId, dateTime, true, true);
    }

    public List<StoreTimeTvMenu> getStoreTimeTvMenus(QueryStoreTvMenusParam param, boolean enableSlave) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long useDate = param.getUseDate();
        long _userDate = DateUtil.getBeginTime(useDate, null);
        //店铺默认的电视菜单
        List<StoreTvMenu> list4Def = this.storeTvMenuDAO.getList4Date(merchantId, storeId, 0, 0, enableSlave);

        //指定日期出现的菜单
        List<StoreTvMenu> list4Date = this.storeTvMenuDAO.getList4Date(merchantId, storeId, -1, _userDate, enableSlave);

        long period = DateUtil.getWeekDayByDate(_userDate);
        //指定周期出现的菜单
        List<StoreTvMenu> list4Period = this.storeTvMenuDAO.getList4Date(merchantId, storeId, -1, period, enableSlave);

        //指定营业时段默认的菜单
        List<StoreTvMenu> list4TimeBucket = this.storeTvMenuDAO.getList4Date(merchantId, storeId, 1, 0, enableSlave);

        List<StoreTvMenu> list = Lists.newArrayList();
        list.addAll(list4Date);
        list.addAll(list4Period);
        list.addAll(list4TimeBucket);

        Map<Long, StoreTimeBucket> timeBucketMap = this.buildTimeBucketInStoreTvmenus(merchantId, storeId, list);

        List<StoreTimeBucket> storeTimeBuckets = new ArrayList<>(timeBucketMap.values());

        StoreTimeBucketUtil.sortTimeBuckets(storeTimeBuckets);

        //构造一个营业时段顺序值
        Map<Long, Integer> sortMap = Maps.newHashMap();
        int i = 0;
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            sortMap.put(storeTimeBucket.getTimeBucketId(), i);
            i++;
        }
        sortMap.put(0L, i);
        Collections.sort(list, (o1, o2) -> o1.compareTo4SameDay(o2, sortMap));
        list.addAll(list4Def);
        return this.makeRecommend(list, timeBucketMap);
    }

    private List<StoreTimeTvMenu> makeRecommend(List<StoreTvMenu> list, Map<Long, StoreTimeBucket> timeBucketMap) {
        List<StoreTimeTvMenu> storeTimeTvMenus = Lists.newArrayList();
        //是否已经设置了没有营业时段的推荐
        boolean recommendableSetted = false;
        for (StoreTvMenu storeTvMenu : list) {
            StoreTimeTvMenu storeTimeTvMenu = new StoreTimeTvMenu();
            StoreTimeBucket storeTimeBucket = timeBucketMap.get(storeTvMenu.getTimeBucketId());
            if (storeTimeBucket != null && storeTimeBucket.isInTime(System.currentTimeMillis())) {
                storeTimeTvMenu.setRecommendable(true);
                recommendableSetted = true;
            }
            storeTimeTvMenu.setStoreTvMenu(storeTvMenu);
            storeTimeTvMenus.add(storeTimeTvMenu);
        }
        if (!recommendableSetted) {
            for (StoreTimeTvMenu storeTimeTvMenu : storeTimeTvMenus) {
                StoreTimeBucket storeTimeBucket = timeBucketMap.get(storeTimeTvMenu.getStoreTvMenu().getTimeBucketId());
                if (storeTimeBucket != null && storeTimeBucket.isBeforeBizTime()) {
                    storeTimeTvMenu.setRecommendable(true);
                    recommendableSetted = true;
                    break;
                }
            }
        }
        if (!recommendableSetted && !storeTimeTvMenus.isEmpty()) {
            storeTimeTvMenus.get(storeTimeTvMenus.size() - 1).setRecommendable(true);
        }
        return storeTimeTvMenus;
    }

    public PageResult getStoreTvMenus4Save(QueryStoreTvMenus4SaveParam param) {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long useDate = param.getUseDate();
        int page = param.getPage();
        int size = param.getSize();
        int total = this.storeTvMenuDAO.count4Save(merchantId, storeId, useDate);
        int begin = PageUtil.getBeginIndex(page, size);

        List<StoreTvMenu> list4Save = this.storeTvMenuDAO.getList4Save(merchantId, storeId, useDate, begin, size);
        this.buildTimeBucketInStoreTvmenus(merchantId, storeId, list4Save);

        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(total);
        pageResult.setList(list4Save);
        pageResult.build();
        return pageResult;
    }

    private Map<Long, StoreTimeBucket> buildTimeBucketInStoreTvmenus(int merchantId, long storeId, List<StoreTvMenu> storeTvMenus) {
        List<Long> timeBucketIds = Lists.newArrayList();
        for (StoreTvMenu storeTvMenu : storeTvMenus) {
            if (storeTvMenu.getTimeBucketId() > 0) {
                timeBucketIds.add(storeTvMenu.getTimeBucketId());
            }
        }
        Map<Long, StoreTimeBucket> timeBucketMap = this.storeTimeBucketService.getStoreTimeBucketMapInIds(merchantId, storeId, timeBucketIds);
        for (StoreTvMenu storeTvMenu : storeTvMenus) {
            storeTvMenu.setStoreTimeBucket(timeBucketMap.get(storeTvMenu.getTimeBucketId()));
        }
        return timeBucketMap;
    }
}
