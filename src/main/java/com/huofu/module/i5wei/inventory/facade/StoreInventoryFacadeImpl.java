package com.huofu.module.i5wei.inventory.facade;

import com.huofu.module.i5wei.base.BatchResult;
import com.huofu.module.i5wei.inventory.dao.StoreInventoryDateDAO;
import com.huofu.module.i5wei.inventory.entity.StoreInventory;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryWeek;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreProductService;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.inventory.*;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofucore.facade.i5wei.menu.StoreProductDTO;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ThriftServlet(name = "storeInventoryFacadeServlet", serviceClass = StoreInventoryFacade.class)
@Component
public class StoreInventoryFacadeImpl implements StoreInventoryFacade.Iface {

    @Autowired
    private StoreInventoryService storeInventoryService;

    @Autowired
    private StoreChargeItemService storeChargeItemService;
    
    @Autowired
    private StoreTimeBucketService storeTimeBucketService;
    
    @Autowired
    private StoreInventoryDateDAO storeInventoryDateDao;

    @Autowired
    private StoreProductService storeProductService;
    
    @Autowired
    private StoreInventoryFacadeValidate storeInventoryFacadeValidate;

    @Override
    public StoreInventoryDTO getStoreInventory(int merchantId, long storeId, long productId) throws T5weiException, TException {
    	StoreInventory storeInventory = storeInventoryService.getStoreInventory(merchantId, storeId, productId);
    	return this.getStoreInventoryDTO(storeInventory);
    }

    @Override
    public StoreInventoryDTO updateStoreInventory(StoreInventoryUpdateParam storeInventoryUpdateParam) throws T5weiException, TException {
		// 更新库存设置
    	StoreInventory storeInventory = storeInventoryService.updateStoreInventory(storeInventoryUpdateParam);
		// 刷新当天库存盘点数据
		if (storeInventory.getStoreProduct().isInvEnabled()) {
			int merchantId = storeInventoryUpdateParam.getMerchantId(); 
	        long storeId = storeInventoryUpdateParam.getStoreId();
	        long productId = storeInventoryUpdateParam.getProductId();
	        long time = System.currentTimeMillis();
	        long repastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
	        long timeBucketId = 0L;
	        try{
	        	StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucketForDate(merchantId, storeId, 0, time);
		        timeBucketId = storeTimeBucket.getTimeBucketId();
	        }catch(T5weiException e){
	        	List<StoreTimeBucket> storeTimeBuckets = storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, repastDate);
				if (storeTimeBuckets != null && !storeTimeBuckets.isEmpty()){
					timeBucketId = storeTimeBuckets.get(0).getTimeBucketId();
	        	}
	        }
	        List<Long> productIds = new ArrayList<Long>();
	        productIds.add(productId);	        
	        storeInventoryService.updateInventoryDate(merchantId, storeId, repastDate, timeBucketId, productIds);
        }
        return this.getStoreInventoryDTO(storeInventory);
    }
    
    private StoreInventoryDTO getStoreInventoryDTO(StoreInventory storeInventory){
		if (storeInventory == null) {
			return null;
		}
		StoreInventoryDTO storeInventoryDTO = BeanUtil.copy(storeInventory, StoreInventoryDTO.class);
		if (storeInventory.getStoreProduct() != null) {
			StoreProductDTO storeProductDTO = BeanUtil.copy(storeInventory.getStoreProduct(), StoreProductDTO.class);
			storeInventoryDTO.setStoreProductDTO(storeProductDTO);
		}
		return storeInventoryDTO;
    }

    @Override
    public StoreInventoryWeekResult getInventoryWeekByProduct(StoreInventoryWeekQueryParam storeInventoryWeekQueryParam) throws TException {
        Map<String, List<StoreInventoryWeek>> resultMap = storeInventoryService.getInventoryWeekByProduct(storeInventoryWeekQueryParam);
        List<StoreInventoryWeek> currentTimeList = resultMap.get("currentTime");
        List<StoreInventoryWeek> nextWeekList = resultMap.get("nextWeek");
        StoreInventoryWeekResult storeInventoryWeekResult = new StoreInventoryWeekResult();
        storeInventoryWeekResult.setCurrentTimeList(this.getinventoryWeekDTOList(currentTimeList));
        storeInventoryWeekResult.setNextWeekList(this.getinventoryWeekDTOList(nextWeekList));
        return storeInventoryWeekResult;
    }

    private List<StoreInventoryWeekDTO> getinventoryWeekDTOList(List<StoreInventoryWeek> inventoryWeekList) {
        List<StoreInventoryWeekDTO> inventoryWeekDTOList = new ArrayList<StoreInventoryWeekDTO>();
        if (inventoryWeekList == null || inventoryWeekList.isEmpty()) {
            return inventoryWeekDTOList;
        }
        for (StoreInventoryWeek storeInventoryWeek : inventoryWeekList) {
            StoreInventoryWeekDTO storeInventoryWeekDTO = BeanUtil.copy(storeInventoryWeek, StoreInventoryWeekDTO.class);
            StoreTimeBucket storeTimeBucket = storeInventoryWeek.getStoreTimeBucket();
            if (storeTimeBucket != null) {
                storeInventoryWeekDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
            }
            inventoryWeekDTOList.add(storeInventoryWeekDTO);
        }
        return inventoryWeekDTOList;
    }

    @Override
    public StoreInventoryWeekUpdateResult updateInventoryWeek(StoreInventoryWeekUpdateParam storeInventoryWeekUpdateParam) throws T5weiException, TException {
    	storeInventoryFacadeValidate.validateStoreInventoryWeekUpdateParam(storeInventoryWeekUpdateParam);
        List<BatchResult<StoreInventoryWeekItemParam>> resultList = storeInventoryService.updateInventoryWeek(storeInventoryWeekUpdateParam);
        return this.getStoreInventoryWeekUpdateResult(storeInventoryWeekUpdateParam, resultList);
    }

    private StoreInventoryWeekUpdateResult getStoreInventoryWeekUpdateResult(StoreInventoryWeekUpdateParam storeInventoryWeekUpdateParam, List<BatchResult<StoreInventoryWeekItemParam>> resultList) {
        //封装返回结果
        StoreInventoryWeekUpdateResult storeInventoryWeekUpdateResult = new StoreInventoryWeekUpdateResult();
        storeInventoryWeekUpdateResult.setMerchantId(storeInventoryWeekUpdateParam.getMerchantId());
        storeInventoryWeekUpdateResult.setStoreId(storeInventoryWeekUpdateParam.getStoreId());
        storeInventoryWeekUpdateResult.setProductId(storeInventoryWeekUpdateParam.getProductId());
        List<StoreInventoryWeekItemResult> storeInventoryWeekItemResults = new ArrayList<StoreInventoryWeekItemResult>();
        for (BatchResult<StoreInventoryWeekItemParam> resultItem : resultList) {
            int errorCode = resultItem.getErrorCode();
            StoreInventoryWeekItemParam storeInventoryWeekItemParam = resultItem.getObj();
            StoreInventoryWeekItemResult storeInventoryWeekItemResult = new StoreInventoryWeekItemResult();
            storeInventoryWeekItemResult.setErrorCode(errorCode);
            storeInventoryWeekItemResult.setStoreInventoryWeekItemParam(storeInventoryWeekItemParam);
            storeInventoryWeekItemResults.add(storeInventoryWeekItemResult);
        }
        storeInventoryWeekUpdateResult.setStoreInventoryWeekItemResults(storeInventoryWeekItemResults);
        return storeInventoryWeekUpdateResult;
    }

    @Override
    public List<StoreInventoryDateDTO> getInventoryDate(StoreInventoryDateQueryParam storeInventoryDateQueryParam) throws T5weiException, TException {
        int merchantId = storeInventoryDateQueryParam.getMerchantId();
        long storeId = storeInventoryDateQueryParam.getStoreId();
        long repastDate = DateUtil.getBeginTime(storeInventoryDateQueryParam.getRepastDate(), null);
        long timeBucketId = storeInventoryDateQueryParam.getTimeBucketId();
        ProductInvTypeEnum queryInvType = storeInventoryDateQueryParam.getInvType();
        //销售中的产品
        List<StoreProduct> productsInSell = storeChargeItemService.getStoreProductsForDate(merchantId, storeId, repastDate, timeBucketId, false, false);
        List<StoreInventoryDate> resultInventoryDateList = storeInventoryService.getInventoryDate(merchantId, storeId, repastDate, timeBucketId, productsInSell, queryInvType);
        if (resultInventoryDateList == null || resultInventoryDateList.isEmpty()) {
            return new ArrayList<StoreInventoryDateDTO>();
        }
        List<StoreInventoryDateDTO> resultInventoryDateDTOList = new ArrayList<StoreInventoryDateDTO>();
        for (StoreInventoryDate storeInventoryDate : resultInventoryDateList) {
            StoreInventoryDateDTO storeInventoryDateDTO = BeanUtil.copy(storeInventoryDate, StoreInventoryDateDTO.class);
            storeInventoryDateDTO.setStoreProductDTO(BeanUtil.copy(storeInventoryDate.getStoreProduct(), StoreProductDTO.class));
            resultInventoryDateDTOList.add(storeInventoryDateDTO);
        }
        return resultInventoryDateDTOList;
    }

    @Override
    public StoreInventoryDateDTO updateInventoryDateAmount(StoreInventoryDateAmountUpdateParam storeInventoryDateAmountUpdateParam) throws T5weiException, TException {
    	storeInventoryFacadeValidate.validateStoreInventoryDateAmountUpdateParam(storeInventoryDateAmountUpdateParam);
        StoreInventoryDate storeInventoryDate = storeInventoryService.updateInventoryDateAmount(storeInventoryDateAmountUpdateParam);
        StoreInventoryDateDTO storeInventoryDateDTO = BeanUtil.copy(storeInventoryDate, StoreInventoryDateDTO.class);
        storeInventoryDateDTO.setStoreProductDTO(BeanUtil.copy(storeInventoryDate.getStoreProduct(), StoreProductDTO.class));
        return storeInventoryDateDTO;
    }
    
    @Override
	public void updateInventoryDateNothingness(StoreInventoryDateNothingnessUpdateParam storeInventoryDateNothingnessUpdateParam) throws T5weiException, TException {
    	storeInventoryFacadeValidate.ValidateStoreInventoryDateNothingnessUpdateParam(storeInventoryDateNothingnessUpdateParam);
    	storeInventoryService.updateInventoryDateNothingness(storeInventoryDateNothingnessUpdateParam);
	}
    
	@Override
	public ProductInventoryPrecisionInfoDTO getProductInventoryPrecisionInfoDTO(int merchantId, long storeId, long date) throws T5weiException, TException {
		long selectedDate = DateUtil.getBeginTime(date, null);
		List<StoreTimeBucket> timeBuckets = storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, selectedDate);
		ProductInventoryPrecisionInfoDTO infoDTO = new ProductInventoryPrecisionInfoDTO();
		if(timeBuckets.isEmpty()){
			infoDTO.setSendStatus(false);
			return infoDTO;
		}
		
		ProductInventoryPrecisionDTO productInventoryPrecision = storeInventoryService.getProductInventoryPrecision(merchantId, storeId, timeBuckets, selectedDate);
		if(!productInventoryPrecision.isSendStatus()){
			infoDTO.setSendStatus(false);
		} else {
			infoDTO.setMerchantId(merchantId);
			infoDTO.setStoreId(storeId);
			infoDTO.setStoreName(productInventoryPrecision.getStoreName());
			infoDTO.setSendDate(selectedDate);
			infoDTO.setTotalPrecision(productInventoryPrecision.getTotalPrecision());
			infoDTO.setPrecisionMessage(this.getPrecisionMessage(productInventoryPrecision.getTotalPrecision()));
			infoDTO.setSendStatus(true);
		}
		return infoDTO;
	}

	@Override
	public ProductInventoryPrecisionDTO getProductInventoryPrecisionDTO(int merchantId, long storeId, long date) throws T5weiException, TException {
		long selectedDate = DateUtil.getBeginTime(date, null);
		List<StoreTimeBucket> timeBuckets = storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, selectedDate);
		ProductInventoryPrecisionDTO productInventoryPrecision = new ProductInventoryPrecisionDTO();
		if(timeBuckets.isEmpty()){
			return productInventoryPrecision.setSendStatus(false);
		}
		productInventoryPrecision = storeInventoryService.getProductInventoryPrecision(merchantId, storeId, timeBuckets, selectedDate);
		if(!productInventoryPrecision.isSendStatus()){
			return productInventoryPrecision.setSendStatus(false);
		}
		return productInventoryPrecision;
	}

	/**
	 * 获取库存信息提示
	 * @param totalPrecision
	 * @return
	 */
	private String getPrecisionMessage(double totalPrecision){
		if(totalPrecision < 0.9){
			return "您的库存计划和实际销售偏差较大，请调整库存计划和备货！";
		}
		return "您的库存计划的准确率较高，请继续保持！";
	}

}
