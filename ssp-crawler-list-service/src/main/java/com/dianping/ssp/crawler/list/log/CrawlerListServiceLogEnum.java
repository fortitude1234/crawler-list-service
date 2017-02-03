package com.dianping.ssp.crawler.list.log;


import com.dianping.ed.logger.LoggerConfig;
import com.dianping.ed.logger.LoggerLevel;

/**
 * Description
 * Created by yuxiang.cao on 16/5/17.
 */
public enum CrawlerListServiceLogEnum {
	WECHAT_LINK_DETECTOR("wechat_link_detector", "subprocessor", false, LoggerLevel.INFO),
	LIST_DETAIL_PROCESSOR("list_detail_processor","subprocessor", false, LoggerLevel.INFO)
	;

	CrawlerListServiceLogEnum(String name, String category, boolean isError, LoggerLevel level) {
		loggerConfig = new LoggerConfig();
		loggerConfig.setApp(APP_NAME);
		loggerConfig.setCategory(category);
		loggerConfig.setName(name);
		loggerConfig.setLevel(level);
		loggerConfig.setDaily(true);
		loggerConfig.setPerm(false);
		loggerConfig.setError(isError);
	}

	private static final String APP_NAME = "ssp-crawler-list-service";

	private LoggerConfig loggerConfig;

	public LoggerConfig getValue() {
		return loggerConfig;
	}
}
