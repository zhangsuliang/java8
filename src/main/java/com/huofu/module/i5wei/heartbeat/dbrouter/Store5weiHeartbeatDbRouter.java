package com.huofu.module.i5wei.heartbeat.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 4/4/15.
 */
public class Store5weiHeartbeatDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_5wei_heartbeat";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
