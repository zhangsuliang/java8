package com.huofu.module.i5wei.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class SystemInit implements InitializingBean {
	
	private static final Log log = LogFactory.getLog(SystemInit.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.fatal("System start, initializing spring beans...");
	}

}
