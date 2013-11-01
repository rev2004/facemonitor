package background;

import org.opencv.core.*;


public class OFTracker {
	
	long addressTracker = 0;
	private native long createOFTracker();
	private native boolean startTracking(long addrObj, long addrMat, int [] rect, int corners, int min_d, float rate);
	private native boolean nextObjectRect(long addrObj, long addrMat);
	private native void release(long addrObj);
	private native void getMovVector(long addrObj, int [] vector);
	
	public OFTracker() {
		
		addressTracker = createOFTracker();
	}

	public boolean startTracking(Mat mat, Rect rect, int corners, int min_d, float rate) {
		
		int [] recarray = new int[]{rect.x,rect.y,rect.width,rect.height};
		return startTracking(addressTracker,mat.getNativeObjAddr(),recarray,corners,min_d,rate);
	}
	public boolean nextObjectRect(Mat mat) {
		return nextObjectRect(addressTracker,mat.getNativeObjAddr());
	}
	public void release() {
		release(addressTracker);
	}
	public Point getMovVector() {
		
		int [] vec = new int[]{0,0};
		getMovVector(addressTracker,vec);
		Point pt = new Point();
		pt.x = vec[0];
		pt.y = vec[1];
		return pt;
	}
}
