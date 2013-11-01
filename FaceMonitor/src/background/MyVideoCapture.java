package background;

import org.opencv.core.Core;

import org.opencv.core.Mat;
import org.opencv.highgui.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class MyVideoCapture {
	private long videoCaptureAddress;
	public native long myVideoCapture();
	public native long myVideoCapture(byte[] filename);
	private native long myFileVideoCapture(byte[] filename,int len);
	public native long myVideoCapture(int device);
	public native boolean read(long videoCaptureAddress, long imageAddress);	
	public native boolean set(long videoCaptureAddress,int propId, double value);
	public native double get(long videoCaptureAddress, int propId);
	public native void releaseVideoCapture(long videoCaptureAddress);
	
	public boolean isNull() {
		if(videoCaptureAddress == 0)
			return true;
		return false;
	}
	
	public MyVideoCapture()
	{
		videoCaptureAddress = myVideoCapture();
	}
	
	public MyVideoCapture(String filename)
	{
		videoCaptureAddress = myFileVideoCapture(filename.getBytes(), filename.length());
	}
	
	public MyVideoCapture(int device)
	{
		videoCaptureAddress = myVideoCapture(device);
	}
	
	public boolean read(Mat mat)
	{
		return read(videoCaptureAddress,mat.nativeObj);
	}
	
	public boolean set(int propId, double value)
	{
		return set(videoCaptureAddress,propId,value);
	}
	
	public double get(int propId)
	{
		return get(videoCaptureAddress,propId);
	}
	
	public void release() {
		
		releaseVideoCapture(videoCaptureAddress);
	}
	public static void main(String[] args)
	{
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
		
		String path = "C:\\Users\\Cheng\\workspace\\HelloCVCheng\\res\\1.wmv";
		Mat m =new Mat();
		MyVideoCapture capture = new MyVideoCapture(path);
		
		int frames = (int)capture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
		
		
		capture.read(m);
		for(int y=0;y<10;y++) {
			for(int x=0;x<10;x++) {
				double [] d = m.get(y, x);
				System.out.println(d[0]);
			}
		}
		capture.release();
		
		System.out.println(frames);
		//m1.read(m.getNativeObjAddr());
	}
}
