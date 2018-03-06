package com.huofu.module.i5wei.order.facade;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.QueryOrderParam;
import huofucore.facade.i5wei.order.StoreOrderCreditStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofucore.facade.i5wei.order.StoreOrderInvoiceDTO;
import huofucore.facade.i5wei.order.StoreOrderInvoiceFinishParam;
import huofucore.facade.i5wei.order.StoreOrderInvoicedParam;
import huofucore.facade.i5wei.order.StoreOrderLockedParam;
import huofucore.facade.i5wei.order.StoreOrderMgrFacade;
import huofucore.facade.i5wei.order.StoreOrderPageDTO;
import huofucore.facade.i5wei.order.UserOrderInvoiceParam;
import huofucore.facade.i5wei.order.UserOrderInvoiceQueryParam;
import huofucore.facade.invoice.ElecInoviceFacade;
import huofucore.facade.invoice.ElecInvoiceDTO;
import huofucore.facade.invoice.ElecInvoiceParam;
import huofucore.facade.merchant.invoice.MerchantInvoiceFacade;
import huofucore.facade.merchant.invoice.MerchantInvoiceParam;
import huofucore.facade.merchant.invoice.MerchantInvoiceStatusEnum;
import huofucore.facade.merchant.store.StoreInvoiceSettingDTO;
import huofucore.facade.merchant.store.StoreSettingFacade;
import huofucore.facade.pay.payment.FacadePayOrderInvalidException;
import huofucore.facade.pay.payment.PayFacade;
import huofucore.facade.pay.payment.PayResultOfPayOrder;
import huofucore.facade.pay.payment.PaySrcEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.PageResult;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderActualPayResult;
import com.huofu.module.i5wei.order.entity.StoreOrderInvoice;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderMrgService;
import com.huofu.module.i5wei.order.service.StoreOrderPriceHelper;
import com.huofu.module.i5wei.order.service.StoreOrderQueryService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.wechat.WechatNotifyService;
import com.huofu.module.i5wei.wechat.WechatQrcodeService;

/**
 * Created by akwei on 7/17/15.
 */
@Component
@ThriftServlet(name = "storeOrderMgrFacadeServlet", serviceClass = StoreOrderMgrFacade.class)
public class StoreOrderMgrFacadeImpl implements StoreOrderMgrFacade.Iface {
	
	private static final Log log = LogFactory.getLog(StoreOrderMgrFacadeImpl.class);

	@Autowired
	private StoreOrderService storeOrderService;
	
	@Autowired
    private StoreOrderQueryService storeOrderQueryService;

	@Autowired
	private StoreOrderMrgService storeOrderMrgService;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StoreOrderPriceHelper storeOrderPriceHelper;

	@Autowired
	private StoreOrderFacadeValidate storeOrderFacadeValidate;
	
	@Autowired
	private WechatQrcodeService wechatQrcodeService;
	
	@Autowired
	private WechatNotifyService wechatNotifyService;

	@ThriftClient
	private PayFacade.Iface payFacade;

	@ThriftClient
	private StoreSettingFacade.Iface storeSettingFacade;
	
	@ThriftClient
	private ElecInoviceFacade.Iface elecInoviceFacade;

	@ThriftClient
	private MerchantInvoiceFacade.Iface merchantInvoiceFacade;

	@Override
	public StoreOrderPageDTO getStoreOrders(QueryOrderParam param, int page, int size) throws TException {
		PageResult pageResult = this.storeOrderMrgService.getStoreOrders(param, page, size);
		StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
		List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(pageResult.getList());
		storeOrderPageDTO.setDataList(storeOrderDTOs);
		storeOrderPageDTO.setSize(size);
		storeOrderPageDTO.setPageNo(page);
		storeOrderPageDTO.setTotal(pageResult.getTotal());
		storeOrderPageDTO.setPageNum(pageResult.getTotalPage());
		return storeOrderPageDTO;
	}

	@Override
	public StoreOrderDTO updateStoreOrderInvoiced(StoreOrderInvoicedParam storeOrderInvoicedParam) throws T5weiException, TException {
		StoreOrder storeOrder = storeOrderMrgService.updateStoreOrderInvoiced(storeOrderInvoicedParam);
		return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
	}

	@Override
	public StoreOrderInvoiceDTO getStoreOrderInvoice(int merchantId, long storeId, String orderId) throws TException {
		StoreOrderInvoice storeOrderInvoice = storeOrderMrgService.getStoreOrderInvoice(merchantId, storeId, orderId);
		StoreOrderInvoiceDTO storeOrderInvoiceDTO = this.buildStoreOrderInvoiceDTO(merchantId, storeId, orderId, storeOrderInvoice);
		StoreInvoiceSettingDTO storeInvoiceSetting = storeSettingFacade.getStoreInvoiceSetting(merchantId, storeId);
		if (storeInvoiceSetting.isEnabledTax()) {
			boolean flowGz = storeInvoiceSetting.isAttentionWechat();
			Map<String, String> resultMap = wechatQrcodeService.getInvoiceQrcode(merchantId, storeId, orderId, flowGz);
			String qrcodeUrl = resultMap.get("qrcode_url");
			String qrcodeLink = resultMap.get("qrcode_link");
			storeOrderInvoiceDTO.setQrcodeUrl(qrcodeUrl);
			storeOrderInvoiceDTO.setQrcodeLink(qrcodeLink);
		}
		return storeOrderInvoiceDTO;
	}
	
	private StoreOrderInvoiceDTO buildStoreOrderInvoiceDTO(int merchantId, long storeId, String orderId, StoreOrderInvoice storeOrderInvoice) throws FacadePayOrderInvalidException, TException{
		if (storeOrderInvoice == null) {
			StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
			storeOrderInvoice = BeanUtil.copy(storeOrder, StoreOrderInvoice.class);
			String payOrderId = storeOrder.getPayOrderId();
			int creditStatus = storeOrder.getCreditStatus();
			if (creditStatus == StoreOrderCreditStatusEnum.NO_CREDIT.getValue() && DataUtil.isNotEmpty(payOrderId)) {
				PayResultOfPayOrder payResult = payFacade.getPayResultOfPayOrder(payOrderId);
				StoreOrderActualPayResult storeOrderActualPay = storeOrderPriceHelper.getStoreOrderActualPayInfo(storeOrder, payResult);
				storeOrderInvoice.setOrderPrice(storeOrderActualPay.getOrderPrice());
				storeOrderInvoice.setActualPrice(storeOrderActualPay.getActualPrice());
				storeOrderInvoice.setInvoicePrice(storeOrderActualPay.getActualPrice());
			} else {
				storeOrderInvoice.setOrderPrice(storeOrder.getOriginalPrice());
				storeOrderInvoice.setActualPrice(storeOrder.getPayablePrice());
				storeOrderInvoice.setInvoicePrice(storeOrder.getPayablePrice());
			}
		}
		StoreOrderInvoiceDTO storeOrderInvoiceDTO = BeanUtil.copy(storeOrderInvoice, StoreOrderInvoiceDTO.class);
		return storeOrderInvoiceDTO;
	}

	@Override
	public StoreOrderInvoiceDTO createUserOrderInvoice(UserOrderInvoiceParam param) throws T5weiException, TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		String orderId = param.getOrderId();
		StoreOrder storeOrder = storeOrderFacadeValidate.validateCreateUserOrderInvoice(param);
		StoreOrderInvoice storeOrderInvoice = storeOrderMrgService.getStoreOrderInvoice(merchantId, storeId, orderId);
		if (storeOrderInvoice != null && storeOrderInvoice.getInvoiceStatus() == MerchantInvoiceStatusEnum.FINISH.getValue()) {
			// 已经开过电子发票
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ELEC_INVOICE_HAS_OPENED.getValue(), DataUtil.infoWithParams("this store order has opened elecInvoice, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		storeOrderInvoice = BeanUtil.copy(param, StoreOrderInvoice.class);
		storeOrderInvoice.setUserId(param.getRequestUserId());
		String payOrderId = storeOrder.getPayOrderId();
		int creditStatus = storeOrder.getCreditStatus();
		if (creditStatus == StoreOrderCreditStatusEnum.NO_CREDIT.getValue() && DataUtil.isNotEmpty(payOrderId)) {
			PayResultOfPayOrder payResult = payFacade.getPayResultOfPayOrder(payOrderId);
			StoreOrderActualPayResult storeOrderActualPay = storeOrderPriceHelper.getStoreOrderActualPayInfo(storeOrder, payResult);
			storeOrderInvoice.setOrderPrice(storeOrderActualPay.getOrderPrice());
			storeOrderInvoice.setActualPrice(storeOrderActualPay.getActualPrice());
			storeOrderInvoice.setInvoicePrice(storeOrderActualPay.getActualPrice());
		} else {
			storeOrderInvoice.setOrderPrice(storeOrder.getOriginalPrice());
			storeOrderInvoice.setActualPrice(storeOrder.getPayablePrice());
			storeOrderInvoice.setInvoicePrice(storeOrder.getPayablePrice());
		}
		if (storeOrderInvoice.getInvoicePrice() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_INVOICE_OVER_TIME.getValue(), DataUtil.infoWithParams("this store order invoice price <= 0, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		try{
			if(param.isAsyncProcess()){
				storeOrderMrgService.saveUserOrderInvoice(storeOrderInvoice);
				// 异步开发票
				MerchantInvoiceParam merchantInvoiceParam = new MerchantInvoiceParam();
				merchantInvoiceParam.setMerchantId(merchantId);
				merchantInvoiceParam.setStoreId(storeId);
				merchantInvoiceParam.setSrc(PaySrcEnum.M_5WEI.getValue());
				merchantInvoiceParam.setSrcId(orderId);
				merchantInvoiceParam.setInvoiceTitle(param.getInvoiceTitle());
				merchantInvoiceParam.setInvoicePrice(storeOrderInvoice.getInvoicePrice());
				merchantInvoiceFacade.createInvoice(merchantInvoiceParam);
			}else{
				// 同步开发票
				ElecInvoiceParam elecInvoiceParam = new ElecInvoiceParam();
				elecInvoiceParam.setMerchantId(merchantId);
				elecInvoiceParam.setStoreId(storeId);
				elecInvoiceParam.setInvoiceTitle(param.getInvoiceTitle());
				elecInvoiceParam.setInvoicePrice(storeOrderInvoice.getInvoicePrice());
				ElecInvoiceDTO elecInvoiceDTO = elecInoviceFacade.createElecInvoice(elecInvoiceParam);
				BeanUtil.copy(elecInvoiceDTO, storeOrderInvoice);
				storeOrderMrgService.saveUserOrderInvoice(storeOrderInvoice);
			}
		} catch (TException e1) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_E_INVOICE_EXCEPTION.getValue(), DataUtil.infoWithParams("e_invoice exception, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		return BeanUtil.copy(storeOrderInvoice, StoreOrderInvoiceDTO.class);
	}
	
	@Override
	public void updateOrderInvoiceFinish(StoreOrderInvoiceFinishParam param) throws T5weiException, TException {
		storeOrderMrgService.updateUserOrderInvoice(param);
	}
	
	@Override
	public StoreOrderInvoiceDTO getStoreOrderInvoiceSimple(int merchantId, long storeId, String orderId) throws TException {
		StoreOrderInvoice storeOrderInvoice = storeOrderMrgService.getStoreOrderInvoice(merchantId, storeId, orderId);
		if (storeOrderInvoice == null) {
			storeOrderInvoice = new StoreOrderInvoice();
		}
		return BeanUtil.copy(storeOrderInvoice, StoreOrderInvoiceDTO.class);
	}
	
	@Override
	public StoreOrderInvoiceDTO getUserOrderInvoice(UserOrderInvoiceQueryParam param) throws T5weiException, TException {
		int merchantId = param.getMerchantId(); 
		long storeId = param.getStoreId(); 
		String orderId = param.getOrderId();
		StoreOrderInvoice storeOrderInvoice;
		StoreInvoiceSettingDTO storeInvoiceSetting = storeSettingFacade.getStoreInvoiceSetting(merchantId, storeId);
		if(storeInvoiceSetting.isLockUser()){
			StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
			storeOrderFacadeValidate.validateCreateUserOrderInvoice(storeOrder, storeInvoiceSetting, param.getQueryUserId());
			storeOrderInvoice = storeOrderMrgService.getUserOrderInvoice(param);
		}else{
			storeOrderInvoice = storeOrderMrgService.getStoreOrderInvoice(merchantId, storeId, orderId);
		}
		return this.buildStoreOrderInvoiceDTO(merchantId, storeId, orderId, storeOrderInvoice);
	}

	@Override
	public StoreOrderDTO updateStoreOrderLocked(StoreOrderLockedParam storeOrderLockedParam) throws T5weiException, TException {
		StoreOrder storeOrder = storeOrderMrgService.updateStoreOrderLocked(storeOrderLockedParam);
		if (storeOrder.getUserId() > 0){
			//订单锁定状态变更通知
			wechatNotifyService.sendOrderLockMsg(storeOrder);
		}
		return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
	}

}
