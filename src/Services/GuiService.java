package Services;

import GUI.WindowHandler;
import GUI.YPScraper;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GuiService {

    YPScraper mainWindow;
    GuiService() {
        mainWindow = new YPScraper();
        mainWindow.pack();
        mainWindow.setSize(new Dimension(600, 210));
        mainWindow.setResizable(false);
        mainWindow.setVisible(true);
        WindowHandler h = WindowHandler.getInstance();
        LogRecord r = new LogRecord(Level.WARNING, "Start logger...");
        h.publish(r);
    }


    public JLabel getlblPostalCodesPathData() {
        return mainWindow.getlblPostalCodesPathData();
    }

    public JLabel getlblOutputPathData() {
        return mainWindow.getlblOutputPathData();
    }


    public JSpinner getTextFieldConnectionTimeout() {
        return mainWindow.getTextFieldConnectionTimeout();
    }

    public String getTextFieldBusiness() {
        return mainWindow.getTextFieldBusiness().getText();
    }

    public String getTextFieldLocation() {
        return mainWindow.getTextFieldLocation().getText();
    }

    public JLabel getTextFieldStatus() {
        return mainWindow.getTextFieldStatus();
    }

    public JProgressBar getProgressBar() {
        return mainWindow.getProgressBar();
    }

    public JButton getBtnStart() {
        return mainWindow.getBtnStart();
    }

    public JButton getBtnStop() {
        return mainWindow.getBtnStop();
    }

    public JButton getBtnOutputPath() {
        return mainWindow.getBtnOutputPath();
    }

    public JButton getBtnChooseCSVPostaCodesPath() {
        return mainWindow.getBtnChooseCSVPostaCodesPath();
    }

    public JButton getBtnCancel() {
        return mainWindow.getBtnCancel();
    }
}
