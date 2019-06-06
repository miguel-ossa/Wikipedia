package com.aia.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aia.Repository.WikipediaRepository;

@Service("wikipediaService")
public class WikipediaServiceImpl implements WikipediaService {

	private WikipediaRepository wikipediaRepository;

	public WikipediaServiceImpl() {
		
	}
	
	public WikipediaServiceImpl(WikipediaRepository wikipediaRepository) {
//		System.out.println("We are using constructor injection");
		this.wikipediaRepository = wikipediaRepository;
	}
	
	@Autowired
	public void setWikipediaRepository(WikipediaRepository wikipediaRepository) {
//		System.out.println("We are using setter injection");
		this.wikipediaRepository = wikipediaRepository;
	}

	public void processWiki(Boolean debug, int min_processed)  throws Exception {
		
		wikipediaRepository.processWiki(debug, min_processed);
		
	}
	
	public int getNotFound() {
		
		return wikipediaRepository.getNotFound();
		
	}
	
	public int getDuplicates() {
	
		return wikipediaRepository.getDuplicates();

	}
	
	public int getProcessed( ) {
		
		return wikipediaRepository.getProcessed();
		
	}
	
	public int getModifiedThisYear() {
		
		return wikipediaRepository.getModifiedThisYear();
		
	}

}
