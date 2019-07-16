package Services;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class FilesService {

    private File outputFolder;
    private File inputLocationsFile;
    private String separator = File.separator;
    private String[] postalCodes = null;

    FilesService() {

    }

    public void getPostalCodes(String path) {
        String csvFile = path;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String> result = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] country = line.split(cvsSplitBy);
                result.add(country[0]);
            }
            String[] postalCodes = result.toArray(new String[0]);
        } catch (FileNotFoundException e) {
            LoggerService.logException(e);
            JOptionPane.showMessageDialog(null, "Something wrong with input CSV file. Try to check path and start again.", "Input csv file problem", JOptionPane.ERROR_MESSAGE);
            continueWork = false;
        } catch (IOException e) {
            LoggerService.logException(e);
            JOptionPane.showMessageDialog(null, "Something wrong with input CSV file.", "Input csv file problem", JOptionPane.ERROR_MESSAGE);
            continueWork = false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    LoggerService.logException(e);
                }
            }
        }
    }

    public void removeOldFileIfExists(String business, String province, String location) {
        if (getOutputFolder() != null) {
            Path path = null;
            if (!parent.getTextFieldLocation().getText().equalsIgnoreCase("")) {
                path = Paths.get(getOutputFolder().getAbsolutePath() + separator + business + "_" + province + ".csv");
            } else {
                path = Paths.get(getOutputFolder().getAbsolutePath() + separator + business + ".csv");
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

    public File getOutputFolder() {
        return outputFolder;
    }

    public void  setOutputFolder(File folder) {
        if (folder != null) {
            outputFolder = folder;
        }
    }

    public File getInputLocationsFile() {
        return inputLocationsFile;
    }
}
