package Services;

import Logic.ScrapedItem;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class FilesService {

    private File outputFolder;
    private File inputLocationsFile;
    private String separator = File.separator;
    private String[] postalCodes = null;

    FilesService() {

    }

     public synchronized void saveDataToFile(ArrayList<ScrapedItem> items) {
        try {
            StringBuilder sb = new StringBuilder();
            for (ScrapedItem item : items) {
                sb.setLength(0);
                sb.append(item.Link);
                sb.append(',');
                sb.append("\"").append(item.Name).append("\"");
                sb.append(',');
                sb.append("\"").append(item.Address).append("\"");
                sb.append(',');
                sb.append("\"").append(item.Location).append("\"");
                sb.append('\n');
            }
            FileWriter writer = new FileWriter(outputFolder, true);
            writer.append(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            LoggerService.logException(ex);
        }
    }

    public ArrayList<String> getPostalCodes() {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String> result = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(inputLocationsFile));
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] country = line.split(cvsSplitBy);
                result.add(country[0]);
            }
            return result;
        } catch (FileNotFoundException e) {
            LoggerService.logException(e);
            JOptionPane.showMessageDialog(null, "Something wrong with input CSV file. Try to check path and start again.", "Input csv file problem", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            LoggerService.logException(e);
            JOptionPane.showMessageDialog(null, "Something wrong with input CSV file.", "Input csv file problem", JOptionPane.ERROR_MESSAGE);
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
        return result;
    }

    public void removeOldFileIfExists() {
        if (getOutputFolder() != null && getOutputFolder().exists()) {
            outputFolder.delete();
        }
    }

    public void createEmptyOutputFile(String business, String location) {
        StringBuilder sb = new StringBuilder();
        Path path = null;

        if (location.equalsIgnoreCase("")) {
            path = Paths.get(outputFolder.getAbsolutePath() + separator + business + ".csv");
        } else {
            path = Paths.get(outputFolder.getAbsolutePath() + separator + business + "_" + location + ".csv");
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
