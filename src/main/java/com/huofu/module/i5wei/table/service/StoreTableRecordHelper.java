package com.huofu.module.i5wei.table.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.table.dao.StoreTableRecordOptlogDAO;
import com.huofu.module.i5wei.table.dao.TableRecordBatchRefundRecordDAO;
import com.huofu.module.i5wei.table.entity.*;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofucore.facade.i5wei.order.StoreOrderPayResultOfDynamicPayMethod;
import huofucore.facade.i5wei.order.StoreOrderPayStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderPromotionDTO;
import huofucore.facade.i5wei.order.StoreOrderPromotionTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderRefundStatusEnum;
import huofucore.facade.i5wei.sharedto.I5weiUserDTO;
import huofucore.facade.i5wei.sharedto.StoreTableStaffDTO;
import huofucore.facade.i5wei.table.*;
import huofucore.facade.merchant.staff.StaffDTO;
import huofucore.facade.merchant.staff.StaffFacade;
import huofucore.facade.pay.payment.DynamicPayMethodRefundInfo;
import huofucore.facade.pay.payment.RefundDetailDTO;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderRefundItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderSubitemDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderPromotion;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.table.dao.StoreTableRecordRefundDAO;
import org.springframework.util.Assert;

/**
 * 桌台记录帮助类
 * @author licheng7
 * 2016年5月22日 上午9:23:20
 */
@Component
public class StoreTableRecordHelper {

	@Autowired
    private IdMakerUtil idMakerUtil;
	
	@Autowired
	private StoreTableSettingService storeTableSettingService;
	
	@Autowired
	private StoreTableService storeTableService;

	@Autowired
	private StoreTableRecordService storeTableRecordService;
	
	@Autowired
	private StoreAreaService storeAreaService;
	
	@Autowired
	private StoreTimeBucketService storeTimeBucketService;
	
	@ThriftClient
	private StaffFacade.Iface staffFacade;
	
	@ThriftClient
	private UserFacade.Iface userFacade;
	
	@Autowired
	private StoreOrderDAO storeOrderDAO;
	
	@Autowired
	private StoreOrderService storeOrderService;
	
	@Autowired
	private StoreOrderHelper storeOrderHelper;
	
	@Autowired
	private StoreOrderItemDAO storeOrderItemDAO;
	
	@Autowired
	private StoreOrderSubitemDAO storeOrderSubitemDAO;
	
	@Autowired
	private StoreOrderRefundItemDAO storeOrderRefundItemDAO;
	
	@Autowired
	private StoreTableRecordRefundDAO storeTableRecordRefundDAO;
	
	@Autowired
	private StoreTableQrcodeService storeTableQrcodeService;

	@Autowired
	private TableRecordBatchRefundRecordDAO tableRecordBatchRefundRecordDAO;

	@Autowired
	private StoreTableRecordOptlogService storeTableRecordOptlogService;
	
	private static final String TABLE_RECORDID_ID = "tb_table_recordid_seq";

	/**
	 * 加载桌台记录的loadCodes
	 */
	private static Map<Integer, List<Integer>> queryLoadCodesMap = Maps.newHashMap();
	
	private static final Log log = LogFactory.getLog(StoreTableRecordHelper.class);
	
	/**
	 * 创建桌台记录
	 * <br/> merchantId 商编
	 * <br/> storeId 店铺id
	 * <br/> repastDate 就餐日期
	 * <br/> timeBucketId 营业时段
	 * <br/> storeTable 桌台
	 * <br/> masterStoreOrder 主订单
	 * <br/> tableRecordSeq 拼台序列号
	 * <br/> customerTraffic 入客数
	 * <br/> createTableRecordStaffId 开台服务员id
	 * <br/> createTableRecordUserId 自助开台用户id
	 * <br/> orderTime 点餐开始时间
	 * <br/> firstUpTime 首次上菜时间
	 * <br/> lastUpTime 菜上齐时间
	 * @param createStoreTableRecordParam
	 * @return StoreTableRecord 桌台记录
	 * @throws T5weiException 
	 */
	public StoreTableRecord createStoreTableRecord (CreateStoreTableRecordParam createStoreTableRecordParam) throws T5weiException {
		int merchantId = createStoreTableRecordParam.getMerchantId();
		long storeId = createStoreTableRecordParam.getStoreId(); 
		long repastDate = createStoreTableRecordParam.getRepastDate(); 
		long timeBucketId = createStoreTableRecordParam.getTimeBucketId(); 
		StoreTable storeTable = createStoreTableRecordParam.getStoreTable(); 
		int tableRecordSeq = createStoreTableRecordParam.getTableRecordSeq();
		int customerTraffic = createStoreTableRecordParam.getCustomerTraffic();
		long createTableRecordStaffId = createStoreTableRecordParam.getCreateTableRecordStaffId();
		long createTableRecordUserId = createStoreTableRecordParam.getCreateTableRecordUserId();
		int clientType = createStoreTableRecordParam.getClientType(); 
		long defaultStaffId = createStoreTableRecordParam.getDefaultStaffId();
		// 获取区域信息
		StoreArea storeArea = this.getStoreArea(merchantId, storeId, storeTable.getAreaId());
		if (storeArea == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "areaId["+storeTable.getAreaId()+"] invalid");
		}
		long currentTime = System.currentTimeMillis();
		StoreTableRecord storeTableRecord = new StoreTableRecord();
		storeTableRecord.snapshot();
		storeTableRecord.setMerchantId(merchantId);
		storeTableRecord.setStoreId(storeId);
		storeTableRecord.setTableId(storeTable.getTableId());
		storeTableRecord.setRepastDate(repastDate);
		storeTableRecord.setTimeBucketId(timeBucketId);
		storeTableRecord.setOrderId(""); // 桌台记录关联主订单
		storeTableRecord.setTableRecordSeq(tableRecordSeq); // 拼桌序列号
		storeTableRecord.setTableRecordTime(currentTime);
		storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.WAIT_MEAL.getValue()); // 初始值为等待点餐
		storeTableRecord.setPayStatus(PayStatusEnum.UN_PAY.getValue()); // 初始值为未付
		storeTableRecord.setCustomerTraffic(customerTraffic); // 就餐人数
		storeTableRecord.setStaffId(defaultStaffId); // 桌台默认服务员
		storeTableRecord.setCreateTableStaffId(createTableRecordStaffId); // 开台服务员
		storeTableRecord.setSettleStaffId(0L); // 结账服务员
		storeTableRecord.setClearTableStaffId(0L); // 清台服务员
		storeTableRecord.setCreateTableUserId(createTableRecordUserId); // 自助开台用户
		storeTableRecord.setSettleUserId(0L); // 自助结账用户
		storeTableRecord.setOrderTime(0L); // 点餐开始时间
		storeTableRecord.setFirstUpTime(0L); // 首次上菜时间
		storeTableRecord.setLastUpTime(0L); // 菜上齐时间
		storeTableRecord.setClearTableTime(0L); // 清台时间
		storeTableRecord.setMergeTableRecordId(0L); // 合台记录id
		storeTableRecord.setMergeTableTime(0L); // 合台时间
		storeTableRecord.setAreaId(storeTable.getAreaId()); // 区域id
		storeTableRecord.setSettleTime(0L); //结账时间
		storeTableRecord.setDiscountPro(100); // 整单折扣比例
		storeTableRecord.setMergeStaffId(0L); // 合台服务员
		storeTableRecord.setTransferStaffId(0L); // 转台服务员
		storeTableRecord.setCreateTime(currentTime);
		storeTableRecord.setUpdateTime(currentTime);
		storeTableRecord.setAreaName(storeArea.getAreaName());
		storeTableRecord.setTableName(storeTable.getName());
		storeTableRecord.setTablePrice(0L);
		storeTableRecord.setTableFee(0L);
		storeTableRecord.setAlreadyDiscountAmount(0L);
		storeTableRecord.setRefundChargeItemPrice(0L);
		storeTableRecord.setPayAbleAmount(0L);
		storeTableRecord.setPaidAmount(0L);
		storeTableRecord.setRefundAmount(0L);
		storeTableRecord.setMealCheckoutNum(0);
		storeTableRecord.setMealTakeupNum(0);
		storeTableRecord.setClientType(clientType);
		//起菜状态
		//storeTableRecord.setSendType(StoreSendTypeEnum.START_TAKE_ORDER.getValue());
		return storeTableRecord;
	}
	
	/**
	 * 根据id获取桌台
	 * @param merchantId
	 * @param storeId
	 * @param tableId
	 * @return
	 * @throws T5weiException
	 */
	public StoreTable getStoreTable (int merchantId, long storeId, long tableId) throws T5weiException {
		StoreTable storeTable = null;
		try {
			storeTable = storeTableService.getStoreTableById(merchantId, storeId, tableId, true);
		} catch (Exception e) {
			throw new T5weiException(StoreTableErrorCodeEnum.TABLE_ID_ERROR.getValue(), "get table by tableId["+tableId+"] fail");
		}
		return storeTable;
	}
	
	/**
	 * 根据id获取区域
	 * @param merchantId
	 * @param storeId
	 * @param areaId
	 * @return
	 * @throws T5weiException
	 */
	public StoreArea getStoreArea (int merchantId, long storeId, long areaId) throws T5weiException {
		StoreArea storeArea = null;
		try {
			storeArea = storeAreaService.getValidStoreAreaById(merchantId, storeId, areaId, false);
		} catch (Exception e) {
			throw new T5weiException(StoreTableErrorCodeEnum.TABLE_ID_ERROR.getValue(), "get area by areaId["+areaId+"] fail");
		}
		return storeArea;
	}
	
	/**
	 * 为子订单设置是否为后付费
	 * @param storeId
	 * @param storeTableSetting
	 * @param subStoreOrder
	 * @throws T5weiException
	 */
	public void checkPayAfter (long storeId, StoreTableSetting storeTableSetting, StoreOrder subStoreOrder) throws T5weiException {
		if (!subStoreOrder.isPayAfter()) {
			// 若非后付费模式，需要校验子订单是否已经支付
			if (subStoreOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
				throw new T5weiException(StoreTableErrorCodeEnum.PAY_BEFORE_CAN_NOT_UNPAY.getValue(), "store["+storeId+"] unable payAfter, order["+subStoreOrder.getOrderId()+"] payStatus can not be ["+StoreOrderPayStatusEnum.findByValue(subStoreOrder.getPayStatus())+"]");
			}
		} 
	}
	
	/**
	 * 支付详情
	 * @param tableRecordPayDetailResultList
	 * @return
	 */
	public PayDetailInfoDTO buildPayDetailInfoDTO (List<TableRecordPayDetailResult> tableRecordPayDetailResultList, TableRecordAmountsResult tableRecordAmountsResult, List<StoreOrder> subStoreOrders, StoreTableRecord storeTableRecord) {
		PayDetailInfoDTO payDetailInfoDTO = new PayDetailInfoDTO();
		if (payDetailInfoDTO.getDynamicPayMethodPayResults() == null) {
			payDetailInfoDTO.setDynamicPayMethodPayResults(new HashMap<Integer,DynamicPayMethodPayResult>());
		}
		for (TableRecordPayDetailResult tableRecordPayDetailResult : tableRecordPayDetailResultList) {
			payDetailInfoDTO.setCashAmount(payDetailInfoDTO.getCashAmount()+tableRecordPayDetailResult.getCashAmount());
			payDetailInfoDTO.setCouponAmount(payDetailInfoDTO.getCouponAmount()+tableRecordPayDetailResult.getCouponAmount());
			payDetailInfoDTO.setPrepaidcardAmount(payDetailInfoDTO.getPrepaidcardAmount()+tableRecordPayDetailResult.getPrepaidcardAmount());
			payDetailInfoDTO.setUserAccountAmount(payDetailInfoDTO.getUserAccountAmount()+tableRecordPayDetailResult.getUserAccountAmount());
			payDetailInfoDTO.setYjpayAmount(payDetailInfoDTO.getYjpayAmount()+tableRecordPayDetailResult.getYjpayAmount());
			payDetailInfoDTO.setWechatAmount(payDetailInfoDTO.getWechatAmount()+tableRecordPayDetailResult.getWechatAmount());
			payDetailInfoDTO.setIposAmount(payDetailInfoDTO.getIposAmount()+tableRecordPayDetailResult.getIposAmount());
			payDetailInfoDTO.setPosAmount(payDetailInfoDTO.getPosAmount()+tableRecordPayDetailResult.getPosAmount());
			payDetailInfoDTO.setPublicTransferAmount(payDetailInfoDTO.getPublicTransferAmount()+tableRecordPayDetailResult.getPublicTransferAmount());
			payDetailInfoDTO.setAliPayAmount(payDetailInfoDTO.getAliPayAmount()+tableRecordPayDetailResult.getAliPayAmount());
			payDetailInfoDTO.setIboxPayAmount(payDetailInfoDTO.getIboxPayAmount()+tableRecordPayDetailResult.getIboxPayAmount());
			payDetailInfoDTO.setCreditAmount(payDetailInfoDTO.getCreditAmount()+tableRecordPayDetailResult.getCreditAmount());
			if (payDetailInfoDTO.getDynamicPayMethodPayResults() == null) {
				payDetailInfoDTO.setDynamicPayMethodPayResults(new HashMap<>());
			}
			Map<Integer,DynamicPayMethodPayResult> dynamicPayMethodPayResultMap = payDetailInfoDTO.getDynamicPayMethodPayResults();
			// add by wxy 后付费会对自定义券进行合并显示
            List<StoreOrderPayResultOfDynamicPayMethod> payResultOfDynamicPayMethodList = tableRecordPayDetailResult.getStoreOrderPayResultOfDynamicPayMethodList();
            if(payResultOfDynamicPayMethodList != null){
                for(StoreOrderPayResultOfDynamicPayMethod dynamicPayMethod : payResultOfDynamicPayMethodList){
                    int dynamicPayMethodId = dynamicPayMethod.getDynamicPayMethodId();
                    String dynamicPayMethodName = dynamicPayMethod.getDynamicPayMethodName();
                    long dynamicPayMethodAmount = dynamicPayMethod.getAmount();
                    long dynamicPayMethodActualAmount = dynamicPayMethod.getActualAmount();
                    if (dynamicPayMethodPayResultMap.containsKey(dynamicPayMethodId)) {
                        DynamicPayMethodPayResult dynamicPayMethodPayResult = dynamicPayMethodPayResultMap.get(dynamicPayMethodId);
                        dynamicPayMethodPayResult.setActualPayAmount(dynamicPayMethodPayResult.getActualPayAmount()+dynamicPayMethodActualAmount);
                        dynamicPayMethodPayResult.setPayAmount(dynamicPayMethodPayResult.getPayAmount()+dynamicPayMethodAmount);
                    } else {
                        DynamicPayMethodPayResult dynamicPayMethodPayResult = new DynamicPayMethodPayResult();
                        dynamicPayMethodPayResult.setDynamicPayMethodId(dynamicPayMethodId);
                        dynamicPayMethodPayResult.setDynamicPayMethodName(dynamicPayMethodName);
                        dynamicPayMethodPayResult.setActualPayAmount(dynamicPayMethodActualAmount);
                        dynamicPayMethodPayResult.setPayAmount(dynamicPayMethodAmount);
                        dynamicPayMethodPayResultMap.put(dynamicPayMethodId, dynamicPayMethodPayResult);
                    }
                }
            }
			payDetailInfoDTO.setDynamicPayDerate(payDetailInfoDTO.getDynamicPayDerate()+tableRecordPayDetailResult.getDynamicPayDerate());
			payDetailInfoDTO.setInternetRebatePrice(payDetailInfoDTO.getInternetRebatePrice()+tableRecordPayDetailResult.getInternetRebatePrice());
			payDetailInfoDTO.setEnterpriseRebatePrice(payDetailInfoDTO.getEnterpriseRebatePrice()+tableRecordPayDetailResult.getEnterpriseRebatePrice());
			payDetailInfoDTO.setMemberRebatePrice(payDetailInfoDTO.getMemberRebatePrice()+tableRecordPayDetailResult.getMemberRebatePrice());
			payDetailInfoDTO.setPromotionRebatePrice(payDetailInfoDTO.getPromotionRebatePrice()+tableRecordPayDetailResult.getPromotionRebatePrice());
			payDetailInfoDTO.setPromotionReducePrice(payDetailInfoDTO.getPromotionReducePrice()+tableRecordPayDetailResult.getPromotionReducePrice());
			payDetailInfoDTO.setGratisPrice(payDetailInfoDTO.getGratisPrice()+tableRecordPayDetailResult.getGratisPrice());
			payDetailInfoDTO.setPromotionPrice(payDetailInfoDTO.getPromotionPrice()+tableRecordPayDetailResult.getPromotionPrice());
		}
		
		long discountProAmount = 0L;
		long discountAmount = 0L;
		if (tableRecordAmountsResult != null) {
			discountProAmount = tableRecordAmountsResult.getDiscountProAmount();
			discountAmount = tableRecordAmountsResult.getDiscountAmount();
		}
		payDetailInfoDTO.setTotalDerate(discountAmount);
		payDetailInfoDTO.setTotalRebatePrice(discountProAmount);
		
		List<String> orderIds = new ArrayList<String>(subStoreOrders.size());
		for (StoreOrder subStoreOrder : subStoreOrders) {
			orderIds.add(subStoreOrder.getOrderId());
		}
		List<StoreOrderPromotion> storeOrderPromotions = storeOrderHelper.getStoreOrderPromotionsByOrderIds(
				storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), orderIds, StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue(), true);
		// 将相同的折扣活动进行合并
		Map<Long, StoreOrderPromotionDTO> map = new HashMap<Long, StoreOrderPromotionDTO>();
		for (StoreOrderPromotion storeOrderPromotion : storeOrderPromotions) {
			StoreOrderPromotionDTO promotion = null;
			if (map.containsKey(storeOrderPromotion.getPromotionId())) {
				promotion = map.get(storeOrderPromotion.getPromotionId());
				promotion.setPromotionDerate(promotion.getPromotionDerate()+storeOrderPromotion.getPromotionDerate());
			} else {
				promotion = new StoreOrderPromotionDTO();
				promotion.setMerchantId(storeTableRecord.getMerchantId());
				promotion.setStoreId(storeTableRecord.getStoreId());
				promotion.setPromotionId(storeOrderPromotion.getPromotionId());
				promotion.setPromotionType(storeOrderPromotion.getPromotionType());
				promotion.setPromotionTitle(storeOrderPromotion.getPromotionTitle());
				promotion.setPromotionDerate(storeOrderPromotion.getPromotionDerate());
			}
			map.put(storeOrderPromotion.getPromotionId(), promotion);
		}
		List<StoreOrderPromotionDTO> storeOrderPromotionDTOs = new ArrayList<StoreOrderPromotionDTO>(map.values());
		payDetailInfoDTO.setStoreOrderPromotionDTOs(storeOrderPromotionDTOs);
		payDetailInfoDTO.setReductionTableFee(storeTableRecord.getReductionTableFee());
		return payDetailInfoDTO;
	}
	
	/**
	 * 创建结账记录返回结果
	 * @param storeTableRecord
	 * @return
	 */
	public SettleTableRecordDTO createSettleRecord (StoreTableRecord storeTableRecord, TableRecordAmountsResult tableRecordAmountsResult, PayDetailInfoDTO payDetailInfoDTO) {
        SettleTableRecordDTO settleTableRecord = new SettleTableRecordDTO();
        settleTableRecord.setTotalAmount(tableRecordAmountsResult.getTotalAmount());
        settleTableRecord.setDiscountPro(tableRecordAmountsResult.getDiscountPro());
        settleTableRecord.setDiscountProAmount(tableRecordAmountsResult.getDiscountProAmount());
        settleTableRecord.setDiscountAmount(tableRecordAmountsResult.getDiscountAmount());
        settleTableRecord.setPaidAmount(tableRecordAmountsResult.getPaidAmount());
        settleTableRecord.setPayAbleAmount(tableRecordAmountsResult.getPayAbleAmount());
        settleTableRecord.setAlreadyDiscountAmount(tableRecordAmountsResult.getAlreadyDiscountAmount());
        settleTableRecord.setWaitSettleAmount(tableRecordAmountsResult.getWaitSettleAmount());
        settleTableRecord.setTableRecordId(storeTableRecord.getTableRecordId());
        settleTableRecord.setTableFee(storeTableRecord.getPayAbleTableFee());
        settleTableRecord.setReductionTableFee(storeTableRecord.getReductionTableFee());
        settleTableRecord.setPayDetailInfoDTO(payDetailInfoDTO);
        return settleTableRecord;
    }
	
	/**
	 * 构造桌台记录返回值
	 * @param storeTableRecord
	 * @throws TException
	 */
	public StoreTableRecordDTO buildTableRecordDTO (StoreTableRecord storeTableRecord, 
			List<TableRecordPayDetailResult> tableRecordPayDetailResultList, List<RefundDetailDTO> refundDetailDTOList, TableRecordAmountsResult tableRecordAmountsResult)
					throws TException {
		boolean enableSlave = false;
		// 复制桌台记录基础信息
		StoreTableRecordDTO storeTableRecordDTO = new StoreTableRecordDTO();
		BeanUtil.copy(storeTableRecord, storeTableRecordDTO);
		storeTableRecordDTO.setTableFee(storeTableRecord.getPayAbleTableFee());
		// 设置区域
		StoreArea storeArea = storeAreaService.getValidStoreAreaById(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getAreaId(), false);
		storeTableRecordDTO.setStoreAreaDTO(BeanUtil.copy(storeArea, StoreAreaDTO.class));
		// 设置桌台
		StoreTable storeTable = storeTableService.getValidStoreTableById(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getTableId(), false);
		storeTableRecordDTO.setStoreTableDTO(BeanUtil.copy(storeTable, StoreTableDTO.class));
		// 设置营业时段
		StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getTimeBucketId());
		storeTableRecordDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
		// 获取退菜记录
		List<StoreTableRecordRefund> storeTableRecordRefundList = storeTableRecordRefundDAO.getStoreTableRecordRefundsByTableRecordId(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getTableRecordId());
		// 设置服务员
		List<Long> staffIds = new ArrayList<Long>();
		if (storeTableRecord.getStaffId() > 0) {
			staffIds.add(storeTableRecord.getStaffId());
		}
		if (storeTableRecord.getCreateTableStaffId() > 0) {
			staffIds.add(storeTableRecord.getCreateTableStaffId());
		}
		if (storeTableRecord.getSettleStaffId() > 0) {
			staffIds.add(storeTableRecord.getSettleStaffId());
		}
		if (storeTableRecord.getClearTableStaffId() > 0) {
			staffIds.add(storeTableRecord.getClearTableStaffId());
		}
		if (storeTableRecord.getMergeStaffId() > 0) {
			staffIds.add(storeTableRecord.getMergeStaffId());
		}
		if (storeTableRecord.getTransferStaffId() > 0) {
			staffIds.add(storeTableRecord.getTransferStaffId());
		}
		for (StoreTableRecordRefund storeTableRecordRefund : storeTableRecordRefundList) {
			if (storeTableRecordRefund.getStaffId() > 0) {
				staffIds.add(storeTableRecordRefund.getStaffId());
			}
		}
		Map<Long, StaffDTO> staffDTOMap = new HashMap<Long, StaffDTO>();
		if (!staffIds.isEmpty()) {
			staffDTOMap = staffFacade.getStaffMapInIds(storeTableRecord.getMerchantId(), staffIds, true);
			if (storeTableRecord.getStaffId() > 0) { // 桌台默认服务员
				if (staffDTOMap.containsKey(storeTableRecord.getStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getStaffId());
					storeTableRecordDTO.setTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
			if (storeTableRecord.getCreateTableStaffId() > 0) { // 开台服务员
				if (staffDTOMap.containsKey(storeTableRecord.getCreateTableStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getCreateTableStaffId());
					storeTableRecordDTO.setCreateTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
			if (storeTableRecord.getSettleStaffId() > 0) { // 结账服务员
				if (staffDTOMap.containsKey(storeTableRecord.getSettleStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getSettleStaffId());
					storeTableRecordDTO.setSettleTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
			if (storeTableRecord.getClearTableStaffId() > 0) { // 清台服务员
				if (staffDTOMap.containsKey(storeTableRecord.getClearTableStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getClearTableStaffId());
					storeTableRecordDTO.setClearTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
			if (storeTableRecord.getMergeStaffId() > 0) { // 合台服务员
				if (staffDTOMap.containsKey(storeTableRecord.getMergeStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getMergeStaffId());
					storeTableRecordDTO.setMergeStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
			if (storeTableRecord.getTransferStaffId() > 0) { // 转台服务员
				if (staffDTOMap.containsKey(storeTableRecord.getMergeStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getMergeStaffId());
					storeTableRecordDTO.setTransferStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
		}
		// 设置用户
		List<Long> userIds = new ArrayList<Long>();
		if (storeTableRecord.getCreateTableUserId() > 0) {
			userIds.add(storeTableRecord.getCreateTableUserId());
		}
		if (storeTableRecord.getSettleUserId() > 0) {
			userIds.add(storeTableRecord.getSettleUserId());
		}
		if (!userIds.isEmpty()) {
			Map<Long, UserDTO> userDTOMap = userFacade.getUserMapByIds(userIds);
			if (storeTableRecord.getCreateTableUserId() > 0) { // 自助开台用户
				if (userDTOMap.containsKey(storeTableRecord.getCreateTableUserId())) {
					UserDTO userDTO = userDTOMap.get(storeTableRecord.getCreateTableUserId());
					storeTableRecordDTO.setCreateTableRecordUserDTO(BeanUtil.copy(userDTO, I5weiUserDTO.class));
				}
			}
			if (storeTableRecord.getSettleUserId() > 0) { // 自助结账用户
				if (userDTOMap.containsKey(storeTableRecord.getSettleUserId())) {
					UserDTO userDTO = userDTOMap.get(storeTableRecord.getSettleUserId());
					storeTableRecordDTO.setSettleTableRecordUserDTO(BeanUtil.copy(userDTO, I5weiUserDTO.class));
				}
			}
		}
		// 主订单&子订单
		List<StoreOrder> subStoreOrderList = new ArrayList<StoreOrder>();
		if (!StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
			StoreOrder masterStoreOrder = storeOrderService.getStoreOrderById(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getOrderId());
			String masterStoreOrderId = null;
			if (masterStoreOrder != null) {
				// 计算桌台记录退款状态，赋值给主订单
				if (storeTableRecord.getTotalRefundAmount() > 0) {
//					if (storeTableRecord.getTotalRefundAmount() == storeTableRecord.getPaidAmount()) {
//						masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
//					} else {
//						masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
//					}
					List<StoreOrder> subStoreOrders = this.storeOrderDAO.getSubStoreOrderByTableRecordId(masterStoreOrder.getMerchantId(), masterStoreOrder.getStoreId(), masterStoreOrder.getTableRecordId(), masterStoreOrder.getOrderId(), false);
					long tableActuralPayAmount = storeTableRecord.getStoreTableActualPrice(subStoreOrders, masterStoreOrder);
					List<TableRecordBatchRefundRecord> tableRecordBatchRefundRecords = tableRecordBatchRefundRecordDAO.getSuccessTableRecordBatchRefundByTableRecordId(masterStoreOrder.getTableRecordId());
					long actualRefundAmount = storeTableRecord.getStoreTableActualRefundAmount(tableRecordBatchRefundRecords);
					if (actualRefundAmount == tableActuralPayAmount) {
						masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
					} else if(actualRefundAmount < tableActuralPayAmount){
						masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
					}else {
						log.warn("storeOrder[" + masterStoreOrder.getOrderId() + "], merchantId[" + masterStoreOrder.getMerchantId() + "],storeId[" + masterStoreOrder.getStoreId() + "] refundAmount["+actualRefundAmount+"] > actualPayAmount["+tableActuralPayAmount+"]");
						masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
					}
				} else {
					masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.NOT.getValue());
				}
				StoreOrderDTO masterStoreOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(masterStoreOrder);
				storeTableRecordDTO.setMasterStoreOrderDTO(masterStoreOrderDTO);
				masterStoreOrderId = masterStoreOrder.getOrderId();
			} 
			// 子订单列表
			subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getTableRecordId(), masterStoreOrderId, false);
			if (subStoreOrderList != null && !subStoreOrderList.isEmpty()) {
				storeOrderHelper.setStoreOrderDetail(subStoreOrderList, enableSlave);
				storeOrderHelper.setStoreOrderTimes(subStoreOrderList, enableSlave);
				storeOrderHelper.setStoreOrderRefundItem(subStoreOrderList, enableSlave);
				List<StoreOrderDTO> storeOrderList = storeOrderHelper.getStoreOrderDTOByEntity(subStoreOrderList);
				storeTableRecordDTO.setSubStoreOrderDTOList(storeOrderList);
			} else {
				storeTableRecordDTO.setSubStoreOrderDTOList(new ArrayList<>());
			}
		}
		if (storeTableRecordRefundList != null && !storeTableRecordRefundList.isEmpty()) {
			List<StoreTableRecordRefundDTO> storeTableRecordRefundDTOs = BeanUtil.copyList(storeTableRecordRefundList, StoreTableRecordRefundDTO.class);
			for (StoreTableRecordRefundDTO storeTableRecordRefundDTO : storeTableRecordRefundDTOs) {
				if (storeTableRecordRefundDTO.getStaffId() > 0) {
					if (staffDTOMap.containsKey(storeTableRecordRefundDTO.getStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecordRefundDTO.getStaffId());
						storeTableRecordRefundDTO.setStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
			storeTableRecordDTO.setStoreTableRecordRefundDTOList(storeTableRecordRefundDTOs);
		} else {
			storeTableRecordDTO.setStoreTableRecordRefundDTOList(new ArrayList<>());
		}
		// 桌台记录支付详情
		if (tableRecordPayDetailResultList != null && !tableRecordPayDetailResultList.isEmpty()) {
			PayDetailInfoDTO payDetailInfoDTO = this.buildPayDetailInfoDTO(tableRecordPayDetailResultList, tableRecordAmountsResult, subStoreOrderList, storeTableRecord);
			storeTableRecordDTO.setPayDetailInfoDTO(payDetailInfoDTO);
		}
		// 桌台记录退款详情
		if (refundDetailDTOList != null && !refundDetailDTOList.isEmpty()) {
			RefundDetailInfoDTO refundDetailInfoDTO = this.buildRefundDetailInfoDTO(refundDetailDTOList);
			storeTableRecordDTO.setRefundDetailInfoDTO(refundDetailInfoDTO);
		}
		return storeTableRecordDTO;
	}
	
	private RefundDetailInfoDTO buildRefundDetailInfoDTO (List<RefundDetailDTO> refundDetailDTOList) {
		RefundDetailInfoDTO refundDetailInfoDTO = new RefundDetailInfoDTO();
		for (RefundDetailDTO refundDetailDTO : refundDetailDTOList) {
			refundDetailInfoDTO.setCashRefund(refundDetailInfoDTO.getCashRefund()+refundDetailDTO.getCashRefund());
			refundDetailInfoDTO.setCouponRefund(refundDetailInfoDTO.getCouponRefund()+refundDetailDTO.getCouponRefund());
			refundDetailInfoDTO.setPrepaidcardRefund(refundDetailInfoDTO.getPrepaidcardRefund()+refundDetailDTO.getPrepaidRefund());
			refundDetailInfoDTO.setUserAccountRefund(refundDetailInfoDTO.getUserAccountRefund()+refundDetailDTO.getAccountRefund());
			refundDetailInfoDTO.setYjpayRefund(refundDetailInfoDTO.getYjpayRefund()+refundDetailDTO.getYjRefund());
			refundDetailInfoDTO.setWechatRefund(refundDetailInfoDTO.getWechatRefund()+refundDetailDTO.getWechatRefund());
			refundDetailInfoDTO.setIposRefund(refundDetailInfoDTO.getIposRefund()+refundDetailDTO.getIposRefund());
			refundDetailInfoDTO.setPosRefund(refundDetailInfoDTO.getPosRefund()+0);// 没有普通pos退款
			refundDetailInfoDTO.setPublicTransferRefund(refundDetailInfoDTO.getPublicTransferRefund()+0);// 没有对公转账退款
			refundDetailInfoDTO.setAliPayRefund(refundDetailInfoDTO.getAliPayRefund()+refundDetailDTO.getAlipayRefund());
			refundDetailInfoDTO.setIboxPayRefund(refundDetailInfoDTO.getIboxPayRefund()+0);// 没有盒子支付退款
			refundDetailInfoDTO.setCancelCredit(0);
			if (refundDetailInfoDTO.getDynamicPayMethodRefundResults() == null) {
				refundDetailInfoDTO.setDynamicPayMethodRefundResults(new HashMap<>());
			}
			Map<Integer, DynamicPayMethodRefundResult> dynamicPayMethodRefundResults = refundDetailInfoDTO.getDynamicPayMethodRefundResults();
            // add by wxy 后付费会对自定义券进行合并显示
            List<DynamicPayMethodRefundInfo> dynamicPayMethodRefundInfos = refundDetailDTO.getDynamicPayMethodRefundInfos();
            if(dynamicPayMethodRefundInfos != null){
                for(DynamicPayMethodRefundInfo dynamicPayMethodRefundInfo : dynamicPayMethodRefundInfos){
                    int dynamicPayMethodId = dynamicPayMethodRefundInfo.getDynamicPayMethod();
                    String dynamicPayMethodName = dynamicPayMethodRefundInfo.getVoucherName();
                    long dynamicPayMethodRefund = dynamicPayMethodRefundInfo.getVoucherRefund();
                    long dynamicPayMethodFaceValue = dynamicPayMethodRefundInfo.getVoucherFaceValue();
                    if (dynamicPayMethodRefundResults.containsKey(dynamicPayMethodId)) {
                        DynamicPayMethodRefundResult dynamicPayMethodRefundResult = dynamicPayMethodRefundResults.get(dynamicPayMethodId);
                        dynamicPayMethodRefundResult.setActualRefundAmount(dynamicPayMethodRefundResult.getActualRefundAmount()+dynamicPayMethodRefund);
                        dynamicPayMethodRefundResult.setRefundAmount(dynamicPayMethodRefundResult.getRefundAmount()+dynamicPayMethodFaceValue);
                    } else {
                        DynamicPayMethodRefundResult dynamicPayMethodRefundResult = new DynamicPayMethodRefundResult();
                        dynamicPayMethodRefundResult.setDynamicPayMethodId(dynamicPayMethodId);
                        dynamicPayMethodRefundResult.setDynamicPayMethodName(dynamicPayMethodName);
                        dynamicPayMethodRefundResult.setActualRefundAmount(dynamicPayMethodRefund);
                        dynamicPayMethodRefundResult.setRefundAmount(dynamicPayMethodFaceValue);
                        dynamicPayMethodRefundResults.put(dynamicPayMethodId, dynamicPayMethodRefundResult);
                    }
                }
            }
		}
		return refundDetailInfoDTO;
	}
	
	/**
	 * 用于计算double类型的百分比
	 * @param v1
	 * @param v2
	 * @param scale
	 * @return
	 */
	public double div(double v1, int v2, int scale) {
        if (scale < 0) {
            throw new RuntimeException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
	
	private long nextId() {
		try {
        	return this.idMakerUtil.nextId2(TABLE_RECORDID_ID);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }
	
	private StoreTableStaffDTO getStoreTableStaffDTO (StaffDTO staffDTO) {
		StoreTableStaffDTO storeTableStaffDTO = new StoreTableStaffDTO();
		storeTableStaffDTO.setUserId(staffDTO.getUserId());
		storeTableStaffDTO.setStaffId(staffDTO.getStaffId());
		storeTableStaffDTO.setAliasName(staffDTO.getAliasName());
		storeTableStaffDTO.setMerchantId(staffDTO.getMerchantId());
		storeTableStaffDTO.setStatus(staffDTO.getStatus());
		storeTableStaffDTO.setCreateTime(staffDTO.getCreateTime());
		storeTableStaffDTO.setUpdateTime(staffDTO.getUpdateTime());
		storeTableStaffDTO.setPwd(staffDTO.getPwd());
		storeTableStaffDTO.setPostQuantity(staffDTO.getPostQuantity());
		storeTableStaffDTO.setShowName(staffDTO.getShowName());
		UserDTO userDTO = staffDTO.getUserDTO();
		if (userDTO != null) {
			I5weiUserDTO i5weiUserDTO = BeanUtil.copy(userDTO, I5weiUserDTO.class);
			storeTableStaffDTO.setI5weiUserDTO(i5weiUserDTO);
		}
		return storeTableStaffDTO;
	}

	/**
	 * 初始化queryLoadCodes
	 */
	static {
		//加载桌台记录
		List<Integer> loadTableRecordCodeList = Lists.newArrayList();
		loadTableRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue(), loadTableRecordCodeList);
		//加载桌台区域
		List<Integer> loadStoreAreaCodeList = Lists.newArrayList();
		loadStoreAreaCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadStoreAreaCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue(), loadStoreAreaCodeList);
		//加载桌台
		List<Integer> loadStoreTableCodeList = Lists.newArrayList();
		loadStoreTableCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadStoreTableCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue(), loadStoreTableCodeList);
		//加载营业时段
		List<Integer> loadTimeBucketCodeList = Lists.newArrayList();
		loadTimeBucketCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTimeBucketCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue(), loadTimeBucketCodeList);
		//加载默认服务员
		List<Integer> loadTableStaffCodeList = Lists.newArrayList();
		loadTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue(), loadTableStaffCodeList);
		//加载开台服务员
		List<Integer> loadCreateTableStaffCodeList = Lists.newArrayList();
		loadCreateTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadCreateTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue(), loadCreateTableStaffCodeList);
		//加载清台服务员
		List<Integer> loadClearTableStaffCodeList = Lists.newArrayList();
		loadClearTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadClearTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue(), loadClearTableStaffCodeList);
		//加载结账服务员
		List<Integer> loadSettleStaffCodeList = Lists.newArrayList();
		loadSettleStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadSettleStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue(), loadSettleStaffCodeList);
		//加载合台服务员
		List<Integer> loadMergeTableStaffCodeList = Lists.newArrayList();
		loadMergeTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadMergeTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue(), loadMergeTableStaffCodeList);
		//加载转台服务员
		List<Integer> loadTransferTableStaffCodeList = Lists.newArrayList();
		loadTransferTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTransferTableStaffCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue(), loadTransferTableStaffCodeList);
		//加载自助开台用户
		List<Integer> loadCreateTableUserCodeList = Lists.newArrayList();
		loadCreateTableUserCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadCreateTableUserCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue(), loadCreateTableUserCodeList);
		//加载自助结账用户
		List<Integer> loadSettleTableUserCodeList = Lists.newArrayList();
		loadSettleTableUserCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadSettleTableUserCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue(), loadSettleTableUserCodeList);
		//加载桌台记录主订单
		List<Integer> loadTableRecordMasterOrderCodeList = Lists.newArrayList();
		loadTableRecordMasterOrderCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordMasterOrderCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue(), loadTableRecordMasterOrderCodeList);
		//加载桌台记录子订单订单
		List<Integer> loadTableRecordSubOrderCodeList = Lists.newArrayList();
		loadTableRecordSubOrderCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordSubOrderCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadTableRecordSubOrderCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue(), loadTableRecordSubOrderCodeList);
		//加载桌台退菜项
		List<Integer> loadTableRecordItemCodeList = Lists.newArrayList();
		loadTableRecordItemCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordItemCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue(), loadTableRecordItemCodeList);
		//加载桌台支付详情
		List<Integer> loadTableRecordPayDetailCodeList = Lists.newArrayList();
		loadTableRecordPayDetailCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordPayDetailCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadTableRecordPayDetailCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadTableRecordPayDetailCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue(), loadTableRecordPayDetailCodeList);
		//加载桌台退款详情
		List<Integer> loadTableRecordRefundDetailCodeList = Lists.newArrayList();
		loadTableRecordRefundDetailCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordRefundDetailCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue(), loadTableRecordRefundDetailCodeList);
		//加载桌台结账单
		List<Integer> loadTableRecordSettleRecordCodeList = Lists.newArrayList();
		loadTableRecordSettleRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordSettleRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadTableRecordSettleRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadTableRecordSettleRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		loadTableRecordSettleRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		loadTableRecordSettleRecordCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue(), loadTableRecordSettleRecordCodeList);
		//加载桌台记录二维码
		List<Integer> loadTableRecordQrcodeCodeList = Lists.newArrayList();
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue());
		loadTableRecordQrcodeCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_QRCODE.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_QRCODE.getValue(), loadTableRecordQrcodeCodeList);
		//加载桌台记录的optlog
		List<Integer> loadTableRecordOptlogCodeList = Lists.newArrayList();
		loadTableRecordOptlogCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadTableRecordOptlogCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_OPTLOG.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_OPTLOG.getValue(), loadTableRecordOptlogCodeList);

		//加载所有
		List<Integer> loadAllCodeList = Lists.newArrayList();
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_QRCODE.getValue());
		loadAllCodeList.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_OPTLOG.getValue());
		queryLoadCodesMap.put(TableRecordDetailLoadCodeEnum.LOAD_ALL.getValue(), loadAllCodeList);
	}

	/**
	 * 根据传入的takeCodes 得到其中的依赖关系得到桌台真正要加载loadCodes
	 * @param loadCodes 前端传入的loadCodes
	 * @return
	 */
	public static List<Integer> handleLoadCodes(List<Integer> loadCodes) {
		Set<Integer> queryLoadCodeSet = new HashSet<>();

		for (Integer loadCode : loadCodes) {
			List<Integer> codes = queryLoadCodesMap.get(loadCode);
			if (codes != null && !codes.isEmpty()) {
				queryLoadCodeSet.addAll(codes);
			}
		}
		return queryLoadCodeSet.stream().collect(Collectors.toList());
	}

	/**
	 * 根据loadCodes 加载前端需要的数据
	 * @param loadCodes 请求加载数据的loadCodes
	 * @param merchantId 商户编号
	 * @param storeId 店铺编号
	 * @param tableRecordId 桌台编号
	 * <p>
	 * <br/> storeTableRecord 桌台记录
	 * <br/> tableRecordPayDetailResultList 桌台记录上包含的每笔支付的金额详情
	 * <br/> refundDetailDTOList 桌台记录上包含的每笔退款的金额详情
	 * <br/> tableRecordAmountsResult 桌台记录的各项金额
	 * </p>
	 * @return 桌台记录的DTO
	 */
	public StoreTableRecordDTO getTableRecordDetailByLoadCodes(List<Integer> loadCodes, int merchantId, long storeId, long tableRecordId) throws TException {

		boolean enableSlave = false;
		StoreTableRecord storeTableRecord = storeTableRecordService.getTableRecordDetail(merchantId, storeId, tableRecordId);
		StoreTableRecordDTO storeTableRecordDTO = new StoreTableRecordDTO();
		//加载桌台记录
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue())) {
			// 复制桌台记录基础信息
			BeanUtil.copy(storeTableRecord, storeTableRecordDTO);
			storeTableRecordDTO.setTableFee(storeTableRecord.getPayAbleTableFee());
		}
		//加载区域
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue())) {
			StoreArea storeArea = storeAreaService.getValidStoreAreaById(merchantId, storeId, storeTableRecord.getAreaId(), false);
			storeTableRecordDTO.setStoreAreaDTO(BeanUtil.copy(storeArea, StoreAreaDTO.class));
		}
		//加载桌台
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue())) {
			StoreTable storeTable = storeTableService.getValidStoreTableById(merchantId, storeId, storeTableRecord.getTableId(), false);
			storeTableRecordDTO.setStoreTableDTO(BeanUtil.copy(storeTable, StoreTableDTO.class));
		}
		//加载营业时段
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue())) {
			StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, storeTableRecord.getTimeBucketId());
			storeTableRecordDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
		}
		//设置需要获取的服务员
		Set<Long> staffIds = new HashSet<>();
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue())) {
			if (storeTableRecord.getStaffId() > 0) {
				staffIds.add(storeTableRecord.getStaffId());
			}
		}
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue())) {
			if (storeTableRecord.getCreateTableStaffId() > 0) {
				staffIds.add(storeTableRecord.getCreateTableStaffId());
			}
		}
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue())) {
			if (storeTableRecord.getClearTableStaffId() > 0) {
				staffIds.add(storeTableRecord.getClearTableStaffId());
			}
		}
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue())) {
			if (storeTableRecord.getSettleStaffId() > 0) {
				staffIds.add(storeTableRecord.getSettleStaffId());
			}
		}
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue())) {
			if (storeTableRecord.getMergeStaffId() > 0) {
				staffIds.add(storeTableRecord.getMergeStaffId());
			}
		}
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue())) {
			if (storeTableRecord.getTransferStaffId() > 0) {
				staffIds.add(storeTableRecord.getTransferStaffId());
			}
		}
		//判断是否加载退菜列表,获得退菜时的服务员
		List<StoreTableRecordRefund> storeTableRecordRefundItemList = Lists.newArrayList();
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue())) {
			storeTableRecordRefundItemList = storeTableRecordRefundDAO.getStoreTableRecordRefundsByTableRecordId(merchantId, storeId, tableRecordId);
			staffIds.addAll(storeTableRecordRefundItemList.stream().filter(storeTableRecordRefund -> storeTableRecordRefund.getStaffId() > 0).map(StoreTableRecordRefund::getStaffId).collect(Collectors.toList()));
		}
		Map<Long, StaffDTO> staffDTOMap = Maps.newHashMap();
		if (!staffIds.isEmpty()) {
			List<Long> queryStaffIds = Lists.newArrayList();
			queryStaffIds.addAll(staffIds);
			staffDTOMap = staffFacade.getStaffMapInIds(storeTableRecord.getMerchantId(), queryStaffIds, true);
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue())) {
				if (storeTableRecord.getStaffId() > 0) { // 桌台默认服务员
					if (staffDTOMap.containsKey(storeTableRecord.getStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getStaffId());
						storeTableRecordDTO.setTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue())) {
				if (storeTableRecord.getCreateTableStaffId() > 0) { // 开台服务员
					if (staffDTOMap.containsKey(storeTableRecord.getCreateTableStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getCreateTableStaffId());
						storeTableRecordDTO.setCreateTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue())) {
				if (storeTableRecord.getClearTableStaffId() > 0) { // 清台服务员
					if (staffDTOMap.containsKey(storeTableRecord.getClearTableStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getClearTableStaffId());
						storeTableRecordDTO.setClearTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue())) {
				if (storeTableRecord.getSettleStaffId() > 0) { // 结账服务员
					if (staffDTOMap.containsKey(storeTableRecord.getSettleStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getSettleStaffId());
						storeTableRecordDTO.setSettleTableRecordStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue())) {
				if (storeTableRecord.getMergeStaffId() > 0) { // 合台服务员
					if (staffDTOMap.containsKey(storeTableRecord.getMergeStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getMergeStaffId());
						storeTableRecordDTO.setMergeStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue())) {
				if (storeTableRecord.getTransferStaffId() > 0) { // 转台服务员
					if (staffDTOMap.containsKey(storeTableRecord.getMergeStaffId())) {
						StaffDTO staffDTO = staffDTOMap.get(storeTableRecord.getMergeStaffId());
						storeTableRecordDTO.setTransferStaffDTO(this.getStoreTableStaffDTO(staffDTO));
					}
				}
			}
		}
		//加载用户
		Set<Long> userIds = new HashSet<>();
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue())) {
			if (storeTableRecord.getCreateTableUserId() > 0) {
				userIds.add(storeTableRecord.getCreateTableUserId());
			}
		}
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue())) {
			if (storeTableRecord.getSettleUserId() > 0) {
				userIds.add(storeTableRecord.getSettleUserId());
			}
		}
		if (!userIds.isEmpty()) {
			List<Long> queryUserIds = userIds.stream().collect(Collectors.toList());
			Map<Long, UserDTO> userDTOMap = userFacade.getUserMapByIds(queryUserIds);
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue())) {
				if (storeTableRecord.getCreateTableUserId() > 0) { // 自助开台用户
					if (userDTOMap.containsKey(storeTableRecord.getCreateTableUserId())) {
						UserDTO userDTO = userDTOMap.get(storeTableRecord.getCreateTableUserId());
						storeTableRecordDTO.setCreateTableRecordUserDTO(BeanUtil.copy(userDTO, I5weiUserDTO.class));
					}
				}
			}
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue())) {
				if (storeTableRecord.getSettleUserId() > 0) { // 自助结账用户
					if (userDTOMap.containsKey(storeTableRecord.getSettleUserId())) {
						UserDTO userDTO = userDTOMap.get(storeTableRecord.getSettleUserId());
						storeTableRecordDTO.setSettleTableRecordUserDTO(BeanUtil.copy(userDTO, I5weiUserDTO.class));
					}
				}
			}
		}
		//加载主订单
		List<StoreOrder> subStoreOrderList = Lists.newArrayList();
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue())) {
			if (!StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
				StoreOrder masterStoreOrder = storeOrderService.getStoreOrderById(merchantId, storeId, storeTableRecord.getOrderId());
				String masterStoreOrderId = null;
				if (masterStoreOrder != null) {
					// 计算桌台记录退款状态，赋值给主订单
					if (storeTableRecord.getTotalRefundAmount() > 0) {
						List<StoreOrder> subStoreOrders = this.storeOrderDAO.getSubStoreOrderByTableRecordId(masterStoreOrder.getMerchantId(), masterStoreOrder.getStoreId(), masterStoreOrder.getTableRecordId(), masterStoreOrder.getOrderId(), false);
						long tableActuralPayAmount = storeTableRecord.getStoreTableActualPrice(subStoreOrders, masterStoreOrder);
						List<TableRecordBatchRefundRecord> tableRecordBatchRefundRecords = tableRecordBatchRefundRecordDAO.getSuccessTableRecordBatchRefundByTableRecordId(masterStoreOrder.getTableRecordId());
						long actualRefundAmount = storeTableRecord.getStoreTableActualRefundAmount(tableRecordBatchRefundRecords);
						if (actualRefundAmount == tableActuralPayAmount) {
							masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
						} else if(actualRefundAmount < tableActuralPayAmount){
							masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
						}else {
							log.warn("storeOrder[" + masterStoreOrder.getOrderId() + "], merchantId[" + masterStoreOrder.getMerchantId() + "],storeId[" + masterStoreOrder.getStoreId() + "] refundAmount["+actualRefundAmount+"] > actualPayAmount["+tableActuralPayAmount+"]");
							masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
						}
					} else {
						masterStoreOrder.setRefundStatus(StoreOrderRefundStatusEnum.NOT.getValue());
					}
					StoreOrderDTO masterStoreOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(masterStoreOrder);
					storeTableRecordDTO.setMasterStoreOrderDTO(masterStoreOrderDTO);
					masterStoreOrderId = masterStoreOrder.getOrderId();
				}
				if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue())) {
					// 子订单列表
					subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrderId, false);
					if (subStoreOrderList != null && !subStoreOrderList.isEmpty()) {
						storeOrderHelper.setStoreOrderDetail(subStoreOrderList, enableSlave);
						storeOrderHelper.setStoreOrderTimes(subStoreOrderList, enableSlave);
						storeOrderHelper.setStoreOrderRefundItem(subStoreOrderList, enableSlave);
						List<StoreOrderDTO> storeOrderList = storeOrderHelper.getStoreOrderDTOByEntity(subStoreOrderList);
						storeTableRecordDTO.setSubStoreOrderDTOList(storeOrderList);
					} else {
						storeTableRecordDTO.setSubStoreOrderDTOList(new ArrayList<>());
					}
				}
			}
		}
		//加载桌台退菜项
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue())) {
			if (!storeTableRecordRefundItemList.isEmpty()) {
				List<StoreTableRecordRefundDTO> storeTableRecordRefundDTOs = BeanUtil.copyList(storeTableRecordRefundItemList, StoreTableRecordRefundDTO.class);
				for (StoreTableRecordRefundDTO storeTableRecordRefundDTO : storeTableRecordRefundDTOs) {
					if (storeTableRecordRefundDTO.getStaffId() > 0) {
						if (staffDTOMap.containsKey(storeTableRecordRefundDTO.getStaffId())) {
							StaffDTO staffDTO = staffDTOMap.get(storeTableRecordRefundDTO.getStaffId());
							storeTableRecordRefundDTO.setStaffDTO(this.getStoreTableStaffDTO(staffDTO));
						}
					}
				}
				storeTableRecordDTO.setStoreTableRecordRefundDTOList(storeTableRecordRefundDTOs);
			} else {
				storeTableRecordDTO.setStoreTableRecordRefundDTOList(new ArrayList<>());
			}
		}
		TableRecordAmountsResult tableRecordAmountsResult = null;
		List<RefundDetailDTO> refundDetailDTOList = Lists.newArrayList();
		//桌台记录支付详情
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue())) {
			TableRecordPayStatusResult tableRecordPayStatusInfo = storeTableRecordService.getSettleRecord(merchantId, storeId, storeTableRecord);
			List<TableRecordPayDetailResult> tableRecordPayDetailResultList = tableRecordPayStatusInfo.getPayDetailResulttList();
			refundDetailDTOList = tableRecordPayStatusInfo.getRefundResultList();
			tableRecordAmountsResult = storeTableRecordService.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);

			if (tableRecordPayDetailResultList != null && !tableRecordPayDetailResultList.isEmpty()) {
				PayDetailInfoDTO payDetailInfoDTO = this.buildPayDetailInfoDTO(tableRecordPayDetailResultList, tableRecordAmountsResult, subStoreOrderList, storeTableRecord);
				storeTableRecordDTO.setPayDetailInfoDTO(payDetailInfoDTO);
			}
		}
		//加载桌台退款详情
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue())) {
			RefundDetailInfoDTO refundDetailInfoDTO = this.buildRefundDetailInfoDTO(refundDetailDTOList);
			storeTableRecordDTO.setRefundDetailInfoDTO(refundDetailInfoDTO);
		}
		//加载结账单
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue())) {
			if (tableRecordAmountsResult == null) {
					throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(),
							"merchantId[" + merchantId + "] storeId[" + storeId + "]  tableRecordId[" + tableRecordId + "], load settle_record must load pay_detail");
			}
			PayDetailInfoDTO payDetailInfoDTO = storeTableRecordDTO.getPayDetailInfoDTO();
			SettleTableRecordDTO settleTableRecordDTO = createSettleRecord(storeTableRecord, tableRecordAmountsResult, payDetailInfoDTO);
			//加载桌台记录二维码
			String urlCode = "";
			String linkCode = "";
			if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_QRCODE.getValue())) {
				Map<String, String> qrcodeMap = storeTableQrcodeService.getTableRecordQrcode(merchantId, storeId, tableRecordId);
				urlCode = qrcodeMap.get("qrcode_url");
				linkCode = qrcodeMap.get("qrcode_link");
			}
			settleTableRecordDTO.setUrlCode(urlCode);
			settleTableRecordDTO.setLinkCode(linkCode);
			storeTableRecordDTO.setSettleTableRecordDTO(settleTableRecordDTO);
		}
		//加载桌台记录的日志 TODO 可以考虑把合台的桌台记录日志也拿出来
		if (loadCodes.contains(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_OPTLOG.getValue())) {
			List<StoreTableRecordOptlog> storeTableRecordOptlogs = storeTableRecordOptlogService.queryStoreTableRecordOptlog(merchantId, storeId, tableRecordId);
			List<StoreTableRecordOptlogDTO> storeTableRecordOptlogDTOs = storeTableRecordOptlogs.stream().map(this::buildStoreTableRecordOptlogDTO).collect(Collectors.toList());
			storeTableRecordDTO.setStoreTableRecordOptlogDTOs(storeTableRecordOptlogDTOs);
		}

		return storeTableRecordDTO;
	}

	public StoreTableRecordOptlogDTO buildStoreTableRecordOptlogDTO(StoreTableRecordOptlog storeTableRecordOptlog) {
		StoreTableRecordOptlogDTO dto = new StoreTableRecordOptlogDTO();
		BeanUtil.copy(storeTableRecordOptlog, dto);
		return dto;
	}
}
