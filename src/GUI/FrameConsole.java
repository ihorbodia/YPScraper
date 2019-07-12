package GUI;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class FrameConsole extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrameConsole frame = new FrameConsole();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrameConsole() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
		setBounds(200, 200, 450, 300);
		AutoScrollTextArea autoScrollTextArea = 
			new AutoScrollTextArea();
		setContentPane(autoScrollTextArea);
		setTitle("YP Crawler Console (showing the last " + autoScrollTextArea.getLinesToShow() + " lines)");
	}

}
