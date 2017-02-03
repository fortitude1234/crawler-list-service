package com.dianping.ssp.crawler.list.quartz;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianrui03 on 17/1/4.
 */
@Component
public class YiouListTrigger extends  AbstractTrigger{
    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());
    private static final String YIOU_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.yiou";

    @Autowired
    private UrlManagerService urlManagerService;
    private static String YIOU_LIST_DOMINTAG = "yiou-list";

    @Override
    @Scheduled(cron="0 0 0/1 * * ?")
    public void run() {
        List<String> urls = new ArrayList<String>();
    	if (Lion.getBooleanValue(YIOU_LION_SWITCH, false)){
	        for( int i = 1 ; i <= 3 ; i ++){
	            urls.add("http://www.iyiou.com/i/canyin/page/"+i+".html");
	        }
	        LOGGER.info("trigger job: " + YIOU_LIST_DOMINTAG);
	        if ( CollectionUtils.isNotEmpty(urls)) {
	            urlManagerService.addUrl2DomainTag(YIOU_LIST_DOMINTAG, urls);
	
	            LOGGER.info("job: " + YIOU_LIST_DOMINTAG + " add urls count: " + urls.size());
	        }
    	}
    }
}
