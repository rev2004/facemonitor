package background;




import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_READ;



import static com.googlecode.javacv.cpp.opencv_core.CV_NODE_MAP;
import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_WRITE;
import static com.googlecode.javacv.cpp.opencv_core.cvEndWriteStruct;
import static com.googlecode.javacv.cpp.opencv_core.cvGetFileNodeByName;
import static com.googlecode.javacv.cpp.opencv_core.cvOpenFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReadByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReadIntByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReadStringByName;
import static com.googlecode.javacv.cpp.opencv_core.cvStartWriteStruct;
import static com.googlecode.javacv.cpp.opencv_core.cvWrite;
import static com.googlecode.javacv.cpp.opencv_core.cvWriteInt;
import static com.googlecode.javacv.cpp.opencv_core.cvWriteString;


import org.opencv.core.Core;


import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core.CvFileNode;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;






import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class WhuFaceRecognizer {

	Size trainSize = new Size(120, 150);
	CascadeClassifier faceDetector = new CascadeClassifier();
	CascadeClassifier eyeDetector = new CascadeClassifier();
	MyFaceModel faceModel = null;

	final double rotateAngle = 5;
	
	public ArrayList<UserInfo> userList = new ArrayList<UserInfo>(); 
	

	public WhuFaceRecognizer() {
		faceModel = new MyFaceModel();
	}
	
	public void release() {
		faceDetector = null;
		eyeDetector = null;
	}
	
	public WhuFaceRecognizer(int width,int height, int radius, int neighbors, int gridx, int gridy) {
		
		trainSize = new Size(width,height);
		faceModel = new MyFaceModel(radius,neighbors,gridx,gridy,Double.MAX_VALUE);
	}

	public WhuFaceRecognizer(int width,int height) {
		
		trainSize = new Size(width,height);
		faceModel = new MyFaceModel();
	}

	public CascadeClassifier getFaceDetector() {
		return this.faceDetector;
	}
	
	public int getNextLabel() {
		return userList.size();
	}
	
	
	public void load(String path) {
		loadUserData(path+"/user.xml");
		faceModel.loadModel(path+"/model.xml");
	}
	public void save(String path) {
		saveUserData(path+"/user.xml");
		faceModel.saveModel(path+"/model.xml");
	}
	
	
	public void saveUserData() {
		
		DBHelper helper = new DBHelper("postgres");
		
		helper.executeUpdate("delete from data");
		
		for(int i=0,e=userList.size();i<e;i++) {
			
			UserInfo info = userList.get(i);
			helper.executeUpdate("insert into data (uid, name, pwd) values ("
			+i+
		    ", '"+info.getName()+
		    "', '" +
					info.getPassword()+"');");
			
		}
		helper.close();
		
	}
	public void loadUserData() {
		
		
		DBHelper helper = new DBHelper("postgres");
		
		ResultSet rs = helper.executeQuery("select * from data");
		
		try {
			while(rs.next()) {
				
				userList.add(rs.getInt(1), new UserInfo(rs.getString(2),rs.getString(3)) );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		helper.close();
	}
	
	public void saveUserData(String path) {
		
		CvFileStorage fileStorage = cvOpenFileStorage( path, null, CV_STORAGE_WRITE, null);
		if(fileStorage == null)
			return;
		
		
		int groups = userList.size();
		
		cvWriteInt(fileStorage, "gourps", groups); 

		for (int i = 0; i < groups; i++) {
			

			cvStartWriteStruct(fileStorage, "group_"+i, CV_NODE_MAP, "group");
			
			cvWriteInt(fileStorage,"id",i);
			
			UserInfo info = userList.get(i);
			cvWriteString(fileStorage, "name", info.getName(), 0);
			cvWriteString(fileStorage, "pwd", info.getPassword(), 0);
			
			cvEndWriteStruct(fileStorage);
		
			
		}
		cvReleaseFileStorage(fileStorage);
	}
	
	
	public void loadUserData(String path) {
		CvFileStorage fileStorage = cvOpenFileStorage( path, null, CV_STORAGE_READ, null);
		if(fileStorage == null)
			return;
		
		int groups = cvReadIntByName(fileStorage,null, "gourps",0); 

		for (int i = 0; i < groups; i++) {
			
			
			CvFileNode map = cvGetFileNodeByName(fileStorage, null, "group_"+i);
			
			int idx = cvReadIntByName(fileStorage,map,"id",0);
			
			String name = cvReadStringByName(fileStorage,map,"name");
			String pwd = cvReadStringByName(fileStorage,map,"pwd");
			
			userList.add(new UserInfo(name,pwd));
			
			
		}
		cvReleaseFileStorage(fileStorage);
	}
	
	
	public void clear() {
		userList.clear();
		faceModel.clear();
	}
	
	
	public void addUser(UserInfo info, ArrayList<Mat> samples, int label) {
		
		
		
		ArrayList<Integer> labels = new ArrayList<Integer>();
		for(int i=0;i<samples.size();i++) {
			labels.add(label);
		}
		
		//System.out.println(label);
		
		trainSamples(samples,labels);
		userList.add(info);
	}
	
	public void updateUser(int label, ArrayList<Mat> samples) {
		
		int total = samples.size();
		
		faceModel.cleanRedundancy(label, total);
		
		ArrayList<Integer> labels = new ArrayList<Integer>();
		for(int i=0;i<samples.size();i++) {
			labels.add(label);
		}
		trainSamples(samples,labels);
		
		
		
	}
	
	public void fitToTrainSize(Mat src, Mat dst) {
		
		Imgproc.resize(src, dst, trainSize);
	
	}
	
	
	public void trainSamples(ArrayList<Mat> samples, ArrayList<Integer> labels) {
		
		for(int i=0,e=samples.size();i<e;i++) {
			
			Mat fit = new Mat();
			fitToTrainSize(samples.get(i),fit);
			samples.set(i, fit);
		
		}
		
		faceModel.train(samples, labels);
	}
	
	public DetectFaceInfo recognize(Mat img) {
		
		Integer [] label = new Integer[]{-1};
		Double [] dist = new Double[]{Double.MAX_VALUE};
		Mat avg = new Mat();
		
		Mat fit = new Mat();
		fitToTrainSize(img,fit);
		
		faceModel.predict(fit, label, dist, avg);
		

		DetectFaceInfo face_info = new DetectFaceInfo(label[0], dist[0], avg, null, img);
		
		return face_info;
	}
	



	public boolean loadDetector(String path) {


		boolean flag1 = faceDetector.load(path + "/haarcascade_frontalface_alt.xml");
		boolean flag2 = eyeDetector.load(path + "/haarcascade_mcs_eyepair_big.xml");
		return flag1 && flag2;
	}

//	public boolean faceDetectSample(Mat frame, Rect mask, ArrayList<Mat> face_imgs,
//			boolean enhance, boolean epc, boolean draw) {
//
//		Mat face_img = new Mat();
//		boolean ret = this.faceDetectSample(frame, mask, face_img, enhance, epc, draw);
//		if(ret)
//			face_imgs.add(face_img);
//		return ret;
//	}
	
	int minNeighbours = 20;
	
	public boolean faceDetectSample(Mat frame, Rect mask, Mat face_img,
			boolean enhance, boolean crop) {

		MatOfRect faces = new MatOfRect();
		MatOfRect eyepairs = new MatOfRect();

		Mat gray = new Mat(frame.size(), CvType.CV_8UC1);

		if (frame.channels() == 1) {
			frame.copyTo(gray);
		} else {
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
		}
		//Imgproc.equalizeHist(gray, gray);
		
		faceDetector.detectMultiScale(gray, faces, 1.1, minNeighbours, 
				Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_FIND_BIGGEST_OBJECT, trainSize, new Size(gray.width(),gray.height()));

		
		
		if (faces.total() != 1) {
			//System.out.println(faces.total());
			return false;
		}

		Rect face = faces.toArray()[0];

		Rect eyepair = null;
		
		Mat sub_mat;

		if (enhance) {
			sub_mat = gray.submat(new Range(face.y,
					(int) (face.y + face.height)), new Range(face.x,
					face.x + face.width));
			
			Size min_size = new Size(face.width/3,face.height/8);
			Size max_size = new Size(face.width,face.height/2);

			eyeDetector.detectMultiScale(sub_mat, eyepairs,1.1,3,Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_FIND_BIGGEST_OBJECT, min_size,max_size);

			Rect[] eyerects = eyepairs.toArray();

			if (eyerects.length < 1) {
				//System.out.println("no eyes");
					return false;
			}
			
			if(crop){
				eyepair = eyerects[0];

				float alpha = 0.5f;

				face.x += eyepair.x * (1 - alpha);
				face.width = (int) (eyepair.x * alpha + eyepair.width + (face.width
						- eyepair.width - eyepair.x)
						* alpha);

				face.y += eyepair.y * 0.5;
				face.height -= eyepair.y * 0.5;
			}

		}

		if (face.width < trainSize.width || face.height < trainSize.height || face.x < 0 || face.y < 0 ||
				face.x +face.width >= gray.width() || face.y+face.height >= gray.height())
			return false;
		if ((float)Math.min(face.width, face.height) / (float)Math.max(face.width, face.height) < 0.3) {
			
			return false;
		}

		sub_mat = gray.submat(new Range(face.y, face.y + face.height),
				new Range(face.x, face.x + face.width));
		
		

		mask.x = face.x; mask.y = face.y; mask.width = face.width; mask.height = face.height;
		sub_mat.copyTo(face_img);


//		face_img.create(trainSize, CvType.CV_8UC1);
//
//
//		Imgproc.resize(sub_mat, face_img, face_img.size());
		

//		if(draw) {
//			Core.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0));
//		}

		return true;

	}
	
	public boolean faceDetectSamples(Mat frame, ArrayList<Rect> masks, ArrayList<Mat> face_imgs,
			boolean enhance, boolean crop) {

		MatOfRect faces = new MatOfRect();
		MatOfRect eyepairs = new MatOfRect();

		Mat gray = new Mat(frame.size(), CvType.CV_8UC1);

		if (frame.channels() == 1) {
			frame.copyTo(gray);
		} else {
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
		}
		//Imgproc.equalizeHist(gray, gray);
		
		faceDetector.detectMultiScale(gray, faces, 1.1, minNeighbours, Objdetect.CASCADE_DO_CANNY_PRUNING, trainSize, new Size(gray.width(),gray.height()));

		
		
		if (faces.total() < 1) {
			//System.out.println(faces.total());
			return false;
		}

		
		for( int i=0,e=(int) faces.total();i<e;i++) {
			
			Rect face = faces.toArray()[i];

			Rect eyepair = null;

			Mat sub_mat = null;

			if (enhance) {
				sub_mat = gray.submat(new Range(face.y,
						(int) (face.y + face.height)), new Range(face.x,
								face.x + face.width));

				Size min_size = new Size(face.width/3,face.height/8);
				Size max_size = new Size(face.width,face.height/2);

				eyeDetector.detectMultiScale(sub_mat, eyepairs,1.1,3,Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_FIND_BIGGEST_OBJECT, min_size,max_size);

				Rect[] eyerects = eyepairs.toArray();

				if (eyerects.length < 1) {
					//System.out.println("no eyes");
					continue;
				}
				
				if(crop) {
					eyepair = eyerects[0];

					float alpha = 0.5f;

					face.x += eyepair.x * (1 - alpha);
					face.width = (int) (eyepair.x * alpha + eyepair.width + (face.width
							- eyepair.width - eyepair.x)
							* alpha);

					face.y += eyepair.y * 0.5;
					face.height -= eyepair.y * 0.5;
				}

			}

			if (face.width < trainSize.width || face.height < trainSize.height || face.x < 0 || face.y < 0 ||
					face.x +face.width >= gray.width() || face.y+face.height >= gray.height())
				continue;
			
			if ((float)Math.min(face.width, face.height) / (float)Math.max(face.width, face.height) < 0.3) {

				continue;
			}

			sub_mat = gray.submat(new Range(face.y, face.y + face.height),
					new Range(face.x, face.x + face.width));


			
			masks.add(face);
			face_imgs.add(sub_mat.clone());


//			if(draw) {
//				Core.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0));
//			}
		}

		return true;

	}
	

	

	
	static void rotate(Mat src, double angle, Mat dst)
	{
	    int len = Math.max(src.cols(), src.rows());
	    Point pt = new Point(len/2., len/2.);
	    Mat r = Imgproc.getRotationMatrix2D(pt, angle, 1.0);
	    
	    Imgproc.warpAffine(src, dst, r, new Size(len, len));
	}

	public UserInfo getUser(int i) {
		
		if( i >= 0 && i < userList.size()) {
			
			return userList.get(i);
		}
		return null;
	}
	
	public Size getTrainSize() {
		
		return this.trainSize;
	}
	
//	public boolean recognize(Mat frame, boolean enhance) {
//		
//		ArrayList<DetectFaceInfo> result = new ArrayList<DetectFaceInfo>();
//		
//		
//		recognize(frame, enhance, result, false);
//		
//		if(result.size() > 0) {
//			
//			return true;
//		}
//		return false;
//		
//	}
//
//	public void recognize(Mat frame, boolean enhance, ArrayList<DetectFaceInfo> result, boolean draw)  {
//
//
//		MatOfRect faces = new MatOfRect();
//		MatOfRect eyepairs = new MatOfRect();
//
//		Mat gray = new Mat(frame.size(), CvType.CV_8UC1);
//
//		if (frame.channels() == 1) {
//			frame.copyTo(gray);
//		} else {
//			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
//
//		}
//		//Imgproc.equalizeHist(gray, gray);
//
//		faceDetector.detectMultiScale(gray, faces, 1.1, minNeighbours, Objdetect.CASCADE_DO_CANNY_PRUNING, trainSize, new Size(gray.width(),gray.height()));
//
//		Rect[] rects = faces.toArray();
//
//		for (int i = 0; i < rects.length; i++) {
//
//			Rect face = rects[i];
//
//			if(draw)
//				Core.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0));
//
//
//			Mat sub_mat;
//			if(enhance) {
//				sub_mat = gray.submat(new Range(face.y, face.y
//						+ face.height), new Range(face.x, face.x + face.width));
//
//				Size min_size = new Size(face.width / 3, face.height / 8);
//				Size max_size = new Size(face.width, face.height / 2);
//				eyeDetector.detectMultiScale(sub_mat, eyepairs, 1.1, 3,
//						Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_FIND_BIGGEST_OBJECT, min_size, max_size);
//
//				Rect[] eyerects = eyepairs.toArray();
//
//				if (eyerects.length < 1) {
//					continue;
//				}
//
//				Rect eyepair = eyerects[0];
//
//				
//
//				float alpha = 0.5f;
//
//				face.x += eyepair.x * (1 - alpha);
//				face.width = (int) (eyepair.x * alpha + eyepair.width + (face.width
//						- eyepair.width - eyepair.x)
//						* alpha);
//
//				face.y += eyepair.y * 0.5;
//				face.height -= eyepair.y * 0.5;
//			}
//
//			if (face.width < trainSize.width || face.height < trainSize.height || face.x < 0 || face.y < 0 ||
//					face.x +face.width >= gray.width() || face.y+face.height >= gray.height())
//				continue;
//
//			if(draw) {
//				Core.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 255));
//			}
//			sub_mat = gray.submat(new Range(face.y, face.y + face.height),
//					new Range(face.x, face.x + face.width));
//			
//			
//			Mat face_img = new Mat(trainSize, CvType.CV_8UC1);
//	
//
//			Imgproc.resize(sub_mat, face_img, face_img.size());
//
//			Integer [] label = new Integer[]{-1};
//			Double [] dist = new Double[]{Double.MAX_VALUE};
//			Mat avg = new Mat();
//			
//			
//			faceModel.predict(face_img, label, dist, avg);
//			
//	
//			DetectFaceInfo face_info = new DetectFaceInfo(label[0],dist[0],avg, face, face_img);
//			result.add(face_info);
//		}
//
//	}
}
