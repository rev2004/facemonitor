package JUnitTest;

//import static org.junit.Assert.*;
//import org.junit.Test;

import org.opencv.core.*;
import org.opencv.highgui.*;

import java.awt.List;
import java.util.ArrayList;
import java.util.Random;



import background.DetectFaceInfo;
import background.UserInfo;
import background.WhuFaceRecognizer;

public class FaceRecgTest {

	
	int threshold = 35;
	//@Test
	public void testErrorRate() {
		
		System.loadLibrary("dll/"+Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("dll/opencv_core245");
		System.loadLibrary("dll/opencv_imgproc245");
		System.loadLibrary("dll/JNIOpenCV");
		
		
		
			
		WhuFaceRecognizer recognizer = new WhuFaceRecognizer();
		recognizer.loadDetector("res");
		String dir_path = "res/att_faces";
		
		
		int total = 0;
		int error = 0;
		int rej = 0;
		
		ArrayList<ArrayList<Mat>> imagesAll = new ArrayList<ArrayList<Mat>>();
		ArrayList<Integer> labelsAll = new ArrayList<Integer>();
		
		
		
		
		for(int ei = 0; ei < 100 ; ei++) {
			
			
			//random imagesAll
			//random labelsAll
			//random sub set of imagesAll;
			
			for(int tri=0;tri<30;tri++)  {
				
				ArrayList<Mat> gm = imagesAll.get(tri);
				ArrayList<Mat> ugm = new ArrayList<Mat>();
				for(int i=0;i<5;i++) {
					
					ugm.add(gm.get(i));
				}
				
				
				int label = labelsAll.get(tri);
				
				recognizer.addUser(new UserInfo(Integer.toString(label), Integer.toString(label)), ugm, label);
			}
			
			
			for(int tti=0;tti<30;tti++) {
				
				ArrayList<Mat> gm = imagesAll.get(tti);
				int label = labelsAll.get(tti);
				
				for(int i=5;i<10;i++) {
					
					Mat mat = gm.get(i);
					DetectFaceInfo info = recognizer.recognize(mat);
					
					if(info.getMinDist() < threshold) {
						
						if(info.getLabel() == label) {
							error++;
						}
						
					}
					else {
						rej++;
					}
					
				}
			}
			for(int tti=30;tti<40;tti++) {
				
				ArrayList<Mat> gm = imagesAll.get(tti);
				
				for(int i=0;i<10;i++) {
					
					Mat mat = gm.get(i);
					DetectFaceInfo info = recognizer.recognize(mat);
					
					if(info.getMinDist() < threshold) {
						error++;
					}
					
				}
			}
			
			
			
		}
		
		

		
		

	
		
		
	}
	
	//@Test
	public void testRejectRate() {
		
		System.loadLibrary("dll/"+Core.NATIVE_LIBRARY_NAME);
		System.loadLibrary("dll/opencv_core245");
		System.loadLibrary("dll/opencv_imgproc245");
		System.loadLibrary("dll/JNIOpenCV");
		
		
		
			
		WhuFaceRecognizer recognizer = new WhuFaceRecognizer();
		recognizer.loadDetector("res");
		String dir_path = "res/att_faces";
		
		int total = 0;
		int failed = 0;
		
		for(int train_label=1;train_label<=40;train_label++)  {
			
			
			String train_path = dir_path + "/s" + train_label + "/";

			ArrayList<Mat> samples = new ArrayList<Mat>();
			ArrayList<Integer> labels = new ArrayList<Integer>();
			
			int [] array_id = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
			Random rand = new Random(train_label);
			for(int i=0;i<10;i++) {
				
				int r = i + rand.nextInt(10-i);
				int temp = array_id[i];
				array_id[i] = array_id[r];
				array_id[r] = temp;
				
			}

			for (int i = 1; i <= 6; i++) {

				String path = train_path + i + ".pgm";

				Mat mat = Highgui.imread(path);
				samples.add(mat);
				labels.add(train_label);
			}

			recognizer.trainSamples(samples, labels);


			
			
			for (int i = 7; i <= 10; i++) {
				String path = dir_path + "/s" + train_label + '/' + i + ".pgm";

				Mat mat = Highgui.imread(path);
				DetectFaceInfo info = recognizer.recognize(mat);

				if (info.getMinDist() >= threshold) {
					failed++;
				}
				total++;

			}
			
			recognizer.clear();
			
			//float ratio = (float)failed/total;
			//System.out.println(ratio);
			
		}
		

		
		float ratio = (float)failed/total;
		
		System.out.println(ratio);
		//assertTrue(ratio < 0.2);
		
		
	}


}
