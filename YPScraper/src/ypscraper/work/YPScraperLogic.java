/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ypscraper.work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ypscraper.YPScraper;

/**
 *
 * @author Ihor
 */
public class YPScraperLogic {

    //private static String[] provinces = {"AB", "BC", "MB", "NB", "NL", "NT", "NS", "NU", "ON", "PE", "QC", "SK", "YT"};
    private String[] postalCodes = null;
    private static String business;
    private String currentURL;
    private int currentPageNumber = 1;
    private String CABaseURLPart = "https://www.yellowpages.ca/search/si";
    private String CAMainURL = "https://www.yellowpages.ca";
    private String province;
    String separator = File.separator;
    ExecutorService executorService;
    int connectionTimeout;
    File propertiesFile;
    ScrapedItemsStorage storage;
    boolean isMultipleSearch;
    public boolean running;
    int postalCodeIndex;

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
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveProperties() {
        try {
            output = new FileOutputStream(propertiesFile.getAbsoluteFile());
            parent.properties.setProperty("business", business);
            if (!isMultipleSearch) {
                parent.properties.setProperty("province", province);
            }
            parent.properties.setProperty("connTimeout", Integer.toString(connectionTimeout));
            parent.properties.setProperty("outputFolder", parent.getlblOutputPathData().getText());
            parent.properties.setProperty("csvPostalCodesFile", parent.getlblPostalCodesPathData().getText());
            parent.properties.setProperty("running", String.valueOf(running));
            parent.properties.setProperty("postalCodeIndex", Integer.toString(postalCodeIndex));
            parent.properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void restoreProperties() {
        try {
            createNewFile();
            input = new FileInputStream(propertiesFile.getAbsoluteFile());
            parent.properties.load(input);

            business = parent.properties.get("business").toString();
            parent.getTextFieldBusiness().setText(business);

            province = parent.properties.get("province").toString();
            parent.getTextFieldLocation().setText(province);

            String value = parent.properties.get("connTimeout").toString();
            connectionTimeout = Integer.parseInt(value);
            parent.getTextFieldConnectionTimeout().setValue(connectionTimeout);

            String path = parent.properties.get("outputFolder").toString();
            parent.getlblOutputPathData().setText(path);
            
            String csvPath = parent.properties.get("csvPostalCodesFile").toString();
            parent.getlblPostalCodesPathData().setText(csvPath);
            
            String postalCodeIndexStr = parent.properties.get("postalCodeIndex").toString();
            postalCodeIndex = Integer.parseInt(postalCodeIndexStr);
            
            String runningStr = parent.properties.get("running").toString();
            running = Boolean.parseBoolean(runningStr);
            if (running) {
                continueRun();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void continueRun() {
        parent.getBtnStop().setEnabled(true);
        parent.getBtnStart().setEnabled(false);
        parent.getBtnChooseCSVPostaCodesPath().setEnabled(false);
        parent.getBtnOutputPath().setEnabled(false);
        getPostalCodes(parent.getlblPostalCodesPathData().getText());
        Run(false);
    }

    public void Run(boolean isStartRun) {
        business = parent.getTextFieldBusiness().getText();
        connectionTimeout = (Integer) parent.getTextFieldConnectionTimeout().getValue();
        province = parent.getTextFieldLocation().getText();

        storage = new ScrapedItemsStorage(business, province);
        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new Runnable() {
            boolean continueWork = true;

            public void run() {
                running = true;
                currentPageNumber = 1;
                    if (!province.equalsIgnoreCase("")) {
                        isMultipleSearch = false;
                        prepareURL(currentPageNumber);
                        int pages = countPages();
                        for (int i = 2; i <= pages; i++) {
                            prepareURL(i);
                            Document doc = scrapePage();
                            parseCurrentPage(doc);
                        }
                        saveDataToFile();
                    } else {
                        if (isStartRun) {
                            postalCodeIndex = 0;
                        }
                        isMultipleSearch = true;
                        while (continueWork) {
                            prepareURL(currentPageNumber);
                            int pages = countPages();
                            for (int i = 2; i <= pages; i++) {
                                prepareURL(i);
                                Document doc = scrapePage();
                                parseCurrentPage(doc);
                            }
                            postalCodeIndex++;
                            saveProperties();
                            
                        }
                        saveDataToFile();
                    }
                running = false;
                saveProperties();
            }
        });
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    if (future.isDone()) {
                        System.out.println("Return: isDone");
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

    private void parseCurrentPage(Document doc) {
        if (doc == null) {
            System.out.println("Return: parseCurrentPage");
            return;
        }
        Element items = (Element) doc.select("div.resultList").first().childNode(1);
        Elements els = items.select("div.listing");
        for (Element el : els) {
            String title = el.select("h3.listing__name").select("a").text();
            String link = processLink(el.select("li.mlr__item--website").select("a").attr("href"));
            String address = el.select("div.listing__address").text();
            storage.List.add(new ScrapedItem(title, address.replace("Get directions", ""), processLink(link)));
        }
        System.out.println(storage.List.size());
    }

    private String processLink(String link) {
        String result = "";
        if (link == null) {
            return result;
        }
        try {
            String res = java.net.URLDecoder.decode(link, "UTF-8");
            result = res.substring(res.indexOf("=") + 1);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private Document scrapePage() {
        Document doc = null;
        try {
            doc = Jsoup.connect(currentURL).get();
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private int countPages() {
        Document doc = scrapePage();
        String count = doc.select("span.contentControls-msg").select("strong").text().replace(",", "");

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
        if (splited.length > 1) {
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
            province = postalCodes[postalCodeIndex];
            url += "/" + province;
        } else {
            url += "/" + province;
        }
        currentURL = url;
    }

    public void saveDataToFile() {
        StringBuilder sb = new StringBuilder();
        //Headers
        sb.append("Link");
        sb.append(',');
        sb.append("\"Name\"");
        sb.append(',');
        sb.append("\"Address\"");
        sb.append('\n');

        //Data
        for (int i = 0; i < storage.List.size(); i++) {
            sb.append(storage.List.get(i).Link);
            sb.append(',');
            sb.append("\"" +storage.List.get(i).Name+ "\"");
            sb.append(',');
            sb.append("\"" + storage.List.get(i).Address + "\"");
            sb.append('\n');
        }
        try {
            Path path = Paths.get(parent.getlblOutputPathData().getText() + separator + business.replace(" ", "") + "_" + province + ".csv");
            Files.write(path, sb.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        storage.List.clear();
    }
    
    public void getPostalCodes(String path){
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
