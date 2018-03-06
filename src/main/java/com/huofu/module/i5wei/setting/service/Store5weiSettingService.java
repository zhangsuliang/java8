package com.huofu.module.i5wei.setting.service;

import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.service.StoreMealPortSendService;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.setting.dao.Store5weiSettingDAO;
import com.huofu.module.i5wei.setting.dao.StoreTableSettingDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.ProductDivRuleEnum;
import huofucore.facade.i5wei.store5weisetting.*;
import huofuhelper.util.bean.BeanUtil;
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

/**
 * Created by akwei on 5/29/15.
 */
@Service
public class Store5weiSettingService {

    private final static Log log = LogFactory.getLog(Store5weiSettingService.class);

    @Autowired
    private Store5weiSettingDAO store5weiSettingDAO;

    @Autowired
    private StoreTableSettingDAO storeTableSettingDAO;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StoreMealPortSendService storeMealPortSendService;

    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;

    @Autowired
    private StoreMealPortService storeMealPortService;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    public Store5weiSetting getStore5weiSettingByStoreId(int merchantId, long storeId) {
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, true);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        return store5weiSetting;
    }

    public Store5weiSetting getStore5weiSettingByStoreId(int merchantId, long storeId, boolean enableSlave) {
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, enableSlave);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        return store5weiSetting;
    }

    /**
     * Created by akwei on 5/29/15.
     * 保存店铺配置
     *
     * @param param
     * @return Store5weiSetting
     */
    public Store5weiSettingSaveResult saveStore5weiSetting(Store5weiSettingParam param) throws TException {
        try {
            return this._saveStore5weiSetting(param);
        } catch (DuplicateKeyException e) {
            return this._saveStore5weiSetting(param);
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    private Store5weiSettingSaveResult _saveStore5weiSetting(Store5weiSettingParam param) throws TException {
        //切换打印模式
        this.changeStorePrintMode(param);
        //定义方法返回对象
        Store5weiSettingSaveResult store5weiSettingSave = new Store5weiSettingSaveResult();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, false);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
            //是否手动切换打印模式
            store5weiSetting.setPrintModeDefined(this.enablePrintModeDefined(param, store5weiSetting));
            BeanUtil.copy(param, store5weiSetting, true);
            store5weiSetting.create();
        } else {
            store5weiSetting.snapshot();
            this.checkTableFeeEnable(param, store5weiSettingSave, store5weiSetting);
            //是否手动切换打印模式
            store5weiSetting.setPrintModeDefined(this.enablePrintModeDefined(param, store5weiSetting));
            BeanUtil.copy(param, store5weiSetting, true);
            store5weiSetting.update();
        }
        store5weiSettingSave.setStore5weiSetting(store5weiSetting);
        return store5weiSettingSave;
    }

    //切换打印模式需要初始化传菜间相关
    private void changeStorePrintMode(Store5weiSettingParam param) throws TException {
        int printMode = param.getPrintMode();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.getStore5weiSettingByStoreId(merchantId, storeId, false);
        if (!param.isSetPrintMode()) {
            return;
        }
        //切换高级打印模式，初始化主传菜口，打包台，和外卖台
        if (printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
                return;
            }
            //校验店铺待出餐列表是否为空
            if (!storeMealTakeupDAO.getStoreMealTakeupsByStoreId(merchantId, storeId, false).isEmpty()) {
                log.error("storeId[" + storeId + "]店铺待出餐列表未全部出餐，请全部出餐完毕再切换打印模式");
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_TAKE_UP_REMAIN_ERROR.getValue(), "店铺待出餐列表未全部出餐，请全部出餐完毕再切换打印模式");
            }
            //初始化店铺传菜间，打包台，外卖台
            storeMealPortSendService.initStoreMealPortSendDTO(merchantId, storeId);
            //普通模式切换成高级模式需要开启出餐台（加工档口）上的智能自动出餐;
            List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreMealPorts(merchantId, storeId);
            List<StoreMealPort> list = new ArrayList<>();
            for (StoreMealPort storeMealPort : storeMealPorts) {
                storeMealPort.setAutoShift(true);
                list.add(storeMealPort);
            }
            storeMealPortDAO.batchUpdateAutoShift(list);
        } else {
            if (printMode == StorePrintModeEnum.NORMAL_PRINT.getValue()) {
                if (store5weiSetting.getPrintMode() == StorePrintModeEnum.NORMAL_PRINT.getValue()) {
                    return;
                }
                //校验店铺是否全部划菜完毕
                if (!storeMealTakeupDAO.getRemainSendStoreMealTakeupsByStoreId(merchantId, storeId, false).isEmpty()) {
                    log.error("storeId[" + storeId + "]店铺待划菜列表未全部被划，请全部划菜完毕再切换打印模式");
                    throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_TAKE_UP_REMAIN_SEND_ERROR.getValue(), "店铺待划菜列表未全部被划，请全部划菜完毕再切换打印模式");
                }
            }
            //需要将收费项目上边的open_div_rule设置为true，div_rule设置为productDivRuleEnum.NOT
            //获取店铺所有收费项目
            List<StoreChargeItem> storeChargeItems = storeChargeItemDAO.getStoreChargeItems(merchantId, storeId);
            List<StoreChargeItem> list = new ArrayList<>();
            for (StoreChargeItem storeChargeItem : storeChargeItems) {
                storeChargeItem.setOpenDivRule(true);
                storeChargeItem.setDivRule(ProductDivRuleEnum.NOT.getValue());
                list.add(storeChargeItem);
            }
            storeChargeItemDAO.batchUpdateDivRuleAndOpenDivRule(list);
        }
    }

    /**
     * 判断台位费的开关是否改变了
     *
     * @param param
     * @param store5weiSettingSave
     * @param store5weiSetting
     */
    private void checkTableFeeEnable(Store5weiSettingParam param, Store5weiSettingSaveResult store5weiSettingSave, Store5weiSetting store5weiSetting) {
        if (param.isSetEnableTableFee()) {
            if (param.isEnableTableFee() != store5weiSetting.isEnableTableFee()) {
                store5weiSettingSave.setTableFeeUpdate(true);
            }
        }
    }

    /**
     * 判断打印模式是否手动切换过
     *
     * @param param
     * @param store5weiSetting
     */
    private boolean enablePrintModeDefined(Store5weiSettingParam param, Store5weiSetting store5weiSetting) {
        if (param.isSetPrintMode()) {
            if (param.getPrintMode() != store5weiSetting.getPrintMode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Created by chenkai on 12/9/15.
     * 保存流水号配置
     *
     * @param param
     * @return Store5weiSetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Store5weiSetting saveStoreSerialNumberStart(StoreSerialNumberSettingParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, false);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        int serialNumberStart = param.getSerialNumberStart();
        if (serialNumberStart <= 0) {
            serialNumberStart = 1;
        }
        boolean serialNumberEnable = param.isSerialNumberEnable();
        store5weiSetting.snapshot();
        store5weiSetting.setMerchantId(merchantId);
        store5weiSetting.setStoreId(storeId);
        store5weiSetting.setSerialNumberEnable(serialNumberEnable);
        store5weiSetting.setSerialNumberStart(serialNumberStart);
        store5weiSetting.replace();
        return store5weiSetting;
    }

    /**
     * Created by chenkai on 12/9/15.
     * 保存餐牌号配置
     *
     * @param param
     * @return Store5weiSetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Store5weiSetting saveStoreSiteNumber(StoreSiteNumberSettingParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, false);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        int siteNumberMax = param.getSiteNumberMax();
        if (siteNumberMax < 3 || siteNumberMax > 99999) {
            throw new T5weiException(T5weiErrorCodeType.STORE_SITE_NUMBER_MAX_SCOPE_INCORRECT.getValue(), "store site number max scope incorrect");
        }
        boolean siteNumberEnable = param.isSiteNumberEnable();
        store5weiSetting.snapshot();
        store5weiSetting.setMerchantId(merchantId);
        store5weiSetting.setStoreId(storeId);
        store5weiSetting.setSiteNumberMax(siteNumberMax);
        store5weiSetting.setSiteNumberEnable(siteNumberEnable);
        store5weiSetting.setSiteNumberSelf(param.isSiteNumberSelf());
        store5weiSetting.setSiteNumberTips(param.getSiteNumberTips());
        //打包自取时是否使用餐牌号
        store5weiSetting.setSiteNumberForTake(param.isSiteNumberForTake());
        store5weiSetting.replace();
        return store5weiSetting;
    }

    /**
     * Created by jiangjiajin on 01/06/16.
     * 保存打包费配置
     *
     * @param param
     * @return Store5weiSetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Store5weiSetting saveStorePackage(StorePackageSettingParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, false);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        store5weiSetting.snapshot();
        //保存打包费设置
        store5weiSetting.setPackageFeeEnable(param.isPackageFeeEnable());
        store5weiSetting.replace();
        return store5weiSetting;
    }

    /**
     * Created by jiangjiajin on 01/06/16.
     * 是否开启排队人数配置
     *
     * @param param
     * @return Store5weiSetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Store5weiSetting saveStoreQueueNumber(StoreQueueNumberSettingParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, false);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        store5weiSetting.snapshot();
        //是否开启排队人数
        store5weiSetting.setQueueNumberEnable(param.isQueueNumberEnable());
        store5weiSetting.replace();
        return store5weiSetting;
    }

    /**
     * Created by lixuwei on 2016-03-16.
     * 客单价的计算方法设置
     *
     * @param param
     * @return Store5weiSetting
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Store5weiSetting saveStoreCustomerAvgPaymentModelSetting(StoreCustomerAvgPaymentModelSettingParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        Store5weiSetting store5weiSetting = this.store5weiSettingDAO.getById(merchantId, storeId, false);
        if (store5weiSetting == null) {
            store5weiSetting = Store5weiSetting.createDefault(merchantId, storeId);
        }
        store5weiSetting.snapshot();
        //客单价的计算方法
        store5weiSetting.setCustomerAvgPaymentModel(param.getCustomerAvgPaymentModel());
        store5weiSetting.replace();
        return store5weiSetting;
    }

}
