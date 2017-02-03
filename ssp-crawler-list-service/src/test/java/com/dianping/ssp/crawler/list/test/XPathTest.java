package com.dianping.ssp.crawler.list.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import us.codecraft.xsoup.XElements;
import us.codecraft.xsoup.Xsoup;

/**
 *
 * @author Mr.Bian
 *
 */
public class XPathTest {

	@Test
	public void xpathTest() {
		String html = "<a class='a_d2b' href='http://www.ccas.com.cn/Article/List_141.html#'>设为首页</a>";
		Document document = Jsoup.parse(html);

		String result = Xsoup.compile("//a/@href").evaluate(document).get();
		System.out.println(result);
	}
}
