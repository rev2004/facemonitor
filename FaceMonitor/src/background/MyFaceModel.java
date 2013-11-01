package background;

import static com.googlecode.javacv.cpp.opencv_core.*;


import java.util.ArrayList;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;



public class MyFaceModel {
	
	
	int _grid_x;
	int _grid_y;
	int _radius;
	int _neighbors;
	double _threshold;
	
	final int _maxMatNum = 20;

	
	ArrayList<Mat> _group_histograms = new ArrayList<Mat>();
	ArrayList<Integer> _group_labels = new ArrayList<Integer>();
	//ArrayList<ArrayList<Mat>> _group_histograms = new ArrayList<ArrayList<Mat>>();
	
	MyFaceModel(int radius_, int neighbors_, int gridx, int gridy, double threshold) {
	    
		
		_radius = radius_;
		_neighbors = neighbors_;
		_grid_x = gridx;
		_grid_y = gridy;
		_threshold = threshold;
	}
	MyFaceModel() {
	    
		_radius = 1;
		_neighbors = 8;
		_grid_x = 8;
		_grid_y = 8;
		_threshold = Double.MAX_VALUE;
	}
	
	void saveModel(String path) {
		
		CvFileStorage fileStorage = cvOpenFileStorage( path, null, CV_STORAGE_WRITE, null);
		if(fileStorage == null)
			return;
		
		
		int total = _group_histograms.size();
		
		cvWriteInt(fileStorage, "total", total); 

		for (int i = 0; i < total; i++) {
			

			Mat mat = _group_histograms.get(i);
			int label = _group_labels.get(i);
			

			cvStartWriteStruct(fileStorage, "group_"+i, CV_NODE_MAP, "group");
			
			cvWriteInt(fileStorage,"label",label);
			IplImage [] ipl = new IplImage[]{new IplImage()};
			ConvertHelper.convertMatToIplImage(mat, ipl);
			
			cvWrite(fileStorage,"matrix" ,ipl[0]);

			cvEndWriteStruct(fileStorage);
		
			
		}
		cvReleaseFileStorage(fileStorage);
		
	}
	
	void loadModel(String path) {
		
		CvFileStorage fileStorage = cvOpenFileStorage( path, null, CV_STORAGE_READ, null);
		if(fileStorage == null)
			return;
		
		int total = cvReadIntByName(fileStorage,null, "total",0); 

		for (int i = 0; i < total; i++) {
			
			
		
			CvFileNode map = cvGetFileNodeByName(fileStorage, null, "group_"+i);
			
			int label = cvReadIntByName(fileStorage,map,"label",0);
			Pointer ptr = cvReadByName(fileStorage,map,"matrix");
			IplImage ipl = new IplImage(ptr);
			
			Mat mat = new Mat();
			
			ConvertHelper.convertIplImageToMat(ipl, mat);
			
			_group_histograms.add(mat);
			_group_labels.add(label);
			
			
			
		}
		cvReleaseFileStorage(fileStorage);
		
	}

	void train(ArrayList<Mat> src, ArrayList<Integer> labels) {
		
		if(src.size() == 0) {
	        return;
	        
	    } 
	    
	  
	    // check if data is well- aligned
	    if(labels.size() != src.size()) {
	       
	        return;
	        
	    }
	   
	    // append labels to _labels matrix
	    for(int idx = 0; idx < labels.size(); idx++) {
	        
	        int label = labels.get(idx);
	        if( label < 0 ) {
	            continue;
	        }
	        
	        
	        Mat eqhist = new Mat();
	        Imgproc.equalizeHist(src.get(idx), eqhist);

	        Mat lbp_image = new Mat();
	       
	        JNICheng.elbp(eqhist, lbp_image, _radius, _neighbors);
	        Mat p = new Mat();
	        JNICheng.spatialHistogram(lbp_image, p, (int)(Math.pow(2.0, (double)(_neighbors))), _grid_x, _grid_y, true);
	        
	       _group_histograms.add(p);
	       _group_labels.add(label);
	       
	    }
	}
	
	void predict(Mat src,Integer [] minClass,Double [] minDist,Mat avgDists) {
		
		if (_group_histograms.isEmpty()) {

			return;
		}

		
		int num_patterns = (int) Math.pow(2, (double) (_neighbors));
		
		
		Mat eqhist = new Mat();
		Imgproc.equalizeHist(src, eqhist);
		
		// get the spatial histogram from input image
		Mat lbp_image = new Mat();
		JNICheng.elbp(eqhist, lbp_image, _radius, _neighbors);
		

		Mat query = new Mat();
		JNICheng.spatialHistogram(lbp_image,
						query,
				num_patterns, 
				_grid_x, 
				_grid_y,
				true );
		
		
		
		// find 1-nearest neighbor
		minDist[0] = Double.MAX_VALUE;
		minClass[0] = -1;


		for (int idx = 0; idx < _group_histograms.size(); idx++) {

			
				Mat sample = _group_histograms.get(idx);
				
				if(sample.rows() != query.rows() || sample.cols() != query.cols())
					continue;
				
				
				double dist = Imgproc.compareHist(sample, query, Imgproc.CV_COMP_CHISQR);
				
			
				if(dist < minDist[0]) {
					
					minClass[0] = _group_labels.get(idx);
					minDist[0] = dist;
				}
				
				

		}

	}
	
	public void clear() {
		this._group_histograms.clear();
		this._group_labels.clear();
	}
	

	public void cleanRedundancy(int label, int num) {
		
		int total = 0;
		for(int i=0,e=_group_labels.size();i<e;i++) {
			
			int l = _group_labels.get(i);
			if( l == label ) {
				total++;
			}
		}
		if( total + num > this._maxMatNum) {
			total = total + num - this._maxMatNum;
			for(int i=0,e=_group_labels.size();i<e && total > 0;i++) {
				
				int l = _group_labels.get(i);
				if( l == label ) {
					_group_labels.remove(i);
					_group_histograms.remove(i);
					e--;
					i--;
					total--;
				}
				
			}
		}
		
		
		
	}
	

}
