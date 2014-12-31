package test;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Binary;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class PA {

	public static void main(String[] args) {
		String path = "/home/hc2008/temp/blank0.tiff";
		ImagePlus img = IJ.openImage(path);
		ImagePlus img1 = img.duplicate();
		
		IJ.run(img, "Fill Holes","");
		
		img.show();
		img1.show();
		
		ImageCalculator ic = new ImageCalculator();
		
		ImagePlus img2 = ic.run("Difference create", img1, img);
		img2.show();
		
		
		/*
		ImageProcessor ip = img.getProcessor();
		
		ip.setAutoThreshold(AutoThresholder.Method.Default, false);
		ip.autoThreshold();
		*/
		IJ.run(img, "Convert to Mask", "");
		
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa =  new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER, Measurements.AREA, rt, (double)1, (double)9999999);
		pa.analyze(img);

		RoiManager rm = RoiManager.getInstance();
		Roi[] r = rm.getRoisAsArray();
		
		ImageStatistics is = new ImageStatistics();
		
		for (int i = 0; i < r.length; i++){
			img2.setRoi(r[i]);
			is = img2.getStatistics();
			img2.getProcessor().setColor(255);
			if (is.mean != 0 && is.mean != 255) img2.getProcessor().fill(r[i]);
		}
		img2.updateAndDraw();
		img2.show();
	}

}
