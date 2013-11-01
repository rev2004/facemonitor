package frontground;

import java.awt.EventQueue;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;

import org.opencv.core.Core;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainInterface extends JFrame {
	
	public VideoMonitorInterface videoMonitor = null;
	public FaceLoginInterface faceLogin = null;

	public static void main(String [] args) {
		
		System.loadLibrary("dll/"+Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("dll/opencv_core245");
		System.loadLibrary("dll/opencv_imgproc245");
		System.loadLibrary("dll/opencv_highgui245");
		System.loadLibrary("dll/opencv_flann245");
		System.loadLibrary("dll/opencv_features2d245");
		System.loadLibrary("dll/opencv_ffmpeg245_64");		
		System.loadLibrary("dll/opencv_ts245");
		System.loadLibrary("dll/opencv_video245");
		System.loadLibrary("dll/JNICheng");
		
		

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				MainInterface frame = new MainInterface();
				frame.setVisible(true);
			}
		});
		
		
	}
	public MainInterface() {
		setTitle("P-unhard C.D.C");
		setAutoRequestFocus(false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel contentPanel = new JPanel();

		setBounds(100, 100, 420, 293);
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPanel);
		contentPanel.setLayout(null);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JButton btnFaceLogin = new JButton("Face Login");
		btnFaceLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				setVisible(false);
				faceLogin = new FaceLoginInterface();
				faceLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				faceLogin.setVisible(true);

				faceLogin.toFront();
				
			}
		});
		btnFaceLogin.setBounds(31, 94, 127, 46);
		contentPanel.add(btnFaceLogin);
		
		JButton btnVideoMonitor = new JButton("Video Monitor");
		btnVideoMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				


				setVisible(false);
				videoMonitor = new VideoMonitorInterface();
				videoMonitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				videoMonitor.setVisible(true);
				videoMonitor.toFront();
			}
		});
		btnVideoMonitor.setBounds(245, 94, 127, 46);
		contentPanel.add(btnVideoMonitor);
		
		
		
	}
}
