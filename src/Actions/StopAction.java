package Actions;

import Services.DIResolver;
import Services.LoggerService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopAction implements ActionListener {
    private DIResolver diResolver;
    public StopAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        diResolver.getBaseSearchStrategy().stopProcessing();
    }
}