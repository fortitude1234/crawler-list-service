package com.dianping.ssp.crawler.list.quartz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;

/**
 *
 * @author Mr.Bian
 *
 */
@Component
public class CCASTrigger extends AbstractTrigger{
	private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());
	
	private static final String URL="http://www.ccas.com.cn/Article/List_141.html";
	private static final String DOMAIN_TAG="ccas-list";
	private static final String SWITCH="ssp-crawler-list-service.crawler.trigger.switch.ccas";
	
	@Autowired
	private UrlManagerService urlManagerService;
	
	@Override
	@Scheduled(cron = "0 0 0 * * ?")
	public void run() {
		LOGGER.info("ccas trigger Run!");
		if (Lion.getBooleanValue(SWITCH, false)) {
			urlManagerService.addUrl2DomainTag(DOMAIN_TAG, Lists.newArrayList(URL));
			LOGGER.info("ccas trigger End Success!");
		}
	}

}
