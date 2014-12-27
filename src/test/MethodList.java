package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MethodList {

	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		MethodList ml = new MethodList();
		Method[] method = ml.getClass().getDeclaredMethods();
		for (Method m: method) {
			System.out.println(m.getName().substring(0, 2));
			if (m.getName().charAt(0) == "p".charAt(0)) System.out.println(m.invoke(m, "OK"));
		}
		
		
	}
	
	public void MethodList(){
		
	}
	
	public static String p_A(String a){
		return ("pAPrint" + "," + a);
	}
	
	public static String p_B(String a){
		return ("pBPrint" + "," + a);
	}
	
	public static String p_C(String a){
		return ("pCPrint" + "," + a);
	}

}
