package com.aia.Wikipedia;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aia.Service.WikipediaService;

/**
 * Hello world!
 *
 */
public class App 
{
    @SuppressWarnings("resource")
	public static void main( String[] args )
    {
		
		ApplicationContext appContext = 
				new AnnotationConfigApplicationContext(AppConfig.class);

		WikipediaService service = 
				appContext.getBean("wikipediaService", WikipediaService.class);
		
		service.processWiki(false, 0); 
		System.out.printf("\nPages modified this year: %d\n\n", service.getModifiedThisYear());
		System.out.printf("Processed: %d\n", service.getProcessed());
		System.out.printf("Not found: %d\n", service.getNotFound());
		System.out.printf("Duplicates: %d\n", service.getDuplicates());
    }
}
