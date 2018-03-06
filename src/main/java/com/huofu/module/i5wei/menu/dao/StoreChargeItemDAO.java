package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemCategory;
import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.CustomerTrafficSelectModeEnum;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionQueryParam;
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
public class StoreChargeItemDAO extends AbsQueryDAO<StoreChargeItem> {

    @Autowired
    private IdMakerUtil idMakerUtil;

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    public List<Long> createIds(int size) {
        return this.idMakerUtil.nextIdsV2("store_charge_item", size);
    }

    public List<StoreChargeItem> batchCreate(List<StoreChargeItem> list, List<Long> ids) {
        if (list.isEmpty()) {
            return list;
        }
        int i = 0;
        for (StoreChargeItem storeChargeItem : list) {
            storeChargeItem.setChargeItemId(ids.get(i));
            i++;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void create(StoreChargeItem storeChargeItem) {
        this.addDbRouteInfo(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId());
        storeChargeItem.setChargeItemId(idMakerUtil.nextId("store_charge_item"));
        super.create(storeChargeItem);
    }

    @Override
    public void update(StoreChargeItem storeChargeItem, StoreChargeItem snapshot) {
        this.addDbRouteInfo(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId());
        super.update(storeChargeItem, snapshot);
    }

    @Override
    public void delete(StoreChargeItem storeChargeItem) {
        this.addDbRouteInfo(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId());
        super.delete(storeChargeItem);
    }

    public void checkDuplicate(int merchantId, long storeId, long exceptChargeItemId, String name) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        int count = this.query.count(StoreChargeItem.class, "where store_id=? and deleted=? and charge_item_id!=? and name=?", new Object[]{storeId, false, exceptChargeItemId, name});
        if (count > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_DUPLICATE.getValue(), "StoreChargeItem merchantId[" + merchantId + "] storeId[" + storeId + "] exceptChargeItemId[" + exceptChargeItemId + "] name[" + name + "] duplicate");
        }
    }

    public StoreChargeItem getById(int merchantId, long storeId, long chargeItemId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        StoreChargeItem storeChargeItem = this.query.objById(StoreChargeItem.class, chargeItemId, forUpdate);
        if (storeChargeItem != null && forSnapshot) {
            storeChargeItem.snapshot();
        }
        return storeChargeItem;
    }

    public StoreChargeItem loadById(int merchantId, long storeId, long chargeItemId, boolean forUpdate, boolean forSnapshot) throws T5weiException {
        StoreChargeItem storeChargeItem = this.getById(merchantId, storeId, chargeItemId, forUpdate, forSnapshot);
        if (storeChargeItem == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(), StoreChargeItem.class.getName() + " merchantId[" + merchantId + "] storeId[" + storeId + "] chargeItemId[" + chargeItemId + "] invalid");
        }
        return storeChargeItem;
    }

    public StoreChargeItem getByIdForQuery(int merchantId, long storeId, long chargeItemId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StoreChargeItem storeChargeItem = this.query.objById(StoreChargeItem.class, chargeItemId);
        return storeChargeItem;
    }

    public StoreChargeItem loadByIdForQuery(int merchantId, long storeId, long chargeItemId, boolean enableSlave, boolean enableCache) throws T5weiException {
        StoreChargeItem storeChargeItem = this.getByIdForQuery(merchantId, storeId, chargeItemId, enableSlave, enableCache);
        if (storeChargeItem == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(), StoreChargeItem.class.getName() + " merchantId[" + merchantId + "] storeId[" + storeId + "] chargeItemId[" + chargeItemId + "] invalid");
        }
        return storeChargeItem;
    }

    public List<StoreChargeItem> getListInIds(int merchantId, long storeId, List<Long> chargeItemIdList, boolean enableSlave, boolean enableCache) {
        if (chargeItemIdList.isEmpty()) {
            return new ArrayList<>(0);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreChargeItem.class, "where store_id=?", "charge_item_id", "order by charge_item_id desc", Lists.newArrayList(storeId), chargeItemIdList);
    }

    public Map<Long, StoreChargeItem> getMapInIds(int merchantId, long storeId, List<Long> chargeItemIdList, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreChargeItem.class, "where store_id=?", "charge_item_id", Lists.newArrayList(storeId), chargeItemIdList);
    }

    public Map<Long, StoreChargeItem> getMapInIdsForWechat(int merchantId, long storeId, List<Long> chargeItemIdList) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreChargeItem.class, "where store_id=? and enable_wechat=?", "charge_item_id", Lists.newArrayList(storeId, true), chargeItemIdList);
    }

    public List<StoreChargeItem> getListByStoreId(int merchantId, long storeId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeItem.class, "where store_id=? and deleted=? order by charge_item_id desc", new Object[]{storeId, false});
    }

    /**
     * 查看有效期内的收费项目是否存在
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param chargeItemIds 收费项目id
     * @return
     */
    public int countContainProduct(int merchantId, long storeId, List<Long> chargeItemIds) {
        if (chargeItemIds.isEmpty()) {
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = Lists.newArrayList();
        params.add(storeId);
        params.addAll(chargeItemIds);
        int count = this.query.count2(StoreChargeItem.class, "where store_id=? and " + Query.createInSql("charge_item_id", chargeItemIds.size()), params);
        return count;
    }

    public int clearPort(int merchantId, long storeId, long portId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeItem.class, "set port_id=0 where " +
                "store_id=? and port_id=?", new Object[]{storeId, portId});
    }

    public String getChargeItemRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeItem.class);
        return dalInfo.getRealTable(StoreChargeItem.class);
    }

    public String getChargeItemCategoryRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeItemCategory.class);
        return dalInfo.getRealTable(StoreChargeItemCategory.class);
    }

    public List<Map<String, Object>> getChargeItemWithCategory(int merchantId, long storeId) {

        String chargeItemTable = getChargeItemRealName(merchantId, storeId);
        String chargeItemCategoryTable = getChargeItemCategoryRealName(merchantId, storeId);


        String sql = "select item.charge_item_id as chargeItemId, item.name as chargeItemName, item.category_id as categoryId, category.name as categoryName from " +
                chargeItemTable + " as item " +
                "left join " + chargeItemCategoryTable + " as category " +
                " on (item.category_id = category.category_id) where item.store_id = ?";

        return this.query.getJdbcSupport().getJdbcTemplate().queryForList(sql, new Object[]{storeId});
    }

    /**
     * 查询当前店铺下的所有的收费项目
     *
     * @param merchantId
     * @param storeId
     */
    public List<StoreChargeItem> getStoreChargeItems(int merchantId, long storeId) {
        String sql = "where merchant_id = ? and store_id = ? and deleted = ? ";
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeItem.class, sql, new Object[]{merchantId, storeId, 0});
    }

    /**
     * 查询当前店铺下的所有的收费项目
     *
     * @param merchantId
     * @param storeId
     * @param chargeItemIds
     */
    public List<StoreChargeItem> getStoreChargeItems(int merchantId, long storeId, List<Long> chargeItemIds) {
        if (chargeItemIds.isEmpty()) {
            return Lists.newArrayList();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues(StoreChargeItem.class, null, "charge_item_id", null, chargeItemIds.toArray());
    }

    public List<StoreChargeItem> getStoreChargeItemList(StoreChargeItemPromotionQueryParam param) {
        this.addDbRouteInfo(param.getMerchantId(), param.getStoreId());
        StringBuffer sql = new StringBuffer();
        List<Object> params = new ArrayList<>();
        sql.append(" where merchant_id=? and store_id=? ");
        params.add(param.getMerchantId());
        params.add(param.getStoreId());
        if (param.getChargeItemName() != null && !param.getChargeItemName().equals("")) {
            sql.append(" and name like ?");
            params.add("%" + param.getChargeItemName() + "%");
        }
        return this.query.list(StoreChargeItem.class, sql.toString(), params.toArray());
    }

    /**
     * 根据收费项目
     *
     * @param merchantId
     * @param storeId
     * @param categoryIds
     * @param loadDeleted
     * @return
     */
    public List<StoreChargeItem> getChargeItemByCategoryIds(int merchantId, long storeId, List<Integer> categoryIds, boolean loadDeleted) {

        if (categoryIds == null || categoryIds.size() == 0) {
            return Lists.newArrayList();
        }

        String sql = "";
        if (loadDeleted) {
            sql += " where merchant_id = ? and store_id = ?  ";
        } else {
            sql += " where merchant_id = ? and store_id = ? and deleted = 0 ";
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = Lists.newArrayList();
        params.add(merchantId);
        params.add(storeId);
        return this.query.listInValues2(StoreChargeItem.class, sql, "category_id",params, categoryIds);
    }

    /**
     * 批量更新分类的ID
     *
     * @param merchantId
     * @param storeId
     * @param updateChargeItems
     */
    public void batchUpdateCategory(int merchantId, long storeId, List<StoreChargeItem> updateChargeItems) {

        if (updateChargeItems == null || updateChargeItems.isEmpty()) {
            return;
        }

        List<Object[]> params = Lists.newArrayList();
        for (StoreChargeItem chargeItem : updateChargeItems) {
            params.add(new Object[]{chargeItem.getCategoryId(), chargeItem.getChargeItemId()});
        }
        this.addDbRouteInfo(merchantId, storeId);
        this.query.batchUpdate(StoreChargeItem.class, "set category_id=? where charge_item_id=?", params);
    }

    /**
     *
     * 批量更新收费项目的 div_rule 和 open_div_rule
     */
    public void batchUpdateDivRuleAndOpenDivRule(List<StoreChargeItem> storeChargeItems){
        if(storeChargeItems == null || storeChargeItems.isEmpty()){
            return ;
        }
        this.addDbRouteInfo(storeChargeItems.get(0).getMerchantId(),storeChargeItems.get(0).getStoreId());
        List<Object[]> valueList = new ArrayList<>();
        for(StoreChargeItem storeChargeItem : storeChargeItems){
            valueList.add(new Object[]{storeChargeItem.getDivRule(),storeChargeItem.isOpenDivRule(),storeChargeItem.getStoreId(),storeChargeItem.getChargeItemId()});
        }
        this.query.batchUpdate(StoreChargeItem.class,"set div_rule=?,open_div_rule=? where store_id=? and charge_item_id=?",valueList);
    }

	// add by yangfei 2016-12-13 批量操作收费项目简单信息更新
	public void batchUpdateChargeItemMeituanEnable(int merchantId, long storeId, List<Long> chargeItemIds, boolean enable) {
		this.addDbRouteInfo(merchantId, storeId);
		List<Object[]> params = new ArrayList<Object[]>();
		for (Long chargeItemId : chargeItemIds) {
			params.add(new Object[]{enable, chargeItemId});
		}
		this.query.batchUpdate(StoreChargeItem.class, "set meituan_waimai_enabled = ? where charge_item_id = ?", params);
	}

	public void batchUpdateCustomerTraffic(List<StoreChargeItem> storeChargeItems) {
		if (storeChargeItems == null || storeChargeItems.isEmpty()) {
			return;
		}
		this.addDbRouteInfo(storeChargeItems.get(0).getMerchantId(), storeChargeItems.get(0).getStoreId());
		List<Object[]> valueList = new ArrayList<>();
		for (StoreChargeItem storeChargeItem : storeChargeItems) {
			valueList.add(new Object[]{storeChargeItem.getCustomerTraffic(), storeChargeItem.getChargeItemId()});
		}
		this.query.batchUpdate(StoreChargeItem.class, "set customer_traffic = ? where charge_item_id = ?", valueList);
	}

}
