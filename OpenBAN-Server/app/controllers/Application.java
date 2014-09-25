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
 * Name: Application.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import play.libs.F.Promise;
import play.mvc.Before;
import play.mvc.Controller;
import edu.pc3.openban.analyze.ExecutionService;
import edu.pc3.openban.analyze.TrainingService;
import edu.pc3.openban.cache.CacheManager;
import edu.pc3.openban.data.DatastreamManager;
import edu.pc3.openban.datastore.DropboxDataStore;
import edu.pc3.openban.datastore.DropboxSessionManager;
import edu.pc3.openban.features.FeatureManager;
import edu.pc3.openban.model.AppFormat;
import edu.pc3.openban.user.UserProfileManager;
import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;
import edu.pc3.openban.util.TimeSeries;

public class Application extends Controller {

	public static String SESSION_KEY = "dbx-uid";
	public static String FLASH_MSG = "flash-msg";
	public static String NEW_APP = "newapp";
	
	public static List<String> classifiers = new ArrayList<String>();
	
	static {
		CacheManager.init();
		
		try {
			//ModelRepo.storeModelInfo();	
			ModelRepo.loadModelInfo();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		classifiers.addAll(ModelRepo.alogrithms.keySet());
		
		//classifiers.add(Const.DECISION_TREE);
		//classifiers.add(Const.REGRESSION);
		//classifiers.add(Const.NEURAL_NETWORK);
		//classifiers.add(Const.SVM);
		//classifiers.add(Const.KNN);
		//classifiers.add(Const.NAIVE_BAYES);
		//classifiers.add(Const.SVR);
	}
	
	@Before(unless = {"index", "signin", "signout", "help", "test"})
	static void checkAuthentication() {
		
		String userId = session.get(SESSION_KEY);
		String token = DropboxSessionManager.getAccessToken(userId);
		
		System.out.println("Request...  " + request.path);
		System.out.println("UserId  " + userId + "  " + token);
				
		//System.out.println("session name " + userId);
		if(null == userId) {
			System.out.println("Invalid session (name).. redirecting..");
			index();
		}
						
		if(null == token ) {
			System.out.println("Invalid session - dropbox.. redirecting..");
			index();
		}		
	}
	
	@Before(only = {"signin", "auth/dropbox/request", "auth/dropbox/response"})
	static void sslOrRedirect() {

		// skip https for localhost
		if(request.getBase().toString().contains("localhost"))
			return;
		
		// force to use https on other domains e.g. google app engine
		if (!request.secure) {
			redirect("https://" + request.host + request.url);
		}
	}
	
	public static class DataOutFormat {
		public List<String> timestamp = new ArrayList<String>();
		public List<Double> data = new ArrayList<Double>();
	}
	
	public static void getDataJson() {
		
		DataOutFormat out = new DataOutFormat();
		
		DateTime d = DateTime.now();
		
		for(int i=0; i<20; ++i) {		
			out.timestamp.add(d.plusMinutes(i).toString());
			out.data.add( new Double(i));
		}
		
		renderJSON(out);		
	}

	public static class TableDataOutFormat {
		public String iTotalRecords; 
		public String iTotalDisplayRecords;
		public List<List<String>> aaData = new ArrayList<List<String>>();		
	}

	public static int length = 10000;
	public static DateTime time = DateTime.now();
	
	public static void getDataJson1_O(String service, String datastream, String from, String to, String iDisplayStart, String iDisplayLength) {
		System.out.println(service + "   " + iDisplayStart + ", " + iDisplayLength);
	
		TableDataOutFormat out = new TableDataOutFormat();
		out.iTotalRecords = length+"";
		out.iTotalDisplayRecords = length+"";
		
		int istart = Integer.parseInt(iDisplayStart);
		int ilength = Integer.parseInt(iDisplayLength);
		
		for(int i=0; i<ilength; ++i) {					
			
			List<String> one = new ArrayList<String>();			
			one.add(time.plusMinutes(istart+i).toInstant().getMillis()+"");
			//one.add(time.plusMinutes(istart+i).toLocalTime().toString());
			one.add(istart+i+"");
			
			out.aaData.add(one);
		}
		
		try {
			Thread.sleep(10000);
		}catch(Exception e) {}
		
		renderJSON(out);		
	}
	
	public static void getDataJson1(String appname, String service, String datastream, String from, String to,
			int iDisplayStart, int iDisplayLength) {
		
		System.out.println(service + "   " + datastream + " "
				+ from + " " + to + " "
				+ iDisplayStart + ", " + iDisplayLength);
		
		String userId = session.get(SESSION_KEY);
		if(userId ==null) {
			renderJSON("Invalid session");
		}
		
		TimeSeries dataMap = DatastreamManager.getRawData(userId, appname, 
				service, datastream, from, to);
		
		//System.out.println("Sending #datapoints:" + dataMap.size());
		//System.out.println(JsonUtil.json1.toJson(dataMap));
		
		Object out  = DatastreamManager.toDatatableFormat(dataMap, iDisplayStart, iDisplayLength);
		renderJSON(out);		
	}

	public static void getDataJson2(String appname, String service, String datastream, 
				String from, String to, String feature, int window_size, 
				int iDisplayStart, int iDisplayLength) {
		
		System.out.println(service + "   " + datastream + " "
				+ feature + " " + window_size + " "
				+ from + " " + to + " "
				+ iDisplayStart + ", " + iDisplayLength);
		
		String userId = session.get(SESSION_KEY);
		if(userId ==null) {			
			renderJSON("Invalid session");
		}
		
		TimeSeries dataMap = DatastreamManager.getFeatureData(userId, appname, 
				service, datastream, from, to, feature, window_size);
		
//		System.out.println("Sending #datapoints:" + dataMap.size());

		
		Object out  = DatastreamManager.toDatatableFormat(dataMap, iDisplayStart, iDisplayLength);
		renderJSON(out);		
	}

	
	public static Map<String, TimeSeries> trainingDataNew = new LinkedHashMap<String, TimeSeries>();
	public static void getDataJson2All(String appname, String service, String datastream, 
			String from, String to, String feature, int window_size, 
			int iDisplayStart, int iDisplayLength) {
	
	System.out.println(service + "   " + datastream + " "
			+ feature + " " + window_size + " "
			+ from + " " + to + " "
			+ iDisplayStart + ", " + iDisplayLength);
	
	String userId = session.get(SESSION_KEY);
	if(userId ==null) {			
		renderJSON("Invalid session");
	}
	
	TimeSeries dataMap = DatastreamManager.getFeatureData(userId, appname, 
			service, datastream, from, to, feature, window_size);
	
//	System.out.println("Sending #datapoints:" + dataMap.size());

	
	Object out  = DatastreamManager.toDatatableFormatAll(iDisplayStart, iDisplayLength);
	renderJSON(out);		
}

	
	// fetch execution result data items
	public static void getDataJson3(String appname, String instance,
			int iDisplayStart, int iDisplayLength) {
	
	System.out.println( instance + "  " + iDisplayStart + ", " + iDisplayLength);	
	String userId = session.get(SESSION_KEY);
	if(userId ==null) {			
		renderJSON("Invalid session");
	}
	
	TimeSeries dataMap = DatastreamManager.getExecutionData(userId, appname, instance);
	
//	System.out.println("Sending #datapoints:" + dataMap.size());

	
	Object out  = DatastreamManager.toDatatableFormat(dataMap, iDisplayStart, iDisplayLength);
	renderJSON(out);		
}

	
	public static void getData() {
		render();
	}

	public static Promise<String> jobPromise = null;
	
		
	public static void test() {		
//		/await(new DataDownloaderJob("p1").now());
		
		System.out.println("testing..");
		
		String csv = "timestamp,value\n2013-07-01T00:00:00.000-07:00, -0.41582599";
	}
	
	public static void index() {
		// renderArgs.put("message", "Login failed");
		
		String time = "1333264200";
		int ms = 0;
		
		DateTime dt;
		
		if(time.length() == 10) {
			int sec = Integer.parseInt(time);
			dt = new DateTime(sec*1000);
		} else if(time.length() == 13) {
			int sec = Integer.parseInt(time);
			dt = new DateTime(sec);
		} else {
			dt = DateTime.parse(time);	
		}
		
		//System.out.println(dt.toString());
		
		String flashmsg = session.get(FLASH_MSG);
		session.remove(FLASH_MSG);
		renderArgs.put("flashmsg", flashmsg);
		
		render();
	}

	public static void signin() {
		System.out.println("sigin");
		
		String userId = session.get(SESSION_KEY);
		String token = DropboxSessionManager.getAccessToken(userId);
		// if valid session
		if(userId != null && token != null) {
			redirect("/home");	
		}
		
		System.out.println("redirecating to " + DropboxAuthentication.SIGNIN_ENDPOINT);
		redirect(DropboxAuthentication.SIGNIN_ENDPOINT);
	}

	public static void signout() {		
		String userId = session.get(SESSION_KEY);
		
		DropboxSessionManager.deleteAccessToken(userId);
		
		//TODO: remove all session data from edu.pc3.openban.cache...
		session.clear();
		
		session.put(FLASH_MSG, "Signed out successfully!");
		index();
	}

	public static void home() {		

		String userId = session.get(SESSION_KEY);				
		//String token = DropboxSessionManager.getAccessToken(userId);
		
		try {
			new UserProfileManager(userId).loadProfile();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		String app = AppManager.getRecentApp(userId);
		
		System.out.println("Recent app :" + app);
		if(app == null || app.length() < 1 ) {
			app = NEW_APP;  // create a new app
		}
		
		// check and load the recent app 
		// or load a new app
		redirect("/home/" + app);
	}
	
	public static void homeApp(String appname) {
		
		System.out.println("appname   " + appname);
		String userId = session.get(SESSION_KEY);				
		//String token = DropboxSessionManager.getAccessToken(userId);
		
		try {
			new UserProfileManager(userId).loadProfile();	
		} catch(Exception e) {			
			session.put(FLASH_MSG, "Unable to load user profile ");
		}
				
		// load app data...

		// TODO: to handle null pointer exception
		renderArgs.put("aggregate_training_tree", "");
		renderArgs.put("aggregate_groundtruth_tree", "");
		renderArgs.put("feature_window_size", "");				
		renderArgs.put("analyze_features_tree", "");				
		renderArgs.put("act_consumer_tree", "");
		
		if(!appname.equals(NEW_APP)) {
			
			System.out.println("Loading app...." + appname);
			AppFormat app = AppManager.loadApp(userId, appname);
			if(app != null) {
				
				renderArgs.put("agr_from_date", app.aggregate.from_date);
				renderArgs.put("agr_to_date", app.aggregate.to_date);
				
				String aggregate_training_tree = JsonUtil.toJson(app.aggregate.training);
				String aggregate_groundtruth_tree = JsonUtil.toJson(app.aggregate.groundtruth);				
				renderArgs.put("aggregate_training_tree", aggregate_training_tree);
				renderArgs.put("aggregate_groundtruth_tree", aggregate_groundtruth_tree);
				
				renderArgs.put("feature_window_size", app.analyze.feature_window_size);				
				String analyze_features_tree = JsonUtil.toJson(app.analyze.features);				
				renderArgs.put("analyze_features_tree", analyze_features_tree);				
				renderArgs.put("classifier_name", app.analyze.classifier);
				renderArgs.put("model_options", app.analyze.options);

				renderArgs.put("act_from_date", app.act.from_date);
				renderArgs.put("act_to_date", app.act.to_date);
				renderArgs.put("act_time_duration", app.act.time_duration);
				renderArgs.put("act_time_unit", app.act.time_unit);
				
				String act_consumer_tree = JsonUtil.toJson(app.act.consumers);
				renderArgs.put("act_consumer_tree", act_consumer_tree);
				
				//TODO: other attributes

			} else {
				//renderArgs.put("initmsg", "Unknown app name " + appname + " New App created!" );
				session.put(FLASH_MSG, "Loading app " + appname + " failed! Created " + NEW_APP);
				redirect("/home/"+NEW_APP);		
			}
		}
		
		List<String> repos = new UserProfileManager(userId).getRepoProfileNames();
		List<String> apps = new UserProfileManager(userId).getAppNames();
		
		//System.out.println("apps  " + apps);
		//System.out.println("repos  " + repos);
		
		String flashmsg = session.get(FLASH_MSG);
		session.remove(FLASH_MSG);
		renderArgs.put("flashmsg", flashmsg);
		
		renderArgs.put("appname", appname);		
		renderArgs.put("applist", apps);
		renderArgs.put("repolist", repos);		
		renderArgs.put("userid", userId);
		renderArgs.put("username", new UserProfileManager(userId).getDisplayName());
		
		// 
		renderArgs.put("classifiers", ModelRepo.alogrithms.keySet());
		
		//renderArgs.put("token", token);		
		render();
	}
	
	
	public static void saveApp() {		
		String userId = session.get(SESSION_KEY);
		String json = request.params.get("body");
		System.out.println(json);
		String res = AppManager.saveApp(userId, json);
		if(!res.equals(Const.SUCCESS)) {
			session.put(FLASH_MSG, res);	
		}
		
		// to update the appname list
		//

		renderText(res);
	}
	
	public static void loadApp(String appname) {		
		String userId = session.get(SESSION_KEY);
		//System.out.println(json);
		Object app = AppManager.loadApp(userId, appname);
		System.out.println("loading app " + appname);
		renderJSON(app);
	}

	public static void datastreamList() {		
		String userId = session.get(SESSION_KEY);		
		String json = new UserProfileManager(userId).getDatastreamListAsJson();
		//System.out.println("data stream list");		
		renderJSON(json);
	}
	
	public static void registerRepo(String reposource, String reponame, String repourl, String userid, String key) {

		String sessionUserId = session.get(SESSION_KEY);
		String res =  new UserProfileManager(sessionUserId).registerRepo(reposource, reponame, repourl, userid, key);
		
		try {
			Thread.sleep(2000);
		}catch(Exception e) {}
		
		renderText( res );
	}
	
	public static void trainModel(String appname) {
		
		String userId = session.get(SESSION_KEY);		
		// validate the appname and userid
		String res = new TrainingService(userId, appname).trainModel();
		renderText(res);
	}

	public static void executeModel(String appname) {
		
		String userId = session.get(SESSION_KEY);		
		// validate the appname and userid
		String res = new ExecutionService(userId, appname).executeModel();
		renderText(res);
	}

	public static void scheduleApp(String appname) {
		
		String userId = session.get(SESSION_KEY);		
		// validate the appname and userid
		int n=20;		
		String res = null;
		for(int i=1; i<=n; i++) {
			String an = appname + "_" + i;
			System.out.println("Scheduling " + an);
			res = new ExecutionService(userId, an).scheduleApp();
		}		
		renderText(res);
	}

	
	public static void help() {
		render();
	}
	
	public static void featureTest() {		
		String userId = session.get(SESSION_KEY);
		String json = request.params.get("body");
		System.out.println(json);
		String res = FeatureManager.testNewFeature(json);
		renderText(res);
	}

	
	public static void featureList() {
		
		Map<String, List<String>> feature = new HashMap<String,List<String>>();
		
		List<String> stat = new ArrayList<String>();		
	
		stat.add(Const.COUNT);
		stat.add(Const.MINIMUM);
		stat.add(Const.MAXIMUM);
		stat.add(Const.RANGE);
		stat.add(Const.SUM);
		stat.add(Const.SUMSQUARE);
		stat.add(Const.MEDIAN);
		stat.add(Const.MEAN);
		stat.add(Const.MEAN_DIFFERENCE);
		stat.add(Const.GEOMETRIC_MEAN);
		stat.add(Const.STANDARD_DEVIATION);
		stat.add(Const.VARIANCE);
				
		List<String> temporal = new ArrayList<String>();
		temporal.add(Const.MIN_OF_THE_HOUR);
		temporal.add(Const.MIN_OF_THE_DAY);
		temporal.add(Const.HOUR_OF_THE_DAY);
		temporal.add(Const.DAY_OF_THE_WEEK);
		temporal.add(Const.DAY_OF_THE_MONTH);
		temporal.add(Const.EPOCH_IN_MILLIS);
		
		feature.put(Const.STATISTICAL, stat);
		feature.put(Const.TEMPORAL, temporal);
		
		renderJSON(feature);
	}

}