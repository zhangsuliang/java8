package com.huofu.module.i5wei.menu.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreDateBizSetting;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreDateBizSettingDAO extends AbsQueryDAO<StoreDateBizSetting> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreDateBizSetting storeDateBizSetting) {
        this.addDbRouteInfo(storeDateBizSetting.getMerchantId(),
                storeDateBizSetting.getStoreId());
        super.create(storeDateBizSetting);
    }

    @Override
    public void update(StoreDateBizSetting storeDateBizSetting, StoreDateBizSetting snapshot) {
        this.addDbRouteInfo(storeDateBizSetting.getMerchantId(),
                storeDateBizSetting.getStoreId());
        super.update(storeDateBizSetting, snapshot);
    }

    @Override
    public void delete(StoreDateBizSetting storeDateBizSetting) {
        this.addDbRouteInfo(storeDateBizSetting.getMerchantId(),
                storeDateBizSetting.getStoreId());
        super.delete(storeDateBizSetting);
    }

    /**
     * 获得指定日期的店铺菜单特殊设置
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param selectedDate 日志的时间戳 00:00:00
     * @return 查询结果
     */
    public StoreDateBizSetting getById(int merchantId, long storeId, long selectedDate, boolean enableSlave, boolean enableCache) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.objByIds(StoreDateBizSetting.class, new Object[]{storeId, selectedDate});
    }

    /**
     * 获得指定日期的店铺菜单特殊设置
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param selectedDate 日志的时间戳 00:00:00
     * @return 查询结果
     */
    public StoreDateBizSetting getForSelectedDate(int merchantId, long storeId, long selectedDate, boolean enableSlave, boolean enableCache) {
        StoreDateBizSetting storeDateBizSetting = this.getById(merchantId, storeId, selectedDate, enableSlave, enableCache);
        if (storeDateBizSetting != null && storeDateBizSetting.isDeleted()) {
            return null;
        }
        return storeDateBizSetting;
    }

    public StoreDateBizSetting loadForSelectedDate(int merchantId, long storeId, long selectedDate, boolean enableSlave, boolean enableCache) throws T5weiException {
        StoreDateBizSetting storeDateBizSetting = this.getForSelectedDate(merchantId, storeId, selectedDate, enableSlave, enableCache);
        if (storeDateBizSetting == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DATE_BIZ_SETTING_INVALID.getValue(),
                    "storeId[" + storeId + "] selectedDate[" + selectedDate + "] invalid");
        }
        return storeDateBizSetting;
    }

    /**
     * 获得指定时间范围的店铺日期菜单特殊设置
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param minDate    最小日期，如果=0表示忽略此参数
     * @param maxDate    最大日期，如果=0表示忽略此参数
     * @return 查询结果
     */
    public List<StoreDateBizSetting> getListForSelectedDateRange(int merchantId, long
            storeId, long minDate, long maxDate) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> param = new ArrayList<>();
        StringBuilder sb = new StringBuilder("where store_id=? and deleted=?");
        param.add(storeId);
        param.add(false);
        if (minDate > 0) {
            sb.append(" and selected_date>=?");
            param.add(minDate);
        }
        if (maxDate > 0) {
            sb.append(" and selected_date<=?");
            param.add(maxDate);
        }
        sb.append(" order by selected_date asc");
        return this.query.list2(StoreDateBizSetting.class, sb.toString(),
                param);
    }

    public Map<Long, StoreDateBizSetting> getMapForSelectedDateRange(int merchantId, long
            storeId, long minDate, long maxDate) {
        List<StoreDateBizSetting> list = this.getListForSelectedDateRange
                (merchantId, storeId, minDate, maxDate);
        Map<Long, StoreDateBizSetting> bizSettingMap = new HashMap<>();
        for (StoreDateBizSetting setting : list) {
            bizSettingMap.put(setting.getSelectedDate(), setting);
        }
        return bizSettingMap;
    }

}
