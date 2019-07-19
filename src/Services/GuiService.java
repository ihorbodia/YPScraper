package Services;

import GUI.WindowHandler;
import GUI.YPScraper;
import Models.AppPropertiesModel;
import Utils.FolderUtils;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GuiService {

    private static YPScraper mainWindow;

    public YPScraper getMainWindow() {
        return mainWindow;
    }

    GuiService() {
        if (mainWindow == null) {
            mainWindow = new YPScraper();
            mainWindow.initActions();
            mainWindow.pack();
            mainWindow.setSize(new Dimension(600, 210));
            mainWindow.setResizable(false);
            mainWindow.setVisible(true);
            WindowHandler h = WindowHandler.getInstance();
            LogRecord r = new LogRecord(Level.WARNING, "Start logger...");
            h.publish(r);
        }
    }

    public void updateStatusTextByFileProcessing(int percent) {
        getTextFieldStatus().setText("Processed "+ percent + " % of locations from file");
    }

    public void updateStatusTextBySingleSearch(int currentPage) {
        getTextFieldStatus().setText("Processed "+currentPage+"/50 pages");
    }

    public String getDialog() {
        return FolderUtils.selectFolderDialog(mainWindow);
    }

    public void guiRestoreByProperties(AppPropertiesModel appPropertiesModel) {
        mainWindow.getTextFieldBusiness().setText(appPropertiesModel.business);
        mainWindow.getTextFieldLocation().setText(appPropertiesModel.province);
        mainWindow.getlblOutputPathData().setText(appPropertiesModel.outputFolder.getName());
        mainWindow.getlblPostalCodesPathData().setText(appPropertiesModel.inputLocationsFile.getName());
    }

    public void changeApplicationState(boolean isWork) {
       mainWindow.getBtnStart().setEnabled(!isWork);
       mainWindow.getBtnStop().setEnabled(isWork);
       mainWindow.getBtnOutputPath().setEnabled(!isWork);
       mainWindow.getBtnChooseCSVPostaCodesPath().setEnabled(!isWork);
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
