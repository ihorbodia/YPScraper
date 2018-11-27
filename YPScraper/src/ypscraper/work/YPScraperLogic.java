/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ypscraper.work;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.text.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ypscraper.YPScraper;

/**
 *
 * @author Ihor
 */
public class YPScraperLogic {
    
        private static String[] provinces = {"AB", "BC", "MB", "NB", "NL", "NT", "NS", "NU", "ON", "PE", "QC", "SK", "YT"};
        private static String business;
        ExecutorService executorService;
        int executionTimoeout;
        
        private YPScraper parent;
        
        
    public YPScraperLogic(YPScraper parent)
    {
        this.parent = parent;
        executionTimoeout = 2000;
    }
    
    public void Run() throws IOException, InterruptedException, ExecutionException
    {
        executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2000);
                        
                        
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(YPScraperLogic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        future.get();
        
        Document doc = Jsoup.connect("https://www.yellowpages.ca/search/si/1/fashion%20retailer/ON").get();
        String str = doc.body().toString();
        Elements el1 = doc.select("#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child(1) > div > div.listing__content__wrapper > div.listing__content__wrap--flexed > div.listing__right.hasIcon > div.listing__title--wrap > h3 > a");
        Elements el2 = doc.select("#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child(2) > div > div.listing__content__wrapper > div.listing__content__wrap--flexed > div.listing__right.hasIcon > div.listing__title--wrap > h3 > a");
        System.out.println(el1.html());
        System.out.println(el2.html());
        Element link = (Element) doc.select("a").first();
    }
}
