package frontground;


import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

import background.JNICheng;
import background.WhuFaceRecognizer;





import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Font;

import javax.swing.border.BevelBorder;



public class MonitorTargets extends JDialog {

	private JPanel contentPane;
	private JList<String> listMain;
	private JList<String> listDetails;
	
//	private JTextField txtAdrLeft;
//	private JTextField txtIDLeft;
	JScrollPane scroller;
	JScrollPane scroller2;
	
	public ArrayList<DefaultListModel<String>> imageGroups = new ArrayList<DefaultListModel<String>>();
	

	Icon[] iconResult;
	DefaultListModel<String> defaultListModel;
	JLabel lblNewLabel;
	private JButton btnOK;
	
	private JButton btnTargetDelete;
	private JButton btnImportImage;
	private JButton btnImageDelete;
	private JButton btnView;
	
	private WhuFaceRecognizer recognizer = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
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
				try {
					
					WhuFaceRecognizer recg = new WhuFaceRecognizer(48,60,1,8,4,4);
					recg.loadDetector("res");
					MonitorTargets frame = new MonitorTargets(recg);
					//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					//frame.loadExports("C:\\Users\\Cheng\\workspace\\FaceMonitor\\grab");
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
	public MonitorTargets(WhuFaceRecognizer recg) {
		
		
		recognizer = recg;
		setBounds(100, 100, 750, 463);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
			
		defaultListModel = new DefaultListModel<String>();
        
		listMain = new JList<String>();
		listMain.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		listMain.setModel(defaultListModel);
		
		listMain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionListener listener = new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent arg0) {
				int index = listMain.getSelectedIndex();
				showList(index);
				
			}
		};
		listMain.addListSelectionListener(listener);
		
		
		
		
		
		scroller = new JScrollPane(listMain);
		scroller.setBounds(20,54,200,268);
		contentPane.add(scroller);
		
		listDetails = new JList<String>();
		listDetails.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		listDetails.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		listDetails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		listDetails.addListSelectionListener( new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent arg0) {
				
				int index = listDetails.getSelectedIndex();
				if(index < 0) {
					btnImageDelete.setEnabled(false);
					btnView.setEnabled(false);
					return;
				}
				btnImageDelete.setEnabled(true);
				btnView.setEnabled(true);
			}
		});
		
		
		
		scroller2 = new JScrollPane(listDetails);
		scroller2.setBounds(274, 54, 433, 268);
		contentPane.add(scroller2);
		
		btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnOK.setBounds(631, 392, 93, 23);
		contentPane.add(btnOK);
		getRootPane().setDefaultButton(btnOK);
		
		JButton btnNewTarget = new JButton("New");
		btnNewTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				addTarget();
			}
		});
		btnNewTarget.setBounds(20, 332, 93, 23);
		contentPane.add(btnNewTarget);
		
		btnTargetDelete = new JButton("Delete");
		btnTargetDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				int index = listMain.getSelectedIndex();
				deleteTarget(index);
				
			}
		});
		btnTargetDelete.setBounds(123, 332, 93, 23);
		btnTargetDelete.setEnabled(false);
		contentPane.add(btnTargetDelete);
		
		btnImportImage = new JButton("Import Image");
		btnImportImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				int index = listMain.getSelectedIndex();
				importImages(index);
			}
		});
		btnImportImage.setBounds(284, 332, 130, 23);
		btnImportImage.setEnabled(false);
		contentPane.add(btnImportImage);
		
		btnImageDelete = new JButton("Delete");
		btnImageDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				int index = listDetails.getSelectedIndex();
				removeImage(index);
			}
		});
		btnImageDelete.setBounds(427, 332, 93, 23);
		btnImageDelete.setEnabled(false);
		contentPane.add(btnImageDelete);
		
		btnView = new JButton("View");
		btnView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
				int index = listDetails.getSelectedIndex();
				if(index < 0 || index >= listDetails.getModel().getSize())
					return;
				String path = listDetails.getModel().getElementAt(index);
				
				if(path == null)
					return;
				try {
					java.awt.Desktop.getDesktop().open(new File(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnView.setBounds(530, 332, 93, 23);
		contentPane.add(btnView);
		btnView.setEnabled(false);
		
		JLabel lblTargets = new JLabel("Targets");
		lblTargets.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblTargets.setBounds(20, 21, 74, 23);
		contentPane.add(lblTargets);
		
		JLabel lblLearningImages = new JLabel("Learning Images");
		lblLearningImages.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblLearningImages.setBounds(274, 21, 130, 20);
		contentPane.add(lblLearningImages);
		
		
	}
	
	public void showList(int index) {
	     
	     
	     
	     
	     if(index >= imageGroups.size() || index < 0) {
	    	 
	    	 btnTargetDelete.setEnabled(false);
	    	 btnImportImage.setEnabled(false);
	    	 btnImageDelete.setEnabled(false);
	    	 btnView.setEnabled(false);
	    	 listDetails.setModel(new DefaultListModel<String>());
	    	 return;
	     }
	     
	     btnTargetDelete.setEnabled(true);
	     btnImportImage.setEnabled(true);
	     
	     
	     ListModel<String> model = imageGroups.get(index);
	     
	     
	     listDetails.setModel(model);
	     
	    
	}
	
	
	public void addTarget() {
		
		InputDialog dlg = new InputDialog();
		dlg.setModal(true);
		dlg.setVisible(true);
		
		if(dlg.isOK()) {
		
			defaultListModel.add(defaultListModel.size(), dlg.getUserName());
			DefaultListModel<String> list = new DefaultListModel<String>();
			imageGroups.add(list);
		}
		
	}
	
	public void deleteTarget(int index) {
		
		if(index < 0) {
			return;
		}
			
		imageGroups.remove(index);
		defaultListModel.remove(index);
		
		listMain.setSelectedIndex(index-1);

	}
	
	public void removeImage(int index) {
		
		if(index < 0)
			return;
		DefaultListModel<String> model = (DefaultListModel)listDetails.getModel();
		model.remove(index);
		
		listDetails.setSelectedIndex(index-1);
	}
	
	public void importImages(int index) {
		
		
		if(index < 0 || index >= imageGroups.size() || recognizer == null)
			return;
		
		JFileChooser fileChooser = new JFileChooser();
		
		//fileChooser.setCurrentDirectory(new File("res"));
		fileChooser.setMultiSelectionEnabled(true);
		
		fileChooser.setFileFilter(new FileFilter() {
		      public boolean accept(File f) { //set available suffix
		    	  
		    	  String name = f.getName();
		          if(name.endsWith(".jpg")||name.endsWith(".png")||name.endsWith(".jpeg")||f.isDirectory()){
		            return true;
		          }
		          return false;
		        }
		        public String getDescription() {
		          return "*.jpg|*.jpeg|*.png";
		        }
		      });
		
		
		int returnValue = fileChooser.showOpenDialog(getContentPane());
		// chose OK to store these paths
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			
			
			DefaultListModel<String> model = imageGroups.get(index);
			File [] files = fileChooser.getSelectedFiles();
			
			
			
			for(File file : files) {
				
				String path = file.getAbsolutePath();
				
				//check image is valid ( has face)
				Mat frame = new Mat();//Highgui.imread(path);
				JNICheng.loadImage(path, 0, frame);
				
				if(frame != null) {
					
					Mat face = new Mat();
					Rect face_rect = new Rect(0,0,0,0);
					if(recognizer.faceDetectSample(frame, face_rect, face, true, false)) {
						
						model.add(model.size(),path);
					}
					else {
						JOptionPane.showMessageDialog(this,"No clear face or More faces appear on the image!\n"+path,"Sorry",JOptionPane.WARNING_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(this,"Not a valid image!\n"+path,"Sorry",JOptionPane.WARNING_MESSAGE);
				}
				//frame.release();
			}
			
			listDetails.setModel(model);
			
		}	
		
	}
	public void getSourcesAndLabels(ArrayList<Mat> presource, ArrayList<Integer> labels, ArrayList<String> names) {
		
		int label = 0;
		int j = 0;
		for(DefaultListModel<String> model : imageGroups) {
			
			j++;
			if(model.size() == 0)
				continue;
			names.add(listMain.getModel().getElementAt(j-1));

			
			for(int i=0;i<model.size();i++) {
				
				String path = model.get(i);
				Mat frame = new Mat();
				JNICheng.loadImage(path, 1, frame);//Highgui.imread(path);
				
				if(frame != null) {
				
					presource.add(frame);
					labels.add(label);
				}
			}
			label++;
		}
		
	}
	public int targetsCount() {
		
		
		return imageGroups.size();
	}
}
