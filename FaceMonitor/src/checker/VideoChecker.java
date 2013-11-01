package checker;

import java.util.ArrayList;












import javax.swing.ImageIcon;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import frontground.VideoMonitorInterface;
import background.ConvertHelper;
import background.MyVideoCapture;
import static com.googlecode.javacv.cpp.opencv_core.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;



public class VideoChecker extends JFrame {
	private JButton btnPlay;
	private JButton btnLoad;
	private JLabel lblSpeedx;
	private JSlider sliderSpeedx;
	public String exportPath = null;
	
	
	public VideoChecker() {
		
		setBounds(100,100,392,275);
		((JComponent) getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().setLayout(null);
		
		btnPlay = new JButton("Play");
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				playVideo();
				
			}
		});
		btnPlay.setBounds(132, 10, 112, 46);
		getContentPane().add(btnPlay);
		
		JButton btnStop = new JButton("Stop");
		btnStop.setBounds(254, 10, 112, 46);
		getContentPane().add(btnStop);
		btnStop.setVisible(false);
		
		btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				loadTarget();
			}
		});
		btnLoad.setBounds(10, 10, 112, 46);
		getContentPane().add(btnLoad);
		
		lblSpeedx = new JLabel("Speed: 1.0x");
		lblSpeedx.setFont(new Font("Arial", Font.PLAIN, 14));
		lblSpeedx.setBounds(10, 90, 95, 29);
		getContentPane().add(lblSpeedx);
		
		sliderSpeedx = new JSlider();
		sliderSpeedx.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				int value = sliderSpeedx.getValue();
				if(value >= 5) {
					speedx = value - 4;
				}
				else {
					speedx = (float)value/5;
				}
				lblSpeedx.setText("Speed: "+Float.toString(speedx)+"x");
			}
		});
		sliderSpeedx.setValue(5);
		sliderSpeedx.setMaximum(9);
		sliderSpeedx.setMinimum(1);
		sliderSpeedx.setBounds(10, 129, 356, 23);
		getContentPane().add(sliderSpeedx);
		
		JLabel lblPressescTo = new JLabel("Press 'ESC' to stop");
		lblPressescTo.setFont(new Font("Arial", Font.PLAIN, 14));
		lblPressescTo.setBounds(10, 180, 182, 29);
		getContentPane().add(lblPressescTo);
		
		
	}

	
	//all targets
	VideoTarget playBoy = null;
	
	int interval = 1000;
	float speedx = 1.0f;
	
	
	public void loadTarget() {

		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.setMultiSelectionEnabled(false);
		if(exportPath != null) {
			fileChooser.setCurrentDirectory(new File(exportPath));
		}
		
		fileChooser.setFileFilter(new FileFilter() {
		      public boolean accept(File f) { //set available suffix
		    	  
		    	  String name = f.getName();
		          if(name.endsWith(".xml")||f.isDirectory()){
		            return true;
		          }
		          return false;
		        }
		        public String getDescription() {
		          return "*.xml";
		        }
		      });
		
		
		int returnValue = fileChooser.showOpenDialog(getContentPane());
		// chose OK to store these paths
		if (returnValue == JFileChooser.APPROVE_OPTION) {

			
			File file = fileChooser.getSelectedFile();
			try {
				playBoy = readTarget(file);
			} catch (ParserConfigurationException e) {
				
				playBoy = null;
				e.printStackTrace();
			} catch (SAXException e) {
				playBoy = null;
				e.printStackTrace();
			} catch (IOException e) {
				playBoy = null;
				e.printStackTrace();
			}
		}
	}

	
	public VideoTarget readTarget(File file) throws ParserConfigurationException, SAXException, IOException {
		
		
		
		VideoTarget target = new VideoTarget();
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  DocumentBuilder builder = dbf.newDocumentBuilder();
		  Document doc = builder.parse(file); 
		  
		  Element root = doc.getDocumentElement(); 
		  this.interval = Integer.parseInt(root.getAttribute("interval"));
		  
		  NodeList videos = root.getElementsByTagName("video");
		 
		  for (int i = 0; i < videos.getLength(); i++) {

			  Element video = (Element) videos.item(i);
			  VideoContainer container = new VideoContainer();

			  container.videoPath = video.getAttribute("path");

			  NodeList kfrms = video.getElementsByTagName("frame");

			  for(int j=0; j < kfrms.getLength(); j++) {
				  
				  Element kfrm = (Element) kfrms.item(j);
				  
				  int f = Integer.parseInt(kfrm.getAttribute("f"));
				  int x = Integer.parseInt(kfrm.getAttribute("x"));
				  int y = Integer.parseInt(kfrm.getAttribute("y"));
				  int w = Integer.parseInt(kfrm.getAttribute("w"));
				  int h = Integer.parseInt(kfrm.getAttribute("h"));
				  
				  
				  container.addFrame(new Rect(x,y,w,h), f);
				  
			  }
			  target.targetInVideos.add(container);
		  }
		  return target;
	}

	

	
	VideoPlayThread videoPlayThread = null;
	
	public void playVideo() {
	
		if(playBoy == null)
			return;
		
		VideoTarget boy = playBoy;
		
		for(int i=0,e=boy.targetInVideos.size();i<e;i++) {
			
			String path = boy.targetInVideos.get(i).videoPath;
			MyVideoCapture cap = new MyVideoCapture(path);
			
			boy.targetInVideos.get(i).videoCap = cap;
			
		}
	
		
		VideoPlayThread thread = new VideoPlayThread();
		thread.setParameters(this, boy, this.interval, this.speedx);
		
		//thread.start();
		
		thread.run();
		videoPlayThread = thread;
		
		
	}
	
	public void stopPlay(VideoTarget boy) {
		
		
		System.out.println("stop");
		for(int i=0,e=boy.targetInVideos.size();i<e;i++) {
			
			MyVideoCapture cap = boy.targetInVideos.get(i).videoCap;
			cap.release();
			 boy.targetInVideos.get(i).videoCap = null;
			
		}
		
	}
	
//	IplImage []showIplImage = {null};
//	public void showBGRMat(Mat mat) {
//		
//
//		
//		if(mat.type() != CvType.CV_8UC3)
//			return;
//
//		
//		if(showIplImage[0] == null || showIplImage[0].width() != mat.width() || showIplImage[0].height() != mat.height()) {
//			int depth = ConvertHelper.depthMatToIplImage(mat.depth());
//			showIplImage[0] = IplImage.create(mat.width(),mat.height(), depth, mat.channels());
//		}
//	
//		
//		IplImage [] ipl = {new IplImage()};
//		ConvertHelper.convertMatToIplImageNoCopy(mat, ipl);
//		cvCopy(ipl[0],showIplImage[0]);
//		setImage(showIplImage[0].getBufferedImage());
//	}
	
//	public void showBlack() {
//		mat.setTo(new Scalar(0));
//		showBGRMat(mat);
//	}
//	
//	
//	protected void setImage(BufferedImage input) {
//		try {
//
//			ImageIcon ic = new ImageIcon(input);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public void createAInfo(String exportPath) {
		
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Document target_doc = null;
		Element root = null;
		
		target_doc = builder.newDocument();

		root = target_doc.createElement("info");
		target_doc.appendChild(root);
		
		Element ele_video = target_doc.createElement("video");
		ele_video.setAttribute("path", "hahahahh");
		root.appendChild(ele_video);
		
		Element kfrm = target_doc.createElement("frame");
		kfrm.setAttribute("f", Integer.toString(12345));
		kfrm.setAttribute("x", Integer.toString(1));
		kfrm.setAttribute("y", Integer.toString(2));
		kfrm.setAttribute("w", Integer.toString(3));
		kfrm.setAttribute("h", Integer.toString(4));
		
		
		
		ele_video.appendChild(kfrm);
		
		try {

			
				File outfile = new File(exportPath+"info.xml");
				FileOutputStream fos = new FileOutputStream(outfile);
				OutputStreamWriter outwriter = new OutputStreamWriter(fos);
				callWriteXmlFile(target_doc, outwriter, "gb2312");
				outwriter.close();
				fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	public static void callWriteXmlFile(Document doc, Writer w, String encoding) throws TransformerFactoryConfigurationError, TransformerException {

		   Source source = new DOMSource(doc);
		   Result result = new StreamResult(w);
		   Transformer xformer = TransformerFactory.newInstance()
		     .newTransformer();
		   xformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		   xformer.transform(source, result);
		
	}
	
	 public void toSave(String filename, Document doc){    
		 try{    
			 TransformerFactory tf = TransformerFactory.newInstance();    
			 Transformer transformer = tf.newTransformer();    
			 DOMSource source = new DOMSource(doc);    
			 transformer.setOutputProperty(OutputKeys.ENCODING,"GB2312");    
			 //transformer.setOutputProperty(OutputKeys.INDENT,"yes");    
			 PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
			 StreamResult result = new StreamResult(pw);    
			 transformer.transform(source,result);  
			 pw.close();
		 }    
		 catch(TransformerException mye){    
			 mye.printStackTrace();    
		 }    
		 catch(IOException exp){    
			 exp.printStackTrace();    
		 }    
	 }  
		    

	public static void main(String [] args) {
		
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
		
	
	
		

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				VideoChecker frame = new VideoChecker();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
