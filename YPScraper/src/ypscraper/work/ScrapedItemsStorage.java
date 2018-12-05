/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ypscraper.work;

import java.util.ArrayList;

/**
 *
 * @author Ihor
 */
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
    


