package Services;

import Logic.YPScraperLogic;

public class DIResolver {

    private static GuiService guiService;
    private static PropertiesService propertiesService;
    private static YPScraperLogic ypScraperLogic;
    private static FilesService filesService;

    public DIResolver() {
        if (guiService == null) {
            guiService = new GuiService();
        }
        if (propertiesService == null) {
            propertiesService = new PropertiesService();
        }

        if (getYpScraperLogic() == null) {
            ypScraperLogic = new YPScraperLogic();
        }

        if (getFilesService() == null) {
            filesService = new FilesService();
        }
    }

    public YPScraperLogic getYpScraperLogic() {
        return ypScraperLogic;
    }

    public FilesService getFilesService() {
        return filesService;
    }

    public GuiService getGuiService() {
        return guiService;
    }

    public PropertiesService getPropertiesService() {
        return propertiesService;
    }
}
