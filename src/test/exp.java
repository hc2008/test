package test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.python.google.common.collect.ArrayListMultimap;
import org.python.google.common.collect.HashBasedTable;
import org.python.google.common.collect.Multimap;
import org.python.google.common.collect.Table;


public class exp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] a = {4,3,3,2,5};
		int[] a1 = {1,1,1,2,2};
		ArrayList<Integer> b = new ArrayList<Integer>();
		for ( int i = 0; i < a.length; i++) b.add(a[i]);
		for (int i = 0; i < b.size(); i++) System.out.print(b.get(i));
		System.out.println("");
		Integer[] c = {3};
		Iterator<Integer> it = b.iterator();
		while (it.hasNext()){
			Object o = it.next();
			if (Arrays.asList(c).contains(o)){
				it.remove();
			}
		}
		
		for (int i = 0; i < b.size(); i++) System.out.print(b.get(i));
		System.out.println("");
		

		Multimap<Integer, Integer> ma =  ArrayListMultimap.create();
		for (int i = 0; i < a.length; i++) ma.put(a1[i], a[i]);
		Collection<Integer> col = ma.get(2);
		Integer[] p = col.toArray(new Integer[0]);
		for (int i = 0; i < p.length; i++) System.out.print(p[i]);
		System.out.println("");
		
		col = ma.get(1);
		p = col.toArray(new Integer[0]);
		for (int i = 0; i < p.length; i++) System.out.print(p[i]);
		System.out.println("");
		
		
		
		int[] f = {1,2};
		ArrayList<int[]> f1 = new ArrayList<int[]>();
		
		int[] r = {2,3,4};
		f1.add(r);
		int[] r1 = {6,7,8};
		f1.add(r1);
		
		Multimap<Integer, int[]> mp =  ArrayListMultimap.create();
		mp.put(f[0], f1.get(0));
		mp.put(f[1], f1.get(1));
		
		Collection<int[]> clp = mp.get(2);
		
		Iterator its = clp.iterator();
		
		while (its.hasNext()){
			Object w = its.next();
			int[] k = (int[])w;
			for (int i = 0; i < k.length; i++) System.out.print(k[i]);
		}
		System.out.println("");
		
		Table<Integer,Integer, String> tpn = HashBasedTable.create();
		
		tpn.put(1,2, "a");
		tpn.put(2,4, "b");
				
		System.out.println(tpn.column(2).values());
	}
}
