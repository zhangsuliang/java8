package com.huofu.module.i5wei.inventory.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.inventory.ProductInventoryPrecisionDTO;
import huofucore.facade.i5wei.inventory.ProductInventoryPrecisionRowDTO;
import huofucore.facade.i5wei.inventory.ProductInventoryPrecisionTableDTO;
import huofucore.facade.i5wei.inventory.StoreInventoryDateAmountUpdateParam;
import huofucore.facade.i5wei.inventory.StoreInventoryDateNothingnessUpdateParam;
import huofucore.facade.i5wei.inventory.StoreInventoryModifiedEnum;
import huofucore.facade.i5wei.inventory.StoreInventoryUpdateParam;
import huofucore.facade.i5wei.inventory.StoreInventoryWeekItemParam;
import huofucore.facade.i5wei.inventory.StoreInventoryWeekQueryParam;
import huofucore.facade.i5wei.inventory.StoreInventoryWeekUpdateParam;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofucore.facade.merchant.exception.TMerchantException;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.query.StoreQueryFacade;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BatchResult;
import com.huofu.module.i5wei.inventory.dao.StoreInventoryDAO;
import com.huofu.module.i5wei.inventory.dao.StoreInventoryDateDAO;
import com.huofu.module.i5wei.inventory.dao.StoreInventoryInvsetDAO;
import com.huofu.module.i5wei.inventory.dao.StoreInventoryWeekDAO;
import com.huofu.module.i5wei.inventory.entity.StoreInventory;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryInvset;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryWeek;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreMenuService;
import com.huofu.module.i5wei.menu.service.StoreProductWeek;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderSubitemDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderInvTypeEnum;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.wechat.WechatTempNotifyService;
import huofucore.facade.config.client.ClientTypeEnum;

@Service
public class StoreInventoryService {
	
	private static final Log log = LogFactory.getLog(StoreInventoryService.class);
	
	private static final int mills_in_day = 24 * 60 * 60 * 1000;
	
	@ThriftClient
	private StoreQueryFacade.Iface storeQueryFacade;
	
    @Autowired
    private StoreOrderDAO storeOrderDao;

    @Autowired
    private StoreOrderItemDAO storeOrderItemDao;

    @Autowired
    private StoreOrderSubitemDAO storeOrderSubitemDao;

    @Autowired
    private StoreProductDAO storeProductDao;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    @Autowired
    private StoreInventoryDAO storeInventoryDao;

    @Autowired
    private StoreInventoryInvsetDAO storeInventoryInvsetDao;

    @Autowired
    private StoreInventoryDateDAO storeInventoryDateDao;

    @Autowired
    private StoreInventoryWeekDAO storeInventoryWeekDao;
    
    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;
    
    @Autowired
    private StoreMealCheckoutDAO storeMealCheckoutDAO;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private StoreInventoryHelper storeInventoryHelper;

    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Autowired
    private StoreOrderService storeOrderService;
    
    @Autowired
    private StoreMenuService storeMenuService;
    
    @Autowired
    private WechatTempNotifyService wechatTempNotifyService;

    /**
     * 查询产品库存设置
     *
     * @param merchantId
     * @param storeId
     * @param productId
     * @return
     * @throws T5weiException
     */
    public StoreInventory getStoreInventory(int merchantId, long storeId, long productId) throws T5weiException {
        StoreProduct storeProduct = storeProductDao.getById(merchantId, storeId, productId, false, false);
        if (storeProduct == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_NOT_EXIST.getValue(),
                    DataUtil.infoWithParams("product not exist, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
        }
        StoreInventory storeInventory = storeInventoryDao.getById(merchantId, storeId, productId, false, false);
		if (storeInventory == null) {
			storeInventory = new StoreInventory();
			storeInventory.construct(storeProduct);
		}
        storeInventory.setStoreProduct(storeProduct);
        return storeInventory;
    }

    /**
     * 修改产品库存模式和默认周期库存
     *
     * @param merchantId
     * @param storeId
     * @param productId
     * @param invType
     * @param amount
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreInventory updateStoreInventory(StoreInventoryUpdateParam storeInventoryUpdateParam) throws T5weiException {
        int merchantId = storeInventoryUpdateParam.getMerchantId();
        long storeId = storeInventoryUpdateParam.getStoreId();
        long productId = storeInventoryUpdateParam.getProductId();
        if (merchantId == 0 || storeId == 0) {
        	throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), " store_id, merchant_id can not be null ");
        }
        if (productId == 0) {
        	throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_ID_CAN_NOT_NULL.getValue(), " product_id can not be null ");
        }
        StoreProduct storeProduct = storeProductDao.getById(merchantId, storeId, productId, false, true);
        if (storeProduct == null) {
        	throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_NOT_EXIST.getValue(),
        			DataUtil.infoWithParams("product not exist, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
        }
        storeProduct.snapshot();
		// 库存开关
		if (storeInventoryUpdateParam.isSetInvEnabled()) {
			storeProduct.setInvEnabled(storeInventoryUpdateParam.isInvEnabled());
		}
		// 库存类型
		if (storeInventoryUpdateParam.isSetInvType()) {
			if (storeInventoryUpdateParam.getInvType() == ProductInvTypeEnum.NONE.getValue()) {
				storeInventoryUpdateParam.setInvType(ProductInvTypeEnum.WEEK_DAY.getValue());
			}
			storeProduct.setInvType(storeInventoryUpdateParam.getInvType());
		}
		// 更新产品库存类型设置
		storeProduct.setWeekAmount(StoreInventoryWeek.defaultAmount);
		storeProduct.setUpdateTime(System.currentTimeMillis());
		storeProduct.update();
		// 不是固定库存模式，则到此处终止
		if (storeProduct.getInvType() != ProductInvTypeEnum.FIXED.getValue()) {
			return this.getStoreInventory(merchantId, storeId, productId);
		}
		// 更新固定库存设置
		StoreInventory storeInventory = null;
		if (storeInventoryUpdateParam.isSetAmount()) {
			storeInventory = this.updateInventory(storeProduct, storeInventoryUpdateParam.getAmount());
		}
		// 确保产品库存设置已初始化
		if (storeInventory == null) {
			storeInventory = storeInventoryDao.getById(merchantId, storeId, productId, true, true);
			if (storeInventory == null) {
				storeInventory = new StoreInventory();
				storeInventory.construct(storeProduct);
				storeInventory.create();
			}
		}
		// 更新固定库存报警设置
		if (storeInventoryUpdateParam.isSetAlarmAmount()) {
			boolean enableInvAlarm = false;
			if (storeInventoryUpdateParam.getAlarmAmount() > 0) {
				enableInvAlarm = true;
			}
			storeInventory.snapshot();
			storeInventory.setAlarmAmount(storeInventoryUpdateParam.getAlarmAmount());
			storeInventory.setEnableInvAlarm(enableInvAlarm);
			storeInventory.setUpdateTime(System.currentTimeMillis());
			storeInventory.update();
		}
		// 返回参数
		storeInventory.setStoreProduct(storeProduct);
        return storeInventory;
    }

    /**
     * 更新固定库存剩余量
     *
     * @param merchantId
     * @param storeId
     * @param productId
     * @param remain
     * @return
     * @throws T5weiException
     */
    private StoreInventory updateInventory(StoreProduct storeProduct, double remain) throws T5weiException {
        if (storeProduct == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_NOT_EXIST.getValue(), " product can not be null ");
        }
        int merchantId = storeProduct.getMerchantId();
        long storeId = storeProduct.getStoreId();
        long productId = storeProduct.getProductId();
        if (merchantId == 0 || storeId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), " store_id, merchant_id can not be null ");
        }
        if (remain < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_AMOUNT_CAN_NOT_LESS_ZERO.getValue(),
                    DataUtil.infoWithParams(" product remain amount can not less 0 , product_id=#1, store_id=#2 ", new Object[]{productId, storeId}));
        }
        //判断库存模式
        int invType = storeProduct.getInvType();
        if (invType != ProductInvTypeEnum.FIXED.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_TYPE_NOT_FIXED.getValue(),
                    DataUtil.infoWithParams("this product not fix invType, product_id=#1, storeId=#2 ", new Object[]{productId, storeId}));
        }
        StoreInventory storeInventory = storeInventoryDao.getById(merchantId, storeId, productId, true, true);
        double amountChange = 0.00;
        double inventory = 0.00;
        String remark = "";
        if (storeInventory == null) {
            storeInventory = new StoreInventory();
            storeInventory.setMerchantId(merchantId);
            storeInventory.setStoreId(storeId);
            storeInventory.setProductId(productId);
            storeInventory.setAmount(remain);
            storeInventory.setCreateTime(System.currentTimeMillis());
            storeInventory.setUpdateTime(System.currentTimeMillis());
            storeInventoryDao.create(storeInventory);
            amountChange = remain;
            remark = "create store inventory ";
        } else {
            inventory = storeInventory.getAmount();
            amountChange = NumberUtil.sub(remain, storeInventory.getAmount());//库存变更
            if (amountChange == 0) {
                return storeInventory;//库存无变化，直接返回
            }
            storeInventory.setAmount(remain);//用输入remain覆盖
            storeInventory.setUpdateTime(System.currentTimeMillis());
            storeInventoryDao.update(storeInventory);
            remark = "update store inventory ";
        }
        //创建库存变更记录
        StoreInventoryInvset storeInventoryInvset = new StoreInventoryInvset();
        storeInventoryInvset.setMerchantId(merchantId);
        storeInventoryInvset.setStoreId(storeId);
        storeInventoryInvset.setProductId(productId);
        storeInventoryInvset.setOrderId("");
        storeInventoryInvset.setInventory(inventory);
        storeInventoryInvset.setAmount(amountChange);
        storeInventoryInvset.setRemain(remain);
        storeInventoryInvset.setRemark(remark);
        storeInventoryInvset.setCreateTime(System.currentTimeMillis());
        storeInventoryInvsetDao.create(storeInventoryInvset);
        storeInventory.setStoreProduct(storeProduct);
        return storeInventory;
    }

    /**
     * 订单取餐固定库存扣减
     *
     * @param merchantId
     * @param storeId
     * @param productId
     * @param orderAmount(正数为扣减库存，负数为恢复库存)
     * @return
     * @throws T5weiException
     */
    private StoreInventoryInvset _deductFixInventoryForOrder(int merchantId, long storeId, long productId, String orderId, double orderAmount, String remark) throws T5weiException {
        if (merchantId == 0 || storeId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), " store_id, merchant_id can not be null ");
        }
        if (productId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_ID_CAN_NOT_NULL.getValue(), " product_id can not be null ");
        }
        if (orderAmount == 0) {
            return null;
        }
        //判断库存模式
        StoreProduct storeProduct = storeProductDao.getById(merchantId, storeId, productId, false, false);
        if (storeProduct == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_NOT_EXIST.getValue(),
                    DataUtil.infoWithParams("product can not be null, product_id=#1, storeId=#2 ", new Object[]{productId, storeId}));
        }
        int invType = storeProduct.getInvType();
		if (invType != ProductInvTypeEnum.FIXED.getValue()) {
			return null;
		}
        StoreInventory storeInventory = storeInventoryDao.getById(merchantId, storeId, productId, true, true);
        double remain = 0;
        double inventory = 0;
        if (storeInventory == null) {
        	log.warn(DataUtil.infoWithParams("this product's storeInventory not exist , product_id=#1, storeId=#2 ", new Object[]{productId, storeId}));
        	return null;
        } else {
            inventory = storeInventory.getAmount();
            remain = NumberUtil.sub(inventory, orderAmount);
            if (remain < 0) {
                remain = 0;
            }
            storeInventory.setAmount(remain);//覆盖剩余量
            storeInventoryDao.update(storeInventory);
        }
        //创建库存变更记录
        StoreInventoryInvset storeInventoryInvset = new StoreInventoryInvset();
        storeInventoryInvset.setMerchantId(merchantId);
        storeInventoryInvset.setStoreId(storeId);
        storeInventoryInvset.setProductId(productId);
        storeInventoryInvset.setOrderId(orderId);
        storeInventoryInvset.setInventory(inventory);
        storeInventoryInvset.setAmount(NumberUtil.sub(0, orderAmount));
        storeInventoryInvset.setRemain(remain);
        storeInventoryInvset.setRemark(remark);
        storeInventoryInvset.setCreateTime(System.currentTimeMillis());
        storeInventoryInvsetDao.create(storeInventoryInvset);
        storeInventoryInvset.setName(storeProduct.getName());
        storeInventoryInvset.setUnit(storeProduct.getUnit());
        storeInventoryInvset.setEnableInvAlarm(storeInventory.isEnableInvAlarm());
        storeInventoryInvset.setAlarmAmount(storeInventory.getAlarmAmount());
        return storeInventoryInvset;
    }

    /**
     * 根据产品查询周期库存信息
     *
     * @param storeInventoryWeekQueryParam
     * @return
     */
    public Map<String, List<StoreInventoryWeek>> getInventoryWeekByProduct(StoreInventoryWeekQueryParam storeInventoryWeekQueryParam) {
        int merchantId = storeInventoryWeekQueryParam.getMerchantId();
        long storeId = storeInventoryWeekQueryParam.getStoreId();
        long productId = storeInventoryWeekQueryParam.getProductId();
        if (merchantId == 0 || storeId == 0) {
            return new HashMap<String, List<StoreInventoryWeek>>(0);
        }
        if (productId == 0) {
            return new HashMap<String, List<StoreInventoryWeek>>(0);
        }
        StoreProduct product = storeProductDao.getById(merchantId, storeId, productId, false, false);
        if (product == null) {
            return new HashMap<String, List<StoreInventoryWeek>>(0);
        }
        long currentTime = System.currentTimeMillis();
        MutableDateTime mdt = new MutableDateTime(currentTime);
        mdt.setDayOfWeek(1);
        mdt.addWeeks(1);
        long nextWeektime = mdt.getMillis();
        List<StoreProductWeek> allChargeItemWeekList = storeChargeItemService.getStoreProductWeeksByProductIdForBiz(merchantId, storeId, currentTime, productId);
        Map<String, List<StoreProductWeek>> allChargeItemWeekMap = this.getStoreProductInvWeekList(allChargeItemWeekList);
        
        List<StoreProductWeek> currentChargeItemWeekList = allChargeItemWeekMap.get("currentTimeList");
        List<StoreInventoryWeek> currentInvWeekList = this.getStoreProductInvWeekList(merchantId, storeId, currentTime, currentChargeItemWeekList, product);
        
        List<StoreProductWeek> nextWeekChargeItemWeekList = allChargeItemWeekMap.get("nextWeekList");
        List<StoreInventoryWeek> nextWeekInvWeekList = this.getStoreProductInvWeekList(merchantId, storeId, nextWeektime, nextWeekChargeItemWeekList, product);
        
        Map<String, List<StoreInventoryWeek>> resultMap = new HashMap<String, List<StoreInventoryWeek>>();
        storeInventoryHelper.sortInvWeekList(currentInvWeekList);
        storeInventoryHelper.sortInvWeekList(nextWeekInvWeekList);
        resultMap.put("currentTime", currentInvWeekList);
        resultMap.put("nextWeek", nextWeekInvWeekList);
        return resultMap;
    }

    private Map<String, List<StoreProductWeek>> getStoreProductInvWeekList(List<StoreProductWeek> allChargeItemWeekList) {
        if (allChargeItemWeekList == null || allChargeItemWeekList.isEmpty()) {
            return new HashMap<String, List<StoreProductWeek>>(0);
        }
        List<StoreProductWeek> currentTimeList = new ArrayList<StoreProductWeek>();
        List<StoreProductWeek> nextWeekList = new ArrayList<StoreProductWeek>();
        for (StoreProductWeek storeProductWeek : allChargeItemWeekList) {
            if (storeProductWeek.isNextWeek()) {
                nextWeekList.add(storeProductWeek);
            } else {
                currentTimeList.add(storeProductWeek);
            }
        }
        Map<String, List<StoreProductWeek>> resultMap = new HashMap<String, List<StoreProductWeek>>();
        resultMap.put("currentTimeList", currentTimeList);
        resultMap.put("nextWeekList", nextWeekList);
        return resultMap;
    }

    private List<StoreInventoryWeek> getStoreProductInvWeekList(int merchantId, long storeId, long datetime, List<StoreProductWeek> storeChargeItemWeekList, StoreProduct product) {
    	boolean enableSlave = true;
        if (storeChargeItemWeekList == null || storeChargeItemWeekList.isEmpty()) {
            return new ArrayList<StoreInventoryWeek>();
        }
        List<StoreInventoryWeek> invWeekList = new ArrayList<StoreInventoryWeek>();
        Map<String, StoreInventoryWeek> invWeekMap = storeInventoryWeekDao.getStoreInventoryWeekMapByTime(merchantId, storeId, product, datetime, enableSlave);
        if (product.getInvType() == ProductInvTypeEnum.WEEK.getValue()) {
        	List<Long> timeBucketIds = this.getTimeBucketIdsOfStoreChargeItemWeeks(storeChargeItemWeekList);
            Map<Long, StoreTimeBucket> storeTimeBucketMap = storeTimeBucketService.getStoreTimeBucketMapInIds(merchantId, storeId, timeBucketIds);
            for (StoreProductWeek productWeek : storeChargeItemWeekList) {
    			long timeBucketId = productWeek.getTimeBucketId();
                int weekDay = productWeek.getWeekDay();
                StoreInventoryWeek invWeek = invWeekMap.get(timeBucketId + "_" + weekDay);
                if (invWeek == null) {
                    invWeek = this.newStoreInventoryWeek(weekDay, timeBucketId, product);
                }
                StoreTimeBucket storeTimeBucket = storeTimeBucketMap.get(timeBucketId);
	            if (storeTimeBucket != null) {
	                invWeek.setStoreTimeBucket(storeTimeBucket);
	                invWeekList.add(invWeek);
	            } else {
	                invWeekList.remove(invWeek);
	            }
            }
        }else if(product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
			long timeBucketId = 0;
			Set<Integer> weekDays = new HashSet<Integer>();
			for (StoreProductWeek productWeek : storeChargeItemWeekList) {
				weekDays.add(productWeek.getWeekDay());
			}
			for (int weekDay : weekDays) {
				StoreInventoryWeek invWeek = invWeekMap.get(timeBucketId + "_" + weekDay);
				if (invWeek == null) {
                    invWeek = this.newStoreInventoryWeek(weekDay, timeBucketId, product);
                }
				invWeekList.add(invWeek);
			}
        }
        return invWeekList;
    }
    
    private StoreInventoryWeek newStoreInventoryWeek(int weekDay, long timeBucketId, StoreProduct product){
    	StoreInventoryWeek invWeek = new StoreInventoryWeek();
        invWeek.setMerchantId(product.getMerchantId());
        invWeek.setStoreId(product.getStoreId());
        invWeek.setTimeBucketId(timeBucketId);
        invWeek.setWeekDay(weekDay);
        invWeek.setProductId(product.getProductId());
        invWeek.setAmount(product.getWeekAmount());
    	return invWeek;
    }

    private List<Long> getTimeBucketIdsOfStoreChargeItemWeeks(List<StoreProductWeek> storeChargeItemWeekList) {
        if (storeChargeItemWeekList == null) {
            return null;
        }
        List<Long> timeBucketIds = new ArrayList<Long>();
        for (StoreProductWeek productWeek : storeChargeItemWeekList) {
            timeBucketIds.add(productWeek.getTimeBucketId());
        }
        return timeBucketIds;
    }

    /**
     * 修改周期库存
     *
     * @param storeInventoryWeekUpdateParam
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResult<StoreInventoryWeekItemParam>> updateInventoryWeek(StoreInventoryWeekUpdateParam storeInventoryWeekUpdateParam) throws T5weiException {
        int merchantId = storeInventoryWeekUpdateParam.getMerchantId();
        long storeId = storeInventoryWeekUpdateParam.getStoreId();
        long productId = storeInventoryWeekUpdateParam.getProductId();
        List<StoreInventoryWeekItemParam> storeInventoryWeekItemParams = storeInventoryWeekUpdateParam.getStoreInventoryWeekItemParams();
        if (merchantId == 0 || storeId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), " store_id, merchant_id can not be null ");
        }
        if (productId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_ID_CAN_NOT_NULL.getValue(), " product_id can not be null ");
        }
        if (storeInventoryWeekItemParams == null || storeInventoryWeekItemParams.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_WEEK_CAN_NOT_NULL.getValue(), "storeInventoryWeekItemParams is null");
        }
        List<BatchResult<StoreInventoryWeekItemParam>> resultList = new ArrayList<BatchResult<StoreInventoryWeekItemParam>>();
        for (StoreInventoryWeekItemParam storeInventoryWeekItemParam : storeInventoryWeekItemParams) {
            BatchResult<StoreInventoryWeekItemParam> item = new BatchResult<StoreInventoryWeekItemParam>();
            int weekDay = storeInventoryWeekItemParam.getWeekDay();
            long timeBucketId = storeInventoryWeekItemParam.getTimeBucketId();
            double amount = storeInventoryWeekItemParam.getAmount();
            boolean nextWeek = storeInventoryWeekItemParam.isNextWeek();
            int resultCode = 0;
            try {
                resultCode = this.updateInventoryWeekItem(merchantId, storeId, productId, weekDay, timeBucketId, amount, nextWeek);
            } catch (T5weiException e) {
                resultCode = e.getErrorCode();
            } catch (Exception e) {
                resultCode = T5weiErrorCodeType.STORE_PRODUCT_INV_WEEK_UPDATE_FAILURE.getValue();
            }
            item.setErrorCode(resultCode);
            item.setObj(storeInventoryWeekItemParam);
            resultList.add(item);
        }
        return resultList;
    }

    /**
     * 修改周期库存
     *
     * @param storeInventoryWeekUpdateParam
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateInventoryWeekItem(int merchantId, long storeId, long productId, int weekDay, long timeBucketId, double amount, boolean nextWeek) throws T5weiException {
        if (amount < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_AMOUNT_CAN_NOT_LESS_ZERO.getValue(),
                    DataUtil.infoWithParams("create store product inventory amount can not less 0, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
        }
        if (weekDay == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_WEEK_NEED_WEEK_DAY.getValue(),
                    DataUtil.infoWithParams("create store product inventory of week need week_day, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
        }
        StoreProduct storeProduct = storeProductDao.getById(merchantId, storeId, productId, false, false);
        if (storeProduct == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_NOT_EXIST.getValue(),
                    DataUtil.infoWithParams("product not exist, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
        } else {
            if (storeProduct.getInvType() == ProductInvTypeEnum.NONE.getValue()||storeProduct.getInvType() == ProductInvTypeEnum.FIXED.getValue()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_TYPE_NOT_WEEK.getValue(),
                        DataUtil.infoWithParams("this product's invType not week type, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
            }
        }
        if(storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
			timeBucketId = 0;
		}
		if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK.getValue() && timeBucketId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_WEEK_NEED_TIME_BUCKET_ID.getValue(),
                    DataUtil.infoWithParams("create store product inventory of week need time_bucket_id, product_id=#1, store_id=#2", new Object[]{productId, storeId}));
        }
        //修改库存&生效时间
        long effectTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);//生效时间默认为立即生效
        if (nextWeek) {
            MutableDateTime mdt = new MutableDateTime(effectTime);
            mdt.setDayOfWeek(1);
            mdt.addWeeks(1);
            mdt.setHourOfDay(0);
            mdt.setMinuteOfHour(0);
            mdt.setSecondOfMinute(0);
            mdt.setMillisOfSecond(0);
            effectTime = mdt.getMillis();
        }
        StoreInventoryWeek invWeek = storeInventoryWeekDao.getStoreInventoryWeekByProductTime(merchantId, storeId, productId, weekDay, timeBucketId, effectTime);
        if (invWeek == null) {
            //根据条件（商户ID，店铺ID，周，营业时段，产品ID，库存）创建周期库存
            invWeek = new StoreInventoryWeek();
            invWeek.setMerchantId(merchantId);
            invWeek.setStoreId(storeId);
            invWeek.setProductId(productId);
            invWeek.setWeekDay(weekDay);
            invWeek.setTimeBucketId(timeBucketId);
            invWeek.setAmount(amount);
            invWeek.setBeginTime(effectTime);
            invWeek.setEndTime(Long.MAX_VALUE);
            invWeek.setUpdateTime(System.currentTimeMillis());
            invWeek.setCreateTime(System.currentTimeMillis());
            storeInventoryWeekDao.replace(invWeek);
        } else {
            if (amount == invWeek.getAmount()) {
                return BatchResult.success;//没有变化直接返回
            }
            long parentWeekId = invWeek.getInvWeekId();
            if (invWeek.getParentWeekId() > 0) {
                parentWeekId = invWeek.getParentWeekId();
            }
            effectTime = DateUtil.getBeginTime(effectTime, null);
            if (invWeek.getBeginTime() == effectTime) {
                invWeek.setAmount(amount);
                invWeek.setUpdateTime(System.currentTimeMillis());
                storeInventoryWeekDao.update(invWeek);
            } else {
                //旧库存设置到此时间(startTime)失效
                invWeek.setEndTime(effectTime - 1);
                invWeek.setUpdateTime(System.currentTimeMillis());
                storeInventoryWeekDao.update(invWeek);
                //新库存设置到此时间(startTime)生效
                StoreInventoryWeek newInvWeek = new StoreInventoryWeek();
                newInvWeek.setParentWeekId(parentWeekId);
                newInvWeek.setMerchantId(merchantId);
                newInvWeek.setStoreId(storeId);
                newInvWeek.setProductId(productId);
                newInvWeek.setWeekDay(weekDay);
                newInvWeek.setTimeBucketId(timeBucketId);
                newInvWeek.setAmount(amount);
                newInvWeek.setBeginTime(effectTime);
                newInvWeek.setEndTime(Long.MAX_VALUE);
                newInvWeek.setUpdateTime(System.currentTimeMillis());
                newInvWeek.setCreateTime(System.currentTimeMillis());
                storeInventoryWeekDao.replace(newInvWeek);
            }
        }
        //根据条件tb_store_inventory_date库存联动，生效时间之后的联动
        storeInventoryDateDao.updateStoreInventoryDateByWeek(merchantId, storeId, timeBucketId, productId, amount, effectTime);
        return BatchResult.success;
    }

    /**
     * 添加在inventoryDateList但不在products中的产品
     *
     * @param merchantId
     * @param storeId
     * @param products
     * @param inventoryDateList
     */
    private Map<Long, StoreInventoryDate> addUnsellProductsAndBuildInventoryDate(int merchantId, long storeId, List<StoreProduct> products, List<StoreInventoryDate> inventoryDateList, boolean addUnsell, boolean enableSlave) {
        if (addUnsell) {
        	//添加未在销售中的产品
            Set<Long> productIdSet = new HashSet<Long>();
            if (products != null && !products.isEmpty()) {
                for (StoreProduct product : products) {
                    productIdSet.add(product.getProductId());
                }
            }
            Set<Long> unSellProductIdSet = new HashSet<Long>();
            if (inventoryDateList != null && !inventoryDateList.isEmpty()) {
                for (StoreInventoryDate inventoryDate : inventoryDateList) {
                    if (!productIdSet.contains(inventoryDate.getProductId())) {
                        unSellProductIdSet.add(inventoryDate.getProductId());
                    }
                }
            }
            if (unSellProductIdSet != null && !unSellProductIdSet.isEmpty()) {
                List<StoreProduct> unSellProducts = storeProductDao.getListInIds(merchantId, storeId, new ArrayList<>(unSellProductIdSet), enableSlave, true);
                products.addAll(unSellProducts);//添加未在销售中的产品
            }
        }
        Map<Long, StoreProduct> productsMap = new HashMap<Long, StoreProduct>();
        if (products != null) {
            for (StoreProduct storeProduct : products) {
				productsMap.put(storeProduct.getProductId(), storeProduct);
            }
        }
        //构造inventoryDateMap
        Map<Long, StoreInventoryDate> inventoryDateMap = new HashMap<Long, StoreInventoryDate>();
        if (inventoryDateList != null && !inventoryDateList.isEmpty()) {
            for (StoreInventoryDate inventoryDate : inventoryDateList) {
            	long productId = inventoryDate.getProductId();
            	StoreProduct product = productsMap.get(productId);
				if (product == null) {
					continue;
				}
            	long invTimeBucketId = inventoryDate.getTimeBucketId();
				if (product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue() && invTimeBucketId > 0){
					continue;// 周天库存invTimeBucketId不能大于0
            	}
				if (product.getInvType() == ProductInvTypeEnum.WEEK.getValue() && invTimeBucketId == 0){
					continue;// 周营业时段库存invTimeBucketId不能等于0
            	}
            	inventoryDateMap.put(productId, inventoryDate); 
            }
        }
        return inventoryDateMap;
    }

    /**
     * 产品库存盘点（一次性返回所有产品库存，为菜单库存提供）
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param products 	已在销售中的产品列表
     * @param addUnsell 是否添加已下架但已预定产品   
     * @return
     * @author kaichen
     */
    public List<StoreInventoryDate> getInventoryDate(int merchantId, long storeId, long repastDate, long timeBucketId, List<StoreProduct> products, boolean addUnsell) {
    	boolean enableSlave = true;
        //定义返回结果，需要考虑周期库存、固定库存和产品销售情况
        List<StoreInventoryDate> resultInventoryDateList = new ArrayList<StoreInventoryDate>();
        //销售中的产品ID
        Set<Long> productIdsInSell = new HashSet<Long>();
        if (products != null) {
            for (StoreProduct storeProduct : products) {
                productIdsInSell.add(storeProduct.getProductId());
            }
        }
        //指定日期产品库存
        List<StoreInventoryDate> inventoryDateList = storeInventoryDateDao.getStoreInventoryDateBySelectDate(merchantId, storeId, timeBucketId, repastDate, enableSlave);
        Map<Long, StoreInventoryDate> inventoryDateMap = this.addUnsellProductsAndBuildInventoryDate(merchantId, storeId, products, inventoryDateList, addUnsell,enableSlave);
        //为空则直接返回
        if (products == null || products.isEmpty()) {
            return resultInventoryDateList;
        }
        //固定库存
        Map<Long, StoreInventory> inventoryMap = storeInventoryDao.getStoreInventoryMapByProductIds(merchantId, storeId, new ArrayList<Long>(productIdsInSell), enableSlave);
        Map<Long,Double> proudctOrderAmount = storeInventoryDateDao.getStoreInventoryProductOrderAmount(merchantId, storeId, products, enableSlave);
        //周期库存
        int weekDay = storeMenuService.getWeekDayOfMenuDate(merchantId, storeId, repastDate);
        Map<Long, StoreInventoryWeek> inventoryWeekMap = storeInventoryWeekDao.getStoreInventoryWeekMapByProductTime(merchantId, storeId, products, weekDay, timeBucketId, repastDate, enableSlave);
        //构造产品库存列表
        for (StoreProduct product : products) {
            if (product.isDeleted()) {
                continue;//过滤已删除产品
            }
            StoreInventoryDate storeInventoryDate = inventoryDateMap.get(product.getProductId());
            if (!product.isInvEnabled()) {
				if (storeInventoryDate != null) {
		            storeInventoryDate.setStoreProduct(product);
					resultInventoryDateList.add(storeInventoryDate);
				}
                continue;//过滤库存未开启产品
            }
            int invType = product.getInvType();
            if (invType == ProductInvTypeEnum.FIXED.getValue()) {
                //固定库存产品
                StoreInventory storeInventory = inventoryMap.get(product.getProductId());
                if (inventoryDateMap.containsKey(product.getProductId())) {
                    storeInventoryDate.setAmount(storeInventory.getAmount());
                } else {
					if (storeInventory != null) {
						storeInventoryDate = this.newStoreInventoryDate(storeInventory);
					}else{
						storeInventoryDate = this.newStoreInventoryDate(product);
					}
                }
                if (proudctOrderAmount.containsKey(product.getProductId())) {
					double amountOrderTotal = proudctOrderAmount.getOrDefault(product.getProductId(), 0D);
					storeInventoryDate.setAmountOrderTotal(amountOrderTotal);
				}
            } else {
                //周期库存产品
                if (inventoryDateMap.containsKey(product.getProductId())) {
                    //在tb_store_inventory_date表中
                    StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(product.getProductId());
                    if (storeInventoryDate.getAmountPlan() == 0) {
                        if (storeInventoryWeek != null) {
                            storeInventoryDate.setAmountPlan(storeInventoryWeek.getAmount());
                        } else {
                            storeInventoryDate.setAmountPlan(product.getWeekAmount());
                        }
                    }
                } else {
                    //不在tb_store_inventory_date表中
                    StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(product.getProductId());
                    if (storeInventoryWeek != null) {
                        //周期库存中已设置
                        storeInventoryDate = this.newStoreInventoryDate(storeInventoryWeek);
                    } else {
                        //周期库存中未设置
                        storeInventoryDate = this.newStoreInventoryDate(product);
                    }
                }
            }
            if (productIdsInSell.contains(product.getProductId())) {
                storeInventoryDate.setInSell(true);
            }
            storeInventoryDate.setSelectDate(DateUtil.getBeginTime(repastDate, null));
            storeInventoryDate.setTimeBucketId(timeBucketId);
            storeInventoryDate.setStoreProduct(product);
            resultInventoryDateList.add(storeInventoryDate);
        }
        //计算产品库存
        return resultInventoryDateList;
    }
    
    /**
     * 产品库存盘点（返回指定类型产品库存，专为库存盘点提供）
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param products     已在销售中的产品列表
     * @param queryInvType 库存类型
     * @return
     * @author kaichen
     */
    public List<StoreInventoryDate> getInventoryDate(int merchantId, long storeId, long repastDate, long timeBucketId, List<StoreProduct> products, ProductInvTypeEnum queryInvType) {
    	boolean enableSlave = true;
        //定义返回结果，需要考虑周期库存、固定库存和产品销售情况
        List<StoreInventoryDate> resultInventoryDateList = new ArrayList<StoreInventoryDate>();
        //销售中的产品ID
        Set<Long> productIdsInSell = new HashSet<Long>();
        if (products != null) {
            for (StoreProduct storeProduct : products) {
            	productIdsInSell.add(storeProduct.getProductId());
            }
        }
        //指定日期产品库存
        List<StoreInventoryDate> inventoryDateList = storeInventoryDateDao.getStoreInventoryDateBySelectDate(merchantId, storeId, timeBucketId, repastDate, enableSlave);
        //添加在inventoryDateList但不在products中的产品，并且构造inventoryDateMap
        Map<Long, StoreInventoryDate> inventoryDateMap = this.addUnsellProductsAndBuildInventoryDate(merchantId, storeId, products, inventoryDateList, true,enableSlave);
        //为空则直接返回
        if (products == null || products.isEmpty()) {
            return resultInventoryDateList;
        }
		if (queryInvType.getValue() == ProductInvTypeEnum.WEEK.getValue()||queryInvType.getValue() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
			//周期库存
			int weekDay = storeMenuService.getWeekDayOfMenuDate(merchantId, storeId, repastDate);
	        Map<Long, StoreInventoryWeek> inventoryWeekMap = storeInventoryWeekDao.getStoreInventoryWeekMapByProductTime(merchantId, storeId, products, weekDay, timeBucketId, repastDate, enableSlave);
			for (StoreProduct product : products) {
				if (product.isDeleted()) continue;
				if (product.isInvEnabled()) {
					if(product.getInvType() != ProductInvTypeEnum.WEEK.getValue()&&product.getInvType() != ProductInvTypeEnum.WEEK_DAY.getValue()){
						continue;
					}
					StoreInventoryDate storeInventoryDate = null;
		            if (inventoryDateMap.containsKey(product.getProductId())) {
		                //在tb_store_inventory_date表中
		                storeInventoryDate = inventoryDateMap.get(product.getProductId());
		                StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(product.getProductId());
		                if (storeInventoryDate.getAmountPlan() == 0) {
		                    if (storeInventoryWeek != null) {
		                        storeInventoryDate.setAmountPlan(storeInventoryWeek.getAmount());
		                    } else {
		                        storeInventoryDate.setAmountPlan(product.getWeekAmount());
		                    }
		                }
		            } else {
		                //不在tb_store_inventory_date表中
		                StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(product.getProductId());
		                if (storeInventoryWeek != null) {
		                    //周期库存中已设置
		                    storeInventoryDate = this.newStoreInventoryDate(storeInventoryWeek);
		                } else {
		                    //周期库存中未设置
		                    storeInventoryDate = this.newStoreInventoryDate(product);
		                }
		            }
		            this.setStoreProductInfo(productIdsInSell, storeInventoryDate, product, repastDate, timeBucketId);
		            resultInventoryDateList.add(storeInventoryDate);
				}
			}
		} else if (queryInvType.getValue() == ProductInvTypeEnum.FIXED.getValue()) {
			//固定库存
	        Map<Long, StoreInventory> inventoryMap = storeInventoryDao.getStoreInventoryMapByProductIds(merchantId, storeId, new ArrayList<Long>(productIdsInSell), enableSlave);
	        Map<Long,Double> proudctOrderAmount = storeInventoryDateDao.getStoreInventoryProductOrderAmount(merchantId, storeId, products, enableSlave);
	        for (StoreProduct product : products) {
	        	if (product.isDeleted()) continue;
	        	if (product.isInvEnabled() && product.getInvType() == ProductInvTypeEnum.FIXED.getValue()) {
	        		StoreInventoryDate storeInventoryDate = null;
	        		StoreInventory storeInventory = inventoryMap.get(product.getProductId());
	                if (inventoryDateMap.containsKey(product.getProductId())) {
	                    storeInventoryDate = inventoryDateMap.get(product.getProductId());
						if (storeInventory != null) {
							storeInventoryDate.setAmount(storeInventory.getAmount());
	                    }
	                } else {
	                	if (storeInventory != null) {
	                		storeInventoryDate = this.newStoreInventoryDate(storeInventory);
						}else{
							storeInventoryDate = this.newStoreInventoryDate(product);
						}
	                }
					if (proudctOrderAmount.containsKey(product.getProductId())) {
						double amountOrderTotal = proudctOrderAmount.get(product.getProductId());
						storeInventoryDate.setAmountOrderTotal(amountOrderTotal);
					}
	                this.setStoreProductInfo(productIdsInSell, storeInventoryDate, product, repastDate, timeBucketId);
		            resultInventoryDateList.add(storeInventoryDate);
		        }
	        }
		}else{
			//库存未开启
			for (StoreProduct product : products) {
				if (product.isDeleted()) continue;
				if (!product.isInvEnabled()) {
					StoreInventoryDate storeInventoryDate = null;
					if (inventoryDateMap.containsKey(product.getProductId())) {
						storeInventoryDate = inventoryDateMap.get(product.getProductId());
		            } else{
		            	storeInventoryDate = this.newStoreInventoryDate(product);
		            }
					this.setStoreProductInfo(productIdsInSell, storeInventoryDate, product, repastDate, timeBucketId);
		            resultInventoryDateList.add(storeInventoryDate);
				}
			}
		}
        //计算产品库存
        return resultInventoryDateList;
    }
    
    private void setStoreProductInfo(Set<Long> productIdsInSell, StoreInventoryDate storeInventoryDate,StoreProduct product,long repastDate,long timeBucketId){
    	if (productIdsInSell.contains(product.getProductId())) {
            storeInventoryDate.setInSell(true);
        }
        storeInventoryDate.setSelectDate(DateUtil.getBeginTime(repastDate, null));
        storeInventoryDate.setTimeBucketId(timeBucketId);
        storeInventoryDate.setStoreProduct(product);
    }

    /**
     * 根据固定库存对象构造StoreInventoryDate
     *
     * @param storeInventory
     * @param timeBucketId
     * @param selectDate
     * @return
     */
    private StoreInventoryDate newStoreInventoryDate(StoreInventory storeInventory) {
        StoreInventoryDate storeInventoryDate = new StoreInventoryDate();
        storeInventoryDate.setMerchantId(storeInventory.getMerchantId());
        storeInventoryDate.setStoreId(storeInventory.getStoreId());
        storeInventoryDate.setProductId(storeInventory.getProductId());
        storeInventoryDate.setAmount(storeInventory.getAmount());
        return storeInventoryDate;
    }

    /**
     * 根据周期库存对象构造StoreInventoryDate
     *
     * @param storeInventoryWeek
     * @param selectDate
     * @return
     */
    private StoreInventoryDate newStoreInventoryDate(StoreInventoryWeek storeInventoryWeek) {
        StoreInventoryDate storeInventoryDate = new StoreInventoryDate();
        storeInventoryDate.setMerchantId(storeInventoryWeek.getMerchantId());
        storeInventoryDate.setStoreId(storeInventoryWeek.getStoreId());
        storeInventoryDate.setProductId(storeInventoryWeek.getProductId());
        storeInventoryDate.setAmount(storeInventoryWeek.getAmount());
        storeInventoryDate.setAmountPlan(storeInventoryWeek.getAmount());
        return storeInventoryDate;
    }

    /**
     * 根据产品对象构造StoreInventoryDate
     *
     * @param product
     * @param timeBucketId
     * @param selectDate
     * @return
     */
    private StoreInventoryDate newStoreInventoryDate(StoreProduct product) {
        StoreInventoryDate storeInventoryDate = new StoreInventoryDate();
        storeInventoryDate.setMerchantId(product.getMerchantId());
        storeInventoryDate.setStoreId(product.getStoreId());
        storeInventoryDate.setProductId(product.getProductId());
        storeInventoryDate.setAmount(product.getWeekAmount());
        storeInventoryDate.setAmountPlan(product.getWeekAmount());
        return storeInventoryDate;
    }

    /**
     * 日常库存盘点修改剩余
     *
     * @param storeInventoryDateAmountUpdateParam
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreInventoryDate updateInventoryDateAmount(StoreInventoryDateAmountUpdateParam storeInventoryDateAmountUpdateParam) throws T5weiException {
    	boolean enableSlave = false;
        int merchantId = storeInventoryDateAmountUpdateParam.getMerchantId();
        long storeId = storeInventoryDateAmountUpdateParam.getStoreId();
        long repastDate = DateUtil.getBeginTime(storeInventoryDateAmountUpdateParam.getRepastDate(), null);
        long timeBucketId = storeInventoryDateAmountUpdateParam.getTimeBucketId();
        long productId = storeInventoryDateAmountUpdateParam.getProductId();
        double amountRemain = storeInventoryDateAmountUpdateParam.getAmount(); // 实际盘点的剩余量，包含待出餐
        if (merchantId == 0 || storeId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), " store_id, merchant_id can not be null ");
        }
        if (productId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_ID_CAN_NOT_NULL.getValue(), " product_id can not be null ");
        }
        if (repastDate == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_CAN_NOT_NULL.getValue(),
                    "when update inventory date amount product's repastDate param can not be null");
        }
        long nowTime = System.currentTimeMillis();
        long instanceDateTime = DateUtil.getBeginTime(nowTime, null);
        StoreTimeBucket storeTimeBucket = null;
		if (timeBucketId > 0) {
        	storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, enableSlave);
        }
        if (repastDate < instanceDateTime) {
            this.checkStoreInvTimeBucket(merchantId, storeId, repastDate, storeTimeBucket);
        }
        //判断产品库存模式
        StoreProduct storeProduct = storeProductDao.getById(merchantId, storeId, productId, false, false);
        if (storeProduct == null || storeProduct.isDeleted()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_NOT_EXIST.getValue(),
                    DataUtil.infoWithParams("product not exist, product_id=#1, store_id=#2 ", new Object[]{productId, storeId}));
        }
        //库存未开启
        if (!storeProduct.isInvEnabled()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_TYPE_NOT_SETUP.getValue(),
                    DataUtil.infoWithParams("store product inv_type do not setup, product_id=#1, store_id=#2 ", new Object[]{productId, storeId}));
        }
        //当前时段销售中的产品
        List<Long> productIdsInSell = storeChargeItemService.getStoreProductIdsForDate(merchantId, storeId, repastDate, timeBucketId, false, false);
        //周天库存营业时段为0
        if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
        	timeBucketId = 0L;
        }
        double amountOrder = this.getStoreOrderProductOrderAmount(merchantId, storeId, repastDate, timeBucketId, storeProduct, StoreOrderInvTypeEnum.BOOK_ORDER);
        double amountTakeup = storeMealTakeupDAO.getStoreMealTakeupProduct(merchantId, storeId, repastDate, timeBucketId, productId, enableSlave);
        double amountCheckout = storeMealCheckoutDAO.getStoreMealCheckoutProduct(merchantId, storeId, repastDate, timeBucketId, productId, enableSlave);
		double amountTake = amountTakeup + amountCheckout;
		StoreInventoryDate inventoryDate = storeInventoryDateDao.getStoreInventoryDateByProductSelectDate(merchantId, storeId, timeBucketId, productId, repastDate, enableSlave);
		if (inventoryDate != null) {
			inventoryDate.setStoreProduct(storeProduct);
		}
        int invType = storeProduct.getInvType();
        //固定库存模式，存在则更新，不存在则创建
        if (invType == ProductInvTypeEnum.FIXED.getValue()) {
        	double amountOrderTotal = storeInventoryDateDao.getStoreInventoryProductOrderAmount(merchantId, storeId, storeProduct, enableSlave); //总预定
            double amount = NumberUtil.sub(amountRemain, amountTakeup); // 固定库存实际剩余量
			if (amount < 0) {
				amount = 0D;
			}
        	this.updateInventory(storeProduct, amount);
            if (inventoryDate != null) {
            	inventoryDate.setAmountOrderTotal(amountOrderTotal);
                inventoryDate.setAmountOrder(amountOrder);
                inventoryDate.setAmountTakeup(amountTakeup);
                inventoryDate.setAmountCheckout(amountCheckout);
                inventoryDate.setAmountTake(amountTake);
                inventoryDate.setAmount(amount);
                inventoryDate.setUpdateTime(System.currentTimeMillis());
                storeInventoryDateDao.update(inventoryDate);
            } else {
                inventoryDate = new StoreInventoryDate();
                inventoryDate.setMerchantId(merchantId);
                inventoryDate.setStoreId(storeId);
                inventoryDate.setProductId(productId);
                inventoryDate.setTimeBucketId(timeBucketId);
                inventoryDate.setSelectDate(repastDate);
                inventoryDate.setModified(StoreInventoryModifiedEnum.DATE.getValue());
                inventoryDate.setAmountOrderTotal(amountOrderTotal);
                inventoryDate.setAmountOrder(amountOrder);
                inventoryDate.setAmountTakeup(amountTakeup);
                inventoryDate.setAmountCheckout(amountCheckout);
                inventoryDate.setAmountTake(amountTake);
                inventoryDate.setAmount(amount);
                inventoryDate.setUpdateTime(System.currentTimeMillis());
                inventoryDate.setCreateTime(System.currentTimeMillis());
                try {
                    storeInventoryDateDao.replace(inventoryDate);
                } catch (DuplicateKeyException e) {
                    storeInventoryDateDao.update(inventoryDate);
                }
            }
            if (productIdsInSell.contains(storeProduct.getProductId())) {
                inventoryDate.setInSell(true);
            }
            inventoryDate.setStoreProduct(storeProduct);
            inventoryDate.setStoreTimeBucket(storeTimeBucket);
            return inventoryDate;
        }
        // 周期库存模式，查询指定日期库存情况，存在则更新，不存在则创建
        int weekDay = storeMenuService.getWeekDayOfMenuDate(merchantId, storeId, repastDate);
        StoreInventoryWeek inventoryWeek = storeInventoryWeekDao.getStoreInventoryWeekByProductTime(merchantId, storeId, productId, weekDay, timeBucketId, repastDate);
        double amount = NumberUtil.add(amountRemain, amountCheckout); // 计划库存实际供应量
		if (amount < 0) {
			amount = 0D;
		}
        if (inventoryDate != null) {
            inventoryDate.setModified(StoreInventoryModifiedEnum.DATE.getValue());
            inventoryDate.setAmount(amount);
            inventoryDate.setAmountOrder(amountOrder);
            inventoryDate.setAmountTakeup(amountTakeup);
            inventoryDate.setAmountCheckout(amountCheckout);
            inventoryDate.setAmountTake(amountTake);
            inventoryDate.setUpdateTime(System.currentTimeMillis());
            storeInventoryDateDao.update(inventoryDate);
        } else {
            inventoryDate = new StoreInventoryDate();
            inventoryDate.setMerchantId(merchantId);
            inventoryDate.setStoreId(storeId);
            inventoryDate.setTimeBucketId(timeBucketId);
            inventoryDate.setSelectDate(repastDate);
            inventoryDate.setInvWeekId(inventoryWeek);
            inventoryDate.setModified(StoreInventoryModifiedEnum.DATE.getValue());
            inventoryDate.setProductId(productId);
            inventoryDate.setAmount(amount);
            inventoryDate.setAmountOrder(amountOrder);
            inventoryDate.setAmountTakeup(amountTakeup);
            inventoryDate.setAmountCheckout(amountCheckout);
            inventoryDate.setAmountTake(amountTake);
            inventoryDate.setUpdateTime(System.currentTimeMillis());
            inventoryDate.setCreateTime(System.currentTimeMillis());
            try {
                storeInventoryDateDao.replace(inventoryDate);
            } catch (DuplicateKeyException e) {
                storeInventoryDateDao.update(inventoryDate);
            }
        }
        if (inventoryWeek == null) {
            inventoryDate.setAmountPlan(storeProduct.getWeekAmount());
        } else {
            inventoryDate.setAmountPlan(inventoryWeek.getAmount());
        }
        inventoryDate.setSelectDate(DateUtil.getBeginTime(repastDate, null));
        inventoryDate.setTimeBucketId(timeBucketId);
        inventoryDate.setStoreProduct(storeProduct);
        inventoryDate.setStoreTimeBucket(storeTimeBucket);
        if (productIdsInSell.contains(storeProduct.getProductId())) {
            inventoryDate.setInSell(true);
        }
        return inventoryDate;
    }
    
    /**
     * 校验库存时间
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param storeTimeBucket
     * @throws T5weiException
     */
    private void checkStoreInvTimeBucket(int merchantId, long storeId, long repastDate, StoreTimeBucket storeTimeBucket) throws T5weiException{
    	long now = System.currentTimeMillis();
    	long repastDateTimeMillis = DateUtil.getBeginTime(repastDate, null);
    	long currentTimeBeginMillis = DateUtil.getBeginTime(now, null);
    	if (repastDateTimeMillis < currentTimeBeginMillis) {
        	MutableDateTime mdt = new MutableDateTime(now);
			if (storeTimeBucket.getEndTime() > mills_in_day){
				MutableDateTime mdt2 = new MutableDateTime(repastDateTimeMillis + storeTimeBucket.getEndTime());
	        	mdt.addMinutes(-mdt2.getMinuteOfDay());
        	}
        	long timeBeginMillis = DateUtil.getBeginTime(mdt.getMillis(), null);
			if (repastDateTimeMillis == timeBeginMillis && storeTimeBucket.compareTo(now) == 0){
				//减去跨天的小时数后与就餐日期相等且在营业时段的可以修改库存
        	}else{
        		//小于当日的，不能点餐
        		throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_HISTORY_CAN_NOT_UPDATE.getValue(),
                        DataUtil.infoWithParams("store repastDateTimeMillis < currentTimeBeginMillis, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));
        	}
        } 
    }

    /**
     * 下单时检查库存量
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param orderChargeItems
     * @param orderItemNumMap
     * @throws T5weiException
     */
    public void checkInventoryForOrderPlace(int merchantId, long storeId, long repastDate, long timeBucketId, List<StoreChargeItem> orderChargeItems, Map<Long, Double> orderItemNumMap) throws T5weiException {
        if (orderChargeItems == null || orderChargeItems.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "store order place input params incomplete");
        }
        if (orderItemNumMap == null || orderItemNumMap.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "store order place input params incomplete");
        }
        List<StoreProduct> orderProducts = new ArrayList<StoreProduct>();
        //检查库存是否足够
        Map<Long, Double> orderProductNumMap = new HashMap<Long, Double>();
        for (StoreChargeItem storeChargeItem : orderChargeItems) {
            double orderNum = orderItemNumMap.get(storeChargeItem.getChargeItemId());
            List<StoreChargeSubitem> storeChargeSubitems = storeChargeItem.getStoreChargeSubitems();
			if (storeChargeSubitems == null || storeChargeSubitems.isEmpty()) {
				log.error("####StoreChargeItem=" + JsonUtil.build(storeChargeItem) + ",storeChargeSubitems is null");
			}
            for (StoreChargeSubitem storeChargeSubitem : storeChargeSubitems) {
                long productId = storeChargeSubitem.getProductId();
                Double amount = orderProductNumMap.get(productId);
                if (amount == null) {
                    amount = Double.valueOf(0);
                }
                double unitAmount = storeChargeSubitem.getAmount();
                double num = NumberUtil.mul(unitAmount, orderNum);
                amount = NumberUtil.add(amount, num);
                orderProductNumMap.put(productId, amount);
                StoreProduct storeProduct = storeChargeSubitem.getStoreProduct();
                orderProducts.add(storeProduct);
            }
        }
        List<StoreInventoryCheck> inventoryCheckResult = this.checkInventoryForOrderPlace(merchantId, storeId, repastDate, timeBucketId, orderProductNumMap);
        if (!inventoryCheckResult.isEmpty()) {
            Map<Long, StoreInventoryCheck> inventoryCheckMap = new HashMap<Long, StoreInventoryCheck>();
            for (StoreChargeItem chargeItem : orderChargeItems) {
                Map<Long, StoreChargeSubitem> products = chargeItem.getProducts();
                for (StoreInventoryCheck storeInventoryCheck : inventoryCheckResult) {
                    if (products.containsKey(storeInventoryCheck.getProductId())) {
                        StoreChargeSubitem subitem = products.get(storeInventoryCheck.getProductId());
                        storeInventoryCheck.setChargeItem(chargeItem.getChargeItemId(), chargeItem.getName(), subitem.getAmount());
                        inventoryCheckMap.put(chargeItem.getChargeItemId(), storeInventoryCheck);
                        continue;
                    }
                }
            }
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_SUBITEM_CHARGE_INV_LACK.getValue(), JsonUtil.build(inventoryCheckMap.values()));
        }
    }
    
    /**
     * 支付未占用库存情况，检查库存量
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param orderChargeItems
     * @param orderItemNumMap
     * @throws T5weiException
     */
    public void checkInventoryForOrderPlace(int merchantId, long storeId, StoreOrder storeOrder) throws T5weiException {
        long repastDate = storeOrder.getRepastDate();
        long timeBucketId = storeOrder.getTimeBucketId();
        if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
        	storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        }
        Map<Long, Double> orderProductNumMap = storeOrderHelper.getProductNumOfStoreOrder(storeOrder);
        List<StoreInventoryCheck> inventoryCheckResult = this.checkInventoryForOrderPlace(merchantId, storeId, repastDate, timeBucketId, orderProductNumMap);
        if (!inventoryCheckResult.isEmpty()) {
            Map<Long, StoreInventoryCheck> inventoryCheckMap = new HashMap<Long, StoreInventoryCheck>();
            List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
            for (StoreOrderItem storeOrderItem : storeOrderItems) {
                Map<Long, StoreOrderSubitem> products = storeOrderItem.getProducts();
                for (StoreInventoryCheck storeInventoryCheck : inventoryCheckResult) {
                    if (products.containsKey(storeInventoryCheck.getProductId())) {
                        StoreOrderSubitem subitem = products.get(storeInventoryCheck.getProductId());
                        storeInventoryCheck.setChargeItem(storeOrderItem.getChargeItemId(), storeOrderItem.getChargeItemName(), subitem.getAmount());
                        inventoryCheckMap.put(storeOrderItem.getChargeItemId(), storeInventoryCheck);
                        continue;
                    }
                }
            }
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_SUBITEM_CHARGE_INV_LACK.getValue(), JsonUtil.build(inventoryCheckMap.values()));
        }
    }

    private List<StoreInventoryCheck> checkInventoryForOrderPlace(int merchantId, long storeId, long repastDate, long timeBucketId, Map<Long, Double> orderProductNumMap) throws T5weiException {
    	boolean enableSlave = false;
        List<StoreInventoryCheck> inventoryChecks = new ArrayList<StoreInventoryCheck>();
        Set<Long> productIdSet = orderProductNumMap.keySet();
        List<Long> productIds = new ArrayList<Long>(productIdSet);
        List<StoreProduct> products = storeProductDao.getListInIds(merchantId, storeId, productIds);
        int weekDay = storeMenuService.getWeekDayOfMenuDate(merchantId, storeId, repastDate);
        Map<Long, StoreInventoryDate> inventoryDateMap = storeInventoryDateDao.getStoreInventoryDateMapByProductSelectDate(merchantId, storeId, timeBucketId, products, repastDate, enableSlave);
        Map<Long, StoreInventoryWeek> inventoryWeekMap = storeInventoryWeekDao.getStoreInventoryWeekMapByProductTime(merchantId, storeId, products, weekDay, timeBucketId, repastDate, enableSlave);
        Map<Long, StoreInventory> inventoryMap = storeInventoryDao.getStoreInventoryMapByProductIds(merchantId, storeId, productIds, enableSlave);
        //产品总预定数量
        Map<Long,Double> amountOrderTotalMap = this.getStoreOrderProductOrderAmountMap(merchantId, storeId, repastDate, timeBucketId, products, StoreOrderInvTypeEnum.BOOK_ORDER);
        //遍历订单的所有产品
		for (StoreProduct storeProduct : products) {
            long productId = storeProduct.getProductId();
            String productName = storeProduct.getName();
            //产品下单订购数量
            double orderAmount = orderProductNumMap.getOrDefault(productId, 0D);
            int invType = storeProduct.getInvType();
            //设置产品信息
            StoreInventoryDate storeInventoryDate = inventoryDateMap.get(productId);
			if (storeInventoryDate != null) {
				storeInventoryDate.setStoreProduct(storeProduct);
			}
            // 库存模式未开启
            if (!storeProduct.isInvEnabled()) {
				if (storeInventoryDate != null && storeInventoryDate.isNothingness()){
					// 本时段库存已估清
					inventoryChecks.add(new StoreInventoryCheck(productId, productName, 0, orderAmount));
            	}
                continue;
            }
            double canOrderNum; // 可售卖
            if (storeInventoryDate != null) {
            	if (invType == ProductInvTypeEnum.WEEK.getValue()||invType == ProductInvTypeEnum.WEEK_DAY.getValue()) {
            		canOrderNum = storeInventoryDate.getAmountCanSell();
            	}else if(invType == ProductInvTypeEnum.FIXED.getValue()){
            		double amountOrderTotal = amountOrderTotalMap.getOrDefault(productId, 0D);//总预定数量
            		StoreInventory storeInventory = inventoryMap.get(productId);
                    double amount = 0D;
                    if (storeInventory == null) {
                        inventoryChecks.add(new StoreInventoryCheck(productId, productName, 0, orderAmount));
                        continue;
                    } else {
                        amount = storeInventory.getAmount();//固定库存剩余量
                    }
            		storeInventoryDate.setAmount(amount);
					storeInventoryDate.setAmountOrderTotal(amountOrderTotal);
					canOrderNum = storeInventoryDate.getAmountCanSell();
            	}else {
                    continue;
                }
            } else {
				if (invType == ProductInvTypeEnum.WEEK.getValue() || invType == ProductInvTypeEnum.WEEK_DAY.getValue()) {
                    StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(productId);
                    if (storeInventoryWeek == null) {
                    	canOrderNum = storeProduct.getWeekAmount();
                    } else {
                    	canOrderNum = storeInventoryWeek.getAmount();
                    }
                } else if (invType == ProductInvTypeEnum.FIXED.getValue()) {
                	StoreInventory storeInventory = inventoryMap.get(productId);
                    double amount = 0D;
                    if (storeInventory == null) {
                        inventoryChecks.add(new StoreInventoryCheck(productId, productName, 0, orderAmount));
                        continue;
                    } else {
                        amount = storeInventory.getAmount();//固定库存剩余量
                    }
                    canOrderNum = amount;
                }else {
                    continue;
                }
            }
            if (orderAmount > canOrderNum) {
                inventoryChecks.add(new StoreInventoryCheck(productId, productName, canOrderNum, orderAmount));
            }
        }
        return inventoryChecks;
    }
    
    
    /**
     * 根据订单重新统计库存（已预定未取餐、已取餐）
     *
     * @param storeOrder
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateInventoryDateByOrder(StoreOrder storeOrder) throws T5weiException {
    	if(storeOrder.getClientType() == ClientTypeEnum.MINA.getValue()){
    		return;
	    }
    	int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
        if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
        	storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        }
        long repastDate = storeOrder.getRepastDate();
        long timeBucketId = storeOrder.getTimeBucketId();
        
        Map<Long, Double> orderProductNumMap = storeOrderHelper.getProductNumOfStoreOrder(storeOrder);
		if (orderProductNumMap.isEmpty()) {
			return;
		}
        Set<Long> productIdSet = orderProductNumMap.keySet();
        List<Long> productIds = new ArrayList<Long>(productIdSet);
        this.updateInventoryDate(merchantId, storeId, repastDate, timeBucketId, productIds);
    }
    
    /**
     * 更新库存估清状态
     * @param storeInventoryDateNothingnessUpdateParam
     * @throws T5weiException
     */
    public int updateInventoryDateNothingness(StoreInventoryDateNothingnessUpdateParam storeInventoryDateNothingnessUpdateParam) throws T5weiException{
    	int merchantId = storeInventoryDateNothingnessUpdateParam.getMerchantId(); 
    	long storeId = storeInventoryDateNothingnessUpdateParam.getStoreId(); 
    	long repastDate = storeInventoryDateNothingnessUpdateParam.getRepastDate(); 
    	long timeBucketId = storeInventoryDateNothingnessUpdateParam.getTimeBucketId();
    	List<Long> productIds = storeInventoryDateNothingnessUpdateParam.getProductIds();
    	List<StoreInventoryDate> storeInventoryDates = this.updateInventoryDate(merchantId, storeId, repastDate, timeBucketId, productIds);
		for (StoreInventoryDate storeInventoryDate : storeInventoryDates){
    		StoreProduct product = storeInventoryDate.getStoreProduct();
			if (product == null) {
				continue;
			}
			if (product.isInvEnabled()) {
				productIds.remove(product.getProductId());
			}
    	}
		boolean nothingness = storeInventoryDateNothingnessUpdateParam.isNothingness();
		int num = storeInventoryDateDao.updateStoreInventoryDateNothingness(merchantId, storeId, repastDate, timeBucketId, productIds, nothingness);
		return num;
    }
    
    /**
     * 根据订单重新统计库存（已预定未取餐、已取餐）
     *
     * @param storeOrder
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public List<StoreInventoryDate> updateInventoryDate(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> productIds) throws T5weiException {
    	List<StoreInventoryDate> storeInventoryDates = Lists.newArrayList(); 
    	if (productIds == null || productIds.isEmpty()) {
			return storeInventoryDates;
		}
    	boolean enableSlave = false;
    	int weekDay = storeMenuService.getWeekDayOfMenuDate(merchantId, storeId, repastDate);
        List<StoreProduct> products = storeProductDao.getListInIds(merchantId, storeId, productIds);
        Map<Long, Double> productOrderAmountMap = this.getStoreOrderProductOrderAmountMap(merchantId, storeId, repastDate, timeBucketId, products,StoreOrderInvTypeEnum.BOOK_ORDER);
        Map<Long, Double> productTakeupAmountMap = storeMealTakeupDAO.getStoreMealTakeupProducts(merchantId, storeId, repastDate, timeBucketId, products, enableSlave);
        Map<Long, Double> productCheckoutAmountMap = storeMealCheckoutDAO.getStoreMealCheckoutProducts(merchantId, storeId, repastDate, timeBucketId, products, enableSlave);
        Map<Long, StoreInventoryDate> inventoryDateMap = storeInventoryDateDao.getStoreInventoryDateMapByProductSelectDate(merchantId, storeId, timeBucketId, products, repastDate, enableSlave);
        Map<Long, StoreInventoryWeek> inventoryWeekMap = storeInventoryWeekDao.getStoreInventoryWeekMapByProductTime(merchantId, storeId, products, weekDay, timeBucketId, repastDate, enableSlave);
		for (StoreProduct product : products) {
			long productId = product.getProductId();
            int invType = product.getInvType();
			if (invType == ProductInvTypeEnum.WEEK_DAY.getValue()) {
				timeBucketId = 0L;
			}
            Double amountOrder = productOrderAmountMap.get(productId);
            Double amountTakeup = productTakeupAmountMap.get(productId);
            Double amountCheckout = productCheckoutAmountMap.get(productId);
			if (amountOrder == null || amountOrder < 0) {
				amountOrder = Double.valueOf(0);
			}
			if (amountTakeup == null || amountTakeup < 0) {
				amountTakeup = Double.valueOf(0);
			}
			if (amountCheckout == null || amountCheckout < 0) {
				amountCheckout = Double.valueOf(0);
			}
            double amountTake = amountTakeup + amountCheckout;
            //更新tb_store_inventory_date，修改已预定，存在则更新，不存在则创建
            StoreInventoryDate storeInventoryDate = inventoryDateMap.get(productId);
            if (storeInventoryDate == null) {
                //不存在则创建
                storeInventoryDate = new StoreInventoryDate();
                storeInventoryDate.setMerchantId(merchantId);
                storeInventoryDate.setStoreId(storeId);
                storeInventoryDate.setTimeBucketId(timeBucketId);
                storeInventoryDate.setSelectDate(repastDate);
                storeInventoryDate.setProductId(productId);
                storeInventoryDate.setUpdateTime(System.currentTimeMillis());
                storeInventoryDate.setCreateTime(System.currentTimeMillis());
                if (invType == ProductInvTypeEnum.WEEK.getValue()||invType == ProductInvTypeEnum.WEEK_DAY.getValue()) {
                    StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(productId);
                    if (storeInventoryWeek != null) {
                        storeInventoryDate.setAmountPlan(storeInventoryWeek.getAmount());
                        storeInventoryDate.setAmount(storeInventoryWeek.getAmount());
                        storeInventoryDate.setInvWeekId(storeInventoryWeek);
                    } else {
                        double weekAmount = product.getWeekAmount();
                        storeInventoryDate.setAmountPlan(weekAmount);
                        storeInventoryDate.setAmount(weekAmount);
                    }
                    storeInventoryDate.setModified(StoreInventoryModifiedEnum.WEEK.getValue());
                } else {
                    storeInventoryDate.setModified(StoreInventoryModifiedEnum.UNKNOWN.getValue());
                }
                storeInventoryDate.setAmountOrder(amountOrder);
                storeInventoryDate.setAmountTakeup(amountTakeup);
                storeInventoryDate.setAmountCheckout(amountCheckout);
                storeInventoryDate.setAmountTake(amountTake);
                storeInventoryDateDao.replace(storeInventoryDate);
            } else {
                storeInventoryDate.setAmountOrder(amountOrder);
                storeInventoryDate.setAmountTakeup(amountTakeup);
                storeInventoryDate.setAmountCheckout(amountCheckout);
                storeInventoryDate.setAmountTake(amountTake);
                storeInventoryDate.setUpdateTime(System.currentTimeMillis());
                storeInventoryDateDao.update(storeInventoryDate);
            }
            storeInventoryDate.setStoreProduct(product);
            storeInventoryDates.add(storeInventoryDate);
        }
		return storeInventoryDates;
    }

    /**
     * 更新固定库存
     *
     * @param storeOrder
     * @throws TException 
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreInventoryInvset> deductFixInventoryForOrder(StoreOrder storeOrder) throws TException {
    	List<StoreInventoryInvset> storeInventoryInvsets = new ArrayList<StoreInventoryInvset>();
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
        String orderId = storeOrder.getOrderId();
        if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
        	storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        }
        Map<Long, Double> orderProductNumMap = storeOrderHelper.getProductNumOfStoreOrder(storeOrder);
        Set<Long> productIdSet = orderProductNumMap.keySet();
        List<Long> productIds = new ArrayList<Long>(productIdSet);
        Map<Long, StoreProduct> productMap = storeProductDao.getMapInIds(merchantId, storeId, productIds);
        for (Iterator<Long> it = productIdSet.iterator(); it.hasNext(); ) {
            Long productId = (Long) it.next();
            StoreProduct product = productMap.get(productId);
            if (!product.isInvEnabled()) {
                continue;
            }
            if (product.getInvType() == ProductInvTypeEnum.FIXED.getValue()) {
                //扣减产品固定库存
                Double orderAmount = orderProductNumMap.get(productId);
                StoreInventoryInvset storeInventoryInvset = this._deductFixInventoryForOrder(merchantId, storeId, productId, orderId, orderAmount, "meal takeup");
				if (storeInventoryInvset != null) {
					storeInventoryInvsets.add(storeInventoryInvset);
				}
            }
        }
        // 库存报警
        wechatTempNotifyService.sendInventoryNotEnoughAlarm(storeOrder, storeInventoryInvsets);
        return storeInventoryInvsets;
    }
    
    /**
     * 恢复已出餐库存
     * @param storeMealCheckoutItems
     * @throws T5weiException 
     */
	public void refundFixInventory(List<StoreMealCheckout> storeMealCheckoutItems) throws T5weiException {
		if (storeMealCheckoutItems == null || storeMealCheckoutItems.isEmpty()) {
			return;
		}
		for (StoreMealCheckout storeMealCheckout : storeMealCheckoutItems) {
			int merchantId = storeMealCheckout.getMerchantId();
			long storeId = storeMealCheckout.getStoreId();
			long productId = storeMealCheckout.getProductId();
			String orderId = storeMealCheckout.getOrderId();
			double amountCheckout = storeMealCheckout.getAmountCheckout();
			if (amountCheckout < 0) {
				double refundAmount = NumberUtil.mul(amountCheckout, storeMealCheckout.getAmount());//退菜项目数量 x 菜品组成
				this._deductFixInventoryForOrder(merchantId, storeId, productId, orderId, refundAmount, "refund meal");
			}
		}
	}

    /**
     * 统计店铺指定日期运营时段内的产品
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param storeProducts
     * @param orderType
     * @return
     */
    private Map<Long, Double> getStoreOrderProductOrderAmountMap(int merchantId, long storeId, long repastDate, long timeBucketId, List<StoreProduct> storeProducts, StoreOrderInvTypeEnum orderType) {
    	boolean enableSlave = false;
    	Map<Long,Double> productAmount = new HashMap<Long,Double>();
    	List<Long> dayProductIds = new ArrayList<Long>();
		List<Long> otherProductIds = new ArrayList<Long>();
		for (StoreProduct product : storeProducts) {
			if (product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
				dayProductIds.add(product.getProductId());
			}else{
				otherProductIds.add(product.getProductId());
			}
		}
		Map<Long,Double> dayProductAmount = storeOrderDao.countStoreOrderSubitemAmountMap(merchantId, storeId, repastDate, 0, dayProductIds, orderType, enableSlave);
		productAmount.putAll(dayProductAmount);
		if (timeBucketId > 0) {
			Map<Long,Double> otherProductAmount = storeOrderDao.countStoreOrderSubitemAmountMap(merchantId, storeId, repastDate, timeBucketId, otherProductIds, orderType, enableSlave);
			productAmount.putAll(otherProductAmount);
		}
        return productAmount;
    }
    
    /**
     * 统计店铺指定日期运营时段内的产品
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param storeProduct
     * @param orderType
     * @return
     */
    private double getStoreOrderProductOrderAmount(int merchantId, long storeId, long repastDate, long timeBucketId, StoreProduct storeProduct, StoreOrderInvTypeEnum orderType) {
    	boolean enableSlave = false; 
    	if(storeProduct==null){
    		return 0D;
    	}
    	long productId = storeProduct.getProductId();
    	List<Long> productIds = new ArrayList<Long>();
        productIds.add(productId);
        Map<Long, Double> amountMap;
    	if(storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
    		amountMap = storeOrderDao.countStoreOrderSubitemAmountMap(merchantId, storeId, repastDate, 0, productIds, orderType, enableSlave);
    	}else{
    		amountMap = storeOrderDao.countStoreOrderSubitemAmountMap(merchantId, storeId, repastDate, timeBucketId, productIds, orderType, enableSlave);
    	}
        return amountMap.getOrDefault(productId, 0D);
    }

    /**
     * 获取产品的库存准确率
     * @param merchantId
     * @param storeId
     * @param storeProducts
     * @param timeBuckets
     * @param selectedDate
     * @return
     * @throws TMerchantException
     * @throws TException
     */
	public ProductInventoryPrecisionDTO getProductInventoryPrecision(int merchantId, long storeId, List<StoreTimeBucket> timeBuckets, long selectedDate) throws TMerchantException, TException {
		ProductInventoryPrecisionDTO inventoryPrecisionDTO = new ProductInventoryPrecisionDTO();
		StoreDTO store = storeQueryFacade.getStore(merchantId, storeId);
		inventoryPrecisionDTO.setMerchantId(merchantId);
		inventoryPrecisionDTO.setStoreId(storeId);
		inventoryPrecisionDTO.setStoreName(store.getName());
		inventoryPrecisionDTO.setSendDate(selectedDate);
		inventoryPrecisionDTO.setSendStatus(true);
		
		int totalProductCount = 0;//库存产品数
		int totalGoodProductCount = 0;//优秀库存率产品数
		int weekDay = storeMenuService.getWeekDayOfMenuDate(merchantId, storeId, selectedDate);
		ProductInventoryPrecisionTableDTO dateInventoryTable = new ProductInventoryPrecisionTableDTO();
		List<ProductInventoryPrecisionTableDTO> weekInventoryTables = new ArrayList<ProductInventoryPrecisionTableDTO>();
		Map<Long,ProductInventoryPrecisionRowDTO> dateInventoryRows = new HashMap<Long,ProductInventoryPrecisionRowDTO>();
		for (int i = 0; i < timeBuckets.size() ; i++) {
			StoreTimeBucket storeTimeBucket = timeBuckets.get(i);
			Map<Long,ProductInventoryPrecisionRowDTO> weekInventoryRows = new HashMap<Long,ProductInventoryPrecisionRowDTO>();
			ProductInventoryPrecisionTableDTO weekInventoryTable = new ProductInventoryPrecisionTableDTO();
			weekInventoryTable.setTimeBucketName(storeTimeBucket.getName());
			weekInventoryTable.setTimeBucketStartTime(storeTimeBucket.getStartTime());
			weekInventoryTable.setTimeBucketEndTime(storeTimeBucket.getEndTime());
			
			long timeBucketId = storeTimeBucket.getTimeBucketId();
			List<StoreProduct> products = storeChargeItemService.getStoreProductsForDate(merchantId, storeId, selectedDate, timeBucketId, true, true);
			if(products.isEmpty()){
				continue;
			}
			for (Iterator<StoreProduct> iterator = products.iterator(); iterator.hasNext();) {
				StoreProduct storeProduct = iterator.next();
				if(!storeProduct.isInvEnabled() || storeProduct.getInvType() == ProductInvTypeEnum.FIXED.getValue() || storeProduct.getInvType() == ProductInvTypeEnum.NONE.getValue()){
					iterator.remove();
				}
			}
			List<StoreInventoryDate> inventoryDateList = storeInventoryDateDao.getStoreInventoryDateBySelectDate(merchantId, storeId, timeBucketId, selectedDate, true);
			Map<Long, StoreInventoryDate> inventoryDateMap = this.addUnsellProductsAndBuildInventoryDate(merchantId, storeId, products, inventoryDateList, true, true);
			Map<Long, StoreInventoryWeek> inventoryWeekMap = storeInventoryWeekDao.getStoreInventoryWeekMapByProductTime(merchantId, storeId, products, weekDay, timeBucketId, selectedDate, true);
			for (StoreProduct product : products) {
				long productId = product.getProductId();
				StoreInventoryDate storeInventoryDate = inventoryDateMap.get(productId);
				ProductInventoryPrecisionRowDTO row = new ProductInventoryPrecisionRowDTO();
				row.setProductId(productId);
				row.setProductName(product.getName());
				row.setPlanInventory(0);
				row.setSaleAmount(0);
				row.setInventoryPrecision(0.0);
				if(storeInventoryDate != null){//有计划库存有销售量
					row.setPlanInventory(storeInventoryDate.getAmountPlan());
					row.setSaleAmount(storeInventoryDate.getAmountTake());
					//准确率 = 销售量 / 计划库存
					double inventoryPrecision = storeInventoryDate.getAmountPlan() != 0.0 ? (NumberUtil.div(storeInventoryDate.getAmountTake(), storeInventoryDate.getAmountPlan(), 4)) : 0 ;
					row.setInventoryPrecision(inventoryPrecision);
					if(!dateInventoryRows.containsKey(productId) || !weekInventoryRows.containsKey(productId)){
						if(row.getSaleAmount() > 0 && row.getPlanInventory() > 0){
							if(product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){//按天库存只算一遍
								if(i == 0){
									totalProductCount++;
									if(inventoryPrecision >= 0.9 && inventoryPrecision <= 1.2){//优秀库存率
										totalGoodProductCount++;
									}
									dateInventoryRows.put(productId, row);
								}
							} else {
								totalProductCount++;
								if(inventoryPrecision >= 0.9 && inventoryPrecision <= 1.2){//优秀库存率
									totalGoodProductCount++;
								}
								weekInventoryRows.put(productId, row);
							}
						}
					}
				} else {
					StoreInventoryWeek storeInventoryWeek = inventoryWeekMap.get(productId);
					if(storeInventoryWeek != null){//无计划库存
						row.setPlanInventory(storeInventoryWeek.getAmount());
						if(row.getPlanInventory() > 0){
							if(product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
								if(i == 0){
									dateInventoryRows.put(productId, row);
								}
							} else {
								weekInventoryRows.put(productId, row);
							}
						}
					}
				}
			}
			List<ProductInventoryPrecisionRowDTO> weekInventoryPrecisionRows = new LinkedList<ProductInventoryPrecisionRowDTO>(weekInventoryRows.values());
			if(weekInventoryPrecisionRows != null && !weekInventoryPrecisionRows.isEmpty()){
				this.sortProductInventoryPrecisionRowDTO(weekInventoryPrecisionRows);
				weekInventoryTable.setRows(weekInventoryPrecisionRows);
				weekInventoryTables.add(weekInventoryTable);
			}
		}
		
		List<ProductInventoryPrecisionRowDTO> dateInventoryPrecisionRows = new LinkedList<ProductInventoryPrecisionRowDTO>(dateInventoryRows.values());
		if(dateInventoryPrecisionRows != null && !dateInventoryPrecisionRows.isEmpty()){
			this.sortProductInventoryPrecisionRowDTO(dateInventoryPrecisionRows);
			dateInventoryTable.setRows(dateInventoryPrecisionRows);
			inventoryPrecisionDTO.setDateInventory(dateInventoryTable);
		}
		
		if(!weekInventoryTables.isEmpty()){
			inventoryPrecisionDTO.setTimeBucketInventory(weekInventoryTables);
		}
		double totalPrecision = totalProductCount == 0 ? 0 : NumberUtil.div(totalGoodProductCount, totalProductCount, 2);//综合准确率
		inventoryPrecisionDTO.setTotalPrecision(totalPrecision);
		if(totalProductCount == 0){
			inventoryPrecisionDTO.setSendStatus(false);
		}
		return inventoryPrecisionDTO;
	}
	
	/**
	 * 按库存准确率排序
	 * 	1.先显示小于90%的菜品，按照准确率由小大的排列
	 * 	2.再显示大于120%的菜品，按照准确率由小大的排列
	 * 	3.最后显示90-120%的菜品，按照准确率由小大的排列
	 * @param dateInventoryPrecisionRows
	 * @return
	 */
	private void sortProductInventoryPrecisionRowDTO(List<ProductInventoryPrecisionRowDTO> dateInventoryPrecisionRows){
		Collections.sort(dateInventoryPrecisionRows, new Comparator<ProductInventoryPrecisionRowDTO>() {
			@Override
			public int compare(ProductInventoryPrecisionRowDTO o1, ProductInventoryPrecisionRowDTO o2) {
				if(o1.getInventoryPrecision() > o2.getInventoryPrecision()){
					return 1;
				}
				if(o1.getInventoryPrecision() < o2.getInventoryPrecision()){
					return -1;
				}
				return 0;
			}
		});
		int fromIndex = -1;
		int toIndex = -1;
		boolean isFromIndex = false;
		boolean isToIndex = false;
		for (int i = 0; i < dateInventoryPrecisionRows.size(); i++) {
			ProductInventoryPrecisionRowDTO productInventoryPrecisionRowDTO = dateInventoryPrecisionRows.get(i);
			double inventoryPrecision = productInventoryPrecisionRowDTO.getInventoryPrecision();
			if(inventoryPrecision >= 0.9 && inventoryPrecision <= 1.2 && !isFromIndex){
				fromIndex = i;
				isFromIndex = true;
			}
			if(inventoryPrecision > 1.2 && !isToIndex){
				toIndex = i;
				isToIndex = true;
			}
		}
		if(toIndex > -1){
			if(fromIndex > -1){
				dateInventoryPrecisionRows.addAll(dateInventoryPrecisionRows.subList(fromIndex, toIndex));
				dateInventoryPrecisionRows.subList(fromIndex, toIndex).clear();
			}
		}
	}
}
