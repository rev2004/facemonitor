package background;

import org.opencv.core.*;

public class JNICheng {
	
	private static native void elbp(long srcAddress, long destAddress,int radius, int neighbors);
	private static native void histc(long srcAddress, long destAddress,int minVal, int maxVal, boolean normed); 
	private static native void spatialHistogram(long srcAddress, long destAddress,int numPatterns,int grid_x, int grid_y, boolean normed);
	
	private static native void loadImage(byte [] name, int len, int flags, long destAddress);
	
	private static native void imshow(byte [] name, long srcAddress);
	private static native void namedWindow(byte [] name);
	private static native void destroyWindow(byte [] name);
	public static native char waitKey(int delay);
	private static native boolean getMouthEllipse(long srcAddress, double [] ell);
	
	
	public static void loadImage(String name, int flags, Mat dst) {
		
		loadImage(name.getBytes(), name.length(), flags, dst.nativeObj);
	}
	
	public static void elbp(Mat src, Mat dst, int radius, int neighbors) {
		
		elbp(src.nativeObj,dst.nativeObj,radius,neighbors);
	}
	
	public static void histc(Mat src, Mat dst, int minVal, int maxVal, boolean normed) {
		
		histc(src.nativeObj,dst.nativeObj,minVal,maxVal,normed);
	}
	
	public static void spatialHistogram(Mat src, Mat dst, int numPatterns, int grid_x, int grid_y, boolean normed) {
		
		spatialHistogram(src.nativeObj,dst.nativeObj,numPatterns,grid_x,grid_y,normed);
	}
	
	public static void imshow(String name, Mat src) {
		
		imshow(name.getBytes(),src.nativeObj);
	}
	
	public static void namedWindow(String name) {
		
		namedWindow(name.getBytes());
	}
	public static void destroyWindow(String name) {
		
		destroyWindow(name.getBytes());
	}
	public static boolean getMouthEllipse(Mat frame, RotatedRect ell) {
		
		double [] array = new double[5];
		boolean ret = getMouthEllipse(frame.nativeObj, array);
		ell.center.x = array[0];
		ell.center.y = array[1];
		ell.size.width = array[2];
		ell.size.height = array[3];
		ell.angle = array[4];
		
		return ret;
	}

	
	public static void main(String []args) {
		
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
		
		Point pt = new Point();
		Rect rect = new Rect();
		RotatedRect rot = new RotatedRect();
		
		
		Mat mat = Mat.eye(100, 100, CvType.CV_8UC1);
	
		
		Mat ret = new Mat();
		JNICheng.elbp(mat,ret, 1, 8);
		
		JNICheng.loadImage("C:\\a.jpg", 0, ret);
		
		System.out.println(rect.toString());
		
		JNICheng.namedWindow("haha");
		JNICheng.imshow("haha", ret);
		
		JNICheng.waitKey(-1);
		
		
	}

}
