package test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;

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
		System.out.println(rm.getCount());
	}

}
