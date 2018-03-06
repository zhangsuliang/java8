package com.huofu.module.i5wei.menumv.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.batchproc.dao.StoreBatchProcDAO;
import com.huofu.module.i5wei.batchproc.entity.StoreBatchProc;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.*;
import com.huofu.module.i5wei.menu.service.StoreTVMenuService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import halo.query.Query;
import huofucore.facade.i5wei.batchproc.StoreBatchProcTypeEnum;
import huofucore.facade.i5wei.mealport.StoreMealPortCallTypeEnum;
import huofucore.facade.i5wei.menu.StoreTvMenuParam;
import huofucore.facade.i5wei.menumv.CopyMenuParam;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofuhelper.module.base.TransactionService;
import huofuhelper.util.MapObject;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 提供产品,营业时段,收费项目,价格,周期,菜单排版,常用备注,出餐口,收银台 Created by akwei on 3/9/16.
 */
@SuppressWarnings("unchecked")
@Service
public class MenuMvService {

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreChargeSubitemDAO storeChargeSubitemDAO;

    @Autowired
    private StoreChargeItemPriceDAO storeChargeItemPriceDAO;

    @Autowired
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Autowired
    private StoreProductDAO storeProductDAO;

    @Autowired
    private StoreMenuDisplayCatDAO storeMenuDisplayCatDAO;

    @Autowired
    private StoreMenuDisplayDAO storeMenuDisplayDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private StoreTvMenuDAO storeTvMenuDAO;

    @Autowired
    private Query query;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StoreTVMenuService storeTVMenuService;

    @Autowired
    private StoreBatchProcDAO storeBatchProcDAO;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreMealPortSendDAO storeMealPortSendDAO;

    private static Log logger = LogFactory.getLog(MenuMvService.class);

    public Map<String, Object> getData4MoveToStore(int merchantId, long srcStoreId) throws TException {
        logger.info("menumv begin load data .......................");

        String whereSQL = "where merchant_id=? and store_id=?";
        Object[] args = new Object[]{merchantId, srcStoreId};

        String deletedSQL = " and deleted = 0 ";

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreTimeBucket> storeTimeBuckets = this.query.list(StoreTimeBucket.class, whereSQL + deletedSQL, args);// add by lizhijun

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreMealPort> storeMealPorts = this.query.list(StoreMealPort.class, whereSQL + deletedSQL, args);// add by lizhijun

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreMealPortSend> storeMealPortSends = this.query.list(StoreMealPortSend.class, whereSQL + " and send_port_type = 0 ", args);//只复制传菜口

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreChargeItemWeek> storeChargeItemWeeks = this.query.list(StoreChargeItemWeek.class, whereSQL + deletedSQL, args);// add by lizhijun
        // add by lizhijun start
        for (Iterator<StoreChargeItemWeek> iterator = storeChargeItemWeeks.iterator(); iterator.hasNext(); ) {
            StoreChargeItemWeek storeChargeItemWeek = iterator.next();
            if (!this.exitTimeBucket(storeTimeBuckets, storeChargeItemWeek.getTimeBucketId())) {
                iterator.remove();
            }
        }
        // add by lizhijun end

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreChargeItem> storeChargeItems = this.query.list(StoreChargeItem.class, whereSQL, args);
        // add by lizhijun start
        for (Iterator<StoreChargeItem> iterator = storeChargeItems.iterator(); iterator.hasNext(); ) {// 判断周期设置中是否存在相应的收费项
            StoreChargeItem storeChargeItem = iterator.next();
            boolean isExit = true;
            if (!exitStoreChargeItemInStoreChargeItemWeek(storeChargeItemWeeks, storeChargeItem.getChargeItemId())) {
                isExit = false;
                iterator.remove();
            }

//			if(isExit && exitStoreMealPort(storeMealPorts, storeChargeItem.getPortId())){
//				isExit = true;
//				iterator.remove();
//			}
        }
        // add by lizhijun end

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreChargeItemPrice> storeChargeItemPrices = this.query.list(StoreChargeItemPrice.class, whereSQL, args);
        // add by lizhijun start
        for (Iterator<StoreChargeItemPrice> iterator = storeChargeItemPrices.iterator(); iterator.hasNext(); ) {
            StoreChargeItemPrice storeChargeItemPrice = iterator.next();
            if (!this.exitStoreChargeItem(storeChargeItems, storeChargeItemPrice.getChargeItemId())) {// 过滤掉收费项定价中不存在的收费项
                iterator.remove();
            }
        }
        // add by lizhijun end

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreProduct> storeProducts = this.query.list(StoreProduct.class, whereSQL, args);

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreChargeSubitem> storeChargeSubitems = this.query.list(StoreChargeSubitem.class, whereSQL, args);
        // add by lizhijun start
        for (Iterator<StoreChargeSubitem> iterator = storeChargeSubitems.iterator(); iterator.hasNext(); ) {// 过滤收费子项目中删除的产品
            StoreChargeSubitem storeChargeSubitem = iterator.next();
//			boolean isExit = true;
//			if (!this.exitStoreProduct(storeProducts, storeChargeSubitem.getProductId())) {
//				isExit = false;
//				iterator.remove();
//			}

            if (!this.exitStoreChargeItem(storeChargeItems, storeChargeSubitem.getChargeItemId())) {// 过滤收费子项目中停用的收费项
//				isExit = false;
                iterator.remove();
            }
        }
        // add by lizhijun end

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreMenuDisplay> storeMenuDisplays = this.query.list(StoreMenuDisplay.class, whereSQL, args);
        // add by lizhijun start
        for (Iterator<StoreMenuDisplay> iterator = storeMenuDisplays.iterator(); iterator.hasNext(); ) {
            StoreMenuDisplay storeMenuDisplay = iterator.next();
            boolean isExit = true;
            if (!exitStoreChargeItem(storeChargeItems, storeMenuDisplay.getChargeItemId())) {
                isExit = false;
                iterator.remove();
            }

            if (isExit && !exitTimeBucket(storeTimeBuckets, storeMenuDisplay.getTimeBucketId())) {
                isExit = false;
                iterator.remove();
            }
        }
        // add by lizhijun end

        BaseStoreDbRouter.addInfo(merchantId, srcStoreId);
        List<StoreMenuDisplayCat> storeMenuDisplayCats = this.query.list(StoreMenuDisplayCat.class, whereSQL, args);
        // add by lizhijun start
        for (Iterator<StoreMenuDisplayCat> iterator = storeMenuDisplayCats.iterator(); iterator.hasNext(); ) {// 过滤餐单类别中营业时段停用的情况
            StoreMenuDisplayCat storeMenuDisplayCat = iterator.next();
            if (!exitTimeBucket(storeTimeBuckets, storeMenuDisplayCat.getTimeBucketId())) {
                iterator.remove();
            }
        }
        // add by lizhijun end


        Map<String, Object> map = Maps.newHashMap();
        map.put("storeProducts", storeProducts);
        map.put("storeTimeBuckets", storeTimeBuckets);
        map.put("storeChargeItems", storeChargeItems);
        map.put("storeChargeItemPrices", storeChargeItemPrices);
        map.put("storeChargeItemWeeks", storeChargeItemWeeks);
        map.put("storeChargeSubitems", storeChargeSubitems);
        map.put("storeMenuDisplays", storeMenuDisplays);
        map.put("storeMenuDisplayCats", storeMenuDisplayCats);
        map.put("storeMealPorts", storeMealPorts);
        map.put("storeMealPortSends", storeMealPortSends);
        logger.info("menumv end load data .......................");
        return map;

    }

    @Transactional(rollbackFor = Exception.class, isolation = READ_COMMITTED)
    public void moveToStore(int merchantId, long targetStoreId, Map<String, Object> map, StoreBatchProc storeBatchProc) {
        logger.info("menumv begin move data .......................targetStoreId[" + targetStoreId + "]");
        Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(merchantId, targetStoreId, false);
        long begin = System.currentTimeMillis();

        List<StoreTimeBucket> storeTimeBuckets = (List<StoreTimeBucket>) map.get("storeTimeBuckets");
        List<StoreProduct> storeProducts = (List<StoreProduct>) map.get("storeProducts");
        List<StoreChargeItem> storeChargeItems = (List<StoreChargeItem>) map.get("storeChargeItems");
        List<StoreChargeItemPrice> storeChargeItemPrices = (List<StoreChargeItemPrice>) map.get("storeChargeItemPrices");
        List<StoreChargeItemWeek> storeChargeItemWeeks = (List<StoreChargeItemWeek>) map.get("storeChargeItemWeeks");
        List<StoreChargeSubitem> storeChargeSubitems = (List<StoreChargeSubitem>) map.get("storeChargeSubitems");
        List<StoreMenuDisplay> storeMenuDisplays = (List<StoreMenuDisplay>) map.get("storeMenuDisplays");
        List<StoreMenuDisplayCat> storeMenuDisplayCats = (List<StoreMenuDisplayCat>) map.get("storeMenuDisplayCats");
        List<StoreMealPort> storeMealPorts = (List<StoreMealPort>) map.get("storeMealPorts");
        List<StoreMealPortSend> storeMealPortSends = (List<StoreMealPortSend>) map.get("storeMealPortSends");

        List<Long> priceIds = this.storeChargeItemPriceDAO.createIds(storeChargeItemPrices.size());
        List<Long> weekIds = this.storeChargeItemWeekDAO.createIds(storeChargeItemWeeks.size());
        List<Long> productIds = this.storeProductDAO.createIds(storeProducts.size());
        List<Long> chargeItemIds = this.storeChargeItemDAO.createIds(storeChargeItems.size());

        Map<Long, Long> timeBucketIdMap = Maps.newHashMap();
        Map<Long, Long> productIdMap = Maps.newHashMap();
        Map<Long, Long> itemIdMap = Maps.newHashMap();
        Map<Long, Long> displayCatIdMap = Maps.newHashMap();
        displayCatIdMap.put(0L, 0L);
        Map<Long, Long> portIdMap = Maps.newHashMap();
        portIdMap.put(0L, 0L);
        Map<Long, Long> portSendIdMap = Maps.newHashMap();
        portSendIdMap.put(0L, 0L);

        if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            //创建传菜口
            List<Long> oldIds = Lists.newArrayList();
            for (StoreMealPortSend obj : storeMealPortSends) {
                oldIds.add(obj.getSendPortId());
                obj.setStoreId(targetStoreId);
                obj.setSendPortName(obj.getSendPortName() + "_" + obj.getSendPortId());
                obj.setMasterSendPort(false);
                obj.setCallType(StoreMealPortCallTypeEnum.MANUAL.getValue());
                obj.setCallPeripheralId(0);
                obj.setPrinterPeripheralId(0);
                obj.setPrintDivItem(false);
            }
            if (!storeMealPortSends.isEmpty()) {
                this.storeMealPortSendDAO.batchCreate(storeMealPortSends);
                int i = 0;
                for (StoreMealPortSend obj : storeMealPortSends) {
                    portSendIdMap.put(oldIds.get(i), obj.getSendPortId());
                    i++;
                }
            }
        }

        {
            // 创建出餐口
            List<Long> oldIds = Lists.newArrayList();
            for (StoreMealPort obj : storeMealPorts) {
                oldIds.add(obj.getPortId());
                obj.setStoreId(targetStoreId);
                obj.setCallType(StoreMealPortCallTypeEnum.MANUAL.getValue());
                obj.setCallPeripheralId(0);
                obj.setPrinterPeripheralId(0);
                if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
                    obj.setSendPortId(portSendIdMap.get(obj.getSendPortId()) != null ? portSendIdMap.get(obj.getSendPortId()) : 0);
                } else {
                    obj.setSendPortId(0L);
                }
            }
            this.storeMealPortDAO.batchCreate(storeMealPorts);
            int i = 0;
            for (StoreMealPort obj : storeMealPorts) {
                portIdMap.put(oldIds.get(i), obj.getPortId());
                i++;
            }
        }

        {
            // 创建营业时段
            List<Long> oldIds = Lists.newArrayList();
            for (StoreTimeBucket obj : storeTimeBuckets) {
                obj.setStoreId(targetStoreId);
                oldIds.add(obj.getTimeBucketId());
            }
            this.storeTimeBucketDAO.batchCreate(storeTimeBuckets);

            int i = 0;
            for (StoreTimeBucket obj : storeTimeBuckets) {
                timeBucketIdMap.put(oldIds.get(i), obj.getTimeBucketId());
                i++;
            }
        }

        {
            // 创建产品
            List<Long> oldIds = Lists.newArrayList();
            for (StoreProduct obj : storeProducts) {
                obj.setStoreId(targetStoreId);
                obj.setPortId(portIdMap.get(obj.getPortId()));
                oldIds.add(obj.getProductId());
            }
            this.storeProductDAO.batchCreate(storeProducts, productIds);

            int i = 0;
            for (StoreProduct obj : storeProducts) {
                productIdMap.put(oldIds.get(i), obj.getProductId());
                i++;
            }
        }

        {
            // 创建收费项目
            List<Long> oldIds = Lists.newArrayList();
            for (StoreChargeItem obj : storeChargeItems) {
                obj.setStoreId(targetStoreId);
                obj.setPortId(portIdMap.get(obj.getPortId()));
                oldIds.add(obj.getChargeItemId());
            }
            this.storeChargeItemDAO.batchCreate(storeChargeItems, chargeItemIds);

            int i = 0;
            for (StoreChargeItem obj : storeChargeItems) {
                itemIdMap.put(oldIds.get(i), obj.getChargeItemId());
                i++;
            }
        }

        {
            // 创建子项目
            for (StoreChargeSubitem obj : storeChargeSubitems) {
                obj.setTid(0);
                obj.setStoreId(targetStoreId);
                obj.setChargeItemId(itemIdMap.get(obj.getChargeItemId()));
                obj.setProductId(productIdMap.get(obj.getProductId()));
            }
            this.storeChargeSubitemDAO.batchCreate(storeChargeSubitems);
        }

        {
            // 创建价格
            for (StoreChargeItemPrice obj : storeChargeItemPrices) {
                obj.setStoreId(targetStoreId);
                obj.setChargeItemId(itemIdMap.get(obj.getChargeItemId()));
            }
            this.storeChargeItemPriceDAO.batchCreate(storeChargeItemPrices, priceIds);
        }

        {
            // 创建周期
            for (StoreChargeItemWeek obj : storeChargeItemWeeks) {
                obj.setStoreId(targetStoreId);
                obj.setChargeItemId(itemIdMap.get(obj.getChargeItemId()));
                obj.setTimeBucketId(timeBucketIdMap.get(obj.getTimeBucketId()));
            }
            this.storeChargeItemWeekDAO.batchCreate(storeChargeItemWeeks, weekIds);
        }

        {
            // 创建菜单排版分类
            List<Long> oldIds = Lists.newArrayList();
            for (StoreMenuDisplayCat obj : storeMenuDisplayCats) {
                oldIds.add(obj.getDisplayCatId());
                obj.setStoreId(targetStoreId);
                obj.setTimeBucketId(timeBucketIdMap.get(obj.getTimeBucketId()));
            }
            this.storeMenuDisplayCatDAO.batchCreate(storeMenuDisplayCats);

            int i = 0;
            for (StoreMenuDisplayCat obj : storeMenuDisplayCats) {
                displayCatIdMap.put(oldIds.get(i), obj.getDisplayCatId());
                i++;
            }
        }

        {
            // 创建菜单排版
            for (StoreMenuDisplay obj : storeMenuDisplays) {
                obj.setTid(0);
                obj.setStoreId(targetStoreId);
                obj.setChargeItemId(itemIdMap.get(obj.getChargeItemId()));
                obj.setTimeBucketId(timeBucketIdMap.get(obj.getTimeBucketId()));
                obj.setDisplayCatId(displayCatIdMap.get(obj.getDisplayCatId()));
            }
            this.storeMenuDisplayDAO.batchCreate(storeMenuDisplays);
        }
        storeBatchProc.makeFinish();
        long end = System.currentTimeMillis();
        logger.info("menumv end move data[" + (end - begin) + "] .......................");
    }

    public void copyTvMenu2Store(int merchantId, long storeId, long targetStoreId) throws TException {
        List<StoreTvMenu> list = this.storeTvMenuDAO.getList4Save(merchantId, storeId, 0, 0, 100);

        List<StoreTimeBucket> buckets = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, false, false);
        Map<Long, StoreTimeBucket> bucketMap = Maps.newHashMap();
        for (StoreTimeBucket bucket : buckets) {
            bucketMap.put(bucket.getTimeBucketId(), bucket);
        }
        List<StoreChargeItem> items = this.storeChargeItemDAO.getListByStoreId(merchantId, storeId, false, false);
        Map<Long, StoreChargeItem> itemMap = Maps.newHashMap();
        for (StoreChargeItem item : items) {
            itemMap.put(item.getChargeItemId(), item);
        }

        List<StoreTimeBucket> buckets2 = this.storeTimeBucketDAO.getListForStore(merchantId, targetStoreId, false,
                false);
        Map<String, StoreTimeBucket> bucketName2Map = Maps.newHashMap();
        for (StoreTimeBucket bucket2 : buckets2) {
            bucketName2Map.put(bucket2.getName(), bucket2);
        }
        List<StoreChargeItem> items2 = this.storeChargeItemDAO.getListByStoreId(merchantId, targetStoreId, false,
                false);
        Map<String, StoreChargeItem> itemName2Map = Maps.newHashMap();
        for (StoreChargeItem item : items2) {
            itemName2Map.put(item.getName(), item);
        }

        List<StoreTvMenuParam> params = new ArrayList<>();
        for (StoreTvMenu storeTvMenu : list) {
            StoreTvMenuParam param = new StoreTvMenuParam();
            BeanUtil.copy(storeTvMenu, param);
            param.setStoreId(targetStoreId);
            if (param.getTimeBucketId() > 0) {
                StoreTimeBucket bucket = bucketMap.get(param.getTimeBucketId());
                StoreTimeBucket bucket2 = bucketName2Map.get(bucket.getName());
                param.setTimeBucketId(bucket2.getTimeBucketId());
            }
            List<Map<String, Object>> jsonList = JsonUtil.parse(param.getContent(), List.class);
            Iterator<Map<String, Object>> iterator = jsonList.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> map = iterator.next();
                MapObject mo = new MapObject(map);
                long chargeItemId = mo.getLong("chargeitem_id", 0);
                if (chargeItemId > 0) {
                    StoreChargeItem item = itemMap.get(chargeItemId);
                    StoreChargeItem item2 = itemName2Map.get(item.getName());
                    if (item2 == null) {
                        iterator.remove();
                        continue;
                    }
                    map.put("chargeitem_id", item2.getChargeItemId());
                }
            }
            String content = JsonUtil.build(jsonList);
            param.setContent(content);
            params.add(param);
        }
        try {
            this.transactionService.doTransaction(() -> {
                for (StoreTvMenuParam param : params) {
                    storeTVMenuService.save(param);
                }
                return null;
            });
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    private boolean exitTimeBucket(List<StoreTimeBucket> storeTimeBuckets, long storeTimeBucketId) {
        if (storeTimeBuckets != null) {
            for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
                if (storeTimeBucket.getTimeBucketId() == storeTimeBucketId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean exitStoreChargeItem(List<StoreChargeItem> storeChargeItems, long chargeItemId) {
        if (storeChargeItems != null) {
            for (StoreChargeItem storeChargeItem : storeChargeItems) {
                if (storeChargeItem.getChargeItemId() == chargeItemId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean exitStoreProduct(List<StoreProduct> storeProducts, long productId) {
        if (storeProducts != null) {
            for (StoreProduct storeProduct : storeProducts) {
                if (storeProduct.getProductId() == productId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean exitStoreMealPort(List<StoreMealPort> storeMealPorts, long portId) {
        if (storeMealPorts != null) {
            for (StoreMealPort storeMealPort : storeMealPorts) {
                if (storeMealPort.getPortId() == portId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean exitStoreChargeItemInStoreChargeItemWeek(List<StoreChargeItemWeek> storeChargeItemWeeks, long chargeItemId) {
        if (storeChargeItemWeeks != null) {
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
                if (storeChargeItemWeek.getChargeItemId() == chargeItemId) {
                    return true;
                }
            }
        }
        return false;
    }

    public StoreBatchProc createCopyStoreBatchProc(CopyMenuParam param) {
        StoreBatchProc obj = new StoreBatchProc();
        obj.setMerchantId(param.getMerchantId());
        obj.setStoreId(param.getTargetStoreId());
        obj.setType(StoreBatchProcTypeEnum.COPY_MENU.getValue());
        obj.setCreateTime(System.currentTimeMillis());
        Map<String, Object> dataMap = Maps.newHashMap();
        dataMap.put(StoreBatchProc.FIELD_SRC_STORE_ID, param.getSrcStoreId());
        dataMap.put(StoreBatchProc.FIELD_TARGET_STORE_ID, param.getTargetStoreId());
        obj.setData(JsonUtil.build(dataMap));
        obj.create();
        return obj;
    }

    public void copyMenu2StoreFail(StoreBatchProc storeBatchProc) {
        storeBatchProc.makeFail();
    }
}
