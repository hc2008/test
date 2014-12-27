package test;

import java.awt.List;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.python.google.common.collect.Lists;

import adt.Points;

public class Kmean {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KMeansPlusPlusClusterer kppc = new KMeansPlusPlusClusterer(2);
		Collection<DoublePoint> points = Lists.newArrayListWithCapacity(10);
		double[] t = new double[10];
		for (int i = 0; i < 10; i++) t[i] = Math.random();
		DoublePoint p = new DoublePoint(t);
		points.add(p);
		for (int i = 0; i < 10; i++) t[i] = Math.random();
		p = new DoublePoint(t);
		points.add(p);
		System.out.println("OK");
		java.util.List<CentroidCluster> tm = kppc.cluster(points);
		for (int i = 0; i < tm.size(); i++) {
			System.out.println(tm.get(i).toString());
		}
		System.out.println("p");
	}

}
