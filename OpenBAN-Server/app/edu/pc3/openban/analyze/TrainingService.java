/*
 * Copyright (c) 2013, Indraprastha Institute of Information Technology,
 * Delhi (IIIT-D) and The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 * 3. Neither the names of the Indraprastha Institute of Information
 *    Technology, Delhi and the University of California nor the names
 *    of their contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE IIIT-D, THE REGENTS, AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE IIITD-D, THE REGENTS
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
/*
 * Name: TrainingService.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.analyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.joda.time.DateTime;

import controllers.AppManager;
import controllers.Application;
import edu.pc3.openban.data.DatastreamManager;
import edu.pc3.openban.model.AppFormat;
import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;
import edu.pc3.openban.util.TimeSeries;

public class TrainingService {

	String dates[] = { "2013-06-23" };
	int gnd_FeedId = 1184036650;
	String gnd_StreamId = "Occupant_count";

	int inFeedId = 644505874;
	String[] inStreamId = { "Foreign_80", "Foreign_8080", "Peers_Max",
			"TCP_Out_Max", "TCP_Out_Sum", "web" };

	//Training traingConfig = new Training();
	//GroundTruth groundTruthConfig = new GroundTruth();

	// Map<String, Map<String, Double>> trainingData = new LinkedHashMap<String,
	// Map<String, Double>>();
	
	//private Map<String, Map<String, Double>> trainingData = new LinkedHashMap<String, Map<String, Double>>();

	// <name, <time,value>>
	//private Map<String, Map<String, Double>> groundTruthData = new LinkedHashMap<String, Map<String, Double>>();

	private Map<String, Collection<Double>> trainingSet = new LinkedHashMap<String, Collection<Double>>();
	
	private Map<String, TimeSeries> groundTruthDataNew = new LinkedHashMap<String, TimeSeries>();
	private Map<String, TimeSeries> trainingDataNew = new LinkedHashMap<String, TimeSeries>();

	public String userId;
	public String appname;
	public AppFormat app;
	
	public TrainingService(String userId, String appname) {
		this.userId = userId;
		this.appname = appname;		
		app = AppManager.loadApp(userId, appname);
	}

	public Collection<Double> getDataPoints1(Map<String, Double> map) {
		return map.values();
	}

	// public Map<String, ArrayList<Double>> mapTraingSet = new
	// LinkedHashMap<String, ArrayList<Double>>();

	public void prepareTrainingSet() {
		
		TimeSeries gtMap = null;
		TimeSeries dataMap = null;
		//Collection<Double> dataList = null;
		String gtKey = null;

		// TODO: assume only one attribute in the ground truth map
		for (String key : groundTruthDataNew.keySet()) {
			gtMap = groundTruthDataNew.get(key);
			gtKey = key;
			// break;
		}

		List<Double> dates = new ArrayList<Double>();		
		for(DateTime dt:gtMap.keySet()){
			dates.add( new Double(dt.getMillis()));
		}
		//trainingSet.put("time", dates);
		
		for (String tkey : trainingDataNew.keySet()) {
			dataMap = trainingDataNew.get(tkey);			
			trainingSet.put(tkey, dataMap.values());
		}

		// TODO: assume only one attribute in the ground truth map
		for (String key : groundTruthDataNew.keySet()) {
			gtMap = groundTruthDataNew.get(key);
			gtKey = key;
			// break;
		}

		// add additional features
		// minute of the day
		//Collection<Double> minsOftheDay = getFeature_MinsOfTheDay(gtMap);
		//trainingSet1.put("MinOfTheDay", minsOftheDay);
		
		
		trainingSet.put(gtKey, gtMap.values());
	}

	public void printSummary(Map<String, TimeSeries> map) {
		for (String key : map.keySet()) {
			System.out.println("\t" + key + "  " + map.get(key).size()
					+ " data points");
		}
	}

	public void printSummary1(Map<String, TimeSeries> map) {
		for (String key : map.keySet()) {
			System.out.println("\t" + key + "  " + map.get(key).size()
					+ " data points");
		}
	}

	public void handleMissingData() {

		printSummary(groundTruthDataNew);
		printSummary(trainingDataNew);
		
		// TODO: assume only one attribute in the ground truth map
		TimeSeries gtMap = null;
		for (String key : groundTruthDataNew.keySet()) {
			gtMap = groundTruthDataNew.get(key);
			// System.out.println(key + " gt# " + gtMap.size());
		}
		
		printSummary(groundTruthDataNew);
		printSummary(trainingDataNew);
		
		// retain only the common keys among all the traing data
		for (String tkey : trainingDataNew.keySet()) {
			System.out.println("Handling " + tkey);
			TimeSeries tMap = trainingDataNew.get(tkey);
			
			/*
			for(DateTime dt:tMap.keySet()) {
				System.out.println("tMap..... " + dt.toString());
				break;
			}

			for(DateTime dt:gtMap.keySet()) {
				System.out.println("gtMap..... " + dt.toString());
				break;
			}*/
			
			gtMap.keySet().retainAll(tMap.keySet());
			
		}
		
		printSummary(groundTruthDataNew);
		printSummary(trainingDataNew);

		// retain only the common keys among all the traing data
		for (String tkey : trainingDataNew.keySet()) {
			System.out.println("Handling " + tkey);
			Map<String, Double> tMap = trainingDataNew.get(tkey);
			tMap.keySet().retainAll(gtMap.keySet());			
		}

		String buf = "";
		String s =null;

/*		for (DateTime key : gtMap.keySet()) {
			s = key.toString();
			for (String tkey : trainingDataNew.keySet()) {
				Map<String, Double> tMap = trainingDataNew.get(tkey);

				if (tMap.containsKey(key)) {
					s = s + "," + tMap.get(key);
				} else {
					System.out.println("Time not found " + key + " in " + tkey);
				}
			}

			buf = buf + s + "\n";
		}
*/
		printSummary(groundTruthDataNew);
		printSummary(trainingDataNew);

		System.out.println("writing training data");
	}

	public String validateAppProfile(AppFormat app) {
		return Const.SUCCESS;
	}

	public List safeList( List other ) {
	    return other == null ? Collections.EMPTY_LIST : other;
	}

	public String getLastToken(String name) {		
		StringTokenizer st = new StringTokenizer(name, " ");
		String x_datastream = null;
		while(st.hasMoreTokens()) {
			x_datastream = st.nextToken();	
		}		
		return x_datastream;
	}
	
	
	public String acquireData() {
		
		// get the ground truth data..
		
		AppFormat.Aggregate aggregate = app.aggregate;
		AppFormat.Analyze analyze = app.analyze;
		AppFormat.Act act = app.act;
		
		String from_date = aggregate.from_date;
		String to_date = aggregate.to_date;		
		int feature_window_size = Integer.parseInt(analyze.feature_window_size);

		// load ground truth data...
		String gt_service = aggregate.groundtruth.get(0).children.get(0).name;
		String gt_datastream = aggregate.groundtruth.get(0).children.get(0).children.get(0).name;
		
		// TODO: Don't find mean for the ground truth data...
		TimeSeries tsGroundTruth = DatastreamManager.getFeatureData(userId, appname, gt_service, 
				gt_datastream, from_date, to_date, Const.MAXIMUM, feature_window_size );
		
		// load training feature data...		
		Map<String, TimeSeries> tsFeatureMap = new LinkedHashMap<String, TimeSeries>();
		
		List<AppFormat.Node> dataRepoList   = analyze.features.get(0).children;		
		for(AppFormat.Node dataRepo: dataRepoList){
			
			List<AppFormat.Node> datastreamList = safeList(dataRepo.children);
			for(AppFormat.Node datastream: datastreamList){
				List<AppFormat.Node> featureList   = safeList(datastream.children);
				for(AppFormat.Node feature : featureList){
					System.out.println("fetching feature " + dataRepo.name + "  " + datastream.name + "  " + feature.name);					
					TimeSeries tt = DatastreamManager.getFeatureData(userId, appname, dataRepo.name, 
							datastream.name, from_date, to_date, feature.name, feature_window_size );
					
					String feature_name = DatastreamManager.getFeatureDataKey(dataRepo.name, 
							datastream.name, from_date, to_date, feature.name, feature_window_size );					
					
					if(tt!=null && tt.size()>0){
						//tsFeatureMap.put(getLastToken(datastream.name)+"__"+feature.name, tt);	
						tsFeatureMap.put(feature.name, tt);
					}					
					//tsFeatureMap.put(feature_name, tt);
				}
			}
		}
		
		groundTruthDataNew.put("GROUNDTRUTH", tsGroundTruth);
		trainingDataNew.putAll(tsFeatureMap);
		
		Application.trainingDataNew.putAll(tsFeatureMap);
		
		return Const.SUCCESS;
	}
	
	public String trainModel() {
		
		//app = AppManager.loadApp(userId, appname);
		
		if(app == null) {
			System.out.println("its is null");
		}
		
		//System.out.println(app.appname);
		//System.out.println(JsonUtil.json1.toJson(app));

		String res = null;
		
		res = validateAppProfile(app);
		if(!res.contains(Const.SUCCESS)){
			return res;
		}
		
		res = acquireData();
		if(!res.contains(Const.SUCCESS)){
			return res;
		}
		
		String trainingFile = userId + appname + " training.raw.json";
		String gtFile = userId + appname + " gt.raw.json";
		// save the feature map
		//DatastreamManager.storeTrainingDataIntoDropbox(userId, appname, trainingFile, trainingDataNew);
		//DatastreamManager.storeTrainingDataIntoDropbox(userId, appname, gtFile, groundTruthDataNew);
		
		System.out.println("Handling missing data.........");
		handleMissingData();
		
		System.out.println("Preparting training set.........");
		prepareTrainingSet();
		// write the training set

		String trainingSetFile = userId + appname + " trainingset";
		// save the feature map
		DatastreamManager.storeTrainingSetIntoDropbox(userId, appname, trainingSetFile, trainingSet);
		
		String jsonData = JsonUtil.json.toJson(trainingSet);
		
		//System.out.println(jsonData);

		//String classifier = toRFunction(app.analyze.classifier);
		String classifier = app.analyze.classifier;
		
		try {
			String ModelId = ProcessService.getInstance().learnModel(classifier, jsonData);
			String ModelInfo = ""; //ProcessService.getInstance().getModelPararm(ModelId);
			String ModelFile = app.analyze.classifier + "--" + ModelId;			
			DatastreamManager.storeModelInfo(userId, appname, ModelFile, ModelInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}

		System.out.println("done...!");
		
		return Const.SUCCESS;
	}
	
	// map the classifier name in the app with the name in R function
	public static String toRFunction(String classifier) {
		
		if(classifier.equals(Const.DECISION_TREE))
			return "dtree";

		if(classifier.equals(Const.REGRESSION))
			return "regression";

		if(classifier.equals(Const.NEURAL_NETWORK))
			return "nnet";

		if(classifier.equals(Const.SVM))
			return "svm";

		if(classifier.equals(Const.KNN))
			return "knn";

		if(classifier.equals(Const.NAIVE_BAYES))
			return "naivebayes";
		
		return classifier;
		
	}
	
/*	public void trainNewTest() {

		System.out.println("** Fetching ground truth data..");
		groundTruthDataNew = DataService.getInstance().fetchGroundTruthData(
				Application.groundTruth);

		System.out.println("saving......................");
		DataService.getInstance().writeJsonToFile(
				"./data/groundtruth_data_raw.json", groundTruthDataNew);

		trainingDataNew = DataService.getInstance().fetchTrainingData(
				Application.groundTruth, Application.trainingData);
		System.out.println("..Done! Summary");

		System.out.println("\n\npreprocessing the training data");
		handleMissingData();
		System.out.println("..........done");
		
		
		for (String key : trainingDataNew.keySet()) {
			TimeSeries ts = trainingDataNew.get(key);

			List<TimeWindow> tw = TimeWindow.split(ts, 300);
			String js2 = JsonUtil.json1.toJson(tw);

			StatisticalFeatures sf = new StatisticalFeatures(tw);
			sf.compute();

			TimeSeries ts1 = sf.getMeanSeries();
			String js3 = JsonUtil.json1.toJson(ts1);

			System.out.println(js3);

			break;
		}

		DataService.getInstance().writeJsonToFile(
				"./data/training_data_raw.json", trainingDataNew);

		printSummary1(groundTruthDataNew);
		printSummary1(trainingDataNew);
		
		
		System.out.println("Preparting training set...");
		prepareTrainingSet();
		// write the training set
		DataService.getInstance().writeJsonToFile("./data/training_set.json",
				trainingSet);

		DataService.getInstance().writeCSVToFile("./data/training_set.csv",
				trainingSet);

		System.out.println("invoking model..");

		String json = JsonUtil.json.toJson(trainingSet);

		try {
			String res = ProcessService.getInstance().sendToOpenCPU(json);
			DataService.getInstance().writeToFile("./data/tested.json", res);
			System.out
					.println("tested data written to " + "./data/tested.json");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("done...!");
	}
*/
}
