package com.dianping.ssp.crawler.list.quartz;

import java.util.List;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.contants.DuplicateType;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;

/**
 * Created by iClaod on 10/19/16.
 */
@Component
public class WechatListTrigger extends AbstractTrigger{

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());
    private static final String DATE_PATTERN="YYYY-MM-dd";
    
    @Autowired
    private UrlManagerService urlManagerService;


    private static final String WECHAT_LIST_LION_KEY = "ssp-crawler-list-service.crawler.wechat.gsdata.list";

    private static final String WECHAT_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.wechat";
    private static final String WECHAT_LIST_DOMAINTAG = "gsdata-list";
    private static final String URL_PATTERN = "http://www.gsdata.cn/Query/article?q=%s&search_field=0&post_time=5&sort=-1&cp=0&range_title=0&range_content=0&range_wx=1&date=%s_%s";
    
    
    @Override
    @Scheduled(cron="0 0 8-23 * * ?")
    public void run() {
    	addUrl(false);
    }
    
    @Scheduled(cron="0 0 0 * * ?")
    public void noRemoveDuplicateRun(){
    	addUrl(true);
    }
    
    
    public void addUrl(boolean duplicate){
    	 LOGGER.info("trigger job: " + WECHAT_LIST_DOMAINTAG);
         List<Request> requests = Lists.newArrayList();
         List<String> wechatList=JSON.parseArray(Lion.get(WECHAT_LIST_LION_KEY, "[]"), String.class);
         if (Lion.getBooleanValue(WECHAT_LION_SWITCH, false) && CollectionUtils.isNotEmpty(wechatList)) {
             DateTime dt=new DateTime();
             DateTime threeDAgo=dt.minusDays(3);
             DateTime fourDAgo=dt.minusDays(4);
             DateTime servenDAgo=dt.minusDays(7);
             for (String wechatName: wechatList) {
                 String url3 = String.format(URL_PATTERN, wechatName, threeDAgo.toString(DATE_PATTERN), dt.toString(DATE_PATTERN));
                 String url7=String.format(URL_PATTERN, wechatName, servenDAgo.toString(DATE_PATTERN), fourDAgo.toString(DATE_PATTERN));
                 Request r3 = new Request();
                 r3.setUrl(url3);
                 Request r7= new Request();
                 r7.setUrl(url7);
                 if (duplicate){
             		r3.putExtra("duplicate", DuplicateType.NEED_DUPLICATE);
             		r7.putExtra("duplicate", DuplicateType.NEED_DUPLICATE);
                 }
                 requests.add(r3);
                 requests.add(r7);
             }
         }
         if (CollectionUtils.isNotEmpty(requests)) {
             urlManagerService.addReqeust2DomainTag(WECHAT_LIST_DOMAINTAG, requests);
             LOGGER.info("job: " + WECHAT_LIST_DOMAINTAG + " add urls count: " + requests.size());
         }
    }

}
