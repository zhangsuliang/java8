package com.huofu.module.i5wei.order.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.delivery.dao.MerchantOrderDeliveryLastDAO;
import com.huofu.module.i5wei.delivery.dao.MerchantUserDeliveryAddressDAO;
import com.huofu.module.i5wei.delivery.dao.StoreDeliverySettingDAO;
import com.huofu.module.i5wei.delivery.entity.*;
import com.huofu.module.i5wei.delivery.service.StoreDeliverySettingService;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.meal.service.StoreMealService;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderDeliveryDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.*;
import com.huofu.module.i5wei.order.facade.StoreOrderFacadeValidate;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteTimeSettingDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteHelpler;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteService;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.dialog.tweet.TweetEventType;
import huofucore.facade.i5wei.delivery.MerchantDeliveryModeEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteOrderQueryParam;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteTimeSettingDTO;
import huofucore.facade.merchant.staff.StaffDTO;
import huofucore.facade.merchant.staff.StaffFacade;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofucore.facade.merchant.wechat.MerchantWechatFacade;
import huofucore.facade.user.info.UserFacade;
import huofucore.facade.user.info.UserWechatFacade;
import huofucore.facade.user.invoice.UserInvoiceDTO;
import huofucore.facade.waimai.exception.TWaimaiException;
import huofucore.facade.waimai.meituan.order.StoreMeituanOrderFacade;
import huofucore.facade.waimai.setting.StoreWaimaiEnabledDTO;
import huofucore.facade.waimai.setting.StoreWaimaiSettingFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.PageResult;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.http.HttpClientFactory;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StoreOrderDeliveryService {

    private static final Log log = LogFactory.getLog(StoreOrderDeliveryService.class);

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private StoreInventoryService storeInventoryService;

    @Autowired
    private StoreMealService storeMealService;

    @Autowired
    private StoreOrderDeliveryDAO storeOrderDeliveryDAO;

    @Autowired
    private StoreOrderOptlogDAO storeOrderOptlogDAO;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @ThriftClient
    private StaffFacade.Iface staffFacadeIface;

    @ThriftClient
    private UserFacade.Iface userFacadeIface;

    @Autowired
    private StoreOrderDAO storeOrderDAO;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @ThriftClient
    private MerchantWechatFacade.Iface merchantWechatFacadeIface;

    @ThriftClient
    private UserWechatFacade.Iface userWechatFacadeIface;

    @ThriftClient
    private StoreFacade.Iface storeFacadeIface;
    
    @ThriftClient
    private StoreWaimaiSettingFacade.Iface storeWaimaiSettingFacade;

	@ThriftClient
	private StoreMeituanOrderFacade.Iface storeMeituanOrderFacade;


    @Autowired
    private StoreDeliverySettingDAO storeDeliverySettingDAO;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    @Autowired
    private MerchantUserDeliveryAddressDAO merchantUserDeliveryAddressDAO;

    @Autowired
    private MerchantOrderDeliveryLastDAO merchantOrderDeliveryLastDAO;

    @Autowired
    private StoreOrderFacadeValidate storeOrderFacadeValidate;

    @Autowired
    private StorePickupSiteTimeSettingDAO storePickupSiteTimeSettingDAO;

    @Autowired
    private StorePickupSiteService storePickupSiteService;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreDeliverySettingService storeDeliverySettingService;

    @Autowired
    private StorePickupSiteHelpler storePickupSiteHelpler;

    public PageResult getMerchantDeliveryOrdersForStaff(int merchantId, long
            deliveryStaffId, MerchantOrderDeliveryQueryParam queryParam, int
                                                                page, int size) throws TException {
        StaffDTO staffDTO = this.staffFacadeIface.getMerchantAdmin(merchantId,
                false);
        List<Long> storeIds;
        if (staffDTO != null && staffDTO.getStaffId() == deliveryStaffId) {
            List<StoreDTO> storeDTOs = this.storeFacadeIface
                    .getStoresByMerchantId(merchantId);
            storeIds = Lists.newArrayList();
            for (StoreDTO storeDTO : storeDTOs) {
                storeIds.add(storeDTO.getStoreId());
            }
        } else {
            storeIds = this.staffFacadeIface.getStoreIdsForStaff
                    (merchantId, deliveryStaffId);
        }
        List<PageResult> list = Lists.newArrayList();
        for (Long storeId : storeIds) {
            StoreOrderDeliveryQueryParam storeOrderDeliveryQueryParam = new
                    StoreOrderDeliveryQueryParam();
            storeOrderDeliveryQueryParam.setDeliveryStaffId(deliveryStaffId);
            storeOrderDeliveryQueryParam.setDeliveryStatus(queryParam
                    .getDeliveryStatus());
            storeOrderDeliveryQueryParam.setMobile(queryParam.getMobile());
            PageResult pageResult = this.getStoreDeliveryOrders(merchantId,
                    storeId, storeOrderDeliveryQueryParam, page, size, false);
            list.add(pageResult);
        }
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setSize(size);
        List<StoreOrder> storeOrders = Lists.newArrayList();
        int allCount = 0;
        for (PageResult result : list) {
            allCount = allCount + result.getTotal();
            storeOrders.addAll(result.getList());
        }
        Collections.sort(storeOrders, (o1, o2) -> {
            if (o1.getStoreOrderDelivery() != null && o2
                    .getStoreOrderDelivery() != null) {
                if (o1.getStoreOrderDelivery().getDeliveryAssignTime() > o2
                        .getStoreOrderDelivery().getDeliveryAssignTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 0;
        });
        pageResult.setTotal(allCount);
        pageResult.build();
        List<StoreOrder> subStoreOrders = DataUtil.subList(storeOrders, 0, size);
        pageResult.setList(subStoreOrders);
        return pageResult;
    }

    public PageResult getStoreDeliveryOrders(int merchantId, long storeId, StoreOrderDeliveryQueryParam queryParam, int page, int size, boolean isContainPickupSiteOrder) throws TException {
        boolean enableSlave = true;
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setSize(size);
        pageResult.setTotal(this.storeOrderDeliveryDAO.countForDelivery(merchantId, storeId, queryParam, isContainPickupSiteOrder, enableSlave));
        int begin = PageUtil.getBeginIndex(page, size);
        List<StoreOrder> storeOrders = this.storeOrderDeliveryDAO.getListForDelivery(merchantId, storeId, queryParam, begin, size, isContainPickupSiteOrder, enableSlave);
        storeOrderHelper.setStoreOrderDetail(storeOrders, enableSlave);
        storeOrderHelper.setStoreOrderDelivery(storeOrders, enableSlave);
        pageResult.setList(storeOrders);
        pageResult.build();
        return pageResult;
    }

    public StoreOrderDelivery getStoreOrderDeliveryById(int merchantId, long storeId, String orderId) {
        boolean enableSlave = true;
        return storeOrderDeliveryDAO.getById(merchantId, storeId, orderId, enableSlave);
    }

    private List<StoreOrder> getStoreOrdersInIdsForUpdate(int merchantId, long storeId, List<String> orderIds) {
        List<StoreOrder> list = this.storeOrderDAO.getListInIdsForUpdate(merchantId, storeId, orderIds);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<StoreOrder> makeDeliveryOrderPreparing(int merchantId, long storeId, List<String> orderIds) throws TException {
        boolean enableSlave = false;
        List<StoreOrder> list = this.getStoreOrdersInIdsForUpdate(merchantId, storeId, orderIds);
        Map<String, StoreOrderDelivery> storeOrderDeliveryMap = this.storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, orderIds, enableSlave);
        List<StoreOrder> olist = Lists.newArrayList();
        for (StoreOrder storeOrder : list) {
            boolean bool = this.makeStoreOrderOutWorkin(storeOrder, storeOrderDeliveryMap.get(storeOrder.getOrderId()));
            if (bool) {
                olist.add(storeOrder);
            }
        }
        return olist;
    }

    private boolean makeStoreOrderOutWorkin(StoreOrder storeOrder, StoreOrderDelivery storeOrderDelivery) throws TException {
    	storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        //属于外送、 支付成功 、未退款、尚未交易才可以进行设置
        if (storeOrder.getTakeMode() != StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
            return false;
        }
        if (storeOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
            return false;
        }
        if (storeOrder.isRefund4All()) {
            return false;
        }
        if (storeOrder.getTradeStatus() != StoreOrderTradeStatusEnum.NOT.getValue()) {
            return false;
        }
        //通知后厨出餐，将信息发给后厨界面
        if (storeOrder.getTakeSerialNumber() == 0) {
        	//收银台直接生成订单流水号，进入后台出餐
        	int takeSerialNumber = this.storeOrderService.getTakeSerialNumber(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getRepastDate());
            int clientType = storeOrder.getClientType();
            //信息入库：交易状态=已取号、流水号、取餐模式
            storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.WORKIN.getValue());
            storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.DEDUCTED.getValue());
            storeOrder.setTakeSerialNumber(takeSerialNumber);
            storeOrder.setTakeClientType(ClientTypeEnum.CASHIER.getValue());
            storeOrder.update();
            storeMealService.storeOrderTakeCode(storeOrder, takeSerialNumber);
            //更新库存
            try {
                storeInventoryService.updateInventoryDateByOrder(storeOrder);
            } catch (Throwable e) {
                log.error("#### fail to updateInventoryByOrder ", e);
            }
            //信息入库
            storeOrderDelivery.snapshot();
            storeOrderDelivery.setPrepareBeginTime(System.currentTimeMillis());
            storeOrderDelivery.update();
            //TODO 传入员工ID 
            storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue(), "make storeOrder out workin");
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public OpDeliveryingResult makeDeliveryOrderDelivering(int merchantId, long storeId, List<String> orderIds, long staffId, long staffUserId) throws TException {
        boolean enableSlave = false;
        List<StoreOrder> list = this.getStoreOrdersInIdsForUpdate(merchantId, storeId, orderIds);
        Map<String, StoreOrderDelivery> storeOrderDeliveryMap = this.storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, orderIds, enableSlave);
        for (StoreOrder storeOrder : list) {
            if ((storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue()
                    || storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.SENTED.getValue())
                    && storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.SENTED.getValue());
                storeOrder.update();
                StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryMap.get(storeOrder.getOrderId());
                if (storeOrderDelivery != null) {
                    storeOrderDelivery.setDeliveryStaffId(staffId);
                    storeOrderDelivery.setDeliveryStaffUserId(staffUserId);
                    storeOrderDelivery.setDeliveryStartTime(System.currentTimeMillis());
                    storeOrderDelivery.update();
                    //发送微信消息提醒使用
                    storeOrder.setStoreOrderDelivery(storeOrderDelivery);
                    //传入员工ID
                    storeOrderOptlogDAO.createOptlog(storeOrder, staffId, storeOrder.getClientType(), StoreOrderOptlogTypeEnum.DELIVERY_BEGIN_DELIVERY.getValue(), "");
                }
            }
        }
        int newAdd = list.size();
        StoreOrderDeliveryQueryParam param = new StoreOrderDeliveryQueryParam();
        param.setDeliveryStatus(StoreOrderDeliveryStatusEnum.DELIVERING);
        param.setDeliveryStaffId(staffId);
        int total = this.storeOrderDeliveryDAO.countForDelivery(merchantId,
                storeId, param, false, false);
        OpDeliveryingResult result = new OpDeliveryingResult();
        result.setNewAdd(newAdd);
        result.setTotal(total);
        result.setDeliverStaffUserId(staffUserId);
        result.setStoreOrders(list);
        return result;
    }

    public int countPickupSiteOrderInDelivering(int merchantId, long storeId, long staffId) {
        return this.storeOrderDeliveryDAO.countStorePickupSiteOrderForDelivery(
                merchantId, storeId, staffId, StoreOrderTradeStatusEnum.SENTED.getValue());
    }

    @Transactional(rollbackFor = Exception.class)
    public List<StoreOrder> makeDeliveryOrderDeliveryFinish(int merchantId, long storeId, List<String> orderIds)
		    throws T5weiException, TException {
	    boolean enableSlave = false;
        List<StoreOrder> list = this.getStoreOrdersInIdsForUpdate(merchantId, storeId, orderIds);
        Map<String, StoreOrderDelivery> storeOrderDeliveryMap = this.storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, orderIds, enableSlave);
        for (StoreOrder storeOrder : list) {
        	//edit by Jemon
        	StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryMap.get(storeOrder.getOrderId());
			if (storeOrder.isSended() || storeOrderDelivery.isWaimaiOrder()) {
                this.storeOrderService.updateOrderTradeFinish(storeOrder);
                if (storeOrderDelivery != null) {
                    storeOrderDelivery.snapshot();
                    storeOrderDelivery.setDeliveryFinishTime(System.currentTimeMillis());
                    storeOrderDelivery.update();
                    storeOrder.setStoreOrderDelivery(storeOrderDelivery);
                    //TODO 传入员工ID 
                    storeOrderOptlogDAO.createOptlog(storeOrder, 0, storeOrder.getClientType(), StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue(), "");
                   //发送交互消息
                   i5weiMessageProducer.sendMessageOfStoreOrderEvent(storeOrder, 0, TweetEventType.PAY_ORDER, "订单消费");
                }
            }
        }
        return list;
    }
    
    public String getLast4WaitPrepare(int merchantId, long storeId, StoreDeliverySetting storeDeliverySetting) {
        long minNotifyTime;
        minNotifyTime = StoreDeliverySetting.getMinNotifyTime(storeDeliverySetting);
        if (minNotifyTime == -1) {
            return null;
        }
//        long begin = System.currentTimeMillis();
        String orderId4last4WaitPrepare = storeOrderDeliveryDAO.getLast4WaitPrepare(merchantId, storeId, minNotifyTime, true);
//        long end = System.currentTimeMillis();
//        log.warn("getLast4WaitPrepare time[" + (end - begin) + "]");
        return orderId4last4WaitPrepare;
    }

    public StoreOrderDeliveryInfoDTO getStoreOrderDeliveryInfo(int merchantId, long storeId, StoreDeliverySetting storeDeliverySetting) {
        boolean enableSlave = true;
        long minNotifyTime;
        StoreOrderDeliveryInfoDTO storeOrderDeliveryInfoDTO = new StoreOrderDeliveryInfoDTO();
        minNotifyTime = StoreDeliverySetting.getMinNotifyTime(storeDeliverySetting);
        if (minNotifyTime == -1) {
            return storeOrderDeliveryInfoDTO;
        }
//        long begin1 = System.currentTimeMillis();
        storeOrderDeliveryInfoDTO.setWaitForPrepare(this.storeOrderDAO.countForDeliveryWaitForPrepareForNotify(merchantId, storeId, minNotifyTime, enableSlave));
//        long end1 = System.currentTimeMillis();
//        long begin2 = System.currentTimeMillis();
        storeOrderDeliveryInfoDTO.setPreparing(this.storeOrderDAO.countForDeliveryTradeStatus(merchantId, storeId, StoreOrderTradeStatusEnum.WORKIN, enableSlave));
//        long end2 = System.currentTimeMillis();
//
//        long begin3 = System.currentTimeMillis();
        int pickupSiteOrderCountPrepareFinish = storeOrderDeliveryDAO.countStorePickupSiteOrderForDelivery(merchantId, storeId, 0, StoreOrderDeliveryStatusEnum.PREPARE_FINISH.getValue());
        int thirdPartOrderCountPrepareFinish = this.storeOrderDAO.countForDeliveryTradeStatus(merchantId, storeId, StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH, enableSlave);
        storeOrderDeliveryInfoDTO.setPrepareFinish(pickupSiteOrderCountPrepareFinish + thirdPartOrderCountPrepareFinish);
//        long end3 = System.currentTimeMillis();
//
//        long begin4 = System.currentTimeMillis();
        int thirdPartOrderCountDelivering = this.storeOrderDAO.countForDeliveryTradeStatus(merchantId, storeId, StoreOrderTradeStatusEnum.SENTED, enableSlave);
        int pickupSiteOrderCountDelivering = this.storeOrderDeliveryDAO.countStorePickupSiteOrderForDelivery(merchantId, storeId, 0, StoreOrderDeliveryStatusEnum.DELIVERING.getValue());
        storeOrderDeliveryInfoDTO.setDelivering(thirdPartOrderCountDelivering + pickupSiteOrderCountDelivering);
//        long end4 = System.currentTimeMillis();
//        long time1 = end1 - begin1;
//        long time2 = end2 - begin2;
//        long time3 = end3 - begin3;
//        long time4 = end4 - begin4;
//        log.warn("getStoreOrderDeliveryInfo merchantId[" + merchantId + "] storeId[" + storeId + "] minNotifyTime[" + minNotifyTime + "] time1[" + time1 + "] time2[" + time2 + "] time3[" + time3 + "] time4[" + time4 + "]");
        return storeOrderDeliveryInfoDTO;
    }

    /**
     * 自动备餐
     *
     * @throws T5weiException
     * @throws TException
     */
    public List<StoreOrderDeliveryPreparingResult> makeDeliveryOrderPreparingAuto() throws T5weiException, TException {
        List<StoreOrderDeliveryPreparingResult> deliveryPreparingResults = Lists.newArrayList();
        long currentTime = System.currentTimeMillis();
        long nowDate = DateUtil.getBeginTime(currentTime, null);
        List<StoreDeliverySetting> storeDeliverySettingsAutoPrepareMeals = this.storeDeliverySettingDAO.getStoreDeliverySettingsAutoPrepareMeal(true);
        if (storeDeliverySettingsAutoPrepareMeals != null) {
            for (StoreDeliverySetting storeDeliverySetting : storeDeliverySettingsAutoPrepareMeals) {
                int merchantId = storeDeliverySetting.getMerchantId();
                long storeId = storeDeliverySetting.getStoreId();
                long minNotifyTime = StoreDeliverySetting.getMinNotifyTime(storeDeliverySetting);//自动提醒时间
                int autoPrepareMealTime = StoreDeliverySetting.getAutoPrepareMealTime(storeDeliverySetting);//自动备餐时间

                if (autoPrepareMealTime == -1) {
                    continue;
                }

                StoreTimeBucket storeTimeBucket = null;
                try {
                    storeTimeBucket = storeTimeBucketService.getDeliveryStoreTimeBucketForDate(merchantId, storeId, 0, nowDate);
                } catch (T5weiException e) {//判断是否存在有效的营业时段
                    if (e.getErrorCode() == T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue()) {
                    } else {
                        log.error(e.getMessage());
                    }
                }

                if (storeTimeBucket != null) {//存在有效的营业时间段则判断是否自动备餐 && storeTimeBucket.isDeliverySupported()
                	
                    List<String> storeOrderIds = this.storeOrderDAO.getWaitPrepareOrderIds(merchantId, storeId, true);//获取未备餐的订单
                    if (storeOrderIds == null || storeOrderIds.isEmpty()) {
                        continue;
                    }

                    Map<String, StoreOrderDelivery> storeOrderDeliverys = this.storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, storeOrderIds, true);
                    Set<String> deliveryOrderIdSet = storeOrderDeliverys.keySet();
                    List<String> deliveryOrderIdList = new ArrayList<String>();
                    if (deliveryOrderIdSet != null && !deliveryOrderIdSet.isEmpty()) {
                        for (Iterator<String> iterator = deliveryOrderIdSet.iterator(); iterator.hasNext(); ) {
                            String deliveryOrderId = iterator.next();
                            StoreOrderDelivery storeOrderDelivery = storeOrderDeliverys.get(deliveryOrderId);
                            if (storeOrderDelivery.getDeliveryAssignTime() <= minNotifyTime) {
                                deliveryOrderIdList.add(deliveryOrderId);
                            }
                        }
                    } else {
                        continue;
                    }

                    Map<String, List<StoreOrderOptlog>> storeOrderOptlogsInIds = this.storeOrderOptlogDAO.getStoreOrderOptlogsInIds(merchantId, storeId, deliveryOrderIdList, true);
                    Set<String> storeOrderOptlogsSet = storeOrderOptlogsInIds.keySet();
                    if (storeOrderOptlogsSet != null) {
                        List<String> deliveryOrderIds = new ArrayList<String>();
                        for (Iterator<String> iterator = storeOrderOptlogsSet.iterator(); iterator.hasNext(); ) {
                            String storeOrderOptlogsOrderId = iterator.next();
                            List<StoreOrderOptlog> storeOrderOptlogsList = storeOrderOptlogsInIds.get(storeOrderOptlogsOrderId);
                            for (StoreOrderOptlog storeOrderOptlog : storeOrderOptlogsList) {
                                if (storeOrderOptlog.getOptType() == StoreOrderOptlogTypeEnum.USER_PAY_ORDER.getValue()) {
                                    String deliveryOrderId = storeOrderOptlog.getOrderId();
                                    StoreOrderDelivery storeOrderDelivery = storeOrderDeliverys.get(deliveryOrderId);
                                    long deliveryAssignTime = storeOrderDelivery.getDeliveryAssignTime();//预约送达时间
                                    long payOrderTime = storeOrderOptlog.getCreateTime();//支付完成时间

                                    if (autoPrepareMealTime != -1) {//开启自动备餐
                                        if (payOrderTime > (deliveryAssignTime - (minNotifyTime - currentTime))) {//下单时间处在外送提醒时间内
                                            if (currentTime - payOrderTime >= (2 * 60 * 1000)) {//下单时间两分中后自动备餐
                                                deliveryOrderIds.add(deliveryOrderId);
                                            }
                                        } else {
                                            if ((minNotifyTime - autoPrepareMealTime) >= deliveryAssignTime) {
                                                deliveryOrderIds.add(deliveryOrderId);
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if (!deliveryOrderIds.isEmpty()) {
                            StoreOrderDeliveryPreparingResult storeOrderDeliveryPreparingResult = new StoreOrderDeliveryPreparingResult();
                            storeOrderDeliveryPreparingResult.setMerchantId(merchantId);
                            storeOrderDeliveryPreparingResult.setStoreId(storeId);
                            storeOrderDeliveryPreparingResult.setDeliveryOrderIds(deliveryOrderIds);
                            deliveryPreparingResults.add(storeOrderDeliveryPreparingResult);
                        }
                    }
                }
            }
        }
        return deliveryPreparingResults;
    }

    /**
     * 非距离模式修改订单和外卖订单相关
     *
     * @param storeOrder
     * @param storeOrderDeliveryParam
     * @param userInvoiceDTO
     * @param storeDeliveryBuilding
     * @param userDeliveryAddress
     * @param storeDeliverySetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderForNoDistanceDelivery(StoreOrder storeOrder, StoreOrderDeliveryParam storeOrderDeliveryParam, UserInvoiceDTO userInvoiceDTO, StoreDeliveryBuilding storeDeliveryBuilding,
                                                 UserDeliveryAddress userDeliveryAddress, StoreDeliverySetting storeDeliverySetting) throws T5weiException {
        this.updateStoreOrderDelivery(storeOrder, storeOrderDeliveryParam, userInvoiceDTO, storeDeliverySetting);
        this.buildOrderNoDistanceDelivery(storeOrderDeliveryParam, storeOrder.getOrderId(), storeDeliveryBuilding, userDeliveryAddress, storeDeliverySetting);
    }

    /**
     * 距离模式修改订单和外卖订单相关
     *
     * @param storeOrder
     * @param storeOrderDeliveryParam
     * @param userInvoiceDTO
     * @param storeDeliverySetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderForDistanceDelivery(StoreOrder storeOrder, StoreOrderDeliveryParam storeOrderDeliveryParam, UserInvoiceDTO userInvoiceDTO, StoreDeliverySetting storeDeliverySetting,
                                               MerchantUserDeliveryAddress merchantUserDeliveryAddress) throws T5weiException {
        this.updateStoreOrderDelivery(storeOrder, storeOrderDeliveryParam, userInvoiceDTO, storeDeliverySetting);
        this.buildOrderDistanceDelivery(storeOrderDeliveryParam, storeOrder.getOrderId(), storeDeliverySetting, merchantUserDeliveryAddress);
    }

    /**
     * 修改订单
     *
     * @param storeOrder
     * @param storeOrderDeliveryParam
     * @param userInvoiceDTO
     * @param storeDeliverySetting
     * @throws T5weiException
     */
    private void updateStoreOrderDelivery(StoreOrder storeOrder, StoreOrderDeliveryParam storeOrderDeliveryParam, UserInvoiceDTO userInvoiceDTO, StoreDeliverySetting storeDeliverySetting) throws T5weiException {
        // 外送判断 包括打包费的判断
        if (!storeDeliverySetting.isDeliverySupportedForPrice(storeOrder.getOrderPrice() + storeOrder.getPackageFee())) {
            // 订单支付金额不够下单最低标准
            throw new T5weiException(T5weiErrorCodeType.DELIVERY_ORDER_PRICE_LESS_THAN_SUPPORTED.getValue(), "order price[" + storeOrder.getFavorablePrice() + "] and order packageFee ["
                    + storeOrder.getPackageFee() + "] not supported for delivery setting [" + storeDeliverySetting.getMinOrderDeliveryAmount() + "]");// by_akwei
        }
        storeOrder.snapshot();

        storeOrder.setDeliveryFee(storeDeliverySetting.buildDeliveryFee(storeOrder.getOrderPrice()));
        storeOrder.setTakeMode(StoreOrderTakeModeEnum.SEND_OUT.getValue());
        if (storeOrderDeliveryParam.getInvoiceId() > 0) {
            storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.NEED.getValue());
            storeOrder.setInvoiceDemand(userInvoiceDTO.getTitle());
        } else {
            storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.NOT.getValue());
            storeOrder.setInvoiceDemand("");
        }
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrder.update();
    }

    /**
     * 非距离模式创建外卖订单
     *
     * @param storeOrderDeliveryParam
     * @param orderId
     * @param storeDeliveryBuilding
     * @param userDeliveryAddress
     * @param storeDeliverySetting
     */
    private void buildOrderNoDistanceDelivery(StoreOrderDeliveryParam storeOrderDeliveryParam, String orderId, StoreDeliveryBuilding storeDeliveryBuilding, UserDeliveryAddress userDeliveryAddress, StoreDeliverySetting storeDeliverySetting) {
        boolean enableSlave = false;
        long currentTime = System.currentTimeMillis();
        StoreOrderDelivery storeOrderDelivery = this.storeOrderDeliveryDAO.getById(storeOrderDeliveryParam.getMerchantId(), storeOrderDeliveryParam.getStoreId(), orderId, enableSlave);
        boolean update = false;
        if (storeOrderDelivery == null) {
            storeOrderDelivery = new StoreOrderDelivery();
        } else {
            update = true;
        }
        BeanUtil.copy(storeOrderDeliveryParam, storeOrderDelivery);
        storeOrderDelivery.setOrderId(orderId);
        storeOrderDelivery.setUserAddress(userDeliveryAddress.getAddress());
        storeOrderDelivery.setDeliveryBuildingId(storeDeliveryBuilding.getBuildingId());
        storeOrderDelivery.setDeliveryBuildingName(storeDeliveryBuilding.getName());
        storeOrderDelivery.setDeliveryBuildingAddress(storeDeliveryBuilding.getAddress());
        storeOrderDelivery.setContactName(userDeliveryAddress.getContactName());
        storeOrderDelivery.setContactPhone(userDeliveryAddress.getContactPhone());
        if (storeOrderDeliveryParam.getDeliveryAssignTime() <= 0) {
            // 下单支付时间+外送提前时间
            storeOrderDelivery.setDeliveryAssignTime(currentTime + storeDeliverySetting.getAheadTime());
        }
        if (update) {
            storeOrderDelivery.update();
        } else {
            storeOrderDelivery.create();
        }

        //记录用户在某个店铺中最近一次的外卖下单的时间
        MerchantOrderDeliveryLast merchantOrderDeliveryLast = new MerchantOrderDeliveryLast();
        merchantOrderDeliveryLast.setMerchantId(storeOrderDeliveryParam.getMerchantId());
        merchantOrderDeliveryLast.setStoreId(storeOrderDeliveryParam.getStoreId());
        merchantOrderDeliveryLast.setUserId(userDeliveryAddress.getUserId());
        merchantOrderDeliveryLast.setDeliveryMode(MerchantDeliveryModeEnum.NO_DISTANCE.getValue());
        merchantOrderDeliveryLast.setLastDeliveryTime(currentTime);
        this.merchantOrderDeliveryLastDAO.replace(merchantOrderDeliveryLast);
    }

    /**
     * 距离模式创建外卖订单
     *
     * @param storeOrderDeliveryParam
     * @param storeDeliverySetting
     */
    private void buildOrderDistanceDelivery(StoreOrderDeliveryParam storeOrderDeliveryParam, String orderId, StoreDeliverySetting storeDeliverySetting, MerchantUserDeliveryAddress merchantUserDeliveryAddress) {
        boolean enableSlave = false;
        long currentTime = System.currentTimeMillis();
        StoreOrderDelivery storeOrderDelivery = this.storeOrderDeliveryDAO.getById(storeOrderDeliveryParam.getMerchantId(), storeOrderDeliveryParam.getStoreId(), orderId, enableSlave);
        boolean update = false;
        if (storeOrderDelivery == null) {
            storeOrderDelivery = new StoreOrderDelivery();
        } else {
            update = true;
        }
        BeanUtil.copy(storeOrderDeliveryParam, storeOrderDelivery);

        storeOrderDelivery.setOrderId(orderId);
        storeOrderDelivery.setUserAddress(merchantUserDeliveryAddress.getUserAddress());
//		storeOrderDelivery.setDeliveryBuildingId(storeDeliveryBuilding.getBuildingId());
        storeOrderDelivery.setDeliveryBuildingName(merchantUserDeliveryAddress.getBuildingName());
        storeOrderDelivery.setDeliveryBuildingAddress(merchantUserDeliveryAddress.getBuildingAddress());
        storeOrderDelivery.setContactName(merchantUserDeliveryAddress.getContactName());
        storeOrderDelivery.setContactPhone(merchantUserDeliveryAddress.getContactPhone());
        if (storeOrderDeliveryParam.getDeliveryAssignTime() <= 0) {
            // 下单支付时间+外送提前时间
            storeOrderDelivery.setDeliveryAssignTime(currentTime + storeDeliverySetting.getAheadTime());
        }
        if (update) {
            storeOrderDelivery.update();
        } else {
            storeOrderDelivery.create();
        }

        //记录用户在某个店铺中最近一次的外卖下单的时间
        MerchantOrderDeliveryLast merchantOrderDeliveryLast = new MerchantOrderDeliveryLast();
        merchantOrderDeliveryLast.setMerchantId(storeOrderDeliveryParam.getMerchantId());
        merchantOrderDeliveryLast.setStoreId(storeOrderDeliveryParam.getStoreId());
        merchantOrderDeliveryLast.setUserId(merchantUserDeliveryAddress.getUserId());
        merchantOrderDeliveryLast.setDeliveryMode(MerchantDeliveryModeEnum.DISTINCE.getValue());
        merchantOrderDeliveryLast.setLastDeliveryTime(currentTime);
        merchantOrderDeliveryLast.setUserAddressLongitude(merchantUserDeliveryAddress.getUserAddressLongitude());
        merchantOrderDeliveryLast.setUserAddressLatitude(merchantUserDeliveryAddress.getUserAddressLatitude());
        this.merchantOrderDeliveryLastDAO.replace(merchantOrderDeliveryLast);
    }

    /**
     * 获取该商户该门店下，分配给特定配送员的所有自提点信息
     * @param merchantId
     * @param storeId
     * @param deliveryStaffId
     * @return
     * @throws TException
     */
    public List<StorePickupSiteDeliveryDTO> getPickupSiteDeliveryOrdersForDelivery(int merchantId, long storeId, long deliveryStaffId, int tradeStatus) throws TException {
        List<StorePickupSiteDeliveryDTO> pickupSiteDeliveryAllStore = Lists.newArrayList();
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStaffId(deliveryStaffId);
        pickupSiteOrderQueryParam.setDeliveryStatus(tradeStatus);
        pickupSiteOrderQueryParam.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(true);
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
        if(storeId > 0) {
            pickupSiteOrderQueryParam.setStoreId(storeId);
            List<StorePickupSiteDeliveryDTO> pickupSiteDeliveryOneStore = this.storeOrderDeliveryDAO.getPickupSiteDeliveryOrdersforDelivery(pickupSiteOrderQueryParam);
            if (CollectionUtils.isNotEmpty(pickupSiteDeliveryOneStore)) {
                pickupSiteDeliveryAllStore.addAll(pickupSiteDeliveryOneStore);
            }
        } else {
            StaffDTO staffDTO = this.staffFacadeIface.getMerchantAdmin(merchantId,false);
            List<Long> storeIds;
            if (staffDTO != null && staffDTO.getStaffId() == deliveryStaffId) {
                List<StoreDTO> storeDTOs = this.storeFacadeIface.getStoresByMerchantId(merchantId);
                storeIds = Lists.newArrayList();
                for (StoreDTO storeDTO : storeDTOs) {
                    storeIds.add(storeDTO.getStoreId());
                }
            } else {
                storeIds = this.staffFacadeIface.getStoreIdsForStaff(merchantId, deliveryStaffId);
            }
            if (CollectionUtils.isNotEmpty(storeIds)) {
                for (Long storeIdQueried : storeIds) {
                    pickupSiteOrderQueryParam.setStoreId(storeIdQueried);
                    List<StorePickupSiteDeliveryDTO> pickupSiteDeliveryOneStore = this.storeOrderDeliveryDAO.getPickupSiteDeliveryOrdersforDelivery(pickupSiteOrderQueryParam);
                    if (CollectionUtils.isNotEmpty(pickupSiteDeliveryOneStore)) {
                        pickupSiteDeliveryAllStore.addAll(pickupSiteDeliveryOneStore);
                    }
                }
            }
        }
        return pickupSiteDeliveryAllStore;
    }

    public StorePickupSiteTimeSettingDTO verifyChangeTakeModel(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = this.storeOrderDAO.getById(merchantId, storeId, orderId, false, false);
        if (storeOrder != null && storeOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
            if(storeOrder.getTakeSerialNumber() == 0) {
                StoreOrderDelivery storeOrderDelivery = this.storeOrderDAO.getDeliveryById(merchantId, storeId, orderId, false, false);
                List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings = this.storePickupSiteTimeSettingDAO.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(
                        merchantId, storeId, storeOrderDelivery.getStorePickupSiteId(), storeOrder.getTimeBucketId());
                if (CollectionUtils.isNotEmpty(storePickupSiteTimeSettings)) {
                    return BeanUtil.copy(storePickupSiteTimeSettings.get(0), StorePickupSiteTimeSettingDTO.class);
                }
            } else {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), "store has take mode");
            }
        }
        if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.DINE_IN.getValue() ||
                storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TAKE_MODE_CHANGED.getValue(), "store take mode error");
        }

        return new StorePickupSiteTimeSettingDTO();
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean changeTakeModel(int merchantId, long storeId, String orderId) throws TException {
        boolean isChanged = false;
        StorePickupSiteTimeSettingDTO storePickupSiteTimeSettingDTO = verifyChangeTakeModel(merchantId, storeId, orderId);
        if(storePickupSiteTimeSettingDTO != null) {
            StoreOrder storeOrder = this.storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
            StoreOrderDelivery storeOrderDelivery = this.storeOrderDAO.getDeliveryById(merchantId, storeId, orderId, true, false);
            storeOrder.setTakeMode(StoreOrderTakeModeEnum.TAKE_OUT.getValue());
            storeOrder.setWaimaiType(WaimaiTypeEnum.UNKNOWN.getValue());
            storeOrder.setTimingTakeTime(0L);
            storeOrder.update();
            storeOrderDelivery.delete();
            isChanged = true;
        }
        return isChanged;
    }

    public List<StoreOrderDeliveryCountDTO> getStoreOrderDeliveryCount(int merchantId, long storeId, int tradeStatus) {
        List<StoreOrderDeliveryCountDTO> storeOrderDeliveryForThridPartCount = this.storeOrderDeliveryDAO.getStoreOrderDeliveryCountForThirdPart(merchantId, storeId, tradeStatus);
        storeOrderDeliveryForThridPartCount.add(this.storeOrderDeliveryDAO.getPickupSiteCountInfoByStoreId(merchantId, storeId, tradeStatus));
        return storeOrderDeliveryForThridPartCount;
    }

    public List<StoreOrderBaseDTO> getDeliveryOrderList(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        List<StoreOrder> storeOrders = this.storeOrderDeliveryDAO.getDeliveryOrderList(pickupSiteOrderQueryParam, true);
        if(CollectionUtils.isNotEmpty(storeOrders)) {
            List<StoreOrderBaseDTO> storeOrderBaseDTOs = new ArrayList<>(storeOrders.size());
            StoreOrderBaseDTO storeOrderBaseDTO;
            StoreOrderDeliveryDTO storeOrderDeliveryDTO;
            for(StoreOrder storeOrder : storeOrders) {
                storeOrderBaseDTO = new StoreOrderBaseDTO();
                storeOrderDeliveryDTO = new StoreOrderDeliveryDTO();
                BeanUtils.copyProperties(storeOrder, storeOrderBaseDTO);
                BeanUtils.copyProperties(storeOrder.getStoreOrderDelivery(), storeOrderDeliveryDTO);
                storeOrderBaseDTO.setPlaceOrderUserName(storeOrder.getStoreOrderDelivery().getContactName());
                storeOrderBaseDTO.setStoreOrderDeliveryDTO(storeOrderDeliveryDTO);
                storeOrderHelper.setStoreOrderDetail(storeOrder, true);
                List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
                if(CollectionUtils.isNotEmpty(storeOrderItems)) {
                    List<StoreOrderItemDTO> storeOrderItemDTOs = new ArrayList<>(storeOrderItems.size());
                    for(StoreOrderItem storeOrderItem : storeOrderItems) {
                        StoreOrderItemDTO orderItemDTO = BeanUtil.copy(storeOrderItem, StoreOrderItemDTO.class);
                        if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                            orderItemDTO.setPackedAmount(orderItemDTO.getAmount());
                        }
                        List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
                        List<StoreOrderSubItemDTO> orderSubItemDTOs = new ArrayList<StoreOrderSubItemDTO>();
                        if (storeOrderSubitems != null && !storeOrderSubitems.isEmpty()) {
                            orderSubItemDTOs = BeanUtil.copyList(storeOrderSubitems, StoreOrderSubItemDTO.class);
                        }
                        orderItemDTO.setStoreOrderSubItemSize(orderSubItemDTOs.size());
                        if(orderSubItemDTOs.size() == 1){
                            orderItemDTO.setSameSubItemName(storeOrderItem.getChargeItemName().equals(orderSubItemDTOs.get(0).getProductName()));
                        }
                        orderItemDTO.setStoreOrderSubItemDTOs(orderSubItemDTOs);
                        storeOrderItemDTOs.add(orderItemDTO);
                    }
                    storeOrderBaseDTO.setStoreOrderItemDTOs(storeOrderItemDTOs);
                }

                storeOrderBaseDTOs.add(storeOrderBaseDTO);
            }
            return storeOrderBaseDTOs;
        }
        return Collections.emptyList();
    }

    /**
     * 获取店铺支持的外卖类型
     * @param merchantId
     * @param storeId
     */
    public List<StoreOrderDeliveryCountDTO> getStoreSupportWaimaiType(int merchantId, long storeId) {
        Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, true);
        StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting4Read(merchantId, storeId);
        StoreOrderDeliveryCountDTO storeOrderDeliveryCountDTO;
        List<StoreOrderDeliveryCountDTO> storeOrderDeliveryCountDTOs = Lists.newArrayList();
        //判断店铺是否支持公众号外卖
        if (storeDeliverySetting.isDeliverySupported()) {
            storeOrderDeliveryCountDTO = new StoreOrderDeliveryCountDTO();
            storeOrderDeliveryCountDTO.setCount(0);
            storeOrderDeliveryCountDTO.setWaimaiType(0);
            storeOrderDeliveryCountDTOs.add(storeOrderDeliveryCountDTO);
        }


        if (storeDeliverySetting.isTakeOutEnabled()) {
        	
			try {
				StoreWaimaiEnabledDTO storeWaimaiEnabledDTO = this.storeWaimaiSettingFacade.getWaimaiStoreEnable(merchantId, storeId);
				 //判断店铺是否支持美团外卖
	            if (storeWaimaiEnabledDTO.isMeituanEnabled()) {
	                storeOrderDeliveryCountDTO = new StoreOrderDeliveryCountDTO();
	                storeOrderDeliveryCountDTO.setCount(0);
	                storeOrderDeliveryCountDTO.setWaimaiType(WaimaiTypeEnum.MEITUAN.getValue());
	                storeOrderDeliveryCountDTOs.add(storeOrderDeliveryCountDTO);
	            }

	            //判断店铺是否支持饿了么外卖
	            if (storeWaimaiEnabledDTO.isElemeEnabled()) {
	                storeOrderDeliveryCountDTO = new StoreOrderDeliveryCountDTO();
	                storeOrderDeliveryCountDTO.setCount(0);
	                storeOrderDeliveryCountDTO.setWaimaiType(WaimaiTypeEnum.ELEME.getValue());
	                storeOrderDeliveryCountDTOs.add(storeOrderDeliveryCountDTO);
	            }

	            //判断店铺是否支持百度外卖
	            if (storeWaimaiEnabledDTO.isBaiduEnabled()) {
	                storeOrderDeliveryCountDTO = new StoreOrderDeliveryCountDTO();
	                storeOrderDeliveryCountDTO.setCount(0);
	                storeOrderDeliveryCountDTO.setWaimaiType(WaimaiTypeEnum.BAIDU.getValue());
	                storeOrderDeliveryCountDTOs.add(storeOrderDeliveryCountDTO);
	            }
			}catch (TException e) {
				log.info("get getWaimaiStoreEnable is error ");
			}
           
        }

        //判断店铺是否支持自提点外卖
        if (store5weiSetting.isEnableUserTake() && store5weiSetting.isEnablePickupSite()) {
            storeOrderDeliveryCountDTO = new StoreOrderDeliveryCountDTO();
            storeOrderDeliveryCountDTO.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
            storeOrderDeliveryCountDTO.setCount(0);
            storeOrderDeliveryCountDTOs.add(storeOrderDeliveryCountDTO);
        }

        return storeOrderDeliveryCountDTOs;
    }

    public StoreSendOutOrderCountDTO getStoreSendOutOrderCount(int merchantId, long storeId, int waimaiType, int tradeStatus) throws TException {
        //获取店铺支持的外卖类型信息
        List<StoreOrderDeliveryCountDTO> storeOrderDeliveryCountDTOs = getStoreSupportWaimaiType(merchantId, storeId);
        StoreSendOutOrderCountDTO storeSendOutOrderCountDTO = new StoreSendOutOrderCountDTO();
        if (CollectionUtils.isEmpty(storeOrderDeliveryCountDTOs)) {
            storeSendOutOrderCountDTO.setCurrentWaimaiType(0);
            storeSendOutOrderCountDTO.setWaimaiTypeCount(0);
            return storeSendOutOrderCountDTO;
        }

        storeSendOutOrderCountDTO.setWaimaiTypeCount(storeOrderDeliveryCountDTOs.size());
        storeSendOutOrderCountDTO.setStoreOrderDeliveryCountDTOs(storeOrderDeliveryCountDTOs);
        //外卖类型为0时设置外卖类型为最小的外卖类型，第一次使用，默认设置为最小的外卖类型
        if (waimaiType == WaimaiTypeEnum.UNKNOWN.getValue()) {
            storeSendOutOrderCountDTO.setCurrentWaimaiType(storeOrderDeliveryCountDTOs.get(0).getWaimaiType());
        } else {
            storeSendOutOrderCountDTO.setCurrentWaimaiType(waimaiType);
        }

        //获取外卖类型订单统计信息
        List<StoreOrderDeliveryCountDTO> storeOrderDeliveryCountDTOLists = this.getStoreOrderDeliveryCount(merchantId, storeId, tradeStatus);
        if (CollectionUtils.isEmpty(storeOrderDeliveryCountDTOLists)) {
            return storeSendOutOrderCountDTO;
        }

        for(StoreOrderDeliveryCountDTO storeOrderDeliveryCountDTO : storeOrderDeliveryCountDTOs) {
            for(StoreOrderDeliveryCountDTO storeOrderDeliveryCount : storeOrderDeliveryCountDTOLists) {
                if(storeOrderDeliveryCountDTO.getWaimaiType() == storeOrderDeliveryCount.getWaimaiType()) {
                    storeOrderDeliveryCountDTO.setCount(storeOrderDeliveryCount.getCount());
                    break;
                }
            }
        }

        //获取默认的外卖类型的所有订单
        if (storeSendOutOrderCountDTO.getCurrentWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
            List<StorePickupSiteDeliveryDTO> pickupSiteDeliveryDTOs = this.getPickupSiteDeliveryOrdersForDelivery(merchantId, storeId, 0, tradeStatus);
            if (CollectionUtils.isNotEmpty(pickupSiteDeliveryDTOs)) {
                //组装自提点基本信息
                storePickupSiteHelpler.addPickupSiteInformation(pickupSiteDeliveryDTOs, merchantId);
                storePickupSiteHelpler.addPickupSiteTimeBucketInfo(pickupSiteDeliveryDTOs, merchantId);
                storeSendOutOrderCountDTO.setStorePickupSiteDeliveryDTOs(pickupSiteDeliveryDTOs);
            }
        } else {
            StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
            pickupSiteOrderQueryParam.setMerchantId(merchantId);
            pickupSiteOrderQueryParam.setStoreId(storeId);
            pickupSiteOrderQueryParam.setWaimaiType(waimaiType);
            pickupSiteOrderQueryParam.setDeliveryStatus(tradeStatus);
            pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
            pickupSiteOrderQueryParam.setContainPickupSiteInfo(false);
            storeSendOutOrderCountDTO.setStoreOrderBaseDTOs(this.getDeliveryOrderList(pickupSiteOrderQueryParam));
        }

        return storeSendOutOrderCountDTO;
    }

    public List<String> getOrderIdsByPickupSiteIds(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        return this.storeOrderDeliveryDAO.getOrderIdsByPickupSiteIds(pickupSiteOrderQueryParam);
    }
}
