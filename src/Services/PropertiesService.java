package Services;

import Models.AppPropertiesModel;
import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.util.Properties;

class PropertiesService {

    private Properties properties = new Properties();
    private File propertiesFile;
    private String separator = File.separator;
    private OutputStream output = null;

    private void createNewPropertiesFile() {
        try {
            File propertiesFileTemp = File.createTempFile("config", ".properties");
            String propPath = FilenameUtils.getFullPathNoEndSeparator(propertiesFileTemp.getAbsolutePath()) + separator + "config.properties";
            File f = new File(propPath);
            if (f.exists() && !f.isDirectory()) {
                propertiesFile = f;
            } else {
                propertiesFile = f;
                f.createNewFile();
                output = new FileOutputStream(propertiesFile.getAbsoluteFile());
                properties.setProperty("business", "");
                properties.setProperty("province", "");
                properties.setProperty("connTimeout", "0");
                properties.setProperty("outputFolder", "");
                properties.setProperty("csvPostalCodesFile", "");
                properties.setProperty("running", "");
                properties.setProperty("postalCodeIndex", "0");
                properties.store(output, null);
            }
            propertiesFileTemp.delete();
        } catch (IOException io) {
            LoggerService.logException(io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException io) {
                    LoggerService.logException(io);
                }
            }
        }
    }

    void saveProperties(AppPropertiesModel appPropertiesModel, boolean isMultipleSearch) {
        try {
            output = new FileOutputStream(propertiesFile.getAbsoluteFile());
            properties.setProperty("business", parent.getTextFieldBusiness().getText());
            if (!isMultipleSearch) {
                properties.setProperty("province", parent.getTextFieldLocation().getText());
            }
            if (parent.outputFolder != null) {
                parent.properties.setProperty("outputFolder", parent.outputFolder.getAbsolutePath());
            }
            if (parent.inputLocationsFile != null) {
                parent.properties.setProperty("csvPostalCodesFile", parent.inputLocationsFile.getAbsolutePath());
            }

            parent.properties.setProperty("running", String.valueOf(running));
            parent.properties.setProperty("postalCodeIndex", Integer.toString(postalCodeIndex));
            parent.properties.store(output, null);
        } catch (IOException io) {
            LoggerService.logException(io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException io) {
                    LoggerService.logException(io);
                }
            }
        }
    }


    public AppPropertiesModel restoreProperties() {
        AppPropertiesModel appPropertiesModel = new AppPropertiesModel();
        try {
            createNewFile();
            input = new FileInputStream(propertiesFile.getAbsoluteFile());
            parent.properties.load(input);

            if (parent.properties.get("business") != null) {
                business = parent.properties.get("business").toString();
                parent.getTextFieldBusiness().setText(business);
            }

            if (parent.properties.get("province") != null) {
                province = parent.properties.get("province").toString();
                parent.getTextFieldLocation().setText(province);
            }

            if (parent.properties.get("outputFolder") != null) {
                String path = parent.properties.get("outputFolder").toString();
                if (!path.equalsIgnoreCase("")) {
                    parent.outputFolder = new File(path);
                    parent.getlblOutputPathData().setText(parent.outputFolder.getName());
                }
            }

            if (parent.properties.get("csvPostalCodesFile") != null) {
                String csvPath = parent.properties.get("csvPostalCodesFile").toString();
                if (!csvPath.equalsIgnoreCase("")) {
                    parent.inputLocationsFile = new File(csvPath);
                    parent.getlblPostalCodesPathData().setText(parent.inputLocationsFile.getName());
                }
            }

            if (parent.properties.get("postalCodeIndex") != null) {
                String postalCodeIndexStr = parent.properties.get("postalCodeIndex").toString();
                postalCodeIndex = Integer.parseInt(postalCodeIndexStr);
            }
            if (parent.properties.get("running") != null) {
                String runningStr = parent.properties.get("running").toString();
                running = Boolean.parseBoolean(runningStr);
            }

            if (running) {
                continueRun();
            }
        } catch (IOException io) {
            LoggerService.logException(io);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException io) {
                    LoggerService.logException(io);
                }
            }
        }
    }
}
