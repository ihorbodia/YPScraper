/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ypscraper.work;

/**
 *
 * @author Ihor
 */
public class ScrapedItem {
        public String Name;
        public String Address;
        public String Link;
        
        public ScrapedItem(String name, String address, String link) {
            Name = name;
            Address = address;
            Link = link;
        }
    }
