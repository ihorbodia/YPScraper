package Actions;

import Services.DIResolver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopAction implements ActionListener {
    DIResolver diResolver;
    public StopAction() {
        this.diResolver = new DIResolver();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Stop");
        if (logic != null) {
            if (logic.future != null) {
                logic.future.cancel(true);
            }
            logic.running = false;
            logic.continueWork = false;
            logic.saveDataToFile();
        }
        logic.saveProperties();
    }
}