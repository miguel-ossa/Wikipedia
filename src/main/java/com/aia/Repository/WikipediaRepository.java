package com.aia.Repository;

public interface WikipediaRepository {

	public void processWiki(Boolean debug, int min_processed);
	public int getProcessed();
	public int getNotFound();
	public int getDuplicates();
	public int getModifiedThisYear();
}
