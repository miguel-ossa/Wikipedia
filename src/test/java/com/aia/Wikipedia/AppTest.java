package com.aia.Wikipedia;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aia.Service.WikipediaService;
import com.aia.Wikipedia.AppConfig;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	ApplicationContext appContext = 
			new AnnotationConfigApplicationContext(AppConfig.class);
	WikipediaService service = 
			appContext.getBean("wikipediaService", WikipediaService.class);
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName ) 
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    
    /**
     * First approach to the problem
     */
    public void testProcessWiki() {
    	service.processWiki(true, 0);
		System.out.printf("\nPages modified this year: %d\n\n", service.getModifiedThisYear());
		System.out.printf("Processed: %d\n", service.getProcessed());
		System.out.printf("Not found: %d\n", service.getNotFound());
		System.out.printf("Duplicates: %d\n", service.getDuplicates());
    	assertTrue(service.getProcessed() != 0);
    }
    

    
}
