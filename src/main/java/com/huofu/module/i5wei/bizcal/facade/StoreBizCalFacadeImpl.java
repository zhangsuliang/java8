package com.huofu.module.i5wei.bizcal.facade;

import com.huofu.module.i5wei.menu.facade.MenuFacadeUtil;
import com.huofu.module.i5wei.menu.service.DateBizCal;
import com.huofu.module.i5wei.menu.service.StoreMenuService;
import com.huofu.module.i5wei.menu.service.TimeBucketMenuCal;
import huofucore.facade.i5wei.bizcal.DateBizCalDTO;
import huofucore.facade.i5wei.bizcal.StoreBizCalFacade;
import huofucore.facade.i5wei.bizcal.TimeBucketBizCalDTO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ThriftServlet(name = "storeBizCalFacadeServlet", serviceClass =
        StoreBizCalFacade.class)
@Component
public class StoreBizCalFacadeImpl implements StoreBizCalFacade.Iface {

    @Autowired
    private StoreMenuService storeMenuService;

    @Autowired
    private MenuFacadeUtil menuFacadeUtil;

    @Override
    public List<DateBizCalDTO> getBizCalendar(int merchantId, long storeId, long beginDate, long endDate) throws TException {
        long _beginDate = DateUtil.getBeginTime(beginDate, null);
        long _endDate = DateUtil.getBeginTime(endDate, null);
        if (_beginDate > _endDate) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID
                    .getValue(), "beginDate[" + _beginDate + "] must <= " +
                    "endDate[" + _endDate + "]");
        }
        List<DateBizCal> dateBizCals = storeMenuService
                .getDateBizCalsForDateRange(merchantId, storeId, _beginDate,
                        _endDate, false);
        List<DateBizCalDTO> dateBizCalDTOs = new ArrayList<>();
        for (DateBizCal dateBizCal : dateBizCals) {
            dateBizCalDTOs.add(this._buildDateBizCalDTO(dateBizCal));
        }
        return dateBizCalDTOs;
    }

    @Override
    public DateBizCalDTO getBizCalendarForDate(int merchantId, long storeId, long date) throws TException {
        DateBizCal dateBizCal = this.storeMenuService.getDateBizCalForDate
                (merchantId, storeId, date);
        return this._buildDateBizCalDTO(dateBizCal);
    }

    private DateBizCalDTO _buildDateBizCalDTO(DateBizCal dateBizCal) {
        DateBizCalDTO dateBizCalDTO = new DateBizCalDTO();
        BeanUtil.copy(dateBizCal, dateBizCalDTO);
        List<TimeBucketBizCalDTO> timeBucketBizCalDTOs = new ArrayList<>();
        for (TimeBucketMenuCal timeBucketMenuCal : dateBizCal.getTimeBucketMenuCals()) {
            TimeBucketBizCalDTO timeBucketBizCalDTO = new
                    TimeBucketBizCalDTO();
            BeanUtil.copy(timeBucketMenuCal, timeBucketBizCalDTO);
            if (timeBucketMenuCal.getStoreTimeBucket() != null) {
                StoreTimeBucketDTO storeTimeBucketDTO = this.menuFacadeUtil.buildStoreTimeBucketDTO(timeBucketMenuCal.getStoreTimeBucket());
                timeBucketBizCalDTO.setStoreTimeBucketDTO(storeTimeBucketDTO);
                timeBucketBizCalDTOs.add(timeBucketBizCalDTO);
            }
        }
        dateBizCalDTO.setTimeBucketBizCalDTOs(timeBucketBizCalDTOs);
        return dateBizCalDTO;
    }
}
