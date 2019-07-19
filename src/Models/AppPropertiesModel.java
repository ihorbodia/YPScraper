package Models;

import java.io.File;

public class AppPropertiesModel {

    public AppPropertiesModel() {
        business = "";
        province = "";
    }


    public String business;
    public String province;
    public File outputFolder;
    public boolean running;
    public int postalCodeIndex;
    public File inputLocationsFile;
}
