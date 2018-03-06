package com.huofu.module.i5wei.menu.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.TableBean;
import halo.query.dal.ParsedInfo;

import java.util.Map;

/**
 * Created by akwei on 2/16/15.
 */
public class StoreChargeItemDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName = "tb_store_charge_item";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
