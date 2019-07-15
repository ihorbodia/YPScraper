package Actions;

import Services.DIResolver;
import Services.LoggerService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartAction implements ActionListener {

    DIResolver diResolver;
    public StartAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Start");
        LoggerService.logMessage("Starting...");
        getTextFieldStatus().setText("Starting...");
        logic.removeOldFileIfExists();
        logic.createOutputFile();
        logic.Run(true);
        logic.saveProperties();
    }
}