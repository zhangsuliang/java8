package com.huofu.module.i5wei.menu.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.eventtype.EventType;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.facade.StoreMealPortFacadeUtil;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemCategory;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.StoreChargeItemCategoryService;
import com.huofu.module.i5wei.menu.service.StoreProductService;
import com.huofu.module.i5wei.menu.validator.StoreProductValidator;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.menu.*;
import huofuhelper.module.event.EventData;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.sqs.SNSHelper;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 产品服务
 * Created by akwei on 3/9/15.
 */
@ThriftServlet(name = "storeProductFacadeServlet", serviceClass = StoreProductFacade.class)
@Component
@SuppressWarnings("unchecked")
public class StoreProductFacadeImpl implements StoreProductFacade.Iface {

    private final static Log log = LogFactory.getLog(StoreProductFacadeImpl.class);

    @Resource
    private StoreProductService storeProductService;

    @Resource
    private StoreMealPortFacadeUtil storeMealPortFacadeUtil;

    @Resource
    private StoreMealPortDAO storeMealPortDAO;

    @Resource
    private StoreChargeItemCategoryService categoryService;

    @Resource
    private SNSHelper snsHelper;

    @Resource
    private StoreProductValidator storeProductValidator;

    private StoreProductDTO createStoreProduct(StoreProductParam param) throws TException {
        StoreProduct storeProduct = this.storeProductService.createStoreProduct(param);
        if (storeProduct.getPortId() > 0) {
            storeProduct.setStoreMealPort(storeMealPortDAO.getById(param.getMerchantId(), param.getStoreId()
                    , storeProduct.getPortId()));
        }
        return this._buildStoreProductDTO(storeProduct);
    }

    private StoreProductDTO updateStoreProduct(StoreProductParam param) throws TException {
        StoreProduct storeProduct = this.storeProductService.updateStoreProduct(param);
        if (storeProduct.getPortId() > 0) {
            storeProduct.setStoreMealPort(storeMealPortDAO.getById(param.getMerchantId(), param.getStoreId()
                    , storeProduct.getPortId()));
        }
        // TODO 发送消息 得到订单入客数集合
        return this._buildStoreProductDTO(storeProduct);
    }

    @Override
    public StoreProductDTO saveStoreProduct(StoreProductParam param) throws TException {
        this.storeProductValidator.validateSaveStoreProduct(param);
        if (param.getProductId() > 0) {
            return this.updateStoreProduct(param);
        }
        return this.createStoreProduct(param);
    }

    @Override
    public void deleteStoreProduct(int merchantId, long storeId, long productId) throws TException {
        this.storeProductService.deleteStoreProduct(merchantId, storeId, productId);
    }

    @Override
    public List<StoreProductDTO> getStoreProducts(int merchantId, long storeId) throws TException {
        List<StoreProduct> list = this.storeProductService
                .getStoreProductList(merchantId, storeId, false);
        return this.buildStoreProductDTOs(list);
    }

    @Override
    public List<StoreProductDTO> getStoreProductsV2(int merchantId, long storeId, boolean loadMealPort) throws TException {
        List<StoreProduct> list = this.storeProductService
                .getStoreProductList(merchantId, storeId, loadMealPort);
        return this.buildStoreProductDTOs(list);
    }

    @Override
    public List<StoreProductDTO> getStoreProductsV3(QueryStoreProductParam param) throws TException {
        if (param == null) {
            return Collections.emptyList();
        }
        List<StoreProduct> list = this.storeProductService.getStoreProducts(param);
        return this.buildStoreProductDTOs(list);
    }

    @Override
    public StoreProductDTO getStoreProduct(int merchantId, long storeId, long productId) throws TException {
        StoreProduct storeProduct = this.storeProductService.getStoreProduct(merchantId, storeId, productId, false);
        if (storeProduct == null || storeProduct.isDeleted()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] productId[" + productId + "] was deleted");
        }
        return this._buildStoreProductDTO(storeProduct);
    }

    @Override
    public StoreProductDTO getStoreProductV2(int merchantId, long storeId, long productId, boolean loadMealPort) throws TException {
        StoreProduct storeProduct = this.storeProductService.getStoreProduct(merchantId, storeId, productId, loadMealPort);
        if (storeProduct == null || storeProduct.isDeleted()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] productId[" + productId + "] was deleted");
        }
        return this._buildStoreProductDTO(storeProduct);
    }

    @Override
    public StoreProductDTO saveStoreProductRemarks(int merchantId, long storeId, long productId, List<String> remarks) throws TException {
        StoreProduct storeProduct = this.storeProductService
                .saveStoreProductRemarks(merchantId, storeId, productId, remarks);
        return this._buildStoreProductDTO(storeProduct);
    }

    @Override
    public StoreProductDTO getStoreProductByName(int merchantId, long storeId, String name) throws TException {
        StoreProduct storeProduct = this.storeProductService.getStoreProductByName(merchantId,
                storeId, name);
        return this._buildStoreProductDTO(storeProduct);
    }

    private List<StoreProductDTO> buildStoreProductDTOs(List<StoreProduct> storeProducts) throws TException {
        if (storeProducts.isEmpty()) {
            return Collections.emptyList();
        }
        int merchantId = storeProducts.get(0).getMerchantId();
        List<StoreMealPort> storeMealPorts = Lists.newArrayList();
        for (StoreProduct storeProduct : storeProducts) {
            if (storeProduct.getStoreMealPort() != null) {
                storeMealPorts.add(storeProduct.getStoreMealPort());
            }
        }

        Map<Long, StoreMealPortDTO> portDTOMap = this.storeMealPortFacadeUtil
                .buildStoreMealPortDTOMap(merchantId, storeMealPorts);
        List<StoreProductDTO> storeProductDTOs = Lists.newArrayList();
        for (StoreProduct storeProduct : storeProducts) {
            storeProductDTOs.add(this._buildStoreProductDTO(storeProduct, portDTOMap));
        }
        return storeProductDTOs;
    }

    private StoreProductDTO _buildStoreProductDTO(StoreProduct storeProduct) throws TException {
        List<StoreMealPort> storeMealPorts = Lists.newArrayList();
        if (storeProduct.getStoreMealPort() != null) {
            storeMealPorts.add(storeProduct.getStoreMealPort());
        }
        Map<Long, StoreMealPortDTO> portDTOMap = this.storeMealPortFacadeUtil
                .buildStoreMealPortDTOMap(storeProduct.getMerchantId(),
                        storeMealPorts);
        return this._buildStoreProductDTO(storeProduct, portDTOMap);
    }

    private StoreProductDTO _buildStoreProductDTO(StoreProduct storeProduct, Map<Long, StoreMealPortDTO> portDTOMap) {
        StoreProductDTO storeProductDTO = new StoreProductDTO();
        BeanUtil.copy(storeProduct, storeProductDTO);
        if (storeProduct.getStoreMealPort() != null) {
            if (portDTOMap != null) {
                storeProductDTO.setStoreMealPortDTO(portDTOMap.get
                        (storeProductDTO.getPortId()));
            }
        }
        return storeProductDTO;
    }

    @Override
    public List<StoreProductDTO> updatePrimeCast(int merchantId, long storeId, Map<Long, Integer> primeCastMap) throws TException {
        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }
        Map<String, Object> storeProductsMap = storeProductService.updatePrimeCast(merchantId, storeId, primeCastMap);
        List<StoreProduct> storeProducts = (List<StoreProduct>) storeProductsMap.get("result");
        List<Map<String, Object>> unSetProductList = (List<Map<String, Object>>) storeProductsMap.get("unset");
        List<Map<String, Object>> updateProductList = (List<Map<String, Object>>) storeProductsMap.get("update");

        //SNS发布事件
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put("merchantId", merchantId);
        eventDataMap.put("storeId", storeId);
        eventDataMap.put("unset", unSetProductList);
        eventDataMap.put("update", updateProductList);
        String topic = SysConfig.getProductEventTopicArn();
        publish(EventType.PRODUCT_COST_UPDATE, eventDataMap, topic);
        return buildStoreProductDTOs(storeProducts);
    }

    @Override
    public List<StoreProductDTO> getStoreProductsCost(int merchantId, long storeId, int sortType, boolean desc) throws TException {
        List<StoreProduct> storeProducts = storeProductService.getStoreProductsCost(merchantId, storeId, sortType, desc);
        return buildStoreProductDTOs(storeProducts);
    }

    @Override
    public List<StoreProductDTO> getAllStoreProducts(int merchantId, long storeId) throws TException {
        List<StoreProduct> storeProducts = storeProductService.getAllStoreProducts(merchantId, storeId);
        return buildStoreProductDTOs(storeProducts);
    }

    @Override
    public List<StoreProductDTO> getStoreProductsByCustomerTraffic(int merchantId, long storeId, int customerTrafficSelectMode) throws TException {
        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }
        List<StoreProduct> products = storeProductService.getStoreProductsByCustomerTraffic(merchantId, storeId, customerTrafficSelectMode);
        return buildStoreProductDTOs(products);
    }

    @Override
    public StoreProductDTO getStoreProductById(int merchantId, long storeId, long productId) throws TException {
        StoreProduct storeProduct = this.storeProductService.getStoreProduct(merchantId, storeId, productId, false);
        if (storeProduct == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] productId[" + productId + "] not found");
        }
        return this._buildStoreProductDTO(storeProduct);
    }

    @Override
    public List<StoreProductDTO> updateStoreProductCategory(int merchantId, long storeId, int categoryId, List<Long> productIds) throws TException {

        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }
        if (categoryId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "categoryId[" + categoryId + "] must > 0");
        }
        if (productIds == null || productIds.size() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "productIds size must > 0");
        }

        List<StoreProduct> products = storeProductService.updateStoreChargeItemsCategory(merchantId, storeId, categoryId, productIds);
        return buildStoreProductDTOs(products);
    }

    @Override
    public StoreProductAndCategory getStoreProductAndCategory(int merchantId, long storeId, List<Long> productIds, boolean loadCategory) throws TException {

        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }

        if (productIds == null || productIds.size() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "productIds size must > 0");
        }

        Map<Long, StoreProduct> storeProductMapInIds = storeProductService.getStoreProductMapInIds(merchantId, storeId, productIds);
        Collection<StoreProduct> storeProducts = storeProductMapInIds.values();
        Set<Integer> categoryIds = Sets.newHashSet();

        List<StoreProductDTO> productDTOs = Lists.newArrayList();
        for (StoreProduct storeProduct : storeProducts) {
            categoryIds.add(storeProduct.getCategoryId());
            StoreProductDTO productDTO = BeanUtil.copy(storeProduct, StoreProductDTO.class);
            productDTOs.add(productDTO);
        }

        List<StoreChargeItemCategoryDTO> categoryDTOs = Lists.newArrayList();
        if (loadCategory) {
            List<StoreChargeItemCategory> categorys = categoryService.getCategoryByIds(merchantId, storeId, categoryIds);
            for (StoreChargeItemCategory category : categorys) {
                StoreChargeItemCategoryDTO categoryDTO = new StoreChargeItemCategoryDTO();
                BeanUtil.copy(category, categoryDTO);
                categoryDTOs.add(categoryDTO);
            }
        }

        StoreProductAndCategory storeProductAndCategory = new StoreProductAndCategory();
        storeProductAndCategory.setMerchantId(merchantId);
        storeProductAndCategory.setStoreId(storeId);
        storeProductAndCategory.setProducts(productDTOs);
        storeProductAndCategory.setCategorys(categoryDTOs);

        return storeProductAndCategory;
    }

    private void publish(int eventType, Map<String, Object> dataMap, String topic) {
        int moduleId = 3; //详见 http://doc.5wei.com/pages/viewpage.action?pageId=25660227
        EventData eventData = new EventData();
        eventData.setModuleId(moduleId);
        eventData.setEventType(eventType);
        eventData.setDataMap(dataMap);
        try {
            snsHelper.publishObj(topic, eventData);
        } catch (Exception e) {
            log.error(e.getMessage() + "eventData : " + JsonUtil.build(eventData));
        }
    }
}
