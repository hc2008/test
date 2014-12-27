package test;

import java.awt.image.BufferedImage;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.superpixels.jSLIC;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class SLIC {
	static int x = 1360;
	static int y = 976;
	public static void main(String[] args) {
		String path = "/home/hc2008/Image/image/klotho/EGFR-1/08-4888D1-N3.tif";
		ImagePlus img2 = IJ.openImage(path);
		img2.setRoi(0, 0, x, y);
		ImagePlus img = cropImg(img2, 0, 0, x, y);
		ImageProcessor ip = img.getProcessor();
		img.show();
		jSLIC js = new jSLIC(img);
		js.process(30, 0.3F);
		Labelling2D ld = js.getSegmentation();
		
		ld.showOverlapROIs(img);
		RoiManager rm = RoiManager.getInstance();
		Roi[] rois = rm.getRoisAsArray();
	}
	
	public static ImagePlus cropImg(ImagePlus img2, int x0, int y0, int x1, int y1){
		ImageProcessor ip1 = img2.getProcessor();
		ip1.setRoi(0, 0, 1360, 976);
		ImageProcessor ip2 = ip1.crop();
		BufferedImage croppedImage = ip2.getBufferedImage();
		ImagePlus img = new ImagePlus("Original", croppedImage);
		return(img);
	}
	
	
}
