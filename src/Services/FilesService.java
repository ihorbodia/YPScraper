package Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FilesService {

    public File outputFolder;
    public File inputLocationsFile;
    private String separator = File.separator;

    FilesService() {

    }
    public void removeOldFileIfExists(String business, String province, String location) {
        if (outputFolder != null) {
            Path path = null;
            if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
                path = Paths.get(outputFolder.getAbsolutePath() + separator + business + "_" + province + ".csv");
            } else {
                path = Paths.get(outputFolder.getAbsolutePath() + separator + business + ".csv");
            }
            try {
                Files.deleteIfExists(path);
            } catch (IOException ex) {
                LoggerService.logException(ex);
            }
        }
    }

    void createEmptyOutputFile() {
        StringBuilder sb = new StringBuilder();
        Path path = null;

        if (!business.equalsIgnoreCase(parent.getTextFieldBusiness().getText())) {
            business = parent.getTextFieldBusiness().getText();
        }
        if (!province.equalsIgnoreCase(parent.getTextFieldLocation().getText())) {
            province = parent.getTextFieldLocation().getText();
        }

        if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
            path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + "_" + province + ".csv");
        } else {
            path = Paths.get(parent.outputFolder.getAbsolutePath() + separator + business + ".csv");
        }

        sb.append("Link");
        sb.append(',');
        sb.append("\"Name\"");
        sb.append(',');
        sb.append("\"Address\"");
        sb.append(',');
        sb.append("\"Location\"");
        sb.append('\n');
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LoggerService.logException(ex);
        }
    }

}
