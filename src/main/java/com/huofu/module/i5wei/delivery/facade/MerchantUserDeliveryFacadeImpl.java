package com.huofu.module.i5wei.delivery.facade;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.huofu.module.i5wei.delivery.entity.MerchantUserDeliveryAddress;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.delivery.service.MerchantUserDeliveryAddressService;
import com.huofu.module.i5wei.delivery.service.StoreDeliverySettingService;
import huofucore.facade.i5wei.delivery.MerchantUserDeliveryAddressDTO;
import huofucore.facade.i5wei.delivery.MerchantUserDeliveryAddressParam;
import huofucore.facade.i5wei.delivery.MerchantUserDeliveryFacade;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofuhelper.util.DataUtil;
import huofuhelper.util.GeoTool;
import huofuhelper.util.ValidateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;

@Component
@ThriftServlet(name = "merchantUserDeliveryFacadeServlet", serviceClass = MerchantUserDeliveryFacade.class)
public class MerchantUserDeliveryFacadeImpl implements MerchantUserDeliveryFacade.Iface{
	private static final Log log = LogFactory.getLog(MerchantUserDeliveryFacadeImpl.class);
	@Autowired
	private MerchantUserDeliveryAddressService merchantUserDeliveryAddressService;
	
	@ThriftClient
	private StoreFacade.Iface storeFacade;
	
	@Autowired
	private StoreDeliverySettingService storeDeliverySettingService;

	@Override
	public MerchantUserDeliveryAddressDTO saveMerchantUserDeliveryAddress(MerchantUserDeliveryAddressParam param) throws T5weiException, TException {
		if (!ValidateUtil.testLength(param.getContactName(), 1, 50, false)) {
			log.error(param);
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_CONTACT_NAME.getValue(), "contact name[" + param.getContactName() + "] invalid");
        }
        if (!ValidateUtil.testLength(param.getContactPhone(), 1, 50, false)) {
        	log.error(param);
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_CONTACT_PHONE.getValue(), "contact phone[" + param.getContactPhone() + "] invalid");
        }
        if (!ValidateUtil.testLength(param.getUserAddress(), 1, 100, false)) {
        	log.error(param);
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_ADDRESS_INVALID.getValue(), "user address[" + param.getUserAddress() + "] invalid");
        }
        if(!ValidateUtil.testLength(param.getBuildingName(), 1, 50, false)){
        	log.error(param);
        	throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_BUILDING_NAME_INVALID.getValue(), "user building name[" + param.getBuildingName() + "] invalid");
        }
        if (!ValidateUtil.testLength(param.getBuildingAddress(), 1, 100, true)) {
        	log.error(param);
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_BUILDING_ADDRESS_INVALID.getValue(), "user building address[" + param.getBuildingAddress() + "] invalid");
        }
        if(DataUtil.isEmpty(param.getBuildingAddress())){
        	param.setBuildingAddress(param.getBuildingName());
        }
        if(param.getUserAddressLongitude() == 0.0 && param.getUserAddressLatitude() == 0.0){
        	log.error(param);
        	throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_NOT_GPS.getValue(), "user address not gps invalid");
        }
		MerchantUserDeliveryAddress merchantUserDeliveryAddress = this.merchantUserDeliveryAddressService.saveMerchantUserDeliveryAddress(param);
		return BeanUtil.copy(merchantUserDeliveryAddress, MerchantUserDeliveryAddressDTO.class);
	}

	@Override
	public void deleteMerchantUserDeliveryAddress(int merchantId, long storeId, long userId, long addressId) throws T5weiException, TException {
		this.merchantUserDeliveryAddressService.deleteMerchantUserDeliveryAddress(merchantId, storeId, userId, addressId);
	}

	@Override
	public List<MerchantUserDeliveryAddressDTO> getMerchantUserDeliveryAddressesInScope(int merchantId, long storeId, long userId) throws T5weiException, TException {
		List<MerchantUserDeliveryAddressDTO> merchantUserDeliveryAddressDTOs = new ArrayList<MerchantUserDeliveryAddressDTO>();
		StoreDeliverySetting storeDeliverySetting = storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeId);
		int deliveryScope = storeDeliverySetting.getDeliveryScope();
		StoreDTO store = storeFacade.getStore(merchantId, storeId);
		double storeLongitude = store.getLongitude();//店铺的经度
		double storeLatitud = store.getLatitude();//店铺的纬度
		
		List<MerchantUserDeliveryAddress> merchantUserDeliveryAddresses = this.merchantUserDeliveryAddressService.getMerchantUserDeliveryAddressByUserId(merchantId, storeId, userId, true);
		if(merchantUserDeliveryAddresses != null){
			for (MerchantUserDeliveryAddress merchantUserDeliveryAddress : merchantUserDeliveryAddresses) {
				double userAddressLongitude = merchantUserDeliveryAddress.getUserAddressLongitude();//经度
				double userAddressLatitud = merchantUserDeliveryAddress.getUserAddressLatitude();//纬度
				double distance = GeoTool.getPointDistance(storeLongitude, storeLatitud, userAddressLongitude, userAddressLatitud);//店铺和用户地址的距离
				if(distance <= deliveryScope){
					MerchantUserDeliveryAddressDTO merchantUserDeliveryAddressDTO = BeanUtil.copy(merchantUserDeliveryAddress, MerchantUserDeliveryAddressDTO.class);
					merchantUserDeliveryAddressDTOs.add(merchantUserDeliveryAddressDTO);
				}
			}
		}
		return merchantUserDeliveryAddressDTOs;
	}
}
