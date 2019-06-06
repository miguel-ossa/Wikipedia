package com.aia.Wikipedia;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.aia.Service.WikipediaService;
import com.aia.aspect.TracingAspect;
import com.aia.configuration.AspectConfiguration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AspectConfiguration.class)
public class AppTest 
    extends TestCase
{
	//@Autowired
	//TracingAspect tracingAspect;
	
	ApplicationContext appContext = 
			new AnnotationConfigApplicationContext(AppConfig.class);
	WikipediaService service = 
			appContext.getBean("wikipediaService", WikipediaService.class);
	TracingAspect tracingAspect = 
			appContext.getBean("tracingAspect", TracingAspect.class);

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
     * Test case for "processWiki".
     */
    public void testProcessWiki() throws Exception {
    	assertFalse(tracingAspect.isEnteringCalled());
    	service.processWiki(true, 10); // Debug and take a sample
		System.out.printf("\nPages modified this year: %d\n\n", service.getModifiedThisYear());
		System.out.printf("Processed: %d\n", service.getProcessed());
		System.out.printf("Not found: %d\n", service.getNotFound());
		System.out.printf("Duplicates: %d\n", service.getDuplicates());
    	assertTrue(service.getProcessed() != 0);
    	assertTrue(tracingAspect.isEnteringCalled());
    }
    

    
}
