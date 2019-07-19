package Actions;

import Models.AppPropertiesModel;
import Services.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ApplicationStartedAction implements ActionListener {

    private DIResolver diResolver;
    public ApplicationStartedAction() {
        diResolver = new DIResolver();
        actionPerformed(new ActionEvent(this, 0, null));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GuiService guiService = diResolver.getGuiService();
        PropertiesService propertiesService = diResolver.getPropertiesService();

        AppPropertiesModel appPropertiesModel = propertiesService.restoreProperties();
        guiService.guiRestoreByProperties(appPropertiesModel);

        InitOutputFolder(appPropertiesModel);
        InitLocationsInputFile(appPropertiesModel);

        if (appPropertiesModel.running) {
            StartAction startAction = new StartAction();
            startAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }
    }

    private void InitLocationsInputFile(AppPropertiesModel appPropertiesModel) {
        if(appPropertiesModel.inputLocationsFile.exists()) {
            diResolver.getFilesService().setInputLocationsFile(appPropertiesModel.inputLocationsFile);
        } else {
            LoggerService.logMessage("Select locations file please");

        }
        File inputFile = diResolver.getFilesService().getInputLocationsFile();
        diResolver.getGuiService().getlblPostalCodesPathData().setText(inputFile == null ? "" : inputFile.getName());
    }

    private void InitOutputFolder(AppPropertiesModel appPropertiesModel) {
        if(diResolver.getFilesService().getOutputFolder() == null && !appPropertiesModel.outputFolder.exists()) {
            File f = new File(".");
            diResolver.getFilesService().setOutputFolder(new File(f.getAbsolutePath()).getParentFile());
        } else {
            diResolver.getFilesService().setOutputFolder(appPropertiesModel.outputFolder);
        }
        File outputFolder = diResolver.getFilesService().getOutputFolder();
        diResolver.getGuiService().getlblOutputPathData().setText(outputFolder == null ? "" :outputFolder.getName());
    }
}
