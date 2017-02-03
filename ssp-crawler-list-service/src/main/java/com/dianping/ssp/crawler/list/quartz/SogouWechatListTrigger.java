package com.dianping.ssp.crawler.list.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.contants.DuplicateType;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;

import java.util.List;

/**
 * Created by iClaod on 10/19/16.
 */
@Component
public class SogouWechatListTrigger extends AbstractTrigger{

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

//    private List<String> wechatList = Lists.newArrayList("Canmouchang", "canyingonghui", "shijie-henda"
//            , "Joplus", "shaozisxy", "weihaizixun365", "xiaozaoketang");


    private static final String WECHAT_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.sogouwechat";
    private static final String WECHAT_LIST_LION_KEY = "ssp-crawler-list-service.crawler.wechat.sogou.list";
    private static final String WECHAT_LIST_DOMAINTAG = "sogou-weixin-list";
    private static final String URL_PATTERN = "http://weixin.sogou.com/weixin?type=1&query=%s&ie=utf8&_sug_=n&_sug_type_=";

    @Override
    @Scheduled(cron="0 0 8/1 * * ?")
    public void run() {
        addUrl(false, JSON.parseArray(Lion.get(WECHAT_LIST_LION_KEY, "[]"), String.class));
    }
    
    
    public void readCountRun(){
    	addUrl(false,Lists.newArrayList("cylbnc","ygcywzz"));
    }
    
    public void addUrl(boolean duplicate,List<String> wechatList){
    	LOGGER.info("trigger job: " + WECHAT_LIST_DOMAINTAG);
        List<Request> requests = Lists.newArrayList();
        try {
            //sleep random from 0 to 10 minutes
            Thread.sleep((long) (Math.random() * 15 * 60 * 1000));
        } catch (Exception e) {
            LOGGER.error("failed to sleep in sogou-weixin-list-trigger", e);
        }
        LOGGER.info("trigger job: " + WECHAT_LIST_DOMAINTAG);
        List<String> urls = Lists.newArrayList();
        if (Lion.getBooleanValue(WECHAT_LION_SWITCH, false) && CollectionUtils.isNotEmpty(wechatList)) {
            for (String wechatName: wechatList) {
                String url = String.format(URL_PATTERN, wechatName);
                Request r =new Request();
                r.setUrl(url);
                if (duplicate){
            		r.putExtra("duplicate", DuplicateType.NEED_DUPLICATE);
                }
                requests.add(r);
            }
        }
        if (CollectionUtils.isNotEmpty(requests)) {
            urlManagerService.addReqeust2DomainTag(WECHAT_LIST_DOMAINTAG, requests);
            LOGGER.info("job: " + WECHAT_LIST_DOMAINTAG + " add urls count: " + requests.size());
        }
    }

}
