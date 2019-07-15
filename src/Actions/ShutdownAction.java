package Actions;

import Services.DIResolver;
import Services.LoggerService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShutdownAction implements ActionListener {

    DIResolver diResolver;
    public ShutdownAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            diResolver.getPropertiesService().saveProperties();
            if (ypScraperLogic.future != null && !logic.future.isDone()) {
                ypScraperLogic.saveDataToFile();
            }
        }));
    }
}