package background;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class DetectFaceInfo {

	int _label = -1;
	double _minDist = Double.MAX_VALUE;
	Mat _avgDists = null;
	Rect _faceRect = null;
	Mat _faceMat = null;
	
	DetectFaceInfo(int label, double min, Mat avg, Rect rect, Mat face) {
		this._label = label;
		this._minDist = min;
		this._avgDists = avg;
		this._faceRect = rect;
		this._faceMat = face;
	}
	
	public int getLabel() {
		return _label;
	}
	public double getMinDist() {
		return _minDist;
	}
	public Mat getAvgDists() {
		return _avgDists;
	}
	public Rect getFaceRect() {
		return _faceRect;
	}
	public int getAvgMinDistLabel() {
		
		int label = -1;
		float min = Float.MAX_VALUE;
		for(int i=0;i<_avgDists.rows();i++) {
			
			float [] d = new float[1];
			_avgDists.get(i, 0, d);
			if(d[0] < min) {
				min = d[0];
				label = i;
			}
		}
		return label;
	}
	public int getRectArea() {
		return _faceRect.width * _faceRect.height;
	}
	public Mat getFaceMat() {
		
		return _faceMat;
	}
}
