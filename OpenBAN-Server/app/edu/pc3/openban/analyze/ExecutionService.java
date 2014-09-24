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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.joda.time.DateTime;

import controllers.AppManager;
import controllers.RepoProfile;
import edu.pc3.openban.analyze.ProcessService.ResultFormat;
import edu.pc3.openban.data.DataSourceAdapter;
import edu.pc3.openban.data.DatastreamManager;
import edu.pc3.openban.data.DatastreamManager.TimeSeriesContainer;
import edu.pc3.openban.model.AppFormat;
import edu.pc3.openban.scheduler.AppScheduler;
import edu.pc3.openban.scheduler.OpenBanAppJob;
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
		
		// TODO: only for openpy nilm tk
		// put timestamp (epoc) as well 
		List<Double> ts = new ArrayList<Double>();		
		for(DateTime dt: dataMap.keySet()) {
			ts.add( new Double(dt.getMillis()));
		}
		executionSet.put("time", ts);
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

		// load training feature data...		
		Map<String, TimeSeries> tsFeatureMap = new LinkedHashMap<String, TimeSeries>();
		
		List<AppFormat.Node> dataRepoList   = analyze.features.get(0).children;		
		for(AppFormat.Node dataRepo: dataRepoList){
			
			List<AppFormat.Node> datastreamList = safeList(dataRepo.children);
			for(AppFormat.Node datastream: datastreamList){
				List<AppFormat.Node> featureList   = safeList(datastream.children);
				for(AppFormat.Node feature : featureList){
					
					OpenBanAppJob.LOG.info(app.appname + "\t fetching feature " + dataRepo.name + "  " + datastream.name + "  " + feature.name);
					//System.out.println("fetching feature " + dataRepo.name + "  " + datastream.name + "  " + feature.name);					
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

	public String acquireDataForSchedule(String from_date, String to_date) {
		
		AppFormat.Aggregate aggregate = app.aggregate;
		AppFormat.Analyze analyze = app.analyze;
		AppFormat.Act act = app.act;
		
		//String from_date = act.from_date;
		//String to_date = act.to_date;		
		int feature_window_size = Integer.parseInt(analyze.feature_window_size);

		// load training feature data...		
		Map<String, TimeSeries> tsFeatureMap = new LinkedHashMap<String, TimeSeries>();
		
		List<AppFormat.Node> dataRepoList   = analyze.features.get(0).children;		
		for(AppFormat.Node dataRepo: dataRepoList){
			
			List<AppFormat.Node> datastreamList = safeList(dataRepo.children);
			for(AppFormat.Node datastream: datastreamList){
				List<AppFormat.Node> featureList   = safeList(datastream.children);
				for(AppFormat.Node feature : featureList){
					System.out.println("\tfetching feature " + dataRepo.name + "  " + datastream.name + "  " + feature.name);					
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
	
	
	public String executeModel() {
		
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
		
		OpenBanAppJob.LOG.info(app.appname + " Getting feature data....");
		long t1 = System.currentTimeMillis();
		res = acquireData();		
		long t2 = System.currentTimeMillis();
		OpenBanAppJob.LOG.info(app.appname + " Getting feature data....done");
		OpenBanAppJob.LOG.info(app.appname + " FEATURE_COMPUTATION " + (t2-t1));

		if(!res.contains(Const.SUCCESS)){
			return res;
		}

		OpenBanAppJob.LOG.info(app.appname + " Getting model info...");
		t1 = System.currentTimeMillis();
		String modelId = DatastreamManager.getModelInfo(userId, appname, app.analyze.classifier);
		t2 = System.currentTimeMillis();
		OpenBanAppJob.LOG.info(app.appname + " Getting model info... done! modelId: " + modelId);
		OpenBanAppJob.LOG.info(app.appname + " DOWNLOAD_MODEL " + (t2-t1));
		
		// 
		if(modelId == null) {
			modelId = "0" ; // for unsupervised model
			//return "No model found!";
		}
		
		if(modelId != null) {
			modelId = modelId.replace(app.analyze.classifier, "").replace("--", "");	
		}
		
		System.out.println("\tModelId : " + modelId);
		
		// save the feature map
		//DatastreamManager.storeTrainingDataIntoDropbox(userId, appname, trainingFile, trainingDataNew);
		
		//System.out.println("Handling missing data.........");
		//handleMissingData();
		
		OpenBanAppJob.LOG.info(app.appname + "\n");
		OpenBanAppJob.LOG.info(app.appname + " Preparting exectuion set...");
		prepareExecutionSet();
		OpenBanAppJob.LOG.info(app.appname + " Preparting exectuion set... done");
		
		// write the execution set
		DatastreamManager.storeExecutionSetIntoDropbox(userId, appname, "execute", executionSet);		
		String jsonData = JsonUtil.json.toJson(executionSet);
		
		System.out.println("\texecution set size : " + executionSet.keySet().size());
		//System.out.println(jsonData);
		
		// get the stored model id.
		//String classifier = toRFunction(app.analyze.classifier);
		String classifier = app.analyze.classifier;
		
		String result="";
		//ResultFormat rf;
		
		ResultFormat rf;
		
		try {
			
			OpenBanAppJob.LOG.info(app.appname + " Execute model ........");
			String options = "{" + app.analyze.options + "}";
			t1 = System.currentTimeMillis();
			rf = ProcessService.getInstance().executeModel(classifier, modelId, jsonData, options);
			t2 = System.currentTimeMillis();
			OpenBanAppJob.LOG.info(app.appname + " Execute model ........done");
			OpenBanAppJob.LOG.info(app.appname + " MODEL_EXECUTION " + (t2-t1));


			//result = ProcessService.getInstance().executeModelOpenPy(classifier, modelId, jsonData);
			//rf = JsonUtil.fromJson(result, ResultFormat.class);
			
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
			/*
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
			*/
			
			
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
		
		System.out.println("Handling consumers...");
		List<AppFormat.Node> dataConsumerList   = app.act.consumers.get(0).children;		
		if(dataConsumerList == null)
			return;
		
		for(AppFormat.Node dataRepo: dataConsumerList){
			List<AppFormat.Node> datastreamList = safeList(dataRepo.children);
			for(AppFormat.Node datastream: datastreamList){
				System.out.println("handling " + dataRepo.name + " " + datastream.name );
				handleRepoDatastream(dataRepo.name, datastream.name, tsData);
			}
		}					
	}

	// duration: 5 and unit: Minutes
	int getDuration(String duration, String unit) {

		int seconds = Integer.parseInt(duration);
		
		// ignore for Second
		if(unit.equals("Minute")) {
			seconds = seconds * 60;
		} else if(unit.equals("Hour")) {
			seconds = seconds * 60 * 60;
		} else if(unit.equals("Day")) {
			seconds = seconds * 60 * 60 * 24;
		}		
		return seconds;
	}
	
	public String executeAppInstance(String jobKey) {
		
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
		
		int dur = getDuration(app.act.time_duration, app.act.time_unit);
		// make start and end time stamp
		DateTime end = new DateTime();
		DateTime start = end.minusSeconds(dur).plusMillis(1);
		
		System.out.println("\t " + jobKey  + "Schedule start : " + start.toString());
		System.out.println("\t " + jobKey  + "Schedule   end : " + end.toString());

		
		
		OpenBanAppJob.LOG.info(jobKey + "");
		OpenBanAppJob.LOG.info(jobKey + " Getting feature data....");
		long t1 = System.currentTimeMillis();
		res = acquireDataForSchedule(start.toString(), end.toString());
		long t2 = System.currentTimeMillis();
		OpenBanAppJob.LOG.info(jobKey + " Getting feature data....done!");
		OpenBanAppJob.LOG.info(jobKey + " FEATURE_COMPUTATION " + (t2-t1));

		if(!res.contains(Const.SUCCESS)){
			return res;
		}

		OpenBanAppJob.LOG.info(jobKey + "");
		OpenBanAppJob.LOG.info(jobKey + " Getting model info");
		t1 = System.currentTimeMillis();
		String modelId = DatastreamManager.getModelInfo(userId, appname, app.analyze.classifier);
		t2 = System.currentTimeMillis();
		OpenBanAppJob.LOG.info(jobKey + " ModelId :" + modelId);
		OpenBanAppJob.LOG.info(jobKey + " DOWNLOAD_MODEL " + (t2-t1));

		
		if(modelId == null) {
			modelId = "0" ; // for unsupervised model
			//return "No model found!";
		}
		
		if(modelId != null) {
			modelId = modelId.replace(app.analyze.classifier, "").replace("--", "");	
		}
		OpenBanAppJob.LOG.info(jobKey + " ModelId :" + modelId);

		
		// save the feature map
		//DatastreamManager.storeTrainingDataIntoDropbox(userId, appname, trainingFile, trainingDataNew);
		
		//System.out.println("Handling missing data.........");
		//handleMissingData();
		
		OpenBanAppJob.LOG.info(jobKey + "");
		OpenBanAppJob.LOG.info(jobKey + " Preparting exectuion set.........");
		prepareExecutionSet();
		OpenBanAppJob.LOG.info(jobKey + " Preparting exectuion set......... done");

		
		// write the execution set
		DatastreamManager.storeExecutionSetIntoDropbox(userId, appname, "execute", executionSet);		
		String jsonData = JsonUtil.json.toJson(executionSet);
		
		
		ResultFormat rf;

		
		// get the stored model id.
		String classifier = toRFunction(app.analyze.classifier);
		
		try {
			String options = "{" + app.analyze.options + "}";

			OpenBanAppJob.LOG.info(jobKey + "");
			OpenBanAppJob.LOG.info(jobKey + " classifier : " + classifier);
			OpenBanAppJob.LOG.info(jobKey + " Execute model ........");
			t1 = System.currentTimeMillis();
			rf = ProcessService.getInstance().executeModel(classifier, modelId, jsonData, options);
			t2 = System.currentTimeMillis();
			OpenBanAppJob.LOG.info(jobKey + " Execute model ........done");
			OpenBanAppJob.LOG.info(jobKey + " MODEL_EXECUTION " + (t2-t1));


			//ResultFormat rf = JsonUtil.fromJson(result, ResultFormat.class);
			
			TimeSeries dataMap = null;
			// get a single time series
			for (String tkey : executionDataNew.keySet()) {
				dataMap = executionDataNew.get(tkey);
			}
			
			System.out.println("\texecution size : " + dataMap.keySet().size());
			System.out.println("\tResult size : " + rf.predicted.length);

			// map to time stamp
			TimeSeriesContainer tsContainer = new TimeSeriesContainer();
			int index = 0;
			for (DateTime dt : dataMap.keySet()) {				
				tsContainer.datapoints.put(dt, rf.predicted[index++]);
			}
			
			Map<DateTime, Double> tsFiltered = new LinkedHashMap<DateTime, Double> ();

			double threshold = -0.6;
			index = 0;
			for (DateTime dt : dataMap.keySet()) {				
				if(rf.predicted[index] > threshold) {
					tsFiltered.put(dt, dataMap.get(dt));	
				}
				++index;
			}
			
			handleConsumers(tsContainer.datapoints);
			
			String execute_now = "execute_now";
			// update the cache
			DatastreamManager.updateExecutionNowResult(userId, appname, execute_now, new TimeSeries(tsContainer.datapoints));
			String result = JsonUtil.json.toJson(tsContainer);
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
			String jobId = AppScheduler.scheduleTasklet(userId, appname, app.act.time_duration, app.act.time_unit);
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
