package com.huofu.module.i5wei.menu.facade;

import com.huofu.module.i5wei.menu.entity.ItemUnit;
import com.huofu.module.i5wei.menu.service.ItemUnitService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.itemunit.ItemUnitDTO;
import huofucore.facade.i5wei.itemunit.ItemUnitFacade;
import huofucore.facade.i5wei.itemunit.ItemUnitParam;
import huofuhelper.util.ValidateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by akwei on 5/12/15.
 */
@Component
@ThriftServlet(name = "itemUnitFacadeServlet", serviceClass = ItemUnitFacade.class)
public class ItemUnitFacadeImpl implements ItemUnitFacade.Iface {

    @Autowired
    private ItemUnitService itemUnitService;

    @Override
    public ItemUnitDTO saveItemUnit(ItemUnitParam param) throws T5weiException, TException {
        if (!ValidateUtil.testLength(param.getName(), 1, 20, false)) {
            throw new T5weiException(T5weiErrorCodeType.ITEM_UNIT_NAME_INVALID.getValue(), "itemUnit name[" + param.getName() + "] invalid");
        }
        ItemUnit itemUnit = this.itemUnitService.saveStoreItemUnit(param);
        return this.buildItemUnitDTO(itemUnit);
    }

    @Override
    public List<ItemUnitDTO> getItemUnits() throws TException {
        List<ItemUnit> list = this.itemUnitService.getItemUnits();
        return BeanUtil.copyList(list, ItemUnitDTO.class);
    }

    @Override
    public void deleteStoreItemUnit(int itemUnitId) throws TException {
        this.itemUnitService.deleteStoreItemUnit(itemUnitId);
    }

    @Override
    public ItemUnitDTO getItemUnitByItemUnitId(int itemUnitId) throws T5weiException, TException {
        ItemUnit itemUnit = this.itemUnitService.loadItemUnitByItemUnitId(itemUnitId);
        return this.buildItemUnitDTO(itemUnit);
    }

    @Override
    public List<ItemUnitDTO> getItemUnitsByUnitType(int unitType) throws T5weiException, TException {
        List<ItemUnit> list = this.itemUnitService.getItemUnitsByUnitType(unitType);
        return BeanUtil.copyList(list, ItemUnitDTO.class);
    }

    private ItemUnitDTO buildItemUnitDTO(ItemUnit itemUnit) {
        ItemUnitDTO itemUnitDTO = new ItemUnitDTO();
        BeanUtil.copy(itemUnit, itemUnitDTO);
        return itemUnitDTO;
    }
}
