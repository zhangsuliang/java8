package com.huofu.module.i5wei.queue;

import java.util.ResourceBundle;

/**
 * SQS相关配置
 * @author chenkai on 2015/04/28.
 */
public class SQSConfig {

    private static ResourceBundle resourceBundle = null;
    static {
        resourceBundle = ResourceBundle.getBundle("sqs");
    }

    public static String getValue(String key){
        return resourceBundle.getString(key);
    }

    /**
     * 获取访问记录队列
     * @return 访问记录队列
     */
    public static String getDialogVisitQueue() {
        String value = getValue("DIALOG_VISIT_QUEUE");
        return value;
    }
    
    /**
     * 获取交互记录队列
     * @return 交互记录队列
     */
    public static String getDialogTweetQueue() {
        String value = getValue("DIALOG_TWEET_QUEUE");
        return value;
    }
    
    /**
     * 获取订单评分情况队列
     * @return 订单评分情况队列
     */
    public static String getDialogGradeQueue() {
        String value = getValue("DIALOG_GRADE_QUEUE");
        return value;
    }
    
    public static String getSendCouponAmountQueue() {
		String value = getValue("DIALOG_SEND_COUPON_AMOUNT");
		return value;
	}
    
    /**
     * 统计分析消息队列
     * @author chenkai on 2015/06/17.
     */
    public static String getStat5weiOrderPayQueue() {
        return getValue("STAT_5WEI_ORDER_PAY_QUEUE");
    }
    
    public static String getStat5weiOrderRefundQueue() {
        return getValue("STAT_5WEI_ORDER_REFUND_QUEUE");
    }

    /**
     * 商户入账
     *
     * @return 消息队列名称
     */
    public static String getMerchantAccountedQueue() {
        return getValue("MERCHANT_ACCOUNTED_QUEUE");
    }

    /**
     * 店铺统计
     *
     * @return 消息队列名称
     */
    public static String getStoreStatisticsQueue() {
        return getValue("STORE_STATISTICS_QUEUE");
    }
    
    /**
     * 自动打印消息队列
     *
     * @return 消息队列名称
     */
    public static String getPrintMessageNotifyQueue() {
        return getValue("PRINT_MESSAGE_NOTIFY_QUEUE");
    }

    /**
     * 获取5wei组合任务队列名称
     */
    public static String getI5weiCombinTaskQueue(){
        return getValue("5WEI_COMBIN_TASK");
    }

    /**
     * 桌台记录结账中延迟处理消息队列
     * @return
     */
    public static String getTableRecordSettlingCancelMessageQueue() {
        return getValue("5WEI_TABLE_RECORD_SETTLING_CANCEL");
    }
    
    /**
     * 定时取餐
     *
     * @return 消息队列名称
     */
    public static String getTimingTakeCodeQueue() {
        return getValue("5WEI_TIMING_TAKE_ORDER_QUEUE");
    }

    /**
     * 打印机报警
     *
     * @return 消息队列名称
     */
    public static String getPrintAlarmQueue() {
        return getValue("NOTIFY_PRINT_ALARM");
    }
    
}
