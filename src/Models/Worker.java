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
    private int currentPage = 0;

    public Worker(DIResolver diResolver, String business, String postalCode) {
        this.diResolver = diResolver;
        this.postalCode = postalCode;
        this.business = business;
    }

    @Override
    public synchronized void run() {
        storage = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            currentPage = i;
            String url = null;
            try {
                url = "https://www.yellowpages.ca/search/si/" + i + "/"+ URLEncoder.encode(business, "UTF-8") + "/" +URLEncoder.encode(postalCode, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Document doc = scrapePage(url);
            addItemsToStorage(new ProcessPageDataToModel().parseCurrentPage(doc, postalCode));
        }
        diResolver.getFilesService().saveDataToFile(storage);
    }

    private synchronized void addItemsToStorage(ArrayList<ScrapedItem> items) {
        for (ScrapedItem item: items) {
            if (storage.stream().noneMatch(x ->
                    x.Address.equalsIgnoreCase(item.Address) ||
                    x.Link.equalsIgnoreCase(item.Link) ||
                    x.Location.equalsIgnoreCase(item.Location) ||
                    x.Name.equalsIgnoreCase(item.Name))) {
                storage.add(item);
            }
        }
    }

    private Document scrapePage(String url) {
        Document doc = null;
        for (int i = 0; i < 5; i++) {
            try {
                doc = Jsoup
                        .connect(url)
                        .userAgent("Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)")
                        .timeout(30*1000)
                        .get();
                break;
            } catch (Exception ex) {
                LoggerService.logException(ex);
            }
        }
        return doc;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}


