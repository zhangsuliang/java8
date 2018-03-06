package com.huofu.module.i5wei.menu.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemCategory;
import com.huofu.module.i5wei.menu.service.StoreChargeItemCategoryService;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreChargeItemCategoryDTO;
import huofucore.facade.i5wei.menu.StoreChargeItemCategoryFacade;
import huofucore.facade.i5wei.menu.StoreChargeItemDTO;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * 收费项目分类接口
 *
 * Created by lixuwei on 16/3/21.
 */
@ThriftServlet(name = "storeChargeItemCategoryFacadeServlet", serviceClass = StoreChargeItemCategoryFacade.class)
@Component
public class StoreChargeItemCategoryFacadeImpl implements StoreChargeItemCategoryFacade.Iface {

    @Autowired
    private StoreChargeItemCategoryService storeChargeItemCategoryService;

    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Override
    public StoreChargeItemCategoryDTO saveChargeItemCategory(int merchantId, long storeId, int categoryId, String name) throws T5weiException, TException {

        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }

        if ("".equals(name)) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "store-chargeItemCategory name [" + name + "] can not be empty ");
        }

        StoreChargeItemCategory storeChargeItemCategory = storeChargeItemCategoryService.saveChargeItemCategory(merchantId, storeId, categoryId, name);

        StoreChargeItemCategoryDTO storeChargeItemCategoryDTO = BeanUtil.copy(storeChargeItemCategory, StoreChargeItemCategoryDTO.class);
        return storeChargeItemCategoryDTO;
    }

    @Override
    public List<StoreChargeItemCategoryDTO> getChargeItemCategorys(int merchantId, long storeId) throws TException {


        List<StoreChargeItemCategoryDTO> categoryDTOs = Lists.newArrayList();

        //get categorys
        List<StoreChargeItemCategory> storeChargeItemCategories = storeChargeItemCategoryService.getStoreChargeItemCategorys(merchantId, storeId);


        for (StoreChargeItemCategory chargeItemCategory : storeChargeItemCategories) {

            StoreChargeItemCategoryDTO categoryDTO = BeanUtil.copy(chargeItemCategory, StoreChargeItemCategoryDTO.class);
            categoryDTOs.add(categoryDTO);
        }
        return categoryDTOs;
    }

    @Override
    public void updateCategory(int merchantId, long storeId, int categoryId, List<Long> productIds, List<Long> chargeItemIds) throws T5weiException, TException {
        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }
        boolean existed = storeChargeItemCategoryService.checkStoreCategoryExist(merchantId, storeId, categoryId);
        if (!existed) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] invalid not have category [" + categoryId + "]");
        }
        storeChargeItemCategoryService.updateChargeItemAndProductCategory(merchantId, storeId, categoryId, productIds, chargeItemIds);
    }

    @Override
    public List<StoreChargeItemCategoryDTO> getCategories(int merchantId, long storeId, List<Integer> categoryIds, boolean loadProducts, boolean loadChargeItems, boolean loadDeleted) throws T5weiException, TException {
        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }

        return storeChargeItemCategoryService.getCategories(merchantId, storeId, categoryIds, loadProducts, loadChargeItems, loadDeleted);
    }
}
