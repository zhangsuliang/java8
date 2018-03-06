package com.huofu.module.i5wei.base;

import huofuhelper.module.event.EventData;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.sqs.SNSHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by jiajin.nervous on 16/5/26.
 */
@Component
public class SnsPublish {

    @Autowired
    private SNSHelper snsHelper;

    private final static Log log = LogFactory.getLog(SnsPublish.class);

    private static final int I5wei = 3;//详见 http://doc.5wei.com/pages/viewpage.action?pageId=25660227

    //SNS发布事件
    public void publish(Map<String,Object> dataMap,int eventType,String topicArn) {

        EventData eventData = new EventData();
        eventData.setModuleId(I5wei);
        eventData.setEventType(eventType);
        eventData.setDataMap(dataMap);
        try {
            //snsHelper.publishObj(topicArn, eventData);
        } catch (Exception e) {
            log.error(e.getMessage() + "eventData : " + JsonUtil.build(eventData));
        }
    }
}
