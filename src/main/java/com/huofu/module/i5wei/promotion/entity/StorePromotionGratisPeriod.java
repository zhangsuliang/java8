package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreDateTimeBucket;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;
/**
 * 买赠活动周期表
 * @author Suliang
 * @Date  2016-12-21
 */
@SuppressWarnings("all")
@Table(name="tb_store_promotion_gratis_period",dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionGratisPeriod extends BaseEntity {
	/**
	 * 买赠商品周期表Id(自增)
	 */
	@ThriftField(1)
	@Id
    @Column("tid")
	private long tid;
	
	/**
	 * 买赠活动ID
	 */
	@ThriftField(2)
	@Column("promotion_gratis_id")
	private long promotionGratisId;
	
	/**
	 * 商户ID
	 */
	@ThriftField(3)
	@Column("merchant_id")
    private int merchantId;
	
	/**
	 * 店铺ID
	 */
	@ThriftField(4)
	@Column("store_id")
	private long storeId;
	
	/**
	 * 星期日期
	 */
	@ThriftField(5)
	@Column("week_day")
	private int weekDay;
	
	/**
	 * 营业时段
	 */
	@ThriftField(6)
	@Column("time_bucket_id")
	private long timeBucketId;
	
	/**
	 * 创建时间
	 */
	@ThriftField(7)
	@Column("create_time")
	private long  createTime;
	
	/**
	 *营业实体
	 */
	private  StoreDateTimeBucket storeDateTimeBucket;
	
	private StoreTimeBucket storeTimeBucket;
	
	public StoreTimeBucket getStoreTimeBucket() {
		return storeTimeBucket;
	}

	public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
		this.storeTimeBucket = storeTimeBucket;
	}

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public long getPromotionGratisId() {
		return promotionGratisId;
	}

	public void setPromotionGratisId(long promotionGratisId) {
		this.promotionGratisId = promotionGratisId;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public long getTimebucketId() {
		return timeBucketId;
	}

	public void setTimebucketId(long timebucketId) {
		this.timeBucketId = timebucketId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public StoreDateTimeBucket getStoreDateTimeBucket() {
		return storeDateTimeBucket;
	}

	public void setStoreDateTimeBucket(StoreDateTimeBucket storeDateTimeBucket) {
		this.storeDateTimeBucket = storeDateTimeBucket;
	}

	public boolean canUse(int weekDay, long timeBucketId) {
        if (this.weekDay == weekDay && this.timeBucketId == timeBucketId) {
            return true;
        }
        return false;
    }

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

}
