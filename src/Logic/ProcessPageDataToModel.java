package Logic;

import Services.LoggerService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ProcessPageDataToModel {

    public ProcessPageDataToModel() {
        storage = new ArrayList<>();
    }
    private ArrayList<ScrapedItem> storage;
    public synchronized ArrayList<ScrapedItem> parseCurrentPage(Document doc, String province) {
        if (doc == null) {
            LoggerService.logMessage("Scraped document is null");
            return storage;
        }
        Elements resultList = doc.select("div.resultList");
        if (resultList.size() == 0) {
            return storage;
        }
        Element items = (Element) resultList.first().childNode(1);
        Elements els = items.select("div.listing");
        for (Element el : els) {
            String title = el.select("h3.listing__name").select("a.listing__link").text();
            String link = processLink(el.select("li.mlr__item--website").select("a").attr("href"));
            String address = el.select("div.listing__address").select("span").text();
            storage.add(new ScrapedItem(title, address.replace("Get directions", ""), processLink(link), province));
        }
        return storage;
    }

    private synchronized String processLink(String link) {
        String result = "";
        if (link == null) {
            return result;
        }
        try {
            String res = java.net.URLDecoder.decode(link, "UTF-8");
            result = res.substring(res.indexOf("redirect=") + 1);
            result = result.replace("edirect=", "");
        } catch (UnsupportedEncodingException ex) {
            LoggerService.logException(ex);
        }
        return result;
    }
}
