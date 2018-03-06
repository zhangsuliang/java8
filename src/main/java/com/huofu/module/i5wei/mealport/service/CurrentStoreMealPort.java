package com.huofu.module.i5wei.mealport.service;

import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealTask;

public class CurrentStoreMealPort {

    private StoreMealPort storeMealPort;

    private StoreMealTask storeMealTask;

    public StoreMealPort getStoreMealPort() {
        return storeMealPort;
    }

    public void setStoreMealPort(StoreMealPort storeMealPort) {
        this.storeMealPort = storeMealPort;
    }

    public StoreMealTask getStoreMealTask() {
        return storeMealTask;
    }

    public void setStoreMealTask(StoreMealTask storeMealTask) {
        this.storeMealTask = storeMealTask;
    }
}
