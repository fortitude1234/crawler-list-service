package com.dianping.ssp.crawler.list.quartz;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by iClaod on 10/19/16.
 */
@Component
public class MtMarketingTrigger extends AbstractTrigger {

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

    private static final String MT_MARKETING_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.wechat";
    private static final String MT_MARKETING_LIST_DOMAINTAG = "meituan-marketing-list";
    private static final String URL_PATTERN = "http://link.meituan.com/pub/app/post/load?pageNo=%d&pageSize=10&boardId=2";

    @Override
    @Scheduled(cron="0 45 8-23 * * ?")
    public void run() {
        LOGGER.info("trigger job: " + MT_MARKETING_LIST_DOMAINTAG);

        if (Lion.getBooleanValue(MT_MARKETING_LION_SWITCH, false)) {
            List<String> urls = Lists.newArrayList();
            for (int i = 1; i <= 5; i++) {
                urls.add(String.format(URL_PATTERN, i));
            }
            urlManagerService.addUrl2DomainTag(MT_MARKETING_LIST_DOMAINTAG, urls);
            LOGGER.info("job: " + MT_MARKETING_LIST_DOMAINTAG + " add urls count: " + urls.size());
        }

    }
}
