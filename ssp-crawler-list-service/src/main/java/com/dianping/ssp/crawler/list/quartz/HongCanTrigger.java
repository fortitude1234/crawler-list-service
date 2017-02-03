package com.dianping.ssp.crawler.list.quartz;

import java.util.List;

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
public class HongCanTrigger extends AbstractTrigger{

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

    private static final String urlPatter = "http://www.canyin88.com/plus/ajaxindex.php?cpage=%d&psize=10&other=index";
    private static final String url4Fangtan = "http://www.canyin88.com/plus/ajaxindex.php?typeid=238&cpage=0&psize=10";
    private static final String url4channel = "http://www.canyin88.com/plus/ajaxindex.php?typeid=%d&cpage=%d&psize=10";
    private static final String HONGCAN_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.hongcan";
    private static final String HONGCAN_LIST_DOMAINTAG = "hongcan-list";
    
    private static final List<Integer> channelList=Lists.newArrayList(249,238,257,272,250);
    
    @Override
    public void run() {
        addUrl();
    }
    
    public void addUrl(){
    	LOGGER.info("trigger job: " + HONGCAN_LIST_DOMAINTAG);
        if (Lion.getBooleanValue(HONGCAN_LION_SWITCH, false)) {
            List<Request> requests = Lists.newArrayList();
            for (Integer ch:channelList){
	            for (int i=0; i<10; i++) {
	            	Request r= new Request();
	            	r.setUrl(String.format(url4channel, ch,i));
	            	requests.add(r);
	            }
            }
            for (int i=0;i<10;i++){
            	Request r= new Request();
            	r.setUrl(String.format(urlPatter,i));
            	requests.add(r);
            }
            urlManagerService.addReqeust2DomainTag(HONGCAN_LIST_DOMAINTAG, requests);
            LOGGER.info("job: " + HONGCAN_LIST_DOMAINTAG + " add urls count: " + requests.size());
        }
    }
}
