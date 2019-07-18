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

    private DIResolver diResolver;
    public ApplicationStartedAction() {
        diResolver = new DIResolver();
        actionPerformed(new ActionEvent(this, 0, null));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FilesService filesService = diResolver.getFilesService();
        GuiService guiService = diResolver.getGuiService();
        PropertiesService propertiesService = diResolver.getPropertiesService();

        AppPropertiesModel appPropertiesModel = propertiesService.restoreProperties();
        guiService.guiRestoreByProperties(appPropertiesModel);

        if(filesService.getOutputFolder() == null) {
            File f = new File(".");
            filesService.setOutputFolder(new File(f.getAbsolutePath()).getParentFile());
            guiService.getlblOutputPathData().setText(filesService.getOutputFolder().getName());
        }

        if (appPropertiesModel.running) {
            StartAction startAction = new StartAction();
            startAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }


    }
}
