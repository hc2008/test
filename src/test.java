import java.awt.Color;
import java.awt.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JTable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import ij.*;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
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
	
	public static  void main(String[] args) {
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
		
		//ArrayList<Feature> nFeat = removeFeatbyScale(feat,6.0F);
		//for (int i = 0; i < nFeat.size(); i++) ipblank.drawOval((int) nFeat.get(i).location[0], (int) nFeat.get(i).location[1], 2,2);
		for (int i = 0; i < feat.size(); i++) ipblank.drawOval((int) feat.get(i).location[0], (int) feat.get(i).location[1], 2,2);
		imgblank.updateAndDraw();
		imgblank.show();
				
		Roi[] rois = doSlic(img, 30, 0.3F);
		/*
		TreeMap<Integer,Roi> hs = new TreeMap<Integer,Roi>();
		for (int i= 0; i < rois.length; i++) hs.put(i, rois[i]);
		
		Iterator it = hs.keySet().iterator();
		while (it.hasNext()) System.out.println((int) it.next());
					
		JTable jt = new JTable();
		*/
		Integer[][] sy = sortCenterRoi(rois);
		int[] featinRoi = findFeatinRoi(feat,rois, sy);
		rois = findRoiwithFeat(featinRoi, rois, 0, 100);
		
		//for (int i = 0; i < rois.length; i++) System.out.println(rois[i].getName());
		RoiManager rm = RoiManager.getInstance();
		rm.reset();
		for (int i = 0; i < rois.length; i++ ) rm.addRoi(rois[i]);
		
		//int[] roisD = countFeatureDotbyRoi(rois, imgblank, ipblank);
		//img.hide();
		
		//for (int i = 0; i < roisD.length; i++) System.out.println(rois[i].getName() + "\t" + roisD[i]);
		
		//String s = rois[0].getName();
		//System.out.println(Integer.parseInt(s.split(" ")[1]));
		/*
		rois = featureCountExceed(0, rois, roisD);
		RoiManager rm = RoiManager.getInstance();
		rm.runCommand("Reset");
		for (int i = 0; i < rois.length; i++ ) rm.addRoi(rois[i]);
		*/
		
		rm.runCommand("Show All");
		
		//---------------------------------------------------------
		
		sy = sortCenterRoi(rois) ;
		ArrayList<Integer[]> nb = findNB(img, sy, rois);
		
		//reiterate removing roi with 0 or 1 neighbor 
		int roisCount;
		do{
		roisCount = rois.length;
		rois = removeSmallRoiCluster(rois, nb, 2);
		sy = sortCenterRoi(rois);
		nb = findNB(img, sy, rois);
		}while(roisCount != rois.length); 
		
				
		//update roiManager
		rm.reset();
		for (int i = 0; i < rois.length; i++ ) rm.addRoi(rois[i]);
		
		
		Colour_Deconvolution cdr = new Colour_Deconvolution(); 
		ArrayList<ImagePlus> S = cdr.run(img);
		
		ImagePlus DAB = S.get(1);
		int[] densityDAB = measureDensity(DAB, rois);
		//nM[0] roi with the minimal mean (including neighbor roi) of median density of DAB, nM[1] the maximum
		int[] mM = findRoiWithMinMaxDensity(rois,nb,densityDAB);
		
	
		AutoThresholder at = new AutoThresholder();
		int th = at.getThreshold(AutoThresholder.Method.Otsu, transferArraysToHistogram256(densityDAB));
		
		for (int i = 0; i < rois.length; i++){
			img.setRoi(rois[i]);
			if (densityDAB[i] >= th) ip.fill(rois[i]);
		}
		
		img.updateAndDraw();
		img.show();
		
		
		//-------------------------------------------------------------
	
		//imgblank.hide();
		//img.show();
		
		/*
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
		
		rm.setSelectedIndexes(toRemoveInt);
		rm.runCommand("Delete");
		rm.moveRoisToOverlay(img);
		
		//computeDescriptorInnerProduct(nFeat);
		float[] ado = computeAverageDistancetoOthers(nFeat);
		
		
		/*
		for (int i = 0; i < rf.size(); i++){
			SummaryStatistics ss = new SummaryStatistics();
			Integer[] a = rf.get(i);
			double[] s = new double[(a.length -1)*(a.length - 2)/2];
			int c = 0;
			/*
			//if (a.length > 2){
			for (int j = 1; j < a.length-1; j++){
				for (int k = j+1; k < a.length; k++){
					//s[c] = nFeat.get(a[j]).descriptorDistance(nFeat.get(a[k]));
					double x1 = nFeat.get(a[j]).location[0];
					double y1 = nFeat.get(a[j]).location[1];
					double x2 = nFeat.get(a[k]).location[0];
					double y2 = nFeat.get(a[k]).location[1];
					ss.addValue(Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) *10);
					//ss.addValue((double) s[c]);
					c++;
				}
			}
			
			//ip.drawString(Integer.toString((int) Math.round(ss.getStandardDeviation()/ss.getMean()*10)), (int) rois[a[0]].getBounds().getCenterX(),(int) rois[a[0]].getBounds().getCenterY() );
			ip.drawString(Integer.toString(a.length - 1), (int) rois[a[0]].getBounds().getCenterX(),(int) rois[a[0]].getBounds().getCenterY());
			
			//}
		}
			img.updateAndDraw();
			img.show();
			rm.runCommand("Show All");
		*/
	
		
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

		//rm.runCommand("Show All");

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
		//img.close();
		
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
	
	public class RoisFeat {
		private Roi[] rois;
		private ArrayList<Feature[]> feat;
		public RoisFeat (Roi[] rois, ArrayList<Feature[]> feat){
			this.rois = rois;
			this.feat = feat;
		}
		public Roi[] getRois() { return rois; }
		public ArrayList<Feature[]> getFeat() {return feat;}
	}
	
	public static int[] transferArraysToHistogram256(int[] density){
		Frequency f = new Frequency();
		int[] p = new int[256];
		for (int i = 0 ; i < density.length; i++) f.addValue(density[i]);
		for (int i = 0 ; i < 256; i++) p[i] = (int) f.getCount(i);
		return p;
	}
	
	public static Roi[] findRoiwithFeat(int[] featinRoi, Roi[] rois, int min, int max){
		Frequency f = new Frequency();
		for (int i = 0; i < featinRoi.length; i++) f.addValue(featinRoi[i]);
		String s = f.toString();
		String[] s1 = s.split("\n");
		s1 = ArrayUtils.remove(s1, 0);
		
		int[][] ft = new int[s1.length][2];
		for (int i = 0; i < s1.length; i++){
			String[] s2 = s1[i].split("\t");
			ft[i][0] = Integer.parseInt(s2[0]);
			ft[i][1] = Integer.parseInt(s2[1]);
			//System.out.println(i + "," + ft[i][0] + "," + ft[i][1] );
		}
	
		
		ArrayList<Roi> temp = new ArrayList<Roi>();
		
		for (int i = 0; i < ft.length; i++) {
			if (ft[i][1] >= min & ft[i][1] <= max){
			temp.add(rois[ft[i][0]]);
			rois[ft[i][0]].setName(rois[ft[i][0]].getName() + " " + ft[i][1]);
			}
		}
			
		
		Roi[] t = new Roi[temp.size()];
		for (int i = 0; i < t.length; i++) t[i] = temp.get(i);
		
		return t;
	}
	
	public static int[] findRoiWithMinMaxDensity(Roi[] rois, ArrayList<Integer[]> nb, int[] density){
		DescriptiveStatistics ds = new DescriptiveStatistics();
		double[] mdDAB = new double[rois.length];
		int[] matchedRoi = new int[2];
		for (int i = 0; i < rois.length; i++){
			DescriptiveStatistics ds1= new DescriptiveStatistics();
			Integer[] r = nb.get(i);
			
			//double sum = 0;
			for (int j = 0; j < r.length; j++){
				ds1.addValue(density[r[j]]);
			}
			mdDAB[i] = ds1.getMean();
			ds.addValue(mdDAB[i]);
		}
		
		double min = ds.getMin();
		double max = ds.getMax();
		for (int i = 0; i < rois.length; i++){
		if (mdDAB[i] == min) matchedRoi[0] = i;
		if (mdDAB[i] == max) matchedRoi[1] = i;
		}
		
		return matchedRoi;
	}
	
	public static Roi[] removeSmallRoiCluster(Roi[] rois, ArrayList<Integer[]> nb, int nbcount){
		ArrayList<Roi> roistemp = new ArrayList<Roi>();
		for (int i = 0; i < nb.size(); i++){
			if (nb.get(i).length > (nbcount + 1)) {
				roistemp.add(rois[i]);
			}
		}
		
		Roi[] rt = new Roi[roistemp.size()];
		for (int i = 0; i < roistemp.size(); i++){
			rt[i] = roistemp.get(i);
		}
		return rt;
	}
	public static Integer[][] sortCenterRoi(Roi[] rois){
		Integer[][] sy = new Integer[rois.length][2];
		for (int i = 0; i < rois.length; i++){
			sy[i][0] = i;
			sy[i][1] = (int) rois[i].getBounds().getCenterY();
		}
		sy = sort2DInteger(sy);
		return sy;
	}
	
	public static int[] measureDensity(ImagePlus DAB, Roi[] rois){
		int options = ImageStatistics.MEDIAN;
		int[] densityDAB = new int[rois.length]; 
		for (int i = 0; i < rois.length; i++){
			ImageStatistics stats = new ImageStatistics();
			DAB.setRoi(rois[i]);
			stats = DAB.getStatistics(options);
			densityDAB[i] = (int) stats.median;
		}
		return densityDAB;
	}
	
	public static ArrayList<Integer[]> findNB(ImagePlus img, Integer[][] sy, Roi[] rois){
	        int options = ImageStatistics.MIN_MAX;
	        ImageStatistics stats = new ImageStatistics();
	        ArrayList<Integer[]> nb = new ArrayList<Integer[]>();
	        int w = img.getWidth();
	        int h = img.getHeight();
	        for (int i = 0; i < rois.length; i++){
	        	ImagePlus imgtemp = IJ.createImage("blank", "8-bit white", img.getWidth(), img.getHeight(), 1);
	            ImageProcessor iptemp = imgtemp.getProcessor();
	        	ArrayList<Integer> r = new ArrayList<Integer>();
	            r.add(i);
	            Rectangle rc = rois[i].getBounds();
	            int maxX = (int) rc.getMaxX();
	            int maxY = (int) rc.getMaxY();
	            int minX = (int) rc.getMinX();
	            int minY = (int) rc.getMinY();
	            int cy = (int) rc.getCenterY();
	            maxX = maxX + (int) Math.signum(w - maxX)*2;
	            maxY = maxY + (int) Math.signum(h - maxY)*2;
	            minX = minX - (int) Math.signum(minX - 0)*2;
	            minY = minY - (int) Math.signum(minX - 0)*2;
	            
	            iptemp.drawRect(minX, minY, maxX - minX, maxY - minY);
	            imgtemp.setRoi(rois[i]);
	            iptemp.setColor(0);
	            iptemp.fill();
	             
	            int start = 0;
	            int end = rois.length - 1;
	   
	            while (sy[start][1] <= (cy - 100)) {
	                start++;
	            }
	       
	            while (sy[end][1] >= (cy + 100)) {
	                end--;
	            }
	               
	            for (int j= start; j <= end; j++){
	               	imgtemp.setRoi(rois[sy[j][0]]);
	        		stats = imgtemp.getStatistics(options);
	        		if (stats.min == 0 & sy[j][0] != i)  r.add(sy[j][0]);
	            }
	           
	            Integer[] ra = new Integer[r.size()];
	            for (int j = 0; j < ra.length; j++) {
	                ra[j] = r.get(j);
	            }
	            nb.add(ra);
	            imgtemp.close();
	            
	        }
	        return nb;
	    }
	    
	public static float[] computeAverageDistancetoOthers(ArrayList<Feature> nFeat){
		int[] t = new int[nFeat.size()];
		float[] s = new float[nFeat.size()];
		for (int k = 0; k < t.length; k++) t[k] = k;
		for (int i = 0; i < nFeat.size(); i++){
			int[] t1 = ArrayUtils.remove(t, i);
			s[i] = 0;
			for (int j = 0; j < t1.length; j++){
				s[i] = s[i] + nFeat.get(j).descriptorDistance(nFeat.get(i));
			}
			s[i] = s[i]/t1.length;
		}
		return s;
	}
	
	public static void computeDescriptorInnerProduct(ArrayList<Feature> nFeat){
		for (int i = 0; i < nFeat.size(); i ++){
			float[] d = nFeat.get(i).descriptor;
			float s = 0;
			for (int j = 0; j < d.length; j++){
				s = s + d[j] * d[j];
			}
			s = (float) Math.sqrt(s/d.length);
			System.out.println(i + "\t" + s);
		}
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
		ArrayList<Feature> pFeat = new ArrayList<Feature>();
		for (int i = 0; i < feat.size(); i++) tFeat.add(feat.get(i));
		
		float s;
		for (int i = 0; i < tFeat.size(); i++){
			 s = tFeat.get(i).scale;
			 if (s >= crit)  pFeat.add(tFeat.get(i));
		}
		
		tFeat.removeAll(pFeat);
		return tFeat;
		
	}
	
	public static int[] findFeatinRoi(ArrayList<Feature> feat, Roi[] rois, Integer[][] sy){
		int[] featinRoi = new int[feat.size()];
		ArrayList<Integer> temp = new ArrayList<Integer>();

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
			for (int k = start; k <= end ; k++){
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
		
		for (int i = 0; i < featinRoi.length; i++){
			if (featinRoi[i] > -1) temp.add(featinRoi[i]);
		}
		
		int[] t = new int[temp.size()];
		for (int i = 0; i < t.length; i++) t[i] = temp.get(i);
		
		return t;
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
		long[] r;
		for (int i = 0; i < rois.length; i++){
			imgblank.setRoi(rois[i]);
			ImageStatistics is = ipblank.getStatistics();
			r = is.getHistogram();
			roisD[i] =  (int) r[255];
		}
		return roisD;
	}
	
	public static Roi[] featureCountExceed(int d, Roi[] rois, int[] roisD){
		ArrayList<Roi> indexes = new ArrayList<Roi>();
		for (int i = 0; i < rois.length; i++){
			if (roisD[i] > d) indexes.add(rois[i]);
		}
		
		Roi[] index = new Roi[indexes.size()];
		for (int i = 0; i < indexes.size(); i++){
			index[i] = indexes.get(i);
		}
		return index;
	}	

}