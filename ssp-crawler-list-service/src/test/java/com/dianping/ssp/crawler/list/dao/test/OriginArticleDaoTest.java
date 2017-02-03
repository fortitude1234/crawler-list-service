package com.dianping.ssp.crawler.list.dao.test;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.ssp.crawler.list.base.AbstractTest;
import com.dianping.ssp.crawler.list.dao.ArticleOriginDao;
import com.dianping.ssp.crawler.list.po.ArticleOriginPO;
import com.google.common.collect.Lists;

/**
 *
 * @author Mr.Bian
 *
 */
public class OriginArticleDaoTest extends AbstractTest{
	@Autowired
	ArticleOriginDao dao;
	
	@Test
	public void getArticleByDomainTagAndUrlTest(){
		List<ArticleOriginPO> pos=dao.getArticleByDomainTagAndUrl("http://mp.weixin.qq.com/s?__biz=MjM5MjAxOTE0MA==&mid=2652861089&idx=2&sn=2cc42b9327711367867c844e1f310392&chksm=bd47750a8a30fc1c95fd9de3807b2c27ae8288fa07431f4aaff953e61d1858ec6a7bb6ac8c53&scene=4#wechat_redirect", "wechat-parser");
		Assert.assertTrue(!pos.isEmpty());
	}
	
	@Test
	public void insertTest(){
		ArticleOriginPO po = new ArticleOriginPO();
		po.setCrawlTime(new Date());
		po.setAuthor("tes");
		po.setContentUrl("123");
		po.setTitle("123");
		dao.insertList(Lists.newArrayList(po));
	}
}
