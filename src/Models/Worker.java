package Models;

import Logic.ProcessPageDataToModel;
import Logic.ScrapedItem;
import Services.DIResolver;
import Services.LoggerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Worker implements Runnable {

    private ArrayList<ScrapedItem> storage;
    private String postalCode;
    private String business;
    private DIResolver diResolver;

    public Worker(DIResolver diResolver, String postalCode, String business) {
        this.diResolver = diResolver;
        this.postalCode = postalCode;
        this.business = business;
    }

    @Override
    public void run() {
        storage = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            String url = null;
            try {
                url = "https://www.yellowpages.ca/search/si/" + i + "/" + URLEncoder.encode(business, "UTF-8") + "/"+ URLEncoder.encode(postalCode, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Document doc = scrapePage(url);
            addItemsToStorage(new ProcessPageDataToModel().parseCurrentPage(doc, postalCode));
        }
        diResolver.getFilesService().saveDataToFile(storage);
    }

    private synchronized void addItemsToStorage(ArrayList<ScrapedItem> items) {
        storage.addAll(items);
    }
    private Document scrapePage(String url) {
        Document doc = null;
        try {
            doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(0)
                    .get();
        } catch (Exception ex) {
            LoggerService.logException(ex);
        }
        return doc;
    }
}


