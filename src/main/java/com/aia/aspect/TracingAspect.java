package com.aia.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TracingAspect {

	public boolean isEnteringCalled() {
		return enteringCalled;
	}
	
	private long start;
	private long finish;
	private long timeElapsed;

	Logger logger = LoggerFactory.getLogger(TracingAspect.class);

	boolean enteringCalled = false;

	@Before("execution(void com.aia.Repository.WikipediaRepository.processWiki(..))")
	public void entering(JoinPoint joinPoint) {
		enteringCalled = true;
		logger.trace("entering "
				+ joinPoint.getStaticPart().getSignature().toString());
		this.start = System.currentTimeMillis();
	}

	@After("execution(void com.aia.Repository.WikipediaRepository.processWiki(..))")
	public void quitting(JoinPoint joinPoint) {
		this.finish = System.currentTimeMillis();
		this.timeElapsed = finish - start;
		logger.trace("quitting "
				+ joinPoint.getStaticPart().getSignature().toString() + "\n" +
				"Elapsed time: " + this.timeElapsed / 1000 + " secs");
	}

}
