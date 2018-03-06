package com.huofu.module.i5wei.order.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderDeliveryService;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.pickupsite.facade.StorePickupSiteFacadeValidator;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteHelpler;
import com.huofu.module.i5wei.wechat.WechatNotifyService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteOrderQueryParam;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteTimeSettingDTO;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.PageResult;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@ThriftServlet(name = "merchantDeliveryOrderFacadeServlet", serviceClass =
        MerchantDeliveryOrderFacade.class)
public class MerchantDeliveryOrderFacadeImpl implements
        MerchantDeliveryOrderFacade.Iface {

    @Autowired
    private StoreOrderDeliveryService storeOrderDeliveryService;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private WechatNotifyService wechatNotifyService;

    @Autowired
    private StorePickupSiteFacadeValidator storePickupSiteFacadeValidator;

    @Autowired
    private StorePickupSiteHelpler storePickupSiteHelpler;
    
    @Override
    public StoreOrderPageDTO getDeliveryOrdersForStaff(int merchantId, long deliverStaffId, MerchantOrderDeliveryQueryParam queryParam, int page, int size) throws TException {
        PageResult pageResult = this.storeOrderDeliveryService
                .getMerchantDeliveryOrdersForStaff(merchantId, deliverStaffId, queryParam, page, size);
        StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
        storeOrderPageDTO.setSize(size);
        storeOrderPageDTO.setPageNo(page);
        storeOrderPageDTO.setTotal(pageResult.getTotal());
        storeOrderPageDTO.setPageNum(pageResult.getTotalPage());
        List<StoreOrder> storeOrders = pageResult.getList();
        List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(storeOrders);
        storeOrderPageDTO.setDataList(storeOrderDTOs);
        return storeOrderPageDTO;
    }

    /**
     * 根据送餐员Id查询分配的外卖订单，自提点订单
     * @param merchantId
     * @param storeId
     * @param staffId
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public StoreMultiTypeDeliveryDTO getStaffDeliveryList(int merchantId, long storeId, long staffId) throws T5weiException, TException {
        //获取外卖订单详细信息
        MerchantOrderDeliveryQueryParam queryParam = new MerchantOrderDeliveryQueryParam();
        queryParam.setDeliveryStatus(StoreOrderDeliveryStatusEnum.DELIVERING);
        PageResult pageResult = this.storeOrderDeliveryService.getMerchantDeliveryOrdersForStaff(merchantId, staffId, queryParam, 1, 1000);
        List<StoreOrder> storeOrders = pageResult.getList();

        //获取自提点订单统计信息
        List<StorePickupSiteDeliveryDTO> pickupSiteDeliverys = this.storeOrderDeliveryService.getPickupSiteDeliveryOrdersForDelivery(merchantId, storeId, staffId, StoreOrderDeliveryStatusEnum.DELIVERING.getValue());
        if (CollectionUtils.isNotEmpty(pickupSiteDeliverys)) {
            //装载自提点信息
            storePickupSiteHelpler.addPickupSiteInformation(pickupSiteDeliverys, merchantId);
            //装载营业时段信息
            storePickupSiteHelpler.addPickupSiteTimeBucketInfo(pickupSiteDeliverys, merchantId);
            Collections.sort(pickupSiteDeliverys, (o1, o2) -> {
                long time1 = o1.getStorePickupSiteInfo().getStorePickupSiteTimeSetting().getOrderCutOffTime();
                long time2 = o2.getStorePickupSiteInfo().getStorePickupSiteTimeSetting().getOrderCutOffTime();
                if (time1 > time2)
                    return 1;
                else if (time1 < time2)
                    return -1;
                else
                    return 0;
            });
        }
        StoreMultiTypeDeliveryDTO multiTypeDelivery = new StoreMultiTypeDeliveryDTO();
        multiTypeDelivery.setStoreOrders(storeOrderHelper.getStoreOrderDTOByEntity(storeOrders));
        multiTypeDelivery.setStorePickupSiteDeliverys(pickupSiteDeliverys);
        multiTypeDelivery.setDeliveryTotalCount(storeOrders.size() + pickupSiteDeliverys.size());
        return multiTypeDelivery;
    }

    /**
     * 根据自提点Id获取该自提点下所有配送中的订单
     * @param merchantId
     * @param storeId
     * @param storePickupSiteId
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public List<StoreOrderBaseDTO> getPickupsiteDeliveryList(int merchantId, long storeId, long storePickupSiteId, int tradeStatus) throws T5weiException, TException {
        //校验店铺和商户参数
        storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
        //组装查询参数
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStoreId(storeId);
        pickupSiteOrderQueryParam.setPickupSiteIds(Lists.newArrayList(storePickupSiteId));
        pickupSiteOrderQueryParam.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(true);
        if (tradeStatus == 0) {
            pickupSiteOrderQueryParam.setDeliveryStatus(StoreOrderDeliveryStatusEnum.DELIVERING.getValue());
        } else {
            pickupSiteOrderQueryParam.setDeliveryStatus(tradeStatus);
        }

        return this.storeOrderDeliveryService.getDeliveryOrderList(pickupSiteOrderQueryParam);
    }

    /**
     * 配送完成，更改订单状态，同时下发微信提醒
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @return
     * @throws TException
     */
    @Override
    public boolean delivered(int merchantId, long storeId, List<String> orderIds) throws TException {
        storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
        List<StoreOrder> storeOrders = this.storeOrderDeliveryService.makeDeliveryOrderDeliveryFinish(merchantId, storeId, orderIds);
        if (CollectionUtils.isNotEmpty(storeOrders)) {
            this.wechatNotifyService.notifyWechatForPickupSiteOrders(merchantId, storeId, storeOrders);
        }
        return true;
    }

    /**
     * 验证当前自提点订单是否可以更改取餐状态
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public StorePickupSiteTimeSettingDTO verifyChangeTakeModel(int merchantId, long storeId, String orderId) throws TException {
        storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
        return this.storeOrderDeliveryService.verifyChangeTakeModel(merchantId, storeId, orderId);
    }

    /**
     * 更改订单取餐状态，删除配送记录，同时修改takeMode字段为外送订单
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public List<StoreOrderDTO> changeTakeModel(int merchantId, long storeId, String orderId) throws TException {
        storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
        boolean isChanged = this.storeOrderDeliveryService.changeTakeModel(merchantId, storeId, orderId);
        if (isChanged) {
            StoreOrder storeOrder = storeOrderService.getStoreOrderDetailById(merchantId, storeId, orderId);
            long timeBucketId = storeOrder.getTimeBucketId();
            StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, true);
            StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
            storeOrderDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
            return Lists.newArrayList(storeOrderDTO);
        }
        return null;
    }

	/**
	 * 获取自提点订单信息统计
	 *
	 * @param merchantId
	 * @param storeId
	 */
	@Override
	public List<StorePickupSiteDeliveryDTO> getStorePickupSiteOrderStat(int merchantId, long storeId) throws TException {
        return this.storeOrderDeliveryService.getPickupSiteDeliveryOrdersForDelivery(merchantId, storeId, 0, StoreOrderTradeStatusEnum.SENTED.getValue());
    }

    /**
     * 获取各外卖类型订单统计信息，及对应的外卖订单
     * @param merchantId
     * @param storeId
     * @param waimaiType
     * @param tradeStatus
     * @return
     * @throws TException
     */
    @Override
    public StoreSendOutOrderCountDTO getStoreSendOutOrderCount(int merchantId, long storeId, int waimaiType, int tradeStatus) throws TException {
        return this.storeOrderDeliveryService.getStoreSendOutOrderCount(merchantId, storeId, waimaiType, tradeStatus);
    }
}
