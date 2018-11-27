package ypscraper;

import gui.FrameMain;
import java.awt.Dimension;
import java.awt.EventQueue;

public class YPScraper {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FrameMain frame = new FrameMain();
                    frame.pack();
                    Dimension frameSize = frame.getSize();
                    frameSize.width += 100;
                    frame.setMinimumSize(frameSize);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
