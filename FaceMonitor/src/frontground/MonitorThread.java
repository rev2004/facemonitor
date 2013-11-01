package frontground;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_FPS;



import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import background.DetectFaceInfo;
import background.MyVideoCapture;
import background.WhuFaceRecognizer;

public class MonitorThread extends Thread {
	
	
	
	private VideoMonitorInterface vmi = null;
	
	private String exportPath =  null;
	private WhuFaceRecognizer recognizer = null;
	private int interval = 0;
	private int recgThreshold = 60;
	private ArrayList<String> names = null;
	private ArrayList<MyVideoCapture> arrayVideos = new ArrayList<MyVideoCapture>();
	private ArrayList<String> arrayVideoPaths = null;
	
	
	boolean switchStop = false;
	
	public MonitorThread(VideoMonitorInterface v) {
		this.vmi = v;
	}
	
	public void stopMonitor() {
		this.switchStop = true;
	}
	
	public boolean isStop() {
		return this.switchStop;
	}
	
	
	public void run() {
		
		System.out.println("run");
		
		int [] target_counter = new int[names.size()];
		
		
		//Setup key frame information saving 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (Exception e) {
		}
		
		Document [] target_doc = new Document[names.size()];
		Element [] root = new Element[names.size()];
		
		for(int i=0;i<target_counter.length;i++) {
			target_counter[i] = 0;
			target_doc[i] = builder.newDocument();
			root[i] = target_doc[i].createElement("info");
			root[i].setAttribute("interval", Integer.toString(interval));
			target_doc[i].appendChild(root[i]);
		}
		
		 
		
		
		for(int i=0,e=arrayVideos.size();i<e;i++) {
			
			MyVideoCapture cap = arrayVideos.get(i);
			String path = arrayVideoPaths.get(i);
			
			
			Element [] ele_video = new Element[names.size()];
			for(int j=0;j<target_doc.length;j++) {
				ele_video[j] = target_doc[j].createElement("video");
				ele_video[j].setAttribute("path", path);
				root[j].appendChild(ele_video[j]);
				
				
			}
			
			if(isStop()) {
				cap.release();
				continue;
			}
			try {
				monitorVideo(cap,target_counter, ele_video, target_doc);
			} catch (Exception exc) {
				
				exc.printStackTrace();
				
			}
			cap.release();
		}
		vmi.stopMonitor();
		
		try {

			for(int l=0;l<target_counter.length;l++) {
				File outfile = new File(exportPath+"/"+(l+1)+'_'+names.get(l)+"/info.xml");
				FileOutputStream fos = new FileOutputStream(outfile);
				OutputStreamWriter outwriter = new OutputStreamWriter(fos);
				callWriteXmlFile(target_doc[l], outwriter, "gb2312");
				outwriter.close();
				fos.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void setParameters(ArrayList<String> videos, ArrayList<String> names,String expath, WhuFaceRecognizer recg, int thres, int interval) {
		
		//this.videos = videos;
		this.names = names;
		this.exportPath = expath;
		this.recognizer = recg;
		this.interval = interval;
		this.recgThreshold = thres;
		this.switchStop = false;
		
		arrayVideos.clear();
		for(String path : videos) {
			
			MyVideoCapture cap = new MyVideoCapture(path);
			arrayVideos.add(cap);
		}
		arrayVideoPaths = videos;
	}

	public void monitorVideo(MyVideoCapture cap, int [] target_counter, Element [] target_video, Document [] target_doc) {
		
		int fps = (int)cap.get(CV_CAP_PROP_FPS);
		
		//sleep parameter to lower CPU engage
		int slt = 75-interval/2;

		System.out.println("start video");
		
		int counter = 0;
		int timer = 0;
		while(!this.switchStop) {
			
			
			Mat frame = new Mat();
			if(!cap.read(frame))
				break;
		
			timer++;
			
			if(++counter < interval) {
				continue;
			}
			
			vmi.addProcFrames(interval);
			counter = 0;
			
			
			ArrayList<Rect> face_rects = new ArrayList<Rect>();
			ArrayList<Mat> face_imgs = new ArrayList<Mat>();
			
			recognizer.faceDetectSamples(frame, face_rects, face_imgs, true, false);
			
			
			//store the faces in to the 'grabs'
			for(int i=0,e=face_imgs.size();i<e;i++) {
				
				Mat face = face_imgs.get(i);
				
				
				DetectFaceInfo info = recognizer.recognize(face);
				
				
				int label = info.getLabel();
				if(label >= 0 && info.getMinDist() < recgThreshold) {
					
					
					Rect r = face_rects.get(i);
					int l = label;
					
					Mat save = frame.clone();

					Core.rectangle(save, r.tl(), r.br(), new Scalar(0,255,0));
					
					if( l > target_counter.length ) {
						
						System.out.println("suck");
						continue;
						
					}
					
					Element kfrm = target_doc[l].createElement("frame");
					kfrm.setAttribute("f", Integer.toString(timer));
					kfrm.setAttribute("x", Integer.toString(r.x));
					kfrm.setAttribute("y", Integer.toString(r.y));
					kfrm.setAttribute("w", Integer.toString(r.width));
					kfrm.setAttribute("h", Integer.toString(r.height));
					
					
					target_video[l].appendChild(kfrm);
					
					
					
					int sec = timer/fps;
					int min = sec/60;
					sec = sec%60;
					String time = Integer.toString(target_counter[l]+1) + '(' + min + 'm' + sec + "s)";
					
					
					
					Highgui.imwrite(exportPath+"/"+(label+1)+'_'+names.get(l)+"/"+time+".jpg",save);
					
					
					target_counter[l]++;
				}
			
			}
			
			if(slt > 0) {
				try {

					MonitorThread.sleep(slt);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
		
		
		System.out.println("finish");
	}
	
	public static void callWriteXmlFile(Document doc, Writer w, String encoding) throws TransformerFactoryConfigurationError, TransformerException {

		   Source source = new DOMSource(doc);
		   Result result = new StreamResult(w);
		   Transformer xformer = TransformerFactory.newInstance()
		     .newTransformer();
		   xformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		   xformer.transform(source, result);
		
	}
	
	
//	MyVideoCapture currentCap;
//	int nFPS;
//	int nTimer;
//	int [] nIndexes;
//	Timer threadTimer = null;
//	int nCap = 0;
//	
//	void startMonitorTimer() {
//		final ActionListener action = new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				
//				timerProc();
//			}
//		};
//		
//		this.switchStop = false;
//		int slt = 150-interval;
//		if(slt <10)
//			slt = 10;
//		nTimer = 0;
//		
//		nIndexes = new int[recognizer.getNextLabel()];
//		nCap = 0;
//		for(int i=0;i<nIndexes.length;i++) {
//			nIndexes[i] = 0;
//		}
//		
//		currentCap = arrayVideos.get(nCap);
//		nFPS = (int)currentCap.get(CV_CAP_PROP_FPS);
//		
//
//		
//		
//		threadTimer = new Timer(slt, action);
//		threadTimer.start();
//	}
//	
//	void timerProc() {
//		
//		if(currentCap == null)
//			return;
//		
//		MyVideoCapture cap = currentCap;
//		
//		
//		Mat frame = new Mat();
//		
//		boolean finished = false;
//		
//		int counter = 0;
//		while(++counter < interval && !isStop()) {
//			nTimer++;
//			finished = !cap.read(frame.getNativeObjAddr());
//			if(finished)
//				break;
//		}
//		
//		if(finished || isStop()) {
//			
//			if(currentCap != null)
//				currentCap.release();
//			nCap++;
//			if(nCap >= arrayVideos.size()) {
//				threadTimer.stop();
//				currentCap = null;
//				vmi.stopMonitor();
//				return;
//			}
//			currentCap = arrayVideos.get(nCap);
//			nFPS = (int)currentCap.get(CV_CAP_PROP_FPS);
//			
//			return;
//			
//		}
//		
//		vmi.addProcFrames(interval);
//		counter = 0;
//		
//		
//		ArrayList<DetectFaceInfo> faces = new ArrayList<DetectFaceInfo>();
//
//		recognizer.recognize(frame,true,faces,false);
//		
//		
//		//store the faces in to the 'grabs'
//		for(DetectFaceInfo info : faces) {
//			
//			int label = info.getLabel();
//			if(label >= 0 && info.getMinDist() < 60) {
//				
//				
//				Rect r = info.getFaceRect();
//				int l = label;
//				
//				Mat save = frame.clone();
//
//				Core.rectangle(save, r.tl(), r.br(), new Scalar(0,255,0));
//				
//				
//
//				
//				
//				int sec = nTimer/nFPS;
//				int min = sec/60;
//				sec = sec%60;
//				String time = Integer.toString(nIndexes[l]+1) + '(' + min + 'm' + sec + "s)";
//				
//				
//				
//				Highgui.imwrite(exportPath+"/"+(label+1)+'_'+names.get(l)+"/"+time+".jpg",save);
//				
//				
//				nIndexes[l]++;
//			}
//		
//		}
//	}
}

