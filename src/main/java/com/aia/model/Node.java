package com.aia.model;

import java.time.LocalDateTime;

public class Node {
	/* Url from Wikipedia 
	 * 
	 * <link rel="canonical" href="https://en.wikipedia.org/wiki/Exclamation_mark"/>
	 * 
	 * */
	private String url;
	
	/* Name in the txt */
	private String name;
	
	/* Page title 
	 * 
	 * <title>Exclamation mark - Wikipedia</title>
	 * 
	 * */
	private String title;
	
	/* Pages last modified 
	 *
	 * <li id="footer-info-lastmod"> This page was last edited on 28 May 2019, at 16:23<span class="anonymous-show">&#160;(UTC)</span>.</li>
	 * 
	 * */
	private LocalDateTime lastModified;
	
	/* Getters and Setters */
	public LocalDateTime getLastModified() {
		return lastModified;
	}
	public String getName() {
		return name;
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}
	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
