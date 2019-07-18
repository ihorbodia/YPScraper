package Actions;

import Services.DIResolver;
import Utils.FolderUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SetOutputPathAction implements ActionListener {
    DIResolver diResolver;
    public SetOutputPathAction() {
        this.diResolver = new DIResolver();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("SetOutputAction");

        String path = diResolver.getGuiService().getDialog().getDirectory();
        if (!path.equalsIgnoreCase("")) {
            File outputFolder = new File(path);
            diResolver.getGuiService().getlblOutputPathData().setText(outputFolder.getName());
        }
    }
}