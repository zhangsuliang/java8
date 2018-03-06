package com.huofu.module.i5wei.promotion.facade;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import huofucore.facade.i5wei.promotion.*;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.facade.MenuFacadeUtil;
import com.huofu.module.i5wei.order.service.StoreOrderPromotionStatService;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisTemp;
import com.huofu.module.i5wei.promotion.service.StorePromotionGratisService;
import com.huofu.module.i5wei.promotion.service.StorePromotionSummary;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderPromotionTypeEnum;
import huofuhelper.util.PageResult;
import huofuhelper.util.thrift.ThriftServlet;

/**
 * Create By Suliang on 2016/12/26
 */
@Component
@ThriftServlet(name = "storePromotionGratisFacadeServlet", serviceClass = StorePromotionGratisFacade.class)
public class StorePromotionGratisFacadeImpl implements StorePromotionGratisFacade.Iface {
	@Resource
	private StorePromotionGratisService storePromotionGratisService;

	@Resource
	private StorePromotionGratisFacadeValidator storePromotionGratisFacadeValidator;

	@Resource
	private StoreOrderPromotionStatService storeOrderPromotionStatService;

	@Resource
	private MenuFacadeUtil menuFacadeUtil;
	
	/**
	 * 保存活动信息
	 * 
	 *@param param 保存参数
	 */
	@Override
	public StorePromotionGratisDTO saveStorePromotionGratis(StorePromotionGratisParam param) throws T5weiException, TException {
		// 参数验证
		this.storePromotionGratisFacadeValidator.validate4Save(param);
		// 保存操作
		StorePromotionGratis storePromotionGratis = this.storePromotionGratisService.saveStorePromotionGratis(param);
		// 构建关联信息
		List<StorePromotionGratis> promotionList = Lists.newArrayList(storePromotionGratis);
		this.storePromotionGratisService.buildStorePromotionGratisRefInfo(param.getMerchantId(), param.getStoreId(), promotionList,false, false, true);
		// 实体转换成DTO
		return this.menuFacadeUtil.buildStorePromotionGratisDTO(storePromotionGratis, true);
	}
	
   /**
    * 根据活动ID删除活动信息
    * 
   	 * @param merchantId 商户ID
	 * @param storeId  商铺ID
	 * @param promotionGratisId  活动ID
	 * @return 
    */
	@Override
	public void deleteStorePromotionGratis(int merchantId, long storeId, long promotionGratisId) throws T5weiException, TException {
		if (this.storeOrderPromotionStatService.hasStoreOrderPromotions(merchantId, storeId, promotionGratisId,StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue(), true)) {
			throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_STAT_EXIST.getValue(),
					"merchantId[" + merchantId + "] storeId[" + storeId + "] promotionGratisId[" + promotionGratisId + "] has stat data");
		}
		this.storePromotionGratisService.deleteStorePromotionGratis(merchantId, storeId, promotionGratisId);
	}

	/**
	 * 根据活动ID获取活动信息
	 * 
	 * @param merchantId 商户ID
	 * @param storeId  商铺ID
	 * @param promotionGratisId  活动ID
	 * @return
	 */
	@Override
	public StorePromotionGratisDTO getStorePromotionGratisInfo(int merchantId, long storeId, long promotionGratisId)throws T5weiException, TException {
		StorePromotionGratis promotionGratis = this.storePromotionGratisService.getStorePromotionGratisInfo(merchantId, storeId,promotionGratisId, false, false);
        return this.menuFacadeUtil.buildStorePromotionGratisDTO(promotionGratis, true);
	}
	
    /**
     * 根据标题获取不同状态下的活动列表
     * 
     * @param param 查询参数
     * @return
     */
	@Override
	public Map<String, List<StorePromotionGratisDTO>> getStorePromotionGratisMapByTitle(QueryByParam param)throws TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		int size = param.getSize();
		String title = param.getTitle();
		Map<String, List<StorePromotionGratisDTO>> promotionGratisDTOMap = Maps.newHashMap();
		StorePromotionGratisTemp temp = storePromotionGratisService.getStorePromotionGratisMapByTitle(merchantId, storeId, size, title);
	    promotionGratisDTOMap.put(StorePromotionGratisTemp.NOT_OPENED_LIST, this.menuFacadeUtil.buildStorePromotionGratisDTOs(temp.getNotOpenedList(), true));
	    promotionGratisDTOMap.put(StorePromotionGratisTemp.NOT_BEGIN_LIST, this.menuFacadeUtil.buildStorePromotionGratisDTOs(temp.getNotBeginList(), true));
	    promotionGratisDTOMap.put(StorePromotionGratisTemp.DOING_LIST, this.menuFacadeUtil.buildStorePromotionGratisDTOs(temp.getDoingList(), true));
	    promotionGratisDTOMap.put(StorePromotionGratisTemp.PAUSED_LIST, this.menuFacadeUtil.buildStorePromotionGratisDTOs(temp.getPausedList(), true));
	    promotionGratisDTOMap.put(StorePromotionGratisTemp.ENDED_LIST, this.menuFacadeUtil.buildStorePromotionGratisDTOs(temp.getEndedList(), true));
		return promotionGratisDTOMap;
	}

	/**
	 * 获取当前店铺活动的概要信息
	 * 
	 * @param merchantId 商户ID
	 * @param storeId  商铺ID
	 */
	@Override
	public StorePromotionGratisSummaryDTO getStorePromotionGratisSummary(int merchantId, long storeId)throws TException {
		StorePromotionSummary<StorePromotionGratis> promotionGratisSummary = this.storePromotionGratisService.getStorePromotionGratisSummary(merchantId, storeId);
		StorePromotionGratisSummaryDTO promotionGratisSummaryDTO = new StorePromotionGratisSummaryDTO();
		promotionGratisSummaryDTO.setCount4NotOpened(promotionGratisSummary.getCount4NotOpened());
		promotionGratisSummaryDTO.setCount4NotBegin(promotionGratisSummary.getCount4NotBegin());
		promotionGratisSummaryDTO.setCount4Doing(promotionGratisSummary.getCount4Doing());
		promotionGratisSummaryDTO.setCount4Paused(promotionGratisSummary.getCount4Paused());
		promotionGratisSummaryDTO.setCount4Ended(promotionGratisSummary.getCount4Ended());

		if (promotionGratisSummary.getList4NotOpened() != null) {
			promotionGratisSummaryDTO.setList4NotOpen(this.menuFacadeUtil.buildStorePromotionGratisDTOs(promotionGratisSummary.getList4NotOpened(), true));
		}
		if (promotionGratisSummary.getList4NotBegin() != null) {
			promotionGratisSummaryDTO.setList4NotBegin(this.menuFacadeUtil.buildStorePromotionGratisDTOs(promotionGratisSummary.getList4NotBegin(), true));
		}
		if (promotionGratisSummary.getList4Doing() != null) {
			promotionGratisSummaryDTO.setList4Doing(this.menuFacadeUtil.buildStorePromotionGratisDTOs(promotionGratisSummary.getList4Doing(), true));
		}
		if (promotionGratisSummary.getList4Paused() != null) {
			promotionGratisSummaryDTO.setList4Paused(this.menuFacadeUtil.buildStorePromotionGratisDTOs(promotionGratisSummary.getList4Paused(), true));
		}
		if (promotionGratisSummary.getList4Ended() != null) {
			promotionGratisSummaryDTO.setList4End(this.menuFacadeUtil.buildStorePromotionGratisDTOs(promotionGratisSummary.getList4Ended(), true));
		}

		return promotionGratisSummaryDTO;
	}

	/**
	 * 修改活动状态
	 * 
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param promotionGratisId 活动ID
	 * @param paused  是否暂停
	 */
	@Override
	public void changePromotionGratisPaused(int merchantId, long storeId, long promotionGratisId, boolean paused) throws T5weiException, TException {
		this.storePromotionGratisService.changeStorePromotionGratisPaused(merchantId, storeId, promotionGratisId, paused);
	}
	
	/**
	 * 获取指定状态的活动列表
	 * 
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param status  状态
	 * @param pageNo  页码
	 * @param pageSize  页面大小
	 * @return
	 */
	@Override
	public StorePromotionGratisPageDTO getStorePromotionGratisByPage(int merchantId, long storeId, int status,int pageNo, int pageSize) throws TException {
		PageResult pageResult = this.storePromotionGratisService.getStorePromotionGratisPage(merchantId, storeId, status,pageNo, pageSize);
		StorePromotionGratisPageDTO gratisDTO = new StorePromotionGratisPageDTO();
		gratisDTO.setSize(pageResult.getSize());
		gratisDTO.setPageNo(pageResult.getPage());
		gratisDTO.setTotal(pageResult.getTotal());
		gratisDTO.setPageNum(pageResult.getTotalPage());
		gratisDTO.setDataList(this.menuFacadeUtil.buildStorePromotionGratisDTOs(pageResult.getList(), true));
		return gratisDTO;
	}
   
	/**
	 * 统计指定日期下所有的买免活动
	 * 
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param repastDate 就餐日期
	 * @return
	 */
	@Override
	public List<StoreOrderItemPromotionGratisStatDTO> getStorePromotionGratisListStatByRepastDate(int merchantId,long storeId, long repastDate) throws TException {
		if (storeId <= 0 || repastDate <= 0) {
			return Lists.newArrayList();
		}
		return this.storeOrderPromotionStatService.getStorePromotionGratisListStat(merchantId, storeId, repastDate);
	}

	/**
	 * 根据活动ID，统计其在指定日期下的活动信息
	 * 
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param repastDate 就餐日期(指定日期)
	 * @param promotionGratisId 活动ID
	 * @return
	 */
	@Override
	public StoreOrderItemPromotionGratisStatDTO getStorePromotionGratisStatByRepastDate(int merchantId, long storeId,long promotionGratisId, long repastDate) throws TException {
		if (storeId <= 0 || promotionGratisId <= 0 || repastDate <= 0) {
			return new StoreOrderItemPromotionGratisStatDTO();
		}
		return this.storeOrderPromotionStatService.getStoreOrderPromotionGratisDTOStatByRepastDate(merchantId, storeId,promotionGratisId, repastDate);
	}
	
	/**
	 * 根据活动ID，统计该活动在指定日期范围内的活动信息
	 * 
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param promotionGratisId  活动ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return
	 */
	@Override
	public StoreOrderItemPromotionGratisStatDTO getStorePromotionGratisStatByTimeRange(int merchantId, long storeId,
			long promotionGratisId, long startDate, long endDate) throws TException {
		if (storeId <= 0 || promotionGratisId <= 0 || startDate <= 0 || endDate <= 0) {
			return new StoreOrderItemPromotionGratisStatDTO();
		}
		return this.storeOrderPromotionStatService.getStoreOrderPromotionGratisDTOStatByTimeRange(merchantId, storeId,
				promotionGratisId, startDate, endDate);
	}

}
