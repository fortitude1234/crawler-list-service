package com.dianping.ssp.crawler.list.api;

import java.util.List;

import us.codecraft.webmagic.Request;

/**
 * Created by iClaod on 10/19/16.
 */
public interface UrlManagerService {

    int addUrl2DomainTag(String domainTag, List<String> urls);
    
    int addReqeust2DomainTag(String domainTag,List<Request> requests);
}
