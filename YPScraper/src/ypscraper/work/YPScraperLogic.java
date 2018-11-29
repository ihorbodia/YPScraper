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
import java.util.Arrays;
import java.util.Properties;
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
    private int currentProvinceIndex;
    private int pagesCount;
    private String CABaseURLPart = "https://www.yellowpages.ca/search/si";
    private String CAMainURL = "https://www.yellowpages.ca";
    private String province;
    String separator = File.separator;
    ExecutorService executorService;
    int connectionTimeout;
    File propertiesFile;
    ScrapedItemsStorage storage;

    String firstPage;
    InputStream input = null;
    OutputStream output = null;
    private YPScraper parent;

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
            parent.properties.setProperty("province", province);
            parent.properties.setProperty("connTimeout", Integer.toString(connectionTimeout));
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
        saveProperties();
        storage = new ScrapedItemsStorage(business, province);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            boolean continueWork = true;
            public void run() {
                while (continueWork) {
                    try {
                        Thread.sleep(connectionTimeout);
                        currentURL = prepareFirstURL();
                        int pages = countPages();
                        for (int i = 2; i <= pages; i++) {
                            Document doc = scrapePage(prepareURL(i));
                            parseCurrentPage(doc);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

    }
    
    private void parseCurrentPage(Document doc) {
        if (doc == null) {
            return;
        }
        int itemIndex = 2;
        boolean continueWork = true;
        while (continueWork) {
            String nameSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child("+itemIndex+") > div > div.listing__content__wrapper > div.listing__content__wrap--flexed > div.listing__right.hasIcon > div.listing__title--wrap > h3 > a";
            String addressSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child("+itemIndex+") > div > div.listing__content__wrapper > div.listing__content__wrap--flexed > div.listing__right.hasIcon > div.listing__address.address.mainLocal.noNum > span";
            String webPageSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child("+itemIndex+") > div > div.listing__mlr__root > ul > li.mlr__item.mlr__item--website > a";

            String name = doc.select(nameSelector).first().text();
            String address = scrapeAddress(doc.select(addressSelector));
            String link = scrapeLink(doc.select(webPageSelector));
            if (name == null) {
                continueWork = false;
                break;
            }
            storage.List.add(new ScrapedItem(name, address, link));
            itemIndex++;
        }
    }
    
    private String scrapeAddress(Elements elements) {
        String resultString = elements.first().text();
        return resultString;
    }
    
    private String scrapeLink(Elements elements){
        String resultString = CAMainURL + elements.attr("href");
        return resultString;
    }
    
    private Document scrapePage(String URL) {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL).get();
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private int countPages() {
        Document doc = null;
        try {
            doc = Jsoup.connect(prepareFirstURL()).get();
        } catch (IOException ex) {
            Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (doc == null) {
            return 0;
        }
        String countSelector = "#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.contentControls.listing-summary > div.contentControls__left > h1 > span > strong";
        String count = doc.select(countSelector).first().html();
        
        float fcount = Float.parseFloat(count);
        int icount = Integer.parseInt(count);
        
        float floatPages = (float) (fcount / 40.0);
        int intPages = icount / 40;
        
        parseCurrentPage(doc);
        
        if (floatPages > intPages) {
            return intPages + 1;
        }
        else {
            return intPages;
        }
    }

    private String prepareFirstURL() {
        String url;
        String[] splited = business.split("\\s+");
        url = CABaseURLPart + "/1/";
        if (splited.length > 1) {
            for (int i = 0; i < splited.length; i++) {
                url += splited[i];
                if (i < splited.length - 1) {
                    url += "%20";
                }
            }
        } else {
            url = splited[0];
        }
        url += "/" + province;

        return url;
    }

    private String prepareURL(int pageNumber) {
        String url;
        String[] splited = business.split("\\s+");
        url = CABaseURLPart + "/" + pageNumber + "/";
        if (splited.length > 1) {
            for (int i = 0; i < splited.length; i++) {
                url += splited[i];
                if (i < splited.length - 1) {
                    url += "%20";
                }
            }
        } else {
            url = splited[0];
        }
        url += "/" + province;

        return url;
    }
}
