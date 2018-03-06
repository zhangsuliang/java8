package com.huofu.module.i5wei.menu.dao;

import huofucore.facade.i5wei.menu.StoreChargeItemPromotionQueryParam;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.PageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreChargeItemPromotionDAO extends AbsQueryDAO<StoreChargeItemPromotion> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreChargeItemPromotion storeChargeItemPromotion) {
        this.addDbRouteInfo(storeChargeItemPromotion.getMerchantId(), storeChargeItemPromotion.getStoreId());
        super.create(storeChargeItemPromotion);
    }

    @Override
    public void update(StoreChargeItemPromotion storeChargeItemPromotion, StoreChargeItemPromotion snapshot) {
        this.addDbRouteInfo(storeChargeItemPromotion.getMerchantId(), storeChargeItemPromotion.getStoreId());
        super.update(storeChargeItemPromotion, snapshot);
    }

    @Override
    public void delete(StoreChargeItemPromotion storeChargeItemPromotion) {
        this.addDbRouteInfo(storeChargeItemPromotion.getMerchantId(), storeChargeItemPromotion.getStoreId());
        super.delete(storeChargeItemPromotion);
    }
    
    @Override
    public void replace(StoreChargeItemPromotion storeChargeItemPromotion) {
        this.addDbRouteInfo(storeChargeItemPromotion.getMerchantId(), storeChargeItemPromotion.getStoreId());
        super.replace(storeChargeItemPromotion);
    }
    
    public StoreChargeItemPromotion getById(int merchantId, long storeId, long chargeItemId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where store_id=? and charge_item_id=?";
        if (forUpdate) {
            //sql = sql + " for update ";
        }
        StoreChargeItemPromotion storeChargeItemPromotion = this.query.obj(StoreChargeItemPromotion.class, sql, new Object[]{storeId, chargeItemId});
        if (storeChargeItemPromotion != null && forSnapshot) {
            storeChargeItemPromotion.snapshot();
        }
        return storeChargeItemPromotion;
    }

    public List<StoreChargeItemPromotion> getListByIds(int merchantId, long storeId, List<Long> chargeItemIds) {
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreChargeItemPromotion> list = this.query.listInValues(
                StoreChargeItemPromotion.class, " where store_id=? ", "charge_item_id", new Object[]{storeId}, chargeItemIds.toArray());
        return list;
    }

    public List<StoreChargeItemPromotion>  getListByIds(int merchantId, long storeId, List<Long> chargeItemIds,long repastDate){
    	 if (chargeItemIds == null || chargeItemIds.isEmpty()) {
             return new ArrayList<>(0);
         }
         this.addDbRouteInfo(merchantId, storeId);
         List<StoreChargeItemPromotion> list = this.query.listInValues(
                 StoreChargeItemPromotion.class, " where merchant_id=? and store_id=? and end_time>=? ", "charge_item_id", new Object[]{merchantId,storeId,repastDate}, chargeItemIds.toArray());
		return list;
    }
    
    public List<StoreChargeItemPromotion> getStoreChargeItemPromotions(StoreChargeItemPromotionQueryParam param, List<Long> chargeItemIds) {
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        StringBuffer limitSql = new StringBuffer();
        if(param.getPageNo() > 0 && param.getSize() > 0){
            int start = PageUtil.getBeginIndex(param.getPageNo(), param.getSize());
            limitSql.append(" order by update_time desc ");
            limitSql.append(" limit ");
            limitSql.append(start);
            limitSql.append(",");
            limitSql.append(param.getSize());
        }
        this.addDbRouteInfo(param.getMerchantId(), param.getStoreId());
        List<StoreChargeItemPromotion> list = this.query.listInValues(StoreChargeItemPromotion.class, " where store_id=?", "charge_item_id",limitSql.toString(), new Object[]{param.getStoreId()}, chargeItemIds.toArray());
        return list;
    }

    /**
     * key=chargeItemId value=StoreChargeItemPromotion
     */
    public Map<Long, StoreChargeItemPromotion> getMapInIds(int merchantId, long storeId, List<Long> chargeItemIds) {
        List<StoreChargeItemPromotion> list = this.getListByIds(merchantId, storeId, chargeItemIds);
        Map<Long, StoreChargeItemPromotion> map = Maps.newHashMap();
        for (StoreChargeItemPromotion obj : list) {
            map.put(obj.getChargeItemId(), obj);
        }
        return map;
    }

    public int countChargeItemPromotions(StoreChargeItemPromotionQueryParam param, List<Long> chargeItemIds) {
        this.addDbRouteInfo(param.getMerchantId(), param.getStoreId());
        List<StoreChargeItemPromotion> list = this.query.listInValues(
                StoreChargeItemPromotion.class, " where store_id=?", "charge_item_id", new Object[]{param.getStoreId()}, chargeItemIds.toArray());
        return list == null ? 0 : list.size();
    }

    public List<StoreChargeItemPromotion> getStoreChargeItemPromotionlist(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeItemPromotion.class, " where store_id=?",new Object[]{storeId});
    }

    /**
     * 根据收费项目id，查询未结束的首份特价活动
     */
    public StoreChargeItemPromotion getByChargeItemId(int merchantId, long storeId, long chargeItemId, long time) {
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where merchant_id=? and store_id=? and charge_item_id=? and end_time>=?";
        return this.query.obj(StoreChargeItemPromotion.class, sql, new Object[]{merchantId, storeId, chargeItemId, time});
    }

    /**
     * 查询未结束的首份特价活动
     */
    public List<StoreChargeItemPromotion> getList4NotEnd(int merchantId, long storeId, long time){
         this.addDbRouteInfo(merchantId, storeId);
         List<StoreChargeItemPromotion> list = this.query.list(
                 StoreChargeItemPromotion.class, " where merchant_id=? and store_id=? and end_time<=? ", new Object[]{merchantId, storeId, time});
		return list;
    }
}
