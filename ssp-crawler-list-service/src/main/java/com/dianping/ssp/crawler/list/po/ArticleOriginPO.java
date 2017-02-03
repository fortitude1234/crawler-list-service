package com.dianping.ssp.crawler.list.po;

import java.util.Date;

/**
 *
 * @author Mr.Bian
 *
 */
public class ArticleOriginPO {
	private long id;
	private String title="";
	private String url="";
	private String thumbnail="";
	private String source="";
	private Date originalTime;
	private Date crawlTime;
	private String author="";
	private String contentUrl="";
	private int status;
	private String extraMessage="";
	private String domainTag="";
	private long similarId;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getOriginalTime() {
		return originalTime;
	}

	public void setOriginalTime(Date originalTime) {
		this.originalTime = originalTime;
	}

	public Date getCrawlTime() {
		return crawlTime;
	}

	public void setCrawlTime(Date crawlTime) {
		this.crawlTime = crawlTime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getExtraMessage() {
		return extraMessage;
	}

	public void setExtraMessage(String extraMessage) {
		this.extraMessage = extraMessage;
	}

	public String getDomainTag() {
		return domainTag;
	}

	public void setDomainTag(String domainTag) {
		this.domainTag = domainTag;
	}

	public long getSimilarId() {
		return similarId;
	}

	public void setSimilarId(long similarId) {
		this.similarId = similarId;
	}
	
}
