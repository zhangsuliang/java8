package com.huofu.module.i5wei.menu.facade;

import com.huofu.module.i5wei.menu.entity.StoreTvMenu;
import com.huofu.module.i5wei.menu.service.StoreTVMenuService;
import com.huofu.module.i5wei.menu.service.StoreTimeTvMenu;
import huofucore.facade.i5wei.menu.*;
import huofuhelper.util.PageResult;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 电视菜单服务
 * Created by akwei on 6/23/15.
 */
@ThriftServlet(name = "storeTvMenuFacadeServlet", serviceClass = StoreTvMenuFacade.class)
@Component
public class StoreTvMenuFacadeImpl implements StoreTvMenuFacade.Iface {

    @Autowired
    private StoreTVMenuService storeTVMenuService;

    @Autowired
    private MenuFacadeUtil menuFacadeUtil;

    @Override
    public StoreTvMenuDTO saveStoreTvMenu(StoreTvMenuParam param) throws TException {
        StoreTvMenu storeTvMenu;
        try {
            storeTvMenu = this.storeTVMenuService.save(param);
        } catch (DuplicateKeyException e) {
            storeTvMenu = this.storeTVMenuService.save(param);
        }
        return this.menuFacadeUtil.buildStoreTvMenuDTO(storeTvMenu);
    }

    @Override
    public void deleteStoreTvMenu(int merchantId, long storeId, long timeBucketId, long useDate) throws TException {
        this.storeTVMenuService.deleteStoreTvMenu(merchantId, storeId, timeBucketId, useDate);
    }

    @Override
    public StoreTvMenuDTO getStoreTvMenu(int merchantId, long storeId, long timeBucketId, long useDate) throws TException {
        StoreTvMenu storeTvMenu = this.storeTVMenuService.getStoreTvMenu(merchantId, storeId, timeBucketId, useDate);
        return this.menuFacadeUtil.buildStoreTvMenuDTO(storeTvMenu);
    }

    @Override
    public List<StoreTimeTvMenuDTO> getStoreTimeTvMenus(QueryStoreTvMenusParam param) throws TException {
        List<StoreTimeTvMenu> storeTimeTvMenus = this.storeTVMenuService.getStoreTimeTvMenus(param, true);
        return this.menuFacadeUtil.buildStoreTimeTvMenuDTOs(storeTimeTvMenus);
    }

    @Override
    public StoreTvMenuPageDTO getStoreTvMenus4Save(QueryStoreTvMenus4SaveParam param) throws TException {
        PageResult pageResult = this.storeTVMenuService.getStoreTvMenus4Save(param);
        List<StoreTvMenuDTO> storeTvMenuDTOs = this.menuFacadeUtil.buildStoreTvMenuDTOs(pageResult.getList());
        StoreTvMenuPageDTO storeTvMenuPageDTO = new StoreTvMenuPageDTO();
        storeTvMenuPageDTO.setTotal(pageResult.getTotal());
        storeTvMenuPageDTO.setDataList(storeTvMenuDTOs);
        return storeTvMenuPageDTO;
    }


}
