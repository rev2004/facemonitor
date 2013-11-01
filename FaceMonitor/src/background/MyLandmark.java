package background;

import java.util.ArrayList;


import org.opencv.core.Mat;
import org.opencv.core.Point;

public class MyLandmark {

	private long mylandmark;
	private native long myLandmark();
	private native void loadLandmarkDetector(long ml, byte[] filename, int len);
	private native void extractLandmarkImage(long ml, long src, long dst, int pd);
	private native void calcLandmark(long ml, long src, double [] marks);
	private native void release(long ml);	
	
	
	public MyLandmark() {
		
		mylandmark = myLandmark();
	}
	public void release() {
		if(mylandmark != 0) {
			release(mylandmark);
			mylandmark = 0;
		}
	}

	public void loadLandmarkDetector(String path) {
		
		loadLandmarkDetector(mylandmark,path.getBytes(),path.length());
	}
	
	public void extractLandmarkImage(Mat src, Mat dst, int pd) {
		extractLandmarkImage(mylandmark, src.nativeObj, dst.nativeObj, pd);
	}
	
	public void calcLandmark(Mat src, ArrayList<Point> points) {
		
		double [] marks = new double[16];
		
		System.out.println("in");
		
		calcLandmark(mylandmark, src.nativeObj, marks);
		
		System.out.println("out");
		
		for(int i=0;i<8;i++) {
			
			Point pt = new Point(marks[i*2],marks[i*2+1]);
			points.add(pt);
		}
		
	}
	
}
