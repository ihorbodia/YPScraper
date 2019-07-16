package Actions;

import Models.AppPropertiesModel;
import Services.DIResolver;
import Services.GuiService;
import Services.PropertiesService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SetCsvPostalCodesAction implements ActionListener {
    DIResolver diResolver;
    public SetCsvPostalCodesAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Set postal codes action raised");
        GuiService guiService = diResolver.getGuiService();
        PropertiesService propertiesService = diResolver.getPropertiesService();

        FileDialog dialog = guiService.getDialog();
        dialog.setVisible(true);
        if (dialog.getFile() != null && !dialog.getFile().equalsIgnoreCase("") && dialog.getFile().toLowerCase().endsWith(".csv")) {
            File inputLocationsFile = new File(dialog.getDirectory() + dialog.getFile());
            guiService.getlblPostalCodesPathData().setText(inputLocationsFile.getName());
            propertiesService.saveLocationsFileLocation(inputLocationsFile);
        }
    }
}