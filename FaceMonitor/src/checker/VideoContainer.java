package checker;

import java.util.ArrayList;


import org.opencv.core.Rect;

import background.MyVideoCapture;

public class VideoContainer {

	public MyVideoCapture videoCap;
	public String videoPath;
	public ArrayList<Rect> targetRects = new ArrayList<Rect>(); 
	public ArrayList<Integer> targetFrames = new ArrayList<Integer>();
	
	void addFrame(Rect rect, int frm) {
		
		targetRects.add(rect);
		targetFrames.add(frm);
		
	}
}
