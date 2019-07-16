package Actions;

import Models.AppPropertiesModel;
import Services.DIResolver;
import Services.FilesService;
import Services.GuiService;
import Services.PropertiesService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ApplicationStartedAction implements ActionListener {

    DIResolver diResolver;
    public ApplicationStartedAction() {
        diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FilesService filesService = diResolver.getFilesService();
        GuiService guiService = diResolver.getGuiService();
        PropertiesService propertiesService = diResolver.getPropertiesService();

        AppPropertiesModel appPropertiesModel = propertiesService.restoreProperties();
        guiService.guiRestoreByProperties(appPropertiesModel);

        if (appPropertiesModel.running) {
            diResolver.getYpScraperLogic().Run(false);
        }

        if(filesService.getOutputFolder() == null) {
            File f = new File(".");
            filesService.setOutputFolder(new File(f.getAbsolutePath()).getParentFile());
            guiService.getlblOutputPathData().setText(filesService.getOutputFolder().getName());
        }
    }
}
