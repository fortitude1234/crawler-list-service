package com.dianping.ssp.crawler.list.quartz;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.utils.HttpConstant;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author Mr.Bian
 *
 */
@Component
public class EZhanWangTrigger extends AbstractTrigger{
	private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());
	
	private List<String> keyWordIds=Lists.newArrayList("34","39","18","44");
	private static int pageCount=5;
	private static final String URL="http://www.eshow365.com/ZhanHui/Ajax/AjaxSearcherV3.aspx";
	private static final String DOMAIN_TAG="ezhanwang-list";
	private static final String SWITCH="ssp-crawler-list-service.crawler.trigger.switch.ezhanwang";
	
	@Autowired
	private UrlManagerService urlManagerService;
	
	@Override
	@Scheduled(cron = "0 0 10,18 * * ?")
	public void run() {
		LOGGER.info("e-zhan wang trigger Run!");
		if (Lion.getBooleanValue(SWITCH, false)) {
			List<Request> requests=Lists.newArrayList();
			for (String keyWordId:keyWordIds){
				for (int i=1;i<=pageCount;++i){
					Map<String,String> params=Maps.newHashMap();
					params.put("startendtime", "2020/12/31");
					params.put("starttime", new DateTime().toString("YYYY/MM/dd"));
					params.put("hangyeci", keyWordId);
					params.put("page", i+"");
					params.put("tag", "0");
					params.put("1", "1");
					Request r=new Request();
					r.setUrl(URL);
					r.setMethod(HttpConstant.Method.POST);
					Map<String,Object> extra=Maps.newHashMap();
					extra.put("params", params);
					r.setExtras(extra);
					requests.add(r);
				}
			}
			urlManagerService.addReqeust2DomainTag(DOMAIN_TAG, requests);
			LOGGER.info("e-zhan wang trigger End Success!");
		}
	}

}
