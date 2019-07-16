package Services;

public class DIResolver {

    private static GuiService guiService;
    private static PropertiesService propertiesService;
    private static FilesService filesService;

    public DIResolver() {
        if (guiService == null) {
            guiService = new GuiService();
        }
        if (propertiesService == null) {
            propertiesService = new PropertiesService();
        }


        if (getFilesService() == null) {
            filesService = new FilesService();
        }
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
