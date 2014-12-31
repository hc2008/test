import java.awt.Color;
import java.awt.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
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
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.python.google.common.collect.ArrayListMultimap;
import org.python.google.common.collect.Multimap;

import ij.*;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
//import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import mpicbg.ij.SIFT;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.superpixels.*;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Remove;



public class testHashMap {
	static int x = 1360;
	static int y = 976;
	
	public static  void main(String[] args) throws Exception{
		testHashMap fl = new testHashMap();
		
		//open the image and crop it to the size not including length mark
		String path = "/home/hc2008/Image/image/klotho/EGFR-1/08-4888D1-N3.tif";
		ImagePlus img2 = IJ.openImage(path);
		img2.setRoi(0, 0, x, y);
		ImagePlus img = fl.cropImg(img2, 0, 0, x, y);
		ImageProcessor ip = img.getProcessor();
		img.show();
		
		//apply SIFT to obtain feature points
		ArrayList<Feature> feat = fl.doSift(ip);
		
		/*
		ImagePlus imgblank = IJ.createImage("blank", "8-bit white", img.getWidth(), img.getHeight(), 1);
		imgblank.show();
		IJ.run("Make Binary","");
		imgblank.hide();
		ImageProcessor ipblank = imgblank.getProcessor();
		ipblank.setColor(new Color(0));
		for (int i = 0; i < feat.size(); i++) ipblank.drawOval((int) feat.get(i).location[0], (int) feat.get(i).location[1], 2,2);
		imgblank.updateAndDraw();
		imgblank.show();
		*/
		//ArrayList<Feature> nFeat = removeFeatbyScale(feat,6.0F);
		
		//apply SLIC to obtain array of ROI
		Roi[] roisT = fl.doSlic(img, 30, 0.3F);
		int roiCount = roisT.length;
		
		//use sorted Y coordinate of ROI center(sy) to accelerate the searching of which ROI contain feature
		Integer[][] sy = fl.sortCenterRoi(roisT);
		Integer[][] featinRoi = fl.findFeatinRoi(feat,roisT, sy);
		
		//find the indices of feature in ROI
		RoisFeat rf = fl.findRoiFeat(featinRoi);
		//find the indices of ROI without any feature
		int[] roisNoFeat = fl.roisWithoutFeat(rf.getRoi(), roiCount);
		//the maximal feature count in a ROI 
		int maxFeatCount = fl.findMaxFeatCount(rf.getCount());
		//use Multimap to store feature count in a roi, roi
		Multimap<Integer, Integer> mp =  ArrayListMultimap.create();
		//hm is a HashMap to store the indices of feature in a ROI
		HashMap<Integer, int[]> hm = new HashMap<Integer, int[]>();
		for (int i = 0; i < rf.getRoi().length; i++) {
			mp.put(rf.getCount()[i], rf.getRoi()[i]);
			hm.put(rf.getRoi()[i], rf.getFeat().get(i));
		}
		//select ROIs with feature counts more than
		int[] selectedRoiByFeatCount = fl.selectRoisWithFeatRange(mp, maxFeatCount, 3, 100);
		//obtain the ROI array (rois) with feature count more than ? and rois0 for ROI array without feature 
		Roi[] rois = fl.indexToRoi(roisT, selectedRoiByFeatCount);
		Roi[] rois0 = fl.indexToRoi(roisT, roisNoFeat);
		
		//rois = removeRoiWithoutFeat(rf, rois);

		//rois = featureCountExceed(0, rois, roisD);
			
		//rois = findRoiwithFeat(featinRoi, rois, 0, 100);
		
		//for (int i = 0; i < rois.length; i++) System.out.println(rois[i].getName());

		
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
		RoiManager rm = RoiManager.getInstance();
		rm.reset();
		for (int i = 0; i < rois.length; i++ ) rm.addRoi(rois[i]);
		rm.runCommand("Show All");
		
		//remove ROI with neighborhood less than 2
		RoiNB rnb = fl.removeLonelyRoi(rois, img, 2);
		rois = rnb.getRoi();
		ArrayList<Integer[]> nb = rnb.getNB();
		
		//update roiManager
		rm.reset();
		for (int i = 0; i < rois.length; i++ ) rm.addRoi(rois[i]);
		
		
		Colour_Deconvolution cdr = new Colour_Deconvolution(); 
		ArrayList<ImagePlus> S = cdr.run(img);
		
		//construct a class with name of method, score, index (0/1 for a certain criteria) and roi array
		
		ScoreIndexName sin = fl.initScoreIndexName();
		
		//measurements in ROI with features more than ?
		ArrayList<double[]> mms = new ArrayList<double[]>();
		ArrayList<String> mmsName = new ArrayList<String>();
				
		for (int i = 0 ; i < S.size(); i++) {
			mms.add(fl.measureDensity(S.get(i), rois));
			mmsName.add(S.get(i).getTitle());
		}
		
		for (int i = 0 ; i < S.size(); i++) sin = fl.scoreByOtsu(sin, mms.get(i), rois, img, S.get(i).getTitle());
				
		//use Kmean++ in weka to obtain selection index of roi
		sin = fl.sinaddKmean(mms, mmsName, rois, img, sin);
		//compare which method has the largest median of ROI area
		sin = fl.compareScore(sin, rois, img);
		
		int[] kindex = sin.getIndex().get(0);
		
		for (int i = 0; i < kindex.length; i++){
			if (kindex[i] == 0){
				img.getProcessor().setColor(255);
			}else{img.getProcessor().setColor(0);}
			img.setRoi(rois[i]);
			ip.fill(rois[i]);
		}
		

		//select attributes and obtain correlation coefficients by logisitc regression
		//lgData and lgDataName are selected data and their names
		ArrayList<double[]> lgData = mms;
		ArrayList<String> lgDataName = mmsName;
		HashMap<String, Double> lgHM = fl.LogRegression(sin.getIndex().get(0), lgData, lgDataName);
		for (Object key : lgHM.keySet())  System.out.println(key + " : " + lgHM.get(key));
		
		img.setRoi(sin.getAreaRoi().get(0).getRoi()[0]);
		ip.fill(sin.getAreaRoi().get(0).getRoi()[0]);
		img.updateAndDraw();
		img.show();
		
		rm.reset();
		rm.addRoi(sin.getAreaRoi().get(0).getRoi()[0]);
		rm.addRoi(sin.getAreaRoi().get(0).getRoi()[1]);
		rm.setVisible(true);
		/*
		img.setRoi(sin.getAreaRoi().get(0).getRoi()[1]);
		ip.fill(sin.getAreaRoi().get(0).getRoi()[1]);
		img.updateAndDraw();
		img.show();
		*/
		
		
		//nM[0] roi with the minimal mean (including neighbor roi) of median density of DAB, nM[1] the maximum
		//int[] mMroisDAB = findRoiWithMinMaxDensity(rois,nb,densityDAB);
		
		//System.out.println((mMroisDAB[0]+1) + "," + (mMroisDAB[1]+ 1));

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
	
	public testHashMap(){
		
	}
	
	public ScoreIndexName sinaddKmean(ArrayList<double[]> mms, ArrayList<String> mmsName, Roi[] rois, ImagePlus img, ScoreIndexName sin){
		int[] kindex = kmeanDens(mms, mmsName, rois);
		AreaRoi ar = findAreaAndRoi(img, rois, kindex);
		sin.put(findM(ar.getArea()), kindex, "kmean", ar);
		return sin;
	}
	
	public ScoreIndexName initScoreIndexName(){
		ArrayList<String> st = new ArrayList<String>();
		ArrayList<Double> score = new ArrayList<Double>();
		ArrayList<int[]> index = new ArrayList<int[]>();
		ArrayList<AreaRoi> arearoi = new ArrayList<AreaRoi>();
		ScoreIndexName sin = new ScoreIndexName(score, index, st, arearoi);
		return sin;
	}
	
	//LogRegression: logistic regression, index has two class (0 or 1), ArrayList<double[]> d are data
	//Instances a is the dataframe, a.setClassIndex(0) to denote index column, AttributeSelectedClassifier, 
	//CfsSubsetEval and GreedyStepwise are used to do attribute selection, 
	//the HashMap of <column of data, regression coefficient> 
	public HashMap<String, Double> LogRegression(int[] index, ArrayList<double[]> d, ArrayList<String> mmsName) throws Exception{
		String[] s = new String[index.length];
		for (int i = 0; i < s.length; i++) s[i] = Integer.toString(index[i]);
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
        ArrayList<String> classVal = new ArrayList<String>();
        classVal.add("0");
        classVal.add("1");
		atts.add(new Attribute("A",classVal));
		//int dc = 65;
		for (String arg:mmsName){
			atts.add(new Attribute(arg));
		}
		Instances a = new Instances("test", atts, 0);
 
        DenseInstance newInstance  = new DenseInstance(a.numAttributes());
               	
        for (int i = 0; i < s.length; i++){
            newInstance.setValue(0 , a.attribute(0).indexOfValue(s[i]) );
            int j = 0;
            for (double[] arg:d){
            	newInstance.setValue(++j, arg[i]);
            }
            a.add(newInstance);
        }

        a.setClassIndex(0);
        AttributeSelection attsel = new AttributeSelection();  // package weka.attributeSelection!
        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);
        attsel.setEvaluator(eval);
        attsel.setSearch(search);
        attsel.SelectAttributes(a);
               
        
        //remove the unwanted attribute
        int[] indices = attsel.selectedAttributes();
        Arrays.sort(indices);
        ArrayList<Integer> notInList = new ArrayList<Integer>();
        
        for (int i = 0; i < atts.size(); i++) {
        	int flag = 0;
        	for (int j : indices) {
        		if (i == j) {
        			flag = 1;
        			break;
        		}
        	}
        	if (flag == 0) notInList.add(i);
        }
        
        int[] toRemove = new int[notInList.size()];
        for (int i = 0; i < toRemove.length; i++) toRemove[i] = notInList.get(i);
        
        Remove rm = new Remove();
        rm.setInvertSelection(false);
        rm.setAttributeIndicesArray(toRemove);
        rm.setInputFormat(a);
        a = Filter.useFilter(a, rm);
        a.compactify();
 
        //apply logistic regression
        Logistic lg = new Logistic();
        lg.buildClassifier(a);
        HashMap <String, Double> hm = new HashMap<String, Double>();
        for (int i = 1; i < a.numAttributes(); i++) hm.put(a.attribute(i).name(),lg.coefficients()[i][0]);
        return hm;

	}
	
	public int[] kmeanDens(ArrayList<double[]> dens, ArrayList<String> densName, Roi[] rois){
		int s = dens.size();
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		for (int i = 0; i < s; i++) atts.add(new Attribute(densName.get(i)));
		
		Instances a = new Instances("test", atts, 0);
		
		for (int i = 0; i < rois.length; i++){
			double[] temp = new double[s];
			for(int j = 0; j < s; j++) temp[j] = dens.get(j)[i];
			a.add(new DenseInstance(1.0, temp));
		}
		
		Normalize tna = new Normalize();
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setPreserveInstancesOrder(true);
		int[] result = null;
		try {
			//kmeans.setOptions(options);
		    tna.setInputFormat(a);
		    a = Filter.useFilter(a, tna);
		    kmeans.setNumClusters(2);
			kmeans.buildClusterer(a);
			int[] assignments = kmeans.getAssignments();
			result = new int[assignments.length];
			for (int j = 0; j < result.length; j++){
			result[j] =  assignments[j];
			}
		}
		catch(Exception ie) {
			
		}
		return result;
	}
	
	public ScoreIndexName compareScore(ScoreIndexName sin, Roi[] rois, ImagePlus img){
		SummaryStatistics ss = new SummaryStatistics();
		HashMap<Double, String> scorehm = new HashMap<Double, String>();
		HashMap<String, int[]> indexhm = new HashMap<String, int[]>();
		HashMap<Double, AreaRoi> scoreAreaRoihm = new HashMap<Double, AreaRoi>();
		double score;
		for(int i = 0; i < sin.getName().toArray().length; i++){
		score = sin.getScore().get(i);
		ss.addValue(score);
		scorehm.put(score, sin.getName().get(i));
		scoreAreaRoihm.put(score, sin.getAreaRoi().get(i));
		indexhm.put(sin.getName().get(i), sin.getIndex().get(i));
		}
		/*
		System.out.println(ss.getMax());
		System.out.println(scorehm.get(ss.getMax()));
		
		int[] temp = indexhm.get(scorehm.get(ss.getMax()));
		*/
		sin.clear();
		sin.put(ss.getMax(), indexhm.get(scorehm.get(ss.getMax())), scorehm.get(ss.getMax()), scoreAreaRoihm.get(ss.getMax()));
		return sin;
	}
	
	public void drawRoi(int[] density, String n, ImagePlus img, RoiManager rm, int th){
		ImagePlus img1 = img.duplicate();
		img1.setTitle(n);
		Roi[] rois = rm.getRoisAsArray();
		ImageProcessor ip = img1.getProcessor();
		for (int i = 0; i < rois.length; i++){
			img1.setRoi(rois[i]);
			if (density[i] > th) ip.fill(rois[i]);
		}
		img1.updateAndDraw();
		img1.show();
		rm.runCommand("Show ALl");
	}
	
	public static class ScoreIndexName {
		
		private ArrayList<int[]> index;
		private ArrayList<Double> score;
		private ArrayList<String> name;
		private ArrayList<AreaRoi> ar;
		
		public ScoreIndexName (ArrayList<Double> score, ArrayList<int[]> index,  ArrayList<String> name, ArrayList<AreaRoi> ar){
			this.index = index;
			this.score = score;
			this.name = name;
			this.ar = ar;
		}
		
		public ArrayList<int[]> getIndex() { return index; }
		public ArrayList<Double> getScore() {return score;}
		public ArrayList<String> getName() {return name;}
		public ArrayList<AreaRoi> getAreaRoi() {return ar;}
		
		public void put (double a, int[] b, String c, AreaRoi d){
			score.add(a);
			index.add(b);
			name.add(c);
			ar.add(d);
		}
		
		public void clear (){
			score.clear();
			index.clear();
			name.clear();
			ar.clear();
		}
		
	}
	
	public ScoreIndexName scoreByOtsu(ScoreIndexName sin, double[] density, Roi[] rois, ImagePlus img, String name){
		AutoThresholder at = new AutoThresholder();
		int th = at.getThreshold(AutoThresholder.Method.Otsu, transferArraysToHistogram256(density));
		
		int[] index = new int[rois.length];
		for (int i = 0; i < index.length; i++){
			if (density[i] >= th) {
				index[i] = 1;
			}else{
				index[i] = 0;
			}
		}
		
		AreaRoi ar = findAreaAndRoi(img, rois, index);
		
		sin.put(findM(ar.getArea()), index, name, ar);
		return sin;
	}
	
	public double findM(float[] b){
		Median m = new Median();
		double[] c = new double[b.length];
		System.out.println(c);
		for (int i = 0; i < c.length; i++) c[i] = (double) b[i];
		return m.evaluate(c);
	}
	
	public static class AreaRoi{
		private float[] area;
		private Roi[] roi;
		public AreaRoi(float[] area, Roi[] roi){
			area = this.area;
			roi = this.roi;
		}
		
		public void put(float[] a, Roi[] b){
			this.area = a;
			this.roi = b;
		}
		
		public float[] getArea() {return area;}
		public Roi[] getRoi() {return roi;}
		
	}
	
	public AreaRoi findAreaAndRoi(ImagePlus img, Roi[] rois, int[] index){
		Roi[] maxArea = new Roi[2];
		
		ImagePlus imgblank = IJ.createImage("blank", "8-bit white", img.getWidth(), img.getHeight(), 1);
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa =  new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE, Measurements.AREA, rt, (double)1, (double)9999999);
				
		for (int i = 0; i < 2; i ++){
			ImagePlus temp = paintRoiByIndex(imgblank, rois, index, i);
			IJ.run(temp, "Make Binary", "");
			System.out.println(temp.isInvertedLut() + "," + temp.getProcessor().isBinary());
			
			pa.analyze(temp);
			temp.duplicate().show();
			maxArea[i] = findLargeAreaWithoutHole(temp);
			temp.close();
		}
		
		float[] area = rt.getColumn(0);
		
		AreaRoi ar = new AreaRoi(area, maxArea);
		ar.put(area, maxArea);
		
		return ar;
			
	}
	
	public ImagePlus paintRoiByIndex(ImagePlus imgblank, Roi[] rois, int[] index, int c){
		ImageProcessor ipblank = imgblank.getProcessor();
		ipblank.setBackgroundValue(255);
		
		ipblank.setColor(0);
		for (int i = 0; i < rois.length; i++){
			imgblank.setRoi(rois[i]);
			if (index[i] == c) {
				ipblank.fill(rois[i]);
				ipblank.dilate();
				imgblank.killRoi();
			}
			
		}
		
		ipblank.erode();
		imgblank.updateAndDraw();
						
		return imgblank;
	}
	
	public Roi findLargeAreaWithoutHole(ImagePlus impblank){
		//ImagePlus temp = IJ.createImage("blank", "8-bit white", tempimg.getWidth(), tempimg.getHeight(), 1);
		//ImagePlus impblank = new ImagePlus();
		//impblank = paintRoiByIndex(temp, rois, index, c);
		//impblank.show();
		HashMap<Double, Roi> areaRoi = new HashMap<Double, Roi>();
		DescriptiveStatistics ds= new DescriptiveStatistics();
		ImageStatistics is = new ImageStatistics();
		ResultsTable rt = new ResultsTable();

		ImagePlus imga = impblank.duplicate();
		
		ParticleAnalyzer pa =  new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER, Measurements.AREA, rt, (double)1, (double)9999999);
		
		IJ.run(impblank, "Fill Holes","");
		RoiManager rm = RoiManager.getInstance();
		rm.setVisible(true);
		rm.reset();
		IJ.run(impblank, "Make Binary", "");
		pa.analyze(impblank);
		Roi[] r = rm.getRoisAsArray();

		ImageCalculator ic = new ImageCalculator();
		ImagePlus imgb = ic.run("Difference create", imga, impblank);
		
		
		for (Roi roi:r){
			imgb.setRoi(roi);
			is = imgb.getStatistics();
			double m = is.mean;
			if (m == 0 || m == 255) {
				areaRoi.put(is.area, roi);
				ds.addValue(is.area);
			}
		}
		
		System.out.println(ds.getMax());
		System.out.println(areaRoi.get(ds.getMax()));
		
		return areaRoi.get(ds.getMax());
	}
	
	public static class RoiNB {
		private Roi[] roi;
		private ArrayList<Integer[]> NB;
		public RoiNB (Roi[] roi, ArrayList<Integer[]> NB){
			this.roi = roi;
			this.NB = NB;
		}
		public Roi[] getRoi() { return roi; }
		public ArrayList<Integer[]> getNB() {return NB;}
	}
	
	public RoiNB removeLonelyRoi(Roi[] rois, ImagePlus img, int lessthan){
		Integer[][] sy = sortCenterRoi(rois) ;
		ArrayList<Integer[]> nb = findNB(img, sy, rois);
		
		//reiterate removing roi with 0 or 1 neighbor 
		int roisCount;
		do{
		roisCount = rois.length;
		rois = removeSmallRoiCluster(rois, nb, lessthan);
		sy = sortCenterRoi(rois);
		nb = findNB(img, sy, rois);
		}while(roisCount != rois.length);
		
		return new RoiNB (rois, nb);
	}
	
	public Roi[] indexToRoi(Roi[] rois, int[] index){
		ArrayList<Roi> t = new ArrayList<Roi>();
		for (int i = 0; i < index.length; i++) t.add(rois[index[i]]); 
		Roi[] r = new Roi[t.size()];
		for (int i = 0; i < r.length; i++) r[i] = t.get(i);
		return r;
	}
	
	public int[] selectRoisWithFeatRange(Multimap<Integer, Integer> mp, int maxFeatCount, int begin, int end){
		
		int[] roiSelectedFeatCount;
		if (begin < 0) begin = 0;
		if (end > maxFeatCount) end = maxFeatCount;
		
		ArrayList<Integer> roiSelected = new ArrayList<Integer>();
		for (int j = begin; j <= end; j++){
		roiSelectedFeatCount = selectRoisWithFeatCount(mp,j);
		for (int i = 0; i < roiSelectedFeatCount.length; i++) roiSelected.add(roiSelectedFeatCount[i]);
		}
		
		int[] r = new int[roiSelected.size()];
		for (int i = 0; i < r.length; i++) r[i] = roiSelected.get(i);
		
		Arrays.sort(r);
		return r;
		
	}
	
	public int findMaxFeatCount(int[] featCount){
		DescriptiveStatistics ds1= new DescriptiveStatistics();
		for (int i = 0; i < featCount.length; i++) ds1.addValue((double) featCount[i]);
		return (int) ds1.getMax();
	}
	
	public int[] selectRoisWithFeatCount(Multimap mp, int select){
		Collection<Integer> cm = mp.get(select);
		Iterator it = cm.iterator();
		ArrayList<Integer> a = new ArrayList<Integer>();
		while (it.hasNext()){
			Object w = it.next();
			a.add((int) w);
		}
		
		int[] r = new int[a.size()];
		for (int i = 0; i < a.size(); i++) r[i] = a.get(i);
		return r;
	}
	
	public int[] roisWithoutFeat(int[] roitemp, int roiCount){
		ArrayList<Integer> ro = new ArrayList<Integer>();
		int temp = 0;
		for (int i = 0; i < roitemp.length; i++){
			while ((roitemp[i] - temp) > 1){
				temp++;
				ro.add(temp);
			}
			temp = roitemp[i];
		}
		
		for (int i = temp + 1 ; i < roiCount; i++) ro.add(i);
		
		int[] roiNoFeat = new int[ro.size()];
		for (int i = 0; i < ro.size(); i++) roiNoFeat[i] = ro.get(i);
		return roiNoFeat;
	}
	
	public Roi[] removeRoiWithoutFeat(RoisFeat rf, Roi[] rois){
		Roi[] roisTemp = new Roi[rf.getRoi().length];
		for (int i = 0; i < roisTemp.length; i++) roisTemp[i] = rois[rf.getRoi()[i]]; 
		return roisTemp;
	}
	
	
	
	public static class RoisFeat {
		private int[] roi;
		private ArrayList<int[]> feat;
		private int[] count;
		public RoisFeat (int[] roi, ArrayList<int[]> feat, int[] count){
			this.roi = roi;
			this.feat = feat;
			this.count = count;
		}
		public int[] getRoi() { return roi; }
		public ArrayList<int[]> getFeat() {return feat;}
		public int[] getCount() { return count; }
	}
	
	public RoisFeat findRoiFeat(Integer[][] featinRoi){
		featinRoi = sort2DInteger(featinRoi);
		
		ArrayList<Integer> r = new ArrayList<Integer>();
		ArrayList<int[]> f = new ArrayList<int[]>();
		ArrayList<Integer> c = new ArrayList<Integer>();
		ArrayList<Integer> a = new ArrayList<Integer>();
		int t = -1;
		for (int i = 0; i < featinRoi.length; i++) {
			if (featinRoi[i][1] != t){
				t = featinRoi[i][1];
				r.add(featinRoi[i][1]);
				if (i > 0){
				int[] b = new int[a.size()];
				for (int j = 0; j < b.length; j++) b[j] = a.get(j);
				c.add(a.size());
				a.clear();
				a.add(featinRoi[i][0]);
				f.add(b);
				}
			}else{
				a.add(featinRoi[i][0]);
			}
			if (i == (featinRoi.length - 1)){
				int[] b = new int[a.size()];
				for (int j = 0; j < b.length; j++) b[j] = a.get(j);
				c.add(a.size());
				a.clear();
				a.add(featinRoi[i][0]);
				f.add(b);
			}
		}
		
		int[] r1 = ArrayUtils.toPrimitive(r.toArray(new Integer[0]));
		int[] c1 = ArrayUtils.toPrimitive(c.toArray(new Integer[0]));
		
		return new RoisFeat(r1, f, c1);
	}
	
	public int[] transferArraysToHistogram256(double[] density){
		Frequency f = new Frequency();
		int[] p = new int[256];
		for (int i = 0 ; i < density.length; i++) f.addValue((int) density[i]);
		for (int i = 0 ; i < 256; i++) p[i] = (int) f.getCount(i);
		return p;
	}
	
	public Roi[] findRoiwithFeat(int[] featinRoi, Roi[] rois, int min, int max){
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
	
	public int[] findRoiWithMinMaxDensity(Roi[] rois, ArrayList<Integer[]> nb, int[] density){
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
	
	public Roi[] removeSmallRoiCluster(Roi[] rois, ArrayList<Integer[]> nb, int nbcount){
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
	public Integer[][] sortCenterRoi(Roi[] rois){
		Integer[][] sy = new Integer[rois.length][2];
		for (int i = 0; i < rois.length; i++){
			sy[i][0] = i;
			sy[i][1] = (int) rois[i].getBounds().getCenterY();
		}
		sy = sort2DInteger(sy);
		return sy;
	}
	
	public double[] measureDensity(ImagePlus img, Roi[] rois){
		int options = ImageStatistics.MEDIAN;
		double[] density = new double[rois.length]; 
		for (int i = 0; i < rois.length; i++){
			ImageStatistics stats = new ImageStatistics();
			img.setRoi(rois[i]);
			stats = img.getStatistics(options);
			density[i] = stats.median;
		}
		return density;
	}
	
	public ArrayList<Integer[]> findNB(ImagePlus img, Integer[][] sy, Roi[] rois){
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
	    
	public float[] computeAverageDistancetoOthers(ArrayList<Feature> nFeat){
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
	
	public void computeDescriptorInnerProduct(ArrayList<Feature> nFeat){
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
	
	public ArrayList<Integer[]> RoiContainingFeats(int[] featInRois){
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
	
	public ArrayList<Feature> removeFeatbyScale(ArrayList<Feature> feat, float crit){
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
	
	public  Integer[][] findFeatinRoi(ArrayList<Feature> feat, Roi[] rois, Integer[][] sy){
		int[] featinRoi = new int[feat.size()];
		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Integer> f = new ArrayList<Integer>();
		
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
			if (featinRoi[i] > -1) {
				temp.add(featinRoi[i]);
				f.add(i);
			}
			
			}
		
		Integer[][] t = new Integer[temp.size()][2];
		for (int i = 0; i < t.length; i++) {
			t[i][1] = temp.get(i);
			t[i][0] = f.get(i);
		}
		
		return t;
	}
	
	public Integer[][] sort2DInteger(Integer[][] data){
		Arrays.sort(data, new Comparator<Integer[]>() {
		    public int compare(Integer[] int1, Integer[] int2) {
		        Integer numOfKeys1 = int1[1];
		        Integer numOfKeys2 = int2[1];
		        return numOfKeys1.compareTo(numOfKeys2);
		    }
		});
		return data;
	}
	
	public ArrayList<Feature> doSift(ImageProcessor ip){
		FloatArray2DSIFT.Param param = new FloatArray2DSIFT.Param();
		ArrayList<Feature> feat = new ArrayList<Feature>();
		SIFT si = new SIFT(new FloatArray2DSIFT(param));
		si.extractFeatures(ip, feat);
		return feat;
	}
	
	public Roi[] doSlic(ImagePlus img, int area, float fit){
		jSLIC js = new jSLIC(img);
		js.process(area, fit);
		Labelling2D ld = js.getSegmentation();
		ld.showOverlapROIs(img);
		RoiManager rm = RoiManager.getInstance();
		Roi[] rois = rm.getRoisAsArray();
		return rois;
	}
	
	public ImagePlus cropImg(ImagePlus img2, int x0, int y0, int x1, int y1){
		ImageProcessor ip1 = img2.getProcessor();
		ip1.setRoi(0, 0, 1360, 976);
		ImageProcessor ip2 = ip1.crop();
		BufferedImage croppedImage = ip2.getBufferedImage();
		ImagePlus img = new ImagePlus("Original", croppedImage);
		return(img);
	}
	
	public int[] countFeatureDotbyRoi (Roi[] rois, ImagePlus imgblank, ImageProcessor ipblank ){
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
	
	public Roi[] featureCountExceed(int d, Roi[] rois, int[] roisD){
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