package com.huofu.module.i5wei.setting.facade;

import com.huofu.module.i5wei.setting.dao.StoreTableSettingDAO;
import com.huofu.module.i5wei.table.dao.StoreTableDAO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.store5weisetting.StoreRefundReasonDTO;
import huofucore.facade.i5wei.store5weisetting.StoreRefundReasonSaveParam;
import huofucore.facade.i5wei.store5weisetting.StoreTableSettingDTO;
import huofucore.facade.i5wei.store5weisetting.StoreTableSettingFacade;
import huofucore.facade.i5wei.store5weisetting.StoreTableSettingSaveParam;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageParam;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.huofu.module.i5wei.base.SnsPublish;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.eventtype.EventType;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.entity.StoreTagMessage;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.setting.service.StoreTagMessageService;

/**
 * Created by jiajin.nervous on 16/4/27.
 */
@Component
@ThriftServlet(name = "storeTableSettingFacadeServlet", serviceClass = StoreTableSettingFacade.class)
public class StoreTableSettingFacadeImpl implements StoreTableSettingFacade.Iface{

    private final static Log log = LogFactory.getLog(StoreTableSettingFacadeImpl.class);

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StoreTagMessageService storeTagMessageService;

    @Autowired
    private SnsPublish snsPublish;

    @Autowired
    private StoreTableSettingDAO storeTableSettingDAO;

    @Override
    public StoreTableSettingDTO saveStoreTableSetting(StoreTableSettingSaveParam param) throws T5weiException ,TException {
        StoreTableSetting storeTableSetting = storeTableSettingService.save(param);
        StoreTableSettingDTO storeTableSettingDTO = this._buildStoreTableSettingDTO(storeTableSetting);
        this.publish(storeTableSettingDTO.getMerchantId(),storeTableSettingDTO.getStoreId());
        return storeTableSettingDTO;
    }

    @Override
    public StoreTableSettingDTO getStoreTableSetting(int merchantId, long storeId)  throws T5weiException ,TException {
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, true);
        return this._buildStoreTableSettingDTO(storeTableSetting);
    }

    @Override
    public StoreRefundReasonDTO saveStoreRefundReason(StoreRefundReasonSaveParam param) throws T5weiException, TException {
        if(DataUtil.isEmpty(param.getRefundReason())){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + param.getStoreId() + "], merchantId[" + param.getMerchantId()
                  +"] " + "refundReason[" + param.getRefundReason() + "] is null !");
        }
        if(param.getRefundReason().length() > 100){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + param.getStoreId() + "], merchantId[" + param.getMerchantId()
                    +"] " + "refundReason[" + param.getRefundReason() + "] length must < 100 !");
        }
        StoreTagMessageParam tagMessageParam = _buildStoreTagMessageParam(param);
        StoreTagMessage storeTagMessage = storeTagMessageService.saveStoreTagMessage(param.getMerchantId(), param.getStoreId(), tagMessageParam);
        return _buildStoreRefundReasonDTO(storeTagMessage);
    }

	@Override
    public List<StoreRefundReasonDTO> getStoreRefundReasons(int merchantId, long storeId)  throws T5weiException ,TException {
        List<StoreTagMessage> storeTagMessages = storeTagMessageService.getStoreTagMessages(merchantId, storeId, StoreTagMessageEnum.REFUND_REASON);
        List<StoreRefundReasonDTO> storeRefundReasonDTOs = new ArrayList<StoreRefundReasonDTO>();
        for(StoreTagMessage storeTagMessage : storeTagMessages){
            StoreRefundReasonDTO storeRefundReasonDTO = this._buildStoreRefundReasonDTO(storeTagMessage);
            storeRefundReasonDTOs.add(storeRefundReasonDTO);
        }
        return storeRefundReasonDTOs;
    }

    @Override
    public void deleteStoreRefundReason(int merchantId, long storeId, long refundReasonId)  throws T5weiException ,TException {
        if(refundReasonId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "], merchantId[" + merchantId
                    +"] " + "refundReasonId [" + refundReasonId + "] must > 0 !");
        }
        storeTagMessageService.deleteStoreTagMessage(merchantId, storeId, refundReasonId);
    }

    @Override
    public void fixCustomerPay() throws T5weiException, TException {
        storeTableSettingDAO.fixCustomerPay();
    }

    public StoreTableSettingDTO _buildStoreTableSettingDTO(StoreTableSetting storeTableSetting){
        StoreTableSettingDTO storeTableSettingDTO = new StoreTableSettingDTO();
        BeanUtil.copy(storeTableSetting,storeTableSettingDTO);
        return storeTableSettingDTO;
    }

    public void publish(int merchantId, long storeId){
        //SNS发布事件
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("merchantId",merchantId);
        dataMap.put("storeId",storeId);
        dataMap.put("updateTime",System.currentTimeMillis());
        int eventType = EventType.STORE_TABLE_SETTING;
        String storeTableSettingTopicArn = SysConfig.getStoreTableSettingTopicArn();
        snsPublish.publish(dataMap,eventType,storeTableSettingTopicArn);
    }
    
    private StoreTagMessageParam _buildStoreTagMessageParam(StoreRefundReasonSaveParam param) {
    	StoreTagMessageParam tagMessageParam = new StoreTagMessageParam();
        tagMessageParam.setTagId(param.getRefundReasonId());
        tagMessageParam.setMerchantId(param.getMerchantId());
        tagMessageParam.setStoreId(param.getStoreId());
        tagMessageParam.setTagMessage(param.getRefundReason());
        tagMessageParam.setTagType(StoreTagMessageEnum.REFUND_REASON.getValue());
        return tagMessageParam;
	}
    
    private StoreRefundReasonDTO _buildStoreRefundReasonDTO(StoreTagMessage tagMessage){
    	StoreRefundReasonDTO refundReasonDTO = new StoreRefundReasonDTO();
    	refundReasonDTO.setMerchantId(tagMessage.getMerchantId());
    	refundReasonDTO.setStoreId(tagMessage.getStoreId());
    	refundReasonDTO.setRefundReason(tagMessage.getTagMessage());
    	refundReasonDTO.setRefundReasonId(tagMessage.getTagId());
    	refundReasonDTO.setCreateTime(tagMessage.getCreateTime());
    	refundReasonDTO.setUpdateTime(tagMessage.getUpdateTime());
    	return refundReasonDTO;
    }
}
