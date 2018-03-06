package com.huofu.module.i5wei.table.facade;

import com.huofu.module.i5wei.table.dao.StoreAreaDAO;
import com.huofu.module.i5wei.table.entity.StoreArea;
import com.huofu.module.i5wei.table.entity.StoreTable;
import com.huofu.module.i5wei.table.service.StoreAreaService;
import com.huofu.module.i5wei.table.service.StoreTableQrcodeService;
import com.huofu.module.i5wei.table.service.StoreTableService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.sharedto.StoreTableStaffDTO;
import huofucore.facade.i5wei.table.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jiajin.nervous on 16/4/26.
 */
@Component
@ThriftServlet(name = "storeTableFacadeServlet", serviceClass = StoreTableFacade.class)
public class StoreTableFacadeImpl implements StoreTableFacade.Iface{

    private final static Log log = LogFactory.getLog(StoreTableFacadeImpl.class);

    @Autowired
    private StoreAreaService storeAreaService;

    @Autowired
    private StoreTableService storeTableService;

    @Autowired
    private StoreAreaDAO storeAreaDAO;

    @Autowired
    private StoreTableQrcodeService storeTableQrcodeService;

    @Override
    public List<StoreAreaDTO> getStoreAreas(int merchantId, long storeId, boolean loadTables) throws TException {
        this.checkParam(merchantId, storeId);
        List<StoreArea> storeAreas = storeAreaService.getValidStoreAreas(merchantId,storeId,loadTables);
        List<StoreAreaDTO> storeAreaDTOs = new ArrayList<StoreAreaDTO>();

        for(StoreArea storeArea : storeAreas){
            StoreAreaDTO storeAreaDTO = new StoreAreaDTO();
            BeanUtil.copy(storeArea,storeAreaDTO);
            List<StoreTable> storeTableList = storeArea.getStoreTableList();
            storeAreaDTO = this._buildStoreAreaDTO(storeAreaDTO,storeTableList);
            storeAreaDTOs.add(storeAreaDTO);
        }

        return storeAreaDTOs;
    }

    @Override
    public StoreAreaDTO getStoreAreaById(int merchantId, long storeId, long areaId, boolean loadTables) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(areaId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], areaId[" + areaId + "] is invalid!");
        }
        StoreArea storeArea = storeAreaService.getValidStoreAreaById(merchantId,storeId,areaId,loadTables);
        StoreAreaDTO storeAreaDTO = new StoreAreaDTO();
        BeanUtil.copy(storeArea,storeAreaDTO);

        List<StoreTable> storeTableList = storeArea.getStoreTableList();
        storeAreaDTO = this._buildStoreAreaDTO(storeAreaDTO,storeTableList);
        return storeAreaDTO;
    }

    @Override
    public StoreAreaDTO saveStoreArea(StoreAreaSaveParam param) throws T5weiException, TException {
        this.checkParam(param.getMerchantId(), param.getStoreId());
        if(param.getAreaId() < 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaId[" + param.getAreaId() + "] is invalid!");
        }
        if(DataUtil.isEmpty(param.getAreaName())){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaName[" + param.getAreaName() + "] is null!");
        }
        if(param.getAreaName().length() > 40){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaName[" + param.getAreaName() + "] length must < 40!");
        }
        StoreArea storeArea = storeAreaService.saveStoreArea(param);
        return BeanUtil.copy(storeArea,StoreAreaDTO.class);
    }

    @Override
    public void deleteStoreArea(int merchantId, long storeId, long areaId) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(areaId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], areaId[" + areaId + "] is invalid!");
        }
        storeAreaService.deleteValidStoreArea(merchantId,storeId,areaId);
    }

    @Override
    public List<StoreTableDTO> getStoreTables(int merchantId, long storeId, long areaId) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(areaId < 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], areaId[" + areaId + "] is invalid!");
        }
        List<StoreTable> storeTables = storeTableService.getValidStoreTables(merchantId,storeId,areaId);
        List<StoreTableDTO> storeTableDTOs = new ArrayList<StoreTableDTO>();
        for(StoreTable storeTable : storeTables){
            StoreTableDTO storeTableDTO =  this._buildStoreTableDTO(storeTable);
            this._setStoreAreaDTO(storeTableDTO);
            storeTableDTOs.add(storeTableDTO);

        }
        this._setStoreAreaDTOs(storeId,storeTableDTOs);
        return storeTableDTOs;
    }

    @Override
    public StoreTableDTO getStoreTableById(int merchantId, long storeId, long tableId) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(tableId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], tableId[" + tableId + "] is invalid!");
        }
        StoreTable storeTable = storeTableService.getValidStoreTableById(merchantId,storeId,tableId,false);
        StoreTableDTO storeTableDTO = this._buildStoreTableDTO(storeTable);
        this._setStoreAreaDTO(storeTableDTO);
        //获取指定桌台二维码
        String markUrl = storeTableQrcodeService.getTableQrcode(merchantId, storeId, tableId);
        storeTableDTO.setMarkUrl(markUrl);
        return storeTableDTO;
    }

    @Override
    public StoreTableDTO saveStoreTable(StoreTableSaveParam param) throws T5weiException, TException {
        this.checkParam(param.getMerchantId(), param.getStoreId());
        if(param.getTableId() < 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaId[" + param.getAreaId() + "],tableId [" + param.getTableId() + "]is invalid!");
        }
        if(param.getAreaId() <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaId[" + param.getAreaId() + "] is invalid!");
        }
        if(DataUtil.isEmpty(param.getName())){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaId[" + param.getAreaId() + "],tableName [" + param.getName() + "] is null !");
        }
        if(DataUtil.isNotEmpty(param.getName()) && param.getName().length() > 40){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaId[" + param.getAreaId() + "],tableName [" + param.getName() + "] length must < 40 !");
        }
        if(param.getSeatNum() > 200){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "],storeId[" + param.getStoreId() + "], areaId[" + param.getAreaId() + "],tableId [" + param.getTableId() + "],seatNum ["+ param.getSeatNum() + "] must < 256!");
        }
        StoreTable storeTable = storeTableService.saveStoreTable(param);
        StoreTableDTO storeTableDTO = this._buildStoreTableDTO(storeTable);
        this._setStoreAreaDTO(storeTableDTO);
        return storeTableDTO;
    }

    @Override
    public void deleteStoreTable(int merchantId, long storeId, long tableId) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(tableId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], tableId[" + tableId + "] is invalid!");
        }
        storeTableService.deleteStoreTable(merchantId,storeId,tableId);
    }

    @Override
    public StoreTableDTO getStoreTableBySiteNumber(int merchantId, long storeId,int siteNumber) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(siteNumber <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], siteNumber[" + siteNumber + "] is invalid!");
        }
        StoreTable storeTable = storeTableService.getStoreTableBySiteNumber(merchantId,storeId,siteNumber);
        StoreTableDTO storeTableDTO = this._buildStoreTableDTO(storeTable);
        this._setStoreAreaDTO(storeTableDTO);
        return storeTableDTO;
    }

    @Override
    public StoreTableDTO getCreateStoreTableDefaultInfo(int merchantId, long storeId, long areaId) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(areaId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "],storeId[" + storeId + "], areaId[" + areaId + "] is invalid!");
        }
        StoreTable storeTable = storeTableService.getCreateStoreTableDefaultInfo(merchantId, storeId, areaId);
        return this._buildStoreTableDTO(storeTable);
    }

    @Override
    public void dealStoreTable(int merchantId, List<Long> storeIds, long staffId) throws T5weiException, TException {
        if(merchantId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "] is invalid!");
        }
        if(staffId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "], staffId[" + staffId + "] is invalid!");
        }
        storeTableService.dealStoreTable(merchantId, storeIds, staffId);
    }

    @Override
    public TableQrcodeDownloadInfoDTO getTableQrcodeBatchDownloadInfo(int merchantId, long storeId, long expireSeconds) throws T5weiException, TException {
        this.checkParam(merchantId, storeId);
        if(expireSeconds <= 0){
            expireSeconds = 2*24*60*60;
        }
        Map<String,Object> map = storeTableQrcodeService.tableQrcodeDownload(merchantId, storeId, expireSeconds);
        return this._buildTableQrcodeDownloadInfoDTO(merchantId,storeId,map);
    }


    /**
     * 构建 StoreTableDTO
     * @param storeTable
     * @return
     */
    private StoreTableDTO _buildStoreTableDTO(StoreTable storeTable){
        StoreTableDTO storeTableDTO = new StoreTableDTO();
        BeanUtil.copy(storeTable,storeTableDTO);
        StoreTableStaffDTO storeTableStaffDTO = storeTable.getStoreTableStaffDTO();
        storeTableDTO.setStoreTableStaffDTO(storeTableStaffDTO);
        return storeTableDTO;
    }

    /**
     * 构建 StoreAreaDTO
     * @param storeAreaDTO
     * @param storeTableList
     * @return
     */
    private StoreAreaDTO _buildStoreAreaDTO(StoreAreaDTO storeAreaDTO,List<StoreTable> storeTableList){
        if(storeTableList != null && !storeTableList.isEmpty()){
            List<StoreTableDTO> storeTableDTOs = new ArrayList<StoreTableDTO>();
            for(StoreTable storeTable : storeTableList){
                StoreTableDTO storeTableDTO =  this._buildStoreTableDTO(storeTable);
                storeTableDTOs.add(storeTableDTO);
            }
            storeAreaDTO.setStoreTables(storeTableDTOs);
        }
        return storeAreaDTO;
    }

    /**
     * StoreTableDTO设置（set）StoreAreaDTO
     * @param storeTableDTO
     * @throws T5weiException
     */
    private void _setStoreAreaDTO(StoreTableDTO storeTableDTO) throws T5weiException {
    	boolean enableSlave = false;
        long areaId = storeTableDTO.getAreaId();
        StoreArea storeArea = storeAreaDAO.loadValidStoreAreaById(areaId, enableSlave);
        if(areaId > 0 && storeArea != null){
            StoreAreaDTO storeAreaDTO = new StoreAreaDTO();
            BeanUtil.copy(storeArea,storeAreaDTO);
            storeTableDTO.setStoreAreaDTO(storeAreaDTO);
        }
    }

    /**
     * 设置List<StoreTableDTO>里的每个StoreAreaDTO
     * @param storeId
     * @param storeTableDTOs
     * @throws T5weiException
     */
    private void _setStoreAreaDTOs(long storeId,List<StoreTableDTO> storeTableDTOs) throws T5weiException {
        if(storeTableDTOs.isEmpty()){
            return;
        }
        boolean enableSlave = false;
        Map<Long,StoreArea> map = storeAreaDAO.getStoreAreaMapByStoreId(storeId, enableSlave);
        if(!map.isEmpty()){
            for(StoreTableDTO storeTableDTO : storeTableDTOs){
                StoreArea storeArea = map.get(storeTableDTO.getAreaId());
                if(storeArea != null){
                    StoreAreaDTO storeAreaDTO = new StoreAreaDTO();
                    BeanUtil.copy(storeArea,storeAreaDTO);
                    storeTableDTO.setStoreAreaDTO(storeAreaDTO);
                }
            }
        }
    }

    /**
     * 校验merchantId，storeId
     * @param merchantId
     * @param storeId
     * @throws T5weiException
     */
    private void checkParam(int merchantId,long storeId) throws T5weiException {
        if(merchantId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + merchantId + "] is invalid!");
        }
        if(storeId <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"storeId[" + storeId + "] is invalid!");
        }
    }

    /**
     * 构建桌台二维码下载信息TableQrcodeDownloadInfoDTO
     * @param merchantId
     * @param storeId
     * @param map
     * @return
     * @throws T5weiException
     */
    private TableQrcodeDownloadInfoDTO _buildTableQrcodeDownloadInfoDTO(int merchantId,long storeId,Map<String,Object> map) throws T5weiException {

        if(map == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_QRCODE_DOWNLOAD_ERROR.getValue(),"request qrcode download failed!");
        }

        String password = null;
        long expireDate = 0L;

        if(map.get("password") != null){
            password = map.get("password").toString();
        }
        if(map.get("expire_date") != null){
            expireDate = Long.valueOf(map.get("expire_date").toString());
        }

        TableQrcodeDownloadInfoDTO tableQrcodeDownloadInfoDTO = new TableQrcodeDownloadInfoDTO();
        tableQrcodeDownloadInfoDTO.setMerchantId(merchantId);
        tableQrcodeDownloadInfoDTO.setStoreId(storeId);
        tableQrcodeDownloadInfoDTO.setPassword(password);
        tableQrcodeDownloadInfoDTO.setExpireDate(expireDate);

        return tableQrcodeDownloadInfoDTO;
    }

}
