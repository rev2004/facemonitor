package background;
import org.opencv.core.Core;
import org.opencv.core.Mat;


public class CRClassifier {

	long addressCR = 0;
	public CRClassifier(int comp) {
		
		addressCR = crClassifier(comp);
	}
	
	private native long crClassifier(int comp);
	private native void train(long samples, long labels);
	private native void predict(long sample, int [] label, double [] dist);
	
	public void train(Mat samples, Mat labels) {
		
		train(samples.nativeObj, labels.nativeObj);
	}
	
	public int predict(Mat sample) {
		
		int [] label = new int[]{-1};
		double [] dist = new double[]{Double.MAX_VALUE};
		
		predict(sample.nativeObj,label,dist);
		
		return label[0];
	}
	
	public static void main(String [] args) {
		
		
		System.loadLibrary("dll/"+Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("dll/opencv_core245");
		System.loadLibrary("dll/opencv_highgui245");
		System.loadLibrary("dll/JNIChengCRC");
		
		
		Mat samples = new Mat();
		Mat labels = new Mat();
		
		
		
		
		//CRClassifier cr = new CRClassifier(200);
		//cr.train(samples, labels);
		
	}
	
	
}
