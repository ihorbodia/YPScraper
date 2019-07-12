package Logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Ihor
 */
public class YPScraperLogic {

    //private static String[] provinces = {"AB", "BC", "MB", "NB", "NL", "NT", "NS", "NU", "ON", "PE", "QC", "SK", "YT"};
    private String[] postalCodes = null;
    private String business;
    private String currentURL;
    public boolean continueWork = false;
    private String CABaseURLPart = "https://www.yellowpages.ca/search/si";
    private String province;
    String separator = File.separator;
    ExecutorService executorService;
    File propertiesFile;
    ScrapedItemsStorage storage;
    boolean isMultipleSearch;
    public boolean running;
    int postalCodeIndex;
    int scrapedItemsCount;

    String firstPage;
    InputStream input = null;
    OutputStream output = null;
    private YPScraper parent;
    public Future<?> future;

    public YPScraperLogic(YPScraper parent) throws IOException, InterruptedException, ExecutionException {
        this.parent = parent;
        restoreProperties();
    }

    private void createNewFile() {
        try {
            File propertiesFileTemp = File.createTempFile("config", ".properties");
            String propPath = FilenameUtils.getFullPathNoEndSeparator(propertiesFileTemp.getAbsolutePath()) + separator + "config.properties";
            File f = new File(propPath);
            if (f.exists() && !f.isDirectory()) {
                propertiesFile = f;
            } else {
                propertiesFile = f;
                f.createNewFile();
                output = new FileOutputStream(propertiesFile.getAbsoluteFile());
                parent.properties.setProperty("business", "");
                parent.properties.setProperty("province", "");
                parent.properties.setProperty("connTimeout", "0");
                parent.properties.setProperty("outputFolder", "");
                parent.properties.setProperty("csvPostalCodesFile", "");
                parent.properties.setProperty("running", "");
                parent.properties.setProperty("postalCodeIndex", "0");
                parent.properties.store(output, null);
            }
            propertiesFileTemp.delete();
        } catch (IOException io) {
            io.printStackTrace();
            System.out.println(io.getMessage());
            parent.logger.log(Level.SEVERE, null, io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    parent.logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    public void saveProperties() {
        try {
            output = new FileOutputStream(propertiesFile.getAbsoluteFile());
            parent.properties.setProperty("business", parent.getTextFieldBusiness().getText());
            if (!isMultipleSearch) {
                parent.properties.setProperty("province", parent.getTextFieldLocation().getText());
            }
            if (parent.outputFolder != null) {
                parent.properties.setProperty("outputFolder", parent.outputFolder.getAbsolutePath());
            }
            if (parent.inputLocationsFile != null) {
                parent.properties.setProperty("csvPostalCodesFile", parent.inputLocationsFile.getAbsolutePath());
            }

            parent.properties.setProperty("running", String.valueOf(running));
            parent.properties.setProperty("postalCodeIndex", Integer.toString(postalCodeIndex));
            parent.properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
            System.out.println(io.getMessage());
            parent.logger.log(Level.SEVERE, null, io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    parent.logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void restoreProperties() {
        try {
            createNewFile();
            input = new FileInputStream(propertiesFile.getAbsoluteFile());
            parent.properties.load(input);

            if (parent.properties.get("business") != null) {
                business = parent.properties.get("business").toString();
                parent.getTextFieldBusiness().setText(business);
            }

            if (parent.properties.get("province") != null) {
                province = parent.properties.get("province").toString();
                parent.getTextFieldLocation().setText(province);
            }

            if (parent.properties.get("outputFolder") != null) {
                String path = parent.properties.get("outputFolder").toString();
                if (!path.equalsIgnoreCase("")) {
                    parent.outputFolder = new File(path);
                    parent.getlblOutputPathData().setText(parent.outputFolder.getName());
                }
            }

            if (parent.properties.get("csvPostalCodesFile") != null) {
                String csvPath = parent.properties.get("csvPostalCodesFile").toString();
                if (!csvPath.equalsIgnoreCase("")) {
                    parent.inputLocationsFile = new File(csvPath);
                    parent.getlblPostalCodesPathData().setText(parent.inputLocationsFile.getName());
                }
            }

            if (parent.properties.get("postalCodeIndex") != null) {
                String postalCodeIndexStr = parent.properties.get("postalCodeIndex").toString();
                postalCodeIndex = Integer.parseInt(postalCodeIndexStr);
            }
            if (parent.properties.get("running") != null) {
                String runningStr = parent.properties.get("running").toString();
                running = Boolean.parseBoolean(runningStr);
            }

            if (running) {
                continueRun();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            parent.logger.log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    parent.logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    public void continueRun() {
        Run(false);
    }

    public void Run(boolean isStartRun) {
        business = parent.getTextFieldBusiness().getText();
        province = parent.getTextFieldLocation().getText();

        if (parent.outputFolder == null) {
            File f = new File(".");
            parent.outputFolder = new File(f.getAbsolutePath()).getParentFile();
            parent.getlblOutputPathData().setText(parent.outputFolder.getName());
        }

        storage = new ScrapedItemsStorage(business, province);
        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new Runnable() {
            public void run() {
                scrapedItemsCount = 0;
                continueWork = true;
                running = true;
                if (!province.equalsIgnoreCase("")) {
                    isMultipleSearch = false;
                    prepareURL(1);
                    int pages = countPages();
                    updateOneLocationSearchGUI(false);
                    for (int i = 2; i <= pages; i++) {
                        prepareURL(i);
                        Document doc = scrapePage();
                        parseCurrentPage(doc);
                        updateOneLocationSearchGUI(false);
                        if (storage.List.size() > 10000) {
                            saveDataToFile();
                        }
                    }
                    updateOneLocationSearchGUI(true);
                    saveDataToFile();
                } else {
                    if (isStartRun) {
                        postalCodeIndex = 0;
                    }
                    if (parent.inputLocationsFile == null) {
                        JOptionPane.showMessageDialog(null, "Input CSV file not specified, program cannot continue.", "Select input locations list", JOptionPane.ERROR_MESSAGE);
                        continueWork = false;
                        running = false;
                        return;
                    }
                    getPostalCodes(parent.inputLocationsFile.getAbsolutePath());
                    isMultipleSearch = true;
                    while (continueWork) {
                        prepareURL(1);
                        int pages = countPages();
                        updateMultipleSearchGUI(false);
                        for (int i = 2; i <= pages; i++) {
                            prepareURL(i);
                            Document doc = scrapePage();
                            parseCurrentPage(doc);
                            updateMultipleSearchGUI(false);
                            if (storage.List.size() > 10000) {
                                saveDataToFile();
                            }
                        }
                        postalCodeIndex++;
                        saveProperties();
                    }
                    updateMultipleSearchGUI(true);
                    saveDataToFile();
                }
                continueWork = false;
                running = false;
                saveProperties();
            }
        });
        Thread thread = new Thread() {
            public void run() {
                parent.getBtnStart().setEnabled(false);
                parent.getBtnStop().setEnabled(true);
                parent.getBtnOutputPath().setEnabled(false);
                parent.getBtnChooseCSVPostaCodesPath().setEnabled(false);
                while (true) {
                    if (future.isDone()) {
                        System.out.println("Program: Finished");
                        parent.logger.log(Level.INFO, "Program: Finished");
                        break;
                    }
                }
                parent.getBtnStart().setEnabled(true);
                parent.getBtnStop().setEnabled(false);
                parent.getBtnOutputPath().setEnabled(true);
                parent.getBtnChooseCSVPostaCodesPath().setEnabled(true);
            }
        };
        thread.start();
    }

    public void updateMultipleSearchGUI(boolean isFinished) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (postalCodeIndex <= postalCodes.length) {
                        int postalCodeItem = postalCodeIndex + 1;
                        parent.getTextFieldStatus().setText(postalCodeItem + "/" + postalCodes.length + " locations processed. " + scrapedItemsCount + " items scraped.");
                    }
                    if (isFinished) {
                        postalCodeIndex--;
                        parent.getTextFieldStatus().setText("Finished. " + postalCodeIndex + "/" + postalCodes.length + " locations processed. " + scrapedItemsCount + " items scraped.");
                    }
                }
            });
            return;
        }
    }

    public void updateOneLocationSearchGUI(boolean isFinished) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (isFinished) {
                        parent.getTextFieldStatus().setText("Finished. "+scrapedItemsCount + " items scraped.");
                    }
                    else
                    {
                        parent.getTextFieldStatus().setText(scrapedItemsCount + " items scraped.");
                    }
                }
            });
            return;
        }
    }

    public void removeOldFileIfExists() {
        if (parent.outputFolder != null) {
            Path path = null;
            if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
                path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + "_" + province + ".csv");
            } else {
                path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + ".csv");
            }
            try {
                Files.deleteIfExists(path);
            } catch (IOException ex) {
                parent.logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void parseCurrentPage(Document doc) {
        if (doc == null) {
            System.out.println("Return: parseCurrentPage");
            parent.logger.log(Level.WARNING, "Scraped document is null");
            return;
        }
        Element items = (Element) doc.select("div.resultList").first().childNode(1);
        Elements els = items.select("div.listing");
        for (Element el : els) {
            String title = el.select("h3.listing__name").select("a.listing__link").text();
            String link = processLink(el.select("li.mlr__item--website").select("a").attr("href"));
            String address = el.select("div.listing__address").text();
            storage.List.add(new ScrapedItem(title, address.replace("Get directions", ""), processLink(link), province));
        }
        scrapedItemsCount += els.size();
        parent.logger.log(Level.INFO, String.valueOf(storage.List.size() +" Location: "+ province +" Link: "+ currentURL));
    }

    private String processLink(String link) {
        String result = "";
        if (link == null) {
            return result;
        }
        try {
            String res = java.net.URLDecoder.decode(link, "UTF-8");
            result = res.substring(res.indexOf("redirect=") + 1);
            result = result.replace("edirect=", "");
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
            parent.logger.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private Document scrapePage() {
        Document doc = null;
        try {
            doc = Jsoup
                    .connect(currentURL)
                    .userAgent("Mozilla/5.0")
                    .timeout(0)
                    .get();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            parent.logger.log(Level.SEVERE, null, ex);
            parent.logger.log(Level.INFO, String.valueOf(storage.List.size() +" Location: "+ province +" Link: "+ currentURL));
        }
        return doc;
    }

    private int countPages() {
        Document doc = scrapePage();
        Element countElement = doc.select("span.contentControls-msg").select("strong").first();
        String count= "";
        if (countElement != null) {
            count = countElement.text().replace(",", "");
        }
        if (count.equalsIgnoreCase("")) {
            count = "0";
        }

        float fcount = Float.parseFloat(count);
        int icount = Integer.parseInt(count);

        float floatPages = (float) (fcount / 40.0);
        int intPages = icount / 40;
        parseCurrentPage(doc);

        if (intPages > 50) {
            return 50;
        }
        if (floatPages > intPages) {
            return intPages + 1;
        } else {
            return intPages;
        }
    }

    private void prepareURL(int pageNumber) {
        String url;
        String[] splited = business.split("\\s+");
        url = CABaseURLPart + "/" + pageNumber + "/";
        if (splited.length > 0) {
            for (int i = 0; i < splited.length; i++) {
                url += splited[i];
                if (i < splited.length - 1) {
                    url += "%2B";
                }
            }
        } else {
            url = splited[0];
        }
        if (isMultipleSearch) {
            if (postalCodes.length > postalCodeIndex) {
                province = postalCodes[postalCodeIndex];
            } else {
                continueWork = false;
            }
            url += "/" + province;
        } else {
            url += "/" + province;
        }
        currentURL = url;
    }

    public void createOutputFile() {
        StringBuilder sb = new StringBuilder();
        Path path = null;

        if (!business.equalsIgnoreCase(parent.getTextFieldBusiness().getText())) {
            business = parent.getTextFieldBusiness().getText();
        }
        if (!province.equalsIgnoreCase(parent.getTextFieldLocation().getText())) {
            province = parent.getTextFieldLocation().getText();
        }

        if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
            path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + "_" + province + ".csv");
        } else {
            path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + ".csv");
        }

        sb.append("Link");
        sb.append(',');
        sb.append("\"Name\"");
        sb.append(',');
        sb.append("\"Address\"");
        sb.append(',');
        sb.append("\"Location\"");
        sb.append('\n');
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            parent.logger.log(Level.SEVERE, null, ex);
        }
    }

    public void saveDataToFile() {
        StringBuilder sb = new StringBuilder();
        try {
            Path path = null;
            if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
                path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + "_" + province + ".csv");
            } else {
                path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + ".csv");
            }
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                createOutputFile();
            }
            for (int i = 0; i < storage.List.size(); i++) {
                sb.append(storage.List.get(i).Link);
                sb.append(',');
                sb.append("\"" + storage.List.get(i).Name + "\"");
                sb.append(',');
                sb.append("\"" + storage.List.get(i).Address + "\"");
                sb.append(',');
                sb.append("\"" + storage.List.get(i).Location + "\"");
                sb.append('\n');
            }
            Files.createDirectories(path.getParent());
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            parent.logger.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Something wrong with output file: \n" + getPrintStacktrace(ex), "Data wasn't saved ", JOptionPane.ERROR_MESSAGE);
        }
        storage.List.clear();
    }

    private String getPrintStacktrace(Exception ex) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }

    public void getPostalCodes(String path) {
        String csvFile = path;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String> result = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] country = line.split(cvsSplitBy);
                result.add(country[0]);
            }
            postalCodes = result.toArray(new String[0]);
        } catch (FileNotFoundException e) {
            parent.logger.log(Level.SEVERE, null, e);
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Something wrong with input CSV file. Try to check path and start again.", "Input csv file problem", JOptionPane.ERROR_MESSAGE);
            continueWork = false;
        } catch (IOException e) {
            parent.logger.log(Level.SEVERE, null, e);
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Something wrong with input CSV file.", "Input csv file problem", JOptionPane.ERROR_MESSAGE);
            continueWork = false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    parent.logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }
}