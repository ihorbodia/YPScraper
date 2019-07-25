package Services;

import Logic.ScrapedItem;
import com.opencsv.CSVWriter;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class FilesService {

    private File outputFolder;
    private File outputFile;
    private File inputLocationsFile;
    private String separator = File.separator;

    FilesService() {

    }

     public synchronized void saveDataToFile(ArrayList<ScrapedItem> items) {
        try {
            StringBuilder sb = new StringBuilder();
            for (ScrapedItem item : items) {
                sb.append(item.Link);
                sb.append(',');
                sb.append("\"").append(item.Name).append("\"");
                sb.append(',');
                sb.append("\"").append(item.Address).append("\"");
                sb.append(',');
                sb.append("\"").append(item.Location).append("\"");
                sb.append('\n');
            }
            FileWriter writer = new FileWriter(outputFile, true);
            writer.append(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            LoggerService.logException(ex);
        }
    }

    public ArrayList<String> getPostalCodes() {
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        ArrayList<String> result = new ArrayList<>();
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
        if (outputFile != null && getOutputFolder().exists()) {
            outputFile.delete();
        }
    }

    public void createEmptyCSVFile(String[] columns, String fileName) {
        try {
            if (outputFolder == null) {
                LoggerService.logMessage("Output folder not specified!");
                return;
            }
            outputFile = new File(outputFolder.getAbsolutePath() +separator+ fileName + ".csv");
            FileWriter mFileWriter;
            mFileWriter = new FileWriter(outputFile);
            CSVWriter mCsvWriter = new CSVWriter(mFileWriter);
            mCsvWriter.writeNext(columns);
            mCsvWriter.close();
            mFileWriter.close();
        } catch (Exception e) {
            LoggerService.logException(e);
        }
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File folder) {
        if (folder != null) {
            outputFolder = folder;
        }
    }

    public void setInputLocationsFile(File file) {
        if (file != null) {
            inputLocationsFile = file;
        }
    }

    public File getInputLocationsFile() {
        return inputLocationsFile;
    }
}
