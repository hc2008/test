import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;

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
		ImagePlus img = cropImg(img2, 0, 0, x, y);
		ImageProcessor ip = img.getProcessor();
		img.show();
		
		ArrayList<Feature> feat = doSift(ip);
		
		ImagePlus imgblank = IJ.createImage("blank", "8-bit white", img.getWidth(), img.getHeight(), 1);
		imgblank.show();
		IJ.run("Make Binary","");
		imgblank.hide();
		ImageProcessor ipblank = imgblank.getProcessor();
		ipblank.setColor(new Color(0));
		
		ArrayList<Feature> nFeat = removeFeatbyScale(feat,5.8F);
		for (int i = 0; i < nFeat.size(); i++) ipblank.drawPixel((int) nFeat.get(i).location[0], (int) nFeat.get(i).location[1]);
		imgblank.updateAndDraw();
		imgblank.show();
		
		
		Roi[] rois = doSlic(img, 30, 0.3F);
		//int[] roisD = countFeatureDotbyRoi(rois, imgblank, ipblank);

		img.hide();
		//drawStringExceed(0, img, rois, roisD);
		RoiManager rm = RoiManager.getInstance();
		rm.runCommand("Show All");
		
		
		Integer[][] sx = new Integer[rois.length][2];
		Integer[][] sy = new Integer[rois.length][2];
		
		for (int i = 0; i < rois.length; i++){
			sx[i][0] = i;
			sy[i][0] = i;
			sx[i][1] = (int) rois[i].getBounds().getCenterX();
			sy[i][1] = (int) rois[i].getBounds().getCenterY();
		}
				
		sx = sort2DInteger(sx);
		sy = sort2DInteger(sy);
		
		int[] featInRois = findFeatinRoi(nFeat, rois, sy);
		
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		ArrayList<Feature> toRemoveFeature = new ArrayList<Feature>();
		
		for (int i = 0; i < featInRois.length; i++){
			if (featInRois[i] == -1) {
				toRemove.add(i);
				toRemoveFeature.add(nFeat.get(i));
			}
		}
		
		int[] toRemoveInt  = new int[toRemove.size()];
		
		for (int i = 0; i < toRemove.size(); i++) toRemoveInt[i] = toRemove.get(i);
		nFeat.removeAll(toRemoveFeature);
		featInRois = ArrayUtils.removeAll(featInRois, toRemoveInt);
		ArrayList<Integer[]> rf = RoiContainingFeats(featInRois);
		
	
		
		/*
		Integer[][] FeatRois = new Integer[featInRois.length][2];
		for (int i = 0; i < featInRois.length; i++) {
			FeatRois[i][0] = i;
			FeatRois[i][1] = featInRois[i];
		}
		
		FeatRois = sort2DInteger(FeatRois);
		
		ArrayList<Integer[]> RoiContainFeat = new ArrayList<Integer[]>();
		int temp = FeatRois[0][1];
		ArrayList<Integer> feats = new ArrayList<Integer>();
		feats.add(temp);
		for (int i = 0; i < FeatRois.length; i++){
			if (FeatRois[i][1] == temp){
				feats.add(FeatRois[i][0]);
			}else {
				Integer[] t = new Integer[feats.size()];
				for (int j = 0; j < feats.size(); j++) t[j] = feats.get(j);
				RoiContainFeat.add(t);
				feats.clear();
				temp = FeatRois[i][1];
				feats.add(temp);
				feats.add(FeatRois[i][0]);
			}
			
		}
		*/
		

	
		//imgblank.show();

		rm.runCommand("Show All");

		/*
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
		
		/*
		Colour_Deconvolution cdr = new Colour_Deconvolution(); 
		ArrayList<ImagePlus> S = cdr.run(img); //S.get(0) H, S.get(1) DAB, S.get(2) Other
		ImagePlus S2 = S.get(2);
		ImageProcessor S2p = S2.getProcessor();
		S2p.setColor(255);
		ArrayList<Feature> feat1 = doSift(S2p);
		for (int i = 0; i < feat1.size(); i++){
			 f = feat1.get(i).location;
			 g = feat1.get(i).orientation;
			 s = feat1.get(i).scale;
			 
			 x = (int) f[0];
			 y = (int) f[1];
			 featD[i] = x*x + y*y;
			 
			 x1 = (int) (x + s * Math.cos((double) g));
			 y1 = (int) (y + s * Math.sin((double) g));
			 
			 ipblank.reset();
			 ipblank.drawPixel(x, y);
			 //if (s < 5.8) 
			 //S2p.drawLine(x, y, x1, y1);
			 //if (s < 6) 
			 if (s > 4 && s < 8)	 ipblank.drawLine(x, y, x1, y1);
		}
		imgblank.updateAndDraw();
		imgblank.show();
		rm.runCommand("Show All");
		
		S2.show();
		rm.runCommand("Show All");
		*/
		
		//imgblank.close();
		img.close();
		
		/*
		double h,w;
		int r;
		for (int i = 0; i < rois.length; i++){
			//ImageStatistics is = ipblank.getStatistics();
			//System.out.println(i);
			Rectangle rt = rois[i].getBounds();
			h = rt.getHeight();
			w = rt.getWidth();
			r = (int) Math.round((float) 100*h/w);
			if (r < 80 || r > 120) ipblank.drawString(Integer.toString(r), (int) rt.getCenterX(), (int) rt.getCenterY());	
			
		}
		imgblank.updateAndDraw();
		imgblank.show();
		rm.runCommand("Show All");
		*/		

	}
	
	public static ArrayList<Integer[]> RoiContainingFeats(int[] featInRois){
		Integer[][] FeatRois = new Integer[featInRois.length][2];
		for (int i = 0; i < featInRois.length; i++) {
			FeatRois[i][0] = i;
			FeatRois[i][1] = featInRois[i];
		}
		
		FeatRois = sort2DInteger(FeatRois);
		
		ArrayList<Integer[]> RoiContainFeat = new ArrayList<Integer[]>();
		int temp = FeatRois[0][1];
		ArrayList<Integer> feats = new ArrayList<Integer>();
		feats.add(temp);
		for (int i = 0; i < FeatRois.length; i++){
			if (FeatRois[i][1] == temp){
				feats.add(FeatRois[i][0]);
			}else {
				Integer[] t = new Integer[feats.size()];
				for (int j = 0; j < feats.size(); j++) t[j] = feats.get(j);
				RoiContainFeat.add(t);
				feats.clear();
				temp = FeatRois[i][1];
				feats.add(temp);
				feats.add(FeatRois[i][0]);
			}
			
		}
		
		return RoiContainFeat;
	}
	
	public static ArrayList<Feature> removeFeatbyScale(ArrayList<Feature> feat, float crit){
		ArrayList<Feature> tFeat = new ArrayList<Feature>();
		for (int i = 0; i < feat.size(); i++) tFeat.add(feat.get(i));
		
		float s;
		int[] ind = new int[tFeat.size()];
		for (int i = 0; i < tFeat.size(); i++){
			 s = tFeat.get(i).scale;
			 if (s >= crit)  ind[i] = 1;
		}
		for (int i: ind){
			if (i == 1) tFeat.remove(i);
		}
		return tFeat;
		
	}
	
	public static int[] findFeatinRoi(ArrayList<Feature> feat, Roi[] rois, Integer[][] sy){
		int[] featinRoi = new int[feat.size()];
		for (int i = 0; i < featinRoi.length; i++) featinRoi[i] = -1;
		for (int i = 0; i < feat.size(); i++){
			
			int xf = (int) feat.get(i).location[0];
			int yf = (int) feat.get(i).location[1];
			int start = 0;
			int end = rois.length - 1;
	
			while (sy[start][1] <= (yf - 50)) {
				start++;
			}
			
			while (sy[end][1] >= (yf + 50)) {
				end--;
			}
				
			int flag = 0;
			for (int k = start; k <=end ; k++){
				if (rois[sy[k][0]].contains(xf, yf)){ 
					featinRoi[i] = sy[k][0];
					flag = 1;
					}
			}
			
			if (flag == 0) {
				int r = 0;
				while (r < rois.length){
					if (rois[r].contains(xf, yf)){
						featinRoi[i] = r;
						break;
					}
					r++;
				}
			}
		}
		return featinRoi;
	}
	
	public static Integer[][] sort2DInteger(Integer[][] data){
		Arrays.sort(data, new Comparator<Integer[]>() {
		    public int compare(Integer[] int1, Integer[] int2) {
		        Integer numOfKeys1 = int1[1];
		        Integer numOfKeys2 = int2[1];
		        return numOfKeys1.compareTo(numOfKeys2);
		    }
		});
		return data;
	}
	
	public static ArrayList<Feature> doSift(ImageProcessor ip){
		FloatArray2DSIFT.Param param = new FloatArray2DSIFT.Param();
		ArrayList<Feature> feat = new ArrayList<Feature>();
		SIFT si = new SIFT(new FloatArray2DSIFT(param));
		si.extractFeatures(ip, feat);
		return feat;
	}
	
	public static Roi[] doSlic(ImagePlus img, int area, float fit){
		jSLIC js = new jSLIC(img);
		js.process(area, fit);
		Labelling2D ld = js.getSegmentation();
		ld.showOverlapROIs(img);
		RoiManager rm = RoiManager.getInstance();
		Roi[] rois = rm.getRoisAsArray();
		return rois;
	}
	
	public static ImagePlus cropImg(ImagePlus img2, int x0, int y0, int x1, int y1){
		ImageProcessor ip1 = img2.getProcessor();
		ip1.setRoi(0, 0, 1360, 976);
		ImageProcessor ip2 = ip1.crop();
		BufferedImage croppedImage = ip2.getBufferedImage();
		ImagePlus img = new ImagePlus("Original", croppedImage);
		return(img);
	}
	
	public static int[] countFeatureDotbyRoi (Roi[] rois, ImagePlus imgblank, ImageProcessor ipblank ){
		int[] roisD = new int[rois.length];
		int[] r;
		for (int i = 0; i < rois.length; i++){
			imgblank.setRoi(rois[i]);
			ImageStatistics is = ipblank.getStatistics();
			r = is.histogram;
			roisD[i] =  r[255];
		}
		return roisD;
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
