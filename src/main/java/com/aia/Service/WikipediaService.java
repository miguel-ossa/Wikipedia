package com.aia.Service;

public interface WikipediaService {

	public void processWiki(Boolean debug, int min_processed) throws Exception;
	public int getProcessed();
	public int getNotFound();
	public int getDuplicates();
	public int getModifiedThisYear();
}
