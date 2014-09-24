package controllers;

import java.util.HashMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import edu.pc3.openban.util.JsonUtil;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import play.Play;

import play.libs.Files;

public class ModelRepo {
	
	private static final String ALGORITHM_FILE = "./conf/algorithms.json";
	private static File algoFile = Play.getFile(ALGORITHM_FILE);
	
	public static class AlgorithmInfo {
		public String server;
		public String domain;
		public String train_url;
		public String exec_url;
	}
	
	public static HashMap<String,AlgorithmInfo> alogrithms = null; 
			//= new HashMap<String,ModelRepoFormat>();
	
	public static void storeModelInfo() throws Exception {
		
		HashMap<String,AlgorithmInfo> alogrithms= new HashMap<String,AlgorithmInfo>();		
		
		AlgorithmInfo a1 = new AlgorithmInfo();
		a1.train_url = "http://openban.iiitd.edu.in/ocpu/library/openban/R/svm.train/";
		a1.exec_url = "http://openban.iiitd.edu.in/ocpu/library/openban/R/svm.test/";		
		alogrithms.put("SVM", a1);
		
		AlgorithmInfo a2 = new AlgorithmInfo();
		a2.train_url = "http://openban.iiitd.edu.in/ocpu/library/openban/R/dtree.train/";
		a2.exec_url = "http://openban.iiitd.edu.in/ocpu/library/openban/R/dtree.test/";		
		alogrithms.put("Decision Tree", a2);
		
		//java.nio.file.Files.write(path, bytes, options)
		//java.nio.file.Files.readAllBytes(arg0)		
		PrintWriter writer = new PrintWriter(algoFile.getAbsoluteFile(), "UTF-8");		
		writer.print(JsonUtil.json1.toJson(alogrithms));
		writer.close();		
	}
	
	public static void loadModelInfo() throws Exception {
	    FileInputStream fis = new FileInputStream(algoFile);
	    byte[] buf = new byte[(int)algoFile.length()];
	    fis.read(buf);
	    fis.close();	    
	    String algoJson = new String(buf);
	    
		Type typeOfHashMap = new TypeToken< HashMap<String,AlgorithmInfo> >() { }.getType();
		alogrithms = JsonUtil.json.fromJson(algoJson, typeOfHashMap); // This type must match TypeToken		
	}
}