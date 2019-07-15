package Actions;

import GUI.YPScraper;
import Services.DIResolver;

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
        FileDialog dialog = new FileDialog(YPScraper.this, "Select File to Open");
        dialog.setVisible(true);
        if (dialog.getFile() != null && !dialog.getFile().equalsIgnoreCase("") && dialog.getFile().toLowerCase().endsWith(".csv")) {
            inputLocationsFile = new File(dialog.getDirectory() + dialog.getFile());
            getlblPostalCodesPathData().setText(inputLocationsFile.getName());
            logic.getPostalCodes(inputLocationsFile.getAbsolutePath());
            logic.saveProperties();
        }
    }
}