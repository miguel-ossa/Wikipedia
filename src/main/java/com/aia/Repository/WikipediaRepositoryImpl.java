package com.aia.Repository;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Year;
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

	public static final String FILES_FOLDER = "src/main/files/";
	
	// List of nodes that would be written to the final csv
	private List<Node> list = new ArrayList<Node>();
	// List of duplicated urls 
	private List<Node> listDuplicates = new ArrayList<Node>();
	// List of nodes which cannot be found
	private List<Node> listNotFound = new ArrayList<Node>();
	
	// Sets of temporary urls and titles for detecting duplicates
	private Set<String> setUrls = new HashSet<String>();
	private Set<String> setTitles = new HashSet<String>();
	
	// Counters
	private int notFound = 0;
	private int duplicates = 0; 
	private int processed = 0;
	private int modifiedThisYear = 0;
	private int minProcessed = 0;
	
	// Indicators
	private Boolean debug = false;
	private Boolean header = true;
	private Boolean cancelProcess = false;
	
	// Formats
	private List<String> formatStrings = Arrays.asList("dd MMM yyyy HH:mm", 
	           "d MMM yyyy HH:mm", 
	           "dd MMMM yyyy HH:mm", 
	           "d MMMM yyyy HH:mm", 
	           "dd MMMMM yyyy HH:mm", 
	   		   "d MMMMM yyyy HH:mm");
	private List<String> cleaningStrings = Arrays.asList("", "^\"|\"$", "^\'|\'$");

	// Getters and Setters
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
	
	public int getMinProcessed() {
		return minProcessed;
	}
	public void setMinProcessed(int minProcessed) {
		this.minProcessed = minProcessed;
	}
	
	/**
	 * processWiki
	 * ===========
	 * 
	 * Processes a list of keywords from the file "firs10000.txt" and 
	 * scrapes in Wikipedia for its absolute urls builded in the form:
	 * 
	 * https://en.wikipedia.org/wiki/[keyword]
	 * 
	 * Reports found and inexistent pages in different CSV files, and
	 * also generates a separate CSV to report urls and titles (both)
	 * duplicates.
	 * 
	 * Other getters:
	 * 
	 *   getProcessed: the number of resolved urls included in "Processed.csv"
	 *   getNotFound: the number of unresolved urls included in "NotFound.csv"
	 *   getDuplicates: the number of duplicated urls included in "Duplicates.csv"
	 *   getModifiedThisYear: number of pages modified this year
	 *   
	 *
	 * @param  debug	a Boolean value to activate outputs during execution
	 * @param  min_processed	minimum found sites found
	 * @return      nothing
	 * @throws Exception 
	 */
	public void processWiki(Boolean debug, int minProcessed) throws Exception {
    	
		this.debug = debug;
		this.minProcessed = minProcessed;
		
    	/**
    	 * Read the file and process it
    	 */
    	Stream<String> lines = Files.lines(Paths.get(FILES_FOLDER + "first10000.txt"), Charset.forName("UTF-8"));
  		lines.forEachOrdered(line -> scrapeIt(line)); 
  		lines.close();

    	if (this.debug) {
    		System.out.println("Generating files. Please, wait...\n");
    	}
    	createCSV();
    	createCSVDuplicates();
    	createCSVNotFound();
    }

	/**
	 * scrapeIt
	 * ========
	 * 
	 * For "processWiki" internal use. This function collect data
	 * from the Wikipedia, and generates the final CSV, doing the
	 * most part of the job.
	 * 
	 * @param  line	keyword to be processed
	 * @return      nothing
	 */
	private void scrapeIt(String line) {
		
		// Process was requested to be cancelled
		if (this.cancelProcess) return;
		
		// Is this the first line? Ignore it
		if (this.header) {
			this.header = false;
			return;
		}
	
	    
	    // Load the page
	    Document doc = tryToConnect(line);
		// Enclose the keyword with quotes to avoid possible errors in the CSV
	    line = "\"" + line.replaceAll("\"","%22") + "\"";
	    if (doc == null) {
	    	if (this.debug) {
	    		System.out.printf("   ***NOT FOUND %s\n", line);
	    	}
	        Node node = new Node();
	        node.setName(line);
	        this.listNotFound.add(node);
	        this.notFound++;
	        return;
	    }
	
	    // Get the canonical url
	    String url = null;	    
	    Elements links = doc.getElementsByTag("link");
	    for (Element link : links) {
	    	if (link.attr("rel").equals("canonical")) {
	    		url = "\"" + link.attr("href").replaceAll("\"","%22") + "\"";
	    		break;
	    	}
	    } 
	    
	    // Get the title
	    String title = "\"" + doc.title().replaceAll("\"","%22") + "\"";
	    
	    // Cleanup the string which contains the last modified datetime
	    String strDate = doc.getElementById("footer-info-lastmod").toString().replace("<li id=\"footer-info-lastmod\"> This page was last edited on ", "").replace("<span class=\"anonymous-show\">&nbsp;(UTC)</span>.</li>", "").replace(", at", "");
	
	    // Check for duplicates
	    Boolean newTitle = setTitles.add(title);
	    Boolean newUrl = setUrls.add(url.toLowerCase());
	    if (!newTitle && !newUrl) {
	    	if (this.debug) {
	    		System.out.printf("   ***DUPLICATED %s\n", line);
	    	}
	        Node node = new Node();
	        node.setName(line);
	        node.setTitle(title);
	        node.setUrl(url);
	        node.setLastModified(tryToParse(strDate));
	        this.listDuplicates.add(node);
	        this.duplicates++;
	    	return;
	    }
	    
	    // All seems to be OK, let's proceed with a valid node
	    Node node = new Node();
	    node.setName(line);
	    node.setTitle(title);
	    node.setUrl(url);
	
	    // Parse and store the datetime
		node.setLastModified(tryToParse(strDate));
		
		// Check if the page was modified during this year
		if (node.getLastModified().isAfter(LocalDateTime.parse((Year.now().getValue() - 1) + "-12-31T23:59:59")) && 
			node.getLastModified().isBefore(LocalDateTime.parse((Year.now().getValue() + 1) + "-01-01T00:00:00"))) {
			this.modifiedThisYear++;
		}
		
		// Collect the node
    	this.list.add(node);
    	
    	this.processed++;

    	// Report if indicated
    	if (this.debug) {
    		System.out.printf("Archived %s\n", line);
    	}

    	// Check if we should interrupt the process
    	if (this.minProcessed != 0) {
    		if (this.processed >= this.minProcessed)  {
    			this.cancelProcess = true;
    		}
    	}
    }
    
	/**
	 * tryToConnect
	 * ============
	 * 
	 * For "scrapeIt" internal use. This function scrapes the Wikipedia,
	 * using the form:
	 * 
	 * https://en.wikipedia.org/wiki/[keyword]
	 * 
	 * @param  line	keyword to be processed
	 * @return      nothing
	 */
    private Document tryToConnect(String line) {
    	int tries = 0;

    	for (String formatString : this.cleaningStrings) {
    		try {
    			return Jsoup.connect("https://en.wikipedia.org/wiki/" + line.replaceAll(formatString, ""))
    						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
    						.maxBodySize(0)
    						.timeout(1000*5)
    						.get();
    		} catch (IOException ex) {
        		if (this.debug) {
        			System.out.println("Tries: " + ++tries);
        		}
    		}
    	}

    	return null;
    }
    
	/**
	 * Parse the datetime found in the Wikipedia. It is used
	 * by the function "scrapeIt".
	 * 
	 * @param  strDate	string with the date to be parsed
	 * @return	LocalDateTime	the date parsed
	 */
    private LocalDateTime tryToParse(String strDate) {
        for (String formatString : this.formatStrings) {
        	try {
        		return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(formatString, Locale.ENGLISH));
        	} catch(DateTimeParseException ex) {}
        }
        return null;
    }

	/**
	 * createCSV
	 * =========
	 * 
	 * Create the final CSV with the data collected from the Wiki.
	 * Used by the function "processWiki".
	 * 
	 * @param  none
	 * @return	nothing
	 */
    private void createCSV() throws Exception {
    	PrintWriter writer = new PrintWriter(new File(FILES_FOLDER + "Processed.csv"));

        for(Node node : this.list){
        	StringBuilder sb = new StringBuilder();	 

            sb.append(node.getUrl() + ",");
            sb.append(node.getName() + ",");
            sb.append(node.getTitle() + ",");
	        sb.append(node.getLastModified()+"\n");
	
	        writer.write(sb.toString());
        }    
        writer.close();
    }
    
	/**
	 * createCSVDuplicates
	 * ===================
	 * 
	 * Create the final CSV with the data collected from the Wiki,
	 * that results in duplicate urls & titles.
	 * 
	 * Used by the function "processWiki".
	 * 
	 * @param  none
	 * @return	nothing
	 */
    private void createCSVDuplicates() throws Exception {

        PrintWriter writer = new PrintWriter(new File(FILES_FOLDER + "Duplicates.csv"));

        for(Node node : this.listDuplicates){
        	StringBuilder sb = new StringBuilder();	 

	        sb.append(node.getUrl() + ",");
		    sb.append(node.getName() + ",");
		    sb.append(node.getTitle() + ",");
	        sb.append(node.getLastModified()+"\n");
	
	        writer.write(sb.toString());
       }    
       writer.close();
    }
    
	/**
	 * createCSVNotFound
	 * =================
	 * 
	 * Create the final CSV with the keywords not found in the Wiki.
	 * 
	 * Used by the function "processWiki".
	 * 
	 * @param  none
	 * @return	nothing
	 */
    private void createCSVNotFound() throws Exception {
        PrintWriter writer = new PrintWriter(new File(FILES_FOLDER + "NotFound.csv"));

        for(Node node : this.listNotFound){
        	StringBuilder sb = new StringBuilder();	 
	            
        	sb.append(node.getName() + "\n");
        	
	        writer.write(sb.toString());
      	}  
        writer.close();
    }
}



