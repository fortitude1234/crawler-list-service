package com.dianping.ssp.crawler.list.quartz;

import com.alibaba.fastjson.JSON;
import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.contants.DuplicateType;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.List;
import java.util.Map;

/**
 * Created by iClaod on 10/19/16.
 */
@Component
public class NewRankWechatListTrigger extends AbstractTrigger{

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

    private static final String WECHAT_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.newrankwechat";
    private static final String WECHAT_LIST_LION_KEY = "ssp-crawler-list-service.crawler.wechat.newrank.list";
    private static final String WECHAT_LIST_DOMAINTAG = "newrank-weixin-list";
    private static final String URL_PATTERN = "http://www.newrank.cn/xdnphb/detail/getAccountArticle";

    @Override
    @Scheduled(cron="0 0 8/14 * * ?")
    public void run() {
    	addUrl(false);
    }
    
    @Scheduled(cron="0 0 0 * * ?")
    public void noRemoveDuplicateRun(){
    	addUrl(true);
    }
    
    public void addUrl(boolean duplicate){
    	 LOGGER.info("trigger job: " + WECHAT_LIST_DOMAINTAG);
         List<Request> requests = Lists.newArrayList();
         List<String> uuidList = JSON.parseArray(Lion.get(WECHAT_LIST_LION_KEY, "[]"), String.class);
         if (Lion.getBooleanValue(WECHAT_LION_SWITCH, false) && CollectionUtils.isNotEmpty(uuidList)) {
             for (String line : uuidList) {
                 if (line == null) {
                     continue;
                 }
                 String uuid = line.contains(",")? line.split(",")[0].trim(): line.trim();
                 Request request = new Request(URL_PATTERN);
                 request.setMethod(HttpConstant.Method.POST);
                 Map<String, String> params = Maps.newHashMap();
                 String nonce = generateNonce();
                 params.put("uuid", uuid);
                 params.put("nonce", nonce);
                 params.put("flag", "false");
                 params.put("xyz", generateXyz(uuid, nonce));
                 request.putExtra("params", params);
                 if (duplicate) {
                     request.putExtra("duplicate", DuplicateType.NEED_DUPLICATE);
                 }
                 requests.add(request);
             }
         }
         if (CollectionUtils.isNotEmpty(requests)) {
             urlManagerService.addReqeust2DomainTag(WECHAT_LIST_DOMAINTAG, requests);
             LOGGER.info("job: " + WECHAT_LIST_DOMAINTAG + " add urls count: " + requests.size());
         }
    }

    private static final String decryptedXyzPattern = "/xdnphb/detail/getAccountArticle?AppKey=joker&flag=false&uuid=%s&nonce=%s";

    private String generateXyz(String uuid, String nonce) {
        String decryptedXyz = String.format(decryptedXyzPattern, uuid, nonce);
        return DigestUtils.md5Hex(decryptedXyz.getBytes());
    }

    private static final String nonceDict = "0123456789abcdef";

    private String generateNonce() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++ ) {
            sb.append(nonceDict.charAt((int) Math.floor(Math.random() * 16)));
        }
        return sb.toString();
    }

}
