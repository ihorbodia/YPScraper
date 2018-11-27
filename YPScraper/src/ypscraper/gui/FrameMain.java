package gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import work.YPPageScraper;

public class FrameMain extends JFrame {
	
	private static JFrame consoleFrame;
	
	static {
		System.setProperty("log4j.configuration", "file:log4j.xml");
		consoleFrame = new FrameConsole(); // catch the sysout and syserr streams before Log4J
	}
	
	private static Log log = LogFactory.getLog(FrameMain.class);
	
	private static final String OPTION_OK = "OK";
	private static final String OPTION_CANCEL = "Cancel";
	
	private class ActionOutputDirectory extends AbstractAction {
		
		public ActionOutputDirectory(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(FrameMain.this.isIdle() && JFileChooser.APPROVE_OPTION == getFileChooserOutputDirectory().showDialog(FrameMain.this, "Select")) {
				setOutputDirectory(getFileChooserOutputDirectory().getSelectedFile());
				getInitProperties().setProperty("frameMain.outputDirectory", getOutputDirectory().getPath());
				// you have to store the new output directory path to be recovered after a shutdown
				// this is not guaranteed to work with files (permissions, preserving comments, s.o.)
				FrameMain.this.shutdownStateFile = null;
				FrameMain.this.init();
			}
		}
	}
	
	private class ActionArchiveDirectory extends AbstractAction {
		
		public ActionArchiveDirectory(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(JFileChooser.APPROVE_OPTION == getFileChooserArchiveDirectory().showDialog(FrameMain.this, "Select")) {
				setArchiveDirectory(getFileChooserArchiveDirectory().getSelectedFile());
			}
		}
	}
	
	private class ActionMaxPause extends AbstractAction {
		
		public ActionMaxPause(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getMaxPauseSpinner().setValue(getMaxPause());
			getMaxPauseDialog().setVisible(true);
			if(OPTION_OK.equals(getMaxPauseOptionPane().getValue())) {
				setMaxPause((Integer)getMaxPauseSpinner().getValue());
			}
		}
	}
	
	private class ActionConnectTimeout extends AbstractAction {
		
		public ActionConnectTimeout(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getConnectTimeoutSpinner().setValue(getConnectTimeout());
			getConnectTimeoutDialog().setVisible(true);
			if(OPTION_OK.equals(getConnectTimeoutOptionPane().getValue())) {
				setConnectTimeout((Integer)getConnectTimeoutSpinner().getValue());
			}
		}
	}
	
	private class ActionParseTimeout extends AbstractAction {
		
		public ActionParseTimeout(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getParseTimeoutSpinner().setValue(getParseTimeout());
			getParseTimeoutDialog().setVisible(true);
			if(OPTION_OK.equals(getParseTimeoutOptionPane().getValue())) {
				setParseTimeout((Integer)getParseTimeoutSpinner().getValue());
			}
		}
	}
	
	private class ActionViewConsole extends AbstractAction {
		public ActionViewConsole(String name) {
			super(name);
		}
		public void actionPerformed(ActionEvent e) {
			getConsoleFrame().setVisible(true);
		}
	}
	
	private class ActionStart extends AbstractAction {

		public ActionStart(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		    if(FrameMain.this.getPanelMain().hasBusinessSpecified()) {
//				getScheduledExecutorService().schedule(
//					  new YPPageScraper(FrameMain.this)
//					, 0
//					, TimeUnit.SECONDS);
		    	FrameMain.this.getPanelMain().getTextFieldStatus()
	    			.setText("Started searching");
		    	FrameMain.this.setStarted();
		    } else {
		    	FrameMain.this.getPanelMain().getTextFieldStatus()
		    		.setText("No Business to look for");
		    }
		}
	}
	
	private class ActionStop extends AbstractAction {
				
		public ActionStop(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
	    	FrameMain.this.setStopped();
		}
	}
	
	private class ActionResume extends AbstractAction {
		
		public ActionResume(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
	    	FrameMain.this.setResumed();
		}
	}

	private PanelMain panelMain;
	private JMenuBar menuBar;
	private JMenu menuSettings;
	private JMenuItem mntmOutputDirectory;
	private JMenuItem mntmArchiveDirectory;
	private JMenuItem mntmMaxPause;
	private JCheckBoxMenuItem mntmUseProxies;
	private Action actionOutputDirectory;
	private Action actionArchiveDirectory;
	private Action actionMaxPause;

	private JFileChooser fileChooserOutputDirectory;
	private File outputDirectory;

	private JFileChooser fileChooserArchiveDirectory;
	private File archiveDirectory;

	private JSpinner maxPauseSpinner;
	private JOptionPane maxPauseOptionPane;
	private JDialog maxPauseDialog;
	private volatile Integer maxPause;

	private ScheduledExecutorService scheduledExecutorService;
	private Templates templatesData;
	private Templates templatesBusinessName;
	private Templates templatesWebSite;
	
	private Action actionStart;
	private Action actionResume;
	
	private volatile boolean running = false;
	private volatile boolean cancelled = false;
	private volatile boolean idle = true;

	private ArrayList<Proxy> proxies;
	private JMenu menuView;
	private JMenuItem mntmConsole;
	private Action actionViewConsole;
	
	private JSpinner connectTimeoutSpinner;
	private JOptionPane connectTimeoutOptionPane;
	private JDialog connectTimeoutDialog;
	private volatile Integer connectTimeout;
	
	private JSpinner parseTimeoutSpinner;
	private JOptionPane parseTimeoutOptionPane;
	private JDialog parseTimeoutDialog;
	private volatile Integer parseTimeout;
	private JMenuItem mntmConnectTimeout;
	private JMenuItem mntmParseTimeout;
	
	private Properties initProperties;
	private File shutdownStateFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameMain frame = new FrameMain();
					frame.pack();
					Dimension frameSize = frame.getSize();
					frameSize.width += 100;
					frame.setMinimumSize(frameSize);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrameMain() {
		setTitle("YP Crawler CA");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setJMenuBar(getMenuBarCustom());
		setBounds(100, 100, 450, 300);
		setContentPane(getPanelMain());
		init();
	}
	
	private void init() {
		Properties shutdownStateProperties = null;
		if(getShutdownStateFile().exists() && getShutdownStateFile().canRead()) {
			shutdownStateProperties = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(getShutdownStateFile());
				shutdownStateProperties.load(fis);
			} catch (Exception e) {
				log.info("Shutdown state properties not loaded", e);
			} finally {
				close(fis);
			}
		}
		if(shutdownStateProperties != null && !shutdownStateProperties.isEmpty()) {
			String propertyValue = shutdownStateProperties.getProperty("frameMain.cancelled");
			if(propertyValue != null && propertyValue.length() > 0) {
				setCancelled(Boolean.parseBoolean(propertyValue));
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.running");
			if(propertyValue != null && propertyValue.length() > 0) {
				setRunning(Boolean.parseBoolean(propertyValue));
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.idle");
			if(propertyValue != null && propertyValue.length() > 0) {
				setIdle(Boolean.parseBoolean(propertyValue));
			}
			if(isIdle()) {
				setIdle();
			} else
			if(!isRunning()) {
				setStopped();
			} else {
				setResumed();
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.connectTimeout");
			if(propertyValue != null && propertyValue.length() > 0) try {
				setConnectTimeout(Integer.parseInt(propertyValue));
			} catch (Exception e) {
				log.error("Failed to restore the 'frameMain.connectTimeout' property", e);
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.outputDirectory");
			if(propertyValue != null && propertyValue.length() > 0) try {
				setOutputDirectory(new File(propertyValue));
				if(!(getOutputDirectory().exists() && getOutputDirectory().isDirectory() && getOutputDirectory().canWrite())) {
					log.error(
						"Failed to restore the 'frameMain.outputDirectory' property. " +
						"Ensure the output path exists, represents a directory and the application is allowed to write there.");
				} else {
					getFileChooserOutputDirectory().setSelectedFile(getOutputDirectory());
				}
			} catch (Exception e) {
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.archiveDirectory");
			if(propertyValue != null && propertyValue.length() > 0) try {
				setArchiveDirectory(new File(propertyValue));
				if(!(getArchiveDirectory().exists() && getArchiveDirectory().isDirectory() && getArchiveDirectory().canWrite())) {
					log.error(
						"Failed to restore the 'frameMain.archiveDirectory' property. " +
						"Ensure the archive path exists, represents a directory and the application is allowed to write there.");
				} else {
					getFileChooserArchiveDirectory().setSelectedFile(getArchiveDirectory());
				}
			} catch (Exception e) {
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.maxPause");
			if(propertyValue != null && propertyValue.length() > 0) try {
				setMaxPause(Integer.parseInt(propertyValue));
			} catch (Exception e) {
				log.error("Failed to restore the 'frameMain.maxPause' property", e);
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.useProxies");
			if(propertyValue != null && propertyValue.length() > 0) {
				getMenuItemUseProxies().setSelected(Boolean.parseBoolean(propertyValue));
			}
			propertyValue = shutdownStateProperties.getProperty("frameMain.parseTimeout");
			if(propertyValue != null && propertyValue.length() > 0) try {
				setParseTimeout(Integer.parseInt(propertyValue));
			} catch (Exception e) {
				log.error("Failed to restore the 'frameMain.parseTimeout' property", e);
			}
			getPanelMain().init(shutdownStateProperties);
			YPPageScraper ypScraper = null;// new YPPageScraper(this);
			ypScraper.init(shutdownStateProperties);
			// Restore the progress bar (maintained by the YPPageScraper when run)
			getPanelMain().getProgressBar().setModel(ypScraper);
			if(!isIdle()) {
				getScheduledExecutorService().schedule(ypScraper, 0, TimeUnit.SECONDS);
			}
		} else {
			setIdle();
		}
	}
	
	public void close(InputStream is) {
		if(is != null) try {
			is.close();
		} catch(Exception e) {
			log.error("Failed to close an InputStream object", e);
		}
	}
	
	public synchronized void save(Properties properties) {
		properties.setProperty("frameMain.cancelled", String.valueOf(isCancelled()));
		properties.setProperty("frameMain.running", String.valueOf(isRunning()));
		properties.setProperty("frameMain.idle", String.valueOf(isIdle()));
		properties.setProperty("frameMain.connectTimeout", String.valueOf(getConnectTimeout()));
		properties.setProperty("frameMain.outputDirectory", getOutputDirectory().getPath());
		properties.setProperty("frameMain.archiveDirectory", getArchiveDirectory().getPath());
		properties.setProperty("frameMain.maxPause", String.valueOf(getMaxPause()));
		properties.setProperty("frameMain.useProxies", String.valueOf(useProxies()));
		properties.setProperty("frameMain.parseTimeout", String.valueOf(getParseTimeout()));
		getPanelMain().save(properties);
	}

	private JMenuBar getMenuBarCustom() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getMenuSettings());
			menuBar.add(getMnView());
		}
		return menuBar;
	}
	private JMenu getMenuSettings() {
		if (menuSettings == null) {
			menuSettings = new JMenu("Settings");
// you have to store the changed output directory path to be recovered after a shutdown
// this is not guaranteed to work with files (permissions, preserving comments, s.o.)
// the output directory may be changed by editing the initial properties file
// the following menu item is blocked for these reasons
//			menuSettings.add(getMenuItemOutputDirectory());
			menuSettings.add(getMenuItemArchiveDirectory());
			menuSettings.add(getMenuItemMaxPause());
			menuSettings.add(getMntmConnectTimeout());
			menuSettings.add(getMntmParseTimeout());
			menuSettings.add(getMenuItemUseProxies());
		}
		return menuSettings;
	}
	
	private JMenuItem getMenuItemArchiveDirectory() {
		if (mntmArchiveDirectory == null) {
			mntmArchiveDirectory = new JMenuItem();
			mntmArchiveDirectory.setAction(getActionArchiveDirectory());
		}
		return mntmArchiveDirectory;
	}
	
	private Action getActionOutputDirectory() {
		if (actionOutputDirectory == null) {
			actionOutputDirectory = new ActionOutputDirectory("Output Directory");
		}
		return actionOutputDirectory;
	}
	
	private JMenuItem getMenuItemOutputDirectory() {
		if (mntmOutputDirectory == null) {
			mntmOutputDirectory = new JMenuItem();
			mntmOutputDirectory.setAction(getActionOutputDirectory());
		}
		return mntmOutputDirectory;
	}
	
	private Action getActionArchiveDirectory() {
		if (actionArchiveDirectory == null) {
			actionArchiveDirectory = new ActionArchiveDirectory("Archive Directory");
		}
		return actionArchiveDirectory;
	}
	
	private JMenuItem getMenuItemMaxPause() {
		if (mntmMaxPause == null) {
			mntmMaxPause = new JMenuItem();
			mntmMaxPause.setAction(getActionMaxPause());
		}
		return mntmMaxPause;
	}
	
	private Action getActionMaxPause() {
		if (actionMaxPause == null) {
			actionMaxPause = new ActionMaxPause("Max Pause");
		}
		return actionMaxPause;
	}

	private JCheckBoxMenuItem getMenuItemUseProxies() {
		if(mntmUseProxies == null) {
			String useProxiesProperty = getInitProperties().getProperty("frameMain.useProxies");
			if(useProxiesProperty != null) {
				mntmUseProxies = new JCheckBoxMenuItem("Use Proxies", Boolean.parseBoolean(useProxiesProperty));
			} else {
				log.info("Could not read the useProxies default, the hardcoded value will be used");
				mntmUseProxies = new JCheckBoxMenuItem("Use Proxies", true);
			}
		}
		return mntmUseProxies;
	}

	private JFileChooser getFileChooserOutputDirectory() {
		if(fileChooserOutputDirectory == null) {
			fileChooserOutputDirectory = new JFileChooser(getOutputDirectory());
			fileChooserOutputDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		return fileChooserOutputDirectory;
	}

	public synchronized File getOutputDirectory() {
		if(outputDirectory == null) {
			try {
				outputDirectory = new File(getInitProperties().getProperty("frameMain.outputDirectory"));
			} catch (Exception e) {
				log.info("Could not read the output directory default, the hardcoded value will be used", e);
			}
			if(outputDirectory == null || !(outputDirectory.exists() && outputDirectory.canWrite())) {
				log.info("Setting the hardcoded value for the output directory");
				outputDirectory = new File(System.getProperty("user.dir"));
			}
			if(!outputDirectory.canWrite()) {
				log.info("Cannot output to '" + outputDirectory + "'. Proposed to select another directory.");
				JOptionPane.showMessageDialog(this, "Cannot output to '" + outputDirectory + "'. Please, select another directory.");
			}
		}
		return outputDirectory;
	}

	private synchronized void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private JFileChooser getFileChooserArchiveDirectory() {
		if(fileChooserArchiveDirectory == null) {
			fileChooserArchiveDirectory = new JFileChooser(getArchiveDirectory());
			fileChooserArchiveDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		return fileChooserArchiveDirectory;
	}

	public File getArchiveDirectory() {
		if(archiveDirectory == null) {
			archiveDirectory = getOutputDirectory();
		}
		return archiveDirectory;
	}

	private void setArchiveDirectory(File archiveDirectory) {
		this.archiveDirectory = archiveDirectory;
	}

	private JDialog getMaxPauseDialog() {
		if(maxPauseDialog == null) {
			maxPauseDialog = 
				getMaxPauseOptionPane().createDialog(this, "Select the maximum pause");
		}
		return maxPauseDialog;
	}

	private JOptionPane getMaxPauseOptionPane() {
		if(maxPauseOptionPane == null) {
			maxPauseOptionPane = new JOptionPane(
				  new Object[]{"Maximum Pause [seconds]: ", getMaxPauseSpinner()}
				, JOptionPane.PLAIN_MESSAGE
				, JOptionPane.OK_CANCEL_OPTION
				, null
				, new Object[]{OPTION_OK, OPTION_CANCEL});
		}
		return maxPauseOptionPane;
	}

	private JSpinner getMaxPauseSpinner() {
		if(maxPauseSpinner == null) {
			maxPauseSpinner = new JSpinner(new SpinnerNumberModel(getMaxPause().intValue(), 0, 600, 5));
		}
		return maxPauseSpinner;
	}

	public synchronized Integer getMaxPause() {
		if(maxPause == null) {
			try {
				maxPause = new Integer(getInitProperties().getProperty("frameMain.maxPause"));
			} catch (Exception e) {
				log.info("Could not read the max pause default, the hardcoded value will be used", e);
			}
			if(maxPause == null || maxPause < 0) {
				log.info("Setting the hardcoded value for the max pause");
				maxPause = new Integer(10); // seconds
			}
		}
		return maxPause;
	}

	public synchronized void setMaxPause(Integer maxPause) {
		this.maxPause = maxPause;
	}

	public synchronized ScheduledExecutorService getScheduledExecutorService() {
		if(scheduledExecutorService == null) {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		}
		return scheduledExecutorService;
	}

	public synchronized Templates getTemplatesData() {
		if(templatesData == null) try {
			templatesData = TransformerFactory.newInstance().newTemplates(
				new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("templatesData.xsl")));
		} catch(Exception e) {
			//TODO log this
			e.printStackTrace();
		}
		return templatesData;
	}

	public synchronized Templates getTemplatesBusinessName() {
		if(templatesBusinessName == null) try {
			templatesBusinessName = TransformerFactory.newInstance().newTemplates(
				new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("templatesBusinessName.xsl")));
		} catch(Exception e) {
			//TODO log this
			e.printStackTrace();
		}
		return templatesBusinessName;
	}

	public synchronized Templates getTemplatesWebSite() {
		if(templatesWebSite == null) try {
			templatesWebSite = TransformerFactory.newInstance().newTemplates(
				new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("templatesWebSite.xsl")));
		} catch(Exception e) {
			//TODO log this
			e.printStackTrace();
		}
		return templatesWebSite;
	}

	public PanelMain getPanelMain() {
		if(panelMain == null) {
			panelMain = new PanelMain();
			panelMain.getBtnStart().setAction(getActionStart()); // subject to later changes
			panelMain.getBtnStop().setAction(new ActionStop("Stop"));
		}
		return panelMain;
	}

	private Action getActionStart() {
		if(actionStart == null) {
			actionStart = new ActionStart("Start");
		}
		return actionStart;
	}

	private Action getActionResume() {
		if(actionResume == null) {
			actionResume = new ActionResume("Resume");
		}
		return actionResume;
	}

	public boolean isRunning() {
		return running;
	}

	private void setRunning(boolean running) {
		this.running = running;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isCancelled() {
		return cancelled;
	}
	
	private boolean isIdle() {
		return idle;
	}

	private void setIdle(boolean idle) {
		this.idle = idle;
	}

	public void setStarted() {
		getPanelMain().getBtnStart().setAction(getActionStart());
		getActionStart().setEnabled(false);
    	getPanelMain().getBtnStop().getAction().setEnabled(true);
    	setRunning(true);
    	setCancelled(false);
    	setIdle(false);
    	getActionOutputDirectory().setEnabled(false);
    	synchronized(this) {
    		notifyAll();
    	}
	}
	
	public void setStopped() {
		getPanelMain().getBtnStart().setAction(getActionResume());
		getActionResume().setEnabled(true);
    	getPanelMain().getBtnStop().getAction().setEnabled(false);
    	setRunning(false);
    	setIdle(false);
    	getActionOutputDirectory().setEnabled(false);
    	synchronized(this) {
    		notifyAll();
    	}
	}
	
	public void setResumed() {
		getPanelMain().getBtnStart().setAction(getActionStart());
		getActionStart().setEnabled(false);
    	getPanelMain().getBtnStop().getAction().setEnabled(true);
    	setRunning(true);
    	setIdle(false);
    	getActionOutputDirectory().setEnabled(false);
    	synchronized(this) {
    		notifyAll();
    	}
	}
	
	public void setIdle() {
		getPanelMain().getBtnStart().setAction(getActionStart());
		getActionStart().setEnabled(true);
    	getPanelMain().getBtnStop().getAction().setEnabled(false);
    	getPanelMain().getTextFieldLocation().setEditable(true);
    	setRunning(false);
    	setIdle(true);
    	getActionOutputDirectory().setEnabled(true);
    	synchronized(this) {
    		notifyAll();
    	}
	}

	public ArrayList<Proxy> getProxies() {
		if(proxies == null) try {
			proxies = new ArrayList<Proxy>();
			BufferedReader br = new BufferedReader(new FileReader(new File("proxies.csv")));
			String line = null;
			while((line = br.readLine()) != null) {
				addProxy(line.split("\t"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return proxies;
	}
	
	private void addProxy(String[] definition) {
		if(definition.length > 1) try {
			proxies.add(new Proxy(
				  Proxy.Type.SOCKS
				, new InetSocketAddress(definition[0], Integer.parseInt(definition[1]))));
		} catch (NumberFormatException e) {
//			e.printStackTrace();
		}
	}
	
	public boolean useProxies() {
		return getMenuItemUseProxies().isSelected();
	}

	public synchronized void waitUntilStarted() {
		while(!running) try {
			wait();
		} catch(InterruptedException e) {
		}
	}
	private JMenu getMnView() {
		if (menuView == null) {
			menuView = new JMenu("View");
			menuView.add(getMenuItemConsole());
		}
		return menuView;
	}
	private JMenuItem getMenuItemConsole() {
		if (mntmConsole == null) {
			mntmConsole = new JMenuItem(getActionViewConsole());
		}
		return mntmConsole;
	}
	private Action getActionViewConsole() {
		if (actionViewConsole == null) {
			actionViewConsole = new ActionViewConsole("Console");
		}
		return actionViewConsole;
	}

	private JFrame getConsoleFrame() {
		if(consoleFrame == null) {
			consoleFrame = new FrameConsole();
		}
		return consoleFrame;
	}

	private JDialog getConnectTimeoutDialog() {
		if(connectTimeoutDialog == null) {
			connectTimeoutDialog = 
				getConnectTimeoutOptionPane().createDialog(this, "Select the connect timeout");
		}
		return connectTimeoutDialog;
	}

	private JOptionPane getConnectTimeoutOptionPane() {
		if(connectTimeoutOptionPane == null) {
			connectTimeoutOptionPane = new JOptionPane(
				  new Object[]{"Connect timeout [milliseconds]: ", getConnectTimeoutSpinner()}
				, JOptionPane.PLAIN_MESSAGE
				, JOptionPane.OK_CANCEL_OPTION
				, null
				, new Object[]{OPTION_OK, OPTION_CANCEL});
		}
		return connectTimeoutOptionPane;
	}

	private JSpinner getConnectTimeoutSpinner() {
		if(connectTimeoutSpinner == null) {
			connectTimeoutSpinner = new JSpinner(new SpinnerNumberModel(getConnectTimeout(), 0, 1000000, 500));
		}
		return connectTimeoutSpinner;
	}

	public synchronized int getConnectTimeout() {
		if(connectTimeout == null) {
			try {
				connectTimeout = new Integer(getInitProperties().getProperty("frameMain.connectTimeout"));
			} catch (Exception e) {
				log.info("Could not read the connect timeout default, the hardcoded value will be used", e);
			}
			if(connectTimeout == null || connectTimeout < 0) {
				log.info("Setting the hardcoded value for the connect timeout");
				connectTimeout = new Integer(5000); // millis
			}
		}
		return connectTimeout;
	}

	private synchronized void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	private JMenuItem getMntmConnectTimeout() {
		if (mntmConnectTimeout == null) {
			mntmConnectTimeout = new JMenuItem(new ActionConnectTimeout("Connect Timeout"));
		}
		return mntmConnectTimeout;
	}

	private JDialog getParseTimeoutDialog() {
		if(parseTimeoutDialog == null) {
			parseTimeoutDialog = 
				getParseTimeoutOptionPane().createDialog(this, "Select the parse timeout");
		}
		return parseTimeoutDialog;
	}

	private JOptionPane getParseTimeoutOptionPane() {
		if(parseTimeoutOptionPane == null) {
			parseTimeoutOptionPane = new JOptionPane(
				  new Object[]{"Parse timeout [seconds]: ", getParseTimeoutSpinner()}
				, JOptionPane.PLAIN_MESSAGE
				, JOptionPane.OK_CANCEL_OPTION
				, null
				, new Object[]{OPTION_OK, OPTION_CANCEL});
		}
		return parseTimeoutOptionPane;
	}

	private JSpinner getParseTimeoutSpinner() {
		if(parseTimeoutSpinner == null) {
			parseTimeoutSpinner = new JSpinner(new SpinnerNumberModel(getParseTimeout(), 0, 360000, 30));
		}
		return parseTimeoutSpinner;
	}

	public synchronized int getParseTimeout() {
		if(parseTimeout == null) {
			try {
				parseTimeout = new Integer(getInitProperties().getProperty("frameMain.parseTimeout"));
			} catch (Exception e) {
				log.info("Could not read the parse timeout default, the hardcoded value will be used", e);
			}
			if(parseTimeout == null || parseTimeout < 0) {
				log.info("Setting the hardcoded value for the parse timeout");
				parseTimeout = new Integer(60); // seconds
			}
		}
		return parseTimeout;
	}

	private synchronized void setParseTimeout(Integer parseTimeout) {
		this.parseTimeout = parseTimeout;
	}

	private JMenuItem getMntmParseTimeout() {
		if (mntmParseTimeout == null) {
			mntmParseTimeout = new JMenuItem(new ActionParseTimeout("Parse Timeout"));
		}
		return mntmParseTimeout;
	}

	public Properties getInitProperties() {
		if(initProperties == null) {
			initProperties = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("ypscraper.properties");
				initProperties.load(fis);
			} catch (Exception e) {
				log.info("Init properties not loaded", e);
			} finally {
				close(fis);
			}
		}
		return initProperties;
	}

	public File getShutdownStateFile() {
		if(shutdownStateFile == null) {
			shutdownStateFile = new File(getOutputDirectory(), "ypscraper-shutdown-state.properties");
			shutdownStateFile.setWritable(true);
		}
		return shutdownStateFile;
	}
}
