package Services;

import Models.AppPropertiesModel;
import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.util.Properties;

public class PropertiesService {

    private Properties properties = new Properties();
    private File propertiesFile;
    private String separator = File.separator;
    private OutputStream output = null;
    private InputStream input = null;


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


    public void saveLocationsFileLocation(File locationsFile) {
        try {
        output = new FileOutputStream(propertiesFile.getAbsoluteFile());
            properties.setProperty("csvPostalCodesFile", locationsFile.getAbsolutePath());
            properties.store(output, null);
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

    public void saveProperties(AppPropertiesModel appPropertiesModel, boolean isMultipleSearch) {
        try {
            output = new FileOutputStream(propertiesFile.getAbsoluteFile());
            properties.setProperty("business", appPropertiesModel.business);
            if (!isMultipleSearch) {
                properties.setProperty("province", appPropertiesModel.province);
            }
            properties.setProperty("outputFolder", appPropertiesModel.outputFolder.getAbsolutePath());
            properties.setProperty("csvPostalCodesFile", appPropertiesModel.inputLocationsFile.getAbsolutePath());
            properties.setProperty("running", String.valueOf(appPropertiesModel.running));
            properties.setProperty("postalCodeIndex", Integer.toString(appPropertiesModel.postalCodeIndex));
            properties.store(output, null);
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
            createNewPropertiesFile();
            InputStream input = new FileInputStream(propertiesFile.getAbsoluteFile());
            properties.load(input);

            if (properties.get("business") != null) {
                appPropertiesModel.business = properties.get("business").toString();
            }

            if (properties.get("province") != null) {
                appPropertiesModel.province = properties.get("province").toString();
            }

            if (properties.get("outputFolder") != null) {
                String path = properties.get("outputFolder").toString();
                if (!path.equalsIgnoreCase("")) {
                    appPropertiesModel.outputFolder = new File(path);
                }
            }

            if (properties.get("csvPostalCodesFile") != null) {
                String csvPath = properties.get("csvPostalCodesFile").toString();
                if (!csvPath.equalsIgnoreCase("")) {
                    appPropertiesModel.inputLocationsFile = new File(csvPath);
                }
            }

            if (properties.get("postalCodeIndex") != null) {
                String postalCodeIndexStr = properties.get("postalCodeIndex").toString();
                appPropertiesModel.postalCodeIndex = Integer.parseInt(postalCodeIndexStr);
            }
            if (properties.get("running") != null) {
                String runningStr = properties.get("running").toString();
                appPropertiesModel.running = Boolean.parseBoolean(runningStr);
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
        return appPropertiesModel;
    }
}
