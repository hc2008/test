package test;

import java.util.ArrayList;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class WekaKmean {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute("a"));
		atts.add(new Attribute("b"));
		atts.add(new Attribute("c"));
		Instances a = new Instances("test", atts, 0);
		double[] instanceValue1 = new double[]{3,0, 2};
		a.add(new DenseInstance(1.0, instanceValue1));
		double[] instanceValue2 = new double[]{2,1, 0};
		a.add(new DenseInstance(1.0, instanceValue2));
		double[] instanceValue3 = new double[]{6,7, 3};
		a.add(new DenseInstance(1.0, instanceValue3));
		
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
		
		for ( int i = 0; i < result.length; i++) System.out.println(result[i]);
		
	}

}
