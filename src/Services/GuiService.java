package Services;

import GUI.WindowHandler;
import GUI.YPScraper;
import Models.AppPropertiesModel;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GuiService {

    private YPScraper mainWindow;
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

    public FileDialog getDialog() {
        return new FileDialog(mainWindow, "Select File to Open");
    }

    public void guiRestoreByProperties(AppPropertiesModel appPropertiesModel) {
        mainWindow.getTextFieldBusiness().setText(appPropertiesModel.business);
        mainWindow.getTextFieldLocation().setText(appPropertiesModel.province);
        mainWindow.getlblOutputPathData().setText(appPropertiesModel.outputFolder.getName());
        mainWindow.getlblPostalCodesPathData().setText(appPropertiesModel.inputLocationsFile.getName());
    }

    public void updateOneLocationSearchGUI(boolean isFinished, String scrapedItemsCount) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                if (isFinished) {
                    mainWindow.getTextFieldStatus().setText("Finished. "+scrapedItemsCount + " items scraped.");
                }
                else
                {
                    mainWindow.getTextFieldStatus().setText(scrapedItemsCount + " items scraped.");
                }
            });
        }
    }

    public void updateMultipleSearchGUI(boolean isFinished, final int postalCodeIndex, int postalCodesLength, int scrapedItemsCount) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                if (postalCodeIndex <= postalCodesLength) {
                    int postalCodeItem = postalCodeIndex + 1;
                    mainWindow.getTextFieldStatus().setText(postalCodeItem + "/" + postalCodesLength + " locations processed. " + scrapedItemsCount + " items scraped.");
                }
                if (isFinished) {
                    mainWindow.getTextFieldStatus().setText("Finished. " + (postalCodeIndex - 1)  + "/" + postalCodesLength + " locations processed. " + scrapedItemsCount + " items scraped.");
                }
            });
        }
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
