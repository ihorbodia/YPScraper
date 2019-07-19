package Actions;

import Logic.SearchStrategies.BaseSearchStrategy;
import Logic.SearchStrategies.MultipleSearchStrategy;
import Logic.SearchStrategies.SingleSearchStrategy;
import Services.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartAction implements ActionListener {
    private DIResolver diResolver;
    private GuiService guiService;
    private FilesService filesService;
    private PropertiesService propertiesService;

    public StartAction() {
        diResolver = new DIResolver();
        guiService = diResolver.getGuiService();
        propertiesService = diResolver.getPropertiesService();
        filesService = diResolver.getFilesService();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        propertiesService.saveBusiness(guiService.getTextFieldBusiness());
        propertiesService.saveLocation(guiService.getTextFieldLocation());


        Run();
    }

    private BaseSearchStrategy getStrategy() {
        BaseSearchStrategy result = null;

        if (!guiService.getTextFieldBusiness().equalsIgnoreCase("") && !guiService.getTextFieldLocation().equalsIgnoreCase("")) {
            result = new SingleSearchStrategy(diResolver);
        } else if ((filesService.getInputLocationsFile() != null && filesService.getInputLocationsFile().exists())
                && guiService.getTextFieldLocation().equalsIgnoreCase("")) {
            result = new MultipleSearchStrategy(diResolver);
        }
        return result;
    }

    private void Run() {
        GuiService guiService = diResolver.getGuiService();
        BaseSearchStrategy strategy = getStrategy();
        diResolver.setBaseSearchStrategy(strategy);
        filesService.removeOldFileIfExists();
        strategy.createEmptyCsvDataFile();
        strategy.processData();
        LoggerService.logMessage("Starting...");
        guiService.getTextFieldStatus().setText("Starting...");
    }
}