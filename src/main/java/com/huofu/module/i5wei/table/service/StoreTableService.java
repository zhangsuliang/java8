package com.huofu.module.i5wei.table.service;

import com.huofu.module.i5wei.table.dao.StoreAreaDAO;
import com.huofu.module.i5wei.table.dao.StoreTableDAO;
import com.huofu.module.i5wei.table.entity.StoreTable;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.sharedto.I5weiUserDTO;
import huofucore.facade.i5wei.sharedto.StoreTableStaffDTO;
import huofucore.facade.i5wei.table.StoreTableSaveParam;
import huofucore.facade.idmaker.IdMakerFacade;
import huofucore.facade.merchant.staff.StaffDTO;
import huofucore.facade.merchant.staff.StaffFacade;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jiajin.nervous on 16/4/29.
 */
@Service
public class StoreTableService {

    private static final Log log = LogFactory.getLog(StoreTableService.class);

    @ThriftClient
    private IdMakerFacade.Iface idMakerFacadeIface;

    @ThriftClient
    private StaffFacade.Iface staffFacade;

    @Autowired
    private StoreAreaDAO storeAreaDAO;

    @Autowired
    private StoreTableDAO storeTableDAO;

    @Autowired
    private StoreTableRecordQueryService storeTableRecordQueryService;

    /**
     * 根据区域ID查询有效的区域下的有效的桌台列表(区域ID为0则查询店铺下的桌台列表)
     * @param merchantId
     * @param storeId
     * @param areaId
     * @return
     * @throws TException
     */
    public List<StoreTable> getValidStoreTables(int merchantId, long storeId, long areaId) throws TException {
    	boolean enableSlave = false;
        List<StoreTable> storeTables;
        if(areaId == 0){
            //查询店铺的所有桌台
            storeTables = storeTableDAO.getValidStoreTables(merchantId,storeId,enableSlave);
        }else{
            //查询店铺区域下所有的桌台
            storeAreaDAO.loadValidStoreAreaById(areaId ,enableSlave);
            storeTables = storeTableDAO.getValidStoreAreaTables(merchantId,storeId,areaId, enableSlave);
        }

        if(storeTables == null){
            return new ArrayList<>();
        }

        //设置店铺桌台的服务员信息
        this.setStoreTableStaffDTOs(merchantId,storeId,storeTables);

        return storeTables;
    }

    /**
     * 批量处理桌台服务员
     * @param merchantId
     * @param storeId
     * @param storeTables
     */
    private void setStoreTableStaffDTOs(int merchantId,long storeId,List<StoreTable> storeTables){
        List<Long> staffIds = new ArrayList<Long>();
        for (StoreTable storeTable : storeTables) {
            if (storeTable.getStaffId() > 0){
                if(!staffIds.contains(storeTable.getStaffId())){
                    staffIds.add(storeTable.getStaffId());
                }

            }
        }
        if (!staffIds.isEmpty()) {
            Map<Long, StaffDTO> staffDTOMap;
            try {
                staffDTOMap = staffFacade.getStaffMapInIds(merchantId, staffIds, true);
                for (StoreTable storeTable : storeTables) {
                    if (staffDTOMap.containsKey(storeTable.getStaffId())){
                        StaffDTO staffDTO = staffDTOMap.get(storeTable.getStaffId());
                        StoreTableStaffDTO storeTableStaffDTO = new StoreTableStaffDTO();
                        BeanUtil.copy(staffDTO,storeTableStaffDTO);
                        if(staffDTO.getUserDTO() != null){
                            I5weiUserDTO i5weiUserDTO = new I5weiUserDTO();
                            BeanUtil.copy(staffDTO.getUserDTO(),i5weiUserDTO);
                            storeTableStaffDTO.setI5weiUserDTO(i5weiUserDTO);
                        }
                        storeTable.setStoreTableStaffDTO(storeTableStaffDTO);
                    }
                }
            } catch (TException e) {

            }

        }
    }

    /**
     * 根据桌台ID查询有效的桌台
     * @param merchantId
     * @param storeId
     * @param tableId
     * @param forUpdate
     * @return
     * @throws TException
     */
    public StoreTable getValidStoreTableById(int merchantId,long storeId,long tableId,boolean forUpdate) throws TException {
        StoreTable storeTable = storeTableDAO.getValidStoreTableById(merchantId,storeId,tableId,forUpdate);
        if(storeTable == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_ID_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], tableId[" + tableId + "] is invalid!");
        }

        //店铺桌台的服务员信息
        List<StoreTable> storeTables = new ArrayList<StoreTable>();
        storeTables.add(storeTable);
        this.setStoreTableStaffDTOs(merchantId,storeId,storeTables);
        //查询桌台下是否有相关联的有效桌台记录，以判定桌台是否可以被删除
        List<StoreTableRecord> storeTableRecordList = storeTableRecordQueryService.getStoreTableRecordByTableId(merchantId, storeId, tableId);
        if(storeTableRecordList.isEmpty()){
            storeTable.setTableEnableDeleted(true);
        }
        return storeTable;
    }
    
    public StoreTable getStoreTableById(int merchantId,long storeId,long tableId,boolean forUpdate) throws TException {
    	StoreTable storeTable = storeTableDAO.getValidStoreTableById(merchantId,storeId,tableId,forUpdate);
        if(storeTable == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_ID_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], tableId[" + tableId + "] is invalid!");
        }
        //店铺桌台的服务员信息
        List<StoreTable> storeTables = new ArrayList<StoreTable>();
        storeTables.add(storeTable);
        this.setStoreTableStaffDTOs(merchantId,storeId,storeTables);
        return storeTable;
    }

    /**
     * 删除桌台
     * @param merchantId
     * @param storeId
     * @param tableId
     * @throws TException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteStoreTable(int merchantId,long storeId,long tableId) throws TException {
    	boolean enableSlave = false;
        //锁桌台
        StoreTable storeTable = this.getValidStoreTableById(merchantId,storeId,tableId,true);
        //查询桌台下是否有相关联的有效桌台记录
        List<StoreTableRecord> storeTableRecordList = storeTableRecordQueryService.getStoreTableRecordByTableId(merchantId, storeId, tableId);
        if(!storeTableRecordList.isEmpty()){
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_HAS_EFFECTIVE_TABLE_RECORD_ERROR.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], tableId[" + tableId + "],current table has effective tableRecord!");
        }
        storeTable.snapshot();
        storeTable.setUpdateTime(System.currentTimeMillis());
        storeTable.setDeleted(true);
        //对删除的桌台操作，给siteNumber做不重操作，保证数据库siteNumber字段的唯一性
        int minSiteNumber = storeTableDAO.getMinSiteNumber(storeTable.getMerchantId(),storeTable.getStoreId(),storeTable.getAreaId(), enableSlave);
        if(minSiteNumber >= 1){
            storeTable.setSiteNumber(-1);
        }else{
            storeTable.setSiteNumber(minSiteNumber - 1);
        }
        storeTableDAO.update(storeTable);
    }

    /**
     * 保存桌台
     * @param param
     * @return
     * @throws TException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreTable saveStoreTable(StoreTableSaveParam param) throws TException {
    	boolean enableSlave = false;
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long areaId = param.getAreaId();
        String paramTableName = param.getName();

        storeAreaDAO.loadValidStoreAreaById(areaId, enableSlave);
        StoreTable storeTable = new StoreTable();

        //创建桌台
        if(param.getTableId() == 0){
            //校验桌台名称是否重复
            storeTableDAO.checkValidTableNameDuplicate(merchantId,storeId,areaId,paramTableName);
            BeanUtil.copy(param,storeTable,true);
            try {
                //根据桌台名称，做出相应的变化
                this.checkNameAndSetSiteNumber(storeTable);
                //获取店铺区域下是否存在相同名字且被删除过的桌台
                StoreTable deletedTable = storeTableDAO.getStoreTableByName(storeTable.getName(),storeId,areaId,merchantId,enableSlave);
                if(deletedTable != null){
                    deletedTable.snapshot();
                    deletedTable.setSiteNumber(storeTable.getSiteNumber());
                    deletedTable.setStaffId(param.getStaffId());
                    deletedTable.setSeatNum(param.getSeatNum());
                    deletedTable.setUpdateTime(System.currentTimeMillis());
                    deletedTable.setDeleted(false);
                    storeTableDAO.update(deletedTable);
                    BeanUtil.copy(deletedTable,storeTable);
                }else{
                    //通过IdMaker获取唯一的tableId
                    long tableId = this.nextId(storeId);
                    storeTable.setTableId(tableId);
                    storeTable.setCreateTime(System.currentTimeMillis());
                    storeTable.setUpdateTime(System.currentTimeMillis());
                    storeTableDAO.create(storeTable);
                }

            } catch (DuplicateKeyException e){
                throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_NAME_DUPLICATE.getValue(),"merchantId[" + merchantId + "] ,store_id[" +
                        storeId + "],areaId[" + areaId + "],tableName ["+ paramTableName + "] is duplicate");
            }

        //修改桌台
        }else{
            //根据桌台Id查询桌台
            storeTable = this.getValidStoreTableById(merchantId,storeId,param.getTableId(),false);
            String storeTableName = storeTable.getName();
            storeTable.snapshot();
            BeanUtil.copy(param,storeTable,true);
	        //根据桌台名称查询是否有删除过的桌台
	        StoreTable deletedTable = storeTableDAO.getStoreTableByName(storeTable.getName(), storeId, areaId, merchantId, true);
	        //判断当前param参数与数据查出来的tableName是否相同
            if(!paramTableName.equals(storeTableName)){
                //校验桌台名称是否重复
                storeTableDAO.checkValidTableNameDuplicate(merchantId,storeId,areaId,paramTableName);
                //判断param参数里的name和数据库查出来的storeTable的name是否相同，如果相同，则不做name和siteNumber的相应变化，如果不同再做相应的变化
                this.checkNameAndSetSiteNumber(storeTable);
            }

            try{
                storeTable.setUpdateTime(System.currentTimeMillis());
                storeTableDAO.update(storeTable);
	            //删除同名且删除过的桌台
	            if (deletedTable != null) {
		            storeTableDAO.delete(deletedTable);
	            }
            }catch (DuplicateKeyException e){
                throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_NAME_DUPLICATE.getValue(),"merchantId[" + merchantId + "] ,store_id[" +
                        storeId + "],areaId[" + areaId + "],tableName ["+ paramTableName + "] is duplicate");
            }
        }
        //店铺桌台的服务员信息
        List<StoreTable> storeTables = new ArrayList<StoreTable>();
        storeTables.add(storeTable);
        this.setStoreTableStaffDTOs(merchantId,storeId,storeTables);
        return storeTable;

    }

    /**
     * 根据桌台名称做出相应变化
     * @param storeTable
     */
    private void checkNameAndSetSiteNumber(StoreTable storeTable){
    	boolean enableSlave = false;
        //桌台名称是否能被正确解析
        if(this.siteNumberParser(storeTable)){
            storeTable.setStoreTableName();
        }else{
            //如果桌台名称不为空，取到当前条件下最小的siteNumber,然后将siteNumber置为 minSiteNumber - 1
            int minSiteNumber = storeTableDAO.getMinSiteNumber(storeTable.getMerchantId(),storeTable.getStoreId(),storeTable.getAreaId(), enableSlave);
            if(minSiteNumber >= 0){
                storeTable.setSiteNumber(-1);
            }else {
                //如果minSiteNumber和当前的siteNumber不相同，才做操作
                if(minSiteNumber != storeTable.getSiteNumber()){
                    storeTable.setSiteNumber(minSiteNumber - 1);
                }
            }
        }

    }

    /**
     * 获取idMaker生成的唯一桌台ID
     * @param storeId
     * @return
     * @throws T5weiException
     */
    private long nextId(long storeId) throws T5weiException {
        //需要捕获异常，重试机制
        long tableSeq = 0;
        for(int i = 0; i < 4; i++){
            try {
                tableSeq = this.idMakerFacadeIface.getNextId2("tb_store_table_seq");
                break;
            } catch (Exception e) {
                if(i == 3){
                    throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_CREATE_ERROR.getValue(),"create table failed，because request IdMaker failed");
                }else{
                    continue;
                }
            }
        }
        return tableSeq;
    }


    public StoreTable getStoreTableBySiteNumber(int merchantId,long storeId,int siteNumber) throws TException {
    	boolean enableSlave = false;
        List<StoreTable> storeTables = storeTableDAO.getStoreTablesBySiteNumber(merchantId,storeId,siteNumber, enableSlave);
        StoreTable storeTable;
        if(storeTables.isEmpty()){
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_SITE_NUMBER_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], siteNumber[" + siteNumber + "] is invalid!");
        }else{
            storeTable = storeTables.get(0);
        }

        //店铺桌台的服务员信息
        List<StoreTable> storeTableList = new ArrayList<StoreTable>();
        storeTables.add(storeTable);
        this.setStoreTableStaffDTOs(merchantId,storeId,storeTableList);
        return storeTable;

    }

    /**
     * 根据name解析设置siteNumber
     * @param storeTable
     */
    private boolean siteNumberParser(StoreTable storeTable){
        String name = storeTable.getName();
        if(name.indexOf("号桌") != -1){
            String suffix = name.substring(name.indexOf("桌") + 1);//截取“桌”后面字符串
            String prefix = name.substring(0,name.indexOf("号"));//截“号”前面的字符串
            if(DataUtil.isEmpty(suffix)){
                try{
                    int siteNumber = Integer.parseInt(prefix);
                    storeTable.setSiteNumber(siteNumber);
                    return true;
                }catch (NumberFormatException e){
                }

            }
        }
        return false;

    }

    /**
     * 获取创建桌台时的默认信息,桌台名称，座位数
     * @param merchantId
     * @param storeId
     * @param areaId
     * @return
     */
    public StoreTable getCreateStoreTableDefaultInfo(int merchantId,long storeId,long areaId) throws TException {
    	boolean enableSlave = false;
        StoreTable storeTable = new StoreTable();
        List<StoreTable> storeTables = this.getValidStoreTables(merchantId, storeId, areaId);
        String tableName = null;
        int seatNum;
        if(!storeTables.isEmpty()){
            seatNum = storeTables.get(storeTables.size() - 1).getSeatNum();
            int maxSiteNumber = storeTableDAO.getValidMaxSiteNumber(merchantId, storeId, areaId, enableSlave);
            if(maxSiteNumber <= 0){
                tableName = 1 + "号桌";
            }else{
                tableName = maxSiteNumber + 1 + "号桌";
            }
        }else{
            seatNum = 4;
            tableName = "1号桌";
        }
        storeTable.setMerchantId(merchantId);
        storeTable.setStoreId(storeId);
        storeTable.setAreaId(areaId);
        storeTable.setSeatNum(seatNum);
        storeTable.setName(tableName);
        return storeTable;
    }

    public void dealStoreTable(int merchantId,List<Long> storeIds,long staffId){
        if(storeIds != null && !storeIds.isEmpty()){
            for(Long storeId : storeIds){
                List<StoreTable> storeTables = storeTableDAO.getDealStoreTables(merchantId,storeId);
                List<StoreTable> dealStoreTables = new ArrayList<>();
                for(StoreTable storeTable : storeTables){
                    if(staffId == storeTable.getStaffId()){
                        storeTable.setUpdateTime(System.currentTimeMillis());
                        dealStoreTables.add(storeTable);
                    }
                }
                storeTableDAO.dealStoreTable(merchantId,storeId,dealStoreTables);
            }
        }
    }

}
