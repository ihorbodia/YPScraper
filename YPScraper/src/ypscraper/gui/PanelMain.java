package gui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PanelMain extends JPanel {
	
	private static Log log = LogFactory.getLog(PanelMain.class);
	
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

	public void init(Properties properties) {
		getTextFieldBusiness().setText(properties.getProperty("panelMain.field.business.text"));
		getTextFieldLocation().setText(properties.getProperty("panelMain.field.location.text"));
		getTextFieldLocation().setEditable(Boolean.parseBoolean(properties.getProperty("panelMain.field.location.editable")));
		getTextFieldStatus().setText(properties.getProperty("panelMain.field.status.text"));
	}
	
	public void save(Properties properties) {
		properties.setProperty("panelMain.field.business.text", getTextFieldBusiness().getText());
		properties.setProperty("panelMain.field.location.text", getTextFieldLocation().getText());
		properties.setProperty("panelMain.field.location.editable", String.valueOf(getTextFieldLocation().isEditable()));
		properties.setProperty("panelMain.field.status.text", getTextFieldStatus().getText());
	}
	
	/**
	 * Create the panel.
	 */
	public PanelMain() {
		setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_lblBusiness = new GridBagConstraints();
		gbc_lblBusiness.anchor = GridBagConstraints.EAST;
		gbc_lblBusiness.insets = new Insets(0, 0, 5, 5);
		gbc_lblBusiness.gridx = 0;
		gbc_lblBusiness.gridy = 0;
		add(getLblBusiness(), gbc_lblBusiness);
		GridBagConstraints gbc_textFieldBusiness = new GridBagConstraints();
		gbc_textFieldBusiness.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldBusiness.gridwidth = 3;
		gbc_textFieldBusiness.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldBusiness.gridx = 1;
		gbc_textFieldBusiness.gridy = 0;
		add(getTextFieldBusiness(), gbc_textFieldBusiness);
		GridBagConstraints gbc_lblLocation = new GridBagConstraints();
		gbc_lblLocation.anchor = GridBagConstraints.EAST;
		gbc_lblLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocation.gridx = 0;
		gbc_lblLocation.gridy = 1;
		add(getLblLocation(), gbc_lblLocation);
		GridBagConstraints gbc_textFieldLocation = new GridBagConstraints();
		gbc_textFieldLocation.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldLocation.gridwidth = 3;
		gbc_textFieldLocation.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldLocation.gridx = 1;
		gbc_textFieldLocation.gridy = 1;
		add(getTextFieldLocation(), gbc_textFieldLocation);
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.anchor = GridBagConstraints.EAST;
		gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 2;
		add(getLblStatus(), gbc_lblStatus);
		GridBagConstraints gbc_textFieldStatus = new GridBagConstraints();
		gbc_textFieldStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldStatus.gridwidth = 3;
		gbc_textFieldStatus.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldStatus.gridx = 1;
		gbc_textFieldStatus.gridy = 2;
		add(getTextFieldStatus(), gbc_textFieldStatus);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 3;
		add(getProgressBar(), gbc_progressBar);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.anchor = GridBagConstraints.WEST;
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 4;
		add(getBtnStart(), gbc_btnStart);
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.insets = new Insets(0, 0, 0, 5);
		gbc_btnStop.gridx = 2;
		gbc_btnStop.gridy = 4;
		add(getBtnStop(), gbc_btnStop);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.anchor = GridBagConstraints.EAST;
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 4;
		add(getBtnCancel(), gbc_btnCancel);
	}
	
	public static void createAndShowGUI() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new PanelMain());
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
	private JLabel getLblBusiness() {
		if (lblBusiness == null) {
			lblBusiness = new JLabel("Business:");
		}
		return lblBusiness;
	}
	private JLabel getLblLocation() {
		if (lblLocation == null) {
			lblLocation = new JLabel("Location:");
		}
		return lblLocation;
	}
	private JLabel getLblStatus() {
		if (lblStatus == null) {
			lblStatus = new JLabel("Status:");
		}
		return lblStatus;
	}
	public JTextField getTextFieldBusiness() {
		if (textFieldBusiness == null) {
			textFieldBusiness = new JTextField();
			textFieldBusiness.setColumns(10);
		}
		return textFieldBusiness;
	}
	public JTextField getTextFieldLocation() {
		if (textFieldLocation == null) {
			textFieldLocation = new JTextField();
			textFieldLocation.setColumns(10);
		}
		return textFieldLocation;
	}
	public JTextField getTextFieldStatus() {
		if (textFieldStatus == null) {
			textFieldStatus = new JTextField() {
				public void setText(String arg0) {
					log.info(arg0);
					super.setText(arg0);
				};
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
	
	public boolean hasBusinessSpecified() {
		return getTextFieldBusiness().getText() != null && getTextFieldBusiness().getText().length() > 0;
	}
}