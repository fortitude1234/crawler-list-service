package com.dianping.ssp.crawler.list.quartz;

import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
public class FairsoTrigger extends AbstractTrigger{

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

    private List<String> keywordsList = Lists.newArrayList("餐饮", "美食", "食品", "食材", "饭店", "餐馆", "餐厅", "餐饮嘉宾", "餐饮大咖");
    private static final int pageCount = 2;

    private static final String FAIRSO_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.fairso";
    private static final String FAIRSO_LIST_DOMAINTAG = "fairso-list";
    private static final String URL_PATTERN = "http://www.fairso.com/index.php?s=/search/index/searchState/2/searchstr/%s/p/%d.html";

    @Override
    @Scheduled(cron="0 10 9 * * ?")
    public void run() {
        addUrl(false);
    }
    
    @Scheduled(cron="0 0 0 * * ?")
    public void noRemoveDuplicateRun(){
    	addUrl(true);
    }
    
    public void addUrl(boolean duplicate){
    	LOGGER.info("trigger job: " + FAIRSO_LIST_DOMAINTAG);
        List<Request> requests = Lists.newArrayList();

        if (Lion.getBooleanValue(FAIRSO_LION_SWITCH, false) && CollectionUtils.isNotEmpty(keywordsList)) {
            for (int i = 1; i <= pageCount; i++) {
                for (String keyword : keywordsList) {
                    try {
                        String encodeKeyword = URLEncoder.encode(keyword, "utf-8");
                        String url = String.format(URL_PATTERN, encodeKeyword, i);
                        Request r=new Request();
                        r.setUrl(url);
                        if (duplicate){
                     		r.putExtra("duplicate", DuplicateType.NEED_DUPLICATE);
                        }
                        requests.add(r);
                    } catch (Exception e) {
                        LOGGER.error("FairsoTrigger failed to parse argue: " + keyword, e);
                    }

                }
            }
        }
        if (CollectionUtils.isNotEmpty(requests)) {
            urlManagerService.addReqeust2DomainTag(FAIRSO_LIST_DOMAINTAG, requests);
            LOGGER.info("job: " + FAIRSO_LIST_DOMAINTAG + " add urls count: " + requests.size());
        }
    }
}
