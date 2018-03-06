package com.huofu.module.i5wei.setting.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

@Table(name="tb_store_default_tag_message",dalParser=BaseDefaultStoreDbRouter.class)
public class StoreDefaultTagMessage extends AbsEntity{
	/**
     * 自增ID
     */
    @Id
    @Column("tid")
    private long tid;

    /**
     * 默认标签信息
     */
    @Column("tag_message")
    private String tagMessage;

    /**
     * 标签类型，0：退款/退菜原因，1：整单备注
     */
    @Column("tag_type")
    private int tagType;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 修改时间
     */
    @Column("update_time")
    private long updateTime;

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public String getTagMessage() {
		return tagMessage;
	}

	public void setTagMessage(String tagMessage) {
		this.tagMessage = tagMessage;
	}

	public int getTagType() {
		return tagType;
	}

	public void setTagType(int tagType) {
		this.tagType = tagType;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
}
