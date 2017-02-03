package com.dianping.ssp.crawler.list.service.impl;

import com.dianping.ssp.crawler.common.scheduler.RedisRepository;
import com.dianping.ssp.crawler.list.api.UrlManagerService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.codecraft.webmagic.Request;

import java.util.List;

/**
 * Created by iClaod on 10/19/16.
 */
@Service("urlManagerService")
public class UrlManagerServiceImpl implements UrlManagerService{

    @Autowired
    private RedisRepository redisRepository;

    public int addUrl2DomainTag(String domainTag, List<String> urls) {
        int successCount = 0;
        if (CollectionUtils.isNotEmpty(urls)) {
            for (String url: urls) {
                Request request = new Request();
                request.setUrl(url);
                redisRepository.addToUrlQueue(domainTag, request);
                successCount ++;
            }
        }
        return successCount;
    }

	@Override
	public int addReqeust2DomainTag(String domainTag, List<Request> requests) {
		int count=0;
		if (!CollectionUtils.isEmpty(requests)){
			for (Request request:requests){
				boolean result =redisRepository.addToUrlQueue(domainTag, request);
				if(result){
					count ++;
				}
			}
		}
		return count;
	}
}
