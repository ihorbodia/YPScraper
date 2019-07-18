package Actions;

import Logic.MultipleSearchStrategy;
import Logic.ScrapedItem;
import Logic.ScrapedItemsStorage;
import Models.AppPropertiesModel;
import Services.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StartAction implements ActionListener {

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

    public StartAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GuiService guiService = diResolver.getGuiService();
        FilesService filesService = diResolver.getFilesService();
        PropertiesService propertiesService = diResolver.getPropertiesService();

        System.out.println("Start");
        LoggerService.logMessage("Starting...");
        guiService.getTextFieldStatus().setText("Starting...");

        AppPropertiesModel appPropertiesModel = diResolver.getPropertiesService().restoreProperties();

        filesService.removeOldFileIfExists();
        filesService.createEmptyOutputFile(appPropertiesModel.business, appPropertiesModel.province);
        Run();
    }

    private void Run() {
        GuiService guiService = diResolver.getGuiService();

        MultipleSearchStrategy multipleSearchStrategy = new MultipleSearchStrategy(diResolver);
        multipleSearchStrategy.processData();

        Thread thread = new Thread(() -> {
            guiService.changeApplicationState(true);
            while (true) {
                if (future.isDone()) {
                    System.out.println("Program: Finished");
                    LoggerService.logMessage("Program: Finished");
                    break;
                }
            }
            guiService.changeApplicationState(false);
        });
        thread.start();
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
            diResolver.getLoggerService().logException(ex);
            diResolver.getLoggerService().logMessage(String.valueOf(storage.List.size() +" Location: "+ province +" Link: "+ currentURL));
        }
        return doc;
    }
}