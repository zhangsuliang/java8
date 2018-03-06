package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;
import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreChargeSubitemDAO extends AbsQueryDAO<StoreChargeSubitem> {

    private RowMapper<Long> longRowMapper = (rs, rowNum) -> rs.getLong(1);

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreChargeSubitem storeChargeSubitem) {
        this.addDbRouteInfo(storeChargeSubitem.getMerchantId(), storeChargeSubitem.getStoreId());
        super.create(storeChargeSubitem);
    }

    public List<StoreChargeSubitem> batchCreate(List<StoreChargeSubitem> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        int merchantId = list.get(0).getMerchantId();
        long storeId = list.get(0).getStoreId();
        this.addDbRouteInfo(merchantId, storeId);
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreChargeSubitem storeChargeSubitem, StoreChargeSubitem snapshot) {
        this.addDbRouteInfo(storeChargeSubitem.getMerchantId(), storeChargeSubitem.getStoreId());
        super.update(storeChargeSubitem, snapshot);
    }

    @Override
    public void delete(StoreChargeSubitem storeChargeSubitem) {
        this.addDbRouteInfo(storeChargeSubitem.getMerchantId(), storeChargeSubitem.getStoreId());
        super.delete(storeChargeSubitem);
    }

    public void deleteByChargeItemId(int merchantId, long storeId, long chargeItemId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.delete(StoreChargeSubitem.class, "where store_id=? and charge_item_id=?", new Object[]{storeId, chargeItemId});
    }

    public int makeDeletedByChargeItemId(int merchantId, long storeId, long chargeItemId, boolean isDelete) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeSubitem.class, "set deleted=? where store_id=? and charge_item_id=?", new Object[]{isDelete, storeId, chargeItemId});
    }

    /**
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param chargeItemIds 收费项目id集合
     * @return key = 收费项目id value = 收费项目包含的子项目集合
     */
    public Map<Long, List<StoreChargeSubitem>> getMapForChargeItemIds(int merchantId, long storeId, List<Long> chargeItemIds, boolean enableSlave, boolean enableCache) {
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return new HashMap<>(0);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreChargeSubitem> storeChargeSubitemList = this.query.listInValues2(StoreChargeSubitem.class, "where store_id=? ", "charge_item_id", "order by main_flag desc, tid asc", Lists.newArrayList(storeId), chargeItemIds);
        Map<Long, List<StoreChargeSubitem>> map = Maps.newHashMap();
        for (StoreChargeSubitem storeChargeSubitem : storeChargeSubitemList) {
            List<StoreChargeSubitem> list = map.get(storeChargeSubitem.getChargeItemId());
            if (list == null) {
                list = Lists.newArrayList();
                map.put(storeChargeSubitem.getChargeItemId(), list);
            }
            list.add(storeChargeSubitem);
        }
        return map;
    }

    /**
     * 获得包含指定产品的有效收费项目id
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param prouctId   产品id
     * @return 收费项目id集合
     */
    public List<Long> getChargeItemIdsContainProduct(int merchantId, long storeId, long prouctId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeSubitem.class);
        String tName = dalInfo.getRealTable(StoreChargeSubitem.class);
        String sql = "select distinct(charge_item_id) from " + tName + " where store_id=? and product_id=? and deleted=?";
        return this.query.getJdbcSupport().list(sql, new Object[]{storeId, prouctId, false}, longRowMapper);
    }

    /**
     * 获得含有指定收费项目的所有产品id
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param chargeItemIds 收费项目id集合
     * @return 产品id集合
     */
    public List<Long> getProductIdsInChargeItemIds(int merchantId, long storeId, List<Long> chargeItemIds) {
        if (chargeItemIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeSubitem.class);
        String tName = dalInfo.getRealTable(StoreChargeSubitem.class);
        List<Object> params = Lists.newArrayList();
        params.add(storeId);
        params.addAll(chargeItemIds);
        return this.query.getJdbcSupport().list("select distinct(product_id) from " + tName + " where store_id=? and " + Query.createInSql("charge_item_id", chargeItemIds.size()), Query.buildArgs(params), longRowMapper);
    }

    public List<StoreChargeSubitem> getListByChargeItemId(int merchantId, long storeId, long chargeItemId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeSubitem.class, "where store_id=? and charge_item_id=?", new Object[]{storeId, chargeItemId});
    }

    /**
     * 根据productIds 得到关联的收费项目ID
     * <b color="yellow">收费项目是 单品</b>
     *
     * @param merchantId
     * @param storeId
     * @param productIds
     * @return
     */
    public List<Long> getOnlyChargeItemIdsContainProductIds(int merchantId, long storeId, List<Long> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            return Lists.newArrayList();
        }

        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeSubitem.class);
        String tName = dalInfo.getRealTable(StoreChargeSubitem.class);
        String insql = Query.createInSql("a.product_id", productIds.size());
        String sql = "select a.charge_item_id from " + tName + " as a " +
                " inner join " + tName + " as b on a.charge_item_id = b.charge_item_id " +
                " where a.store_id = ? and " + insql + " group by a.charge_item_id  having count(a.charge_item_id) = 1 ";
        List<Object> params = Lists.newArrayList();
        params.add(storeId);
        params.addAll(productIds);
        return this.query.getJdbcSupport().list(sql, params.toArray(), longRowMapper);
    }

    /**
     * 根据单品收费项目获得 productIds
     */
    public List<Long> getChargeItemIdsByOnlyChargeItem(int merchantId, long storeId, List<Long> chargeItemIds) {

        if (chargeItemIds == null || chargeItemIds.size() <= 0) {
            return Lists.newArrayList();
        }
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeSubitem.class);
        String tName = dalInfo.getRealTable(StoreChargeSubitem.class);
        String insql = Query.createInSql("charge_item_id", chargeItemIds.size());
        String sql = "select charge_item_id from " + tName + " where store_id = ? and " + insql + " group by charge_item_id having count(tid) = 1 ";
        List<Object> params = Lists.newArrayList();
        params.add(storeId);
        params.addAll(chargeItemIds);
        return this.query.getJdbcSupport().list(sql, params.toArray(), longRowMapper);
    }

    /**
     * 根据收费项目ID集合 得到收费子项集合
     */
    public List<StoreChargeSubitem> getListByChargeItemIds(int merchantId, long storeId, List<Long> chargeItemIds) {
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return Lists.newArrayList();
        }
        this.addDbRouteInfo(merchantId, storeId);
        DALStatus.setSlaveMode();
        return this.query.listInValues2(StoreChargeSubitem.class, "where merchant_id =? and store_id =?", "charge_item_id",
                Lists.newArrayList(merchantId, storeId), chargeItemIds);
    }
}
