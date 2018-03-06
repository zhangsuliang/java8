package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreTvMenu;
import halo.query.dal.DALContext;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
@SuppressWarnings("unchecked")
public class StoreTvMenuDAO extends AbsQueryDAO<StoreTvMenu> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreTvMenu storeTvMenu) {
        this.addDbRouteInfo(storeTvMenu.getMerchantId(), storeTvMenu.getStoreId());
        super.create(storeTvMenu);
    }

    @Override
    public void update(StoreTvMenu storeTvMenu, StoreTvMenu snapshot) {
        this.addDbRouteInfo(storeTvMenu.getMerchantId(), storeTvMenu.getStoreId());
        super.update(storeTvMenu, snapshot);
    }

    public int deleteById(int merchantId, long storeId, long timeBucketId, long useDate) throws T5weiException {
        DALContext dalContext = BaseStoreDbRouter.buildDalContext(merchantId, storeId, false);
        int resp = this.query.deleteById(StoreTvMenu.class, new Object[]{storeId, timeBucketId, useDate}, dalContext);
        if (resp == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TV_MENU_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] timeBucketId[" + timeBucketId + "] useDate[" + useDate + "] invalid");
        }
        return resp;
    }

    public StoreTvMenu getById(int merchantId, long storeId, long timeBucketId, long useDate, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objByIds(StoreTvMenu.class, new Object[]{storeId, timeBucketId, useDate});
    }

    public StoreTvMenu loadById(int merchantId, long storeId, long timeBucketId, long useDate, boolean enableSlave, boolean enableCache) throws T5weiException {
        StoreTvMenu storeTvMenu = this.getById(merchantId, storeId, timeBucketId, useDate, enableSlave, enableCache);
        if (storeTvMenu == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TV_MENU_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] timeBucketId[" + timeBucketId + "] useDate[" + useDate + "] invalid");
        }
        return storeTvMenu;
    }

    public int count4Save(int merchantId, long storeId, long date) {
        Object[] objs = this.buildArgs4Edit(merchantId, storeId, date);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.count2(StoreTvMenu.class, sb.toString(), params);
    }

    public List<StoreTvMenu> getList4Save(int merchantId, long storeId, long date, int begin, int size) {
        Object[] objs = this.buildArgs4Edit(merchantId, storeId, date);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        sb.append(" order by update_time desc");
        return this.query.mysqlList2(StoreTvMenu.class, sb.toString(), begin, size, params);
    }

    public List<StoreTvMenu> getList4Date(int merchantId, long storeId, long timeBucketId, long useDate, boolean enableSlave) {
        Object[] objs = this.buildArgs4GetList(merchantId, storeId, timeBucketId, useDate, enableSlave);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.list2(StoreTvMenu.class, sb.toString(), params);
    }

    /**
     * 构造查询条件和参数
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param timeBucketId 营业时段id,=-1表示不参与查询条件
     * @param useDate      使用日期/周期 =-1表示不参与查询
     * @param enableSlave  是否读slave
     * @return 查询条件和参数
     */
    public Object[] buildArgs4GetList(int merchantId, long storeId, long timeBucketId, long useDate, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        sb.append("where merchant_id=? and store_id=?");
        params.add(merchantId);
        params.add(storeId);
        if (timeBucketId == 0) {
            sb.append(" and time_bucket_id=0");
        } else if (timeBucketId > 0) {
            sb.append(" and time_bucket_id>0");
        }
        if (useDate >= 0) {
            sb.append(" and use_date=?");
            params.add(useDate);
        }
        sb.append(" and time_bucket_paused = 0");//添加time_bucket_paused = 0,add by lizhijun
        Object[] objs = new Object[2];
        objs[0] = sb;
        objs[1] = params;
        return objs;
    }

    public Object[] buildArgs4Edit(int merchantId, long storeId, long date) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        sb.append("where merchant_id=? and store_id=? and (use_date<=7 or use_date>=?) and time_bucket_paused = 0");//添加time_bucket_paused = 0,add by lizhijun
        params.add(merchantId);
        params.add(storeId);
        params.add(DateUtil.getBeginTime(date, null));
        Object[] objs = new Object[2];
        objs[0] = sb;
        objs[1] = params;
        return objs;
    }

    /**
     * 暂停使用电视餐单
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @return
     */
    public int pausedStoreTvMenu(int merchantId, long storeId, long timeBucketId){
        this.addDbRouteInfo(merchantId, storeId);
        if(timeBucketId > 0){
            List<Object> params = new ArrayList<Object>();
            params.add(timeBucketId);
            return this.query.update2(StoreTvMenu.class, "set time_bucket_paused=1 where time_bucket_id=?", params);
        }
        return 0;
    }

    /**
     * 启用电视餐单
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @return
     */
    public int onStoreTvMenu(int merchantId, long storeId, long timeBucketId){
        this.addDbRouteInfo(merchantId, storeId);
        if(timeBucketId > 0){
             List<Object> params = new ArrayList<Object>();
             params.add(timeBucketId);
             return this.query.update2(StoreTvMenu.class, "set time_bucket_paused=0 where time_bucket_id=?", params);
        }
        return 0;
    }
}
