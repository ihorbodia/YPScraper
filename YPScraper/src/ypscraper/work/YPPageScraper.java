package work;

import gui.FrameMain;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JOptionPane;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class YPPageScraper extends DefaultBoundedRangeModel implements Runnable {
	
	private static Log log = LogFactory.getLog(YPPageScraper.class);
	private static String[] provinces = {"AB", "BC", "MB", "NB", "NL", "NT", "NS", "NU", "ON", "PE", "QC", "SK", "YT"};
	private static int maxPages = 50;
	private static String postalCodeDelimiter = new String(new char[]{178,114,112,99,45});
	private static String locationDelimiter = "/rci-";

	private class ActionCancel extends AbstractAction {
		
		private HttpGet httpGet;
				
		public ActionCancel(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
	    	frameMain.setCancelled(true);
	    	if(this.httpGet != null) {
	    		try {
					this.httpGet.abort();
					this.httpGet = null;
				} catch (Exception e1) {
					log.error("Failed to abort HttpGet request", e1);
				}
	    	}
		}

		public void setHttpGet(HttpGet httpGet) {
			this.httpGet = httpGet;
		}
	}
	
	private class ConnectionCloseTask implements Runnable {
		
		private HttpGet httpGet;
		
		public ConnectionCloseTask() {
		}

		public ConnectionCloseTask(HttpGet httpGet) {
			super();
			this.httpGet = httpGet;
		}

		public ConnectionCloseTask setHttpGet(HttpGet httpGet) {
			this.httpGet = httpGet;
			return this;
		}

		@Override
		public void run() {
			log.info("Parse timeout elapsed, aborting the HttpGet request");
			if(this.httpGet != null) try {
				this.httpGet.abort();
				this.httpGet = null;
			} catch(Exception e) {
				log.error("Failed to abort HttpGet request", e);
				getTimeoutService().schedule(this, YPPageScraper.this.frameMain.getParseTimeout(), TimeUnit.SECONDS);
			}
		}
	}
	
	private class ProxyConnectionFactory extends PlainSocketFactory {
		
		@Override
		public Socket createSocket() {
			return super.createSocket();
		}
		
		@Override
		public Socket createSocket(HttpParams params) {
			if(frameMain.useProxies() && frameMain.getProxies().size() > 0) {
				YPPageScraper.this.currentProxy = 
					frameMain.getProxies().get(getRandom().nextInt(frameMain.getProxies().size()));
				frameMain.getPanelMain().getTextFieldStatus()
					.setText("Opening page: " + YPPageScraper.this.totalPagesScraped + "  (proxy: " + YPPageScraper.this.currentProxy.address() + ")");
				log.info(YPPageScraper.this.url);
				return new Socket(YPPageScraper.this.currentProxy);
			} else {
				YPPageScraper.this.currentProxy = null;
				frameMain.getPanelMain().getTextFieldStatus()
					.setText("Opening page: " + YPPageScraper.this.totalPagesScraped);
				log.info(YPPageScraper.this.url);
				return super.createSocket(params);
			}
		}
	}
	
	private class PagingInfo {

		private boolean available;
		private int totalPages;
		private int currentPage;
		
		public PagingInfo(XPath xpath, Node resultDocument) {
			try {
				if((Boolean)xpath.evaluate("//pagination[range]", resultDocument, XPathConstants.BOOLEAN)) {
					Matcher paginationRangeMatcher = getPaginationRangePattern()
						.matcher(xpath.evaluate("//pagination/range/text()", resultDocument));
					if(paginationRangeMatcher.matches()) {
						this.totalPages = 
							Integer.parseInt(paginationRangeMatcher.group(2));
						this.currentPage =
							Integer.parseInt(paginationRangeMatcher.group(1));
						this.available = true;
					}
				}
			} catch (Exception e) {
				log.error("Failed to obtain paging info", e);
			}
		}
	}
	
	private class SearchRefinementStatus {
		private boolean refined;
		private boolean refinementOptionsExhausted;
		
		public SearchRefinementStatus(boolean refined, boolean refinementOptionsExhausted) {
			super();
			this.refined = refined;
			this.refinementOptionsExhausted = refinementOptionsExhausted;
		}
	}

	private String serverUrl = "http://www.yellowpages.ca";
	
	private String url;
	private File outputDirectory;
	private FrameMain frameMain;
	
	private URL currentUrl;
	private int currentPage = 1;
	private boolean locationSpecified;
	private int currentStateIndex = 0;
	private Random random;
	
	private ScheduledExecutorService timeoutService;
	private ConnectionCloseTask connectionCloseTask;
	private ActionCancel actionCancel;
	
	private ProxyConnectionFactory proxyConnectionFactory;
	private Proxy currentProxy;
	
	private Properties shutdownStateProperties;
	private StringBuffer blockedProxies;
	
	private String currentStatusString;

	private Pattern paginationRangePattern;
	private boolean isLastPage;
	private boolean isUndoRequest;
	private int currentPostalCodeIndex;
	private int currentLocationIndex;
	
	private volatile int totalPagesScraped = 1;
	private volatile int totalPagesAvailable;
	
	public void init(Properties properties) {
		url = properties.getProperty("scraper.url");
		String propertyValue = properties.getProperty("scraper.currentPage");
		if(propertyValue != null && propertyValue.length() > 0) try {
			currentPage = Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.currentPage' property", e);
		}
		propertyValue = properties.getProperty("scraper.locationSpecified");
		if(propertyValue != null && propertyValue.length() > 0) {
			locationSpecified = Boolean.parseBoolean(propertyValue);
		}
		propertyValue = properties.getProperty("scraper.currentStateIndex");
		if(propertyValue != null && propertyValue.length() > 0) try {
			currentStateIndex = Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.currentStateIndex' property", e);
		}
		propertyValue = properties.getProperty("scraper.currentProxyAddress");
		if(propertyValue != null && propertyValue.length() > 0) try {
			URI proxyUri = new URI(propertyValue);
			if(proxyUri.getScheme() == null) {
				proxyUri = new URI("tcp:/" + properties.getProperty("scraper.currentProxyAddress"));
			}
			currentProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort()));
		} catch (Exception e) {
			log.error("Failed to restore the 'scraper.currentProxyAddress' property", e);
		}
		propertyValue = properties.getProperty("scraper.blockedProxies");
		if(propertyValue != null && propertyValue.length() > 0) {
			blockedProxies = new StringBuffer(propertyValue);
			for(String blockedProxyAddress : propertyValue.split(";")) try {
				URI blockedProxyUri = new URI(blockedProxyAddress);
				if(blockedProxyUri.getScheme() == null) {
					blockedProxyUri = new URI("tcp:/" + blockedProxyAddress);
				}
				frameMain.getProxies().remove(
					new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(blockedProxyUri.getHost(), blockedProxyUri.getPort())));
			} catch (Exception e) {
				log.error("Failed to restore the 'scraper.blockedProxies' property: " + blockedProxyAddress, e);
			}
		}
		propertyValue = properties.getProperty("scraper.progressBar.maximum");
		if(propertyValue != null && propertyValue.length() > 0) try {
			setMaximum(Integer.parseInt(propertyValue));
		} catch (Exception e) {
			log.error("Failed to restore the 'scraper.progressBar.maximum' property", e);
		}
		propertyValue = properties.getProperty("scraper.progressBar.value");
		if(propertyValue != null && propertyValue.length() > 0) try {
			setValue(Integer.parseInt(propertyValue));
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.progressBar.value' property", e);
		}
		propertyValue = properties.getProperty("scraper.isLastPage");
		if(propertyValue != null && propertyValue.length() > 0) {
			isLastPage = Boolean.parseBoolean(propertyValue);
		}
		propertyValue = properties.getProperty("scraper.isUndoRequest");
		if(propertyValue != null && propertyValue.length() > 0) {
			isUndoRequest = Boolean.parseBoolean(propertyValue);
		}
		propertyValue = properties.getProperty("scraper.currentPostalCodeIndex");
		if(propertyValue != null && propertyValue.length() > 0) try {
			currentPostalCodeIndex = Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.currentPostalCodeIndex' property", e);
		}
		propertyValue = properties.getProperty("scraper.currentLocationIndex");
		if(propertyValue != null && propertyValue.length() > 0) try {
			currentLocationIndex = Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.currentLocationIndex' property", e);
		}
		propertyValue = properties.getProperty("scraper.totalPagesScraped");
		if(propertyValue != null && propertyValue.length() > 0) try {
			totalPagesScraped = Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.totalPagesScraped' property", e);
		}
		propertyValue = properties.getProperty("scraper.totalPagesAvailable");
		if(propertyValue != null && propertyValue.length() > 0) try {
			totalPagesAvailable = Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			log.error("Failed to restore the 'scraper.totalPagesAvailable' property", e);
		}
	}
	
	public void save(Properties properties) {
		if(url != null) {
			properties.setProperty("scraper.url", url);
		}
		properties.setProperty("scraper.currentPage", String.valueOf(currentPage));
		properties.setProperty("scraper.locationSpecified", String.valueOf(locationSpecified));
		properties.setProperty("scraper.currentStateIndex", String.valueOf(currentStateIndex));
		if(currentProxy != null) {
			properties.setProperty("scraper.currentProxyAddress", currentProxy.address().toString());
		}
		if(blockedProxies != null) { // be sure we actually have blocked proxies
			properties.setProperty("scraper.blockedProxies", blockedProxies.toString());
		}
		properties.setProperty("scraper.progressBar.maximum", String.valueOf(getMaximum()));
		properties.setProperty("scraper.progressBar.value", String.valueOf(getValue()));
		properties.setProperty("scraper.isLastPage", String.valueOf(isLastPage));
		properties.setProperty("scraper.isUndoRequest", String.valueOf(isUndoRequest));
		properties.setProperty("scraper.currentPostalCodeIndex", String.valueOf(currentPostalCodeIndex));
		properties.setProperty("scraper.currentLocationIndex", String.valueOf(currentLocationIndex));
		properties.setProperty("scraper.totalPagesScraped", String.valueOf(totalPagesScraped));
		properties.setProperty("scraper.totalPagesAvailable", String.valueOf(totalPagesAvailable));
		frameMain.save(properties);
	}
	
	private void persist() {
		getShutdownStateProperties().clear();
		save(getShutdownStateProperties());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(frameMain.getShutdownStateFile());
			getShutdownStateProperties().store(fos, "");
		} catch (Exception e) {
			log.error("Failed to store the shutdown state.", e);
		} finally {
			close(fos);
		}
	}

	public YPPageScraper(FrameMain frameMain) {
		super();
		this.frameMain = frameMain;
		outputDirectory = frameMain.getOutputDirectory();
	    String locationString = 
	    	frameMain.getPanelMain().getTextFieldLocation().getText();
		locationSpecified = locationString != null && locationString.trim().length() > 0;
		// Do not maintain GUI state here. 
		// This constructor is used when recovering from a shutdown state.
	}

	@Override
	public void run() {
		if(!locationSpecified) { // keep it here and not in the constructor to orderly restore the GUI from a shutdown state
			frameMain.getPanelMain().getTextFieldLocation().setEditable(false);
		}
		waitUntilStarted();
		processCurrentUrl();
		persist();
		if(isLastPage) { // we have parsed the last page
			if(locationSpecified || currentStateIndex >= provinces.length) {
				try {
					moveOutputFilesToArchive();
				} catch (Exception e) {
					log.error("Failed to move the output files to the Archive Directory", e);
				}
				frameMain.getPanelMain().getTextFieldStatus()
					.setText(currentStatusString);
				if(!locationSpecified) {
					frameMain.getPanelMain().getTextFieldLocation()
						.setText(null);
				}
				frameMain.setIdle();
				persist();
			} else {
				reschedule("Scraping page " + totalPagesScraped + " scheduled");
			}
		}
	}
	
	private void processCurrentUrl() {
		prepareUrl();
		if(frameMain != null && url != null && url.length() > 0) {
			SchemeRegistry schemeRegistry = 
				new SchemeRegistry();
			schemeRegistry.register(
				new Scheme("http", 80, getProxyConnectionFactory()));
			HttpClient httpClient = 
				new DefaultHttpClient(
					new ThreadSafeClientConnManager(schemeRegistry));
			frameMain.getPanelMain().getProgressBar().setModel(this);
			frameMain.getPanelMain().getBtnCancel().setAction(getActionCancel());
			DOMResult domResult = new DOMResult();
			ScheduledFuture futureConnectionCloseTask = null;
			try {
				String absoluteUrl = composeNextUrl(url);
				if(absoluteUrl == null) { // Cannot resolve the given URL
					log.error("Cannot resolve URI: " + url);
					return;
				}
				HttpGet httpGet = new HttpGet(absoluteUrl);
				httpGet.getParams().setParameter("http.socket.timeout", frameMain.getConnectTimeout());
				httpGet.getParams().setParameter("http.connection-manager.timeout", frameMain.getConnectTimeout());
				httpGet.getParams().setParameter("http.protocol.head-body-timeout", frameMain.getConnectTimeout());
				httpGet.getParams().setParameter("http.connection.timeout", frameMain.getConnectTimeout());
				httpGet.setHeader(
						  "User-Agent"
						, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
//				httpGet.setHeader(
//						  "Host"
//						, "www.yellowpages.ca");
//				httpGet.setHeader(
//						  "Accept"
//						, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//				httpGet.setHeader(
//						  "Accept-Charset"
//						, "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
//				httpGet.setHeader(
//						  "Accept-Language"
//						, "en-GB,en-US;q=0.8,en;q=0.6");
//				httpGet.setHeader(
//						  "Cache-Control"
//						, "max-age=0");
//				httpGet.setHeader(
//						  "Connection"
//						, "keep-alive");
				getActionCancel().setHttpGet(httpGet);
				HttpResponse response = httpClient.execute(httpGet);
				futureConnectionCloseTask =
					getTimeoutService().schedule(getConnectionCloseTask().setHttpGet(httpGet), frameMain.getParseTimeout(), TimeUnit.SECONDS);
				HttpEntity entity = response.getEntity();
				if(currentProxy == null) {
					frameMain.getPanelMain().getTextFieldStatus()
						.setText("Parsing page: " // + currentPage + "/" + currentLocationIndex + "/" + currentPostalCodeIndex 
								+ totalPagesScraped);
				} else {
					frameMain.getPanelMain().getTextFieldStatus()
						.setText("Parsing page: " // + currentPage + "/" + currentLocationIndex + "/" + currentPostalCodeIndex 
								+ totalPagesScraped
								+ " (proxy: " + currentProxy.address() + ")");
				}
//ByteArrayOutputStream baos = new ByteArrayOutputStream();
//InputStream is = entity.getContent();
//int next = is.read();
//while (next > -1) {
//    baos.write(next);
//    next = is.read();
//}
//baos.flush();
				frameMain.getTemplatesData().newTransformer().transform(
//new SAXSource(getParser(), new InputSource(new ByteArrayInputStream(baos.toByteArray())))
					  new SAXSource(getParser(), new InputSource(entity.getContent()))
					, domResult);
//TransformerFactory.newInstance().newTransformer().transform(
//	  new DOMSource(domResult.getNode())
//	, new StreamResult(System.out));
				getActionCancel().setHttpGet(null);
				futureConnectionCloseTask.cancel(true);
				currentUrl = httpGet.getURI().toURL(); 
				XPath xpath = XPathFactory.newInstance().newXPath();
				PagingInfo pagingInfo =
					new PagingInfo(xpath, domResult.getNode());
				setValueIsAdjusting(true);
				if(!locationSpecified) {
			    	setMaximum(provinces.length);
				} else
				if(totalPagesAvailable < pagingInfo.totalPages) {
					totalPagesAvailable = pagingInfo.totalPages;
					setMaximum(totalPagesAvailable);
				}
				if(!isUndoRequest && (!pagingInfo.available || (pagingInfo.totalPages <= maxPages))) {
					storeToFile(domResult.getNode());
					if(locationSpecified) {
						setValue(totalPagesScraped - 1);
					} else {
						setValue(currentStateIndex);
					}
				} else {
					SearchRefinementStatus searchRefinementStatus =
						refineSearch(pagingInfo, xpath, domResult.getNode());
					if(searchRefinementStatus.refined) {
						reschedule("Search refine scheduled");
					} else
					if(searchRefinementStatus.refinementOptionsExhausted) {
						isLastPage = true;
						currentStatusString = "Scraping finished";
						if(locationSpecified || currentStateIndex >= provinces.length) { // set here to avoid doing this when blocked
							setValue(getMaximum());
						}
					} else {
						log.error("Failed to refine the search. A maximum of " + maxPages + " will be available for scraping.");
						if(undoRefine()) {
							reschedule("Search refine undo scheduled");
						} else {
							isLastPage = true;
							currentStatusString = "Scraping finished";
							if(locationSpecified || currentStateIndex >= provinces.length) { // set here to avoid doing this when blocked
								setValue(getMaximum());
							}
						}
					}
					return;
				}
				setValueIsAdjusting(false);
				fireStateChanged();
				String nextUrl = 
					xpath.evaluate("//pagination/next/text()", domResult.getNode());
				boolean nextUrlAvailable = 
					nextUrl != null && nextUrl.length() > 0;
				if(!nextUrlAvailable) {
//					Boolean isBlocked = (Boolean)
//						xpath.evaluate("//is-blocked/text()", domResult.getNode(), XPathConstants.BOOLEAN);
//					if(isBlocked) {
//						currentProxyBlocked();
//						return;
//					}
					Boolean isError = (Boolean)
						xpath.evaluate("//is-error/text()", domResult.getNode(), XPathConstants.BOOLEAN);
					if(isError && totalPagesScraped > 1) {
						rescheduleAfter("Scraping page " + totalPagesScraped + " re-scheduled", 10);
//storePageToFile(new ByteArrayInputStream(baos.toByteArray()));
						return;
					}
				}
				setValueIsAdjusting(true);
				totalPagesScraped++;
				adjustValue(pagingInfo);
				if(nextUrlAvailable) {
					advancePage(); // Do not use nextUrl. It may be PROXIED and we are changing the PROXY (if any) for each new page
					reschedule("Scraping page " + totalPagesScraped + " scheduled");
//storeLastMinusOnePageToFile(new ByteArrayInputStream(baos.toByteArray()));
				} else 
				if(isLastPage) {
//storePageToFile(new ByteArrayInputStream(baos.toByteArray()));
					resetPage();
					if(undoRefine()) {
						isLastPage = false;
						reschedule("Search refine undo scheduled");
					} else {
						currentStatusString = "Scraping finished";
						if(locationSpecified || currentStateIndex >= provinces.length) { // set here to avoid doing this when blocked
							setValue(getMaximum());
						}
					}
				} else {
					log.info("Current page appeared last in sequence, but not recognized as such. Considered it temporarily unavailable.");
					getActionCancel().setHttpGet(null);
					if(futureConnectionCloseTask != null) {
						futureConnectionCloseTask.cancel(true);
					}
					reschedule("Scraping page " + totalPagesScraped + " re-scheduled");
//storeLastMinusOnePageToFile(new ByteArrayInputStream(baos.toByteArray()));
				}
				setValueIsAdjusting(false);
				fireStateChanged();
			} catch (Exception e) {
				log.info("Recoverable exception while scraping:", e);
				getActionCancel().setHttpGet(null);
				if(futureConnectionCloseTask != null) {
					futureConnectionCloseTask.cancel(true);
				}
				rescheduleAfter("Scraping page " + totalPagesScraped + " re-scheduled", 10);
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
		} // if
	}
	
	private String composeNextUrl(String nextUrl) {
		if(nextUrl.startsWith("http://")) {
			return nextUrl;
		} else
		if(nextUrl.startsWith("/")) {
			return serverUrl + nextUrl;
		} else 
		if(currentUrl != null) {
			return currentUrl.toString().split("\\?")[0] + "/" + nextUrl;
		}
		return null;
	}
	
	private void adjustValue(PagingInfo pagingInfo) {
		isLastPage = false;
		if(pagingInfo != null) {
			if(pagingInfo.available) {
				isLastPage = 
					(pagingInfo.currentPage == pagingInfo.totalPages);
			} else {
				isLastPage = true;
			}
		} else {
			log.error("Failed to adjust the progress bar. The PagingInfo object is null.");
		}
	}
	
	private void waitUntilStarted() {
		if(!frameMain.isRunning() && !frameMain.isCancelled()) {
			frameMain.getPanelMain().getTextFieldStatus()
				.setText("Scraping stopped");
			persist();
			frameMain.waitUntilStarted();
		}
	}
	
	private void reschedule(String nextStatusPrefix) {
		if(frameMain.isCancelled()) {
			frameMain.setIdle();
			frameMain.getPanelMain().getTextFieldStatus()
				.setText("Scraping cancelled");
			if(!locationSpecified) {
				frameMain.getPanelMain().getTextFieldLocation().setText(null);
			}
			persist();
		} else {
			int pause = getRandom().nextInt(1 + frameMain.getMaxPause());
			frameMain.getScheduledExecutorService().schedule(
				  this
				, pause
				, TimeUnit.SECONDS);
			frameMain.getPanelMain().getTextFieldStatus()
				.setText(nextStatusPrefix + " in: " + pause + " seconds");
		}
	}
	
	private void rescheduleAfter(String nextStatusPrefix, int pause) {
		if(frameMain.isCancelled()) {
			frameMain.setIdle();
			frameMain.getPanelMain().getTextFieldStatus()
				.setText("Scraping cancelled");
			if(!locationSpecified) {
				frameMain.getPanelMain().getTextFieldLocation().setText(null);
			}
			persist();
		} else {
			frameMain.getScheduledExecutorService().schedule(
				  this
				, pause
				, TimeUnit.SECONDS);
			frameMain.getPanelMain().getTextFieldStatus()
				.setText(nextStatusPrefix + " in: " + pause + " seconds");
		}
	}

	private XMLReader getParser() throws SAXNotRecognizedException, SAXNotSupportedException {
		XMLReader reader = new Parser();
		//reader.setFeature(Parser.namespacesFeature, false);
		//reader.setFeature(Parser.namespacePrefixesFeature, false);
		reader.setFeature(Parser.ignoreBogonsFeature, true);
		reader.setFeature(Parser.bogonsEmptyFeature, false);
		return reader;
	}
	
	private void prepareUrl() {
		// The (url == null) below covers the initial state and is used for clarity instead of initiating isLastPage to "true"
		if(isLastPage || url == null) { 
		    String locationString = null;
		    if(locationSpecified) {
		    	locationString = 
		    		frameMain.getPanelMain().getTextFieldLocation().getText();
		    } else
		    if(currentStateIndex < provinces.length) {
		    	locationString = provinces[currentStateIndex];
		    	currentStateIndex++;
		    	frameMain.getPanelMain().getTextFieldLocation().setText(locationString);
		    }
		    
		    if(locationString != null) try {
		    	String businessString = 
		    		frameMain.getPanelMain().getTextFieldBusiness().getText();
			    String key1 = 
			    	URLEncoder.encode(businessString, Charset.defaultCharset().name());
			    String key3 = 
			    	URLEncoder.encode(locationString, Charset.defaultCharset().name());
			    url = "/search/si/" + currentPage + "/" + key1 + "/" + key3;
			    isLastPage = false;
		    } catch(Exception e) {
		    	log.error("Failed to prepare the URL: ", e);
		    }
		}
	}
	
	private void advancePage() {
		currentPage++;
		if(url != null) {
			url = url.replaceFirst("/si/[0-9]*/", "/si/" + currentPage + "/");
		}
	}
	
	private void resetPage() {
		currentPage = 1;
		if(url != null) {
			url = url.replaceFirst("/si/[0-9]*/", "/si/" + currentPage + "/");
		}
	}

	private Random getRandom() {
		if(random == null) {
			random = new Random(System.currentTimeMillis());
		}
		return random;
	}
	
//	private void storeLastMinusOnePageToFile(InputStream is) throws IOException {
//		StringBuffer fileNamePrefix = new StringBuffer(
//			frameMain.getPanelMain().getTextFieldBusiness().getText())
//			.append("_")
//			.append(frameMain.getPanelMain().getTextFieldLocation().getText())
//			.append("_kast.html");
//		OutputStream os =
//			new FileOutputStream(new File(outputDirectory,fileNamePrefix.toString()), false);
//		int next = is.read();
//		while (next > -1) {
//		    os.write(next);
//		    next = is.read();
//		}
//		os.close();
//	}
//	
//	private void storePageToFile(InputStream is) throws IOException {
//		StringBuffer fileNamePrefix = new StringBuffer(
//			frameMain.getPanelMain().getTextFieldBusiness().getText())
//			.append("_")
//			.append(frameMain.getPanelMain().getTextFieldLocation().getText())
//			.append("_last.html");
//		OutputStream os =
//			new FileOutputStream(new File(outputDirectory,fileNamePrefix.toString()), false);
//		int next = is.read();
//		while (next > -1) {
//		    os.write(next);
//		    next = is.read();
//		}
//		os.close();
//	}
	
	private void storeToFile(Node resultDocument) throws TransformerConfigurationException, TransformerException, IOException {
		StringBuffer fileNamePrefix = new StringBuffer(
			  frameMain.getPanelMain().getTextFieldBusiness().getText());
		int fileNamePrefixLength = fileNamePrefix.length();
		if(locationSpecified) {
			fileNamePrefix
				.append("_")
				.append(frameMain.getPanelMain().getTextFieldLocation().getText());
			fileNamePrefixLength = fileNamePrefix.length();
		}
		OutputStream osName =
			new FileOutputStream(new File(outputDirectory,fileNamePrefix.append("_name.txt").toString()), true);
		frameMain.getTemplatesBusinessName().newTransformer().transform(
				  new DOMSource(resultDocument)
				, new StreamResult(osName));
		osName.close();
		fileNamePrefix.setLength(fileNamePrefixLength);
		OutputStream osWeb =
			new FileOutputStream(new File(outputDirectory,fileNamePrefix.append("_web.txt").toString()), true);
		frameMain.getTemplatesWebSite().newTransformer().transform(
				  new DOMSource(resultDocument)
				, new StreamResult(osWeb));
		osWeb.close();
	}
	
	private void moveOutputFilesToArchive() throws IOException {
		StringBuffer fileNamePrefix = new StringBuffer(
			  frameMain.getPanelMain().getTextFieldBusiness().getText());
		int fileNamePrefixLength = fileNamePrefix.length();
		if(locationSpecified) {
			fileNamePrefix
				.append("_")
				.append(frameMain.getPanelMain().getTextFieldLocation().getText());
			fileNamePrefixLength = fileNamePrefix.length();
		}
		fileNamePrefix.append("_name.txt");
		if(!rename(
			  new File(outputDirectory, fileNamePrefix.toString())
			, new File(frameMain.getArchiveDirectory(), fileNamePrefix.toString()))) {
			currentStatusString = "Failed to move an output file to the Archive Directory";
			log.error("Failed to move the output file for names to the Archive Directory");
		}
		fileNamePrefix.setLength(fileNamePrefixLength);
		fileNamePrefix.append("_web.txt");
		if(!rename(
			  new File(outputDirectory, fileNamePrefix.toString())
			, new File(frameMain.getArchiveDirectory(), fileNamePrefix.toString()))){
			currentStatusString = "Failed to move an output file to the Archive Directory";
			log.error("Failed to move the output file for WEB addresses to the Archive Directory");
		}
	}
	
	private boolean rename(File fromFile, File toFile) {
		if(fromFile != null && fromFile.exists() && toFile != null) {
			if(fromFile.equals(toFile)) {
				return true;
			}
			if(toFile.exists()) {
				if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(frameMain, "The file " + toFile.getAbsolutePath() + " already exists. Override?")) {
					log.info("Renamaming " + fromFile.getAbsolutePath() + " refused by the user. The target file " + toFile.getAbsolutePath() + " already exists.");
					return false;
				} else 
				if(!toFile.delete()) {
					log.info("Renamaming " + fromFile.getAbsolutePath() + " failed. The target file " + toFile.getAbsolutePath() + " already exists and the delete operation failed.");
					return false;
				}
			}
			return fromFile.renameTo(toFile);
		}
		return false;
	}

	private ScheduledExecutorService getTimeoutService() {
		if(timeoutService == null) {
			timeoutService = Executors.newSingleThreadScheduledExecutor();
		}
		return timeoutService;
	}

	private ConnectionCloseTask getConnectionCloseTask() {
		if(connectionCloseTask == null) {
			connectionCloseTask = new ConnectionCloseTask();
		}
		return connectionCloseTask;
	}

	private ActionCancel getActionCancel() {
		if(actionCancel == null) {
			actionCancel = new ActionCancel("Cancel");
		}
		return actionCancel;
	}

	private ProxyConnectionFactory getProxyConnectionFactory() {
		if(proxyConnectionFactory == null) {
			proxyConnectionFactory = new ProxyConnectionFactory();
		}
		return proxyConnectionFactory;
	}

	private Properties getShutdownStateProperties() {
		if(shutdownStateProperties == null) {
			shutdownStateProperties = new Properties();
		}
		return shutdownStateProperties;
	}
	
	private void currentProxyBlocked() {
		if(frameMain.useProxies()) {
			log.error("Removing a blacklisted proxy: " + currentProxy.address());
			frameMain.getProxies().remove(currentProxy);
			if(getBlockedProxies().length() < 1) {
				getBlockedProxies().append(currentProxy.address().toString());
			} else {
				getBlockedProxies().append(';').append(currentProxy.address().toString());
			}
			persist();
			if(frameMain.getProxies().size() < 1) {
				log.error("No more life proxies available");
				currentStatusString = "Scraping blocked by the site";
				isLastPage = true;
			} else {
				reschedule("Scraping page " + totalPagesScraped + " re-scheduled");
			}
		} else {
			currentStatusString = "Scraping blocked by the site";
			isLastPage = true;
		}
	}
	
	private StringBuffer getBlockedProxies() {
		if(blockedProxies == null) {
			blockedProxies = new StringBuffer();
		}
		return blockedProxies;
	}

	private void close(OutputStream os) {
		if(os != null) try {
			os.flush();
			os.close();
		} catch (IOException e) {
			log.error("Failed to close an OutputStream", e);
		}
	}

	private Pattern getPaginationRangePattern() {
		if(paginationRangePattern == null) {
			paginationRangePattern = Pattern.compile("Page\\s+([0-9]+)\\s+of\\s+([0-9]+)");
		}
		return paginationRangePattern;
	}
	
	private SearchRefinementStatus refineSearch(PagingInfo pagingInfo, XPath xpath, Node resultDocument) throws XPathExpressionException, UnsupportedEncodingException, DOMException {
		if(url != null) {
			NodeList locationNameNodeList = (NodeList)
				xpath.evaluate("//refinement/location/name", resultDocument, XPathConstants.NODESET);
			if(!url.contains(locationDelimiter)) {
				if(locationNameNodeList == null || locationNameNodeList.getLength() < 1) {
					isUndoRequest = false;
					return new SearchRefinementStatus(false, false);
				}
				if(currentLocationIndex < locationNameNodeList.getLength()) {
					url = url + locationDelimiter 
					    + URLEncoder.encode( locationNameNodeList.item(currentLocationIndex).getTextContent().trim()
					    		           , Charset.defaultCharset().name());
					currentLocationIndex++;
					isUndoRequest = false;
					return new SearchRefinementStatus(true, false);
				} else {
					isUndoRequest = false;
					return new SearchRefinementStatus(false, true);
				}
			} else
			if(!url.contains(postalCodeDelimiter)) {
				NodeList postalCodeNodeList = (NodeList)
					xpath.evaluate("//refinement/postal-code/value", resultDocument, XPathConstants.NODESET);
				if(postalCodeNodeList == null || postalCodeNodeList.getLength() < 1) {
					isUndoRequest = false;
					return new SearchRefinementStatus(false, false);
				}
				if(postalCodeNodeList.getLength() == 1 && postalCodeNodeList.item(0).getTextContent().contains("undo")) {
					return new SearchRefinementStatus(undoRefine(), false);
				}
				if(currentPostalCodeIndex < postalCodeNodeList.getLength()) {
					url = url + postalCodeDelimiter 
					    + URLEncoder.encode( postalCodeNodeList.item(currentPostalCodeIndex).getTextContent().trim()
					    		           , Charset.defaultCharset().name());
					currentPostalCodeIndex++;
					isUndoRequest = false;
					return new SearchRefinementStatus(true, false);
				}
				// An "undo" has been done for the postal code and all the postal codes has been processed:
				if(locationNameNodeList.getLength() == 1 && locationNameNodeList.item(0).getTextContent().contains("undo")) {
					return new SearchRefinementStatus(undoRefine(), false);
				}
			}
		}
		isUndoRequest = false;
		return new SearchRefinementStatus(false, false);
	}
	
	private boolean undoRefine() {
		if(url != null) {
			if(url.contains(postalCodeDelimiter)) {
				url = url.substring(0, url.indexOf(postalCodeDelimiter));
				isUndoRequest = true;
				return true;
			} else
			if(url.contains(locationDelimiter)) {
				url = url.substring(0, url.indexOf(locationDelimiter));
				currentPostalCodeIndex = 0;
				isUndoRequest = true;
				return true;
			}
		}
		return false;
	}
}
