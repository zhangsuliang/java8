package com.huofu.module.i5wei.menu.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreDateTimeBucketSetting;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreDateTimeBucketSettingDAO extends AbsQueryDAO<StoreDateTimeBucketSetting> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreDateTimeBucketSetting storeDateTimeBucketSetting) {
        this.addDbRouteInfo(storeDateTimeBucketSetting.getMerchantId(),
                storeDateTimeBucketSetting.getStoreId());
        super.create(storeDateTimeBucketSetting);
    }

    @Override
    public void update(StoreDateTimeBucketSetting storeDateTimeBucketSetting, StoreDateTimeBucketSetting snapshot) {
        this.addDbRouteInfo(storeDateTimeBucketSetting.getMerchantId(),
                storeDateTimeBucketSetting.getStoreId());
        super.update(storeDateTimeBucketSetting, snapshot);
    }

    @Override
    public void delete(StoreDateTimeBucketSetting storeDateTimeBucketSetting) {
        this.addDbRouteInfo(storeDateTimeBucketSetting.getMerchantId(),
                storeDateTimeBucketSetting.getStoreId());
        super.delete(storeDateTimeBucketSetting);
    }

    public StoreDateTimeBucketSetting getById(int merchantId, long storeId,
                                              long selectedDate,
                                              long timeBucketId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objByIds(StoreDateTimeBucketSetting.class, new
                Object[]{storeId, selectedDate, timeBucketId});
    }

    /**
     * 获得指定日期范围的暂停营业时段设置
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param minDate    最小日期
     * @param maxDate    最大日期
     * @return 查询结果
     */
    public List<StoreDateTimeBucketSetting> getListForDateRange(int merchantId, long storeId, long minDate, long maxDate) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreDateTimeBucketSetting.class, "where " +
                "store_id=? and selected_date>=? and selected_date<=? and " +
                "deleted=?", new
                Object[]{storeId, minDate, maxDate, false});
    }

    /**
     * 获得指定日期范围的暂停营业时段设置
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param minDate    最小日期
     * @param maxDate    最大日期
     * @return 以map的方式返回，key=[selectedDate]_[timeBucketId]
     */
    public Map<String, StoreDateTimeBucketSetting> getMapForDateRange(int merchantId, long storeId, long minDate, long maxDate) {
        List<StoreDateTimeBucketSetting> storeDateTimeBucketSettings = this
                .getListForDateRange(merchantId, storeId, minDate, maxDate);
        Map<String, StoreDateTimeBucketSetting> map = new HashMap<>();
        for (StoreDateTimeBucketSetting setting : storeDateTimeBucketSettings) {
            map.put(setting.getSelectedDate() + "_" + setting.getTimeBucketId(), setting);
        }
        return map;
    }

    /**
     * 获得指定日期的暂停营业时段设置
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param date       日期的时间戳
     * @return 查询结果
     */
    public List<StoreDateTimeBucketSetting> getListForDate(int merchantId, long storeId, long date, boolean enableSlave, boolean enablCache) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreDateTimeBucketSetting.class, "where store_id=? and selected_date=? and deleted=?", new
                Object[]{storeId, date, false});
    }

    public Map<Long, StoreDateTimeBucketSetting> getMapForDate(int merchantId, long storeId, long date, boolean enableSlave, boolean enablCache) {
        List<StoreDateTimeBucketSetting> list = this.getListForDate(merchantId, storeId, date, enableSlave, enablCache);
        Map<Long, StoreDateTimeBucketSetting> map = new HashMap<>();
        for (StoreDateTimeBucketSetting o : list) {
            map.put(o.getTimeBucketId(), o);
        }
        return map;
    }

    public StoreDateTimeBucketSetting getForDate(int merchantId, long storeId, long date, long timeBucketId, boolean enableSlave, boolean enablCache) {
        List<StoreDateTimeBucketSetting> list = this.getListForDate(merchantId, storeId, date, enableSlave, enablCache);
        for (StoreDateTimeBucketSetting o : list) {
            if (o.getTimeBucketId() == timeBucketId) {
                return o;
            }
        }
        return null;
    }

    public StoreDateTimeBucketSetting loadForDate(int merchantId, long storeId, long date, long timeBucketId, boolean enableSlave, boolean enablCache) throws T5weiException {
        StoreDateTimeBucketSetting setting = this.getForDate(merchantId, storeId, date, timeBucketId, enableSlave, enablCache);
        if (setting == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DATE_TIME_BUCKET_SETTING_INVALID.getValue(),
                    "storeId[" + storeId + "] date[" + date + "] timeBucketId[" + timeBucketId + "] invalid");
        }
        return setting;
    }
}
