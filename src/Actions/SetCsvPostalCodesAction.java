package Actions;

import Services.DIResolver;
import Services.FilesService;
import Services.GuiService;
import Services.PropertiesService;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SetCsvPostalCodesAction implements ActionListener {
    private DIResolver diResolver;
    public SetCsvPostalCodesAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Set postal codes action raised");
        GuiService guiService = diResolver.getGuiService();
        PropertiesService propertiesService = diResolver.getPropertiesService();
        FilesService filesService = diResolver.getFilesService();

        FileDialog dialog = new FileDialog(guiService.getMainWindow());
        dialog.setVisible(true);
        if (dialog.getFile() != null && !dialog.getFile().equalsIgnoreCase("") && dialog.getFile().toLowerCase().endsWith(".csv")) {
            File inputLocationsFile = new File(dialog.getDirectory() + dialog.getFile());
            filesService.setInputLocationsFile(inputLocationsFile);
            guiService.getlblPostalCodesPathData().setText(inputLocationsFile.getName());
            propertiesService.saveLocationsFileLocation(inputLocationsFile);
        }
    }
}