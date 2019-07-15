package Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FolderUtils {
    public static String selectFolderDialog(Frame parent) {
        String osName = System.getProperty("os.name");
        String result = "";
        if (osName.equalsIgnoreCase("mac os x")) {
            FileDialog chooser = new FileDialog(parent, "Select folder");
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            chooser.setVisible(true);

            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            if (chooser.getDirectory() != null) {
                String folderName = chooser.getDirectory();
                folderName += chooser.getFile();
                result = folderName;
            }
        } else {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Target Folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int returnVal = chooser.showDialog(parent, "Select folder");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File userSelectedFolder = chooser.getSelectedFile();
                result = userSelectedFolder.getAbsolutePath();
            }
        }
        return result;
    }
}
