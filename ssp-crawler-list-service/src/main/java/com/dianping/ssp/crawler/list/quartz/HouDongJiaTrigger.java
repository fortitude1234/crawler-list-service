package com.dianping.ssp.crawler.list.quartz;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
public class HouDongJiaTrigger extends AbstractTrigger {
	private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());
	
	List<String> keyWordList = Lists.newArrayList("餐饮","食品","饭店","餐饮嘉宾","餐饮大咖");
	private static final String URL_PREFIX="http://www.huodongjia.com/search/page-%d/?keyword=%s";
	private static final String DOMAIN_TAG="huodongjia-list";
	private static final String SWITCH="ssp-crawler-list-service.crawler.trigger.switch.huodongjia";
	
	@Autowired
	private UrlManagerService urlManagerService;

	@Override
	@Scheduled(cron = "0 0 10,18 * * ?")
	public void run() {
		LOGGER.info("HouDongJiaTrigger Run!");
		List<String> urlList=Lists.newArrayList();
		if (Lion.getBooleanValue(SWITCH, false)) {
			for (String keyWord:keyWordList){
				for (int i=1;i<=2;++i){
					try {
						String encodeKeyWord=URLEncoder.encode(keyWord,"utf-8");
						String url=String.format(URL_PREFIX, i,encodeKeyWord);
						urlList.add(url);
					} catch (UnsupportedEncodingException e) {
						LOGGER.error("HouDongJiaTrigger URL encode fail! ",e);
					}
				}
			}
			if (!CollectionUtils.isEmpty(urlList)){
				urlManagerService.addUrl2DomainTag(DOMAIN_TAG, urlList);
			}
		}
		LOGGER.info("HouDongJiaTrigger End Success!");
	}

}
