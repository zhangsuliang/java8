package com.huofu.module.i5wei.promotion.facade;

import com.google.common.collect.Lists;
import huofucore.facade.i5wei.promotion.StoreMenuPromotionFacade;
import huofucore.facade.i5wei.promotion.StorePromotionConflictFacade;
import huofucore.facade.i5wei.promotion.StorePromotionConflictMapDTO;
import huofucore.facade.i5wei.promotion.StorePromotionGratisParam;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Suliang on 2017/2/14.
 */
@Component
@ThriftServlet(name = "storePromotionConflictFacadeServlet", serviceClass = StorePromotionConflictFacade.class)
public class StorePromotionConflictFacadeImp implements StorePromotionConflictFacade.Iface{

/*
    @Override
    public List<StorePromotionConflictMapDTO> getStorePriomotionConflictDTOs(List<StorePromotionConflictMapDTO> conflictMapDTOs) throws TException {
        List<StorePromotionConflictMapDTO> list=Lists.newArrayList();
        list.addAll(conflictMapDTOs);
        return list;
    }*/

	@Override
	public List<StorePromotionConflictMapDTO> getStorePriomotionConflictDTOs(
			StorePromotionGratisParam param) throws TException {
		
		return null;
	}
}
