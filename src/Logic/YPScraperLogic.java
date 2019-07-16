package Logic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Services.*;
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

    private String business;
    private String currentURL;
    boolean continueWork = false;
    private String province;
    private String separator = File.separator;

    private ScrapedItemsStorage storage;
    private boolean isMultipleSearch;
    boolean running;
    private int postalCodeIndex;
    private int scrapedItemsCount;

    private InputStream input = null;

    private Future<?> future;
    private DIResolver diResolver;

    public YPScraperLogic() {
        this.diResolver = new DIResolver();
    }

    public void Run(boolean isStartRun) {
        GuiService guiService = diResolver.getGuiService();
        PropertiesService propertiesService = diResolver.getPropertiesService();
        FilesService filesService = diResolver.getFilesService();

        storage = new ScrapedItemsStorage(business, province);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(() -> {
            scrapedItemsCount = 0;
            continueWork = true;
            running = true;
            if (province.equalsIgnoreCase("")) {
                if (isStartRun) {
                    postalCodeIndex = 0;
                }
                if (parent.inputLocationsFile == null) {
                    JOptionPane.showMessageDialog(null, "Input CSV file not specified, program cannot continue.", "Select input locations list", JOptionPane.ERROR_MESSAGE);
                    continueWork = false;
                    running = false;
                    return;
                }
                String[] postalCodes = filesService.getPostalCodes(parent.inputLocationsFile.getAbsolutePath());
                isMultipleSearch = true;
                while (continueWork) {
                    prepareURL(1);
                    int pages = countPages();
                    guiService.updateMultipleSearchGUI(false, postalCodeIndex, postalCodes.length, storage.List.size());
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
                    propertiesService.saveProperties();
                }
                guiService.updateMultipleSearchGUI(false, postalCodeIndex, postalCodes.length, storage.List.size());
                saveDataToFile();
            } else {
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
            }
            continueWork = false;
            running = false;
            propertiesService.saveProperties();
        });
        Thread thread = new Thread(() -> {
            parent.getBtnStart().setEnabled(false);
            parent.getBtnStop().setEnabled(true);
            parent.getBtnOutputPath().setEnabled(false);
            parent.getBtnChooseCSVPostaCodesPath().setEnabled(false);
            while (true) {
                if (future.isDone()) {
                    System.out.println("Program: Finished");
                    LoggerService.logMessage("Program: Finished");
                    break;
                }
            }
            parent.getBtnStart().setEnabled(true);
            parent.getBtnStop().setEnabled(false);
            parent.getBtnOutputPath().setEnabled(true);
            parent.getBtnChooseCSVPostaCodesPath().setEnabled(true);
        });
        thread.start();
    }


    private void parseCurrentPage(Document doc) {
        if (doc == null) {
            LoggerService.logMessage("Scraped document is null");
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
        LoggerService.logMessage(storage.List.size() +" Location: "+ province +" Link: "+ currentURL);
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
            LoggerService.logException(ex);
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
            LoggerService.logException(ex);
            LoggerService.logMessage(String.valueOf(storage.List.size() +" Location: "+ province +" Link: "+ currentURL));
        }
        return doc;
    }

    private int countPages() {
        Document doc = scrapePage();
        Element countElement = doc.select("span.pageCount").select("span").last();
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
        String CABaseURLPart = "https://www.yellowpages.ca/search/si";
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


    void saveDataToFile() {
        try {
            Path path;
            if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
                path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + "_" + province + ".csv");
            } else {
                path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + ".csv");
            }
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                createOutputFile();
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < storage.List.size(); i++) {
                sb.setLength(0);
                sb.append(storage.List.get(i).Link);
                sb.append(',');
                sb.append("\"").append(storage.List.get(i).Name).append("\"");
                sb.append(',');
                sb.append("\"").append(storage.List.get(i).Address).append("\"");
                sb.append(',');
                sb.append("\"").append(storage.List.get(i).Location).append("\"");
                sb.append('\n');
            }
            FileWriter writer = new FileWriter(path.toFile(), true);
            writer.append(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            LoggerService.logException(ex);
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


}
