package com.dianping.ssp.crawler.list.subprocessor;

import com.dianping.ssp.crawler.common.contants.CrawlerCommonConstants;
import com.dianping.ssp.crawler.common.contants.DuplicateType;
import com.dianping.ssp.crawler.common.entity.ProStatus;
import com.dianping.ssp.crawler.common.entity.ProcessorContext;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.CrawlerSubProcessTag;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.ICrawlerSubProcess;
import com.dianping.ssp.crawler.common.scheduler.RedisRepository;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by iClaod on 1/5/17.
 */
@CrawlerSubProcessTag(name = "58urlFilter")
public class UrlFilterFor58SubProcessor implements ICrawlerSubProcess {

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public ProStatus process(Page page) {
        ProcessorContext context = ProcessorContext.getContext(page);
        List<Request> requestsFromPage = (List<Request>) context.getParam(CrawlerCommonConstants.ProcessorContextConstant.TARGET_URL);
        Request parentRequest=page.getRequest();
        Integer duplicate=(Integer)parentRequest.getExtra("duplicate");
        String referer = page.getRequest().getUrl();
        if (CollectionUtils.isNotEmpty(requestsFromPage)) {
            for (Request requestFromPage : requestsFromPage) {
                String urlFromPage = requestFromPage.getUrl();
                if (StringUtils.isBlank(urlFromPage)) {
                    continue;
                }
                if (!isDuplicate(referer, urlFromPage)) {
                    requestFromPage.putExtra("referer", referer);
                    if (duplicate!=null && duplicate== DuplicateType.NEED_DUPLICATE){
                        requestFromPage.putExtra("duplicate", DuplicateType.NEED_DUPLICATE);
                    }
                    page.addTargetRequest(requestFromPage);
                }
            }
        }
        return ProStatus.success();
    }

    private boolean isDuplicate(String currentUrl, String urlFromPage) {
        String uniqueKey = get58UniqueKey(currentUrl, urlFromPage);
        if (uniqueKey == null) {
            return false;
        }
        return !redisRepository.addToUrlSet("58-uniqueKey", uniqueKey);
    }

    private static Set<String> GONGZHUANG_CHANNEL = Sets.newHashSet("cyzhuangxiu", "jiubazx", "kafeitzx", "chaguanzx");

    private static String get58UniqueKey(String currentUrl, String urlFromPage) {
        Pattern p = Pattern.compile(".*?58\\.com/([a-zA-Z]*?)/([\\d]*)x\\.shtml\\??.*");
        Matcher m = p.matcher(urlFromPage);
        if (m.find()) {
            String channel = m.group(1);
            String id = m.group(2);
            return channel + id;
        } else {
            Pattern p1 = Pattern.compile(".*?58\\.com/([a-zA-Z]*).*");
            Matcher m1 = p1.matcher(currentUrl);
            String channel = "gongzhuang";
            if (m1.find()) {
                String currentChannel = m1.group(1);
                if (!GONGZHUANG_CHANNEL.contains(currentChannel)) {
                    channel = currentChannel;
                }
            }
            Pattern p2 = Pattern.compile(".*?entinfo=([\\d]+).*");
            Matcher m2 = p2.matcher(urlFromPage);
            if (m2.find()) {
                return channel + m2.group(1);
            }
        }
        //TODO for future optimize, we can send request to get the http response of 302 with the real address
//        else {
//        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(get58UniqueKey("http://sh.58.com/cyzhuangxiu/?PGTID=0d30960f-0000-2c56-ecbf-283a8b4cb39f&ClickID=1",
                "http://sh.58.com/gongzhuang/25225100476201x.shtml"));
    }
}
