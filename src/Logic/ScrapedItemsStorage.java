package Logic;

import java.util.ArrayList;


public class ScrapedItemsStorage {

    public ArrayList<ScrapedItem> List;
    public String businessName;
    public String locationName;
    public String location;

    public ScrapedItemsStorage(String businessName, String locationName) {
        this.businessName = businessName;
        this.locationName = locationName;
        List = new ArrayList<ScrapedItem>();
    }
}
    