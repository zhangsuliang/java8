package com.huofu.module.i5wei.table.facade;


import com.google.common.collect.Lists;
import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundRecord;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderRefundService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.printer.*;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTable;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.entity.TableRecordBatchRefundRecord;
import com.huofu.module.i5wei.table.service.*;
import com.huofu.module.i5wei.wechat.WechatNotifyService;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.dialog.tweet.TweetEventType;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofucore.facade.i5wei.order.StoreOrderRefundModeParam;
import huofucore.facade.i5wei.order.StoreOrderRefundStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderRefundVersion;
import huofucore.facade.i5wei.sharedto.I5weiUserDTO;
import huofucore.facade.i5wei.table.*;
import huofucore.facade.i5wei.table.RefundResultDTO;
import huofucore.facade.merchant.staff.StaffDTO;
import huofucore.facade.merchant.staff.StaffFacade;
import huofucore.facade.notify.NotifyFacade;
import huofucore.facade.pay.payment.*;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 桌台记录相关接口实现类
 * @author licheng7
 * 2016年4月27日 上午9:27:52
 */
@Component
@ThriftServlet(name = "storeTableRecordFacadeServlet", serviceClass = StoreTableRecordFacade.class)
public class StoreTableRecordFacadeImpl implements StoreTableRecordFacade.Iface{

	@ThriftClient
	private StaffFacade.Iface staffFacade;
	
	@Autowired
	private StoreTableRecordService storeTableRecordService;
	
	@Autowired
	private StoreOrderDAO storeOrderDAO;
	
	@Autowired
	private StoreOrderHelper storeOrderHelper;
	
	@Autowired
	private StoreTableRecordDAO storeTableRecordDAO;
	
	@Autowired
	private StoreTableRecordFacadeValidate storeTableRecordFacadeValidate;
	
	@Autowired
	private StoreTableRecordHelper storeTableRecordHelper;
	
	@Autowired
	private StoreTableQrcodeService storeTableQrcodeService;
	
	@ThriftClient
	private UserFacade.Iface userFacade;
	
	@Autowired
	private I5weiRefundMealPrinter i5weiRefundMealPrinter;
	
	@Autowired
	private I5weiMergeTablePrinter i5weiMergeTablePrinter;
	
	@Autowired
	private I5weiTransTablePrinter i5weiTransTablePrinter;
	
	@Autowired
	private WechatNotifyService wechatNotifyService;
	
	@Autowired
    private I5weiMessageProducer i5weiMessageProducer;
	
	@Autowired
	private StoreOrderRefundService storeOrderRefundService;
	
	@ThriftClient
    private NotifyFacade.Iface notifyFacade;
	
	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private I5weiTakeCodePrinter i5weiTakeCodePrinter;

	@Autowired
	private Store5weiSettingService store5weiSettingService;
	
	@Autowired
	private StoreHeartbeatService storeHeartbeatService;

	@Autowired
	private I5weiKitchenMealListPrinter i5weiKitchenMealListPrinter;

	@Autowired
	private I5weiTakeAndSendOutPrinter i5weiTakeAndSendOutPrinter;

	private static final Log log = LogFactory.getLog(StoreTableRecordFacadeImpl.class);
	
	
	/**
	 * 开台、拼台(关联取餐码开台)
	 */
	@Override
	public StoreTableRecordDTO createTableRecordWithTakeCodes(CreateTableRecordWithTakeCodesParam createTableRecordWithTakeCodesParam)
			throws  TException {
		storeTableRecordFacadeValidate.checkCreateTableRecordParam(createTableRecordWithTakeCodesParam);
		OpenTableRecordResult openTableRecordResult = storeTableRecordService.createTableRecordWithTakeCodes(createTableRecordWithTakeCodesParam);
		StoreTableRecord storeTableRecord = openTableRecordResult.getStoreTableRecord();
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTO(storeTableRecord);
		// 发送开台消息
		long userId = createTableRecordWithTakeCodesParam.getUserId();
		afterOpenTable(openTableRecordResult, storeTableRecord, userId);
		return storeTableRecordDTO;
	}

	public void afterOpenTable(OpenTableRecordResult openTableRecordResult, StoreTableRecord storeTableRecord, long userId) throws T5weiException {
		if (userId == 0) {
			if (openTableRecordResult.getStoreOrders()!=null && !openTableRecordResult.getStoreOrders().isEmpty()) {
				userId = openTableRecordResult.getStoreOrders().get(0).getUserId();
			}
		}
		try {
			wechatNotifyService.notifyOpenTableRecordMsg(storeTableRecord, userId);
		} catch (Exception e) {
			log.warn("send open storeTableRecord["+storeTableRecord+"] msg to user["+userId+"] fail");
		}
		// 发送交互消息
		List<StoreOrder> storeOrders = openTableRecordResult.getStoreOrders();
		for (StoreOrder storeOrder : storeOrders) {
			// 取餐票打印
			storeOrderHelper.insertCashierPrintOrder(storeOrder);
			// 客户单(桌台模式)
			i5weiTakeCodePrinter.sendPrintMessages(storeOrder);
			// 打包清单
			i5weiTakeAndSendOutPrinter.sendPrintMessages(storeOrder);
			i5weiMessageProducer.sendMessageOfStoreOrderEvent(storeOrder, 0, TweetEventType.PAY_ORDER, "订单消费");
			// 更改统计信息为订单交易中
			if (storeOrder.isPayFinish()) {
	            // 发送入账通知
	            i5weiMessageProducer.sendMessageOfMerchantAccounted(storeOrder);
	            // 发送统计通知
	            i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(storeOrder);
	        }
		}
		//客户取餐计入消费次数统计
		storeOrderHelper.accumulateStoreUserOrders(openTableRecordResult.getMasterOrder());
	}

	public StoreTableRecordDTO buildStoreTableRecordDTO(StoreTableRecord storeTableRecord) throws TException {
//		TableRecordPayStatusResult tableRecordPayStatusInfo = storeTableRecord.getTableRecordPayStatusInfo();
//		if (tableRecordPayStatusInfo == null) {
//			tableRecordPayStatusInfo = storeTableRecordService.getSettleRecord(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord);
//		}
//		List<TableRecordPayDetailResult> tableRecordPayDetailResultList = tableRecordPayStatusInfo.getPayDetailResulttList();
//		List<RefundDetailDTO> refundDetailDTOList = tableRecordPayStatusInfo.getRefundResultList();
//		TableRecordAmountsResult tableRecordAmountsResult = storeTableRecordService.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);
//		return storeTableRecordHelper.buildTableRecordDTO(storeTableRecord, tableRecordPayDetailResultList, refundDetailDTOList, tableRecordAmountsResult);

		TableRecordDetailParam param = new TableRecordDetailParam();
		param.setMerchantId(storeTableRecord.getMerchantId());
		param.setStoreId(storeTableRecord.getStoreId());
		param.setTableRecordId(storeTableRecord.getTableRecordId());
		List<Integer> loadCodes = Lists.newArrayList();
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		param.setLoadCodes(loadCodes);
		return getStoreTableRecordDetail(param);
	}

	/**
	 * 开台、拼台(关联订单号开台)
	 */
	@Override
	public StoreTableRecordDTO createTableRecordWithOrderIds(CreateTableRecordWithOrderIdsParam createTableRecordWithOrderIdsParam)
			throws TException {
		storeTableRecordFacadeValidate.checkCreateTableRecordParam(createTableRecordWithOrderIdsParam);
		OpenTableRecordResult openTableRecordResult = storeTableRecordService.createTableRecordWithOrderIds(createTableRecordWithOrderIdsParam);
		StoreTableRecord storeTableRecord = openTableRecordResult.getStoreTableRecord();
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTO(storeTableRecord);
		// 发送开台消息
		long userId = createTableRecordWithOrderIdsParam.getUserId();
		afterOpenTable(openTableRecordResult, storeTableRecord, userId);
		return storeTableRecordDTO;
	}

	/**
	 * 清台
	 */
	@Override
	public StoreTableRecordDTO clearTableRecord(ClearTableRecordParam clearTableRecordParam) throws TException {
		storeTableRecordFacadeValidate.checkClearTableRecordParam(clearTableRecordParam);
		Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(clearTableRecordParam.getMerchantId(), clearTableRecordParam.getStoreId(), true);
		ClearTableRecordResult clearTableRecordResult = storeTableRecordService.clearTableRecord(clearTableRecordParam, store5weiSetting);
		StoreTableRecord storeTableRecord = clearTableRecordResult.getStoreTableRecord();
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTO(storeTableRecord);
		// 发送统计消息
		int merchantId = clearTableRecordParam.getMerchantId();
		long storeId = clearTableRecordParam.getStoreId();
		long tableRecordId = storeTableRecord.getTableRecordId();
		StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, false);
		List<StoreOrder> subStoreOrders = new ArrayList<StoreOrder>();
		if (masterStoreOrder != null) {
			subStoreOrders = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), false);
			List<StoreOrder> toSendPayStatMsgOrders = new ArrayList<StoreOrder>(subStoreOrders.size()+1);
			toSendPayStatMsgOrders.add(masterStoreOrder);
			toSendPayStatMsgOrders.addAll(subStoreOrders);
			if (clearTableRecordResult.getPreClearTableRecordStatus().getValue() != TableRecordStatusEnum.SETTLEMENT.getValue() &&
					clearTableRecordResult.getPreClearTableRecordStatus().getValue() != TableRecordStatusEnum.CLEAR_TABLE.getValue()) {
				i5weiMessageProducer.sendMessageOfStatTableRecordOrder(toSendPayStatMsgOrders);
			}
			if (masterStoreOrder.getUserId() == 0) {
				long userId = 0;
				// 查询第一个userId不为0的子订单，取userId赋值给主订单，用于统计用户消费次数
				for (StoreOrder subStoreOrder : subStoreOrders) {
					if (subStoreOrder.getUserId() > 0) {
						userId = subStoreOrder.getUserId();
						break;
					}
				}
				if (userId > 0) {
					masterStoreOrder.setUserId(userId);
					masterStoreOrder.setUpdateTime(System.currentTimeMillis());
					masterStoreOrder.update();
				}
			}
			//客户取餐计入消费次数统计
			storeOrderHelper.accumulateStoreUserOrders(masterStoreOrder);
		}
		this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), false, store5weiSetting);
		return storeTableRecordDTO;
	}

	/**
	 * 合台
	 */
	@Override
	public StoreTableRecordDTO mergeTableRecord(MergeTableRecordParam mergeTableRecordParam) throws TException {
		storeTableRecordFacadeValidate.checkMergeTableRecordParam(mergeTableRecordParam);
		Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(mergeTableRecordParam.getMerchantId(), mergeTableRecordParam.getStoreId(), true);
		MergeTableRecordResult mergeTableRecordResult = storeTableRecordService.mergeTableRecord(mergeTableRecordParam, store5weiSetting);
		StoreTableRecord storeTableRecord = mergeTableRecordResult.getTargetTableRecord();
		StoreTableRecord srcTableRecord = mergeTableRecordResult.getOriginalTableRecord();
		List<StoreOrder> storeOrderList = mergeTableRecordResult.getSubOrderList();
		List<String> orderIds = new ArrayList<String>();
		for (StoreOrder storeOrder : storeOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}
		// 高级模式纸划菜：传菜间的合台单，更新心跳
		i5weiMergeTablePrinter.sendPrintMessages(storeTableRecord.getMergeStaffId(), srcTableRecord, storeTableRecord, orderIds);
		this.storeHeartbeatService.updateSweepLastUpdateTime(mergeTableRecordParam.getMerchantId(),mergeTableRecordParam.getStoreId(), System.currentTimeMillis(), false, store5weiSetting);//心跳更新
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTO(storeTableRecord);
		return storeTableRecordDTO;
	}

	/**
	 * 转台
	 */
	@Override
	public StoreTableRecordDTO transferTableRecord(TransferTableRecordParam transferTableRecordParam) throws TException {
		storeTableRecordFacadeValidate.checkTransferTableRecordParam(transferTableRecordParam);
		Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(transferTableRecordParam.getMerchantId(), transferTableRecordParam.getStoreId(), true);
		TransferTableRecordResult transferTableRecordResult = storeTableRecordService.transferTableRecord(transferTableRecordParam);
		StoreTableRecord originalStoreTableRecord = transferTableRecordResult.getOriginalTableRecord();
		StoreTableRecord storeTableRecord = transferTableRecordResult.getTableRecord();
		StoreTable targetTable = transferTableRecordResult.getTargetTable();
		List<StoreOrder> storeOrderList = transferTableRecordResult.getSubOrderList();
		List<String> orderIds = new ArrayList<String>();
		for (StoreOrder storeOrder : storeOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}
		i5weiTransTablePrinter.sendPrintMessages(storeTableRecord.getTransferStaffId(), originalStoreTableRecord, targetTable, orderIds);
	    this.storeHeartbeatService.updateSweepLastUpdateTime(transferTableRecordParam.getMerchantId(),transferTableRecordParam.getStoreId(), System.currentTimeMillis(), false, store5weiSetting);

		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTO(storeTableRecord);
		return storeTableRecordDTO;
	}
	
	/**
	 * 退菜
	 */
	@Override
	public StoreTableRecordDTO refundChargeItem(RefundTableItemParam refundTableItemParam) throws T5weiException, TException {
		storeTableRecordFacadeValidate.checkRefundOrderItemParam(refundTableItemParam);
		Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(refundTableItemParam.getMerchantId(), refundTableItemParam.getStoreId(), true);
		//退菜
		RefundChargeItemResult refundChargeItemResult = storeTableRecordService.refundChargeItem(refundTableItemParam);
		StoreTableRecord storeTableRecord = refundChargeItemResult.getStoreTableRecord();
		// 小票划菜，有退菜，加工档口和传菜间需打印退菜通知单、更新心跳
		i5weiRefundMealPrinter.sendPrintMessages(refundTableItemParam.getStaffId(), refundChargeItemResult.getStoreOrderRefundItems());
		this.storeHeartbeatService.updateSweepLastUpdateTime(refundTableItemParam.getMerchantId(),refundTableItemParam.getStoreId(), System.currentTimeMillis(), false, store5weiSetting);//更新心跳
		//刷新相关产品库存
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTOWithSettleRecord(storeTableRecord);
		return storeTableRecordDTO;
	}

	public StoreTableRecordDTO buildStoreTableRecordDTOWithSettleRecord(StoreTableRecord storeTableRecord) throws TException {
//		TableRecordPayStatusResult tableRecordPayStatusInfo = storeTableRecord.getTableRecordPayStatusInfo();
//		if (tableRecordPayStatusInfo == null) {
//			tableRecordPayStatusInfo = storeTableRecordService.getSettleRecord(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord);
//		}
//		List<TableRecordPayDetailResult> tableRecordPayDetailResultList = tableRecordPayStatusInfo.getPayDetailResulttList();
//		List<RefundDetailDTO> refundDetailDTOList = tableRecordPayStatusInfo.getRefundResultList();
//		TableRecordAmountsResult tableRecordAmountsResult = storeTableRecordService.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);
//		StoreTableRecordDTO storeTableRecordDTO = storeTableRecordHelper.buildTableRecordDTO(storeTableRecord, tableRecordPayDetailResultList, refundDetailDTOList, tableRecordAmountsResult);
//		PayDetailInfoDTO payDetailInfoDTO = storeTableRecordDTO.getPayDetailInfoDTO();
//		SettleTableRecordDTO settleTableRecordDTO =  storeTableRecordHelper.createSettleRecord(storeTableRecord, tableRecordAmountsResult, payDetailInfoDTO);
//		storeTableRecordDTO.setSettleTableRecordDTO(settleTableRecordDTO);
//		return storeTableRecordDTO;

		TableRecordDetailParam param = new TableRecordDetailParam();
		param.setMerchantId(storeTableRecord.getMerchantId());
		param.setStoreId(storeTableRecord.getStoreId());
		param.setTableRecordId(storeTableRecord.getTableRecordId());
		List<Integer> loadCodes = Lists.newArrayList();
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue());
		param.setLoadCodes(loadCodes);
		return getStoreTableRecordDetail(param);
	}

	/**
	 * 根据id获取桌台记录详情
	 * 调用新的接口 按需加载桌台详情
	 * @throws TException 
	 */
	@Override
	@Deprecated
	public StoreTableRecordDTO getStoreTableRecordById(int merchantId, long storeId, long tableRecordId) throws TException {
		TableRecordDetailParam param = new TableRecordDetailParam();
		param.setMerchantId(merchantId);
		param.setStoreId(storeId);
		param.setTableRecordId(tableRecordId);
		List<Integer> loadCodes = Lists.newArrayList();
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_AREA.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_STORE_TABLE.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TIME_BUCKET.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CLEAR_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_MERGE_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TRANSFER_TABLE_STAFF.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_CREATE_TABLE_USER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_SETTLE_TABLE_USER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_MASTER_ORDER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SUB_ORDER.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_ITEM.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_PAY_DETAIL.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_REFUND_DETAIL.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_SETTLE_RECORD.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_QRCODE.getValue());
		loadCodes.add(TableRecordDetailLoadCodeEnum.LOAD_TABLE_RECORD_OPTLOG.getValue());
		param.setLoadCodes(loadCodes);
		return getStoreTableRecordDetail(param);
	}
	
	/**
	 * 获取桌台记录列表
	 * @param merchantId
	 * @param storeId
	 * @param areaId
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Override
	public List<TableInfoDTO> getStoreTableRecords(int merchantId, long storeId, long areaId) throws T5weiException, TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (areaId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "areaId["+areaId+"] invalid");
		}
		List<TableInfoDTO> list = storeTableRecordService.getStoreTableRecords(merchantId, storeId, areaId);
		return list;
	}
	
	/**
	 * 获取真实桌台记录列表
	 * @param merchantId
	 * @param storeId
	 * @param areaId
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Override
	public List<TableInfoDTO> getRealStoreTableRecords (int merchantId, long storeId, long areaId) throws T5weiException, TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (areaId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "areaId["+areaId+"] invalid");
		}
		List<TableInfoDTO> list = storeTableRecordService.getRealStoreTableRecords(merchantId, storeId, areaId);
		return list;
	}
	
	/**
	 * 获取结账单
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @throws TException
	 */
	@Override
	public SettleTableRecordDTO getSettleTableRecord(int merchantId, long storeId, long tableRecordId) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			log.error("tableRecordId["+tableRecordId+"] invalid");
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeTableRecord = storeTableRecordService.getMergeTableRecord(storeTableRecord);
		tableRecordId = storeTableRecord.getTableRecordId();
		TableRecordPayStatusResult tableRecordPayStatusInfo = storeTableRecordService.getSettleRecord(merchantId, storeId, storeTableRecord);
		TableRecordAmountsResult tableRecordAmountsResult = storeTableRecordService.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);
		// 封装返回结果
		List<StoreOrder> subStoreOrders = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, storeTableRecord.getOrderId(), false);
		List<TableRecordPayDetailResult> tableRecordPayDetailResultList = tableRecordPayStatusInfo.getPayDetailResulttList();
		PayDetailInfoDTO payDetailInfoDTO = storeTableRecordHelper.buildPayDetailInfoDTO(tableRecordPayDetailResultList, tableRecordAmountsResult, subStoreOrders, storeTableRecord);
		SettleTableRecordDTO settleTableRecordDTO =  storeTableRecordHelper.createSettleRecord(storeTableRecord, tableRecordAmountsResult, payDetailInfoDTO);
		Map<String,String> qrcodeMap = storeTableQrcodeService.getTableRecordQrcode(merchantId, storeId, tableRecordId);
		settleTableRecordDTO.setUrlCode(qrcodeMap.get("qrcode_url"));
		settleTableRecordDTO.setLinkCode(qrcodeMap.get("qrcode_link"));
		return settleTableRecordDTO;
	}
	
	/**
	 * 为桌台记录设置整单折扣和整单减免 
	 */
	@Override
	public StoreTableRecordDTO setDerate4TableRecord (int merchantId, long storeId, long tableRecordId, double totalRebate, long totalDerate) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (totalRebate < 0 || totalRebate > 100) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "totalRebate["+totalRebate+"] invalid");
		}
		if (totalDerate < 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "totalDerate["+totalDerate+"] invalid");
		}
		StoreTableRecord storeTableRecord = storeTableRecordService.setDerate4TableRecord(merchantId, storeId, tableRecordId, totalRebate, totalDerate);
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTOWithSettleRecord(storeTableRecord);
		return storeTableRecordDTO;
	}

	/**
	 * 结账-支付（暂时兼容微信端，将被替换）
	 * @throws TException 
	 */
	@Override
	@Deprecated
	public StoreOrderDTO settleTableRecordPay (int merchantId, long storeId, long tableRecordId, long staffId, long userId, int clientType) throws TException {
		SettlePayTableRecordParam settlePayTableRecordParam = new SettlePayTableRecordParam();
		settlePayTableRecordParam.setMerchantId(merchantId);
		settlePayTableRecordParam.setStoreId(storeId);
		settlePayTableRecordParam.setTableRecordId(tableRecordId);
		settlePayTableRecordParam.setStaffId(staffId);
		settlePayTableRecordParam.setUserId(userId);
		settlePayTableRecordParam.setClientType(clientType);
		return this.settleTableRecordPay2(settlePayTableRecordParam);
	}
	
	/**
	 * 结账-支付（参数对象化，新增抹零金额字段，之后将替换原来的方法）
	 * @throws TException 
	 */
	@Override
	public StoreOrderDTO settleTableRecordPay2 (SettlePayTableRecordParam settlePayTableRecordParam) throws TException {
		int merchantId = settlePayTableRecordParam.getMerchantId();
		long storeId = settlePayTableRecordParam.getStoreId();
		long tableRecordId = settlePayTableRecordParam.getTableRecordId();
		long staffId = settlePayTableRecordParam.getStaffId();
		long userId = settlePayTableRecordParam.getUserId();
		int clientType = settlePayTableRecordParam.getClientType();
		long staffDerate = settlePayTableRecordParam.getStaffDerate();
		
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (staffId <= 0 && userId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "staffId["+staffId+"] or userId["+userId+"] invalid");
		}
		if (staffDerate < 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "staffDerate["+staffDerate+"] invalid");
		}
		StoreOrder storeOrder = storeTableRecordService.settleTableRecordPay(merchantId, storeId, tableRecordId, staffId, userId, clientType, staffDerate);
		StoreOrderDTO storeOrderDTO = new StoreOrderDTO();
		BeanUtil.copy(storeOrder, storeOrderDTO);
		return storeOrderDTO;
	}

    @Override
	@Deprecated
	public List<StoreOrderDTO> getRefundDetail (int merchantId, long storeId, long tableRecordId) throws TException {
		return new ArrayList<StoreOrderDTO>();
	}
	
	/**
	 * 将订单关联到桌台记录（加菜）
	 */
	@Override
	public StoreTableRecordDTO relateSubOrderToTableRecord (int merchantId, long storeId, long tableRecordId, List<String> orderIds) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (orderIds == null || orderIds.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "orderIds invalid");
		}
		RelateSubOrderToTableRecordResult relateSubOrderToTableRecordResult = storeTableRecordService.relateSubOrderToTableRecord(merchantId, storeId, tableRecordId, orderIds);
		StoreTableRecord storeTableRecord = relateSubOrderToTableRecordResult.getStoreTableRecord();
		// 后厨清单（自定义打印 + 传菜间）
		i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,storeTableRecord.getTableRecordId(),orderIds,relateSubOrderToTableRecordResult.getStoreMealTakeups());
		long userId = relateSubOrderToTableRecordResult.getUserId();
		List<StoreOrder> storeOrders = relateSubOrderToTableRecordResult.getStoreOrders();
		if (storeOrders!=null && !storeOrders.isEmpty()) {
			for (StoreOrder storeOrder : storeOrders) {
				// 取餐打印
				storeOrderHelper.insertCashierPrintOrder(storeOrder);
				// 点（加）菜单
				i5weiTakeCodePrinter.sendPrintMessages(storeOrder);
				// 打包清单
				i5weiTakeAndSendOutPrinter.sendPrintMessages(storeOrder);
				// 发送交互消息
		        i5weiMessageProducer.sendMessageOfStoreOrderEvent(storeOrder, 0, TweetEventType.PAY_ORDER, "订单消费");
				// 更改统计信息为订单交易中
				if (storeOrder.isPayFinish()) {
		            // 发送入账通知
		            i5weiMessageProducer.sendMessageOfMerchantAccounted(storeOrder);
		            // 发送统计通知
		            i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(storeOrder);
		        }
			}
			//客户取餐计入消费次数统计
			storeOrderHelper.accumulateStoreUserOrders(relateSubOrderToTableRecordResult.getMasterOrder());
			try {
				wechatNotifyService.notifyTableRecordAddDishMsg(storeTableRecord, (long) userId);
			} catch (Exception e) {
				log.warn("send storeTableRecord["+storeTableRecord+"] add dish msg to user["+userId+"] fail");
			}
		}
		StoreTableRecordDTO storeTableRecordDTO = buildStoreTableRecordDTO(storeTableRecord);
		this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), false, null);
		return storeTableRecordDTO;
	}
	
	/**
	 * 获取退款详情
	 * @throws TException 
	 */
	@Override
	public List<StoreOrderDTO> getRefundDetail2 (int merchantId, long storeId, long tableRecordId) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		List<StoreOrder> list = storeTableRecordService.getRefundDetail(merchantId, storeId, tableRecordId);
		if (list == null || list.isEmpty()) {
			return new ArrayList<StoreOrderDTO>();
		}
		List<StoreOrderDTO> storeOrderDTOList = BeanUtil.copyList(list, StoreOrderDTO.class);
		for (StoreOrderDTO storeOrderDTO : storeOrderDTOList) {
			// 给员工和用户赋值
			if (storeOrderDTO.getStaffId() > 0) {
				List<Long> staffIdList = new ArrayList<Long>();
				staffIdList.add(storeOrderDTO.getStaffId());
				Map<Long, StaffDTO> staffDTOMap = staffFacade.getStaffMapInIds(merchantId, staffIdList, true);
				if (staffDTOMap != null && staffDTOMap.containsKey(storeOrderDTO.getStaffId())) {
					StaffDTO staffDTO = staffDTOMap.get(storeOrderDTO.getStaffId());
					storeOrderDTO.setStaffDTO(storeTableRecordService.getStoreTableStaffDTO(staffDTO));
				}
			}
			if (storeOrderDTO.getUserId() > 0) {
				UserDTO userDTO = userFacade.getUserByUserId(storeOrderDTO.getUserId());
				if (userDTO != null) {
					storeOrderDTO.setUserDTO(BeanUtil.copy(userDTO, I5weiUserDTO.class));
				}
			}
		}
		return storeOrderDTOList;
	}
	
	/**
	 * 结账-退款
	 */
	@Override
	@Deprecated
	public List<StoreOrderDTO> settleTableRecordRefund (int merchantId, long storeId, long tableRecordId, List<StoreOrderRefundModeParam> orderRefundList) throws T5weiException, TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (orderRefundList == null || orderRefundList.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "orderRefundList invalid");
		}
		List<StoreOrderDTO> storeOrderDTOList = storeTableRecordService.settleTableRecordRefund(merchantId, storeId, tableRecordId, orderRefundList);
		return storeOrderDTOList;
	}
	
	/**
	 * 修改桌台服务员、客人数量
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param serviceStaffId
	 * @return StoreTableRecordDTO
	 * @throws TException 
	 */
	@Override
	public StoreTableRecordDTO changeTableRecord (int merchantId, long storeId, long tableRecordId, long serviceStaffId, int customerTraffic) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (serviceStaffId < -1) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "serviceStaffId["+serviceStaffId+"] invalid");
		}
		if (customerTraffic < -1 || customerTraffic > 999) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "customerTraffic["+customerTraffic+"] invalid");
		}
		StoreTableRecord storeTableRecord = storeTableRecordService.changeTableRecord(merchantId, storeId, tableRecordId, serviceStaffId, customerTraffic);
		TableRecordPayStatusResult tableRecordPayStatusInfo = storeTableRecordService.getSettleRecord(merchantId, storeId, storeTableRecord);
		List<TableRecordPayDetailResult> tableRecordPayDetailResultList = tableRecordPayStatusInfo.getPayDetailResulttList();
		List<RefundDetailDTO> refundDetailDTOList = tableRecordPayStatusInfo.getRefundResultList();
		TableRecordAmountsResult tableRecordAmountsResult = storeTableRecordService.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);
		StoreTableRecordDTO storeTableRecordDTO = storeTableRecordHelper.buildTableRecordDTO(storeTableRecord, tableRecordPayDetailResultList, refundDetailDTOList, tableRecordAmountsResult);
		PayDetailInfoDTO payDetailInfoDTO = storeTableRecordDTO.getPayDetailInfoDTO();
		SettleTableRecordDTO settleTableRecordDTO = storeTableRecordHelper.createSettleRecord(storeTableRecord, tableRecordAmountsResult, payDetailInfoDTO);
		Map<String,String> qrcodeMap = storeTableQrcodeService.getTableRecordQrcode(merchantId, storeId, tableRecordId);
		settleTableRecordDTO.setUrlCode(qrcodeMap.get("qrcode_url"));
		settleTableRecordDTO.setLinkCode(qrcodeMap.get("qrcode_link"));
		storeTableRecordDTO.setSettleTableRecordDTO(settleTableRecordDTO);
		return storeTableRecordDTO;
	}
	
	/**
	 * 判断桌台记录是否允许结账
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @return
	 * @throws T5weiException
	 */
	@Override
	public boolean checkSettleCondition (int merchantId, long storeId, long tableRecordId) throws T5weiException {
		return storeTableRecordService.checkSettleCondition(merchantId, storeId, tableRecordId);
	}
	
	/**
	 * 计算折扣和应付（目前仅提供给ios使用）
	 * @param price
	 * @param discountPro
	 * @param discountAmount
	 * @return
	 */
	@Override
	public Map<String, Long> caculateDiscountAmount (long price, double discountPro, long discountAmount) {
		return storeTableRecordService.caculateDiscountAmount(price, discountPro, discountAmount);
	}

	/**
	 * 店铺设置台位费开关/营业时段台位费变动
	 * @param merchantId
	 * @param storeId
	 * @throws TException
	 */
	@Override
	public void changeTableFee(int merchantId, long storeId) throws TException {
		storeTableRecordService.changeTableFee(merchantId, storeId);
	}
	
	/**
	 * 将桌台记录置为结账中状态
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 * @param isToSettling
	 * @throws T5weiException
	 */
	@Override
	public void tableRecordToSettling (int merchantId, long storeId, String orderId, boolean isToSettling) throws T5weiException {
		storeTableRecordService.tableRecordToSettling(merchantId, storeId, orderId, isToSettling, ClientTypeEnum.CASHIER.getValue());
	}
	
	/**
	 * 订单管理-退款
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param orderRefundList
	 * @return
	 * @throws TPayException
	 * @throws TException
	 */
	@Override
	public long tableRecordRefund (int merchantId, long storeId, long tableRecordId, List<StoreOrderRefundModeParam> orderRefundList, int refundVersion) throws TPayException, TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (orderRefundList == null || orderRefundList.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "orderRefundList invalid");
		}
		if(StoreOrderRefundVersion.findByValue(refundVersion) == null){
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "not exist refundVersion["+refundVersion+"]");
		}
		return storeTableRecordService.tableRecordRefund(merchantId, storeId, tableRecordId, orderRefundList, refundVersion);
	}
	
	
	/**
	 * 结账-退款
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param orderRefundList
	 * @return
	 * @throws TException
	 */
	@Override
	public long settleTableRecordRefund2 (int merchantId, long storeId, long tableRecordId, List<StoreOrderRefundModeParam> orderRefundList, int refundVersion) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (orderRefundList == null || orderRefundList.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "orderRefundList invalid");
		}
		if(StoreOrderRefundVersion.findByValue(refundVersion) == null){
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "not exist refundVersion["+refundVersion+"]");
		}
		return storeTableRecordService.settleTableRecordRefund2(merchantId, storeId, tableRecordId, orderRefundList, refundVersion);
	}
	
	/**
	 * 接收退款回调
	 * @param batchRefundId
	 * @param refundResults
	 * @param status
	 * @param type
	 * @param errorCode
	 * @param errorMsg
	 * @throws TException
	 */
	@Override
	public void confirmRefundResult (long batchRefundId, List<RefundResultDTO> refundResults, int status, int type, String errorCode, String errorMsg, int refundVersion) throws TException {
		ConfirmRefundResult confirmRefundResult = storeTableRecordService.confirmRefund(batchRefundId, refundResults, status, type, errorCode, errorMsg);
		if (confirmRefundResult == null) {
			return;
		}
		boolean refundSuccess = confirmRefundResult.isRefundSuccess();
		Map<String, StoreOrder> storeOrderMap = confirmRefundResult.getStoreOrderMap();
		TableRecordBatchRefundRecord tableRecordBatchRefundRecord = confirmRefundResult.getTableRecordBatchRefundRecord();
		if (tableRecordBatchRefundRecord == null) {
			return;
		}
		int merchantId = tableRecordBatchRefundRecord.getMerchantId();
		long storeId = tableRecordBatchRefundRecord.getStoreId();
		long tableRecordId = tableRecordBatchRefundRecord.getTableRecordId();
		if (refundSuccess) {
			try{
				//生成订单操作日志 
				List<Long> refundRecordIds = new ArrayList<Long>();
				for (RefundResultDTO refundResultDTO : refundResults) {
					long refundRecordId = refundResultDTO.getRefundRecordId();
					refundRecordIds.add(refundRecordId);
				}
				List<RefundRecordDBDTO> refundRecords = confirmRefundResult.getRefundRecordDBDTOs();
				Map<Long, RefundRecordDBDTO> map = new HashMap<Long, RefundRecordDBDTO>();
				if(refundRecords != null && !refundRecords.isEmpty()){
					for (RefundRecordDBDTO refundRecordDBDTO : refundRecords) {
						map.put(refundRecordDBDTO.getRefundRecordId(), refundRecordDBDTO);
					}
				}
				for (RefundResultDTO refundResultDTO : refundResults) {
					StoreOrderRefundStatusEnum merchantRefund;
					if (refundResultDTO.getPayOrderStatus() == PayOrderStatusDTO.FULL_REFUND.getValue()) {
						merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_ALL;
					} else {
						merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_PART;
					}
					long refundRecordId = refundResultDTO.getRefundRecordId();
					String storeOrderId = refundResultDTO.getBusinId();
			        if (map.containsKey(refundRecordId) && storeOrderMap.containsKey(storeOrderId)) {
			        	RefundRecordDBDTO refundRecordDBDTO = map.get(refundRecordId);
			        	StoreOrderRefundRecord storeOrderRefundRecord = new StoreOrderRefundRecord();
			        	storeOrderRefundRecord.setRefundRecordId(refundRecordId);
			        	storeOrderRefundRecord.setMerchantId(refundResultDTO.getMerchantId());
			        	storeOrderRefundRecord.setStoreId(refundResultDTO.getStoreId());
			        	storeOrderRefundRecord.setOrderId(refundResultDTO.getBusinId());
			        	storeOrderRefundRecord.setStatus(status);
			        	if (tableRecordBatchRefundRecord != null) {
			        		storeOrderRefundRecord.setStaffId(tableRecordBatchRefundRecord.getStaffId());
				        	storeOrderRefundRecord.setClientType(tableRecordBatchRefundRecord.getClientType());
			        	}
			        	storeOrderRefundRecord.setErrorCode(Integer.parseInt(errorCode));
			        	storeOrderRefundRecord.setErrorMsg(errorMsg);
			        	storeOrderRefundRecord.setCreateTime(refundRecordDBDTO.getCreateTime());
			        	storeOrderRefundRecord.setFinishTime(System.currentTimeMillis());
			        	storeOrderRefundRecord.create();
			        	storeOrderRefundService.createRefundDetails(refundResultDTO.getMerchantId(), refundResultDTO.getStoreId(), refundResultDTO.getBusinId(), refundRecordDBDTO.getSuccessRefundDetails());
			        	StoreOrder storeOrder = storeOrderMap.get(storeOrderId);
			        	long staffId = storeOrderRefundRecord.getStaffId();
			        	StoreOrder refundOrder = storeOrderService.refundStoreOrder(storeOrder, staffId, merchantRefund, refundRecordId);
			        	// 统计消息
				        i5weiMessageProducer.sendMessageOfStatStoreOrderRefund(refundOrder, staffId, refundRecordId, false);
				        // 事件消息
				        i5weiMessageProducer.sendMessageOfStoreOrderEvent(refundOrder, staffId, TweetEventType.REFUND_ORDER, "订单退款");
				        // 退款完成向店铺统计发送退款消息
				        i5weiMessageProducer.sendRefundMessageOfStoreStatistics(refundRecordId);
			        } else {
			        	log.error("refundRecordId["+refundRecordId+"] can not query RefundRecordDBDTO or storeOrderId["+storeOrderId+"] can not StoreOrder");
			        }
			        String orderId = refundResultDTO.getBusinId();
			        try {
				        StoreOrder storeOrder = null;
				        if (storeOrderMap.containsKey(orderId)) {
				        	storeOrder = storeOrderMap.get(orderId);
				        } else {
				        	storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, false);
				        }
				        if (storeOrder != null && storeOrder.getUserId()>0) {
							String notifyMessage = storeOrderHelper.getRefundTweetInfo(storeOrder, refundResultDTO.getRefundRecordId(), refundVersion);
							// 发消息
							notifyFacade.wechatSendSimple(merchantId, storeId, storeOrder.getUserId(), notifyMessage, 1);
				        }
			        } catch (Exception e) {
			        	log.warn("orderId["+orderId+"] refund msg send fail");
			        }
				}
				// 发送订单支付信息到统计库
				// 查询出主订单
				StoreOrder masterOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, false);
				List<StoreOrder> subStoreOrders = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterOrder.getOrderId(), false);
				List<StoreOrder> storeOrders = new ArrayList<StoreOrder>(subStoreOrders.size() + 1);
				storeOrders.add(masterOrder);
				storeOrders.addAll(subStoreOrders);
				if (!confirmRefundResult.isSettleRefund()) {
					storeOrders.removeAll(subStoreOrders);
				}
				i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
			} catch (DuplicateKeyException e) {
				log.warn("DuplicateKeyException batchRefundId["+batchRefundId+"]");
			}
		}
	}
	
	/**
	 * 查询退款结果
	 */
	@Override
	public TableRecordBatchRefundRecordDTO queryTableRecordBatchRefundResult (long batchRefundId) throws T5weiException {
		if (batchRefundId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "batchRefundId["+batchRefundId+"] invalid");
		}
		TableRecordBatchRefundRecord tableRecordBatchRefundRecord = storeTableRecordService.queryTableRecordBatchRefundResult(batchRefundId);
		TableRecordBatchRefundRecordDTO tableRecordBatchRefundRecordDTO = new TableRecordBatchRefundRecordDTO();
		BeanUtil.copy(tableRecordBatchRefundRecord, tableRecordBatchRefundRecordDTO);
		return tableRecordBatchRefundRecordDTO;
	}

	/**
	 * 撤销结账
	 */
	@Override
	public boolean tableRecordSettleCancel(int merchantId, long storeId, long tableRecordId) throws T5weiException, TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		return storeTableRecordService.tableRecordSettleCancel(merchantId, storeId, tableRecordId);
	}
	
	/**
	 * 心跳查询桌台记录的最后更新时间
	 * @param merchantId
	 * @param storeId
	 * @return
	 * @throws TException 
	 */
	@Override
	public Map<Long, AreaAndTableRecordUpdateTimeDTO> getTableRecordUpdateTime (int merchantId, long storeId) throws TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		return storeTableRecordService.getTableRecordUpdateTime(merchantId, storeId);
	}

	public void tableRecordSettlingCancelMsgProcessor (int merchantId, long storeId, long tableRecordId) {
		System.out.println(StaffFacade.Iface.class.getName());
	}
	
	/**
	 * 退菜时计算单个收费项目可以退多少钱
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param chargeItemId
	 * @param packed
	 * @return
	 * @throws T5weiException
	 */
	@Override
	public long caculateChargeItemAvePrice(int merchantId, long storeId, long tableRecordId, long chargeItemId, boolean packed) throws T5weiException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (chargeItemId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "chargeItemId["+chargeItemId+"] invalid");
		}
		return storeTableRecordService.caculateChargeItemAvePrice(merchantId, storeId, tableRecordId, chargeItemId, packed);
	}

	/**
	 * 根据loadCodes获取桌台记录详情
	 *
	 * @param param 桌台记录ID和loadCodes
	 * @return
	 * @throws TException
	 */
	@Override
	public StoreTableRecordDTO getStoreTableRecordDetail(TableRecordDetailParam param) throws TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long tableRecordId = param.getTableRecordId();
		List<Integer> loadCodes = param.getLoadCodes();
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId[" + merchantId + "] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId[" + storeId + "] invalid");
		}
		if (tableRecordId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId[" + tableRecordId + "] invalid");
		}
		if (loadCodes == null || loadCodes.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(),
					"merchantId[" + merchantId + "] storeId[" + storeId + "]  tableRecordId[" + tableRecordId + "], loadCodes is empty");
		}

		List<Integer> queryLoadCodes =  StoreTableRecordHelper.handleLoadCodes(loadCodes);
		return storeTableRecordHelper.getTableRecordDetailByLoadCodes(queryLoadCodes, merchantId, storeId, tableRecordId);
	}

	@Override
	public StoreTableRecordDTO reduceTableRecordTableFee(int merchantId, long storeId, long tableRecordId, long reductionTableFee) throws T5weiException, TException {
		if (merchantId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+merchantId+"] invalid");
		}
		if (storeId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+storeId+"] invalid");
		}
		if (tableRecordId <= 0L) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (reductionTableFee < 0L) {
			throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_REDUCE_TABLE_FEE_LT_ZERO.getValue(), " store table reduce table fee lt zero ");
		}
		Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
		if (!store5weiSetting.isEnableTableFee()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_DISABLE_TABLE_FEE.getValue(), " store table disable table fee ");
		}
		StoreTableRecord storeTableRecord = this.storeTableRecordService.reduceTableRecordTableFee(merchantId, storeId, tableRecordId, reductionTableFee);
		return buildStoreTableRecordDTOWithSettleRecord(storeTableRecord);
	}

	/**
	 * 根据条件获取桌台记录集合
	 *
	 * @param param 查询条件和分页信息
	 * @return
	 * @throws TException
	 */
	@Override
	public StoreTableRecordPageDTO queryStoreTableRecords(StoreTableRecordQueryParam param) throws TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		int pageNo = param.getPageNo();
		int size = param.getSize();

		long tableRecordId = param.getTableRecordId();//指定桌台记录[可选]
		long repastDate = param.getRepastDate();//指定就餐日期[可选]
		long staffId = param.getStaffId();//指定的默认服务员[可选]
		if (merchantId <= 0 || storeId <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] invalid");
		}
		if (pageNo <= 0 || size <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "page size[" + size + "] page no[" + pageNo + "] invalid");
		}

		int count = storeTableRecordService.getStoreTableRecordsCount(merchantId, storeId, tableRecordId, repastDate, staffId);
		List<StoreTableRecord> storeTableRecords = storeTableRecordService.getStoreTableRecords(merchantId, storeId, tableRecordId, repastDate, staffId, pageNo, size);
		List<StoreTableRecordDTO> storeTableRecordDTOs = storeTableRecords.stream().map(this::buildSimpleStoreTableRecord).collect(Collectors.toList());

		StoreTableRecordPageDTO pageDTO = new StoreTableRecordPageDTO();
		pageDTO.setTotal(count);
		pageDTO.setPageNo(pageNo);
		pageDTO.setPageNum(PageUtil.getPageNum(count, size));
		pageDTO.setSize(size);
		pageDTO.setDataList(storeTableRecordDTOs);
		return pageDTO;
	}

	public StoreTableRecordDTO buildSimpleStoreTableRecord(StoreTableRecord storeTableRecord) {
		StoreTableRecordDTO dto = new StoreTableRecordDTO();
		BeanUtil.copy(storeTableRecord, dto);
		return dto;
	}
}
