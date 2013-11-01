package background;



import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
import org.opencv.highgui.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;


public class MouthLander{

	
	Rect prevRect = null;
	Rect mouthRect = null;
	Rect mouthShift = null;
	int shiftCounter = 0;
	boolean isBooting = true;
	
	RotatedRect mouthEll = null;
	RotatedRect ellShift = null;
	int ellCounter = 0;
	
	OFTracker noseTracker = new OFTracker();
	
	CascadeClassifier mouthDetector = new CascadeClassifier();
	CascadeClassifier faceDetector = null;

	
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
		
		VideoCapture cap = new VideoCapture(0);
		MouthLander lander = new MouthLander(null);
		lander.loadDetector("res");
		
		Mat frame = new Mat();
		
		IplImage [] ipl = {new IplImage()};
		
		ipl[0] = IplImage.create(frame.width(),frame.height(), ConvertHelper.depthMatToIplImage(frame.depth()), frame.channels());
		
		int timer = 0;
		
		while(cap.read(frame)) {
			
			Core.flip(frame, frame, 1);
			boolean good =lander.mouthLand(frame);
			if(good) {
				//JOptionPane.showMessageDialog(null,"Mouth Opened!","Good",JOptionPane.WARNING_MESSAGE);
				timer = 20;
				
			}
			if(timer > 0) {
				timer--;
				Core.putText(frame, "Good", new Point(0,30), Core.FONT_ITALIC, 1, new Scalar(0,255,0));
			}
			ConvertHelper.convertMatToIplImage(frame, ipl);
			cvShowImage("image",ipl[0]);
			char key = JNICheng.waitKey(10);
			if(key == 27)
				break;
			else if(key == 'r') {
				lander.reset();
			}
		}
		
		cap.release();
		

		
		
		
	}
	

	
	
	public MouthLander( CascadeClassifier fd) {
		
		this.faceDetector = fd;
		reset();
		

	}
	
	public void release() {
		
		if(noseTracker != null) {
			noseTracker.release();
			noseTracker = null;
		}
	}

	public void loadDetector(String path) {

		mouthDetector.load(path + "/haarcascade_mcs_mouth.xml");
		if(faceDetector == null) {
			faceDetector = new CascadeClassifier();
			faceDetector.load(path + "/haarcascade_frontalface_alt.xml");
		}
	}
	
	public void reset() {
		
		isBooting = true;
		readyLand();
		readyBoot();
		
	}
	
	void readyLand() {
		
		ellCounter = 0;
		ellShift = new RotatedRect();
		ellShift.center.x = ellShift.center.y = ellShift.size.width = ellShift.size.height = ellShift.angle = 0;
		
	}
	
	void readyBoot() {
		
		shiftCounter = 0;
		mouthShift = new Rect(0,0,0,0);
		mouthRect = new Rect(0,0,0,0);
	}

	void bootUp(Mat frame) {

		MatOfRect faces = new MatOfRect();
		MatOfRect mouths = new MatOfRect();

		Mat gray = new Mat(frame.size(), CvType.CV_8UC1);

		if (frame.channels() == 1) {
			frame.copyTo(gray);
		} else {
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
		}
		// Imgproc.equalizeHist(gray, gray);

		faceDetector.detectMultiScale(gray, faces, 1.1, 3,
				Objdetect.CASCADE_DO_CANNY_PRUNING
						| Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(100,
						100), new Size(gray.width(), gray.height()));

		if (faces.total() != 1) {
			return;
		}
		
		

		Rect face = faces.toArray()[0];
		
		Core.rectangle(frame, face.tl(), face.br(), new Scalar(0,255,0));

		Mat sub_mat;
		
		Rect sub_face = new Rect(face.x,(int)(face.y+face.height*0.667),face.width,face.height/2);
		
		sub_face = this.adjustCaptureRect(sub_face, gray.size());

		sub_mat = gray.submat(sub_face);

		Size min_size = new Size(face.width / 6, face.height / 6);
		Size max_size = new Size(face.width, face.height / 2);

		mouthDetector.detectMultiScale(sub_mat, mouths, 1.1, 3,
				Objdetect.CASCADE_DO_CANNY_PRUNING
						| Objdetect.CASCADE_FIND_BIGGEST_OBJECT, min_size,
				max_size);

		if (mouths.total() != 1) {
			return;
		}
		
		Rect m_rect = mouths.toArray()[0];
		
		m_rect.x += sub_face.x;
		m_rect.y += sub_face.y;
		
		
		//Core.rectangle(frame, m_rect.tl(), m_rect.br(), new Scalar(0,255,0));
		
		if(shiftCounter > 0) {
	
			mouthShift.x += m_rect.x - prevRect.x;
			mouthShift.y += m_rect.y - prevRect.y;
			mouthShift.width += m_rect.width - prevRect.width;
			mouthShift.height += m_rect.height - prevRect.height;
			
		}
		prevRect = m_rect;
		mouthRect.x += m_rect.x;
		mouthRect.y += m_rect.y;
		mouthRect.height += m_rect.height;
		mouthRect.width += m_rect.width;
		
		shiftCounter++;
		
		if(Math.abs(mouthShift.x) > 20 || Math.abs(mouthShift.y) > 20 || 
				Math.abs(mouthShift.width) > 20 || Math.abs(mouthShift.height) > 20) {
			readyBoot();
		}
		else  if(shiftCounter >= 5){
			isBooting = false;
			readyLand();
			
			mouthRect.x /= shiftCounter;
			mouthRect.y /= shiftCounter;
			mouthRect.width /= shiftCounter;
			mouthRect.height /= shiftCounter;
			
			mouthRect.height *= 1.5;

			 
			
			Rect nose = new Rect();
			nose.x = (int) (face.x + face.width * 0.4);
			nose.width = (int) (face.width * 0.2);
			nose.y = (int) (face.y + face.height * 0.5);
			nose.height = (int) (face.height * 0.2);
			noseTracker.startTracking(gray, nose, 30, 3, 0.5f);
			
		}

	}
	Rect adjustCaptureRect(Rect rect, Size bound_size) {
	    
		Rect m_rect = new Rect(0,0,0,0);
		
	    int lx = 0;
	    int ly = 0;
	    
	    lx = rect.x;
	    
	    m_rect.x = lx > 0 ? lx : 0;
	    
	    
	    ly = rect.y;
	    
	    m_rect.y = ly > 0 ? ly : 0;
	    
	    
	    
	    
	    if(rect.x + rect.width > bound_size.width) {
	        
	        m_rect.width = (int) (bound_size.width - rect.x);
	    }
	    else {
	    	m_rect.width = rect.width;
	    }
	    
	    if(rect.y + rect.height > bound_size.height) {
	        
	        m_rect.height = (int) (bound_size.height - rect.y);
	    }
	    else {
	    	m_rect.height = rect.height;
	    }
	    
	    return m_rect;
	}
	
	public boolean mouthLand(Mat frame) {
		
		if(isBooting) {
			bootUp(frame);
			return false;
		}
		
		Mat gray = new Mat(frame.size(), CvType.CV_8UC1);

		if (frame.channels() == 1) {
			frame.copyTo(gray);
		} else {
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
		}
		
		if(!noseTracker.nextObjectRect(gray)) {
			readyBoot();
			isBooting = true;
			return false;
		}
		
		Point vec = noseTracker.getMovVector();
		
		mouthRect.x += vec.x;
		mouthRect.y += vec.y;
		
		Rect m_rect = adjustCaptureRect(mouthRect,frame.size());
		
		if(m_rect.width <= mouthRect.width/2 || m_rect.height <= mouthRect.height/2) {
			readyLand();
			return false;
		}
		

		
		Mat sub_frame = frame.submat(m_rect);
		RotatedRect ell = new RotatedRect();
		
		if(!JNICheng.getMouthEllipse(sub_frame, ell)) {
			
			readyLand();
			
			//Core.rectangle(frame, mouthRect.tl(), mouthRect.br(), new Scalar(255,0,0));
			return false;
		}
		
		ell.center.x += m_rect.x;
		ell.center.y += m_rect.y;
		
		if(ellCounter > 0) {
	
			ellShift.center.x += ell.center.x - mouthEll.center.x;
			ellShift.center.y += ell.center.y - mouthEll.center.y;
			ellShift.size.width += ell.size.width - mouthEll.size.width;
			ellShift.size.height += ell.size.height - mouthEll.size.height;
			ellShift.angle += ell.angle - mouthEll.angle;
			
		}
		
		this.mouthEll = ell;
		
		ellCounter++;
		
		boolean good = false;
		
		if(Math.abs(ellShift.center.x) > 10 || Math.abs(ellShift.center.y) > 10 || 
				Math.abs(ellShift.size.width) > mouthRect.width/6 || Math.abs(ellShift.size.height) > mouthRect.height/6) {
			
			readyLand();
			//Core.ellipse(frame, ell, new Scalar(0,255,0));
		}
		else if(ellCounter > 15){
			
			good = true;
			readyLand();
			Core.ellipse(frame, ell, new Scalar(0,255,255));
		}
		else if(ellCounter > 7) {
			Core.ellipse(frame, ell, new Scalar(0,255,0));
		}
		
		
		//Core.rectangle(frame, mouthRect.tl(), mouthRect.br(), new Scalar(0,255,0));
		
	
		
		return good;
		
	}
}
