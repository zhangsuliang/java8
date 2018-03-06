package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.dao.UpdateProductPortParam;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;
import com.huofu.module.i5wei.menu.entity.StoreDateBizSetting;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.remark.dao.StoreProductRemarkDAO;
import com.huofu.module.i5wei.remark.entity.StoreProductRemark;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofucore.facade.i5wei.menu.QueryStoreProductParam;
import huofucore.facade.i5wei.menu.QueryStoreProductType;
import huofucore.facade.i5wei.menu.StoreProductParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 3/6/15.
 */
@Service
public class StoreProductService {

    @Resource
    private StoreProductDAO storeProductDAO;

    @Resource
    private StoreChargeSubitemDAO storeChargeSubitemDAO;

    @Resource
    private StoreChargeItemDAO storeChargeItemDAO;

    @Resource
    private StoreProductRemarkDAO storeProductRemarkDAO;

    @Resource
    private StoreMealTakeupDAO storeMealTakeupDAO;

    @Resource
    private StoreMealPortDAO storeMealPortDAO;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Resource
    private StoreDateBizSettingDAO storeDateBizSettingDAO;

    @Resource
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    private static Logger logger = Logger.getLogger(StoreProductService.class);

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreProduct createStoreProduct(StoreProductParam param) throws T5weiException {
        this.storeProductDAO.checkDuplicate(param.getMerchantId(), param.getStoreId(), param.getProductId(), param.getName(), param.getUnit());
        StoreProduct storeProduct = new StoreProduct();
        BeanUtil.copy(param, storeProduct, true);
        storeProduct.initForCreate(System.currentTimeMillis());
        //默认周营业时段计划库存
        storeProduct.setInvType(ProductInvTypeEnum.WEEK.getValue());
        storeProduct.create();
        this.processRemark(storeProduct);
        return storeProduct;
    }

    private void processRemark(StoreProduct storeProduct) {
        List<String> remarks = storeProduct.getRemarks();
        if (remarks == null) {
            return;
        }
        int merchantId = storeProduct.getMerchantId();
        long storeId = storeProduct.getStoreId();
        for (String remark : remarks) {
            if (storeProductRemarkDAO.getById(merchantId, storeId, remark) == null) {
                StoreProductRemark storeProductRemark = new StoreProductRemark();
                storeProductRemark.setMerchantId(merchantId);
                storeProductRemark.setStoreId(storeId);
                storeProductRemark.setRemark(remark);
                storeProductRemark.init4Create();
                try {
                    storeProductRemark.create();
                } catch (DuplicateKeyException e) {
                    storeProductRemark = storeProductRemarkDAO.getById
                            (merchantId, storeId, remark);
                    storeProductRemark.snapshot();
                    storeProductRemark.setUpdateTime(System.currentTimeMillis());
                    storeProductRemark.update();
                }
            }
        }
    }

    /**
     * 更新产品信息
     *
     * @param param 产品对象
     * @return 更新后的数据
     * @throws T5weiException 产品名称与单位组合重复
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreProduct updateStoreProduct(StoreProductParam param) throws T5weiException {
        StoreProduct storeProduct = this.storeProductDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getProductId(), true, true);
        long oldPortId = storeProduct.getPortId();
        boolean oldEnableCustomerTraffic = storeProduct.isEnableCustomerTraffic();
        BeanUtil.copy(param, storeProduct, true);
        this.storeProductDAO.checkDuplicate(storeProduct.getMerchantId(), storeProduct.getStoreId(), storeProduct.getProductId(), storeProduct.getName(), storeProduct.getUnit());
        if (storeProduct.getInvType() == ProductInvTypeEnum.NONE.getValue()) {
            //默认周营业时段计划库存
            storeProduct.setInvType(ProductInvTypeEnum.WEEK.getValue());
        }
        storeProduct.setUpdateTime(System.currentTimeMillis());
        //更新产品之前对 判断产品 <是否开启入客数> 需不需要更新
        if (param.isSetEnableCustomerTraffic() && oldEnableCustomerTraffic != storeProduct.isEnableCustomerTraffic()) {
            //对产品关联的收费项目进行更新
            updateStoreProductCustomerTraffic(storeProduct);
        }
        storeProduct.update();
        this.processRemark(storeProduct);
        if (param.isSetPortId() && oldPortId != param.getPortId()) {
            //获取产品所关联的收费项目
            List<Long> chargeItemds = this.storeChargeSubitemDAO
                    .getChargeItemIdsContainProduct
                            (storeProduct.getMerchantId(), storeProduct.getStoreId(),
                                    storeProduct.getProductId());
            List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO
                    .getListInIds(storeProduct.getMerchantId(), storeProduct
                            .getStoreId(), chargeItemds, false, false);
            List<UpdateProductPortParam> updateProductPortParams = Lists
                    .newArrayList();
            //对没有出餐口的收费项目进行处理
            storeChargeItems.stream().filter(item -> item.getPortId() <= 0).forEach(item -> {
                UpdateProductPortParam updateProductPortParam = new
                        UpdateProductPortParam();
                updateProductPortParam.setChargeItemId(item.getChargeItemId());
                updateProductPortParam.setProductId(storeProduct.getProductId());
                updateProductPortParams.add(updateProductPortParam);
            });
            boolean hasPackagePort = false;
            StoreMealPort storeMealPort = this.storeMealPortDAO.getParkagePort(storeProduct.getMerchantId(), storeProduct.getStoreId(), false);
            if (storeMealPort != null) {
                hasPackagePort = true;
            }
            if (updateProductPortParams.size() > 0) {
                //进行出餐口
                this.storeMealTakeupDAO.updateProductPort(param.getMerchantId(),
                        param.getStoreId(), updateProductPortParams, param
                                .getPortId(), hasPackagePort);
            }
        }
        return storeProduct;
    }

    /**
     * 删除产品
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param productId  产品id
     * @throws T5weiException 无效产品
     */
    public void deleteStoreProduct(int merchantId, long storeId, long productId) throws T5weiException {
        List<Long> chargeItemIds = this.storeChargeSubitemDAO.getChargeItemIdsContainProduct(merchantId, storeId, productId);
        long now = System.currentTimeMillis();
        int count = this.storeChargeItemDAO.countContainProduct(merchantId, storeId, chargeItemIds);
        if (count > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INUSE.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] productId[" + productId + "] in use");
        }
        StoreProduct storeProduct = this.storeProductDAO.loadById(merchantId, storeId, productId, false, false);
        if (storeProduct.isDeleted()) {
            return;
        }
        storeProduct.makeDeleted(now);
    }

    /**
     * 获得店铺所有有效的产品
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @return 产品集合
     */
    public List<StoreProduct> getStoreProductList(int merchantId, long storeId, boolean loadMealPort) {
        QueryStoreProductParam param = new QueryStoreProductParam();
        param.setMerchantId(merchantId);
        param.setStoreId(storeId);
        param.setLoadMealPort(loadMealPort);
        param.setQueryType(QueryStoreProductType.ALL);
        return this.getStoreProducts(param);
    }

    public StoreProduct getStoreProduct(int merchantId, long storeId, long productId, boolean loadMealPort) throws T5weiException {
        StoreProduct storeProduct = this.storeProductDAO.loadByIdForQuery(merchantId, storeId, productId, true, true);
        if (storeProduct != null) {
            if (loadMealPort) {
                StoreMealPort storeMealPort = this.storeMealPortDAO.getById
                        (merchantId, storeId, storeProduct.getPortId());
                storeProduct.setStoreMealPort(storeMealPort);
            }
        }
        return storeProduct;
    }

    public Map<Long, StoreProduct> getStoreProductMapInIds(int merchantId, long storeId, List<Long> productIds) {
        return this.storeProductDAO.getMapInIds(merchantId, storeId, productIds, true, true);
    }

    public List<StoreProduct> getStoreProductInIds(int merchantId, long storeId, List<Long> productIds) {
        return this.storeProductDAO.getListInIds(merchantId, storeId, productIds, true, true);
    }

    public StoreProduct saveStoreProductRemarks(int merchantId, long storeId, long productId, List<String> remarks) throws T5weiException {
        StoreProduct storeProduct = this.storeProductDAO.loadById(merchantId,
                storeId, productId, false, true);
        storeProduct.setRemarks(remarks);
        storeProduct.setUpdateTime(System.currentTimeMillis());
        storeProduct.update();
        return storeProduct;
    }

    public StoreProduct getStoreProductByName(int merchantId, long storeId, String name) throws T5weiException {
        List<StoreProduct> list = this.storeProductDAO.getList(merchantId, storeId, true, true);
        for (StoreProduct storeProduct : list) {
            if (storeProduct.getName().equals(name)) {
                return storeProduct;
            }
        }
        throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(),
                "merchantId[" + merchantId + "] storeId[" + storeId + "] name[" + name + "] " +
                        "storeProduct invalid");
    }

    /**
     * 更新菜品的 成本
     *
     * @param merchantId
     * @param storeId
     * @param primeCastMap
     * @return
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> updatePrimeCast(int merchantId, long storeId, Map<Long, Integer> primeCastMap) throws T5weiException, TException {

        List<Map<String, Object>> unsetList = Lists.newArrayList();
        List<Map<String, Object>> updateList = Lists.newArrayList();

        // 加载菜品集合
        ArrayList<Long> storeProductIds = Lists.newArrayList();
        storeProductIds.addAll(primeCastMap.keySet());
        List<StoreProduct> productList = storeProductDAO.getListInIds(merchantId, storeId, storeProductIds);

        for (StoreProduct storeProduct : productList) {

            //判断这个产品是否需要修改
            Integer newPrimeCast = primeCastMap.get(storeProduct.getProductId());

            boolean primeCostSet = storeProduct.isPrimeCostSet();
            if (!primeCostSet) {
                //从未设置过
                updateProductPrimeCost(newPrimeCast, storeProduct);

                Map<String, Object> unSetMap = new HashMap<>();
                unSetMap.put("productId", storeProduct.getProductId());
                unSetMap.put("primeCost", storeProduct.getPrimeCost());
                unSetMap.put("createTime", System.currentTimeMillis());
                unsetList.add(unSetMap);
            } else {
                //设置过了 判断成本值是否有改动
                long oldPrimeCost = storeProduct.getPrimeCost();
                if (oldPrimeCost != newPrimeCast) {
                    updateProductPrimeCost(newPrimeCast, storeProduct);

                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("productId", storeProduct.getProductId());
                    updateMap.put("primeCost", storeProduct.getPrimeCost());
                    updateMap.put("createTime", System.currentTimeMillis());
                    updateList.add(updateMap);
                }
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", productList);
        resultMap.put("unset", unsetList);
        resultMap.put("update", updateList);

        return resultMap;
    }

    public void updateProductPrimeCost(Integer primeCost, StoreProduct storeProduct) {
        //更新菜品的 成本
        storeProduct.snapshot();
        storeProduct.setPrimeCostSet(true);
        storeProduct.setPrimeCost(primeCost);
        storeProduct.update();

        List<Long> chargeItemds = this.storeChargeSubitemDAO
                .getChargeItemIdsContainProduct
                        (storeProduct.getMerchantId(), storeProduct.getStoreId(),
                                storeProduct.getProductId());

        //更改收费项目的 成本设置状态
        storeChargeItemService.updateChargeItemPrimeCastSet(storeProduct.getMerchantId(), storeProduct.getStoreId(), storeProduct.getProductId(), chargeItemds);
    }

    /**
     * 查询产品列表，返回加上成本字段，排序
     *
     * @param merchantId
     * @param storeId
     * @param sortType：0=成本，1=名称
     * @param desc：true=降序，false=升序
     * @return
     */
    public List<StoreProduct> getStoreProductsCost(int merchantId, long storeId, int sortType, boolean desc) {

        return storeProductDAO.getStoreProductsCost(merchantId, storeId, sortType, desc, true, true);
    }

    /**
     * 查询产品列表 包括已经删除的产品
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreProduct> getAllStoreProducts(int merchantId, long storeId) {

        return storeProductDAO.getAllStoreProduct(merchantId, storeId);
    }

    /**
     * 根据customerTrafficModel 查询产品
     *
     * @param merchantId
     * @param storeId
     * @param customerTrafficSelectMode
     * @return
     */
    public List<StoreProduct> getStoreProductsByCustomerTraffic(int merchantId, long storeId, int customerTrafficSelectMode) {
        return storeProductDAO.getStoreProductsByCustomerTraffic(merchantId, storeId, customerTrafficSelectMode);
    }

    /**
     * 更新产品 开启入客数, 默认产品的入客数为1
     * 可以传入未更新前的入客数, 简化算收费项目入客数的更新流程
     *
     * @param storeProduct
     * @return
     */
    public void updateStoreProductCustomerTraffic(StoreProduct storeProduct) {
        //获取产品所关联的收费项目
        List<Long> chargeItemds = this.storeChargeSubitemDAO.getChargeItemIdsContainProduct(storeProduct.getMerchantId(), storeProduct.getStoreId(), storeProduct.getProductId());
        if (chargeItemds.isEmpty()) {
            return;
        }
        Map<Long, StoreChargeItem> storeChargeItemMap = this.storeChargeItemDAO.getMapInIds(storeProduct.getMerchantId(), storeProduct.getStoreId(), chargeItemds, false, false);
        List<Long> productIds = storeChargeSubitemDAO.getProductIdsInChargeItemIds(storeProduct.getMerchantId(), storeProduct.getStoreId(), chargeItemds);
        Map<Long, StoreProduct> storeProductMap = this.storeProductDAO.getMapInIds(storeProduct.getMerchantId(), storeProduct.getStoreId(), productIds, false, false);
        Map<Long, List<StoreChargeSubitem>> storeChargeSubitemMap = storeChargeSubitemDAO.getMapForChargeItemIds(storeProduct.getMerchantId(), storeProduct.getStoreId(), chargeItemds, false, false);
        //得到收费项目下 收费子项的数量, 得到要更新最终数量
        for (Long chargeItemd : chargeItemds) {
            StoreChargeItem storeChargeItem = storeChargeItemMap.get(chargeItemd);
            if (storeChargeItem == null) {
                continue;
            }
            //判断收费项目上是否手动设置了入客数, 手动设置了 就不允许自动更新收费项目上的入客数
            if (storeChargeItem.isEnableManualCustomerTraffic()) {
                continue;
            }
            List<StoreChargeSubitem> chargeSubitems = storeChargeSubitemMap.get(chargeItemd);
            if (chargeSubitems.isEmpty()) {
                continue;
            }
            double sumChargeItemCustomerTraffic = 0.0;
            for (StoreChargeSubitem chargeSubitem : chargeSubitems) {
                if (chargeSubitem.getProductId() == storeProduct.getProductId()) {
                    if (storeProduct.isEnableCustomerTraffic()) {
                        sumChargeItemCustomerTraffic += NumberUtil.mul(storeProduct.getCustomerTraffic(), chargeSubitem.getAmount());
                    }
                } else {
                    //查出其他的收费子项关联的产品的
                    StoreProduct product = storeProductMap.get(chargeSubitem.getProductId());
                    if (product == null || !product.isEnableCustomerTraffic()) {
                        continue;
                    }
                    sumChargeItemCustomerTraffic += NumberUtil.mul(product.getCustomerTraffic(), chargeSubitem.getAmount());
                }
            }
            storeChargeItem.setCustomerTraffic(BigDecimal.valueOf(sumChargeItemCustomerTraffic).setScale(0, BigDecimal.ROUND_UP).intValue());
        }
        //批量更新收费项目的入客数
        storeChargeItemDAO.batchUpdateCustomerTraffic(new ArrayList<StoreChargeItem>(storeChargeItemMap.values()));
    }

    /**
     * 对产品的 分类进行更新,并联动更新 只包含这个菜品的
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @param productIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreProduct> updateStoreChargeItemsCategory(int merchantId, long storeId, int categoryId, List<Long> productIds) {

        List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds, false, true);
        List<Long> updateProductIds = Lists.newArrayList();
        for (StoreProduct product : products) {
            product.setCategoryId(categoryId);
            updateProductIds.add(product.getProductId());
        }
        //批量更新产品
        batchUpdateProduct(merchantId, storeId, products);

        //通过产品ID 对收费项目进行更新
        if (updateProductIds.size() > 0) {
            storeChargeItemService.updateChargeItemsCategoryByProductIds(merchantId, storeId, categoryId, updateProductIds);
        }

        return products;
    }

    /**
     * 批量更新
     *
     * @param merchantId
     * @param storeId
     * @param products
     */
    public void batchUpdateProduct(int merchantId, long storeId, List<StoreProduct> products) {
        if (products == null || products.size() <= 0) {
            return;
        }
        storeProductDAO.batchUpdateCategory(merchantId, storeId, products);
    }

    public void updateProductCategoryByChargeItem(int merchantId, long storeId, int categoryId, List<Long> productIds) {

        if (productIds == null || productIds.size() <= 0) {
            return;
        }

        List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds, false, true);
        for (StoreProduct product : products) {
            product.setCategoryId(categoryId);
        }
        batchUpdateProduct(merchantId, storeId, products);
    }

    public List<StoreProduct> getStoreProducts(QueryStoreProductParam param) {
        List<StoreProduct> list;
        if (param.getQueryType().equals(QueryStoreProductType.ALL)) {
            list = this.getStoreProducts(param.getMerchantId(), param.getStoreId());
        } else if (param.getQueryType().equals(QueryStoreProductType.AVALIABLE)) {
            list = this.getStoreProductsForAvaliable(param.getMerchantId(), param.getStoreId());
        } else {
            long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
            list = this.getStoreProductsForDateSell(param.getMerchantId(), param.getStoreId(), today);
        }
        if (param.isLoadMealPort()) {
            List<Long> portIds = Lists.newArrayList();
            for (StoreProduct storeProduct : list) {
                if (storeProduct.getPortId() > 0) {
                    portIds.add(storeProduct.getPortId());
                }
            }
            if (!portIds.isEmpty()) {
                Map<Long, StoreMealPort> portMap = this.storeMealPortDAO.getMapInIds(param.getMerchantId(), param.getStoreId(), portIds, true);
                for (StoreProduct storeProduct : list) {
                    storeProduct.setStoreMealPort(portMap.get(storeProduct
                            .getPortId()));
                }
            }
        }
        return list;
    }

    private List<StoreProduct> getStoreProductsForDateSell(int merchantId, long storeId, long date) {
        StoreDateBizSetting dateBizSetting = this.storeDateBizSettingDAO.getById(merchantId, storeId, date, true, true);
        int weekDay = StoreDateBizSetting.getWeekDay(dateBizSetting, date);
        List<Long> itemIds = this.storeChargeItemWeekDAO.getChargeItemIdsForWeekDay(merchantId, storeId, 0, weekDay, date, true, true);
        List<StoreChargeSubitem> subitems = this.storeChargeSubitemDAO.getListByChargeItemIds(merchantId, storeId, itemIds);
        List<Long> productIds = Lists.newArrayList();
        for (StoreChargeSubitem subitem : subitems) {
            productIds.add(subitem.getProductId());
        }
        return this.storeProductDAO.getListInIds(merchantId, storeId, productIds, true, true);
    }

    private List<StoreProduct> getStoreProducts(int merchantId, long storeId) {
        return this.storeProductDAO.getList(merchantId, storeId, true, true);
    }

    private List<StoreProduct> getStoreProductsForAvaliable(int merchantId, long storeId) {
        List<Long> itemIds = this.storeChargeItemWeekDAO.getChargeItemIdsForWeekDay(merchantId, storeId,
                0, 0, System.currentTimeMillis(), true, true);
        List<StoreChargeSubitem> subitems = this.storeChargeSubitemDAO.getListByChargeItemIds(merchantId, storeId, itemIds);
        List<Long> productIds = Lists.newArrayList();
        for (StoreChargeSubitem subitem : subitems) {
            productIds.add(subitem.getProductId());
        }
        return this.storeProductDAO.getListInIds(merchantId, storeId, productIds, true, true);
    }
}
