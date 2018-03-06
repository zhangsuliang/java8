package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemWeek;
import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALParserUtil;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;
import huofuhelper.util.thrift.ThriftInvoker;
import huofuhelper.util.thrift.ThriftParam;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreChargeItemWeekDAO extends AbsQueryDAO<StoreChargeItemWeek> {

    private static final String idKey = "store_charge_item_week";

    @Autowired
    private IdMakerUtil idMakerUtil;

    private RowMapper<Long> longRowMapper = (rs, rowNum) -> rs.getLong(1);

    private RowMapper<Integer> intRowMapper = (rs, rowNum) -> rs.getInt(1);

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public List<StoreChargeItemWeek> batchCreate(List<StoreChargeItemWeek> list) {
        return super.batchCreate(list);
    }

    public List<StoreChargeItemWeek> batchCreate(List<StoreChargeItemWeek> list, List<Long> ids) {
        if (list.isEmpty()) {
            return list;
        }
        int merchantId = list.get(0).getMerchantId();
        long storeId = list.get(0).getStoreId();
        this.addDbRouteInfo(merchantId, storeId);
        List<Long> idList = ids;
        if (idList == null) {
            idList = this._createIds(list.size(), null);
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setItemWeekId(idList.get(i));
        }
        return super.batchCreate(list);
    }

    public List<Long> createIds(int size) {
        ThriftParam thriftParam = new ThriftParam();
        thriftParam.setSoTimeout(15000);
        return _createIds(size, thriftParam);
    }

    private List<Long> _createIds(int size, ThriftParam thriftParam) {
        try {
            List<Long> idList = Lists.newArrayList();
            int fixedSize = 500;
            if (size > fixedSize) {
                int sizea = size / fixedSize;
                int sizeb = size % fixedSize;
                for (int i = 0; i < sizea; i++) {
                    ThriftInvoker.invoke(thriftParam, () -> {
                        idList.addAll(this.idMakerUtil.nextIds(idKey, fixedSize));
                        return null;
                    });
                }
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.idMakerUtil.nextIds(idKey, sizeb));
                    return null;
                });
            } else {
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.idMakerUtil.nextIds(idKey, size));
                    return null;
                });
            }
            return idList;
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量插入周期设置，其中item_week_id已经存在
     *
     * @param list
     * @return
     */
    public List<StoreChargeItemWeek> batchCreateHasId(int merchantId, long storeId, List<StoreChargeItemWeek> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(merchantId, storeId);
        return super.batchCreate(list);
    }

    /**
     * 获得指定收费项目id集合所有有的有效周期
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param time          指定时间
     * @param chargeItemIds 收费项目id几个
     * @return 查询结果
     */
    public List<StoreChargeItemWeek> getListInChargeItemIds(int merchantId, long storeId, long time,
                                                            List<Long> chargeItemIds) {
        if (chargeItemIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreChargeItemWeek.class, "where store_id=? and end_time>=?", "charge_item_id",
                Lists.newArrayList(storeId, time), chargeItemIds);
    }

    public List<StoreChargeItemWeek> getListByChargeItemId(int merchantId, long storeId, long chargeItemId, long time) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeItemWeek.class, "where store_id=? and charge_item_id=? and end_time>=?",
                new Object[]{storeId, chargeItemId, time});
    }

    /**
     * 获得有效的收费项目id
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param timeBucketId 营业时段id。可以为0，表示不查询营业时段
     * @param weekDay      指定周期。可以为0，表示不查询星期X
     * @param time         指定的在有效时间内
     * @return 查询结果
     */
    public List<Long> getChargeItemIdsForWeekDay(int merchantId, long storeId, long timeBucketId, int weekDay,
                                                 long time, boolean enableSlave, boolean enableCache) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        DALInfo dalInfo = DALParserUtil.process(StoreChargeItemWeek.class);
        String tname = dalInfo.getRealTable(StoreChargeItemWeek.class);
        List<Object> params = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct(charge_item_id) from ");
        sb.append(tname);
        sb.append(" where store_id=?");
        params.add(storeId);
        if (timeBucketId > 0) {
            sb.append(" and time_bucket_id=?");
            params.add(timeBucketId);
        }
        if (weekDay > 0) {
            sb.append(" and week_day=?");
            params.add(weekDay);
        }
        sb.append(" and begin_time<=? and end_time>? and deleted=?");
        params.add(time);
        params.add(time);
        params.add(false);
        return this.query.getJdbcSupport().list(sb.toString(), params.toArray(), longRowMapper);
    }

    /**
     * 获得指定日期有效的收费项目周期设置。<br>
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param date       日期的时间戳
     * @return 查询结果
     */
    public List<StoreChargeItemWeek> getListForDate(int merchantId, long storeId, int weekDay, long date) {
        this.addDbRouteInfo(merchantId, storeId);
        long beginTime = DateUtil.getBeginTime(date, null);
        long endTime = DateUtil.getEndTime(date, null);
        return this.query.list(StoreChargeItemWeek.class,
                "where store_id=? and deleted=? and week_day=? and " + "begin_time<=? and end_time>=?",
                new Object[]{storeId, false, weekDay, beginTime, endTime});
    }

    public List<Long> getChargeItemIds4Sort(int merchantId, long storeId, long timeBucketId) {
        this.addDbRouteInfo(merchantId, storeId);
        long todayEnd = DateUtil.getEndTime(System.currentTimeMillis(), null);
        DALInfo dalInfo = DALParserUtil.process(StoreChargeItemWeek.class);
        String tName = dalInfo.getRealTable(StoreChargeItemWeek.class);
        String sql = "select distinct(charge_item_id) from " + tName
                + " where store_id=? and time_bucket_id=? and end_time>=? and deleted=?";
        return this.query.getJdbcSupport().list(sql, new Object[]{storeId, timeBucketId, todayEnd, false},
                longRowMapper);
    }

    public int deleteForFuture(int merchantId, long storeId, long chargeItemId, long beginTime) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreChargeItemWeek.class, "where store_id=? and charge_item_id=? and begin_time>=?",
                new Object[]{storeId, chargeItemId, beginTime});
    }

    public int updateEndTimeByTimeBucketIdForPast(int merchantId, long storeId, long timeBucketId, long endTime,
                                                  long time) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeItemWeek.class,
                "set end_time=? where store_id=? and time_bucket_id=? and begin_time<? and end_time>=?",
                new Object[]{endTime, storeId, timeBucketId, time, time});
    }

    public int deleteByTimeBucketIdForFuture(int merchantId, long storeId, long timeBucketId, long time) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreChargeItemWeek.class, "where store_id=? and time_bucket_id=? and begin_time>=?",
                new Object[]{storeId, timeBucketId, time});
    }

    /**
     * 更新在指定开始时间之前的数据的截止时间
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param chargeItemId 收费项目id
     * @param beginTime    开始时间
     * @param minEndTime   最小结束时间
     * @param newEndTime   新的结束时间
     * @return 更新数量
     */
    public int updateEndTimeForValid(int merchantId, long storeId, long chargeItemId, long beginTime, long minEndTime,
                                     long newEndTime) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeItemWeek.class,
                "set end_time=? where store_id=? and charge_item_id=? and begin_time<? and end_time>=? and deleted=?",
                new Object[]{newEndTime, storeId, chargeItemId, beginTime, minEndTime, false});
    }

    public int countByChargeItemIdAndTimeBucketId(int merchantId, long storeId, long chargeItemId, long timeBucketId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreChargeItemWeek.class,
                "where store_id=? and charge_item_id=? and time_bucket_id=? and deleted=?",
                new Object[]{storeId, chargeItemId, timeBucketId, false});
    }

    /**
     * 获取指定日期存在的营业时段id
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param time       指定日期的时间 00:00:00
     * @param weekDay    指定星期[X]
     * @return 营业时段id
     */
    public List<Long> getTimeBucketIdsForDate(int merchantId, long storeId, long time, int weekDay) {
        String tname = this.buildTableName(merchantId, storeId);
        return this.query.getJdbcSupport()
                .list("select distinct(time_bucket_id) from " + tname
                                + " where store_id=? and deleted=? and week_day=? and begin_time<=? and end_time>?",
                        new Object[]{storeId, false, weekDay, time, time}, longRowMapper);
    }

    /**
     * 获得有效期内的星期[X]
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param beginTime  时间下限
     * @param endTime    时间上限
     * @return 星期[X]的set
     */
    public Set<Integer> getWeekDaysForTime(int merchantId, long storeId, long beginTime, long endTime) {
        String tname = this.buildTableName(merchantId, storeId);
        String sql = "select distinct(week_day) from " + tname + " where store_id=?"
                + " and deleted=? and begin_time<=? and end_time>=?";
        List<Integer> weekDays = this.query.getJdbcSupport().list(sql,
                new Object[]{storeId, false, beginTime, endTime}, intRowMapper);
        return new HashSet<>(weekDays);
    }

    /**
     * 获得有效期内的收费项目周期设置。<br>
     * 注:数据的有效期开始时间 <= 赋值的结束时间。<br>
     * 数据有效期结束时间 >= 赋值的有效期开始时间
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param beginTime  时间下限
     * @param endTime    时间上限
     * @return 查询结果
     */
    public List<StoreChargeItemWeek> getListForTimeRange(int merchantId, long storeId, long beginTime, long endTime) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeItemWeek.class,
                "where store_id=? and deleted=? and begin_time<=? and " + "end_time>=?",
                new Object[]{storeId, false, beginTime, endTime});
    }

    private String buildTableName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreChargeItemWeek.class);
        return dalInfo.getRealTable(StoreChargeItemWeek.class);
    }

    public int getTimeBucketForPastCount(int merchantId, long storeId, long timeBucketId, long time,
                                         boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreChargeItemWeek.class,
                "where store_id=? and time_bucket_id=? and begin_time<=? and end_time>=? and deleted=?",
                new Object[]{storeId, timeBucketId, time, time, false});
    }

    public List<StoreChargeItemWeek> getTimeBucketForPastPage(int merchantId, long storeId, long timeBucketId,
                                                              long time, int begin, int size, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.mysqlList(StoreChargeItemWeek.class,
                "where store_id=? and time_bucket_id=? and begin_time<=? and end_time>=? and deleted=?", begin, size,
                new Object[]{storeId, timeBucketId, time, time, false});
    }

    public int getTimeBucketForFeatureCount(int merchantId, long storeId, long timeBucketId, long time,
                                            boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreChargeItemWeek.class,
                "where store_id=? and time_bucket_id=? and begin_time>=? and deleted=?",
                new Object[]{storeId, timeBucketId, time, false});
    }

    public List<StoreChargeItemWeek> getTimeBucketForFeaturePage(int merchantId, long storeId, long timeBucketId,
                                                                 long time, int begin, int size, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.mysqlList(StoreChargeItemWeek.class,
                "where store_id=? and time_bucket_id=? and begin_time>=? and deleted=?", begin, size,
                new Object[]{storeId, timeBucketId, time, false});
    }

    /**
     * 批量删除
     *
     * @param merchantId
     * @param storeId
     * @param chargeItemWeekBackIds
     * @return
     */
    public void batchDelete(int merchantId, long storeId, List<Long> chargeItemWeekBackIds) {
        if (!chargeItemWeekBackIds.isEmpty()) {
            this.addDbRouteInfo(merchantId, storeId);
            List<Object[]> valuesList = new ArrayList<Object[]>();
            for (Long chargeItemWeekBackId : chargeItemWeekBackIds) {
                valuesList.add(new Object[]{chargeItemWeekBackId});
            }
            this.query.batchDelete(StoreChargeItemWeek.class, "where item_week_id = ?", valuesList);
        }
    }

}
