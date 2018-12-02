/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ypscraper.work;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static String[] provinces = {"AB", "BC", "MB", "NB", "NL", "NT", "NS", "NU", "ON", "PE", "QC", "SK", "YT"};
    private static String business;
    private String currentURL;
    private int currentPageNumber = 1;
    private int currentProvinceIndex = 0;
    private String CABaseURLPart = "https://www.yellowpages.ca/search/si";
    private String CAMainURL = "https://www.yellowpages.ca";
    private String province;
    private int itemsCount;
    String separator = File.separator;
    ExecutorService executorService;
    int connectionTimeout;
    File propertiesFile;
    ScrapedItemsStorage storage;
    boolean isMultipleSearch;

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

    private void saveProperties() {
        try {
            output = new FileOutputStream(propertiesFile.getAbsoluteFile());
            parent.properties.setProperty("business", business);
            if (!isMultipleSearch) {
                parent.properties.setProperty("province", province);
            }
            parent.properties.setProperty("connTimeout", Integer.toString(connectionTimeout));
            parent.properties.setProperty("outputFolder", parent.getlblOutputPathData().getText());
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

    public void Run() throws IOException, InterruptedException, ExecutionException {
        business = parent.getTextFieldBusiness().getText();
        connectionTimeout = (Integer) parent.getTextFieldConnectionTimeout().getValue();
        province = parent.getTextFieldLocation().getText();

        storage = new ScrapedItemsStorage(business, province);
        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new Runnable() {
            boolean continueWork = true;
            public void run() {
                currentPageNumber = 1;
                try {
                    if (!province.equalsIgnoreCase("")) {
                        isMultipleSearch = false;
                        saveProperties();
                        prepareURL(currentPageNumber);
                        int pages = countPages();
                        for (int i = 2; i <= pages; i++) {
                            Thread.sleep(connectionTimeout);
                            prepareURL(i);
                            Document doc = scrapePage(currentURL);
                            parseCurrentPage(doc);
                        }
                        itemsCount = 0;
                        saveDataToFile();
                    } else {
                        currentProvinceIndex = 0;
                        isMultipleSearch = true;
                        saveProperties();
                        while (continueWork || currentProvinceIndex < 13) {
                            prepareURL(currentPageNumber);
                            saveProperties();
                            int pages = countPages();
                            for (int i = 2; i <= pages; i++) {
                                Thread.sleep(connectionTimeout);
                                prepareURL(i);
                                Document doc = scrapePage(currentURL);
                                parseCurrentPage(doc);
                            }
                            saveDataToFile();
                            storage.List.clear();
                            itemsCount = 0;
                            currentProvinceIndex++;
                        }
                    }
                    //TODO: Run search if application have been closed and opened again
                    //TODO: Format CSV
                } catch (InterruptedException ex) {
                    Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
                }
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
            }
        };
        thread.start();
    }

    private void parseCurrentPage(Document doc) {
        if (doc == null) {
            System.out.println("Return: parseCurrentPage");
            return;
        }
        int itemIndex = 1;
        boolean continueWork = true;
        try {
            Element item;
            while (continueWork) {
                item = getItemOnPage(doc, itemIndex);
                String webSiteItemSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child(" + itemIndex + ") > div > div.listing__mlr__root > ul > li.mlr__item.mlr__item--website > a";
                if (item == null) {
                    if (itemIndex > 40 || storage.List.size() == itemsCount) {
                        break;
                    }
                    itemIndex++;
                    continue;
                }
                String title = processTitle(item);  
                String address = scrapeAddress(item);
                String link = scrapeLink(doc.select(webSiteItemSelector));
                itemIndex++;
                storage.List.add(new ScrapedItem(title, address.replace("Get directions", ""), processLink(link)));
                System.out.println(storage.List.size());
            }
        } catch (Exception ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String processTitle(Element item) {
        String result = "";
        Element titleElement = (Element) item.childNode(3).childNode(1).childNode(1).childNode(1);
        if (titleElement.text().equalsIgnoreCase("")) {
            titleElement = (Element) item.childNode(3).childNode(1).childNode(1);
            result = titleElement.attr("title");
            return result;
        }
        int size = titleElement.text().length();
        if (size == 1) {
            titleElement = (Element) item.childNode(3).childNode(1).childNode(1).childNode(3);
            result = titleElement.text();
            return result;
        }
        return result;
    }
    
    private String processLink(String link){
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
    
    private Element getItemOnPage(Document doc, int itemIndex) {
        Element item;
        String itemSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child(" + itemIndex + ") > div > div.listing__content__wrapper > div.listing__content__wrap--flexed";
        String secondItemSelector = "#ypgBody > div.page__container.jsTabsContent.page__container--right-sidebar.hasMap.margin-top-20 > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div:nth-child(2) > div:nth-child(" + itemIndex + ") > div > div.listing__content__wrapper > div.listing__content__wrap--flexed";
        item = doc.select(itemSelector).first();
        if (item == null) {
            item = doc.select(secondItemSelector).first();
        }

        return item;
    }

    private String scrapeAddress(Element element) {
        Element addresElement = (Element) element.childNode(3);
        String resultString;
        if (addresElement.childNodeSize() > 3) {
            addresElement = (Element) addresElement.childNode(3);
            resultString = addresElement.text();
        } else {
            resultString = "";
        }
        return resultString;
    }

    private String scrapeLink(Elements elements) {
        String resultString = elements.attr("href");
        return resultString;
    }

    private Document scrapePage(String URL) {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer(CAMainURL).get();
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private int countPages() {
        Document doc = null;
        try {
            doc = Jsoup.connect(currentURL).get();
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (doc == null) {
            return 0;
        }
        String countSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.contentControls.listing-summary > div.contentControls__left > h1 > span > strong";
        String count = doc.select(countSelector).first().html().replace(",", "");

        float fcount = Float.parseFloat(count);
        int icount = Integer.parseInt(count);
        itemsCount = icount;

        float floatPages = (float) (fcount / 40.0);
        int intPages = icount / 40;

        parseCurrentPage(doc);

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
            province = provinces[currentProvinceIndex];
            url += "/" + province;
        } else {
            url += "/" + province;
        }
        currentURL = url;
    }

    private void saveDataToFile() {
        StringBuilder sb = new StringBuilder();
        //Headers
        sb.append("Link");
        sb.append(',');
        sb.append("Name");
        sb.append(',');
        sb.append("Address");
        sb.append('\n');

        //Data
        for (int i = 0; i < storage.List.size(); i++) {
            sb.append(storage.List.get(i).Link);
            sb.append(',');
            sb.append(storage.List.get(i).Name);
            sb.append(',');
            sb.append("\""+storage.List.get(i).Address+"\"");
            sb.append('\n');
        }
        try {
            Path path = Paths.get(parent.getlblOutputPathData().getText() + separator + business.replace(" ", "") + "_" + province + ".csv");
            Files.write(path, sb.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
