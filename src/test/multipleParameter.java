package test;

public class multipleParameter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] a = {1,2,3};
		double[] b = {4,5,6};
		double c = 1;
		list(c, a, b);
	}

	public static void list(double a, double[]...p){
		System.out.println(a);
		int j = 0;
		for (double[] arg : p) {
			System.out.println(++j);
			for (int i = 0; i < arg.length; i++) System.out.print(arg[i] + ",");
			System.out.println("");
		}
		
	}
}
