import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import ij.*;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
//import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.superpixels.*;

public class test {
	static int x = 1360;
	static int y = 976;
	
	public static void main(String[] args) {
		String path = "/home/hc2008/Image/image/klotho/EGFR-1/08-4888D1-N3.tif";
		ImagePlus img2 = IJ.openImage(path);
		img2.setRoi(0, 0, x, y);
		//ImageProcessor ip2 = img2.getProcessor().crop();
		ImagePlus img = cropImg(img2, 0, 0, x, y);
		ImageProcessor ip = img.getProcessor();
		img.show();
		//ImageConverter ic = new ImageConverter(img);
		//ic.convertToGray8();
		FloatArray2DSIFT.Param param = new FloatArray2DSIFT.Param();
		ArrayList<Feature> feat = new ArrayList<Feature>();
		SIFT si = new SIFT(new FloatArray2DSIFT(param));
		si.extractFeatures(ip, feat);
		float[] f = new float[2];
		int x, y;
		ImagePlus imgblank = IJ.createImage("blank", "8-bit white", img.getWidth(), img.getHeight(), 1);
		imgblank.show();
		IJ.run("Make Binary","");
		imgblank.hide();
		ImageProcessor ipblank = imgblank.getProcessor();
		ipblank.setColor(new Color(0));
		
		
		long[] featD = new long[feat.size()];
		for (int i = 0; i < feat.size(); i++){
			 f = feat.get(i).location;
			 x = (int) f[0];
			 y = (int) f[1];
			 featD[i] = x*x + y*y;
			 ipblank.drawPixel(x, y);

		}
		imgblank.updateAndDraw();

		//imgblank.show();
		
		jSLIC js = new jSLIC(img);
		js.process(30, 0.2F);
		Labelling2D ld = js.getSegmentation();
		ld.showOverlapROIs(img);
		RoiManager rm = RoiManager.getInstance();
		Roi[] rois = rm.getRoisAsArray();

		int[] r;
		int[] roisD = new int[rois.length];
		for (int i = 0; i < rois.length; i++){
			imgblank.setRoi(rois[i]);
			ImageStatistics is = ipblank.getStatistics();
			r = is.histogram;
			roisD[i] =  r[255];
			//ip.drawString(Integer.toString(roisD[i]), (int) is.xCentroid, (int) is.yCentroid);
		}
		
		//img.updateAndDraw();
		//img.show();
		img.hide();
		drawStringExceed(0, img, rois, roisD);
		rm.runCommand("Show All");
		
		
		ArrayList <Integer> hi = new ArrayList <Integer>();
		int[] hid = roisD;
		Arrays.sort(hid);
		int temp = 0;
		for (int i = 0; i < hid.length; i++){
			if (hid[i] != temp){
				hi.add(hid[i]);
				temp = hid[i];
				//System.out.println(hid[i]);
			}
		}
		
		
		
		Colour_Deconvolution cdr = new Colour_Deconvolution(); 
		ArrayList<ImagePlus> S = cdr.run(img); //S.get(0) H, S.get(1) DAB, S.get(2) Other
		
		
	}
	
	public static ImagePlus cropImg(ImagePlus img2, int x0, int y0, int x1, int y1){
		ImageProcessor ip1 = img2.getProcessor();
		ip1.setRoi(0, 0, 1360, 976);
		ImageProcessor ip2 = ip1.crop();
		BufferedImage croppedImage = ip2.getBufferedImage();
		ImagePlus img = new ImagePlus("Original", croppedImage);
		return(img);
	}
	
	public static Roi[] drawStringExceed(int d, ImagePlus img, Roi[] rois, int[] roisD){
		ArrayList<Roi> indexes = new ArrayList<Roi>();
		ImagePlus temp = img.duplicate();
		temp.setTitle(Integer.toString(d));
		ImageProcessor tp = temp.getProcessor();
		tp.setColor(255);
		for (int i = 0; i < rois.length; i++){
			if (roisD[i] > d){
			temp.setRoi(rois[i]);
			ImageStatistics is = tp.getStatistics();
			tp.drawString(Integer.toString(roisD[i]), (int) is.xCentroid, (int) is.yCentroid);
			indexes.add(rois[i]);
			}else{
			tp.setColor(new Color(255,255,255));
			tp.fill(rois[i]);

			}
		}
		
		temp.updateAndDraw();
		temp.show();
		
		Roi[] index = new Roi[indexes.size()];
		
		for (int i = 0; i < indexes.size(); i++){
			index[i] = indexes.get(i);
		}
		
		return index;
	}	

}
