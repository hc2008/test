package test;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import bsh.Capabilities;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.SubsetEvaluator;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;



public class wekaLogistic{

    public static void main(String[] args) throws Exception {
        //construct the data
    	ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
        ArrayList<String> classVal = new ArrayList<String>();
        classVal.add("0");
        classVal.add("1");
        atts.add(new Attribute("A",classVal));
        int dc = 65;
        atts.add(new Attribute(Character.toString((char) ++dc)));
        atts.add(new Attribute(Character.toString((char) ++dc)));
        atts.add(new Attribute(Character.toString((char) ++dc)));
        atts.add(new Attribute(Character.toString((char) ++dc)));
        Instances a = new Instances("test", atts, 0);
                
        String[] s = {"1", "1", "1", "0", "0", "0"};
        double[] d1 = {4,3,2, -1,-3, -2};
        double[] d2 = {1,2,3, -3, -4, -5};
                
        DenseInstance newInstance  = new DenseInstance(a.numAttributes());
                   
        for (int i = 0; i < s.length; i++){
            newInstance.setValue(0 , a.attribute(0).indexOfValue(s[i]) );
            newInstance.setValue(2, d1[i]);
            newInstance.setValue(4, d2[i]);
            newInstance.setValue(3, 1);
            newInstance.setValue(1, 0);
            a.add(newInstance);
        }
        
        a.setClassIndex(0);

        // attribute selection
        AttributeSelection attsel = new AttributeSelection();  // package weka.attributeSelection!
        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);
        attsel.setEvaluator(eval);
        //GainRatioAttributeEval eval1 = new GainRatioAttributeEval(); 
        //attsel.setEvaluator(eval1);
        //Ranker ra = new Ranker();
        attsel.setSearch(search);
        //attsel.setSearch(ra);
        attsel.SelectAttributes(a);
        
        
        
        
        //remove the unwanted attribute
        int[] indices = attsel.selectedAttributes();
        Arrays.sort(indices);
        ArrayList<Integer> notInList = new ArrayList<Integer>();
        
        for (int i = 0; i < atts.size(); i++) {
        	int flag = 0;
        	for (int j : indices) {
        		if (i == j) {
        			flag = 1;
        			break;
        		}
        	}
        	if (flag == 0) notInList.add(i);
        }
        
        int[] toRemove = new int[notInList.size()];
        for (int i = 0; i < toRemove.length; i++) toRemove[i] = notInList.get(i);
        
        Remove rm = new Remove();
        rm.setInvertSelection(false);
        rm.setAttributeIndicesArray(toRemove);
        rm.setInputFormat(a);
        a = Filter.useFilter(a, rm);
        a.compactify();
 
        //apply logistic regression
       
        Logistic lg = new Logistic();
        lg.buildClassifier(a);
        for (int i = 1; i < a.numAttributes(); i++) System.out.println(a.attribute(i).name() + "," + lg.coefficients()[i][0]);
            
        
        /*
        AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
        //String[] options = weka.core.Utils.splitOptions("-R -B");
        //classifier.setOptions(options);
        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        //search.setGenerateRanking(true);
        //search.setOptions(weka.core.Utils.splitOptions("-R -B"));
        classifier.setClassifier(lg);
        classifier.setEvaluator(eval);
        classifier.setSearch(search);
        classifier.buildClassifier(a1);
        System.out.println(classifier.getClassifier().toString());
   
        
        
        
        /*
        String p = classifier.getClassifier().toString();
        String[] sp = p.split("\n");
        ArrayList<String> parN = new ArrayList<String>();
        ArrayList<Double> parV = new ArrayList<Double>();
   
        int count = 0;
        int flag = 0;
        for (String p1 : sp) {
            if (p1.contains("Coefficients")) flag = 1;
            if (flag == 1 ) count++;
            if (p1.contains("Intercept")) flag = 0;
            if (count > 4 && flag == 1) {
                String[] st = p1.split(" ");
                //System.out.println(st[0] + "," + st[st.length -1]);
                parN.add(st[0]);
                parV.add(Double.parseDouble(st[st.length-1]));
        }

        ArrayList<Integer> parS = new ArrayList<Integer>();
        
        for (int i = 0; i < parN.size(); i++) parS.add((int) parN.get(i).charAt(0) - 66);
        HashMap <Integer, Double> hm = new HashMap<Integer, Double>();
        for (int i = 0; i < parN.size(); i++) hm.put(parS.get(i), parV.get(i));
        for (Object key : hm.keySet())  System.out.println(key + " : " + hm.get(key));
        
    }
    */
    }
}
    
    





