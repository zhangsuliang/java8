package com.huofu.module.i5wei.delivery.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.delivery.entity.UserDeliveryAddress;
import com.huofu.module.i5wei.delivery.service.UserDeliveryAddressService;
import huofucore.facade.i5wei.delivery.StoreDeliveryBuildingDTO;
import huofucore.facade.i5wei.delivery.UserDeliveryAddressDTO;
import huofucore.facade.i5wei.delivery.UserDeliveryAddressParam;
import huofucore.facade.i5wei.delivery.UserDeliveryFacade;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by akwei on 5/6/15.
 */
@Component
@ThriftServlet(name = "userDeliveryFacadeServlet", serviceClass = UserDeliveryFacade.class)
public class UserDeliveryFacadeImpl implements UserDeliveryFacade.Iface {

    @Autowired
    private UserDeliveryAddressService userDeliveryAddressService;

    @Autowired
    private UserDeliveryValidator userDeliveryValidator;

    @Override
    public UserDeliveryAddressDTO saveUserDeliveryAddress(UserDeliveryAddressParam param) throws T5weiException, TException {
        this.userDeliveryValidator.validateUserAddress(param);
        UserDeliveryAddress userDeliveryAddress = this.userDeliveryAddressService.saveUserDeliveryAddress(param);
        UserDeliveryAddressDTO userDeliveryAddressDTO = new UserDeliveryAddressDTO();
        BeanUtil.copy(userDeliveryAddress, userDeliveryAddressDTO);
        return userDeliveryAddressDTO;
    }

    @Override
    public List<UserDeliveryAddressDTO> getUserDeliveryAddressesByUserId(long userId, int merchantId, long storeId, int size, boolean loadBuilding) throws TException {
        List<UserDeliveryAddress> userDeliveryAddresses = this.userDeliveryAddressService.getUserDeliveryAddressesByUserId(userId, merchantId, storeId, size, loadBuilding);
        List<UserDeliveryAddressDTO> userDeliveryAddressDTOs = Lists.newArrayList();
        for (UserDeliveryAddress userDeliveryAddress : userDeliveryAddresses) {
            UserDeliveryAddressDTO userDeliveryAddressDTO = new UserDeliveryAddressDTO();
            BeanUtil.copy(userDeliveryAddress, userDeliveryAddressDTO);
            if (userDeliveryAddress.getStoreDeliveryBuilding() != null) {
                StoreDeliveryBuildingDTO storeDeliveryBuildingDTO = new StoreDeliveryBuildingDTO();
                BeanUtil.copy(userDeliveryAddress.getStoreDeliveryBuilding(), storeDeliveryBuildingDTO);
                userDeliveryAddressDTO.setStoreDeliveryBuildingDTO(storeDeliveryBuildingDTO);
            }
            userDeliveryAddressDTOs.add(userDeliveryAddressDTO);
        }
        return userDeliveryAddressDTOs;
    }

    @Override
    public void deleteUserDeliveryAddress(long userId, long addressId) throws TException {
        this.userDeliveryAddressService.deleteUserDeliveryAddress(userId, addressId);
    }

    @Override
    public void updateUsrDeliveryAddressLastUsedTime(long userId, long addressId) throws TException {
        this.userDeliveryAddressService.updateUsrDeliveryAddressLastUsedTime(userId, addressId);
    }
}
