package checker;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import background.JNICheng;
import background.MyVideoCapture;
import background.OFTracker;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class VideoPlayThread extends Thread {

	
	VideoTarget playBoy = null;
	VideoChecker videoChecker = null;
	int interval = 100;
	float speedx = 1.0f;
	
	public void setParameters(VideoChecker checker, VideoTarget boy, int itl, float spd)  {
		
		this.playBoy = boy;
		this.videoChecker = checker;
		this.interval = itl;
		this.speedx = spd;
	}
	
	public void run() {
		
		System.out.println("play");
		
		ArrayList <VideoContainer> contains = playBoy.targetInVideos;
		
		for( VideoContainer contain : contains) {
			
			MyVideoCapture cap = contain.videoCap;
			ArrayList<Rect> rects = contain.targetRects;
			ArrayList<Integer> frames = contain.targetFrames;
			
			try {
				trackingVideo(cap, rects, frames);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		
		}
		
		videoChecker.stopPlay(playBoy);
		
	}
	public void trackingVideo(MyVideoCapture cap, ArrayList<Rect> rects, ArrayList<Integer> frames) throws InterruptedException {
		
		int total = (int) cap.get(CV_CAP_PROP_FRAME_COUNT);
		int fps = (int) cap.get(CV_CAP_PROP_FPS);
		
		int plymsec = (int) (1000/speedx/fps);
		
		
		
		OFTracker tracker = new OFTracker();
		boolean tracking = false;
		Rect track_rect = null;
		int counter = 0;
		
		int timer = 0;
		
		Mat frame = new Mat();
		Mat gray = new Mat();
		
		for(int i=0,e=rects.size();i<e;i++) {
			
			Rect tag_rect = rects.get(i);
			int tag_frm = frames.get(i);
			
			
			
			if( timer + fps*2 < tag_frm && i > 0) {
				Mat somm = new Mat();
				Imgproc.blur(gray, somm, new Size(7,7));
				
				JNICheng.imshow("video", somm);
				char c = JNICheng.waitKey(1);
				if( c == 27 )
					break;
			}
			
			
			while(timer++ < tag_frm) {
				if(!cap.read(frame))
					break;
			}
			
			//cap.set(CV_CAP_PROP_POS_FRAMES, (int)(tag_frm-1));
			
			
			
			if(!cap.read(frame))
				break;
			timer++;
			
			
			Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
			
			tracking = tracker.startTracking(gray, tag_rect, 50, 3, 0.6f);
			
			Core.rectangle(frame, tag_rect.tl(), tag_rect.br(), new Scalar(0,255,0),1);
			track_rect = tag_rect;
			
			//System.out.println("track");
			
			
			JNICheng.imshow("video", frame);
			char c = JNICheng.waitKey(plymsec);
			if( c == 27 )
				break;
			
			counter = 1;
			
			
			while(tracking) {
				
				if(!cap.read(frame)) {
					break;
				}
				timer++;
				
				Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
				
				tracking =  tracker.nextObjectRect(gray);
				if(++counter >= interval) {
					tracking = false;
				}
				
				if(tracking) {
					
					Point vec = tracker.getMovVector();
					track_rect.x += vec.x;
					track_rect.y += vec.y;
					Core.rectangle(frame, track_rect.tl(), track_rect.br(), new Scalar(0,255,0));
					
					//play this frame
					
					JNICheng.imshow("video", frame);
					c = JNICheng.waitKey(plymsec);
					if( c == 27 )
						break;
				}
				
				
			}
			if( c == 27 )
				break;
			
			
		}
		tracker.release();
		JNICheng.destroyWindow("video");
		
	}
}


