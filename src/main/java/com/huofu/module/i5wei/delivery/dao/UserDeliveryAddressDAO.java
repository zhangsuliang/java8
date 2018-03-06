package com.huofu.module.i5wei.delivery.dao;

import com.huofu.module.i5wei.delivery.entity.UserDeliveryAddress;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class UserDeliveryAddressDAO extends AbsQueryDAO<UserDeliveryAddress> {

    public UserDeliveryAddress getById(long userId, long addressId) {
        return this.query.objById(UserDeliveryAddress.class, addressId);
    }

    public UserDeliveryAddress loadById(long userId, long addressId) throws T5weiException {
        UserDeliveryAddress userDeliveryAddress = this.getById(userId, addressId);
        if (userDeliveryAddress == null) {
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_ADDRESS_INVALID.getValue(), "userId[" + userId + "] addressId[" + addressId + "] invalid");
        }
        return userDeliveryAddress;
    }

    public List<UserDeliveryAddress> getListByUserIdForStore(long userId, int merchantId, long storeId, int size) {
        return this.query.list(UserDeliveryAddress.class, "where user_id=? and merchant_id=? and store_id=? order by last_used_time desc limit ?", new Object[]{userId, merchantId, storeId, size});
    }

    public void deleteById(long userId, long addressId) {
        this.query.deleteById(UserDeliveryAddress.class, new Object[]{addressId});
    }

    public void updateLastUsedTime(long userId, long addressId, long time) {
        this.query.update(UserDeliveryAddress.class, "set last_used_time=? where user_id=? and address_id=?", new Object[]{time, userId, addressId});
    }
}
