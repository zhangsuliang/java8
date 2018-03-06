package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreTvMenu;

public class StoreTimeTvMenu {

    private StoreTvMenu storeTvMenu;

    private boolean recommendable;

    public StoreTvMenu getStoreTvMenu() {
        return storeTvMenu;
    }

    public void setStoreTvMenu(StoreTvMenu storeTvMenu) {
        this.storeTvMenu = storeTvMenu;
    }

    public boolean isRecommendable() {
        return recommendable;
    }

    public void setRecommendable(boolean recommendable) {
        this.recommendable = recommendable;
    }
}
