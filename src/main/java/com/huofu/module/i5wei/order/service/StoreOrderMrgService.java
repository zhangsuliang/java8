package com.huofu.module.i5wei.order.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.QueryOrderParam;
import huofucore.facade.i5wei.order.StoreOrderInvoiceFinishParam;
import huofucore.facade.i5wei.order.StoreOrderInvoiceStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderInvoicedParam;
import huofucore.facade.i5wei.order.StoreOrderLockStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderLockedParam;
import huofucore.facade.i5wei.order.StoreOrderOptlogTypeEnum;
import huofucore.facade.i5wei.order.UserOrderInvoiceQueryParam;
import huofucore.facade.merchant.invoice.MerchantInvoiceStatusEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.PageResult;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderInvoiceDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderInvoice;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;

@Service
public class StoreOrderMrgService {

	@Autowired
	private StoreOrderDAO storeOrderDAO;

	@Autowired
	private StoreOrderItemDAO storeOrderItemDAO;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StoreOrderPriceHelper storeOrderPriceHelper;

	@Autowired
	private StoreOrderInvoiceDAO storeOrderInvoiceDAO;
	
	@Autowired
	private StoreOrderOptlogDAO storeOrderOptlogDAO;

	public PageResult getStoreOrders(QueryOrderParam queryOrderParam, int page, int size) {
		int begin = PageUtil.getBeginIndex(page, size);
		List<StoreOrder> storeOrders = this.storeOrderDAO.getList(queryOrderParam, begin, size, true);
		List<String> orderIds = this.storeOrderHelper.getStoreOrderIds(storeOrders);
		Map<String, List<StoreOrderItem>> map = this.storeOrderItemDAO.getMapsInOrderIds(queryOrderParam.getMerchantId(), queryOrderParam.getStoreId(), orderIds);
		for (StoreOrder storeOrder : storeOrders) {
			storeOrder.setStoreOrderItems(map.get(storeOrder.getOrderId()));
		}
		int count = this.storeOrderDAO.count(queryOrderParam, true);
		PageResult pageResult = new PageResult();
		pageResult.setTotal(count);
		pageResult.setSize(size);
		pageResult.setPage(page);
		pageResult.setList(storeOrders);
		pageResult.build();
		return pageResult;
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreOrder updateStoreOrderInvoiced(StoreOrderInvoicedParam storeOrderInvoicedParam) throws T5weiException {
		int merchantId = storeOrderInvoicedParam.getMerchantId();
		long storeId = storeOrderInvoicedParam.getStoreId();
		String orderId = storeOrderInvoicedParam.getOrderId();
		boolean invoiced = storeOrderInvoicedParam.isInvoiced();
		long staffId = storeOrderInvoicedParam.getStaffId();
		int clientType = storeOrderInvoicedParam.getClientType();
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		StoreOrderInvoice storeOrderInvoice = storeOrderInvoiceDAO.queryById(merchantId, storeId, orderId);
		if (storeOrderInvoice != null && invoiced == false) {
			// 已经开过电子发票，不能直接关闭
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ELEC_INVOICE_HAS_OPENED.getValue(), DataUtil.infoWithParams("this store order has opened elecInvoice, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		String optRemark = "";
		if (invoiced) {
			storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.FINISH.getValue());
			storeOrder.setUpdateTime(System.currentTimeMillis());
			storeOrder.update();
			optRemark = "invoiced=" + invoiced;
		} else {
			if (storeOrder.getInvoiceStatus() == StoreOrderInvoiceStatusEnum.FINISH.getValue()) {
				storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.NOT.getValue());
				storeOrder.setUpdateTime(System.currentTimeMillis());
				storeOrder.update();
				optRemark = "invoiced=" + invoiced;
			}
		}
		// 记录日志
		if (!optRemark.isEmpty()) {
			storeOrderOptlogDAO.createOptlog(storeOrder, staffId, clientType, StoreOrderOptlogTypeEnum.ORDER_INVOICE.getValue(), optRemark);
		}
		return storeOrder;
	}
	
	public StoreOrderInvoice getStoreOrderInvoice(int merchantId, long storeId, String orderId) {
		StoreOrderInvoice storeOrderInvoice = storeOrderInvoiceDAO.queryById(merchantId, storeId, orderId);
		return storeOrderInvoice;
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void saveUserOrderInvoice(StoreOrderInvoice storeOrderInvoice) throws T5weiException {
		String orderId = storeOrderInvoice.getOrderId();
		int merchantId = storeOrderInvoice.getMerchantId();
		long storeId = storeOrderInvoice.getStoreId();
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		long currentTime = System.currentTimeMillis();
		storeOrderInvoice.setInvoiceTime(currentTime);
		storeOrderInvoice.setRepastDate(storeOrder.getRepastDate());
		storeOrderInvoice.setTimeBucketId(storeOrder.getTimeBucketId());
		// 保存发票信息
		try{
			storeOrderInvoice.setUpdateTime(currentTime);
			storeOrderInvoice.setCreateTime(currentTime);
			storeOrderInvoice.create();
		}catch(DuplicateKeyException e){
			storeOrderInvoice.setUpdateTime(currentTime);
			storeOrderInvoice.update();
		}
		// 更新订单状态
		if (storeOrder.getInvoiceStatus() != StoreOrderInvoiceStatusEnum.FINISH.getValue() && storeOrderInvoice.getInvoiceStatus() == MerchantInvoiceStatusEnum.FINISH.getValue()) {
			storeOrder.snapshot();
			storeOrder.setUpdateTime(currentTime);
			storeOrder.setInvoiceDemand(storeOrderInvoice.getInvoiceTitle());
			storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.FINISH.getValue());
			storeOrder.update();
		}
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void updateUserOrderInvoice(StoreOrderInvoiceFinishParam param) throws T5weiException {
		int merchantId = param.getMerchantId(); 
		long storeId = param.getStoreId(); 
		String orderId = param.getOrderId();
		long currentTime = System.currentTimeMillis();
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		storeOrder.snapshot();
		StoreOrderInvoice storeOrderInvoice = storeOrderInvoiceDAO.getById(merchantId, storeId, orderId, true, true);
		if (storeOrderInvoice == null) {
			storeOrderInvoice = new StoreOrderInvoice();
			BeanUtil.copy(storeOrder, storeOrderInvoice);
			BeanUtil.copy(param, storeOrderInvoice);
			storeOrderInvoice.setInvoiceTime(currentTime);
			storeOrderInvoice.setCreateTime(currentTime);
			storeOrderInvoice.setUpdateTime(currentTime);
			storeOrderInvoice.create();
		}else{
			BeanUtil.copy(param, storeOrderInvoice);
			storeOrderInvoice.setUpdateTime(System.currentTimeMillis());
			storeOrderInvoice.update();
		}
		// 更新订单状态
		if (storeOrder.getInvoiceStatus() != StoreOrderInvoiceStatusEnum.FINISH.getValue() && storeOrderInvoice.getInvoiceStatus() == MerchantInvoiceStatusEnum.FINISH.getValue()) {
			storeOrder.snapshot();
			storeOrder.setUpdateTime(currentTime);
			storeOrder.setInvoiceDemand(storeOrderInvoice.getInvoiceTitle());
			storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.FINISH.getValue());
			storeOrder.update();
		}
	}
	
	public StoreOrderInvoice getUserOrderInvoice(UserOrderInvoiceQueryParam param) throws T5weiException {
		int merchantId = param.getMerchantId(); 
		long storeId = param.getStoreId(); 
		String orderId = param.getOrderId();
		long queryUserId = param.getQueryUserId();
		StoreOrderInvoice storeOrderInvoice = storeOrderInvoiceDAO.queryById(merchantId, storeId, orderId);
		if (storeOrderInvoice == null) {
			return null;
		}
		if (queryUserId != storeOrderInvoice.getUserId()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_INVOICE_USER_NO_PERMISSION.getValue(), DataUtil.infoWithParams(" this user has no permission to visit the order invoice, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		return storeOrderInvoice;
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreOrder updateStoreOrderLocked(StoreOrderLockedParam storeOrderLockedParam) throws T5weiException {
		int merchantId = storeOrderLockedParam.getMerchantId();
		long storeId = storeOrderLockedParam.getStoreId();
		String orderId = storeOrderLockedParam.getOrderId();
		boolean locked = storeOrderLockedParam.isLocked();
		long staffId = storeOrderLockedParam.getStaffId();
		int clientType = storeOrderLockedParam.getClientType();
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (storeOrder.isOrderLocked() != locked) {
			storeOrder.snapshot();
			String optRemark = "";
			if(locked){
				storeOrder.setOrderLockStatus(StoreOrderLockStatusEnum.MANUAL.getValue());
				optRemark = "manual lock";
			}else{
				storeOrder.setOrderLockStatus(StoreOrderLockStatusEnum.NOT.getValue());
				optRemark = "manual unlock";
			}
			storeOrder.setUpdateTime(System.currentTimeMillis());
			storeOrder.update();
			storeOrderOptlogDAO.createOptlog(storeOrder, staffId, clientType, StoreOrderOptlogTypeEnum.ORDER_LOCK.getValue(), optRemark);
		}
		return storeOrder;
	}

}
