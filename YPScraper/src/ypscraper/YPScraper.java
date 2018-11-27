package ypscraper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class YPScraper extends JFrame {

    private JLabel lblBusiness;
    private JLabel lblLocation;
    private JLabel lblStatus;
    private JTextField textFieldBusiness;
    private JTextField textFieldLocation;
    private JTextField textFieldStatus;
    private JProgressBar progressBar;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnCancel;

    public static void main(String[] args) throws IOException {
        YPScraper frame = new YPScraper();
        frame.pack();
        Dimension frameSize = frame.getSize();
        frameSize.width += 100;
        frame.setMinimumSize(frameSize);
        frame.setVisible(true);

//        org.jsoup.nodes.Document doc = Jsoup.connect("https://www.yellowpages.ca/search/si/1/fashion%20retailer/ON").get();
//        String str = doc.body().toString();
//        Elements el1 = doc.select("#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child(1) > div > div.listing__content__wrapper > div.listing__content__wrap--flexed > div.listing__right.hasIcon > div.listing__title--wrap > h3 > a");
//        Elements el2 = doc.select("#ypgBody > div.page__container.jsTabsContent.margin-top-20.page__container--right-sidebar.hasMap > div > div.page__content.jsListingMerchantCards.jsListContainer > div.resultList.jsResultsList.jsMLRContainer > div > div:nth-child(2) > div > div.listing__content__wrapper > div.listing__content__wrap--flexed > div.listing__right.hasIcon > div.listing__title--wrap > h3 > a");
//        System.out.println(el1.html());
//        System.out.println(el2.html());
//        Element link = (Element) doc.select("a").first();
    }

    public YPScraper() {
        setTitle("YP Crawler CA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        add(PanelMain());
        setContentPane(PanelMain());
    }

    public JPanel PanelMain() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        panel.setLayout(gridBagLayout);
        
        GridBagConstraints gbc_lblBusiness = new GridBagConstraints();
        gbc_lblBusiness.anchor = GridBagConstraints.EAST;
        gbc_lblBusiness.insets = new Insets(0, 0, 5, 5);
        gbc_lblBusiness.gridx = 0;
        gbc_lblBusiness.gridy = 0;
        panel.add(getLblBusiness(), gbc_lblBusiness);
        
        GridBagConstraints gbc_textFieldBusiness = new GridBagConstraints();
        gbc_textFieldBusiness.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldBusiness.gridwidth = 3;
        gbc_textFieldBusiness.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldBusiness.gridx = 1;
        gbc_textFieldBusiness.gridy = 0;
        panel.add(getTextFieldBusiness(), gbc_textFieldBusiness);
        
        GridBagConstraints gbc_lblLocation = new GridBagConstraints();
        gbc_lblLocation.anchor = GridBagConstraints.EAST;
        gbc_lblLocation.insets = new Insets(0, 0, 5, 5);
        gbc_lblLocation.gridx = 0;
        gbc_lblLocation.gridy = 1;
        panel.add(getLblLocation(), gbc_lblLocation);
        
        GridBagConstraints gbc_textFieldLocation = new GridBagConstraints();
        gbc_textFieldLocation.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldLocation.gridwidth = 3;
        gbc_textFieldLocation.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldLocation.gridx = 1;
        gbc_textFieldLocation.gridy = 1;
        panel.add(getTextFieldLocation(), gbc_textFieldLocation);
        
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.anchor = GridBagConstraints.EAST;
        gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 2;
        panel.add(getLblStatus(), gbc_lblStatus);
        
        GridBagConstraints gbc_textFieldStatus = new GridBagConstraints();
        gbc_textFieldStatus.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldStatus.gridwidth = 3;
        gbc_textFieldStatus.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldStatus.gridx = 1;
        gbc_textFieldStatus.gridy = 2;
        panel.add(getTextFieldStatus(), gbc_textFieldStatus);
        
        GridBagConstraints gbc_lblConnTimeout = new GridBagConstraints();
        gbc_lblConnTimeout.anchor = GridBagConstraints.EAST;
        gbc_lblConnTimeout.insets = new Insets(0, 0, 5, 5);
        gbc_lblConnTimeout.gridx = 0;
        gbc_lblConnTimeout.gridy = 1;
        panel.add(getLblLocation(), gbc_lblConnTimeout);
        
        GridBagConstraints gbc_textFieldConnTimeout = new GridBagConstraints();
        gbc_textFieldConnTimeout.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldConnTimeout.gridwidth = 3;
        gbc_textFieldConnTimeout.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldConnTimeout.gridx = 1;
        gbc_textFieldConnTimeout.gridy = 1;
        panel.add(getTextFieldLocation(), gbc_textFieldConnTimeout);
        
        GridBagConstraints gbc_progressBar = new GridBagConstraints();
        gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
        gbc_progressBar.gridwidth = 3;
        gbc_progressBar.insets = new Insets(0, 0, 5, 5);
        gbc_progressBar.gridx = 1;
        gbc_progressBar.gridy = 4;
        panel.add(getProgressBar(), gbc_progressBar);
        GridBagConstraints gbc_btnStart = new GridBagConstraints();
        gbc_btnStart.anchor = GridBagConstraints.WEST;
        gbc_btnStart.insets = new Insets(0, 0, 0, 5);
        gbc_btnStart.gridx = 1;
        gbc_btnStart.gridy = 5;
        panel.add(getBtnStart(), gbc_btnStart);
        GridBagConstraints gbc_btnStop = new GridBagConstraints();
        gbc_btnStop.insets = new Insets(0, 0, 0, 5);
        gbc_btnStop.gridx = 2;
        gbc_btnStop.gridy = 5;
        panel.add(getBtnStop(), gbc_btnStop);
        GridBagConstraints gbc_btnCancel = new GridBagConstraints();
        gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
        gbc_btnCancel.anchor = GridBagConstraints.EAST;
        gbc_btnCancel.gridx = 4;
        gbc_btnCancel.gridy = 5;
        panel.add(getBtnCancel(), gbc_btnCancel);
        panel.setVisible(true);
        return panel;
    }

    private JLabel getLblBusiness() {
        if (lblBusiness == null) {
            lblBusiness = new JLabel("Business:");
        }
        return lblBusiness;
    }

    public JTextField getTextFieldBusiness() {
        if (textFieldBusiness == null) {
            textFieldBusiness = new JTextField();
            textFieldBusiness.setColumns(10);
        }
        return textFieldBusiness;
    }

    private JLabel getLblLocation() {
        if (lblLocation == null) {
            lblLocation = new JLabel("Location:");
        }
        return lblLocation;
    }

    public JTextField getTextFieldLocation() {
        if (textFieldLocation == null) {
            textFieldLocation = new JTextField();
            textFieldLocation.setColumns(10);
        }
        return textFieldLocation;
    }

    private JLabel getLblStatus() {
        if (lblStatus == null) {
            lblStatus = new JLabel("Status:");
        }
        return lblStatus;
    }

    public JTextField getTextFieldStatus() {
        if (textFieldStatus == null) {
            textFieldStatus = new JTextField() {
                public void setText(String arg0) {
                    super.setText(arg0);
                }
            ;
            };
			textFieldStatus.setEditable(false);
            textFieldStatus.setColumns(10);
            textFieldStatus.setBorder(BorderFactory.createEmptyBorder());
        }
        return textFieldStatus;
    }

    public JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar();
            progressBar.setStringPainted(true);
        }
        return progressBar;
    }

    public JButton getBtnStart() {
        if (btnStart == null) {
            btnStart = new JButton("Start");
        }
        return btnStart;
    }

    public JButton getBtnStop() {
        if (btnStop == null) {
            btnStop = new JButton("Stop");
            btnStop.setEnabled(false);
        }
        return btnStop;
    }

    public JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
        }
        return btnCancel;
    }
}
