package Services;

import Logic.SearchStrategies.BaseSearchStrategy;

public class DIResolver {

    private static GuiService guiService;
    private static PropertiesService propertiesService;
    private static FilesService filesService;
    private static LoggerService loggerService;

    private static BaseSearchStrategy baseSearchStrategy;

    public DIResolver() {
    }

    public BaseSearchStrategy getBaseSearchStrategy() {
        return baseSearchStrategy;
    }

    public void setBaseSearchStrategy(BaseSearchStrategy baseSearchStrategy) {
        DIResolver.baseSearchStrategy = baseSearchStrategy;
    }


    public FilesService getFilesService() {
        if (filesService == null) {
            filesService = new FilesService();
        }
        return filesService;
    }

    public GuiService getGuiService() {
        if (guiService == null) {
            guiService = new GuiService();
        }
        return guiService;
    }

    public PropertiesService getPropertiesService() {
        if (propertiesService == null) {
            propertiesService = new PropertiesService();
        }
        return propertiesService;
    }

    public LoggerService getLoggerService() {
        if (loggerService == null) {
            loggerService = new LoggerService();
        }
        return loggerService;
    }
}
