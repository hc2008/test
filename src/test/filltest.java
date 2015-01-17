package test;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class filltest {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = "/home/hc2008/temp/blobs.gif";
		ImagePlus img = IJ.openImage(path);
		img.show();
		IJ.run(img, "Make Binary", "");
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa =  new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER, Measurements.AREA, rt, (double)1, (double)9999999);
		pa.analyze(img);
		RoiManager rm = RoiManager.getInstance();
		Roi[] rois = rm.getRoisAsArray();
		int[] index = new int [rm.getCount()];
		for (int i = 0; i<  rm.getCount(); i++)	index[i] = (((int) (10* Math.random())) % 2);
		ArrayList<ArrayList<Integer>> ar = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < 2; i++) ar.add(new ArrayList<Integer>());
		for (int i = 0; i < index.length; i++) ar.get(index[i]).add(i);
		
		ShapeRoi[] s = new ShapeRoi[2];
		
		ArrayList<Integer> t = ar.get(0);
		
		
		ShapeRoi sr = new ShapeRoi(rois[ar.get(0).get(0)]);
		for (int i = 1; i < ar.get(0).size(); i++){
			sr.or(new ShapeRoi(rois[ar.get(0).get(i)]));
		}
		
		img.setRoi(sr);
		img.getProcessor().fill(sr);
		img.getProcessor().setColor(255);
		img.updateAndDraw();
		img.show();
		
		ImageStatistics is = img.getStatistics();
		System.out.println(is.area);
		
		double sum = 0;
		for (int i: ar.get(0)){
			img.setRoi(rois[i]);
			is = img.getStatistics();
			sum = sum + is.area;
		}
		System.out.println(sum);
		
		//drawRoi(img, rm.getRoisAsArray(), index, 0);
		//drawRoi(img, rm.getRoisAsArray(), index, 1);

	}
	
	public static void drawRoi(ImagePlus img, Roi[] rois, int[] index, int c){
		ImagePlus imgblank = IJ.createImage("blank", "8-bit white", img.getWidth(), img.getHeight(), 1);
		ImageProcessor ipblank = imgblank.getProcessor();
		for (int i = 0; i < rois.length; i++){
			if (index[i] == c){
				imgblank.setRoi(rois[i]);
				ipblank.fill(rois[i]);
				ipblank.dilate();
			}
		}
		ipblank.erode();
		imgblank.updateAndDraw();
		imgblank.show();
	}

}
