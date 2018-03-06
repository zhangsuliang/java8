package com.huofu.module.i5wei.promotion.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisChargeItemDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisDAO;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItem;
import com.huofu.module.i5wei.promotion.service.StoreChargeItemPromotionService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.promotion.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 买免活动验证信息 Create By Suliang on 2016/12/26
 */
@Component
public class StorePromotionGratisFacadeValidator extends BasePromotionValidator {
	@Resource
	private StoreChargeItemPromotionService storeChargeItemPromotionService;

	@Resource
	private StorePromotionGratisChargeItemDAO storePromotionGratisChargeItemDAO;

	@Resource
	private StorePromotionGratisDAO storePromotionGratisDAO;

	// 自动买免中收费项目是否参加首单特价和买免活动
	public void validate4Activities(int privilegeWay,StorePromotionGratisParam param,StorePromotionGratis storePromotionGratis) throws T5weiException{
		if (privilegeWay==StorePromotionGratis.PROMOTION_GRATIS_4_AUTO) {
            boolean bool = false;
            if (param.getPromotionGratisId() > 0) {
                //根据活动的ID获取对应的收费项目列表
                Map<Long, List<StorePromotionGratisChargeItem>> map = this.storePromotionGratisChargeItemDAO.getMapInStorePromotionGratisIds(param.getMerchantId(), param.getStoreId(), Lists.newArrayList(param.getPromotionGratisId()), false, false);
                if (map.get(param.getPromotionGratisId()) != null) {
                    bool = this._equalList(param.getChargeItemIds(), StorePromotionGratisChargeItem.getIds(map.get(param.getPromotionGratisId())));
                } 
            }
            if(!bool){
            	//判断已经选择的收费项目是否参加收单特价
                List<StoreChargeItemPromotion> chargeItemPromotions = this.storeChargeItemPromotionService.getListByIds(param.getMerchantId(), param.getStoreId(), param.getChargeItemIds(), System.currentTimeMillis());
                if (!chargeItemPromotions.isEmpty() && chargeItemPromotions != null) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_INVALID.getValue(),
                            "merchantId [" + param.getMerchantId() + "] storeId [" + param.getStoreId() + "] some chargeItmes already has participated in the 'other Activity' ");
                }
            	//判断已选择的收费项目是否参加买免活动
                List<StorePromotionGratis> gratisList = this.storePromotionGratisDAO.getStorePromotionGratisIntersectTime(param.getMerchantId(),  param.getStoreId(), privilegeWay, param.getBeginTime(), param.getEndTime());
            	if(gratisList!=null){
            		Map<Long, List<StorePromotionGratisChargeItem>> chargeItemMap = this.storePromotionGratisChargeItemDAO.getMapInStorePromotionGratisIds(param.getMerchantId(), param.getStoreId(), StorePromotionGratis.getIds(gratisList), false, false);
            		 for (Entry<Long, List<StorePromotionGratisChargeItem>> entry : chargeItemMap.entrySet()) {
            			 List<Long> chargeitemIds = StorePromotionGratis.getChargeitemIds(entry.getValue());
            			 chargeitemIds.retainAll(param.chargeItemIds);
            			 if(chargeitemIds!=null){
            				 throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_INVALID.getValue(),
                                     "merchantId [" + param.getMerchantId() + "] storeId [" + param.getStoreId() + "] some chargeItmes already has participated in the 'other Activity' "); 
            			 }
            			  }
         
            	}
            }
            storePromotionGratis.setCouponSupport(false);
            storePromotionGratis.setShared(false);

        } else {
            storePromotionGratis.setWechatOnly(false);
            storePromotionGratis.setCouponSupport(false);
        }
	}

	public void validate4Save(StorePromotionGratisParam param) throws T5weiException {
		String prefix = "merchantId[" + param.getMerchantId() + "] storeId[" + param.getStoreId() + "]";
		if (param.getPurchaseNum() < param.getGratisNum()) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + "purchaseNum ["
					+ param.getPurchaseNum() + "] must greater than gratisNum [" + param.getGratisNum() + "]");
		}
		if (param.getGratisNum() == 0 || param.getPurchaseNum() == 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + "purchaseNum ["
					+ param.getPurchaseNum() + "] or gratisNum [" + param.getGratisNum() + "] can not equal to zero");
		}
		if (param.getGratisNum() % 0.5 > 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
					prefix + "gratisNum[" + param.getGratisNum() + "] must be the multiple of 0.5");
		}
		if (DataUtil.isEmpty(param.getTitle())) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
					prefix + " title[" + param.getTitle() + "] must be not empty");
		}
		if (param.getTitle().length() > 20) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
					prefix + " title[" + param.getTitle() + "] length must <=20");
		}
		long todayBegin = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		if (param.getPromotionGratisId() <= 0) {
			if (param.getBeginTime() < todayBegin) {
				throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
						prefix + " beginTime[" + param.getBeginTime() + "] must >= today");
			}
		}
		if (!param.isUnlimit() && param.getEndTime() < todayBegin) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
					prefix + " endTime[" + param.getEndTime() + "] must >= today");
		}
		if (param.getTakeModesSize() == 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
					prefix + " takeMode must be not empty");
		}
		this.validateTakeMode(param.getTakeModes(), prefix);
		if (param.getPeriodParams() != null) {
			this.validatePeriod(param.getPeriodParams(), prefix);
		}

	}

	
	/**
	 * 判断两个集合是否相等
	 *
	 * @param list1
	 * @param list2
	 * @return
	 */
	private boolean _equalList(List<Long> list1, List<Long> list2) {
		return (list1.size() == list2.size()) && list1.containsAll(list2);
	}

	private String _spliceChargeItemPromotionGratisIds(List<StorePromotionGratis> gratisList) {
		String idSpliceStr = "";
		for (StorePromotionGratis gratis : gratisList) {
			idSpliceStr += gratis.getPromotionGratisId() + " ";
		}
		return idSpliceStr;

	}

	private String _spliceChargeItemPromotionIds(List<StoreChargeItemPromotion> chargeItemPromotions) {
		String idSpliceStr = "";
		for (StoreChargeItemPromotion storeChargeItemPromotion : chargeItemPromotions) {
			idSpliceStr += storeChargeItemPromotion.getChargeItemId() + " ";
		}
		return idSpliceStr;
	}

}
