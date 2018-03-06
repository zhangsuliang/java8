package com.huofu.module.i5wei.promotion.service;

import com.google.common.collect.Lists;
import huofucore.facade.i5wei.promotion.StorePromotionStatusEnum;

import java.util.List;

/**
 * Created by akwei on 11/16/16.
 */
public class StorePromotionSummary<T> {

    private int count4Doing = 0;

    private int count4Paused = 0;

    private int count4NotBegin = 0;
    
    private int count4Ended = 0;
    
    private int count4NotOpened = 0;
    
    private List<T> list4Doing = Lists.newArrayList();

    private List<T> list4Paused = Lists.newArrayList();

    private List<T> list4NotBegin = Lists.newArrayList();

    private List<T> list4Ended=Lists.newArrayList();
    
    private List<T> list4NotOpened=Lists.newArrayList();
    
    public int getCount4Doing() {
        return count4Doing;
    }

    public void setCount4Doing(int count4Doing) {
        this.count4Doing = count4Doing;
    }

    public int getCount4Paused() {
        return count4Paused;
    }

    public void setCount4Paused(int count4Paused) {
        this.count4Paused = count4Paused;
    }

    public int getCount4NotBegin() {
        return count4NotBegin;
    }
    public void setCount4NotOpened(int count4NotOpened) {
		this.count4NotOpened = count4NotOpened;
	}
    
    public int getCount4NotOpened() {
		return count4NotOpened;
	}

    public void setCount4NotBegin(int count4NotBegin) {
        this.count4NotBegin = count4NotBegin;
    }

    public List<T> getList4Doing() {
        return list4Doing;
    }

    public void setList4Doing(List<T> list4Doing) {
        this.list4Doing = list4Doing;
    }

    public List<T> getList4Paused() {
        return list4Paused;
    }

    public void setList4Paused(List<T> list4Paused) {
        this.list4Paused = list4Paused;
    }

    public List<T> getList4NotBegin() {
        return list4NotBegin;
    }

    public void setList4NotBegin(List<T> list4NotBegin) {
        this.list4NotBegin = list4NotBegin;
    }

    public int getCount4Ended() {
        return count4Ended;
    }

    public void setCount4Ended(int count4Ended) {
        this.count4Ended = count4Ended;
    }
    
    
    public List<T> getList4Ended() {
		return list4Ended;
	}

	public void setList4Ended(List<T> list4Ended) {
		this.list4Ended = list4Ended;
	}

	public List<T> getList4NotOpened() {
		return list4NotOpened;
	}

	public void setList4NotOpened(List<T> list4NotOpened) {
		this.list4NotOpened = list4NotOpened;
	}

	public void build(T t, int status) {
        if (status == StorePromotionStatusEnum.NOT_BEGIN.getValue()) {
            count4NotBegin++;
            list4NotBegin.add(t);
        } else if (status == StorePromotionStatusEnum.PAUSED.getValue()) {
            count4Paused++;
            list4Paused.add(t);
        } else if (status == StorePromotionStatusEnum.DOING.getValue()) {
            count4Doing++;
            list4Doing.add(t);
        }else if (status==StorePromotionStatusEnum.ENDED.getValue()){
        	count4Ended++;
        	list4Ended.add(t);
        }else if(status==StorePromotionStatusEnum.NOT_OPENED.getValue()){
        	count4NotOpened++;
            list4NotOpened.add(t);
        }
    }

	@Override
	public String toString() {
		return "StorePromotionSummary [count4Doing=" + count4Doing + ", count4Paused=" + count4Paused
				+ ", count4NotBegin=" + count4NotBegin + ", count4Ended=" + count4Ended + ", count4NotOpened="
				+ count4NotOpened + ", list4Doing=" + list4Doing + ", list4Paused=" + list4Paused + ", list4NotBegin="
				+ list4NotBegin + ", list4Ended=" + list4Ended + ", list4NotOpened=" + list4NotOpened + "]";
	}
	
}
