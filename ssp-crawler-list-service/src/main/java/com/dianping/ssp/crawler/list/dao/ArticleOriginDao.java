package com.dianping.ssp.crawler.list.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.ssp.crawler.list.po.ArticleOriginPO;

/**
 *
 * @author Mr.Bian
 *
 */

public interface ArticleOriginDao {

	List<ArticleOriginPO> getArticleByDomainTagAndUrl(@Param("url") String url,
			@Param("domainTag") String domainTag );
	
	void insertList(@Param("list") List<ArticleOriginPO> list);
}
