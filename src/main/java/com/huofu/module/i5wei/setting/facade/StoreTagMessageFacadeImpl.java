package com.huofu.module.i5wei.setting.facade;

import java.util.List;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.huofu.module.i5wei.setting.entity.StoreTagMessage;
import com.huofu.module.i5wei.setting.service.StoreTagMessageService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageDTO;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageFacade;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageParam;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofuhelper.util.ValidateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;

@Component
@ThriftServlet(name = "storeTagMessageFacadeServlet", serviceClass = StoreTagMessageFacade.class)
public class StoreTagMessageFacadeImpl implements StoreTagMessageFacade.Iface {

	@Autowired
	private StoreTagMessageService storeTagMessageService;
	
	@ThriftClient
	private StoreFacade.Iface storeFacade;
	
	@Override
	public List<StoreTagMessageDTO> getStoreTagMessageDTOs(int merchantId, long storeId, StoreTagMessageEnum storeTagMessageEnum) throws T5weiException, TException {
		if(merchantId == 0 || storeId == 0){
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId is " + merchantId + ",storeId is" + storeId);
		}
		List<StoreTagMessage> storeTagMessages = this.storeTagMessageService.getStoreTagMessages(merchantId, storeId, storeTagMessageEnum);
		return BeanUtil.copyList(storeTagMessages, StoreTagMessageDTO.class);
	}

	@Override
	public List<StoreTagMessageDTO> updateStoreTagSort(int merchantId, long storeId, List<Long> tagIds) throws T5weiException, TException {
		if(merchantId == 0 || storeId == 0){
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId is " + merchantId + ",storeId is" + storeId);
		}
		List<StoreTagMessage> storeTagMessages = this.storeTagMessageService.updateStoreTagSort(merchantId, storeId, tagIds);
		return BeanUtil.copyList(storeTagMessages, StoreTagMessageDTO.class);
	}

	@Override
	public StoreTagMessageDTO saveStoreTagMessageDTO(int merchantId, long storeId, StoreTagMessageParam param) throws T5weiException, TException {
		if(merchantId == 0 || storeId == 0){
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId is " + merchantId + ",storeId is" + storeId);
		}
		if(!ValidateUtil.testLength(param.getTagMessage(), 1, 90, false)){
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId is " + merchantId + ",storeId is" + storeId + ",tagMessage" + param.getTagMessage());
		}
		StoreTagMessage storeTagMessage = this.storeTagMessageService.saveStoreTagMessage(merchantId, storeId, param);
		return BeanUtil.copy(storeTagMessage, StoreTagMessageDTO.class);
	}

	@Override
	public void deleteStoreTagMessageDTO(int merchantId, long storeId, long tagId) throws T5weiException, TException {
		if(merchantId == 0 || storeId == 0 || tagId == 0){
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId is " + merchantId + ",storeId is " + storeId + ",tagId is " + tagId);
		}
		this.storeTagMessageService.deleteStoreTagMessage(merchantId, storeId, tagId);
	}

	@Override
	public void initStoreTagMessageDTOs(int merchantId, long storeId, StoreTagMessageEnum storeTagMessageEnum) throws T5weiException, TException {
		this.storeTagMessageService.initStoreTagMessages(merchantId, storeId, storeTagMessageEnum);
	}

	@Override
	public void initAllStoreTagMessageDTOs(StoreTagMessageEnum storeTagMessageEnum) throws T5weiException, TException {
		if(storeTagMessageEnum == null){
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeTagMessageEnum is null");
		}
		int page=1;
		int size=100;
		boolean hasStore = true;
		while(hasStore){
			List<StoreDTO> storeDTOs = storeFacade.getAllNormalStores(page, size);
			if(!storeDTOs.isEmpty()){
				this.storeTagMessageService.initAllStoreTagMessageDTOs(storeDTOs, storeTagMessageEnum);
				page++;
			}else{
				hasStore = false;
			}
		}
	}
}
