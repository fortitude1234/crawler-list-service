package com.dianping.ssp.crawler.list.test;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import us.codecraft.webmagic.Request;

import com.dianping.ssp.crawler.common.scheduler.RedisRepository;
import com.dianping.ssp.crawler.list.base.AbstractTest;

/**
 *
 * @author Mr.Bian
 *
 */
public class EzhanwangTest extends AbstractTest {
	@Autowired
	RedisRepository redisRepository;
	@Test
	public void test() {
		while (true) {
			try {
				Request r=redisRepository.getUrlFromQueue("ezhanwang-list");
				Assert.assertNotNull(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
