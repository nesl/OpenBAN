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
 * Name: ExecutionService.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.analyze;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.joda.time.DateTime;

import controllers.AppManager;
import controllers.RepoProfile;
import edu.pc3.openban.data.DataSourceAdapter;
import edu.pc3.openban.data.DatastreamManager;
import edu.pc3.openban.data.DatastreamManager.TimeSeriesContainer;
import edu.pc3.openban.model.AppFormat;
import edu.pc3.openban.scheduler.AppScheduler;
import edu.pc3.openban.user.UserProfileManager;
import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;
import edu.pc3.openban.util.TimeSeries;

public class ExecutionService {


	//Training traingConfig = new Training();
	//GroundTruth groundTruthConfig = new GroundTruth();

	// Map<String, Map<String, Double>> trainingData = new LinkedHashMap<String,
	// Map<String, Double>>();
	
	//private Map<String, Map<String, Double>> trainingData = new LinkedHashMap<String, Map<String, Double>>();

	// <name, <time,value>>
	//private Map<String, Map<String, Double>> groundTruthData = new LinkedHashMap<String, Map<String, Double>>();

	private Map<String, Collection<Double>> executionSet = new LinkedHashMap<String, Collection<Double>>();
	
	private Map<String, Collection<String>> experimentSet = new LinkedHashMap<String, Collection<String>>();
	
	private Map<String, TimeSeries> groundTruthDataNew = new LinkedHashMap<String, TimeSeries>();
	private Map<String, TimeSeries> executionDataNew = new LinkedHashMap<String, TimeSeries>();

	public String userId;
	public String appname;
	public AppFormat app;
	
	public ExecutionService(String userId, String appname) {
		this.userId = userId;
		this.appname = appname;		
		app = AppManager.loadApp(userId, appname);
	}

	public Collection<Double> getDataPoints1(Map<String, Double> map) {
		return map.values();
	}

	// public Map<String, ArrayList<Double>> mapTraingSet = new
	// LinkedHashMap<String, ArrayList<Double>>();

	public void prepareExecutionSet() {		
		TimeSeries dataMap = null;
		for (String tkey : executionDataNew.keySet()) {
			dataMap = executionDataNew.get(tkey);			
			executionSet.put(tkey, dataMap.values());
		}
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

	public String validateAppProfile(AppFormat app) {
		return Const.SUCCESS;
	}

	public List safeList( List other ) {
	    return other == null ? Collections.EMPTY_LIST : other;
	}

	public String acquireData() {
		
		AppFormat.Aggregate aggregate = app.aggregate;
		AppFormat.Analyze analyze = app.analyze;
		AppFormat.Act act = app.act;
		
		String from_date = act.from_date;
		String to_date = act.to_date;		
		int feature_window_size = Integer.parseInt(analyze.feature_window_size);

		// load ground truth data...
		String gt_service = aggregate.groundtruth.get(0).children.get(0).name;
		String gt_datastream = aggregate.groundtruth.get(0).children.get(0).children.get(0).name;
		
		// TODO: Don't find mean for the ground truth data...
		TimeSeries tsGroundTruth = DatastreamManager.getFeatureData(userId, appname, gt_service, 
				gt_datastream, from_date, to_date, "Minimum", feature_window_size );
		
		System.out.println("tsGroundTruth# " + tsGroundTruth.size());
		groundTruthDataNew.put("GROUNDTRUTH", tsGroundTruth);

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
					
					tsFeatureMap.put(feature.name, tt);
					//tsFeatureMap.put(feature_name, tt);
				}
			}
		}		
		executionDataNew.putAll(tsFeatureMap);
		
		return Const.SUCCESS;
	}
	
	
	public static class ResultFormat {	
		//public double GROUNDTRUTH[];
		public double predicted[];
		
	}
	
	public String executeModal() {
		
		//app = AppManager.loadApp(userId, appname);
		
		if(app == null) {
			System.out.println("its is null");
			return "Invalid app";
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

		System.out.println("Getting model info...");
		String modelId = DatastreamManager.getModalInfo(userId, appname, app.analyze.classifier);		
		System.out.println("modalId : " + modelId);
		
		if(modelId == null) {
			return "No model found!";
		}
		
		modelId = modelId.replace(app.analyze.classifier, "").replace("--", "");
		System.out.println("modalId : " + modelId);

		
		// save the feature map
		//DatastreamManager.storeTrainingDataIntoDropbox(userId, appname, trainingFile, trainingDataNew);
		
		//System.out.println("Handling missing data.........");
		//handleMissingData();
		
		System.out.println("Preparting exectuion set.........");
		prepareExecutionSet();
		
		// write the execution set
		DatastreamManager.storeExecutionSetIntoDropbox(userId, appname, "execute", executionSet);		
		String jsonData = JsonUtil.json.toJson(executionSet);
		
		System.out.println("execution set size : " + executionSet.keySet().size());
		
		// get the stored model id.
		String classifier = toRFunction(app.analyze.classifier);
		
		String result="";
		ResultFormat rf;
		
		try {
			result = ProcessService.getInstance().executeModal(classifier, modelId, jsonData);
			//result = ProcessService.getInstance().executeModalOpenPy(classifier, modelId, jsonData);
			rf = JsonUtil.fromJson(result, ResultFormat.class);
			
			TimeSeries dataMap = null;
			// get a single time series
			for (String tkey : executionDataNew.keySet()) {
				dataMap = executionDataNew.get(tkey);
			}
			
			System.out.println("execution size : " + dataMap.keySet().size());
			System.out.println("Result size : " + rf.predicted.length);
			
			/*
			TimeSeries dataMap = null;
			for (String tkey : executionDataNew.keySet()) {
				dataMap = executionDataNew.get(tkey);			
				executionSet.put(tkey, dataMap.values());
			}
			*/
			
			// map to time stamp
			TimeSeriesContainer tsContainer = new TimeSeriesContainer();
			int index = 0;
			for (DateTime dt : dataMap.keySet()) {				
				tsContainer.datapoints.put(dt, rf.predicted[index++]);
			}
						
			// for the experiments...
			TimeSeries gtMap = null;
			for (String key : groundTruthDataNew.keySet()) {
				gtMap = groundTruthDataNew.get(key);
				System.out.println(key + " gt# " + gtMap.size());
			}			
			for (String tkey : executionDataNew.keySet()) {
				dataMap = executionDataNew.get(tkey);
				gtMap.keySet().retainAll(dataMap.keySet());				
			}
			executionSet.put("GT", gtMap.values());
			executionSet.put("result", tsContainer.datapoints.values());
			
			DatastreamManager.storeExecutionSetIntoDropbox(userId, appname, "experiment_set", executionSet);		
			
			String execute_now = "execute_now";
			// update the cache
			DatastreamManager.updateExecutionNowResult(userId, appname, execute_now, new TimeSeries(tsContainer.datapoints));
			result = JsonUtil.json1.toJson(tsContainer);
			
			
			DatastreamManager.storeExecutionOutputDropbox(userId, appname, execute_now, result);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println(result);
			return e.getMessage();
		}

		System.out.println("done...!");
		return Const.SUCCESS;
	}
	
	public void handleRepoDatastream(String reponame, String datastream, Map<DateTime, Double> tsData) {
		
		RepoProfile repoProfile = new UserProfileManager(userId).getRepoProfile(reponame);
		
		if(repoProfile == null) {
			return;
		}
		
		String reposource = repoProfile.getReposource();

		if (reposource.equals(Const.XIVELY)) {

			String key = repoProfile.getKey();
					
			// TODO: assumptions
			// datastream string edu.pc3.openban.model
			// feedid $ feedname <space> datastream name (last token)
			
			// split and feed id and datastream
			StringTokenizer st = new StringTokenizer(datastream, "$ ");
			System.out.println(datastream+"...............");
			String feed = st.nextToken();
			System.out.println(feed+"...............");
			
			String x_datastream = null;
			// datastream name is the last token
			while(st.hasMoreTokens()) {
				x_datastream = st.nextToken();	
			}
			
			//TODO: Push data to xively
			// get xively datastream name which is last token
			System.out.println(x_datastream+"...............");
			System.out.println("dowloading data....");
			//dataMap = DataSourceAdapter.fetchXivelyData(key, feed, x_datastream,
				//	from, to);
			
			
		} else if (reposource.equals(Const.DROPBOX)) {
			// download the datastream from dropbox
			
			// parse the folder and dir name
			// we assume that there is no space in folder and filename
			StringTokenizer st = new StringTokenizer(datastream, " ");
			
			System.out.println(datastream+"...............");
			
			String dsFolder = st.nextToken();
			String dsFile = st.nextToken();
			
			//TODO: push data to dropbox
			//System.out.println("Fetching.. " + dsFolder + "/" + dsFile);
			
			//String csvData =  DropboxDataStore.getInstance(userId).fetchDatastream( dsFolder, dsFile);
			//System.out.println("lengh " + csvData.length());
			//dataMap = DataFomatter.toTimeSeries(csvData, from, to);	
		} else if (reposource.equals(Const.SENSORACT)) {
			
			System.out.println(repoProfile.getRepourl() + " " + repoProfile.getKey() + "  " + datastream);
			
			// get device, sensor and channel
			
			// split and feed id and datastream
			StringTokenizer st = new StringTokenizer(datastream, " ");			
			String device = st.nextToken();			
			String sensor= st.nextToken();
			
			StringTokenizer st1 = new StringTokenizer(sensor, "$");
			sensor= st1.nextToken();
			String channel = st1.nextToken();
			
			System.out.println(reposource + "  " + device + " " + sensor + " " + channel );

			DataSourceAdapter.storeSensorActData(repoProfile.getRepourl(), repoProfile.getKey(), 
					device, sensor, channel, tsData);
		}
	}
	
	public void handleConsumers(Map<DateTime, Double> tsData) {
		
		List<AppFormat.Node> dataConsumerList   = app.act.consumers.get(0).children;		
		for(AppFormat.Node dataRepo: dataConsumerList){
			List<AppFormat.Node> datastreamList = safeList(dataRepo.children);
			for(AppFormat.Node datastream: datastreamList){
				System.out.println("handling " + dataRepo.name + " " + datastream.name );
				handleRepoDatastream(dataRepo.name, datastream.name, tsData);
			}
		}					
	}

	public String executeAppInstance() {
		
		//app = AppManager.loadApp(userId, appname);
		
		if(app == null) {
			System.out.println("its is null");
			return "Invalid app";
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

		System.out.println("Getting model info...");
		String modelId = DatastreamManager.getModalInfo(userId, appname, app.analyze.classifier);		
		System.out.println("modalId : " + modelId);
		
		if(modelId == null) {
			return "No model found!";
		}
		
		modelId = modelId.replace(app.analyze.classifier, "").replace("--", "");
		System.out.println("modalId : " + modelId);
		
		// save the feature map
		//DatastreamManager.storeTrainingDataIntoDropbox(userId, appname, trainingFile, trainingDataNew);
		
		//System.out.println("Handling missing data.........");
		//handleMissingData();
		
		System.out.println("Preparting exectuion set.........");
		prepareExecutionSet();
		
		// write the execution set
		DatastreamManager.storeExecutionSetIntoDropbox(userId, appname, "execute", executionSet);		
		String jsonData = JsonUtil.json.toJson(executionSet);
		
		
		// get the stored model id.
		String classifier = toRFunction(app.analyze.classifier);
		
		try {
			String result = ProcessService.getInstance().executeModal(classifier, modelId, jsonData);			
			ResultFormat rf = JsonUtil.fromJson(result, ResultFormat.class);
			
			TimeSeries dataMap = null;
			// get a single time series
			for (String tkey : executionDataNew.keySet()) {
				dataMap = executionDataNew.get(tkey);
			}
			
			System.out.println("execution size : " + dataMap.keySet().size());
			System.out.println("Result size : " + rf.predicted.length);

			// map to time stamp
			TimeSeriesContainer tsContainer = new TimeSeriesContainer();
			int index = 0;
			for (DateTime dt : dataMap.keySet()) {				
				tsContainer.datapoints.put(dt, rf.predicted[index++]);
			}
			
			Map<DateTime, Double> tsFiltered = new LinkedHashMap<DateTime, Double> ();

			double threshold = 0.6;
			index = 0;
			for (DateTime dt : dataMap.keySet()) {				
				if(rf.predicted[index] > threshold) {
					tsFiltered.put(dt, dataMap.get(dt));	
				}
				++index;
			}
			
			handleConsumers(tsFiltered);
			
			String execute_now = "execute_now";
			// update the cache
			DatastreamManager.updateExecutionNowResult(userId, appname, execute_now, new TimeSeries(tsContainer.datapoints));
			result = JsonUtil.json.toJson(tsContainer);
			DatastreamManager.storeExecutionOutputDropbox(userId, appname, execute_now, result);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}

		System.out.println("done...!");
		return Const.SUCCESS;
	}

	
	public String scheduleApp() {
		
		//app = AppManager.loadApp(userId, appname);
		
		if(app == null) {
			System.out.println("its is null");
			return "Invalid app";
		}
		
		//System.out.println(app.appname);
		//System.out.println(JsonUtil.json1.toJson(app));

		String res = null;
		
		res = validateAppProfile(app);
		if(!res.contains(Const.SUCCESS)){
			return res;
		}
		
		try {			
			String jobId = AppScheduler.scheduleTasklet(userId, appname);
			System.out.println("Job scheduled " + jobId);
			if( jobId == null ) {
				return "Error occured while schedulting the job";
			}			
			
		} catch (Exception e) {
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
