package com.dianping.ssp.crawler.list.base;


import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:config/spring/local/appcontext-*.xml",
        "classpath*:config/spring/common/appcontext-*.xml"})
@TransactionConfiguration(defaultRollback = true)
public abstract class AbstractTest {

    private Gson gson = new Gson();

    public void notNull(Object obj) {
        assertNotNull(obj);
    }

    public void isNull(Object obj) {
        assertNull(obj);
    }

    public void equal(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }

    protected void printToJson(Object o) {
        System.out.println(gson.toJson(o));
    }

}