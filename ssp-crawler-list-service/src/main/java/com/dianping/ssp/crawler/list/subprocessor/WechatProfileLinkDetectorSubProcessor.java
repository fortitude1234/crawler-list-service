package com.dianping.ssp.crawler.list.subprocessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.Selectable;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.article.utils.JsonUtil;
import com.dianping.ssp.crawler.common.entity.ProStatus;
import com.dianping.ssp.crawler.common.enums.ProMessageCode;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.CrawlerSubProcessTag;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.ICrawlerSubProcess;
import com.dianping.ssp.crawler.common.util.ImgUploadUtil;
import com.dianping.ssp.crawler.list.log.CrawlerListServiceLogEnum;
import com.google.common.collect.Lists;

/**
 * Created by iClaod on 11/25/16.
 */
@CrawlerSubProcessTag(name = "wechatProfileLinkDetector")
public class WechatProfileLinkDetectorSubProcessor implements ICrawlerSubProcess {
    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerListServiceLogEnum.WECHAT_LINK_DETECTOR.getValue());
    private static String RETRY_TIME="ssp-crawler-list-service.crawler.wechat.readcount.retry";
    @Override
    public ProStatus process(Page page) {
        if (page.getRequest().getUrl().matches("http://mp.weixin.qq.com/profile.*")) {
            String rawJson = null;
            Pattern p = Pattern.compile("msgList\\s=(.*);");
            Matcher m = p.matcher(page.getRawText());
            if (m.find()) {
                rawJson = m.group(1);
            }
            if (rawJson != null) {
            	try {
	                List<Request> requests = Lists.newArrayList();
	                Json jsonSoup = new Json(rawJson);
	                Selectable messageList = jsonSoup.jsonPath("$..app_msg_ext_info");
	                for (String message: messageList.all()) {
	                    Json messageSoup = new Json(message);
	                    String thumbnail = messageSoup.jsonPath("$.cover").get();
	                    String contentUrl = "http://mp.weixin.qq.com" + messageSoup.jsonPath("$.content_url").get();
	                    addRequest(requests, contentUrl, thumbnail);
	                    String isMulti = messageSoup.jsonPath("$.is_multi").get();
	                    if ("1".equals(isMulti)) {
	                        List<String> childList = messageSoup.jsonPath("$.multi_app_msg_item_list").all();
	                        if (childList != null)
	                            for (String  child: childList) {
	                                Json childSoup = new Json(child);
	                                String childThumbnail = childSoup.jsonPath("$.cover").get();
	                                String childContentUrl = "http://mp.weixin.qq.com" + childSoup.jsonPath("$.content_url").get();
	                                addRequest(requests, childContentUrl, childThumbnail);
	                            }
	
	                    }
	                }
	                for (Request request: requests) {
	                    page.addTargetRequest(request);
	                }
            	}catch (Exception e){
            		LOGGER.error("wechat profile handler error!",e);
            	}
            }else{
            	LOGGER.info("wechat can not get list need retry! rawJson=" + rawJson);
            	Request r = page.getRequest();
            	String referer=(String)r.getExtra("referer");
            	if (referer!=null ){
            		Integer retryTimes = (Integer) r.getExtra(Request.CYCLE_TRIED_TIMES);
    				if (retryTimes == null) {
    				    r.putExtra(Request.CYCLE_TRIED_TIMES, 1);
    				    page.addTargetRequest(r);
    				}else{
                        int maxRetryTime = Lion.getIntValue(RETRY_TIME,20) ;
    					if (retryTimes<maxRetryTime){
    						retryTimes++;
    						r.putExtra(Request.CYCLE_TRIED_TIMES, retryTimes);
    						page.addTargetRequest(r);
    					}else{
                            LOGGER.error("wechat read count retry request:"+r);
                        }
    				}
            	}
            }
        }
        return ProStatus.success();
    }

    private void addRequest(List<Request> list, String url, String thumbnail) {
        url = url.replaceAll("&amp;", "&");
        Request request = new Request(url);
        String dataType = "jpg";
        Pattern p = Pattern.compile("wx_fmt=([a-zA-Z]+)");
        Matcher m = p.matcher(thumbnail);
        if (m.find()) {
            dataType = m.group(1);
        }
        byte[] remoteFileStream = ImgUploadUtil.downloadImg(thumbnail);
        String newDataSrc = ImgUploadUtil.upload(remoteFileStream, "sixBing." + dataType);
        request.putExtra("thumbnail", newDataSrc);
        list.add(request);
    }

    public static void main(String[] args) {
        Page page = new Page();
        page.setRawText(content);
        Request request = new Request("http://mp.weixin.qq.com/profile?src=3&timestamp=1480067427&ver=1&signature=*7SJZvM2lJp1WURpKDOqCorp36txkWcsQkHin*Bs0ruoVJr-oStXrHX4St2R9aEXU9IevlnEqEdlDNrAG0todg==");
        page.setRequest(request);
        WechatProfileLinkDetectorSubProcessor subProcessor = new WechatProfileLinkDetectorSubProcessor();
        subProcessor.process(page);
        for (Request request1: page.getTargetRequests()) {
            System.out.println(request1.getUrl());
            System.out.println(request1.getExtra("thumbnail"));
        }
        System.out.println(JsonUtil.toJson(page.getTargetRequests()));
        System.out.println(page.getTargetRequests().size());
    }

    private static String content = "\n" +
            "<!DOCTYPE html>\n" +
            "<!--headTrap<body></body><head></head><html></html>--><html>\n" +
            "    <head>\n" +
            "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
            "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=0\" />\n" +
            "<meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n" +
            "<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\">\n" +
            "<meta name=\"format-detection\" content=\"telephone=no\">\n" +
            "\n" +
            "<script nonce=\"\" type=\"text/javascript\">\n" +
            "window.__nonce_str = \"\"\n" +
            "if (location.href.indexOf(\"safe=0\") == -1 && window.__nonce_str) {\n" +
            "\twindow.__moonsafe_csp_offset || (window.__moonsafe_csp_offset = 18);\n" +
            "\tdocument.write('<meta http-equiv=\"Content-Security-Policy\" content=\"script-src https: \\'unsafe-inline\\' \\'unsafe-eval\\' *.qq.com *.weishi.com'+(window.__nonce_str ? ' \\'nonce-' + window.__nonce_str + \"\\'\":\"\")+ '\">');\n" +
            "        \n" +
            "}\n" +
            "</script>\n" +
            "\n" +
            "        <script nonce=\"\" type=\"text/javascript\">\n" +
            "            window.logs = {\n" +
            "                pagetime: {}\n" +
            "            };\n" +
            "            window.logs.pagetime['html_begin'] = (+new Date());\n" +
            "        </script>\n" +
            "        \n" +
            "        <link rel=\"dns-prefetch\" href=\"//res.wx.qq.com\">\n" +
            "<link rel=\"dns-prefetch\" href=\"//mmbiz.qpic.cn\">\n" +
            "<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/images/icon/common/favicon22c41b.ico\">\n" +
            "<script nonce=\"\" type=\"text/javascript\">\n" +
            "    String.prototype.html = function(encode) {\n" +
            "        var replace =[\"&#39;\", \"'\", \"&quot;\", '\"', \"&nbsp;\", \" \", \"&gt;\", \">\", \"&lt;\", \"<\", \"&amp;\", \"&\", \"&yen;\", \"¥\"];\n" +
            "        if (encode) {\n" +
            "            replace.reverse();\n" +
            "        }\n" +
            "        for (var i=0,str=this;i< replace.length;i+= 2) {\n" +
            "             str=str.replace(new RegExp(replace[i],'g'),replace[i+1]);\n" +
            "        }\n" +
            "        return str;\n" +
            "    };\n" +
            "\n" +
            "    window.isInWeixinApp = function() {\n" +
            "        return /MicroMessenger/.test(navigator.userAgent);\n" +
            "    };\n" +
            "\n" +
            "    window.getQueryFromURL = function(url) {\n" +
            "        url = url || 'http://qq.com/s?a=b#rd'; \n" +
            "        var tmp = url.split('?'),\n" +
            "            query = (tmp[1] || \"\").split('#')[0].split('&'),\n" +
            "            params = {};\n" +
            "        for (var i=0; i<query.length; i++) {\n" +
            "            var arg = query[i].split('=');\n" +
            "            params[arg[0]] = arg[1];\n" +
            "        }\n" +
            "        if (params['pass_ticket']) {\n" +
            "        \tparams['pass_ticket'] = encodeURIComponent(params['pass_ticket'].html(false).html(false).replace(/\\s/g,\"+\"));\n" +
            "        }\n" +
            "        return params;\n" +
            "    };\n" +
            "\n" +
            "    (function() {\n" +
            "\t    var params = getQueryFromURL(location.href);\n" +
            "        window.uin = params['uin'] || '';\n" +
            "        window.key = params['key'] || '';\n" +
            "        window.wxtoken = params['wxtoken'] || '';\n" +
            "        window.pass_ticket = params['pass_ticket'] || '';\n" +
            "    })();\n" +
            "\n" +
            "</script>\n" +
            "\n" +
            "        <title>餐饮老板内参 </title>\n" +
            "        \n" +
            "<link rel=\"stylesheet\" href=\"https://res.wx.qq.com/open/libs/weui/0.2.0/weui.css\">  \n" +
            "<link rel=\"stylesheet\" href=\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/style/page/profile/sougou_profile306adb.css\">\n" +
            "<!--[if lt IE 9]>\n" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/style/page/profile/sougou_profile_pc2c57d0.css\"> \n" +
            "<![endif]-->\n" +
            "\n" +
            "    </head>\n" +
            "    <body id=\"\" class=\"zh_CN \" ontouchstart=\"\">\n" +
            "        \n" +
            "<div class=\"page_profile_info\">\n" +
            "    <div class=\"page_profile_info_inner\">\n" +
            "        <div class=\"profile_info_area\">\n" +
            "            <div class=\"profile_info_group\">\n" +
            "                <span class=\"radius_avatar profile_avatar\">\n" +
            "                                        <img src=\"http://wx.qlogo.cn/mmhead/Q3auHgzwzM4HeibPCmjqrzo8bhg7OtBPjzJuwwtzVPlpxSubBl45euw/0\">\n" +
            "                                    </span>\n" +
            "                <div class=\"profile_info\">\n" +
            "                    <strong class=\"profile_nickname\">\n" +
            "                      餐饮老板内参\n" +
            "                    </strong>\n" +
            "                                        <p class=\"profile_account\">微信号: cylbnc</p>\n" +
            "                                    </div>\n" +
            "            </div>\n" +
            "            <ul class=\"profile_desc\">\n" +
            "                <li>\n" +
            "                    <label class=\"profile_desc_label\" for=\"\">功能介绍</label>\n" +
            "                    <div class=\"profile_desc_value\" title=\"中国首席餐饮经管新媒体，最大餐饮产业链社交与服务平台。餐饮老板、创业者、供应链一手干货。连接跨界，一起重塑互联网+时代餐饮新生态。【北京瓦特新媒旗下品牌之一】\">中国首席餐饮经管新媒体，最大餐饮产业链社交与服务平台。餐饮老板、创业者、供应链一手干货。连接跨界，一起重塑互联网+时代餐饮新生态。【北京瓦特新媒旗下品牌之一】</div>\n" +
            "                </li>\n" +
            "                <li>\n" +
            "                    <label class=\"profile_desc_label\" for=\"\">帐号主体</label>\n" +
            "                    <div class=\"profile_desc_value\"><img class=\"icon_verify success\" src=\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/images/icon/common/icon_verify_success.2x2a26bd.png\">河南瓦特文化传播有限公司</div>\n" +
            "                </li>\n" +
            "            </ul>\n" +
            "            \n" +
            "            <div class=\"profile_opr\"  style=\"display:none\">\n" +
            "                            <a href=\"javascript:void(0);\" id=\"copyBt\" class=\"weui_btn weui_btn_plain_primary\">复制微信号</a>\n" +
            "                        </div>\n" +
            "                \n" +
            "        </div>\n" +
            "        <div class=\"weui_category_title\">最近10条群发</div>\n" +
            "        <div class=\"weui_msg_card_list\" id=\"history\">  \n" +
            "            \n" +
            "        </div>\n" +
            "        <div class=\"msg_card_tips\">仅显示最近10条群发</div>\n" +
            "\n" +
            "        <div class=\"loadmore\" style=\"display:none;\" id=\"js_loading\">\n" +
            "            <div class=\"tips_wrp\"><i class=\"icon_loading\"></i><span class=\"tips\">正在加载</span></div>\n" +
            "        </div>\n" +
            "        <div class=\"loadmore with_line\" style=\"display:none;\" id=\"js_nomore\">\n" +
            "            <div class=\"tips_wrp\"><span class=\"tips\">已无更多</span></div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <div id=\"js_pc_qr_code\" class=\"qr_code_pc_outer\">\n" +
            "        <div class=\"qr_code_pc_inner\">\n" +
            "            <div class=\"qr_code_pc\">\n" +
            "                <img id=\"js_pc_qr_code_img\" class=\"qr_code_pc_img\" src=\"/rr?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=7yKfBcWLpghblMmkvVI8wKbi9wntx6roezwBRiI4qAvpHVZYbnyN7yHxRzFdL4-sS9SwuT3g0qDshM9SS8edzRHOUUUnMEvUYQ6VdBqGX0U=\">\n" +
            "                <p>微信扫一扫<br>关注该公众号</p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>\n" +
            "\n" +
            "        \n" +
            "        <script nonce=\"\">\n" +
            "    var __DEBUGINFO = {\n" +
            "        debug_js : \"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_wap/debug/console2ca724.js\",\n" +
            "        safe_js : \"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_wap/safe/moonsafe2f3e84.js\",\n" +
            "        res_list: []\n" +
            "    };\n" +
            "</script>\n" +
            "\n" +
            "<script nonce=\"\">\n" +
            "(function() {\n" +
            "\tfunction _addVConsole(uri) {\n" +
            "\t\tvar url = '//res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/vconsole/' + uri;\n" +
            "\t\tdocument.write('<script nonce=\"\" type=\"text/javascript\" src=\"' + url + '\"><\\/script>');\n" +
            "\t}\n" +
            "\tif (\n" +
            "\t\t(document.cookie && document.cookie.indexOf('vconsole_open=1') > -1)\n" +
            "\t\t|| location.href.indexOf('vconsole=1') > -1\n" +
            "\t) {\n" +
            "\t\t_addVConsole('2.5.1/vconsole.min.js');\n" +
            "\t\t_addVConsole('plugin/vconsole-elements/1.0.2/vconsole-elements.min.js');\n" +
            "\t\t_addVConsole('plugin/vconsole-sources/1.0.1/vconsole-sources.min.js');\n" +
            "\t\t_addVConsole('plugin/vconsole-resources/1.0.0/vconsole-resources.min.js');\n" +
            "\t\t_addVConsole('plugin/vconsole-mpopt/1.0.0/vconsole-mpopt.js');\n" +
            "\t}\n" +
            "})();\n" +
            "</script>\n" +
            "        \n" +
            "        <script>window.__moon_host = 'res.wx.qq.com';window.moon_map = {\"biz_common/utils/respTypes.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/utils/respTypes2c57d0.js\",\"biz_common/utils/url/parse.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/utils/url/parse2fb01a.js\",\"biz_common/template-2.0.1-cmd.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/template-2.0.1-cmd275627.js\",\"biz_wap/jsapi/core.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_wap/jsapi/core2ffa93.js\",\"biz_common/dom/class.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/dom/class275627.js\",\"biz_common/utils/string/emoji.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/utils/string/emoji275627.js\",\"biz_wap/utils/ajax.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_wap/utils/ajax2f1747.js\",\"history/profile_history.html.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/history/profile_history.html306adb.js\",\"biz_common/utils/string/html.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/utils/string/html29f4e9.js\",\"history/template_helper.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/history/template_helper24f185.js\",\"appmsg/cdn_img_lib.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/appmsg/cdn_img_lib30b785.js\",\"biz_common/dom/event.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_common/dom/event275627.js\",\"history/profile_history.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/history/profile_history3209cb.js\",\"sougou/profile.js\":\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/sougou/profile31dd7f.js\"};</script><script type=\"text/javascript\">window.__wxgspeeds={}; window.__wxgspeeds.moonloadtime=+new Date()</script><script  type=\"text/javascript\" src=\"http://res.wx.qq.com/mmbizwap/zh_CN/htmledition/js/biz_wap/moon3165f7.js\"></script>\n" +
            "<script type=\"text/javascript\">\n" +
            "    document.domain=\"qq.com\";\n" +
            "    var biz = \"MjM5MjcwMjcwNA==\" || \"\";\n" +
            "    var src = \"3\" ; \n" +
            "    var ver = \"1\" ; \n" +
            "    var timestamp = \"1480066740\" ; \n" +
            "    var signature = \"FkQyMgKEPAFiXZlWD3aGCVWbLwoo9f*arwn9bcp2F7w87cbwzt*mVJcCyjH5d-s0oq4rWmSmhRRfj9n*jB4X-g==\" ; \n" +
            "    var name=\"cylbnc\"||\"餐饮老板内参\";\n" +
            "        var msgList = {\"list\":[{\"app_msg_ext_info\":{\"author\":\"曾莉\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGrqaHmQR*TsDgnF1GputoIF7yZkUSVzZvGLpmF4xTzqVZb3bilxMNW07iVaUBHji5k*F3MBuS2PIzRPa-*hqThg=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4DALnHkibDtsuliaBJdnbjib76LlhZ4eKiccnj9Dgc9fZF0QjchHfc1f1b1uc52PR6xnQTbKYaABicYticA/s640?\",\"digest\":\"想提高人效，看看日本餐企的思路\",\"fileid\":504822509,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGrqaHmQR*TsDgnF1GputoIHW1QwK6oTFyxcCpffnwVXXq11PUrtMafoDxCzCX89s*yqv*S5YrLy1AfJsWudpK1o=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4AylGP8VMScBybMQFbyib91V1NGBV8TGyDO05VzP9jrJnbrxEla3kcIiaggOh9ibKJzvtzvgsoRdBBicg/0?wx_fmt=png\",\"digest\":\"实现“美国梦”你必须知道的\",\"fileid\":504821790,\"source_url\":\"http://hd.hinabian.com/Activity_Adv/index/f0cddb59.html?cid=CYNC\",\"title\":\"川普移民政策不友好，餐饮移民有哪些机会？\"},{\"author\":\"\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGrqaHmQR*TsDgnF1GputoIEoPaaCmzBQYM8VOAWaNkqrMZSA1DbpvnNituuFEtDMxjXoQDLhOyfdI07fc203H8I=\",\"copyright_stat\":101,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4Cyuc9RZHz6gCTjF0pOzenztRX0pVnDSexc64H5PujIzJaFc2ORKic230rqicrj1zrwuNgw7ozIUF0Q/0?wx_fmt=png\",\"digest\":\"做品牌从来不是一件简单的事情，尤其对于初创公司，来学学20年公关老司机的方法。\",\"fileid\":504820254,\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50446&amp;pageid=105&amp;id=111\",\"title\":\"初创公司如何从0做品牌？20年公关老司机教你用好品牌壁垒的5把刀！\"}],\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50008&amp;pageid=105&amp;id=111\",\"subtype\":9,\"title\":\"小面店减员一半，人效提升50%，只因用对了日本餐企这5招！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1480029914,\"fakeid\":\"2392702704\",\"id\":1000000156,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"黄文潇\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGi9npO8S6ySX-32p4BdZu4JSUrbXtckJVHB*0qvz8boR88mBHxz4lKcC58nq*mKDeyJgRTZtNjmMbOUERsdFcMI=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4BEm7ViaT0SRUrowGr4KO18L8c8qdiaVTVstDJymbBOnxdhbp8HIRibwmZS34y3b88LnliatVlBllsWsA/s640?\",\"digest\":\"熊猫快餐其实是一家数据公司，本文内含大量超干货！\",\"fileid\":504822495,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGi9npO8S6ySX-32p4BdZu4LreYIqq5cYKueGoEFb0ipV-V5o0PafqOhz0uGDIcFdoQWIW7kxUrrAFPrCB5u-IKI=\",\"copyright_stat\":101,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4Cyuc9RZHz6gCTjF0pOzenztRX0pVnDSexc64H5PujIzJaFc2ORKic230rqicrj1zrwuNgw7ozIUF0Q/0?wx_fmt=png\",\"digest\":\"多品牌的魅力为何如此之大？背后究竟有着怎样的战略法则？\",\"fileid\":504820254,\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50493&amp;pageid=105&amp;id=111\",\"title\":\"开设16个品牌，400多家店，王品的品牌营销战略是怎么做的？\"},{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGi9npO8S6ySX-32p4BdZu4LrW9jGERxWbwU5hMY*DgBT-XCMivxzKbuhM0qA5QjpwX1S*vJuY3XCaeAr7GfnB1w=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4CPJoaLQRmwFcchZoJFGicibjLNO4HUibEsATWXH111ZwJicuJwbhc6bFsficLK6ZxnOd29QiczSlZuLLAg/0?wx_fmt=png\",\"digest\":\"美味不用等获得资本青睐第一靠创新，第二……\",\"fileid\":504821754,\"source_url\":\"\",\"title\":\"餐饮服务商仅此这一家，斩获16年度中国创客\"}],\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50493&amp;pageid=105&amp;id=111\",\"subtype\":9,\"title\":\"独家：全球最大中式快餐店，不用店长也能玩转库存！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479944037,\"fakeid\":\"2392702704\",\"id\":1000000155,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"王艳艳&nbsp;&nbsp;虎萌\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGhGJnTIPycb6wIg36OB6EPp9gLDqM9ZyaHmrRNzn6zGDwOpRymb2PWbqTN56f19gOCoqgbmpuX1jhSbFh2YQ2CM=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4BnabA2C7QicnDT2CGz8xZw0L41Gy2XenX78sNFKMlkia5gjvWibwtNPgWWevmItdstRzuzlnFtzVMcw/s640?\",\"digest\":\"招人难？留人难？麦当劳有办法！\",\"fileid\":504822478,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGhGJnTIPycb6wIg36OB6EPr2SX4dbItg8yZ7OaR*iNVig*NG6sDqWTyPNkeFrEYeP5QqM5aoL6EcA*-PItZROpE=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4BnabA2C7QicnDT2CGz8xZw0ztMdQ17YwWI72y5icfrjepIiaOMibuhl7QmRt799gagU1cLjBNRibWUuqA/s640?\",\"digest\":\"这是餐饮业第一个微信小程序，等你体验见证！\",\"fileid\":504822472,\"source_url\":\"http://xiquewo.com/pay/xiaochengxu?channel=cylbnc\",\"title\":\"餐饮业第一个小程序，帮你解决三个场景痛点！\"},{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGhGJnTIPycb6wIg36OB6EPrTLuLUAqsaWkCeFl*2dnop65mpB87vYV91AG77ABdjPn2tljCxfdJP1Yr78f4Ysf4=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4D6TbQpfOW2BG4KvsTsvXU4f1fEvdK4LQxqL5ZFcrtM1mMOuGgKSiaYnYJibnxlPrEGpfex6e17xdVg/0?wx_fmt=png\",\"digest\":\"你们在哪里餐饮业的的未来就在哪里。\",\"fileid\":504820267,\"source_url\":\"https://jinshuju.net/f/av6ulP\",\"title\":\"消费升级靠中产？你距餐饮新锐中产有多远？\"}],\"source_url\":\"\",\"subtype\":9,\"title\":\"留人是最好的招聘，这家餐企搞定90后员工就用这些招！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479857240,\"fakeid\":\"2392702704\",\"id\":1000000154,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"刘宇豪\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGnNThZtcMm*l3NZ5EFxBbzB7j6Iij6qt*L1i-nTxvO2T2-eG6K*bwkz7x-WtjWZA2bV4Bhpi*lgLYQBnGmfuLnk=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4B04Xs7O9dlPYq37W1aKZOj9fbXQBMlTLZ1xumfa2MkugibEEQuLSlYrBtwiczTdhafOZLicf5sveP1w/s640?\",\"digest\":\"那些年，这家连锁羊汤店老板收获的教训……\",\"fileid\":504822460,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"番书\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGnNThZtcMm*l3NZ5EFxBbzBS5QtZFhFpfJeia6wuZh-i3DPEu7VtJhJCkWoWYZqWNFIKqhqD38ybefTIuoEC8cM=\",\"copyright_stat\":101,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4Cyuc9RZHz6gCTjF0pOzenztRX0pVnDSexc64H5PujIzJaFc2ORKic230rqicrj1zrwuNgw7ozIUF0Q/0?wx_fmt=png\",\"digest\":\"11月30日，小灶课堂“餐饮品牌特训营”开营，手把手教你从零打造强势品牌！\",\"fileid\":504820254,\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50008&amp;pageid=105&amp;id=111\",\"title\":\"三位资深品牌操盘手告诉你：什么是留住顾客的终极武器！\"},{\"author\":\"羽诺\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGnNThZtcMm*l3NZ5EFxBbzC9jsqnV4oJgTyS*OnWSpjUWE7y7Dr2NaTYVG-jwigH*tx*v-RNchAJ94UdSVX-h3o=\",\"copyright_stat\":101,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_jpg/2ibmtGl5Ra4B04Xs7O9dlPYq37W1aKZOjzpg1Wur1v74a0Z2icGYqtzmkSFq4ryy8y8Gkk6I64pcpt9eZMRwjicYA/0?wx_fmt=jpeg\",\"digest\":\"戳我领取奖学金！\",\"fileid\":504822468,\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50008&amp;pageid=105&amp;id=111\",\"title\":\"课代表你好，请速来领取你的奖学金！\"}],\"source_url\":\"\",\"subtype\":9,\"title\":\"几次净身出户，现在80多家小店年营收1亿，我经历了什么？\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479771892,\"fakeid\":\"2392702704\",\"id\":1000000153,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"段明珠&nbsp;刘宇豪\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGkML9zBpJkHypphP72HWqzFSe2VLB4*c9s-4IZgrQcVH9RTYv67pg3S1PiauVW*vozJk1rLsx01HkWtZXeseNRI=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4BjaFNHicTIzo1IvB5mOxFv6iav1TmTu32nHoMWM5AnVCPZeVDiahQicWTbOfxwMyaynRriabgXfIgzpFA/s640?\",\"digest\":\"进购物中心真的必有一死？\",\"fileid\":504822440,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"闫太然\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGkML9zBpJkHypphP72HWqzHDMLsUqDvk0aGEqaPcFsFDSRpPZe-Pbl8EzgrRdNf6tQDbzvSyA-GlZSU7sT9E3cw=\",\"copyright_stat\":101,\"cover\":\"http://mmbiz.qpic.cn/mmbiz/2ibmtGl5Ra4CCwlGjpDAZ3gVSIby6hrsc9xefcrwk2I4uV36TNhUicdkIib6FuosNamSWxyBuW6QBtXFice6azrLlw/0?wx_fmt=png\",\"digest\":\"标准化服务时代的终结来临了\",\"fileid\":504819677,\"source_url\":\"\",\"title\":\"失控的“中杯”，星巴克们到底错在哪？\"},{\"author\":\"于德浩\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGkML9zBpJkHypphP72HWqzECB5B7apOW-sVBrPAj8kCTJX*1AfAsDFYcy-HbOYNi-l*4vgKeM6czirUlWsSKHwc=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4CkI9TVpDaDUNgHic87SOLudfQSbxr77eiaYu6h4WDV3Kb0KxfTKPYmc9hIGT1hHAImydQEc0aoic9lA/s640?\",\"digest\":\"一家由艺术家“把关”的甜品店！\",\"fileid\":504821950,\"source_url\":\"\",\"title\":\"这儿有一家深藏在写字楼里的奇葩甜品店……\"},{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGkML9zBpJkHypphP72HWqzE9Fio95pei-JGPK*zIdytdOoC8aaarbpeJIvCrZ8Gw14ac1jcHSzXYSyQW1rtnYVQ=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4CPJoaLQRmwFcchZoJFGicibjLNO4HUibEsATWXH111ZwJicuJwbhc6bFsficLK6ZxnOd29QiczSlZuLLAg/0?wx_fmt=png\",\"digest\":\"餐饮效率革新的前沿阵地在这里！\",\"fileid\":504821754,\"source_url\":\"\",\"title\":\"美团点评智能POS机亮相央视新闻联播，凭啥？\"}],\"source_url\":\"\",\"subtype\":9,\"title\":\"购物中心到底进是不进？看完这篇你就全明白了！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479685074,\"fakeid\":\"2392702704\",\"id\":1000000152,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGlvEm-FeyK1sQiOL*o7nv-629O-Om0cGTAv5*hu7nB9iDdaoyJaym0WwOlThtTTL3kx8o*wqtiKB8iJPEGeuVGU=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4AibLVdsUf8V2ztS9b9I963rr3hQaWmHl3dFBAl50XDKicxncISR2AZicfIBqf00aRCdrAmUKu04qFqA/s640?\",\"digest\":\"用这三个好办法，不怕顾客不上门！\",\"fileid\":504822422,\"is_multi\":0,\"multi_app_msg_item_list\":[],\"source_url\":\"\",\"subtype\":9,\"title\":\"送你三条中国餐饮人很少想得到的店面运营思路！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479597800,\"fakeid\":\"2392702704\",\"id\":1000000151,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"关雪菁\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGsq89g38XJDaYQBnkZIY6hc6CyVJBPYK0xDtMpxWUP0auF3skZrSg4XocOAFKQtwOYKATpKXIVyQOuoHDwYen1M=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4Bsu0CAjFA8Xfelh0M0Nyq2sdWI5C9UdicAQQZMRseZ0Ytntuot2P9LiblnJibHxW4LibSryiaQaWiaNfqA/s640?\",\"digest\":\"它也是为数不多被星巴克CEO舒尔茨看在眼里的餐企！它究竟是谁？\",\"fileid\":504822398,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"巴九灵\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGsq89g38XJDaYQBnkZIY6hf9CA-CIroI-x7ydgIRj0bcLp8N3EXCHMKSYyq4UTTMY5zWU4uudsvwhZxTTWKjU4g=\",\"copyright_stat\":101,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4A6yqRDa5bIkibS97pKGylBLecibhENL46vx72ENfVSv7PVrcYTdQRk5ghz6NVAnc5OAdpOIUia6zfuA/0?wx_fmt=png\",\"digest\":\"无论身处哪个行业，行业大事和行业大势都是必修课。\",\"fileid\":504821847,\"source_url\":\"\",\"title\":\"预见2017，餐饮人加持自己就在这个机会！\"}],\"source_url\":\"\",\"subtype\":9,\"title\":\"海底捞都想成为这家餐企，看看创始人就知道它为啥牛逼……\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479511715,\"fakeid\":\"2392702704\",\"id\":1000000150,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGqg96oxqZgFmVdBHHMxHZvzb0wpk5FsxceDcBA3DzvgtYzch*B2EaNbSSzjvz*3qmjyfLRdxc67OrnjlOU9fyo4=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4AEWiaIgrBMxicO7r9SXiaZcd8pib6Xj6Mh2spQYbJbUibd7GoNWGaJPwoiaJvZibdcNXXeJa9zC9Fic5AjVQ/s640?\",\"digest\":\"餐企的未来，是扁平的，是属于项目经理的！\",\"fileid\":504822383,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGqg96oxqZgFmVdBHHMxHZvz34jtkLirIu38T7OmJEFoINFZb3WkRPMP2oop2C0dd7gn0AzcT0nhBbdcpsK8UmeQ=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4AylGP8VMScBybMQFbyib91V1NGBV8TGyDO05VzP9jrJnbrxEla3kcIiaggOh9ibKJzvtzvgsoRdBBicg/0?wx_fmt=png\",\"digest\":\"餐饮老板内参教你如何在海外开个中餐馆\",\"fileid\":504821790,\"source_url\":\"http://xiu.hinabian.com/v-U711FE7AE8?eqrcode=1&amp;from=singlemessage&amp;isappinstalled=0\",\"title\":\"如何轻松去国外开一家中餐馆？\"},{\"author\":\"王新磊\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGqg96oxqZgFmVdBHHMxHZvwey6llwxMsP1v1p*HvfPAcdFb67U9aZ*0acSomadHrkBvdeNwzSQ58GbckiEO8H9M=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4CkI9TVpDaDUNgHic87SOLudfQSbxr77eiaYu6h4WDV3Kb0KxfTKPYmc9hIGT1hHAImydQEc0aoic9lA/s640?\",\"digest\":\"大多数做小面的，都是战斗力不足的渣儿。\",\"fileid\":504821950,\"source_url\":\"\",\"title\":\"只有10家店，就被资本凶猛追捧，这家餐企底气何来？\"}],\"source_url\":\"http://www.xiaozaoketang.com/?fromUser=50014&amp;pageid=105&amp;id=104\",\"subtype\":9,\"title\":\"这个品类冠军两年培养了50个一流店长，只因做对了这3件事！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479425556,\"fakeid\":\"2392702704\",\"id\":1000000149,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"王艳艳\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGtaGgVgbzppObx0s*YHCGGejAA2PAVS5wuKd8Fv*4qNQyMZ6sdvUw4lotz-xfQlTUocPTCMsagA2HxRKSMhjWeI=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4DfhIFBXVibZZJVbgTD3k9TeJBgqr2OicfU907ItjJmznOY5xNGJmkYkiaic8RLQV20xPgfao5UXgNHGA/s640?\",\"digest\":\"留不住人，你可能错在了招聘环节！\",\"fileid\":504822359,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"内参FM\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGtaGgVgbzppObx0s*YHCGGfuerNIkHVvJI3h-BWxZQjQ6*aqk802cn602ncB9cRXNW5hbURCgj3F3CqhTtShShM=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz/2ibmtGl5Ra4C1SDAYAOxWCE8LjZN0aM2BbBdmZ46h2PgO4cx4LgWTgLcFibicXSp3kfEib6mf5J3fjXI3CEg6VicBlA/0?wx_fmt=png\",\"digest\":\"麦当劳这样占领儿童的心智\",\"fileid\":504819620,\"source_url\":\"http://www.ximalaya.com/41280293/album/3585110?order=desc#rd\",\"title\":\"多年深耕一件事，它就牢牢抓住了儿童餐饮市场！\"},{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGtaGgVgbzppObx0s*YHCGGdkBWMoeSbk3-oGkrVZq3*jWjwC13w4u*AJTPvsEA3TkhyZIqUmYAO-7aaMs6bMArc=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz/2ibmtGl5Ra4ACAL0vUPzLxBicsoatLx70ibkbS6biaIVk6eWlap7PLMFMUZaDrcw5J4hWsODic1NN1kM7q9QahS50sg/0?wx_fmt=png\",\"digest\":\"点&nbsp;&nbsp;击&nbsp;&nbsp;上&nbsp;&nbsp;面&nbsp;&nbsp;蓝&nbsp;&nbsp;字&nbsp;&nbsp;关&nbsp;&nbsp;注&nbsp;&nbsp;我&nbsp;&nbsp;们第&nbsp;1113&nbsp;期▼\",\"fileid\":504819768,\"source_url\":\"\",\"title\":\"做为全球最大广告主，可口可乐竟然要放弃传统广告代理商……\"}],\"source_url\":\"\",\"subtype\":9,\"title\":\"星座、血型、笔迹、QQ等级？这样招人，离职率降一半！\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479339098,\"fakeid\":\"2392702704\",\"id\":1000000148,\"status\":2,\"type\":49}},{\"app_msg_ext_info\":{\"author\":\"秦朝\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGskdJvthlBb8uDXcqRzAkdhiT8-mcaL7oDs0NPCgz1Vk6d5sqUAF6CFtYurOWsNtdLR4089GZL3Ll0FP3uegzAU=\",\"copyright_stat\":11,\"cover\":\"http://mmbiz.qpic.cn/mmbiz_png/2ibmtGl5Ra4B3zCz4n5V7cecZVEnHqqgrtoYPePh8XAwA0akew8dcNOQbdwMeY1JuaAqQa5YtzbBxYcQ7rDmvAw/s640?\",\"digest\":\"线上仍是巨大增量市场，可你真的玩对了么？\",\"fileid\":504822303,\"is_multi\":1,\"multi_app_msg_item_list\":[{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGskdJvthlBb8uDXcqRzAkdihe0zJlyA-Rcr8FgHEcHHz19JA-t5RDlZcVt2zS5kClzdjq8tfuBLv2x53eCMztgk=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz/2ibmtGl5Ra4ACAL0vUPzLxBicsoatLx70ibscGrYib5iajQDGgbSlic6W95IKMRRic8znkgbNslm9dK2F1Eaa8ibHy2GtA/0?wx_fmt=png\",\"digest\":\"支付的便捷性，决定餐厅运营效率！\",\"fileid\":504819774,\"source_url\":\"\",\"title\":\"运营效率提升新杀器：餐厅买单“秒付”时代来啦！\"},{\"author\":\"内参君\",\"content\":\"\",\"content_url\":\"/s?timestamp=1480066742&amp;src=3&amp;ver=1&amp;signature=6o5XPsv4hRdQEg19FogQSg*GTeOLY3VFvrF9T1d1BLFOlm2ZSGWi9qWS5dghfEL4FJPYtxazEnYmnfyBBfCEGskdJvthlBb8uDXcqRzAkdjNQCrvBVxdYmsThCNGkU-PxmsXhoE1MVdmjoQ*xVFln6k7CPERkimNncUxX-2CHWM=\",\"copyright_stat\":100,\"cover\":\"http://mmbiz.qpic.cn/mmbiz/2ibmtGl5Ra4ACAL0vUPzLxBicsoatLx70ibkbS6biaIVk6eWlap7PLMFMUZaDrcw5J4hWsODic1NN1kM7q9QahS50sg/0?wx_fmt=png\",\"digest\":\"明星开了餐厅，来了好多人。然后就没有然后了……\",\"fileid\":504819768,\"source_url\":\"\",\"title\":\"韩寒餐厅接连关店，用明星聚客怎么总是不长久？\"}],\"source_url\":\"\",\"subtype\":9,\"title\":\"餐厅外卖占比超过70%，我是在给美团打工么？\"},\"comm_msg_info\":{\"content\":\"\",\"datetime\":1479252231,\"fakeid\":\"2392702704\",\"id\":1000000147,\"status\":2,\"type\":49}}]};\n" +
            "        seajs.use(\"sougou/profile.js\");\n" +
            "</script>\n" +
            "\n" +
            "    </body>\n" +
            "</html>\n" +
            "<!--tailTrap<body></body><head></head><html></html>-->\n";
}
