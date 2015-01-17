package test;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

public class deleteHashMap {
	
	
	//public static ArrayList<int[]> group;
	
	public void deleteHashMap(){
		;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		deleteHashMap dl = new deleteHashMap();
		ArrayList<int[]> group = new ArrayList<int[]>();
		
		int[] a = {2,3};
		int[] b = {1,3};
		int[] c = {1,2,4};
		int[] d = {3,5};
		int[] e = {4};
		int[] f = {7,8};
		int[] g = {6,8,9};
		int[] h1 = {6,7,9};
		int[] i1 = {7,8};
		int[] j1 = {};
		int[] j2 = {12, 13};
		int[] j3 = {11};
		int[] j4 = {11};
		
		HashMap<Integer, int[]> h = new HashMap<Integer, int[]>();
		h.put(1, a);
		h.put(2, b);
		h.put(3, c);
		h.put(4, d);
		h.put(5, e);
		h.put(6, f);
		h.put(7, g);
		h.put(8, h1);
		h.put(9, i1);
		h.put(10, j1);
		h.put(11, j2);
		h.put(12, j3);
		h.put(13, j4);
				
		dl.recurse(h, group);
		for (int[] i:group){
			for (int j:i) System.out.print(j + ",");
			System.out.println("");
		}
	
	
		System.out.println("ok");
	}
	
	public static HashMap<Integer, int[]> recurse(HashMap<Integer, int[]> h, ArrayList<int[]> group){
		int[] y;
		int x = h.keySet().iterator().next();
		int[] v = h.get(x);
		if (v.length > 0){
		y = arrayAdd(v, h, 0);
		}else{
		y = new int[1];
		y[0] = x;
		}
		group.add(y);
		for (int i: y) h.remove(i);
		if (!h.isEmpty()){	
			h = recurse(h, group);
		}
		return h;
		
		
	}
	
	public static int[] arrayAdd(int[] a1, HashMap<Integer, int[]> h, int s){
		HashSet<Integer> a = new HashSet<Integer>();
		for (int i: a1) a.add(i);
		for (int i: a1){
			for (int j: h.get(i)){
				a.add(j);
			}
		}
		
		int[] p = new int[a.size()];
		int m = 0;
		for (Integer i : a) p[m++] = i; 
		int s1 = a.size();
		if (a.size() != s) p = arrayAdd(p, h, s1);
		return p;
		
	}
	
	
	
	
}
