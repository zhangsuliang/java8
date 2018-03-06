package com.huofu.module.i5wei.menu.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.base.SnsPublish;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.eventtype.EventType;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketSave;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.*;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by akwei on 2/15/15.
 */
@Component
@ThriftServlet(name = "storeTimeBucketFacadeServlet", serviceClass = StoreTimeBucketFacade.class)
public class StoreTimeBucketFacadeImpl implements StoreTimeBucketFacade.Iface {

    /**
     * 60天的毫秒数
     */
    private static final long MILLIS_OF_60_DAY = 5184000000l;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;


    @Autowired
    private MenuFacadeUtil menuFacadeUtil;

    @Autowired
    private SnsPublish snsPublish;

    @Autowired
    private StoreTimeBucketValidator storeTimeBucketValidator;

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    private StoreTimeBucketDTO createStoreTimeBucket(StoreTimeBucketParam param) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = new StoreTimeBucket();
        BeanUtil.copy(param, storeTimeBucket);
        storeTimeBucket = this.storeTimeBucketService.createStoreTimeBucket(storeTimeBucket);
        this.publish(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId(), storeTimeBucket.getTimeBucketId(), false);
        return this.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    private StoreTimeBucketDTO updateStoreTimeBucket(StoreTimeBucketParam param) throws T5weiException, TException {

        StoreTimeBucketSave storeTimeBucketSave = this.storeTimeBucketService.updateStoreTimeBucket(param);
        //如果营业时段的台位费有变动
        StoreTimeBucket storeTimeBucket = storeTimeBucketSave.getStoreTimeBucket();
        this.publish(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId(), storeTimeBucket.getTimeBucketId(), storeTimeBucketSave.isTableFeeUpdate());
        return this.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    @Override
    public List<StoreTimeBucketDTO> batchCreateStoreTimeBucket(List<StoreTimeBucketParam> params) throws T5weiException, TException {
        List<StoreTimeBucket> storeTimeBuckets = storeTimeBucketService.batchCreateStoreTimeBucket(params);
        return BeanUtil.copyList(storeTimeBuckets, StoreTimeBucketDTO.class);
    }

    @Override
    public StoreTimeBucketDTO saveStoreTimeBucket(StoreTimeBucketParam param) throws T5weiException, TException {

        if (param.getTimeBucketId() > 0) {
            this.storeTimeBucketValidator.checkSaveStoreTimeBucketParamLength(param);
            return this.updateStoreTimeBucket(param);
        }

        this.storeTimeBucketValidator.checkSaveStoreTimeBucketEmpty(param);
        this.storeTimeBucketValidator.checkSaveStoreTimeBucketParamLength(param);
        return this.createStoreTimeBucket(param);
    }

    @Override
    public void deleteStoreTimeBucket(int merchantId, long storeId, long timeBucketId) throws T5weiException, TException {
    	this.storeTimeBucketService.deleteStoreTimeBucket(merchantId, storeId, timeBucketId);
        this.publish(merchantId, storeId, timeBucketId, false);
    }

    @Override
    public List<StoreTimeBucketDTO> getStoreTimeBuckets(int merchantId, long storeId) throws TException {
        List<StoreTimeBucket> list = this.storeTimeBucketService.getStoreTimeBucketListForStore(merchantId, storeId);
        return BeanUtil.copyList(list, StoreTimeBucketDTO.class);
    }

    @Override
    public StoreTimeBucketDTO getStoreTimeBucket(int merchantId, long storeId, long timeBucketId) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId);
        return this.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    @Override
    public List<StoreTimeBucketDTO> getStoreItemBucketsInIds(int merchantId, List<StoreTimeBucketIdsParam> storeTimeBucketIdsParams) throws TException {
        List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService.getStoreItemBucketsInIds(merchantId, storeTimeBucketIdsParams);
        return BeanUtil.copyList(storeTimeBuckets, StoreTimeBucketDTO.class);
    }

    @Override
    public Map<Long, StoreTimeBucketDTO> getStoreItemBucketMapInIds(int merchantId, List<StoreTimeBucketIdsParam> storeTimeBucketIdsParams) throws TException {
        List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService.getStoreItemBucketsInIds(merchantId, storeTimeBucketIdsParams);
        Map<Long, StoreTimeBucketDTO> map = Maps.newHashMap();
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            StoreTimeBucketDTO storeTimeBucketDTO = this.buildStoreTimeBucketDTO(storeTimeBucket);
            map.put(storeTimeBucketDTO.getTimeBucketId(), storeTimeBucketDTO);
        }
        return map;
    }

    @Override
    public List<StoresTimeBucketsDTO> getStoresTimeBucketsInStoreIds(int merchantId, List<Long> storeIds) throws TException {
        Set<Long> storeIdSet = Sets.newHashSet();
        storeIdSet.addAll(storeIds);
        List<StoresTimeBucketsDTO> storesTimeBucketsDTOs = Lists.newArrayList();
        for (Long storeId : storeIdSet) {
            StoresTimeBucketsDTO storesTimeBucketsDTO = new StoresTimeBucketsDTO();
            storesTimeBucketsDTOs.add(storesTimeBucketsDTO);
            storesTimeBucketsDTO.setStoreId(storeId);
            List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService.getStoreTimeBucketListForStore(merchantId, storeId);
            storesTimeBucketsDTO.setStoreTimeBucketDTOs(this.menuFacadeUtil.buildStoreTimeBucketDTOs(storeTimeBuckets));
        }
        return storesTimeBucketsDTOs;
    }

    @Override
    public Map<Long, StoreTimeBucketDTO> getStoreTimeBucketMapInStoreIds(int merchantId, List<Long> storeIds) throws TException {
        List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService.getStoreTimeBucketListForStores(merchantId, storeIds);
        Map<Long, StoreTimeBucketDTO> map = Maps.newHashMap();
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            StoreTimeBucketDTO storeTimeBucketDTO = this.buildStoreTimeBucketDTO(storeTimeBucket);
            map.put(storeTimeBucketDTO.getTimeBucketId(), storeTimeBucketDTO);
        }
        return map;
    }

    @Override
    public StoreTimeBucketDTO changeStoreTimeBucketDeliverySupported(int merchantId, long storeId, long timeBucketId, boolean deliverySupported) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.changeStoreTimeBucketDeliverySupported(merchantId, storeId, timeBucketId, deliverySupported);
        return this.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    @Override
    public List<StoreTimeBucketDTO> getStoreTimeBucketsInStoreForTime(int merchantId, long storeId, long time) throws TException {
        long _time = DateUtil.getBeginTime(time, null);
        List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService
                .getStoreTimeBucketsInStoreForTime(merchantId, storeId, _time);
        List<StoreTimeBucketDTO> storeTimeBucketDTOs = Lists.newArrayList();
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            StoreTimeBucketDTO storeTimeBucketDTO = this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
            storeTimeBucketDTOs.add(storeTimeBucketDTO);
        }
        return storeTimeBucketDTOs;
    }

    @Override
    public StoreTimeBucketDTO saveStoreTimeBucketDelivery(StoreTimeBucketDeliveryParam param) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.saveStoreTimeBucketDelivery(param);
        return this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    private StoreTimeBucketDTO buildStoreTimeBucketDTO(StoreTimeBucket storeTimeBucket) {
        return this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    @Override
    public StoreTimeBucketDTO2 getStoreTimeBucketByName(int merchantId, long storeId, String name) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucketByName
                (merchantId, storeId, name);
        return this.menuFacadeUtil.buildStoreTimeBucketDTO2(storeTimeBucket);
    }

    /**
     * 查询一定时间范围内有效的营业时段
     *
     * @param param StoreTimeBucketsByTimeRangeParam
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public List<StoreTimeBucketDTO> getStoreTimeBucketsByTimeRange(StoreTimeBucketsByTimeRangeParam param) throws T5weiException, TException {

        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long startTime = param.getStartTime();
        long endTime = param.getEndTime();
        //参数校验
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] can not <=0");
        }

        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] can not <=0");
        }

        if (startTime <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "startTime[" + startTime + "] can not <=0");
        }

        if (endTime <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "endTime[" + endTime + "] can not <=0");
        }

        if (endTime < startTime) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "startTime[" + startTime + "] can not < endTime[" + endTime + "]");
        }
        //60天的毫秒数
        if ((endTime - startTime) > MILLIS_OF_60_DAY) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "param startTime[" + startTime + "] - endTime[" + endTime + "] > 60 days");
        }

        Collection<StoreTimeBucket> storeTimeBuckets = storeTimeBucketService.getStoreTimeBucketsByTimeRange(merchantId, storeId, startTime, endTime);

        List<StoreTimeBucketDTO> dtos = new ArrayList<>();
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            StoreTimeBucketDTO storeTimeBucketDTO = this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
            dtos.add(storeTimeBucketDTO);
        }

        return dtos;
    }

    /**
     * 获取指定日期中,每天有效的营业时段
     *
     * @param storeTimeBucketOfDaysParam
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public Map<Long, List<StoreTimeBucketDTO>> getStoreTimeBucketOfDays(StoreTimeBucketOfDaysParam storeTimeBucketOfDaysParam) throws T5weiException, TException {

        int merchantId = storeTimeBucketOfDaysParam.getMerchantId();
        long storeId = storeTimeBucketOfDaysParam.getStoreId();
        List<Long> days = storeTimeBucketOfDaysParam.getDays();

        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] can not <=0");
        }

        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] can not <=0");
        }

        if (days == null || days.size() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "days can not be <=0 and can not be null");
        }

        Map<Long, List<StoreTimeBucketDTO>> timeBucketOfDayMapDTO = new HashMap<>();
        for (Long day : days) {
            //获取一天的有效营业时段
            List<StoreTimeBucket> timeBucketOfDay = storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, day);

            List<StoreTimeBucketDTO> timeBucketDTOs = Lists.newArrayList();
            for (StoreTimeBucket storeTimeBucket : timeBucketOfDay) {
                StoreTimeBucketDTO storeTimeBucketDTO = this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
                timeBucketDTOs.add(storeTimeBucketDTO);
            }
            timeBucketOfDayMapDTO.put(day, timeBucketDTOs);
        }

        return timeBucketOfDayMapDTO;
    }

    @Override
    public List<StoreTimeBucketDTO> getStoreAllTimeBucket(int merchantId, List<Long> storeIds) throws T5weiException, TException {

        List<StoreTimeBucketDTO> storeTimeBucketDTOs = Lists.newArrayList();
        for (Long storeId : storeIds) {
            List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService.getStoreAllTimeBucket(merchantId, storeId, true, true);
            for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
                StoreTimeBucketDTO storeTimeBucketDTO = new StoreTimeBucketDTO();
                BeanUtil.copy(storeTimeBucket, storeTimeBucketDTO);
                storeTimeBucketDTOs.add(storeTimeBucketDTO);
            }
        }
        return storeTimeBucketDTOs;
    }

    public void publish(int merchantId, long storeId, long timeBucketId, boolean tableFeeUpdate) {
        //SNS发布事件
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("merchantId", merchantId);
        dataMap.put("storeId", storeId);
        dataMap.put("timeBucketId", timeBucketId);
        dataMap.put("updateTime", System.currentTimeMillis());
        if (tableFeeUpdate) {
            dataMap.put("tableFeeUpdate", 1);
        }
        String storeTimeBucketTopicArn = SysConfig.getStoreTimeBucketTopicArn();
        snsPublish.publish(dataMap, EventType.TIME_BUCKET, storeTimeBucketTopicArn);
    }

    @Override
    public void restoreStoreTimeBucket(int merchantId, long storeId, long timeBucketId) throws T5weiException, TException {
        //参数校验
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] can not <=0");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] can not <=0");
        }
        if (timeBucketId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "timeBucketId[" + timeBucketId + "] can not <=0");
        }
        this.storeTimeBucketService.restoreStoreTimeBucket(merchantId, storeId, timeBucketId);
    }

    @Override
    public Map<String, List<StoreTimeBucketDTO>> getStoreAllTimeBuckets(int merchantId, long storeId) throws T5weiException, TException {
        Map<String, List<StoreTimeBucketDTO>> resultMap = new HashMap<String, List<StoreTimeBucketDTO>>();
        List<StoreTimeBucketDTO> normalTimeBuckets = new ArrayList<StoreTimeBucketDTO>();
        List<StoreTimeBucketDTO> noTimeBuckets = new ArrayList<StoreTimeBucketDTO>();
        List<StoreTimeBucket> storeAllTimeBuckets = this.storeTimeBucketService.getStoreAllTimeBucket(merchantId, storeId, false, false);
        if(storeAllTimeBuckets != null && !storeAllTimeBuckets.isEmpty()){
            for (StoreTimeBucket storeTimeBucket : storeAllTimeBuckets) {
                StoreTimeBucketDTO storeTimeBucketDTO = new StoreTimeBucketDTO();
                BeanUtil.copy(storeTimeBucket, storeTimeBucketDTO);
                if (storeTimeBucket.isDeleted()) {//删除状态
                    noTimeBuckets.add(storeTimeBucketDTO);
                } else {
                    normalTimeBuckets.add(storeTimeBucketDTO);
                }
            }
        }
        resultMap.put("normalTimeBuckets", normalTimeBuckets);
        resultMap.put("noTimeBuckets", noTimeBuckets);
        return resultMap;
    }

    @Override
    public StoreTimeBucketDTO getDeletedTimeBucketByName(int merchantId, long storeId, String name) throws T5weiException, TException {
        //参数校验
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] can not <=0");
        }

        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] can not <=0");
        }

        if (name == null || "".equals(name)) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "timeBucketName[" + name + "] can not <=0");
        }

        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getDeletedTimeBucketByName(merchantId, storeId, name);

        if (storeTimeBucket != null) {
            return BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class);
        }
        return new StoreTimeBucketDTO();
    }

    @Override
    public StoreTimeBucketDTO getStoreTimeBucket4Waimai(int merchantId, long storeId, long time) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucket4Waimai(merchantId, storeId, time);
        return this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    @Override
    public Map<Long, List<StoreTimeBucketDTO>> getTimeBuckets4StoreIdsByTime(int merchantId, List<Long> storeIds, long beginTime,
                                                                             long endTime) throws TException {
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] can not <=0");
        }

        if (storeIds == null || storeIds.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + JsonUtil.build(storeIds) + "] is null");
        }

        if (endTime <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "endTime can not be <=0");
        }

        if (endTime < beginTime) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "beginTime can not be more than endTime");
        }
        //60天的毫秒数
        if ((endTime - beginTime) > MILLIS_OF_60_DAY) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "param startTime[" + beginTime + "] - endTime[" + endTime + "] > 60 days");
        }
        DateTime dt = new DateTime(endTime);
        long dayEndTime =  dt.plusDays(1).getMillis() -1 ;
        Map<Long, List<StoreTimeBucketDTO>> result = new HashMap<Long, List<StoreTimeBucketDTO>>();
        for (Long storeId : storeIds) {
            List<StoreTimeBucket> storeAllTimeBucket = storeTimeBucketService.getStoreAllTimeBucket(merchantId, storeId, true, true);
            List<StoreTimeBucketDTO> storeTimeBucketDTOList = Lists.newArrayList();
            for (StoreTimeBucket storeTimeBucket : storeAllTimeBucket) {
                if (storeTimeBucket.getCreateTime() < dayEndTime) { //营业时段的创建时间小于结束时间
                    if (storeTimeBucket.isDeleted() && storeTimeBucket.getUpdateTime() < beginTime) { //营业时段停用且时间小于开始时间
                        continue;
                    }
                    storeTimeBucketDTOList.add(this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket));
                }
            }
            result.put(storeId, storeTimeBucketDTOList);
        }
        return result;
	}

}
