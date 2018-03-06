package com.huofu.module.i5wei.table.service;

import com.huofu.module.i5wei.setting.service.StoreDefinedPrinterService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.table.StoreAreaSaveParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofuhelper.util.bean.BeanUtil;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.huofu.module.i5wei.table.dao.StoreAreaDAO;
import com.huofu.module.i5wei.table.entity.StoreArea;
import com.huofu.module.i5wei.table.entity.StoreTable;

/**
 * Created by jiajin.nervous on 16/4/27.
 */
@Service
public class StoreAreaService {

    private final static Log log = LogFactory.getLog(StoreAreaService.class);

    @Autowired
    private StoreAreaDAO storeAreaDAO;

    @Autowired
    private StoreTableService storeTableService;

    @Autowired
    private StoreDefinedPrinterService storeDefinedPrinterService;

    //初始化默认区域
    public void initStoreArea(int merchantId,long storeId){
        StoreArea storeArea = StoreArea.createDefault(merchantId,storeId);
        storeArea.create();
    }

    /**
     * TODO 一次性拿出店铺下所有桌台，内存计算分配
     * @param merchantId
     * @param storeId
     * @param loadTables
     * @return
     * @throws TException
     */
    public List<StoreArea> getValidStoreAreas(int merchantId, long storeId, boolean loadTables) throws TException {
    	boolean enableSlave = false;
        List<StoreArea> storeAreas = storeAreaDAO.getValidStoreAreas(storeId, enableSlave);
        if(loadTables){
            for(StoreArea storeArea : storeAreas){
                List<StoreTable> storeTables = storeTableService.getValidStoreTables(merchantId,storeId,storeArea.getAreaId());
                storeArea.setStoreTableList(storeTables);
            }
        }
        return storeAreas;
    }

    /**
     * 查询有效区域
     * @throws TException 
     */
    public StoreArea getValidStoreAreaById(int merchantId, long storeId, long areaId, boolean loadTables) throws TException {
    	boolean enableSlave = false;
        StoreArea storeArea = storeAreaDAO.loadValidStoreAreaById(areaId, enableSlave);
        if(loadTables){
            //查询当前有效区域下的有效桌台列表
            List<StoreTable> storeTables = storeTableService.getValidStoreTables(merchantId,storeId,areaId);
            storeArea.setStoreTableList(storeTables);
        }
        return storeArea;
    }

    //保存
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreArea saveStoreArea(StoreAreaSaveParam param) throws TException {
    	boolean enableSlave = false;
        StoreArea storeArea;
        if(param.getAreaId() == 0){
            storeArea = _createValidStoreArea(param);
        }else{
            storeArea = storeAreaDAO.loadValidStoreAreaById(param.getAreaId(), enableSlave);
            storeArea = _updateValidStoreArea(param,storeArea);
        }
        return storeArea;
    }

    //创建
    private StoreArea _createValidStoreArea(StoreAreaSaveParam param) throws T5weiException {
    	boolean enableSlave = false;
        //校验
        storeAreaDAO.checkValidStoreAreaNameDuplicate(param);
        StoreArea deletedArea = storeAreaDAO.getStoreAreaByName(param.getAreaName(),param.getStoreId(), enableSlave);
        if(deletedArea != null){
            deletedArea.snapshot();
            deletedArea.setDeleted(false);
            deletedArea.setCreateTime(System.currentTimeMillis());
            deletedArea.setUpdateTime(System.currentTimeMillis());
            deletedArea.update();
            return deletedArea;
        }
        StoreArea storeArea = StoreArea._create(param.getMerchantId(),param.getStoreId());
        BeanUtil.copy(param,storeArea,true);
        try {
            storeArea.create();
        }catch (DuplicateKeyException e){
            throw new T5weiException(T5weiErrorCodeType.STORE_AREA_NAME_DUPLICATE.getValue(),"storeId[" + param.getStoreId() + "] ,merchantId[" +
                    param.getMerchantId() + "],areaName[" + param.getAreaName() + "] is duplicate");

        }
        return storeArea;
    }

    //修改
    private StoreArea _updateValidStoreArea(StoreAreaSaveParam param,StoreArea storeArea) throws T5weiException {
        storeArea.snapshot();

        //如果当前要修改的店铺区域名称和数据库查出来的店铺区域名称不相同,则执行checkStoreAreaNameDuplicate校验
        if(!param.getAreaName().equals(storeArea.getAreaName())){
            storeAreaDAO.checkValidStoreAreaNameDuplicate(param);
        }
        BeanUtil.copy(param,storeArea,true);

        try {
            storeArea.setUpdateTime(System.currentTimeMillis());
            storeArea.update();
        }catch (DuplicateKeyException e){
            throw new T5weiException(T5weiErrorCodeType.STORE_AREA_NAME_DUPLICATE.getValue(),"storeId[" + param.getStoreId() + "] ,merchantId[" +
                    param.getMerchantId() + "],areaName[" + param.getAreaName() + "] is duplicate");
        }
        return storeArea;
    }

	// 删除
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void deleteValidStoreArea(int merchantId, long storeId, long areaId) throws TException {
		StoreArea storeArea = this.getValidStoreAreaById(merchantId, storeId, areaId, true);
		// 最后判断当前要删除的区域下有没有桌台
		if (storeArea.getStoreTableList() != null && !storeArea.getStoreTableList().isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_AREA_HAS_TABLE_ERROR.getValue(), "merchantId[" + merchantId + "],storeId[" + storeId + "], areaId[" + areaId
					+ "] area's tables must = 0!");
		}
		// 其次保证当前店铺下有效的区域数量必须大于等于1
		List<StoreArea> storeAreas = this.getValidStoreAreas(merchantId, storeId, false);
		if (storeAreas.size() <= 1) {
			throw new T5weiException(T5weiErrorCodeType.STORE_AREA_NUM_ERROR.getValue(), "merchantId[" + merchantId + "],storeId[" + storeId + "], areaId[" + areaId + "] area num must >= 1!");
		}
		// 最后执行标记删除操作
		storeArea.snapshot();
		storeArea.setDeleted(true);
		storeArea.setUpdateTime(System.currentTimeMillis());// 标记删除时间
		storeArea.update();
        //删除（过滤）自定义打印中含有的该区域
        storeDefinedPrinterService.filterStoreDefinedPrinter(merchantId, storeId, areaId, PrintMsgTypeEnum.I5WEI_ORDER_MEAL.getValue());
	}

}
