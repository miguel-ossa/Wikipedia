package com.aia.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;

import com.aia.model.Node;

@Repository("wikipediaRepository")
public class WikipediaRepositoryImpl implements WikipediaRepository {

	// List of nodes that would be written to the final csv
	private List<Node> list = new ArrayList<Node>();
	// List of urls duplicated
	private List<Node> listDuplicates = new ArrayList<Node>();
	// List of nodes not found
	private List<Node> listNotFound = new ArrayList<Node>();
	
	// Sets of temporary urls and titles to detect duplicates
	private Set<String> setUrls = new HashSet<String>();
	private Set<String> setTitles = new HashSet<String>();
	
	private int notFound = 0;
	private int duplicates = 0; 
	private int processed = 0;
	private int modifiedThisYear = 0;
	private int process_limit = 0;
	private Boolean debug = false;
	private Boolean header = true;
	private List<String> formatStrings = Arrays.asList("dd MMM yyyy HH:mm", 
	           "d MMM yyyy HH:mm", 
	           "dd MMMM yyyy HH:mm", 
	           "d MMMM yyyy HH:mm", 
	           "dd MMMMM yyyy HH:mm", 
	   		   "d MMMMM yyyy HH:mm");
	private List<String> cleaningStrings = Arrays.asList("^\"|\"$", "", 
				"^\'|\'$", "");

	public int getNotFound() {
		return notFound;
	}
	public int getDuplicates() {
		return duplicates;
	}
	public int getProcessed() {
		return processed;
	}
	public int getModifiedThisYear() {
		return modifiedThisYear;
	}
	
	public int getProcess_limit() {
		return process_limit;
	}
	public void setProcess_limit(int process_limit) {
		this.process_limit = process_limit;
	}
	
	public void processWiki(Boolean debug, int process_limit) {
    	
		this.debug = debug;
		this.process_limit = process_limit;
		
    	/**
    	 * Read the file and store it in a set
    	 */
    	try (Stream<String> lines = Files.lines(Paths.get("src/main/Resources/first10000.txt"), Charset.defaultCharset())) {
    		  lines.forEachOrdered(line -> scrapeIt(line)); 
    	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
    	    	
    	createCSV();
    	createCSVDuplicates();
    	createCSVNotFound();
    }

	private void scrapeIt(String line) {
    	if (!this.header) {
	
    		// Load the page
    	    Document doc = tryToConnect(line);
    	    if (doc == null) {
    	    	if (this.debug) {
    	    		System.out.printf("   ***NOT FOUND |%s|\n", line);
    	    	}
    	    	notFound++;
           	    Node node = new Node();
        	    node.setName(line);
        	    node.setTitle(null);
        	    node.setUrl(null);
        	    node.setLastModified(null);
        	    this.listNotFound.add(node);
        	    this.notFound++;
        	    return;
    	    }

     	    String url = null;
     	    
    	    Elements links = doc.getElementsByTag("link");
    		for (Element link : links) {
    	    	if (link.attr("rel").equals("canonical")) {
    	    		url = link.attr("href");
    	    		break;
    	    	}
    	    } 
    		
    		// Check if title already exists
     	    Boolean newTitle = setTitles.add(doc.title());
    		Boolean newUrl = setUrls.add(url.toLowerCase());

    		if (!newTitle && !newUrl) {
    			if (this.debug) {
    				System.out.printf("   ***DUPLICATED |%s| with title |%s|\n", url, doc.title());
    			}
           	    Node node = new Node();
        	    node.setName(line);
        	    node.setTitle(doc.title());
        	    node.setUrl(url);
        	    node.setLastModified(null);
        	    this.listDuplicates.add(node);
        	    this.duplicates++;
    			return;
    		}
    		
    		// All seems to be OK, let's proceed with a valid node
       	    Node node = new Node();
    	    node.setName(line);
    	    node.setTitle(doc.title().replaceAll("\"",""));
    	    node.setUrl(url.replaceAll("\"","\\\""));
    	    
    	    // Cleanup the string which contains the last modified datetime
    	    String strDate = doc.getElementById("footer-info-lastmod").toString().replace("<li id=\"footer-info-lastmod\"> This page was last edited on ", "").replace("<span class=\"anonymous-show\">&nbsp;(UTC)</span>.</li>", "").replace(", at", "");
    	    // Parse and store the datetime
			node.setLastModified(tryToParse(strDate));
			if (node.getLastModified().isAfter(LocalDateTime.parse("2018-12-31T23:59:59")) && 
					node.getLastModified().isBefore(LocalDateTime.parse("2020-01-01T00:00:00"))) {
				this.modifiedThisYear++;
			}
    	    this.list.add(node);
    	    this.processed++;

    	    // Report if indicated
    	    if (this.debug) {
    	    	System.out.printf("Archived Word %s\n" +
    	    				  	  "         Title: |%s|\n" +
    	    				  	  "         Url: |%s|\n" +
    	    				  	  "         Date: |%s|\n", 
    	    				  	  node.getName(), 
    	    				  	  node.getTitle(),
    	    				  	  node.getUrl(),
    	    				  	  node.getLastModified().toString());
    	    }

    	    if (this.process_limit != 0) {
    	    	if (this.processed >= this.process_limit) {
    	    		return;
    	    	}
    	    }
    	}
    	else this.header = false;
    }
    
    private Document tryToConnect(String line) {
    	try {
    		return Jsoup.connect("https://en.wikipedia.org/wiki/" + line)
    				.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
    	             .maxBodySize(0)
    	             .timeout(1000*5)
    	             .get();
    	} catch (IOException e) {
    		for (String formatString : this.cleaningStrings) {
    			line = line.replaceAll(formatString, "");
    			try {
    				return Jsoup.connect("https://en.wikipedia.org/wiki/" + line)
    						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
    						.maxBodySize(0)
    						.timeout(1000*5)
    						.get();
    			} catch (IOException e1) {}
    		}
    	}
    	return null;
    }
    
    private LocalDateTime tryToParse(String strDate) {
        for (String formatString : this.formatStrings) {
        	try {
        		return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(formatString, Locale.ENGLISH));
        	} catch(DateTimeParseException ex) {}
        }
        return null;
    }

    private void createCSV() {
        try (PrintWriter writer = new PrintWriter(new File("src/main/Resources/result.csv"))) {

        	for(Node node : this.list){
	            StringBuilder sb = new StringBuilder();	 
	            
	            if (node.getUrl().contains(",") && 
	            	!node.getUrl().contains("\"")) {
	            	sb.append("\"" + node.getUrl() + "\",");
	            } else {
	            	sb.append(node.getUrl() + ",");
	            }
	            
	            if ((node.getName().contains(" ") ||
	            	node.getName().contains(",")) &&
	            	!node.getName().contains("\"")) {
	            	sb.append("\""+ node.getName() + "\",");
	            }
	            else {
	            	sb.append(node.getName() + ",");
	            }
	            
	            if ((node.getTitle().contains(" ") ||
	            	node.getTitle().contains(",")) &&
	             	!node.getTitle().contains("\""))
	            {
	            	sb.append("\"" + node.getTitle() + "\",");
	            } else {
	            	sb.append(node.getTitle() + ",");
	            }
	            
	            sb.append(node.getLastModified()+"\n");
	
	            writer.write(sb.toString());
        	}    

          } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
          }
    }
    private void createCSVDuplicates() {
        try (PrintWriter writer = new PrintWriter(new File("src/main/Resources/resultDuplicates.csv"))) {

        	for(Node node : this.listDuplicates){
	            StringBuilder sb = new StringBuilder();	 
	            
	            if (node.getUrl().contains(",")) {
	            	sb.append("\"" + node.getUrl() + "\",");
	            } else {
	            	sb.append(node.getUrl() + ",");
	            }
	            
	            if (node.getName().contains(" ") ||
		            	node.getName().contains(",")) {
		            	sb.append('"'+ node.getName() + "\",");
		        }
		        else if (node.getName().contains("\"")) {
		            	sb.append("'"+ node.getName() + "',");
		        }
		        else {
		        	sb.append(node.getName() + ",");
		        }
		   
	            if (node.getTitle().contains(" ") ||
		            	node.getTitle().contains(",")) {
		            	sb.append('"'+ node.getTitle() + "\",");
		        }
		        else if (node.getTitle().contains("\"")) {
		            	sb.append("'"+ node.getTitle() + "',");
		        } else {
		            	sb.append(node.getTitle() + ",");
		        }
		           
	            sb.append(node.getLastModified()+"\n");
	
	            writer.write(sb.toString());
        	}    

          } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
          }
    }
    
    private void createCSVNotFound() {
        try (PrintWriter writer = new PrintWriter(new File("src/main/Resources/resultNotFound.csv"))) {

        	for(Node node : this.listNotFound){
	            StringBuilder sb = new StringBuilder();	 
	            
	            sb.append("null,");

	            if (node.getName().contains(" ") ||
	            	node.getName().contains(",")) {
	            	sb.append('"'+ node.getName() + "\",");
	            }
	            else {
	            	sb.append(node.getName() + ",");
	            }
	            
	            sb.append("null,null\n");
	
	            writer.write(sb.toString());
        	}    

          } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
          }
    }
}



