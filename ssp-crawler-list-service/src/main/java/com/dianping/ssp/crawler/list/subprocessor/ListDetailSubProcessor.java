package com.dianping.ssp.crawler.list.subprocessor;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import com.alibaba.fastjson.JSON;
import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.ssp.article.constant.OriginalStatusCode;
import com.dianping.ssp.crawler.common.contants.CrawlerCommonConstants;
import com.dianping.ssp.crawler.common.contants.DuplicateType;
import com.dianping.ssp.crawler.common.entity.ProStatus;
import com.dianping.ssp.crawler.common.entity.ProcessorContext;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.CrawlerSubProcessTag;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.ICrawlerSubProcess;
import com.dianping.ssp.crawler.list.dao.ArticleOriginDao;
import com.dianping.ssp.crawler.list.log.CrawlerListServiceLogEnum;
import com.dianping.ssp.crawler.list.po.ArticleOriginPO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author Mr.Bian
 *
 */
@CrawlerSubProcessTag(name = "listDetailSubProcessor")
public class ListDetailSubProcessor implements ICrawlerSubProcess{
	private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerListServiceLogEnum.LIST_DETAIL_PROCESSOR.getValue());
	
	@Autowired
	private ArticleOriginDao articleOriginDao;
	
	@Override
	public ProStatus process(Page page) {
		String listDomainTag=(String) ProcessorContext.getContext(page).getParam(CrawlerCommonConstants.ProcessorContextConstant.DOMAIN_TAG);
		String parserDomaintag=listDomainTag.replace("list", "parser");
		List<Request> requestList=page.getTargetRequests();
		List<ArticleOriginPO> poList=Lists.newArrayList();
		Iterator<Request> it = requestList.iterator();
		while(it.hasNext()){
			Request request=it.next();
			ArticleOriginPO po=getArticleOriginPO(request,parserDomaintag);
			if (po!=null){
				poList.add(po);
				it.remove();
			}
		}
		if (!CollectionUtils.isEmpty(poList)){
			articleOriginDao.insertList(poList);
		}
		return ProStatus.success();
	}
	
	public ArticleOriginPO getArticleOriginPO(Request request,String domainTag){
		ArticleOriginPO po = new ArticleOriginPO();
		String url=request.getUrl();
		List<ArticleOriginPO> similarPOList=articleOriginDao.getArticleByDomainTagAndUrl(url, domainTag);
		if (!CollectionUtils.isEmpty(similarPOList)){
			ArticleOriginPO similarPo=similarPOList.get(0);
			BeanUtils.copyProperties(similarPo, po);
			po.setSimilarId(similarPo.getId());
			Map<String,String> extraMap=Maps.newHashMap();
			String readCount = (String)request.getExtra("readCount");
			if (StringUtils.isNotEmpty(readCount)) {
				extraMap.put("readCount", readCount);
			}
			po.setCrawlTime(new Date());
			po.setExtraMessage(JSON.toJSONString(extraMap));
			po.setStatus(OriginalStatusCode.REPAT_CRAWL);
			po.setDomainTag(domainTag);
			return po;
		}else{
			return null;
		}
		
	}

}
