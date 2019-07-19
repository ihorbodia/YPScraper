package Actions;

import Services.DIResolver;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SetOutputPathAction implements ActionListener {
    private DIResolver diResolver;
    public SetOutputPathAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("SetOutputAction");

        String path = diResolver.getGuiService().getDialog();
        if (!path.equalsIgnoreCase("")) {
            File outputFolder = new File(path);
            diResolver.getGuiService().getlblOutputPathData().setText(outputFolder.getName());
            diResolver.getPropertiesService().saveOutputFolder(outputFolder);
        }
    }
}