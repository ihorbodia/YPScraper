package ypscraper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import ypscraper.work.YPScraperLogic;

public class YPScraper extends JFrame {

    private JLabel lblBusiness;
    private JLabel lblLocation;
    private JLabel lblStatus;
    private JLabel lblOutputPathData;
    private JLabel lblOutputPath;
    private JLabel lblConnectionTimeout;
    private JTextField textFieldBusiness;
    private JSpinner textFieldConnectionTimeout;
    private JTextField textFieldLocation;
    private JTextField textFieldStatus;
    private JProgressBar progressBar;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnSetOutputPath;
    private JButton btnCancel;
    private JFileChooser jfileChooser;
    
    YPScraperLogic logic;
    public Properties properties = new Properties();

    public class StartAction implements ActionListener {
        public StartAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Start");
             try {
                logic.Run();
            } catch (IOException ex) {
                Logger.getLogger(YPScraper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(YPScraper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(YPScraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public class SetOutputPathAction implements ActionListener {
        public SetOutputPathAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("SetOutputAction");
            int returnVal = getJFileChooser().showSaveDialog(YPScraper.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File folder = getJFileChooser().getSelectedFile();
                getlblOutputPathData().setText(folder.getPath());
            }
        }
    }

    public class StopAction implements ActionListener {
        public StopAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Stop");
        }
    }

    public class CancelAction implements ActionListener {
        public CancelAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Cancel");
        }
    }

    public static void main(String[] args) throws IOException {
        YPScraper frame = new YPScraper();
        frame.pack();
        Dimension frameSize = frame.getSize();
        frameSize.width += 100;
        frame.setMinimumSize(frameSize);
        frame.setVisible(true);
    }

    private void initActions() {
        getBtnStart().addActionListener(new StartAction());
        getBtnStop().addActionListener(new StopAction());
        getBtnCancel().addActionListener(new CancelAction());
        getBtnOutputPath().addActionListener(new SetOutputPathAction());
    }
    
    private void initLogic() {
        try {
            logic = new YPScraperLogic(YPScraper.this);
        } catch (IOException ex) {
            Logger.getLogger(YPScraper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(YPScraper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(YPScraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public YPScraper() {
        setTitle("YP Crawler CA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        add(PanelMain());
        setContentPane(PanelMain());
        initLogic();
        initActions();
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

        GridBagConstraints gbc_lblConnTimeout = new GridBagConstraints();
        gbc_lblConnTimeout.anchor = GridBagConstraints.EAST;
        gbc_lblConnTimeout.insets = new Insets(0, 0, 5, 5);
        gbc_lblConnTimeout.gridx = 0;
        gbc_lblConnTimeout.gridy = 2;
        panel.add(getLblConnectionTimeout(), gbc_lblConnTimeout);

        GridBagConstraints gbc_textFieldConnTimeout = new GridBagConstraints();
        gbc_textFieldConnTimeout.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldConnTimeout.gridwidth = 3;
        gbc_textFieldConnTimeout.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldConnTimeout.gridx = 1;
        gbc_textFieldConnTimeout.gridy = 2;
        panel.add(getTextFieldConnectionTimeout(), gbc_textFieldConnTimeout);

 

        GridBagConstraints gbc_lblOutputPath = new GridBagConstraints();
        gbc_lblOutputPath.anchor = GridBagConstraints.EAST;
        gbc_lblOutputPath.insets = new Insets(0, 0, 5, 5);
        gbc_lblOutputPath.gridx = 0;
        gbc_lblOutputPath.gridy = 3;
        panel.add(getlblOutputPath(), gbc_lblOutputPath);

        GridBagConstraints gbc_lblOutputPathData = new GridBagConstraints();
        gbc_lblOutputPathData.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblOutputPathData.gridwidth = 3;
        gbc_lblOutputPathData.insets = new Insets(0, 0, 5, 5);
        gbc_lblOutputPathData.gridx = 1;
        gbc_lblOutputPathData.gridy = 3;
        panel.add(getlblOutputPathData(), gbc_lblOutputPathData);
        
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.anchor = GridBagConstraints.EAST;
        gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 4;
        panel.add(getLblStatus(), gbc_lblStatus);

        GridBagConstraints gbc_textFieldStatus = new GridBagConstraints();
        gbc_textFieldStatus.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldStatus.gridwidth = 3;
        gbc_textFieldStatus.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldStatus.gridx = 1;
        gbc_textFieldStatus.gridy = 4;
        panel.add(getTextFieldStatus(), gbc_textFieldStatus);

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
        
        GridBagConstraints gbc_btnSetOutputFolder = new GridBagConstraints();
        gbc_btnSetOutputFolder.insets = new Insets(0, 0, 0, 5);
        gbc_btnSetOutputFolder.gridx = 3;
        gbc_btnSetOutputFolder.gridy = 5;
        panel.add(getBtnOutputPath(), gbc_btnSetOutputFolder);

        GridBagConstraints gbc_btnCancel = new GridBagConstraints();
        gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
        gbc_btnCancel.anchor = GridBagConstraints.EAST;
        gbc_btnCancel.gridx = 7;
        gbc_btnCancel.gridy = 5;
        panel.add(getBtnCancel(), gbc_btnCancel);
        panel.setVisible(true);
        return panel;
    }
    
    public JLabel getlblOutputPathData() {
        if (lblOutputPathData == null) {
            lblOutputPathData = new JLabel("");
        }
        return lblOutputPathData;
    }

    private JLabel getlblOutputPath() {
        if (lblOutputPath == null) {
            lblOutputPath = new JLabel("Output files folder:");
        }
        return lblOutputPath;
    }

    private JLabel getLblConnectionTimeout() {
        if (lblConnectionTimeout == null) {
            lblConnectionTimeout = new JLabel("Connection timeout:");
        }
        return lblConnectionTimeout;
    }

    public JSpinner getTextFieldConnectionTimeout() {
        if (textFieldConnectionTimeout == null) {
            textFieldConnectionTimeout = new JSpinner();
        }
        return textFieldConnectionTimeout;
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

    public JButton getBtnOutputPath() {
        if (btnSetOutputPath == null) {
            btnSetOutputPath = new JButton("Set output folder");
            btnSetOutputPath.setEnabled(true);
        }
        return btnSetOutputPath;
    }

    public JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
        }
        return btnCancel;
    }
    
    private JFileChooser getJFileChooser() {
        if (jfileChooser == null) {
            jfileChooser = new JFileChooser();
            jfileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        return jfileChooser;
    }
}
