package com.dianping.ssp.crawler.list.quartz;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dianping.ssp.crawler.list.api.UrlManagerService;

import java.util.List;

/**
 * Created by iClaod on 10/19/16.
 */
@Component
public class CanYinJListTrigger extends AbstractTrigger{

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

    private List<String> urls = Lists.newArrayList(
            "http://www.canyinj.com/article/lists/category/default_blog/p/1.html",
            "http://www.canyinj.com/article/lists/category/ctsj/p/1.html",
            "http://www.canyinj.com/article/lists/category/cygs/p/1.html",
            "http://www.canyinj.com/article/newslists/category/rz/p/1.html",
            "http://www.canyinj.com/article/newslists/category/jy/p/1.html",
            "http://www.canyinj.com/article/newslists/category/gz/p/1.html",
            "http://www.canyinj.com/article/newslists/category/zs/p/1.html",
            "http://www.canyinj.com/article/newslists/category/gq/p/1.html",
            "http://www.canyinj.com/article/newslists/category/yygl/p/1.html",
            "http://www.canyinj.com/article/newslists/category/cl/p/1.html",
            "http://www.canyinj.com/article/newslists/category/yxtg/p/1.html",
            "http://www.canyinj.com/article/newslists/category/cf/p/1.html",
            "http://www.canyinj.com/article/newslists/category/fw/p/1.html",
            "http://www.canyinj.com/article/newslists/category/guanli/p/1.html"
    );

    private static final String CANYINJ_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.canyinj";
    private static final String CANYINJ_LIST_DOMAINTAG = "canyinj-list";

    @Override
    @Scheduled(cron="0 15 8-23 * * ?")
    public void run() {
        LOGGER.info("trigger job: " + CANYINJ_LIST_DOMAINTAG);
        if (Lion.getBooleanValue(CANYINJ_LION_SWITCH, false) && CollectionUtils.isNotEmpty(urls)) {
            urlManagerService.addUrl2DomainTag(CANYINJ_LIST_DOMAINTAG, urls);
            LOGGER.info("job: " + CANYINJ_LIST_DOMAINTAG + " add urls count: " + urls.size());
        }
    }
}
