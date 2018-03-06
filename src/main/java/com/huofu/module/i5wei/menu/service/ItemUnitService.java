package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.dao.ItemUnitDAO;
import com.huofu.module.i5wei.menu.entity.ItemUnit;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.itemunit.ItemUnitParam;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by akwei on 5/12/15.
 */
@Service
public class ItemUnitService {

    @Autowired
    private ItemUnitDAO itemUnitDAO;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public ItemUnit saveStoreItemUnit(ItemUnitParam param) throws T5weiException, TException {
        try {
            if (param.getItemUnitId() > 0) {
                ItemUnit itemUnit = this.itemUnitDAO.loadById(param.getItemUnitId());
                BeanUtil.copy(param, itemUnit);
                itemUnit.update();
                return itemUnit;
            }
            ItemUnit itemUnit = new ItemUnit();
            BeanUtil.copy(param, itemUnit);
            itemUnit.create();
            return itemUnit;
        } catch (DuplicateKeyException e) {
            throw new T5weiException(T5weiErrorCodeType.ITEM_UNIT_NAME_DUPLICATE.getValue(), "item name[" + param.getName() + "] duplicate");
        }
    }

    public ItemUnit loadItemUnitByItemUnitId(int itemUnitId) throws T5weiException {
        return this.itemUnitDAO.loadById(itemUnitId);
    }

    public List<ItemUnit> getItemUnits() throws TException {
        return this.itemUnitDAO.getList(0, true, true);
    }

    public void deleteStoreItemUnit(int itemUnitId) {
        try {
            ItemUnit itemUnit = this.itemUnitDAO.loadById(itemUnitId);
            itemUnit.delete();
        } catch (T5weiException e) {
            //ignore
        }
    }

    public List<ItemUnit> getItemUnitsByUnitType(int unitType){
        return this.itemUnitDAO.getList(unitType, true, true);
    }


}
