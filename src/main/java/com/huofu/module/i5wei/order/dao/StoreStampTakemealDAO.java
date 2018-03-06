package com.huofu.module.i5wei.order.dao;


import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreStampTakemeal;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.order.StatusEnum;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by chengq on 16/4/11.
 */
@Repository
public class StoreStampTakemealDAO extends AbsQueryDAO<StoreStampTakemeal> {

    public StoreStampTakemeal getStoreStampTakemeal(int merchantId,long storeId,long storeStampTakemealId){
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        return this.query.objById(StoreStampTakemeal.class,storeStampTakemealId);
    }

    /**
     * 获取需要待打印(打印失败)的出餐单信息
     * @param merchantId 商户id
     * @param storeId 店铺id
     * @param count 查询数量
     * @return
     */
    public List<StoreStampTakemeal> getStoreStampTakemeals(int merchantId,long storeId,int count){
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        //40分钟以内的订单打印
        long currentTime = System.currentTimeMillis()-1000*60*30;
        return this.query.list(StoreStampTakemeal.class,"where store_id=? AND (status=? or status=?) and create_time>=?  order by create_time asc limit 0,"+count,new Object[]{storeId,StatusEnum.WAIT.getValue(),StatusEnum.FAIL.getValue(),currentTime});
    }

    public void create(int merchantId,long storeId,StoreStampTakemeal storeStampTakemeal){
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        this.create(storeStampTakemeal);
    }

    public void update(int merchantId,long storeId,StoreStampTakemeal storeStampTakemeal){
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        this.update(storeStampTakemeal);
    }

    /**
     * 查询店铺是否有待打印的出餐单信息
     * @param merchantId
     * @param storeId
     * @return
     */
    public int isStoreStampTakemeal(int merchantId, long storeId) {
    	DALStatus.setSlaveMode();
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        //40分钟以内的订单打印
        long currentTime = System.currentTimeMillis()-1000*60*30;
        return this.query.count(StoreStampTakemeal.class,"where store_id=? AND (status=? or status=?) and create_time>=? limit 0,1",new Object[]{storeId, StatusEnum.WAIT.getValue(),StatusEnum.FAIL.getValue(),currentTime});
    }

    /**
     * 修复正在打印没有处理的取餐单
     * @param merchantId
     * @param storeId
     */
    public void updateStoreStampTakemealStatus(int merchantId, long storeId){
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        //40分钟以内的订单打印
        long currentTime = System.currentTimeMillis()-1000*60*30;
        this.query.update(StoreStampTakemeal.class,"set status=?, update_time=?  where store_id=? AND status=? AND create_time>=?",new Object[]{StatusEnum.FAIL.getValue(),System.currentTimeMillis(), storeId, StatusEnum.PROCESS.getValue(),currentTime});
    }
}
