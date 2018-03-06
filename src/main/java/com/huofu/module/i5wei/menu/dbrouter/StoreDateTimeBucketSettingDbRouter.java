package com.huofu.module.i5wei.menu.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 2/16/15.
 */
public class StoreDateTimeBucketSettingDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName =
            "tb_store_date_time_bucket_setting";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
