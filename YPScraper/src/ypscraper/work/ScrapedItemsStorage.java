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
    
    public ArrayList<ScrapedItems> List;
    
    public class ScrapedItems {
        public String Name;
        public String Address;
        public String Link;
    }
    
    public ScrapedItemsStorage() {
        List = new ArrayList<ScrapedItems>();
    }
}


