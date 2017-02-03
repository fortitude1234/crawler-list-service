package com.dianping.ssp.crawler.list.quartz;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;

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
public class FiveEightTrigger extends AbstractTrigger {
	private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());
	private static final int pageCount = 10;
	private String BASE_URL = "http://sh.58.com/%s/pn%d/";
	private static String domain_tag="58-list";
	private List<String> category = Lists.newArrayList("cyzhuangxiu",
			"jiubazx", "kafeitzx", "chaguanzx", "xitong", "zhuce", "jiajusp");
	
	@Autowired
	private UrlManagerService urlManagerService;
	

	@Override
	@Scheduled(cron = "0 0 6 * * ?")
	public void run() {
		LOGGER.info("trigger job: 58定时JOB");
		List<Request> requests = Lists.newArrayList();

		if (Lion.getBooleanValue("ssp-crawler-list-service.crawler.trigger.switch.fiveeight", false)
				&& CollectionUtils.isNotEmpty(category)) {
			for (int i = 1; i <= pageCount; i++) {
				for (String keyword : category) {
					try {
						String url = String.format(BASE_URL, keyword,i);
						Request r = new Request();
						r.setUrl(url);
						requests.add(r);
					} catch (Exception e) {
						LOGGER.error("58 failed to parse argue: "+ keyword, e);
					}
				}
			}
		}
		if (CollectionUtils.isNotEmpty(requests)) {
			urlManagerService.addReqeust2DomainTag(domain_tag,requests);
			LOGGER.info("job: " + domain_tag + " add urls count: "+ requests.size());
		}
	}

}
