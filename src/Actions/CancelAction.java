package Actions;

import Services.DIResolver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CancelAction implements ActionListener {

    DIResolver diResolver;
    public CancelAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (logic != null) {
            logic.future.cancel(true);
            getBtnStop().setEnabled(false);
        }
    }
}