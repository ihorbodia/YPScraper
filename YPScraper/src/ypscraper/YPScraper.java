package ypscraper;

import java.io.IOException;
import javax.swing.text.Element;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;

public class YPScraper {

    public static void main(String[] args) throws IOException {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    FrameMain frame = new FrameMain();
//                    frame.pack();
//                    Dimension frameSize = frame.getSize();
//                    frameSize.width += 100;
//                    frame.setMinimumSize(frameSize);
//                    frame.setVisible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        org.jsoup.nodes.Document doc = Jsoup.connect("https://www.yellowpages.ca/search/si/1/fashion%20retailer/ON").get();

        Element link = (Element) doc.select("a").first();
    }
}
