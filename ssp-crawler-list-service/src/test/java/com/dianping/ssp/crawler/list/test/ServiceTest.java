package com.dianping.ssp.crawler.list.test;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.ssp.crawler.common.spider.SpiderFactory;
import com.dianping.ssp.crawler.common.util.DomainTagUtils;
import com.dianping.ssp.crawler.list.base.AbstractTest;
import com.dianping.ssp.crawler.list.quartz.SogouWechatListTrigger;

/**
 * Created by iClaod on 10/12/16.
 */
public class ServiceTest extends AbstractTest {
    @Autowired
    private SogouWechatListTrigger trigger ;
    @Test
    public void test() throws Exception{
       trigger.readCountRun();
        List<String> allDomainTags = DomainTagUtils.getAllDomainTags();
        if (CollectionUtils.isNotEmpty(allDomainTags)) {
            for (String domainTag : allDomainTags) {
                SpiderFactory.initSpider(domainTag);
            }
        }
       Thread.currentThread().join();
    }
}