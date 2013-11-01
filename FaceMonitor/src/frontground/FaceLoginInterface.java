package frontground;

import background.DetectFaceInfo;

import background.CRClassifier;
import background.ConvertHelper;
import background.MouthLander;
import background.MyLandmark;
import background.UserInfo;
import background.WhuFaceRecognizer;
import background.JNICheng;



import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.util.ArrayList;



import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.JLabel;
import javax.swing.JButton;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.awt.Font;
import java.awt.Color;

import javax.swing.JTextField;

public class FaceLoginInterface extends JFrame {


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
		//System.loadLibrary("dll/JNICheng");
		//System.loadLibrary("dll/MyVideoCapture");
		
		//CRClassifier cr = new CRClassifier(54);
	
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceLoginInterface frame = new FaceLoginInterface();
					frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
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
	
	WhuFaceRecognizer recognizer = new WhuFaceRecognizer(100,100);
	
	//Interface controls.
	private JPanel contentPane;
	JLabel lblNewLabel;
	JLabel lblMessage;

	private JButton btnFaceLogin;
	private JButton btnFaceTrain;
	private JButton btnClear;
	
	private JTextField textUsername;
	private JPasswordField textPassword;
	
	
	//Necessary stuff.
	VideoCapture videoCapture = null;
	Timer activityTimer = null;
	
	
	//paths of the data.
	final String dataDirPath = "data";
	final String resDirPath = "res";
	
	
	//Threshold of the recognition result value;
	//if the value bellow than 'recgThreshold', then the face is accept.
	final double recgThreshold = 60;
	
	//User login dialog
	//UserLoginDialog loginDialog = null;
	
	//New user dialog.
	AddUserDialog addUserDlg = null;
	
	//Face library update parameters
	final int maxUpdate = 3;
	Mat []  updateFaces = new Mat[maxUpdate];
	
	
	
	//Using for display
//	BufferedImage bufImage = null;
//	byte [] bufBytes = null;
	
	public FaceLoginInterface() {
		setTitle("AirRafer");
		
		if(!recognizer.loadDetector(resDirPath)) {
			
			JOptionPane.showMessageDialog(this,"Detector load failed!","Sorry",JOptionPane.WARNING_MESSAGE);
		}
		
		if (!new File(dataDirPath).isDirectory())
			new File(dataDirPath).mkdirs();
		recognizer.load(dataDirPath);
		
		setAutoRequestFocus(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 916, 618);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		lblNewLabel = new JLabel("display");
		lblNewLabel.setBackground(Color.BLACK);
		lblNewLabel.setBounds(10, 10, 640, 480);
		contentPane.add(lblNewLabel);

		

		btnFaceLogin = new JButton("Face Login");
		btnFaceLogin.setFont(new Font("Arial", Font.PLAIN, 13));
		btnFaceLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startRecognize();
			}
		});

		btnFaceLogin.setBounds(680, 70, 210, 50);
		contentPane.add(btnFaceLogin);
		

		btnFaceTrain = new JButton("New User");
		btnFaceTrain.setFont(new Font("Arial", Font.PLAIN, 12));
		btnFaceTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startTrain();
			}
		});
		btnFaceTrain.setBounds(680, 10, 100, 50);
		contentPane.add(btnFaceTrain);
		
		btnClear = new JButton("Clean");
		btnClear.setFont(new Font("Arial", Font.PLAIN, 12));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				clearAllUsers();
				lblMessage.setText("All data has been cleared out!");
			}
		});
		btnClear.setBounds(790, 10, 100, 50);
		
		contentPane.add(btnClear);
		
		JButton btnStop = new JButton("Stop");
		btnStop.setFont(new Font("Arial", Font.PLAIN, 13));
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				shutdownActivity();
				showBlack();
			}
		});
		btnStop.setBounds(680, 367, 210, 45);
		contentPane.add(btnStop);
		
		lblMessage = new JLabel("Hello!");
		lblMessage.setFont(new Font("Arial Black", Font.PLAIN, 15));
		lblMessage.setBounds(10, 500, 509, 38);
		contentPane.add(lblMessage);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
		lblUsername.setBounds(680, 151, 210, 23);
		contentPane.add(lblUsername);
		
		textUsername = new JTextField();
		textUsername.setFont(new Font("Arial", Font.PLAIN, 13));
		textUsername.setBounds(680, 184, 210, 23);
		contentPane.add(textUsername);

		
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
		lblPassword.setBounds(680, 217, 210, 23);
		contentPane.add(lblPassword);
		
		textPassword = new JPasswordField();
		textPassword.setFont(new Font("Arial", Font.PLAIN, 13));
		textPassword.setColumns(10);
		textPassword.setBounds(680, 250, 210, 23);
		contentPane.add(textPassword);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setFont(new Font("Arial", Font.PLAIN, 13));
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				pressLogin();
			}
		});
		btnLogin.setBounds(755, 295, 135, 38);
		contentPane.add(btnLogin);
		getRootPane().setDefaultButton(btnLogin);
		
		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				importLibImages();
				
			}
		});
		btnImport.setFont(new Font("Arial", Font.PLAIN, 13));
		btnImport.setBounds(680, 441, 210, 40);
		contentPane.add(btnImport);
		
		JButton btnRecognizeImages = new JButton("Recognize");
		btnRecognizeImages.setFont(new Font("Arial", Font.PLAIN, 13));
		btnRecognizeImages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					recognizeLibImages();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
		});
		btnRecognizeImages.setBounds(680, 489, 210, 40);
		contentPane.add(btnRecognizeImages);
		
		
		this.addWindowListener(new WindowAdapter() {
			   public void windowClosing(WindowEvent e) {
				   
				   dispose();
			   }
			    
			    });
		

		showBlack();

		textUsername.requestFocusInWindow();
		
	}
	
	
	//Shutdown Activity: Camera.
	void shutdownActivity() {
		
		if(activityTimer != null) {
			activityTimer.stop();
		}
		if(videoCapture != null && videoCapture.isOpened()) {
			videoCapture.release();
			videoCapture = null;
		}

		textUsername.setText(null);
		textPassword.setText(null);
		lblMessage.setText(null);
	}


	double recgValue = -1;
	int recgClass = -1;
	int nAccCounter  = 0;
	int nFrameCounter = 0;
	boolean bMouthLand = false;
	int userLabel = -1;
	MouthLander mouthLander = null;
	
	//
	protected void startRecognize() {
		
		shutdownActivity();
		showBlack();
		if(videoCapture == null) {
			videoCapture = new VideoCapture();
		}
		if(!videoCapture.isOpened()) {
			
			videoCapture.open(0);
		}
		if(!videoCapture.isOpened()) {
			JOptionPane.showMessageDialog(this,"No camera found!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		Mat frame = new Mat();
		videoCapture.read(frame);
		
		recgValue = -1;
		recgClass = -1;
		nAccCounter = 0;
		nFrameCounter = 0;
		bMouthLand = false;
		userLabel = -1;
		//loginDialog.clear();
		
		for(int i=0;i<maxUpdate;i++) {
			
			updateFaces[i] = null;
		}
		
		final ActionListener action = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				if(bMouthLand) {
					mouthLandProc();
				}
				else {
					recognizeProc();
				}
			}
		};
		
		
		activityTimer = new Timer(40, action);
		activityTimer.start();
		lblMessage.setText("Recognizing...");
		
	}
	
	
	
	void recognizeProc() {
		Mat frame = new Mat();
		if(!videoCapture.read(frame)) {
			return;
		}
		
		Core.flip(frame, frame, 1);
		
		Rect face_rect = new Rect(0,0,0,0);
		Mat face_img = new Mat();
		
		boolean found = recognizer.faceDetectSample(frame, face_rect, face_img, true, false);
		
//		JNICheng.imshow("haha", face_img);
		
		nFrameCounter++;
		if(nFrameCounter >= 100) {
			
			lblMessage.setText("Recommend to use Password for login.");
			nFrameCounter = 0;
			if(!textUsername.isFocusOwner() && !textPassword.isFocusOwner()) {
				textUsername.requestFocusInWindow();
			}
		}
		
		if(!found) {
			showBGRMat(frame);
			return;
		}
		
		DetectFaceInfo info = recognizer.recognize(face_img);
	
		Core.rectangle(frame, face_rect.tl(), face_rect.br(), new Scalar(0,255,0));
		
		
		//compare the result to threshold
		int label = info.getLabel();
		
		if(label < 0) {
			showBGRMat(frame);
			return;
		}
		
		double min_dist = info.getMinDist();
		
		if(recgValue < 0 || recgClass != label) {
			recgValue = min_dist;
			recgClass = label;
			nAccCounter = 0;
		}
		else {
			recgValue = 0.5*recgValue + 0.5*min_dist;
		}
		//System.out.println(recgValue);
		
		
		//UserInfo user = recognizer.getUser(label);
		
		updateFaces[nAccCounter%maxUpdate] = info.getFaceMat();
		
		if(nAccCounter > 5 && recgValue <  recgThreshold) {
			
			//Login successful
			//loginSystem(label,user.getName());
			if(mouthLander == null) {
				mouthLander = new MouthLander(recognizer.getFaceDetector());
				mouthLander.loadDetector(resDirPath);
			}
			mouthLander.reset();
			userLabel = label;
			bMouthLand = true;
			
			UserInfo user = recognizer.getUser(label);
			
			String text = "Hello,"+user.getName()+" Please OPEN your MOUTH until unlock...";
			lblMessage.setText(text);
			return;
		}
		
		
		showBGRMat(frame);
		
		nAccCounter++;
		
	}
	
	public void mouthLandProc() {
		
		
		
		Mat frame = new Mat();
		if(!videoCapture.read(frame)) {
			return;
		}
		
		Core.flip(frame, frame, 1);
		
		UserInfo user = recognizer.getUser(userLabel);
		
		
		if(mouthLander.mouthLand(frame)) {
			
			loginSystem(userLabel,user.getName());
			return;
		}

		showBGRMat(frame);
		
		return;
		
	}
	
	public void loginSystem(int label,String name) {
		
		shutdownActivity();
		String text = "Welcome,"+name+"!\n\nThe face library has been updated.";
		JOptionPane.showMessageDialog(this,text,"Login Successful!",JOptionPane.PLAIN_MESSAGE);
		showWelcome();
		
		//update faces
		ArrayList<Mat> samples = new ArrayList<Mat>();
		//ArrayList<Integer> labels = new ArrayList<Integer>();
		
		
		for(int i=0;i<maxUpdate;i++) {
			
			if(updateFaces[i] != null) {
				samples.add(updateFaces[i]);
				//labels.add(label);
			}
		}
		recognizer.updateUser(label,samples);
		
		System.out.println("Update "+samples.size());
		
		for(int i=0;i<maxUpdate;i++) {
			
			updateFaces[i] = null;
		}
		
		
	}
	
	//Training data and parameters. 
	ArrayList<Mat> listTrainSamples = new ArrayList<Mat>();
	int trainLabel = 0;
	int trainCounter = 0;
	final int trainSampleCount = 10;
	
	
	//Train, add new user
	protected void startTrain() {
		
		shutdownActivity();
		showBlack();
		
		addUserDlg = new AddUserDialog(this,recognizer.userList);
		addUserDlg.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addUserDlg.setModal(true);
		addUserDlg.setVisible(true);
		
		
		
		
		if(!addUserDlg.isSuccessful())
			return;
	
		
		if(videoCapture == null) {
			videoCapture = new VideoCapture();
		}
		if(!videoCapture.isOpened()) {
			
			videoCapture.open(0);
		}
		if(!videoCapture.isOpened()) {
			JOptionPane.showMessageDialog(this,"No camera found!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		Mat frame = new Mat();
		videoCapture.read(frame);
		
		listTrainSamples.clear();
		
		nFrameCounter= 0;
		trainCounter = 0;
		
		trainLabel = recognizer.getNextLabel();
		
		final ActionListener action = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				trainProc();
			}
		};
		
		activityTimer = new Timer(40, action);
		activityTimer.start();
		lblMessage.setText("Training...");
	}
	
	protected void trainProc() {
		
		Mat frame = new Mat();
		if(!videoCapture.read(frame)) {
			return;
		}
		

		Core.flip(frame, frame, 1);
		
		
		
		Rect face_rect = new Rect(0,0,0,0);
		Mat face_img = new Mat();
		
		boolean found = recognizer.faceDetectSample(frame, face_rect, face_img, true, false);
		if(found) {
			
			Core.rectangle(frame, face_rect.tl(), face_rect.br(), new Scalar(0,255,0));
		}
		showBGRMat(frame);
		
		if(nFrameCounter++ < 10) {
			return;
		}
		nFrameCounter = 0;
		if(found) {
			trainCounter++;
			listTrainSamples.add(face_img);
		}
		
		int rate = 100*trainCounter/trainSampleCount;
		lblMessage.setText("Waiting..."+rate+"%");
		
		
		if(trainCounter >= trainSampleCount) {
			
			
			shutdownActivity();
			showBlack();
			if(listTrainSamples.size() == trainSampleCount) {
				
				recognizer.addUser(new UserInfo(addUserDlg.getUserName(),addUserDlg.getUserPwd()),
						listTrainSamples, trainLabel);
				String text = "Training finished!";
				JOptionPane.showMessageDialog(this,text,"Congratulations",JOptionPane.PLAIN_MESSAGE);
			}
		}
		
	}
	
	
	//Clean all users.
	protected void clearAllUsers() {
		
		shutdownActivity();
		showBlack();
		recognizer.clear();
	}
	
	protected void pressLogin() {
		if(textUsername.getText().equals(""))
		{
			JOptionPane.showMessageDialog(this,"Username is empty!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		else if(String.valueOf(textPassword.getPassword()).equals(""))
		{
			JOptionPane.showMessageDialog(this,"Password is empty!","Sorry",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean login = false;
		int idx = 0;
		UserInfo luser = null;
		for(UserInfo user : recognizer.userList) {
			
			if(textUsername.getText().equals(user.getName()) && String.valueOf(textPassword.getPassword()).equals(user.getPassword())) {
				login = true;
				luser = user;
				break;
			}
			idx++;
			
		}
		
		if(login) {
			loginSystem(idx, luser.getName());
		}
		else {
			JOptionPane.showMessageDialog(this,"Incorrect Username or Password!","Sorry",JOptionPane.WARNING_MESSAGE);
		}
	}
	
	
	
	public void importLibImages() {
		
		
		//chose file for training
		
		JFileChooser fileChooser = new JFileChooser();

		fileChooser.setCurrentDirectory(new File("test"));
		fileChooser.setMultiSelectionEnabled(true);

		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { //set available suffix

				String name = f.getName();
				if(name.endsWith(".jpg")||name.endsWith(".png")||name.endsWith(".bmp")||name.endsWith(".pgm")||
						name.endsWith(".jpeg")||name.endsWith(".gif")||f.isDirectory()){
					return true;
				}
				return false;
			}
			public String getDescription() {
				return "*.jpg|*.jpeg|*.png|*.bmp|*.pgm|*.gif";
			}
		});


		int returnValue = fileChooser.showOpenDialog(getContentPane());
		
		if (returnValue != JFileChooser.APPROVE_OPTION) {
			
			return;
		}


		File [] files = fileChooser.getSelectedFiles();

		ArrayList<File> list = new ArrayList<File>();


		for(File file : files) {

			list.add(file);
		}

		while(!list.isEmpty()) {

			File file = list.get(0);
			String name = file.getName();
			
			
			String [] splits = name.split("_");
			
			
			
			//format incorrect
			if(splits[0].equals(name)) {
				list.remove(0);
				continue;
			}
			String user_pwd = splits[0];
			ArrayList<Mat> samples = new ArrayList<Mat>();

			int label = recognizer.getNextLabel();


			//select all images from the same user
			for(int i=0,e=list.size();i<e;i++) {

				File f = list.get(i);
				
				if(f.getName().startsWith(user_pwd)) {


					
					String path = f.getAbsolutePath();
				
					
					Mat mat = new Mat();
					JNICheng.loadImage(path, 0, mat);
					
					
					samples.add(mat);

					list.remove(i);
					i--;
					e--;
				}

			}


			//add new user
			recognizer.addUser(new UserInfo(user_pwd,user_pwd), samples, label);	
			samples.clear();


		}
		

	}
	



	public void recognizeLibImages() throws IOException {
		
		
		//choose file for recognizing
		JFileChooser fileChooser = new JFileChooser();

		fileChooser.setCurrentDirectory(new File("test"));
		fileChooser.setMultiSelectionEnabled(true);

		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { //set available suffix

				String name = f.getName();
				if(name.endsWith(".jpg")||name.endsWith(".png")||name.endsWith(".bmp")||name.endsWith(".pgm")||
						name.endsWith(".jpeg")||name.endsWith(".gif")||f.isDirectory()){
					return true;
				}
				return false;
			}
			public String getDescription() {
				return "*.jpg|*.jpeg|*.png|*.bmp|*.pgm|*.gif";
			}
		});


		int returnValue = fileChooser.showOpenDialog(getContentPane());
		
		if (returnValue != JFileChooser.APPROVE_OPTION) {
			return;
		}
	
		
		File [] files = fileChooser.getSelectedFiles();
		
		File csv = new File("P-unhard_face.csv");
		
		BufferedWriter output=new BufferedWriter(new FileWriter(csv));
	     

	    //File dir = fileChooser.getCurrentDirectory();
		
		int i = 0;
	    
		for(File file : files) {
			
			//file.renameTo(new File(dir.getAbsolutePath()+'/'+dir.getName()+'_'+file.getName()));
			
			String path = file.getAbsolutePath();
			
			Mat mat = new Mat();
			
			JNICheng.loadImage(path, 0, mat);
			
			DetectFaceInfo dfi = recognizer.recognize(mat);

			int label = dfi.getLabel();
		
			if(dfi.getMinDist() <= 60) {
				
				UserInfo user = recognizer.getUser(label);
				if(file.getName().startsWith(user.getName())) {
					i++;
				}
				output.write(file.getName()+','+user.getName()+"\n");
			}
			else {
				
				output.write(file.getName()+", \n");
			}
			
			output.flush();
			
			
		}
		
		System.out.println(i);
		
		output.flush();
		output.close();
		
		JOptionPane.showMessageDialog(this,"Recognition has finished","OK",JOptionPane.INFORMATION_MESSAGE);
	
	}
	
	public void dispose() {
		
	
		this.shutdownActivity();
		if (!new File(dataDirPath).isDirectory())
			new File(dataDirPath).mkdirs();
		recognizer.save(dataDirPath);
		
		System.out.println("save");
		setVisible(false);
	}
	
	
	IplImage []showIplImage = {null};
	public void showBGRMat(Mat mat) {
		

		
		if(mat.type() != CvType.CV_8UC3)
			return;

		
		if(showIplImage[0] == null || showIplImage[0].width() != mat.width() || showIplImage[0].height() != mat.height()) {
			int depth = ConvertHelper.depthMatToIplImage(mat.depth());
			showIplImage[0] = IplImage.create(mat.width(),mat.height(), depth, mat.channels());
		}
	
		
		IplImage [] ipl = {new IplImage()};
		ConvertHelper.convertMatToIplImageNoCopy(mat, ipl);
		cvCopy(ipl[0],showIplImage[0]);
		setImage(showIplImage[0].getBufferedImage());
		
		
//		BufferedImage bimg = new BufferedImage(mat.width(),mat.height(),BufferedImage.TYPE_3BYTE_BGR);
//		DataBuffer out = bimg.getData().getDataBuffer();
//		
//		  byte[] a = ((DataBufferByte)out).getData();
//          mat.get(0, 0, a);
//          
//          setImage(bimg);

		
        
		

//		int width = mat.width(), height = mat.height();
//		if(bufImage == null || bufImage.getWidth() != width || bufImage.getHeight() != height) {
//			
//			bufImage = new BufferedImage(width, height,
//					BufferedImage.TYPE_3BYTE_BGR);
//			bufBytes = new byte[width * height * 3];
//		}
//		
//		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
//
//		mat.get(0, 0, bufBytes);
//
//		WritableRaster wr = bufImage.getRaster();
//		wr.setDataElements(0, 0, width, height, bufBytes);
//
//		setImage(bufImage);
	}
	
	public void showBlack() {
		
		Mat mat = new Mat(lblNewLabel.getHeight(),lblNewLabel.getWidth(),CvType.CV_8UC3);
		mat.setTo(new Scalar(0));
		showBGRMat(mat);
	}
	public void showWelcome() {
		
		Mat mat = new Mat(lblNewLabel.getHeight(),lblNewLabel.getWidth(),CvType.CV_8UC3);
		mat.setTo(new Scalar(0));
		Core.putText(mat, "Welcome!", new Point(mat.width()/4,mat.height()/2), Core.FONT_ITALIC, 2, new Scalar(0,255,0));
		showBGRMat(mat);
	}

	protected void setImage(BufferedImage input) {
		try {

			ImageIcon ic = new ImageIcon(input);
			lblNewLabel.setIcon(ic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
