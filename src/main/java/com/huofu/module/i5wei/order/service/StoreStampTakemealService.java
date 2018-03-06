package com.huofu.module.i5wei.order.service;

import com.huofu.module.i5wei.order.dao.StoreStampTakemealDAO;
import com.huofu.module.i5wei.order.entity.StoreStampTakemeal;
import huofucore.facade.i5wei.order.StatusEnum;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by chengq on 16/4/11.
 */
@Service
public class StoreStampTakemealService {

    @Resource
    private StoreStampTakemealDAO storeStampTakemealDAO;

    public void create(int merchantId, long storeId, StoreStampTakemeal storeStampTakemeal) {
        try {
            storeStampTakemealDAO.create(merchantId, storeId, storeStampTakemeal);
        } catch (DuplicateKeyException e) {
            // ignore exception
        }
    }

    public void update(int merchantId, long storeId, StoreStampTakemeal storeStampTakemeal) {
        storeStampTakemeal.setUpdateTime(System.currentTimeMillis());
        storeStampTakemealDAO.update(merchantId, storeId, storeStampTakemeal);
    }

    public StoreStampTakemeal getStoreStampTakemeal(int merchantId, long storeId, long storeStampTakemealId) {
        return storeStampTakemealDAO.getStoreStampTakemeal(merchantId, storeId, storeStampTakemealId);
    }

    /**
     * 获取需要待打印(打印失败)的出餐单信息,并且状态改为正在打印
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param count      数量控制在 1 <= count <= 3
     * @return
     */
    public synchronized List<StoreStampTakemeal> getStoreStampTakemeals(int merchantId, long storeId, int count) {
        if (count <= 0) {
            count = 1;
        }
        if (count > 3) {
            count = 3;
        }
        List<StoreStampTakemeal> storeStampTakemeals = storeStampTakemealDAO.getStoreStampTakemeals(merchantId, storeId, count);
        for (StoreStampTakemeal storeStampTakemeal : storeStampTakemeals) {
            storeStampTakemeal.setStatus(StatusEnum.PROCESS.getValue());//改为正在打印状态
            this.update(merchantId, storeId, storeStampTakemeal);
        }
        return storeStampTakemeals;
    }

    /**
     * 是否有需要打印的小票信息
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @return true 有需要打印的出餐单
     */
    public boolean isStoreStampTakemeal(int merchantId, long storeId) {
        //如果能limit 0,1 条数据,则为true
        if (storeStampTakemealDAO.isStoreStampTakemeal(merchantId, storeId) >= 1) {
            return true;
        }
        return false;
    }

    public void updateStoreStampTakemealStatus(int merchantId, long storeId) {
        storeStampTakemealDAO.updateStoreStampTakemealStatus(merchantId, storeId);
    }
}
