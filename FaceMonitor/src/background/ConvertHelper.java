package background;

import static com.googlecode.javacv.cpp.opencv_core.CV_16S;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import static com.googlecode.javacv.cpp.opencv_core.CV_16U;
import static com.googlecode.javacv.cpp.opencv_core.CV_32F;
import static com.googlecode.javacv.cpp.opencv_core.CV_32S;
import static com.googlecode.javacv.cpp.opencv_core.CV_64F;
import static com.googlecode.javacv.cpp.opencv_core.CV_8S;
import static com.googlecode.javacv.cpp.opencv_core.CV_8U;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16S;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32S;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_64F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8S;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;



import java.awt.image.*;
import java.nio.DoubleBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ConvertHelper {
	

	public static void main(String [] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		IplImage image = IplImage.create(20,20,IPL_DEPTH_32F,3);
		cvZero(image);
		cvCircle(image, cvPoint(10,10), 10, cvScalarAll(255), 1, 8, 0);
		
		CvCapture cap = cvCreateFileCapture("C:\\Users\\Cheng\\Documents\\130101193609_wanghanlin_x1.wmv");
		for(int i=0;;i++) {
			
			IplImage ipl = cvQueryFrame(cap);
			if(ipl == null)
				break;
			Mat mat = new Mat();
			ConvertHelper.convertIplImageToMat(ipl, mat);
		}
		cvReleaseCapture(cap);
		System.out.println("finished");

		
		//System.out.println(mat);
		
//		Mat mat = new Mat();
//		mat.create(50,50, CV_8UC3);
//		
//		
//		for(int i=0;;i++) {
//		
//			IplImage[] abc = new IplImage[] { new IplImage() };
//			ConvertHelper.convertMatToIplImage(mat, abc);
//			System.out.println(i);
//		
//		}
		
		//cvWaitKey();
		
	}
	
	public static void convertIplImageToMat(IplImage ipl, Mat mat) {
		int width = ipl.width();
		int height = ipl.height();

		int depth = ConvertHelper.depthIplImageToMat(ipl.depth());
		
		
		mat.create(height, width, CvType.makeType(depth, ipl.nChannels()));
	

		Raster r  = ipl.getBufferedImage().getRaster();
        DataBuffer out = r.getDataBuffer();
        
        if (out instanceof DataBufferByte) {
            byte[] a = ((DataBufferByte)out).getData();
            mat.put(0, 0, a);
      
        } else if (out instanceof DataBufferDouble) {
            double[] a = ((DataBufferDouble)out).getData();
            mat.put(0, 0, a);
            
        } else if (out instanceof DataBufferFloat) {
            float[] a = ((DataBufferFloat)out).getData();
            mat.put(0, 0, a);
            
        } else if (out instanceof DataBufferInt) {
            int[] a = ((DataBufferInt)out).getData();
            mat.put(0, 0, a);
            
        } else if (out instanceof DataBufferShort) {
            short[] a = ((DataBufferShort)out).getData();
            mat.put(0, 0, a);
           
        } else if (out instanceof DataBufferUShort) {
            short[] a = ((DataBufferUShort)out).getData();
            mat.put(0, 0, a);
        }
		
		
	
	}
	

	public static void convertMatToIplImageNoCopy(Mat mat, IplImage ipl[]) {

		int depth = ConvertHelper.depthMatToIplImage(mat.depth());
		ipl[0] = IplImage.createHeader(cvSize(mat.width(), mat.height()), depth,
				mat.channels());

		long addr = mat.dataAddr();
		Pointer pt = new Pointer();
		pt.position((int) addr);
		
		
		cvSetData(ipl[0], pt, (int)( mat.width()*mat.elemSize()));

	}

	public static void convertMatToIplImage(Mat mat, IplImage ipl[]) {

		int depth = ConvertHelper.depthMatToIplImage(mat.depth());
		ipl[0] = IplImage.create(cvSize(mat.width(), mat.height()), depth,
				mat.channels());
		IplImage c = IplImage.createHeader(cvSize(mat.width(), mat.height()), depth,
				mat.channels());

		long addr = mat.dataAddr();
		Pointer pt = new Pointer();
		pt.position((int) addr);
		
		
		cvSetData(c, pt, (int)( mat.width()*mat.elemSize()));
		cvCopy(c,ipl[0]);

	}

	public static int depthMatToIplImage(int mat_depth) {

		
		int ipl_depth = Integer.MAX_VALUE;

		switch (mat_depth) {
		case CV_8U:
			ipl_depth = IPL_DEPTH_8U;
			break;
		case CV_8S:
			ipl_depth = IPL_DEPTH_8S;
			break;
		case CV_16U:
			ipl_depth = IPL_DEPTH_16U;
			break;
		case CV_16S:
			ipl_depth = IPL_DEPTH_16S;
			break;
		case CV_32S:
			ipl_depth = IPL_DEPTH_32S;
			break;
		case CV_32F:
			ipl_depth = IPL_DEPTH_32F;
			break;
		case CV_64F:
			ipl_depth = IPL_DEPTH_64F;
			break;
		default:
			ipl_depth = 0;
			break;
		}

		return ipl_depth;
	}

	public static int depthIplImageToMat(int ipl_depth) {

		int mat_depth = -1;

		switch (ipl_depth) {
		case IPL_DEPTH_8U:
			mat_depth = CV_8U;
			break;
		case IPL_DEPTH_8S:
			mat_depth = CV_8S;
			break;
		case IPL_DEPTH_16U:
			mat_depth = CV_16U;
			break;
		case IPL_DEPTH_16S:
			mat_depth = CV_16S;
			break;
		case IPL_DEPTH_32S:
			mat_depth = CV_32S;
			break;
		case IPL_DEPTH_32F:
			mat_depth = CV_32F;
			break;
		case IPL_DEPTH_64F:
			mat_depth = CV_64F;
			break;
		default:
			mat_depth = -1;
			break;
		}

		return mat_depth;
	}

}
