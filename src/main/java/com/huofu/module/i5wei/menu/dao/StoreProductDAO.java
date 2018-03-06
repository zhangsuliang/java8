package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.CustomerTrafficSelectModeEnum;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreProductDAO extends AbsQueryDAO<StoreProduct> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Autowired
    private IdMakerUtil idMakerUtil;

    public List<Long> createIds(int size) {
        return this.idMakerUtil.nextIdsV2("store_product", size);
    }

    public List<StoreProduct> batchCreate(List<StoreProduct> list, List<Long> ids) {
        if (list.isEmpty()) {
            return list;
        }
        int i = 0;
        for (StoreProduct storeProduct : list) {
            storeProduct.setProductId(ids.get(i));
            i++;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void create(StoreProduct storeProduct) {
        storeProduct.setProductId(this.idMakerUtil.nextId("store_product"));
        this.addDbRouteInfo(storeProduct.getMerchantId(), storeProduct.getStoreId());
        super.create(storeProduct);
    }

    @Override
    public void update(StoreProduct storeProduct, StoreProduct snapshot) {
        this.addDbRouteInfo(storeProduct.getMerchantId(), storeProduct.getStoreId());
        super.update(storeProduct, snapshot);
    }

    /**
     * 加载店铺产品
     *
     * @param merchantId  商户id
     * @param storeId     店铺id
     * @param productId   产品id
     * @param forUpdate   是否执行 for update
     * @param forSnapshot 是否进行对象快照,只有进行数据更新的时候使用
     * @return
     * @throws T5weiException
     */
    public StoreProduct loadById(int merchantId, long storeId, long productId, boolean forUpdate, boolean forSnapshot) throws T5weiException {
        StoreProduct storeProduct = this.getById(merchantId, storeId, productId, forUpdate, forSnapshot);
        if (storeProduct == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] productId[" + productId + "] invalid");
        }
        return storeProduct;
    }

    /**
     * 获得店铺产品
     *
     * @param merchantId  商户id
     * @param storeId     店铺id
     * @param productId   产品id
     * @param forUpdate   是否执行 for update
     * @param forSnapshot 是否进行对象快照,只有进行数据更新的时候使用
     * @return
     */
    public StoreProduct getById(int merchantId, long storeId, long productId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        StoreProduct storeProduct = this.query.objById(StoreProduct.class, productId, forUpdate);
        if (storeProduct != null && forSnapshot) {
            storeProduct.snapshot();
        }
        return storeProduct;
    }

    public StoreProduct loadByIdForQuery(int merchantId, long storeId, long productId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StoreProduct storeProduct = this.query.objById(StoreProduct.class, productId);
        return storeProduct;
    }


    public Map<Long, StoreProduct> getMapInIds(int merchantId, long storeId, List<Long> productIds) {
        return this.getMapInIds(merchantId, storeId, productIds, false, false);
    }

    public Map<Long, StoreProduct> getMapInIds(int merchantId, long storeId, List<Long> productIds, boolean enableSlave, boolean enableCache) {
        if (productIds.isEmpty()) {
            return Maps.newHashMap();
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreProduct.class, "where store_id=?", "product_id", Lists.newArrayList(storeId), productIds);
    }

    public List<StoreProduct> getListInIds(int merchantId, long storeId, List<Long> productIds) {
        return this.getListInIds(merchantId, storeId, productIds, false, false);
    }

    public List<StoreProduct> getListInIds(int merchantId, long storeId, List<Long> productIds, boolean enableSlave, boolean enableCache) {
        if (productIds.isEmpty()) {
            return Lists.newArrayList();
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreProduct.class, "where store_id=?", "product_id", "order by product_id desc", Lists.newArrayList(storeId), productIds);
    }

    public void checkDuplicate(int merchantId, long storeId, long exceptedProductId, String name, String unit) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        int count = this.query.count2(StoreProduct.class, "where store_id=? and product_id!=? and name=? and unit=? and deleted=?", Lists.newArrayList(storeId, exceptedProductId, name, unit, false));
        if (count > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_DUPLICATE.getValue(), "storeId[" + storeId + "] name[" + name + "] unit[" + unit + "] duplicate");
        }
    }

    /**
     * 获得所有有效的产品
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreProduct> getList(int merchantId, long storeId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreProduct.class, "where store_id=? and deleted=? order by product_id desc", new Object[]{storeId, false});
    }

    /**
     * 更新产品是否需要出餐统计
     *
     * @param merchantId
     * @param storeId
     * @param products
     */
    public void batchUpdateMealStat(int merchantId, long storeId, List<StoreProduct> products) {
        List<Object[]> params = Lists.newArrayList();
        for (StoreProduct p : products) {
            params.add(new Object[]{p.isMealStat(), storeId, p.getProductId()});
        }
        this.addDbRouteInfo(merchantId, storeId);
        this.query.batchUpdate(StoreProduct.class, "set meal_stat=? where store_id=? and product_id=?", params);
    }

    /**
     * 更新产品的分单规则
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param products   产品id集合
     */
    public void batchUpdateDivRule(int merchantId, long storeId, List<StoreProduct> products) {
        List<Object[]> params = Lists.newArrayList();
        for (StoreProduct p : products) {
            params.add(new Object[]{p.getDivRule(), storeId, p.getProductId()});
        }
        this.addDbRouteInfo(merchantId, storeId);
        this.query.batchUpdate(StoreProduct.class, "set div_rule=? where store_id=? and product_id=?", params);
    }

    /**
     * 获得所有需要出餐统计的有效产品
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreProduct> getMealStatList(int merchantId, long storeId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreProduct.class, "where store_id=? and meal_stat=? and deleted=? order by product_id asc", new Object[]{storeId, true, false});
    }

    /**
     * 获得所有需要出餐统计的有效产品
     *
     * @param merchantId
     * @param storeId
     * @param portId
     * @return
     */
    public List<StoreProduct> getMealStatList(int merchantId, long storeId, long portId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreProduct.class, "where store_id=? and port_id=? and meal_stat=? and deleted=? order by product_id asc", new Object[]{storeId, portId, true, false});
    }

    /**
     * 获得出餐口相关的有效产品
     *
     * @param merchantId
     * @param storeId
     * @param portIds
     * @param enableSlave
     * @return
     */
    public List<StoreProduct> getStoreProductByPortIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        params.add(storeId);
        params.add(false);
        return this.query.listInValues2(StoreProduct.class, " where store_id=? and deleted=? ", "port_id", params, portIds);
    }

    public int clearPort(int merchantId, long storeId, long portId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreProduct.class, "set port_id=0 where " +
                "store_id=? and port_id=?", new Object[]{storeId, portId});
    }

    /**
     * 查询产品列表，返回加上成本字段，排序
     *
     * @param merchantId
     * @param storeId
     * @param sortType：0=成本，1=名称
     * @param desc：true=降序，false=升序
     * @param enableSlave
     * @param enableCache
     * @return
     */
    public List<StoreProduct> getStoreProductsCost(int merchantId, long storeId, int sortType, boolean desc, boolean enableSlave, boolean enableCache) {

        String sql = " where deleted = 0 and merchant_id=? and store_id=? ";

        if (sortType == 1) {
            sql += " order by convert(name using gbk) ";
        } else {
            sql += " order by prime_cost ";
        }

        if (desc) {
            sql += " desc ";
        } else {
            sql += " asc";
        }

        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreProduct.class, sql, new Object[]{merchantId, storeId});
    }

    /**
     * 查询所有的产品
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreProduct> getAllStoreProduct(int merchantId, long storeId) {

        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreProduct.class, null, null);
    }

    public List<StoreProduct> getStoreProductsByCustomerTraffic(int merchantId, long storeId, int customerTrafficSelectMode) {
        String sql = "where deleted = 0 and merchant_id=? and store_id=? ";

        if (CustomerTrafficSelectModeEnum.ALL.getValue() != customerTrafficSelectMode) {
            sql += " and enable_customer_traffic = ?";
            this.addDbRouteInfo(merchantId, storeId);
            if (customerTrafficSelectMode == CustomerTrafficSelectModeEnum.SELECT.getValue()) {
                return this.query.list(StoreProduct.class, sql, new Object[]{merchantId, storeId, 1});
            } else {
                return this.query.list(StoreProduct.class, sql, new Object[]{merchantId, storeId, 0});
            }
        } else {
            this.addDbRouteInfo(merchantId, storeId);
            return this.query.list(StoreProduct.class, sql, new Object[]{merchantId, storeId});
        }
    }

    /**
     * 根据分类ID 查询产品列表
     *
     * @param categoryIds
     */
    public List<StoreProduct> getProductByCategoryIds(int merchantId, long storeId, List<Integer> categoryIds, boolean loadDeleted) {

        if (categoryIds == null || categoryIds.size() == 0) {
            return Lists.newArrayList();
        }
        String sql = "";
        if (loadDeleted) {
            sql += " where merchant_id = ? and store_id = ? ";
        } else {
            sql += " where merchant_id = ? and store_id = ? and  deleted = 0 ";
        }
        List<Object> params = Lists.newArrayList();
        params.add(merchantId);
        params.add(storeId);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreProduct.class, sql, "category_id", params, categoryIds);
    }

    /**
     * 批量更新产品分类ID
     *
     * @param merchantId
     * @param storeId
     * @param products
     */
    public void batchUpdateCategory(int merchantId, long storeId, List<StoreProduct> products) {

        if (products == null || products.isEmpty()) {
            return;
        }
        List<Object[]> params = Lists.newArrayList();
        for (StoreProduct p : products) {
            params.add(new Object[]{p.getCategoryId(), p.getProductId()});
        }
        this.addDbRouteInfo(merchantId, storeId);
        this.query.batchUpdate(StoreProduct.class, "set category_id=? where product_id=?", params);
    }
	
    /**
     * 更新产品上的单位
     *
     * @param merchantId
     * @param storeId
     * @param productId
     * @param unit
     */
    public void updateStoreProductUnit(int merchantId, long storeId, long productId, String unit) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreProduct.class, "set unit=? where product_id=?", new Object[]{unit, productId});
    }
}
