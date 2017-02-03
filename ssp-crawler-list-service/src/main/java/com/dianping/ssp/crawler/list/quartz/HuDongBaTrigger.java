package com.dianping.ssp.crawler.list.quartz;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.lion.client.Lion;
import com.dianping.ssp.crawler.common.log.CrawlerCommonLogEnum;
import com.dianping.ssp.crawler.list.api.UrlManagerService;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by iClaod on 10/19/16.
 */
@Component
public class HuDongBaTrigger extends AbstractTrigger {

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerCommonLogEnum.QUARTZ_TRIGGER.getValue());

    @Autowired
    private UrlManagerService urlManagerService;

    private List<String> keywordsList = Lists.newArrayList("餐饮", "美食", "食品", "食材", "饭店", "餐馆", "餐厅", "餐饮嘉宾", "餐饮大咖");

    private List<String> areaList = Lists.newArrayList("4a", "n3", "ca", "l5", "qa", "9p", "ya", "6e", "dt", "96", "3p", "ar", "bk", "5o", "j7", "fo", "eo", "r6", "nj", "7t", "x7", "v3", "zj", "ze", "m3", "8k", "x5", "lt", "cr", "ue", "a6", "0y");
    private static final int pageCount = 3;

    private static final String HUDONGBA_LION_SWITCH = "ssp-crawler-list-service.crawler.trigger.switch.hudongba";
    private static final String HUDONGBA_LIST_DOMAINTAG = "fairso-list";
    private static final String URL_PATTERN = "http://www.hdb.com/info_search?word=%s&page_num=%d&area_id=%s";

    @Override
    @Scheduled(cron="0 10 7 * * ?")
    public void run() {
        LOGGER.info("trigger job: " + HUDONGBA_LIST_DOMAINTAG);
        List<String> urls = Lists.newArrayList();

        if (Lion.getBooleanValue(HUDONGBA_LION_SWITCH, false) && CollectionUtils.isNotEmpty(keywordsList)) {
            for (int i = 1; i <= pageCount; i++) {
                for (String keyword : keywordsList) {
                    for (String area : areaList) {
                        try {
                            String encodeKeyword = URLEncoder.encode(keyword, "utf-8");
                            String url = String.format(URL_PATTERN, encodeKeyword, i, area);
                            urls.add(url);
                        } catch (Exception e) {
                            LOGGER.error("HuDongBaTrigger failed to parse argue: " + keyword, e);
                        }
                    }

                }
            }
        }
        if (CollectionUtils.isNotEmpty(urls)) {
            urlManagerService.addUrl2DomainTag(HUDONGBA_LIST_DOMAINTAG, urls);
            LOGGER.info("job: " + HUDONGBA_LIST_DOMAINTAG + " add urls count: " + urls.size());
        }
    }

    /**
     * area list
     阿拉善	no
     安顺	ut
     安阳	st
     阿里	br
     阿克苏	jr
     阿勒泰	mr
     阿拉尔	yr
     鞍山	fe
     安康	h3
     阿坝	qj
     安庆	h5
     北海	c6
     百色	i6
     包头	d6
     巴彦淖尔	bo
     毕节	5t
     保定	kt
     白沙县	go
     保亭县	ho
     博尔塔拉	3r
     巴音郭楞	6r
     白山	gk
     本溪	de
     滨州	83
     宝鸡	g3
     巴中	8j
     蚌埠	q5
     保山	wj
     白银	b7
     白城	mk
     北京	4a
     潮州	m7
     崇左	z6
     赤峰	w6
     承德	6t
     沧州	ot
     澄迈	qo
     昌江县	zo
     昌都	so
     昌吉	er
     常德	nk
     郴州	kk
     长春	8k
     常州	ke
     朝阳	1e
     长治	f3
     滁州	g5
     楚雄	fj
     巢湖	d5
     池州	w5
     成都	nj
     重庆	ya
     长沙	bk
     定西	p7
     儋州	jo
     东方	mo
     定安	yo
     大庆	4t
     大兴安岭	9t
     丹东	se
     德州	a3
     东营	53
     大同	93
     德阳	7j
     达州	lj
     德宏	u6
     迪庆	26
     大理	n6
     大连	ie
     东莞	g7
     鄂尔多斯	uo
     鄂州	lp
     恩施	gp
     防城港	g6
     佛山	h7
     抚州	qe
     阜新	he
     抚顺	9e
     阜阳	45
     福州	9p
     甘南	37
     桂林	86
     贵港	y6
     贵阳	x7
     固原	ko
     果洛州	hr
     赣州	ye
     高雄	5y
     广元	aj
     广安	pj
     甘孜	cj
     广州	j7
     河源	l7
     惠州	c7
     河池	16
     贺州	v6
     呼伦贝尔	s6
     淮安	4hb
     海口	eo
     邯郸	tt
     衡水	8t
     黑河	yt
     鹤岗	ct
     鹤壁	xt
     喀什	7r
     哈密	kr
     和田	rr
     黄冈	8p
     黄石	mp
     海东	gr
     海贝	zr
     黄南	ir
     海南	1r
     海西	9r
     衡阳	pk
     怀化	jk
     葫芦岛	b3
     菏泽	l3
     汉中	z3
     淮北	y5
     淮南	c5
     黄山	v5
     红河	sj
     亳州	s5
     湖州	76
     呼和浩特	96
     哈尔滨	lt
     杭州	a6
     合肥	l5
     酒泉	n7
     嘉兴	t6
     金华	j6
     揭阳	47
     江门	17
     佳木斯	gt
     鸡西	1t
     焦作	up
     济源	ep
     荆门	op
     荆州	4p
     吉林	lk
     江阴	je
     九江	oe
     吉安	ge
     锦州	u3
     济宁	63
     基隆	uy
     晋城	s3
     晋中	0j
     嘉义	ay
     嘉峪关	7
     金昌	u7
     景德镇	re
     济南	n3
     克拉玛依	5r
     克州	or
     昆明	zj
     开封	ffb
     陇南	k7
     临夏	e7
     丽水	o6
     柳州	l6
     来宾	h6
     六盘水	s7
     廊坊	rt
     临高	co
     乐东县	io
     陵水县	1o
     拉萨	fo
     洛阳	wt
     林芝	2r
     漯河	np
     龙岩	0k
     娄底	6k
     辽源	qk
     连云港	2e
     辽阳	we
     聊城	23
     临沂	33
     莱芜	o3
     吕梁	uj
     临汾	bj
     乐山	3j
     泸州	6j
     凉山	gj
     丽江	hj
     临沧	dj
     六安	95
     兰州	x5
     梅州	87
     茂名	d7
     牡丹江	ht
     绵阳	5j
     眉山	mj
     马鞍山	z5
     南宁	r6
     南沙群岛	do
     那曲	ur
     南阳	5p
     南平	dp
     宁德	uk
     南通	te
     南充	tj
     内江	ej
     怒江	b6
     南昌	6e
     南京	ue
     宁波	56
     平凉	t7
     平顶山	ft
     濮阳	bp
     莆田	fp
     萍乡	me
     盘锦	3
     攀枝花	rj
     庆阳	77
     衢州	e6
     清远	o7
     钦州	q6
     黔西南州	2t
     黔东南州	nt
     黔南州	at
     秦皇岛	et
     琼海	8o
     七台河	zt
     琼中县	vo
     潜江	ip
     泉州	sp
     曲靖	ij
     齐齐哈尔	mt
     青岛	yhb
     日照	e3
     日喀则	0r
     绍兴	k6
     韶关	r7
     汕头	y7
     汕尾	q7
     石嘴山	7o
     双鸭山	it
     绥化	vt
     石河子	tr
     三门峡	ap
     商丘	7p
     十堰	jp
     随州	cp
     神农架	hp
     三明	wp
     邵阳	3k
     松原	yk
     四平	4k
     宿迁	ne
     上饶	4e
     商洛	13
     朔州	w3
     遂宁	kj
     宿州	m5
     思茅	9j
     山南	xo
     三亚	3o
     上海	qa
     深圳	67
     沈阳	ze
     石家庄	7t
     苏州	3e
     台州	36
     通辽	x6
     铜仁	bt
     唐山	pt
     屯昌	4o
     塔城	lr
     图木舒克	4r
     天门	1p
     通化	ck
     泰州	7e
     铁岭	ve
     泰安	r3
     铜川	43
     台中	by
     台南	2y
     铜陵	15
     天水	27
     台北	0y
     吐鲁番	pr
     太原	v3
     天津	ca
     武威	57
     温州	66
     梧州	m6
     乌海	f6
     乌兰察布	0o
     吴忠	to
     五指山	oo
     文昌	ro
     万宁	lo
     乌鲁木齐	ar
     潍坊	p3
     威海	k3
     渭南	c3
     芜湖	i5
     文山	xj
     五家渠	qr
     无锡	ee
     武汉	3p
     锡林郭勒	2o
     兴安	ao
     邢台	3t
     西沙群岛	9o
     新乡	0p
     许昌	2p
     信阳	kp
     襄樊	6p
     孝感	rp
     西宁	cr
     咸宁	yp
     仙桃	zp
     湘潭	tk
     湘西	rk
     徐州	be
     新余	le
     咸阳	q3
     忻州	x3
     新竹	ny
     西双版纳	6
     宣城	f5
     厦门	vp
     西安	m3
     玉林	46
     云浮	97
     阳江	w7
     银川	5o
     伊春	qt
     伊犁哈萨克	8r
     宜昌	qp
     玉树	vr
     益阳	ak
     岳阳	5k
     永州	ek
     延边	zk
     盐城	ae
     扬州	5e
     鹰潭	8e
     宜春	ce
     营口	xe
     延安	y3
     榆林	i3
     阳泉	d3
     运城	2j
     宜宾	oj
     雅安	4j
     玉溪	1j
     烟台	t3
     张掖	a7
     舟山	p6
     珠海	z7
     中山	i7
     肇庆	v7
     湛江	f7
     遵义	0t
     中卫	po
     张家口	jt
     中沙群岛	wo
     周口	tp
     驻马店	pp
     漳州	xp
     张家界	2k
     株洲	7k
     镇江	pe
     淄博	73
     枣庄	j3
     自贡	jj
     资阳	yj
     昭通	vj
     郑州	dt
     */
}
