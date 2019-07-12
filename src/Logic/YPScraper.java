package Logic;

import GUI.WindowHandler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
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
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


public class YPScraper extends JFrame {

    private JLabel lblBusiness;
    private JLabel lblLocation;
    private JLabel lblStatus;
    private JLabel lblOutputPathData;
    private JLabel lblOutputPath;
    private JLabel lblPostalCodesPathData;
    private JLabel lblPostalCodesPath;
    private JLabel lblConnectionTimeout;
    private JTextField textFieldBusiness;
    private JSpinner textFieldConnectionTimeout;
    private JTextField textFieldLocation;
    private JLabel textFieldStatus;
    private JProgressBar progressBar;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnSetOutputPath;
    private JButton btnChooseCSVPostaCodesPath;
    private JButton btnCancel;
    private JFileChooser jfileChooser;
    private JFileChooser jfolderChooser;

    public File outputFolder;
    public File inputLocationsFile;

    private WindowHandler handler = null;

    public Logger logger = null;

    YPScraperLogic logic;
    public Properties properties = new Properties();

    public class StartAction implements ActionListener {

        public StartAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Start");
            logMessage("Starting...");
            getTextFieldStatus().setText("Starting...");
            logic.removeOldFileIfExists();
            logic.createOutputFile();
            logic.Run(true);
            logic.saveProperties();
        }
    }

    public class SetOutputPathAction implements ActionListener {

        public SetOutputPathAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("SetOutputAction");

            String path = selectFolderDialog();
            if (!path.equalsIgnoreCase("")) {
                outputFolder = new File(path);
                getlblOutputPathData().setText(outputFolder.getName());
                logic.saveProperties();
            }
        }
    }

    public class SetCSVPostaCodesAction implements ActionListener {

        public SetCSVPostaCodesAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Set postal codes action raised");
            FileDialog dialog = new FileDialog(YPScraper.this, "Select File to Open");
            dialog.setVisible(true);
            if (dialog.getFile() != null && !dialog.getFile().equalsIgnoreCase("") && dialog.getFile().toLowerCase().endsWith(".csv")) {
                inputLocationsFile = new File(dialog.getDirectory() + dialog.getFile());
                getlblPostalCodesPathData().setText(inputLocationsFile.getName());
                logic.getPostalCodes(inputLocationsFile.getAbsolutePath());
                logic.saveProperties();
            }
        }
    }

    private String selectFolderDialog() {
        String osName = System.getProperty("os.name");
        String result = "";
        if (osName.equalsIgnoreCase("mac os x")) {
            FileDialog chooser = new FileDialog(YPScraper.this, "Select folder");
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            chooser.setVisible(true);

            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            if (chooser.getDirectory() != null) {
                String folderName = chooser.getDirectory();
                folderName += chooser.getFile();
                result = folderName;
            }
        } else {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Target Folder");
            chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);

            int returnVal = chooser.showDialog(YPScraper.this, "Select folder");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File userSelectedFolder = chooser.getSelectedFile();
                String folderName = userSelectedFolder.getAbsolutePath();
                result = folderName;
            }
        }
        return result;
    }

    public class StopAction implements ActionListener {

        public StopAction() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Stop");
            if (logic != null) {
                if (logic.future != null) {
                    logic.future.cancel(true);
                }
                logic.running = false;
                logic.continueWork = false;
                logic.saveDataToFile();
            }
            logic.saveProperties();
        }
    }

    public class CancelAction implements ActionListener {

        public CancelAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (logic != null) {
                logic.future.cancel(true);
                getBtnStop().setEnabled(false);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        YPScraper frame = new YPScraper();
        frame.pack();
        frame.setSize(new Dimension(600, 210));
        frame.setResizable(false);
        frame.setVisible(true);
        WindowHandler h = WindowHandler.getInstance();
        LogRecord r = new LogRecord(Level.WARNING, "Start logger...");
        h.publish(r);
    }

    private void initActions() {
        getBtnStart().addActionListener(new StartAction());
        getBtnStop().addActionListener(new StopAction());
        getBtnCancel().addActionListener(new CancelAction());
        getBtnOutputPath().addActionListener(new SetOutputPathAction());
        getBtnChooseCSVPostaCodesPath().addActionListener(new SetCSVPostaCodesAction());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                logic.saveProperties();
                if (logic.future != null && !logic.future.isDone()) {
                    logic.saveDataToFile();
                }
            }
        }));
    }

    private void initLogger(){
        handler = WindowHandler.getInstance();
        logger = Logger.getLogger("logging.handler");
        logger.addHandler(handler);
    }

    public void logMessage(String message) {
        logger.info(message);
    }

    private void initLogic() {
        try {
            logic = new YPScraperLogic(YPScraper.this);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public YPScraper() {
        setTitle("YP Crawler CA v1.8");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        add(PanelMain());
        setContentPane(PanelMain());
        initLogic();
        initActions();
        initLogger();
    }

    public JPanel PanelMain() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        GridBagConstraints gbc_lblBusiness = new GridBagConstraints();
        gbc_lblBusiness.insets = new Insets(0, 0, 5, 5);
        gbc_lblBusiness.anchor = GridBagConstraints.EAST;
        gbc_lblBusiness.gridx = 0;
        gbc_lblBusiness.gridy = 0;
        panel.add(getLblBusiness(), gbc_lblBusiness);

        GridBagConstraints gbc_textFieldBusiness = new GridBagConstraints();
        gbc_textFieldBusiness.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldBusiness.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldBusiness.gridwidth = 5;
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
        gbc_textFieldLocation.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldLocation.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldLocation.gridwidth = 5;
        gbc_textFieldLocation.gridx = 1;
        gbc_textFieldLocation.gridy = 1;
        panel.add(getTextFieldLocation(), gbc_textFieldLocation);

        GridBagConstraints gbc_lblOutputPath = new GridBagConstraints();
        gbc_lblOutputPath.insets = new Insets(0, 0, 5, 5);
        gbc_lblOutputPath.anchor = GridBagConstraints.EAST;
        gbc_lblOutputPath.gridx = 0;
        gbc_lblOutputPath.gridy = 3;
        panel.add(getlblOutputPath(), gbc_lblOutputPath);

        GridBagConstraints gbc_lblOutputPathData = new GridBagConstraints();
        gbc_lblOutputPathData.insets = new Insets(0, 0, 5, 5);
        gbc_lblOutputPathData.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblOutputPathData.gridwidth = 5;
        gbc_lblOutputPathData.gridx = 1;
        gbc_lblOutputPathData.gridy = 3;
        panel.add(getlblOutputPathData(), gbc_lblOutputPathData);

        GridBagConstraints gbc_lblPostalCodes = new GridBagConstraints();
        gbc_lblPostalCodes.insets = new Insets(0, 0, 5, 5);
        gbc_lblPostalCodes.anchor = GridBagConstraints.EAST;
        gbc_lblPostalCodes.gridx = 0;
        gbc_lblPostalCodes.gridy = 4;
        panel.add(getlblPostalCodesPath(), gbc_lblPostalCodes);

        GridBagConstraints gbc_lblPostaCodesData = new GridBagConstraints();
        gbc_lblPostaCodesData.insets = new Insets(0, 0, 5, 5);
        gbc_lblPostaCodesData.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblPostaCodesData.weightx = 0.6;
        gbc_lblPostaCodesData.gridwidth = 5;
        gbc_lblPostaCodesData.gridx = 1;
        gbc_lblPostaCodesData.gridy = 4;
        panel.add(getlblPostalCodesPathData(), gbc_lblPostaCodesData);

        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
        gbc_lblStatus.anchor = GridBagConstraints.EAST;
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 5;
        panel.add(getLblStatus(), gbc_lblStatus);

        GridBagConstraints gbc_textFieldStatus = new GridBagConstraints();
        gbc_textFieldStatus.insets = new Insets(0, 0, 5, 5);
        gbc_textFieldStatus.anchor = GridBagConstraints.WEST;
        gbc_textFieldStatus.gridwidth = 5;
        gbc_textFieldStatus.gridx = 1;
        gbc_textFieldStatus.gridy = 5;
        panel.add(getTextFieldStatus(), gbc_textFieldStatus);

        GridBagConstraints gbc_btnStart = new GridBagConstraints();
        gbc_btnStart.insets = new Insets(25, 0, 5, 5);
        gbc_btnStart.anchor = GridBagConstraints.SOUTHEAST;
        gbc_btnStart.gridx = 0;
        gbc_btnStart.gridy = 8;
        panel.add(getBtnStart(), gbc_btnStart);

        GridBagConstraints gbc_btnStop = new GridBagConstraints();
        gbc_btnStop.insets = new Insets(25, 0, 5, 5);
        gbc_btnStop.gridx = 7;
        gbc_btnStop.gridy = 8;
        panel.add(getBtnStop(), gbc_btnStop);

        GridBagConstraints gbc_btnSetOutputFolder = new GridBagConstraints();
        gbc_btnSetOutputFolder.insets = new Insets(25, 0, 5, 5);
        gbc_btnSetOutputFolder.gridx = 1;
        gbc_btnSetOutputFolder.gridy = 8;
        panel.add(getBtnOutputPath(), gbc_btnSetOutputFolder);

        GridBagConstraints gbc_btnSetCSVPostaCodesFolder = new GridBagConstraints();
        gbc_btnSetCSVPostaCodesFolder.insets = new Insets(25, 0, 5, 5);
        gbc_btnSetCSVPostaCodesFolder.gridx = 2;
        gbc_btnSetCSVPostaCodesFolder.gridy = 8;
        panel.add(getBtnChooseCSVPostaCodesPath(), gbc_btnSetCSVPostaCodesFolder);

        panel.setVisible(true);
        return panel;
    }

    public JLabel getlblPostalCodesPathData() {
        if (lblPostalCodesPathData == null) {
            lblPostalCodesPathData = new JLabel("");
        }
        return lblPostalCodesPathData;
    }

    private JLabel getlblPostalCodesPath() {
        if (lblPostalCodesPath == null) {
            lblPostalCodesPath = new JLabel("Postal codes CSV:");
        }
        return lblPostalCodesPath;
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
            textFieldBusiness.setVisible(true);
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

    public JLabel getTextFieldStatus() {
        if (textFieldStatus == null) {
            textFieldStatus = new JLabel();
            textFieldStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
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

    public JButton getBtnChooseCSVPostaCodesPath() {
        if (btnChooseCSVPostaCodesPath == null) {
            btnChooseCSVPostaCodesPath = new JButton("Choose postal codes");
            btnChooseCSVPostaCodesPath.setEnabled(true);
        }
        return btnChooseCSVPostaCodesPath;
    }

    public JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
        }
        return btnCancel;
    }

    private JFileChooser getJFolderChooser() {
        if (jfolderChooser == null) {
            jfolderChooser = new JFileChooser();
            jfolderChooser.setAcceptAllFileFilterUsed(false);
            jfolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        return jfolderChooser;
    }

    private JFileChooser getJFilesChooser() {
        if (jfileChooser == null) {
            jfileChooser = new JFileChooser();
            jfileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        return jfileChooser;
    }
}