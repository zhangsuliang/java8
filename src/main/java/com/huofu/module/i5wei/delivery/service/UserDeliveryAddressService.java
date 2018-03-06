package com.huofu.module.i5wei.delivery.service;

import huofucore.facade.i5wei.delivery.UserDeliveryAddressParam;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.delivery.dao.StoreDeliveryBuildingDAO;
import com.huofu.module.i5wei.delivery.dao.UserDeliveryAddressDAO;
import com.huofu.module.i5wei.delivery.entity.StoreDeliveryBuilding;
import com.huofu.module.i5wei.delivery.entity.UserDeliveryAddress;

/**
 * Created by akwei on 5/6/15.
 */
@Service
public class UserDeliveryAddressService {

    @Autowired
    private UserDeliveryAddressDAO userDeliveryAddressDAO;

    @Autowired
    private StoreDeliveryBuildingDAO storeDeliveryBuildingDAO;

    public UserDeliveryAddress loadUserDeliveryAddressByAddressId(long userId, long addressId) throws T5weiException {
        return this.userDeliveryAddressDAO.loadById(userId, addressId);
    }

    public UserDeliveryAddress saveUserDeliveryAddress(UserDeliveryAddressParam param) throws T5weiException {
        UserDeliveryAddress userDeliveryAddress;
        if (param.getAddressId() > 0) {
            userDeliveryAddress = this.userDeliveryAddressDAO.loadById(param.getUserId(), param.getAddressId());
            userDeliveryAddress.snapshot();
            BeanUtil.copy(param, userDeliveryAddress);
            userDeliveryAddress.setUpdateTime(System.currentTimeMillis());
            userDeliveryAddress.setLastUsedTime(System.currentTimeMillis());
            userDeliveryAddress.update();
        } else {
            userDeliveryAddress = new UserDeliveryAddress();
            BeanUtil.copy(param, userDeliveryAddress);
            userDeliveryAddress.init4Create();
            userDeliveryAddress.create();
        }
        return userDeliveryAddress;
    }

    public List<UserDeliveryAddress> getUserDeliveryAddressesByUserId(long userId, int merchantId, long storeId, int size, boolean loadBuilding) {
        int _size = size + 20;//多获取20个为了防止店铺删除外送楼宇后的数据获取出现问题
        List<UserDeliveryAddress> userDeliveryAddresses = this
                .userDeliveryAddressDAO.getListByUserIdForStore(userId,
                        merchantId, storeId, _size);
        List<Long> buildingIds = Lists.newArrayList();
        for (UserDeliveryAddress userDeliveryAddress : userDeliveryAddresses) {
            buildingIds.add(userDeliveryAddress.getBuildingId());
        }
        Map<Long, StoreDeliveryBuilding> storeDeliveryBuildingMap = this.storeDeliveryBuildingDAO.getMapInIds(merchantId, storeId, buildingIds);
        Iterator<UserDeliveryAddress> iterator = userDeliveryAddresses
                .iterator();
        while (iterator.hasNext()) {
            UserDeliveryAddress userDeliveryAddress = iterator.next();
            StoreDeliveryBuilding storeDeliveryBuilding =
                    storeDeliveryBuildingMap.get(userDeliveryAddress
                            .getBuildingId());
            if (storeDeliveryBuilding == null || storeDeliveryBuilding.isDeleted()) {
                iterator.remove();
                continue;
            }
            if (loadBuilding) {
                userDeliveryAddress.setStoreDeliveryBuilding(storeDeliveryBuilding);
            }
        }
        List<UserDeliveryAddress> _userDeliveryAddresses = DataUtil.subList
                (userDeliveryAddresses, 0, size);
        return _userDeliveryAddresses;
    }

    public void deleteUserDeliveryAddress(long userId, long addressId) {
        this.userDeliveryAddressDAO.deleteById(userId, addressId);
    }

    public void updateUsrDeliveryAddressLastUsedTime(long userId, long addressId) {
        this.userDeliveryAddressDAO.updateLastUsedTime(userId, addressId, System.currentTimeMillis());
    }
}
