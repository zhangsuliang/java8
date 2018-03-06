package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.entity.ItemUnit;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class ItemUnitDAO extends AbsQueryDAO<ItemUnit> {

    public ItemUnit loadById(int itemUnitId) throws T5weiException {
        ItemUnit itemUnit = this.query.objById(ItemUnit.class, itemUnitId);
        if (itemUnit == null) {
            throw new T5weiException(T5weiErrorCodeType.ITEM_UNIT_INVALID.getValue(), "itemUnitId[" + itemUnitId + "] invalid");
        }
        return itemUnit;
    }

    public List<ItemUnit> getList(int unitType, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        String sql = "";
        List<Object> params = Lists.newArrayList();
        if (unitType > 0) {
            sql = " where unit_type = ? ";
            params.add(unitType);
        }
        return this.query.list2(ItemUnit.class, sql, params);
    }

}
