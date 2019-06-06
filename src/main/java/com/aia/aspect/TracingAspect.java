package com.aia.aspect;

//import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TracingAspect  extends CallTracker {

	public boolean isEnteringCalled() {
		return enteringCalled;
	}
	
//	private long start;
//	private long finish;
//	private long timeElapsed;

	Logger logger = LoggerFactory.getLogger(TracingAspect.class);

	boolean enteringCalled = false;

//	@Before("execution(void com.aia.Repository.WikipediaRepository.processWiki(..))")
//	public void entering(JoinPoint joinPoint) {
//		enteringCalled = true;
//		logger.trace("entering "
//				+ joinPoint.getStaticPart().getSignature().toString());
//		this.start = System.currentTimeMillis();
//	}
//
//	@After("execution(void com.aia.Repository.WikipediaRepository.processWiki(..))")
//	public void quitting(JoinPoint joinPoint) {
//		this.finish = System.currentTimeMillis();
//		this.timeElapsed = finish - start;
//		logger.trace("quitting "
//				+ joinPoint.getStaticPart().getSignature().toString() + "\n" +
//				"Elapsed time: " + this.timeElapsed / 1000 + " secs");
//	}

	@Around("SystemArchitecture.Repository() || SystemArchitecture.Service()")
	public void trace(ProceedingJoinPoint proceedingJP) throws Throwable {
		
		enteringCalled = true;
		
		String methodInformation = proceedingJP.getStaticPart().getSignature().toString();
		logger.trace("Entering " + methodInformation);
		trackCall();
		try {
			proceedingJP.proceed();
		} catch (Throwable ex) {
			logger.error("Exception in " + methodInformation, ex);
			throw ex;
		} finally {
			logger.trace("Exiting " + methodInformation);
		}
	}
}
