package test;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

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
		int[] index = new int [rm.getCount()];
		for (int i = 0; i<  rm.getCount(); i++)	index[i] = (((int) (10* Math.random())) % 2);
		drawRoi(img, rm.getRoisAsArray(), index, 0);
		drawRoi(img, rm.getRoisAsArray(), index, 1);
		System.out.println(0.00001 == 0);
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
