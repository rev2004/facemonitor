package frontground;




import java.awt.EventQueue;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import checker.VideoChecker;




import background.MyVideoCapture;
import background.WhuFaceRecognizer;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import javax.swing.JSlider;

import java.awt.Font;

import javax.swing.JProgressBar;
import javax.swing.JCheckBox;


public class VideoMonitorInterface extends JFrame {
	
	
	//recognizer is set to match the small object.
	WhuFaceRecognizer recognizer = new WhuFaceRecognizer(60,60,1,8,8,8);
	
	int recgThreshold = 60;
	
	private JPanel contentPane;
	
	
	//Array of load groups  
	//ArrayList<String> arrayInputImages = new ArrayList<String>();
	ArrayList<String> arrayInputVideos = new ArrayList<String>();
	String exportPath = null;

	private JButton btnChoseVideo;
	private JButton btnStartMonitor;
	private JButton btnChoseImage;
	private JButton btnStop;
	
	private JCheckBox chckbxQuickSafe;
	
	private JLabel lblSpeed;
	private JSlider sliderSpeed;
	private JLabel lblAccurate;
	private JSlider sliderAccurate;
	private JProgressBar progressBar;
	public int framePercent = 50;
	
	public int nTotalFrames = 0;
	public int nProcFrames = 0;
	
	public MonitorThread monitorThread = null;
	private MonitorTargets targDlg = null;
	
	//A multi-display viewer.
	//private MonitorResultViewer mrViewer = null;
	
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
				VideoMonitorInterface frame = new VideoMonitorInterface();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
	
	public VideoMonitorInterface() {
		
		if(!recognizer.loadDetector("res")) {
			
			JOptionPane.showMessageDialog(this,"Detector load failed!","Sorry",JOptionPane.WARNING_MESSAGE);
		}
		monitorThread = null;
		
		setAutoRequestFocus(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 368, 551);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);


		btnChoseVideo = new JButton("Import Videos");
		btnChoseVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				choseVideos();
			}
		});

		btnChoseVideo.setBounds(10, 10, 160, 50);
		contentPane.add(btnChoseVideo);
		
		btnChoseImage = new JButton("Add Targets");
		btnChoseImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				choseImages();
			}
		});
		btnChoseImage.setBounds(180, 10, 160, 50);
		contentPane.add(btnChoseImage);
		

		btnStartMonitor = new JButton("Start");
		btnStartMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startMonitor();
			}
		});
		btnStartMonitor.setBounds(10, 384, 160, 50);
		contentPane.add(btnStartMonitor);
		
		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(monitorThread == null || monitorThread.isStop())
					stopMonitor();
				else
					monitorThread.stopMonitor();
				
			}
		});
		btnStop.setBounds(180, 384, 160, 50);
		contentPane.add(btnStop);
		btnStop.setEnabled(false);
		
		
		
		lblSpeed = new JLabel("Speed: 50%");
		lblSpeed.setFont(new Font("Arial", Font.PLAIN, 13));
		lblSpeed.setBounds(10, 83, 105, 23);
		contentPane.add(lblSpeed);
		
		sliderSpeed = new JSlider();
		sliderSpeed.setMinimum(1);
		
		sliderSpeed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JSlider) {
                   
                	JSlider js = sliderSpeed;
                	
                      framePercent = js.getValue();
                      lblSpeed.setText("Speed: "+ 100*framePercent/js.getMaximum() + "%");
                }
            }
        });
		sliderSpeed.setBounds(10, 116, 330, 23);
		contentPane.add(sliderSpeed);
		

		
		JLabel lblProgress = new JLabel("Progress:");
		lblProgress.setFont(new Font("Arial", Font.PLAIN, 13));
		lblProgress.setBounds(10, 256, 77, 23);
		contentPane.add(lblProgress);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(10, 288, 330, 14);
		contentPane.add(progressBar);
		
		JButton btnViewFolder = new JButton("View Result");
		btnViewFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewFolder();
			}
		});
		btnViewFolder.setBounds(10, 444, 160, 50);
		contentPane.add(btnViewFolder);
		
		lblAccurate = new JLabel("Accurate: 50%");
		lblAccurate.setFont(new Font("Arial", Font.PLAIN, 13));
		lblAccurate.setBounds(10, 170, 105, 15);
		contentPane.add(lblAccurate);
		
		
		sliderAccurate = new JSlider();
		sliderAccurate.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JSlider) {
                   
                	JSlider js = sliderAccurate;
                	
                	int v = js.getValue();
                	
                	if( v < 75 && v > 25) {
                		recgThreshold = (int) (-0.8*v + 100);
                	}
                	else if(v >= 75){
                		recgThreshold = (int) (-1.6*v + 160);
                	}
                	else {
                		recgThreshold = (int) (-4*v + 180);
                	}
                    lblAccurate.setText("Accurate: "+ 100*v/js.getMaximum() + "%");
                }
            }
        });
		sliderAccurate.setMinimum(1);
		sliderAccurate.setBounds(10, 195, 330, 23);
		contentPane.add(sliderAccurate);
		
		chckbxQuickSafe = new JCheckBox("Quick & Safe");
		chckbxQuickSafe.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxQuickSafe.setBounds(10, 308, 103, 23);
		contentPane.add(chckbxQuickSafe);
		
		JButton btnCheckVideo = new JButton("Check Video");
		btnCheckVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(exportPath == null)
					return;
				
				VideoChecker frame = new VideoChecker();
				frame.exportPath = exportPath;
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
				
			}
		});
		btnCheckVideo.setBounds(180, 444, 160, 50);
		contentPane.add(btnCheckVideo);
		

	
		

	}
	
	public void dispose() {
		
		if(monitorThread != null) {
			monitorThread.stopMonitor();
		}
		setVisible(false);
	}
	
	protected void choseVideos() {
		
		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.setMultiSelectionEnabled(true);
		//fileChooser.setCurrentDirectory(new File("res"));
		
		fileChooser.setFileFilter(new FileFilter() {
		      public boolean accept(File f) { //set available suffix
		    	  
		    	  String name = f.getName();
		          if(name.endsWith(".wmv")||name.endsWith(".mpeg")||name.endsWith(".avi")||name.endsWith(".mp4")||
		        		  name.endsWith(".rm")||name.endsWith(".rmvb")||f.isDirectory()){
		            return true;
		          }
		          return false;
		        }
		        public String getDescription() {
		          return "*.wmv|*.avi|*.mpeg|*.rm|*.rmvb|*.mp4";
		        }
		      });
		
		
		int returnValue = fileChooser.showOpenDialog(getContentPane());
		// chose OK to store these paths
		if (returnValue == JFileChooser.APPROVE_OPTION) {

			this.arrayInputVideos.clear();
			File [] files = fileChooser.getSelectedFiles();
			
			
			for(File file : files) {
				
				arrayInputVideos.add(file.getAbsolutePath());
				System.out.println(file.getAbsolutePath());
			}
		}
	}

	protected void choseImages() {
		
		if(targDlg == null) {
			targDlg = new MonitorTargets(recognizer);
			targDlg.setModal(true);
		}
		targDlg.setVisible(true);
	}
	
	public void stopMonitor() {
		
		btnStartMonitor.setEnabled(true);
		btnChoseImage.setEnabled(true);
		btnChoseVideo.setEnabled(true);
		btnStop.setEnabled(false);
		
		
		progressBar.setValue(progressBar.getMaximum());
		JOptionPane.showMessageDialog(this,"Finished!","OK",JOptionPane.PLAIN_MESSAGE);
	}

	protected void startMonitor() {
		
		
		monitorThread = null;
		this.nTotalFrames = 0;
		this.nProcFrames = 0;
		progressBar.setValue(0);
		
		if(targDlg == null || targDlg.targetsCount() <= 0) {
			
			JOptionPane.showMessageDialog(this,"Please add some targets first!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if(!setExportPath()) {
			return;
		}
		
		recognizer.clear();
		
		ArrayList<Mat> sources = new ArrayList<Mat>();
		ArrayList<Mat> samples = new ArrayList<Mat>();
		ArrayList<Integer> labels = new ArrayList<Integer>();
		ArrayList<String> names = new ArrayList<String>();
		
		targDlg.getSourcesAndLabels(sources, labels, names);
		
		int l_i = 1;
		int label = 0;
		for(int i=0;i<sources.size();i++) {
			
			Mat frame = sources.get(i);
			
			Rect face_rect = new Rect(0,0,0,0);
			Mat mat = new Mat();
			
			//the frame has been tested, face must be detected.
			recognizer.faceDetectSample(frame, face_rect, mat, true, false);
			
			
			Core.rectangle(frame, face_rect.tl(), face_rect.br(), new Scalar(0,255,0));
			
			samples.add(mat);
			
			int lab = labels.get(i);
			if( lab != label) {
				l_i = 1;
				label = lab;
			}
			
			String dirpath = exportPath + "/" + (label+1) + '_' + names.get(label);
			if (!new File(dirpath).isDirectory())
				new File(dirpath).mkdirs();
			
			Highgui.imwrite(dirpath+"/_sample_"+l_i+".jpg",frame);
			
			l_i++;
			
		}
		

		if(sources.size() < 1) {
			
			JOptionPane.showMessageDialog(this,"Please add some images first!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		
		
		
		recognizer.trainSamples(samples, labels);
		
		samples.clear();
		labels.clear();
 
		
		int total = 0;
		
		for(String path : arrayInputVideos) {
			
			MyVideoCapture video = new MyVideoCapture(path);
			if(video.isNull())
				continue;
			
			total += (int)video.get(CV_CAP_PROP_FRAME_COUNT);
			video.release();
		}
		
		if(total < 1) {
			JOptionPane.showMessageDialog(this,"Videos are invalid!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		this.nTotalFrames = total;
		progressBar.setMaximum(total);
		
		int maxInterval = total/200;
		if(maxInterval > 200) {
			maxInterval = 200;
		}
		
		int interval = maxInterval*sliderSpeed.getValue()/(sliderSpeed.getMaximum());
		
		if(sliderSpeed.getValue() == 1)
			interval = 1;
		
		if(interval < 1)
			interval = 1;
		
		System.out.println("Total Frames:"+total);
		System.out.println("Frame Interval:"+interval);
		

		btnStartMonitor.setEnabled(false);
		btnChoseVideo.setEnabled(false);
		btnChoseImage.setEnabled(false);
		btnStop.setEnabled(true);
		
		if(monitorThread == null)
			monitorThread = new MonitorThread(this);
		monitorThread.setParameters(arrayInputVideos,names,this.exportPath,this.recognizer,recgThreshold,interval);
		
		if(this.chckbxQuickSafe.isSelected()) {
			this.viewFolder();
			monitorThread.run();
		}
		else {
			monitorThread.start();
		}
		
		
	}
	
	public void addProcFrames(int add) {
		this.nProcFrames += add;
		if(this.nProcFrames <= this.nTotalFrames) {
			progressBar.setValue(nProcFrames);
		}
	}
	public void setProgressFinished() {
		
		this.progressBar.setValue(this.progressBar.getMaximum());
	}
	
	
	
	protected boolean setExportPath() {
		
		JFileChooser chooser = new JFileChooser();
		//chooser.setCurrentDirectory(new File("res"));
		chooser.setDialogTitle("Export path");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(null);
		if (result != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		String filePath = chooser.getSelectedFile().getAbsolutePath();
		
		filePath += "/grabs";
		
		deleteDirectory(filePath);
		
		if (!new File(filePath).isDirectory())
			new File(filePath).mkdirs();
		
		exportPath = filePath;
		return true;
	}
	
	protected void viewFolder() {
		
		if(exportPath == null)
			return;
		try {
			java.awt.Desktop.getDesktop().open(new File(exportPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean deleteFile(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);   
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}  
	
	public boolean deleteDirectory(String sPath) {  
	  
	    File dirFile = new File(sPath);  

	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) 
	            	break;  
	        }
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) 
	            	break;  
	        }  
	    }  
	    if (!flag) 
	    	return false;  

	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}  
}
