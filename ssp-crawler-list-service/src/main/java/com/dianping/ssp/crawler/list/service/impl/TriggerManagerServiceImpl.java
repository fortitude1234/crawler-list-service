package com.dianping.ssp.crawler.list.service.impl;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.dianping.ssp.crawler.list.api.TriggerManagerService;
import com.dianping.ssp.crawler.list.quartz.AbstractTrigger;

/**
 *
 * @author Mr.Bian
 *
 */
@Service("triggerManagerService")
public class TriggerManagerServiceImpl implements TriggerManagerService,ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	@Override
	public void runTrigger(String triggerName,String methodName) {
		AbstractTrigger t=(AbstractTrigger)applicationContext.getBean(triggerName);
		if (StringUtils.isEmpty(methodName)){
			methodName="run";
		}
		Class clazz=t.getClass();
		Method m=null;
		try {
			m=clazz.getDeclaredMethod(methodName);
			m.invoke(t);
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.applicationContext=arg0;
	}

}
