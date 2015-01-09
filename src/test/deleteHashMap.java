package test;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class deleteHashMap {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] a = {3,4};
		int[] b = {1,3};
		int[] c = {1,2};
		int[] d = {1,3};
		
		HashMap<Integer, int[]> h = new HashMap<Integer, int[]>();
		h.put(1, a);
		h.put(2, b);
		h.put(3, c);
		h.put(4, d);
				
		Iterator it = h.keySet().iterator();
		
		while (h.size() > 0){
			for (int i: h.get(h.keySet().iterator().next())) System.out.print(i);
			System.out.println("");
			h.remove(h.keySet().iterator().next());
		}
		
		System.out.println("ok");
	}
	
	public static void recurse(HashMap<Integer, int[]> h){
		HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
		int flag = 1;
		
		if (flag != 0){
			for (int i: h.get(h.keySet().iterator().next())) temp.put(i, i);
		
		}
		
		
	}

}
