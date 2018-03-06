package com.huofu.module.i5wei.table.dao;

import huofuhelper.util.AbsQueryDAO;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.table.entity.TableRecordBatchRefundRecord;
import com.huofu.module.i5wei.table.service.TableRecordBatchRefundStatusEnum;

/**
 * 桌台记录批量退款记录
 * @author licheng7
 * 2016年7月10日 上午11:24:07
 */
@Repository
public class TableRecordBatchRefundRecordDAO extends AbsQueryDAO<TableRecordBatchRefundRecord> {

	public TableRecordBatchRefundRecord getTableRecordBatchRefundRecordById (long batchRefundId) {
		return this.query.objById(TableRecordBatchRefundRecord.class, batchRefundId);
	}
	
	public List<TableRecordBatchRefundRecord> getSuccessTableRecordBatchRefundByTableRecordId (long tableRecordId) {
		return this.query.list(TableRecordBatchRefundRecord.class, " where table_record_id=? and status=?", new Object[]{tableRecordId, TableRecordBatchRefundStatusEnum.FINISH.getValue()});
	}
	
	public List<TableRecordBatchRefundRecord> getSettleTableRecordBatchRefund (long tableRecordId) {
		return this.query.list(TableRecordBatchRefundRecord.class, " where table_record_id=? and type=?", new Object[]{tableRecordId, 1});
	}
}
