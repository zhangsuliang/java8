package com.huofu.module.i5wei.mealport.dao;

import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.mealport.StoreMealPortTaskStatusEnum;
import huofuhelper.util.AbsQueryDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.mealport.entity.StoreMealTask;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealTaskDAO extends AbsQueryDAO<StoreMealTask> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreMealTask storeMealTask) {
        this.addDbRouteInfo(storeMealTask.getMerchantId(), storeMealTask.getStoreId());
        super.create(storeMealTask);
    }
    
    @Override
    public void replace(StoreMealTask storeMealTask) {
        this.addDbRouteInfo(storeMealTask.getMerchantId(), storeMealTask.getStoreId());
        super.replace(storeMealTask);
    }

    @Override
    public void update(StoreMealTask storeMealTask) {
        this.addDbRouteInfo(storeMealTask.getMerchantId(), storeMealTask.getStoreId());
        super.update(storeMealTask);
    }

    @Override
    public void update(StoreMealTask storeMealTask, StoreMealTask snapshot) {
        this.addDbRouteInfo(storeMealTask.getMerchantId(), storeMealTask.getStoreId());
        super.update(storeMealTask, snapshot);
    }
    
    @Override
    public void delete(StoreMealTask storeMealTask) {
        this.addDbRouteInfo(storeMealTask.getMerchantId(), storeMealTask.getStoreId());
        super.delete(storeMealTask);
    }
	
    public StoreMealTask getById(int merchantId, long storeId, long portId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(StoreMealTask.class, portId, forUpdate);
    }

    public List<StoreMealTask> getList(int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where merchant_id=? and store_id=? ";
        return this.query.list(StoreMealTask.class, sql, new Object[]{merchantId, storeId});
    }

    public List<StoreMealTask> getStoreMealPortsIdle(int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where merchant_id=? and store_id=? and task_status=? ";
        return this.query.list(StoreMealTask.class, sql, new Object[]{merchantId, storeId, StoreMealPortTaskStatusEnum.OFF.getValue()});
    }

    public List<StoreMealTask> getStoreAppMealTasks(int merchantId, long storeId, long appcopyId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where merchant_id=? and store_id=? and appcopy_id=? and task_status=? ";
        return this.query.list(StoreMealTask.class, sql, new Object[]{merchantId, storeId, appcopyId, StoreMealPortTaskStatusEnum.ON.getValue()});
    }

    public List<StoreMealTask> getStoreAppMealTasks(int merchantId, long storeId, long appcopyId, long checkoutType, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where merchant_id=? and store_id=? and appcopy_id=? and task_status=? and checkout_type=? ";
        return this.query.list(StoreMealTask.class, sql, new Object[]{merchantId, storeId, appcopyId, StoreMealPortTaskStatusEnum.ON.getValue(), checkoutType});
    }

    public List<StoreMealTask> getStoreAppTaskRegistMealPorts(int merchantId, long storeId, long appcopyId, List<Long> registPortIds, boolean enableSlave) {
        if (appcopyId == 0 || registPortIds == null || registPortIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        String sql = " where merchant_id=? and store_id=? and appcopy_id=? and task_status=? ";
        List<Object> values = new ArrayList<Object>();
        values.add(merchantId);
        values.add(storeId);
        values.add(appcopyId);
        values.add(StoreMealPortTaskStatusEnum.ON.getValue());
        if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreMealTask.class, sql, "port_id", values, registPortIds);
    }

    /**
     * 更新出餐口打印状态
     *
     * @param merchantId
     * @param storeId
     * @param appcopyId
     * @param printerParams
     */
    public void batchUpdatePrinterStatus(int merchantId, long storeId, Map<Long, Integer> printerParams) {
        if (storeId == 0 || printerParams == null || printerParams.isEmpty()) {
            return;
        }
        long time = System.currentTimeMillis();
        List<Object[]> params = Lists.newArrayList();
        for (long portId : printerParams.keySet()) {
            int printerStatus = printerParams.get(portId);
            params.add(new Object[]{printerStatus, time, time, time, portId});
        }
        this.addDbRouteInfo(merchantId, storeId);
        this.query.batchUpdate(StoreMealTask.class, " set printer_status=?, printer_time=?, meal_time=?, update_time=? where port_id=? ", params);
    }

    public Map<Long, StoreMealTask> getMapInIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreMealTask.class, "where store_id=?", "port_id", Lists.newArrayList(storeId), portIds);
    }

    public List<StoreMealTask> getByIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        if (portIds == null || portIds.isEmpty()) {
            return new ArrayList<StoreMealTask>();
        }
        if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreMealTask.class,
                " where store_id=? ", "port_id", Lists.newArrayList(storeId), portIds);
    }
}
