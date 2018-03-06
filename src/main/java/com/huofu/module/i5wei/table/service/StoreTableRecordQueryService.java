package com.huofu.module.i5wei.table.service;

import huofucore.facade.i5wei.exception.T5weiException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.dao.StoreTableRecordRefundDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

/**
 * 桌台记录业务查询服务类
 * @author chenkai
 * 2016年5月12日 上午10:15:27
 */
@Service
public class StoreTableRecordQueryService {
	
	@Autowired
	private StoreTableRecordDAO storeTableRecordDAO;
	@Autowired
	private StoreOrderDAO storeOrderDAO;
	@Autowired
	private StoreTableRecordRefundDAO storeTableRecordRefundDAO;
	
	/**
	 * 根据id获取桌台记录
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @return
	 * @throws T5weiException 
	 */
	public StoreTableRecord getStoreTableRecordById (int merchantId,
			long storeId, long tableRecordId) throws T5weiException {
		return storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, false);
	}
	
	/**
	 * 查询桌台下是否有尚未清台的桌台记录
	 * @param merchantId
	 * @param storeId
	 * @param tableId
	 * @return
	 */
	public List<StoreTableRecord> getStoreTableRecordByTableId (int merchantId, long storeId,long tableId) {
		return storeTableRecordDAO.getStoreTableRecordListByTableIdSeqDesc(merchantId, storeId, tableId, false);
	}
}
