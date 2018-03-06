package com.huofu.module.i5wei.heartbeat.facade;

import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.delivery.service.StoreDeliverySettingService;
import com.huofu.module.i5wei.heartbeat.entity.Store5weiHeartbeat;
import com.huofu.module.i5wei.heartbeat.service.StoreAppcopyRefreshService;
import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.order.service.StoreOrderDeliveryService;
import huofucore.facade.i5wei.heartbeat.Store5weiHeartbeatFacade;
import huofucore.facade.i5wei.heartbeat.Store5weiHeartbeatInfoDTO;
import huofucore.facade.i5wei.order.StoreOrderDeliveryInfoDTO;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ThriftServlet(name = "store5weiHeartbeatFacadeServlet", serviceClass = Store5weiHeartbeatFacade.class)
public class Store5weiHeartbeatFacadeImpl implements Store5weiHeartbeatFacade.Iface {

    @Autowired
    private StoreHeartbeatService storeHeartbeatService;

    @Autowired
    private StoreOrderDeliveryService storeOrderDeliveryService;

    @Autowired
    private StoreAppcopyRefreshService storeAppcopyRefreshService;

    @Autowired
    private StoreDeliverySettingService storeDeliverySettingService;

    @Override
    public Store5weiHeartbeatInfoDTO getStore5weiHeartbeatInfo(int merchantId, long storeId) throws TException {
        Store5weiHeartbeat store5weiHeartbeat = storeHeartbeatService.getStore5weiHeartbeat(merchantId, storeId);
        StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting4Read(merchantId, storeId);
        StoreOrderDeliveryInfoDTO storeOrderDeliveryInfoDTO = this.storeOrderDeliveryService.getStoreOrderDeliveryInfo(merchantId, storeId, storeDeliverySetting);
        Store5weiHeartbeatInfoDTO store5weiHeartbeatInfoDTO = new Store5weiHeartbeatInfoDTO();
        BeanUtil.copy(store5weiHeartbeat, store5weiHeartbeatInfoDTO);
        store5weiHeartbeatInfoDTO.setDelivering(storeOrderDeliveryInfoDTO.getDelivering());
        store5weiHeartbeatInfoDTO.setDeliveryPrepareFinish(storeOrderDeliveryInfoDTO.getPrepareFinish());
        store5weiHeartbeatInfoDTO.setDeliveryPreparing(storeOrderDeliveryInfoDTO.getPreparing());
        store5weiHeartbeatInfoDTO.setDeliveryWaitForPrepare(storeOrderDeliveryInfoDTO.getWaitForPrepare());
        store5weiHeartbeatInfoDTO.setLastDeliveryOrderId(storeOrderDeliveryService.getLast4WaitPrepare(merchantId, storeId, storeDeliverySetting));
        return store5weiHeartbeatInfoDTO;
    }

    @Override
    public boolean refreshSignInAppcopy(int merchantId, long storeId, long appcopyId, boolean enable) throws TException {
        return storeAppcopyRefreshService.refreshSignInAppcopy(merchantId, storeId, appcopyId, enable);
    }

    @Override
    public Map<Long, Long> getSignInAppcopys(int merchantId, long storeId) throws TException {
        return storeAppcopyRefreshService.getSignInAppcopys(merchantId, storeId);
    }

}
