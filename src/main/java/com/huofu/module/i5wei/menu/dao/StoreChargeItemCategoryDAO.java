package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemCategory;
import halo.query.Query;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreChargeItemCategoryDAO extends AbsQueryDAO<StoreChargeItemCategory> {

    @Autowired
    private IdMakerUtil idMakerUtil;

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreChargeItemCategory storeChargeItemCategory) {
        Long id = idMakerUtil.nextId2("tb_charge_item_category_seq");
        storeChargeItemCategory.setCategoryId(Integer.parseInt(id.toString()));
        super.create(storeChargeItemCategory);
    }

    /**
     * 查询可用的分类集合
     *
     * @param merchantId
     * @param storeId
     * @param categoryIds
     * @param enableSlave
     * @param enableCache @return
     */
    public List<StoreChargeItemCategory> getStoreChargeItemCategorys(int merchantId, long storeId, List<Integer> categoryIds, boolean enableSlave, boolean enableCache) {

        List<Object> params = Lists.newArrayList();
        //如果分类ID集合不为空
        String sql = "where merchant_id = ? and store_id = ? and deleted = 0 ";
        params.add(merchantId);
        params.add(storeId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            String categoryIdInSql = Query.createInSql("category_id", categoryIds.size());
            params.addAll(categoryIds);
            sql += " and " + categoryIdInSql;
            this.addDbRouteInfo(merchantId, storeId);
            return this.query.list(StoreChargeItemCategory.class, sql, params.toArray());
        } else {
            this.addDbRouteInfo(merchantId, storeId);
            return this.query.list(StoreChargeItemCategory.class, sql, params.toArray());
        }
    }

    public StoreChargeItemCategory getStoreChargeItemCategoryByName(int merchantId, long storeId, String name) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreChargeItemCategory.class, "where name = ? and deleted=?", new Object[]{name, false});
    }

    /**
     * 根据分类ID 加载对象
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @param enableSlave
     * @param enableCache
     * @return
     */
    public StoreChargeItemCategory getById(int merchantId, long storeId, int categoryId, boolean enableSlave, boolean enableCache) {

        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objByIds(StoreChargeItemCategory.class, new Object[]{categoryId});
    }

    /**
     * 根据分类ID集合 加载对象
     *
     * @param merchantId
     * @param storeId
     * @param categoryIds
     * @return
     */
    public List<StoreChargeItemCategory> getCategoryByIds(int merchantId, long storeId, Set<Integer> categoryIds) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return Lists.newArrayList();
        }
        String categoryInSql = Query.createInSql("category_id", categoryIds.size());
        String sql = " where merchant_id =? and store_id = ? and " + categoryInSql;
        List<Object> params = Lists.newArrayList();
        params.add(merchantId);
        params.add(storeId);
        params.addAll(categoryIds);

        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list2(StoreChargeItemCategory.class, sql, params);
    }
}
