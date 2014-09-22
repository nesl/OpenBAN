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
 * Name: DatastreamManager.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.joda.time.DateTime;

import controllers.Application;
import controllers.RepoProfile;
import edu.pc3.openban.cache.CacheManager;
import edu.pc3.openban.datastore.DropboxDataStore;
import edu.pc3.openban.datastore.DropboxDataStore.DataType;
import edu.pc3.openban.features.FeatureManager;
import edu.pc3.openban.user.UserProfileManager;
import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;
import edu.pc3.openban.util.TimeSeries;


public class DatastreamManager {

	private static final String DELIM = "--";
	private static final String DATA = "DATA";

	public static String getCacheKey(String userId, String appname, String service,
			String datastream, String from, String to) {
		return userId + DELIM + appname + DELIM + service + DELIM 
				+ datastream + DELIM + from + DELIM + to;
	}

	public static String getDataKey(String service,
			String datastream, String from, String to) {
		return service + DELIM + datastream + DELIM + from + DELIM + to;
	}

	public static String getFeatureDataKey(String service,
			String datastream, String from, String to, String feature, int window_size) {
		return service + DELIM 
				+ datastream + DELIM + from + DELIM + to + DELIM + feature + DELIM + window_size;
	}

	public static String getFeatureCacheKey(String userId, String appname, String service,
			String datastream, String from, String to, String feature, int window_size) {
		return userId + DELIM + appname + DELIM + service + DELIM 
				+ datastream + DELIM + from + DELIM + to + DELIM + feature + DELIM + window_size;
	}

	private static TimeSeries downloadDataFromRepo(String userId, String appname,
			String reponame, String datastream, String from, String to) {

		Map<DateTime, Double> dataMap = null;		
		
		RepoProfile repoProfile = new UserProfileManager(userId).getRepoProfile(reponame);
		
		if(repoProfile == null) {
			return null;
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
			
			// get xively datastream name which is last token
			System.out.println(x_datastream+"...............");
			System.out.println("dowloading data....");
			dataMap = DataSourceAdapter.fetchXivelyData(key, feed, x_datastream,
					from, to);
		} else if (reposource.equals(Const.DROPBOX)) {
			// download the datastream from dropbox
			
			// parse the folder and dir name
			// we assume that there is no space in folder and filename
			StringTokenizer st = new StringTokenizer(datastream, " ");
			
			System.out.println(datastream+"...............");
			
			String dsFolder = st.nextToken();
			String dsFile = st.nextToken();
			
			System.out.println("Fetching.. " + dsFolder + "/" + dsFile);
			
			String csvData =  DropboxDataStore.getInstance(userId).fetchDatastream( dsFolder, dsFile);
			System.out.println("lengh " + csvData.length());
			dataMap = DataFomatter.toTimeSeries(csvData, from, to);	
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
			
			System.out.println(device + " " + sensor + " " + channel + " " + from + " " + to);			
			dataMap = DataSourceAdapter.fetchSensorActData( repoProfile.getRepourl(), repoProfile.getKey(), 
					device, sensor, channel, from, to);			
		} else if (reposource.equals(Const.SMAP)) {
			
			System.out.println(repoProfile.getRepourl() + " " + repoProfile.getKey() + "  " + datastream);
			
			// get device, sensor and channel
			
			// split and feed id and datastream
			StringTokenizer st = new StringTokenizer(datastream, " ");			
			String device = st.nextToken();			
			String sensor= st.nextToken();
			
			StringTokenizer st1 = new StringTokenizer(sensor, "$");
			sensor= st1.nextToken();
			String channel = st1.nextToken();
			
			System.out.println(device + " " + sensor + " " + channel + " " + from + " " + to);			
			dataMap = DataSourceAdapter.fetchsMapData( repoProfile.getRepourl(), repoProfile.getKey(), 
					device, sensor, channel, from, to);			
		}
		
		
		if(dataMap == null || dataMap.size() == 0) {
			return null;
		}
		
		return new TimeSeries(dataMap);
	}

	// we can't deserialize Time Java map directly
	public static class  TimeSeriesContainer  {
		//public TimeSeries datapoints;
		public Map<DateTime, Double> datapoints = new LinkedHashMap<DateTime, Double>();
	}	

	public static class  TimeSeriesContainer_map {
		//TODO: error will come while deserializing
		public Map<String, TimeSeries> datapoints = new LinkedHashMap<String, TimeSeries>();
	}

	public static void storeTrainingDataIntoDropbox(String userId, String appname, String dataKey, 
			Map<String, TimeSeries> data) {	
		System.out.println("Storing to dbx " + dataKey);		
		TimeSeriesContainer_map out = new TimeSeriesContainer_map();		
		out.datapoints = data;
		String jsonData = JsonUtil.json1.toJson(out);
		System.out.println("#bytes " + jsonData.length());
		DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, jsonData, DataType.TRAINING);
	}

	public static void storeTrainingSetIntoDropbox(String userId, String appname, String dataKey, 
			Map<String, Collection<Double>> data) {	
		System.out.println("Storing to dbx " + dataKey);		
		//out.datapoints = data;
		//String jsonData = JsonUtil.json1.toJson(out);
		String csv = DataFomatter.makeCSV1(data);
		DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, csv, DataType.TRAINING);
	}

	public static void storeExecutionSetIntoDropbox(String userId, String appname, String dataKey, 
			Map<String, Collection<Double>> data) {	
		System.out.println("Storing to dbx " + dataKey);		
		//out.datapoints = data;
		//String jsonData = JsonUtil.json1.toJson(out);
		String csv = DataFomatter.makeCSV(data);
		DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, csv, DataType.EXECUTION);
	}
	
	public static void storeExecutionSetIntoDropbox1(String userId, String appname, String dataKey, 
			Map<String, Collection<String>> data) {	
		System.out.println("Storing to dbx " + dataKey);		
		//out.datapoints = data;
		//String jsonData = JsonUtil.json1.toJson(out);
		//String csv = DataFomatter.makeCSV(data);
		//DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, csv, DataType.EXECUTION);
	}

	public static void storeModelInfo(String userId, String appname, String dataKey, 
			String data) {	
		System.out.println("Storing to dbx " + dataKey);		
		//out.datapoints = data;
		//String jsonData = JsonUtil.json1.toJson(out);
		DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, data, DataType.Model);
	}

	public static String getModelInfo(String userId, String appname, String classifier) {
		return DropboxDataStore.getInstance(userId).getModelInfo(appname, classifier, DataType.Model);
	}

	public static void storeExecutionOutputDropbox(String userId, String appname, String dataKey, 
			String data) {	
		System.out.println("Storing to dbx " + dataKey);		
		//out.datapoints = data;
		//String jsonData = JsonUtil.json1.toJson(out);
		DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, data, DataType.EXECUTION);
	}

	// store data into dropbox
	public static void storeRawData(String userId, String appname, String dataKey, 
			TimeSeries data, DataType dataType) {	
		System.out.println("Storing to dbx " + dataKey);		
		TimeSeriesContainer out = new TimeSeriesContainer();		
		out.datapoints = data;
		String jsonData = JsonUtil.json1.toJson(out);
		DropboxDataStore.getInstance(userId).storeAppData(appname, dataKey, jsonData, dataType);
	}

	// retrieve data from dropbox
	public static TimeSeries retrieveData(String userId, String appname, String dataKey, DataType dataType) {	
		System.out.println("Reading from dbx " + dataKey);		
		String dataJson = DropboxDataStore.getInstance(userId).loadAppData(appname, dataKey, dataType);
		TimeSeriesContainer dataMap = JsonUtil.json.fromJson(dataJson, TimeSeriesContainer.class);
		
		if(dataMap!= null && dataMap.datapoints!=null) {
			return new TimeSeries(dataMap.datapoints);
		}	
		return null;
	}
	
	public static TimeSeries filterWaterMeterData(TimeSeries tsData) {
		
		if(tsData == null || tsData.size() == 0) {
			return null;
		}
		
		TimeSeries newData = new TimeSeries();		
		double preVal = 0;
		for(DateTime keyTime : tsData.keySet()) {			
			double val = (Double)tsData.get(keyTime).doubleValue();			
			if(val >= 0 && val <= 5000) {
				newData.put(keyTime, (Double)tsData.get(keyTime));	
			} else {
				if(preVal >= 0 && preVal <= 5000){
					newData.put(keyTime, new Double(preVal));
				}	
				else  {
					newData.put(keyTime, new Double(0));
				}	
			}
			preVal = val;
		}
		return newData;
	}

		
	// order of retrieval
	// 1. Cache
	// 2. Dropbox
	// 3. Data service
	public static TimeSeries getRawData(String userId, String appname, String service,
			String datastream, String from, String to) {

		TimeSeries tsData = null;
		String cacheKey = getCacheKey(userId, appname, service, datastream, from, to);
		String dataKey  = getDataKey(service, datastream, from, to);

		RepoProfile repoProfile = new UserProfileManager(userId).getRepoProfile(service);
		
		System.out.println("Looking data at Cache:" + dataKey );
		Object obj = CacheManager.get(cacheKey);
		if (obj == null) {
			
			// check only for xively data
			if(repoProfile!= null && repoProfile.getReposource().equals(Const.XIVELY)) {
				System.out.println("Looking data at Dropbox:" + dataKey );
				tsData = retrieveData(userId, appname, dataKey, DataType.RAW);				
			}			
			
			// If not found in dropbox? Go and download from data repository
			if(tsData == null) {
				System.out.println("Downloading data from Repo:" + dataKey );
				tsData = downloadDataFromRepo(userId, appname, service, datastream, from, to);
				
				if(tsData != null) {
					
					for(DateTime keyTime : tsData.keySet()) {
						System.out.println("***************** downloadDataFromRepo " + keyTime.toString());
						break;
					}
					
					//TODO: Filter.. at present only for water meter data..				
					//tsData = filterWaterMeterData(tsData);
					
					for(DateTime keyTime : tsData.keySet()) {
						System.out.println("***************** filterWaterMeterData " + keyTime.toString());
						break;
					}
					
				}
				
				if (tsData != null && tsData.entrySet().size() > 0) {
					System.out.println("Downloaded #datapoitns " + tsData.entrySet().size());
					
					// store only the xively data
					if(repoProfile!= null && repoProfile.getReposource().equals(Const.XIVELY)) {
						System.out.println("Storing data to Dropbox:" + dataKey );
						storeRawData(userId, appname, dataKey, tsData, DataType.RAW);						
					}
				}
			} 
			
			if (tsData != null && tsData.size() > 0) {
					System.out.println("Storing data to Cache:" + dataKey );
					
					for(DateTime dt:tsData.keySet()) {
						System.out.println("before cached..... " + dt.toString());
						break;
					}					
					CacheManager.set(cacheKey, tsData);
					
					TimeSeries tsData1 = (TimeSeries)CacheManager.get(cacheKey);
					
					for(DateTime dt:tsData1.keySet()) {
						System.out.println("after cached..... " + dt.toString());
						break;
					}

			}			
			
		} else {			
			tsData = (TimeSeries)obj;
			for(DateTime dt:tsData.keySet()) {
				System.out.println("cached..... " + dt.toString());
				break;
			}
			System.out.println("Found #datapoints at Cache:" + dataKey + "  " + tsData.entrySet().size());
		}
		
		return tsData;
	}

	// order of retrieval
	// 1. Cache
	// 2. Dropbox
	// 3. Data service
	public static TimeSeries getFeatureData(String userId, String appname, String service,
			String datastream, String from, String to, String feature, int window_size) {

		TimeSeries tsData = null;
		String cacheKey = getFeatureCacheKey(userId, appname, service, datastream, from, to, feature, window_size);
		String dataKey = getFeatureDataKey(service, datastream, from, to, feature, window_size);

		RepoProfile repoProfile = new UserProfileManager(userId).getRepoProfile(service);

		System.out.println("Looking data at Cache:" + dataKey );
		Object obj = CacheManager.get(cacheKey);
		if (obj == null) {
			
			// check only for xively data
			if(repoProfile!= null && repoProfile.getReposource().equals(Const.XIVELY)) {
				System.out.println("Looking data at Dropbox:" + dataKey );
				tsData = retrieveData(userId, appname, dataKey, DataType.FEATURE);
			}			
			
			// If not found in dropbox? Go and download from data repository
			if(tsData == null) {
				System.out.println("Downloading data from Repo:" + dataKey );
				TimeSeries rawData = getRawData(userId, appname, service, datastream, from, to);
				
				if (rawData == null || rawData.entrySet().size() == 0) {
					System.out.println("getRawData : no data found" );

					return null;
				}				
				System.out.println("Computing feature :" + feature );
				tsData = FeatureManager.computeFeature(rawData, feature, window_size );
				//tsData = FeatureManager.computeFeature1(rawData, feature, window_size, from, to );
				
				if (tsData != null && tsData.entrySet().size() > 0) {
					System.out.println("Downloaded #datapoitns " + tsData.entrySet().size());
					
					// store only the xively data
					if(repoProfile!= null && repoProfile.getReposource().equals(Const.XIVELY)) {
						System.out.println("Storing data to Dropbox:" + dataKey );
						storeRawData(userId, appname, dataKey, tsData, DataType.FEATURE);						
					}
				}
			} 
			
			if (tsData != null && tsData.entrySet().size() > 0) {
					System.out.println("Storing data to Cache:" + dataKey );
					CacheManager.set(cacheKey, tsData);
			}			
			
		} else {			
			tsData = (TimeSeries)obj;

			System.out.println("Foun #datapoints at Cache:" + dataKey + "  " + tsData.entrySet().size());
		}
		
		return tsData;
	}
	
	public static String getExecutionDataCacheKey(String userId, String appname, String instanceName) {
		return userId + DELIM + appname + DELIM + instanceName;
	}
	
	
	public static void updateExecutionNowResult(String userId, String appname, String instanceName, TimeSeries tsData) {		
		String cacheKey = getExecutionDataCacheKey(userId, appname, instanceName);
		CacheManager.delete(cacheKey);
		CacheManager.set(cacheKey, tsData);
	}
	
	
	// order of retrieval
	// 1. Cache
	// 2. Dropbox
	public static TimeSeries getExecutionData(String userId, String appname, String instanceName) {

		TimeSeries tsData = null;		
		String cacheKey = getExecutionDataCacheKey(userId, appname, instanceName);
		System.out.println("Looking data at Cache:" + cacheKey );
		Object obj = CacheManager.get(cacheKey);
		if (obj == null) {			
			System.out.println("Looking data at Dropbox:" + instanceName);
			tsData = retrieveData(userId, appname, instanceName, DataType.EXECUTION);
			
			// If not found in dropbox? there is some problem somewhere
			if (tsData != null && tsData.entrySet().size() > 0) {
					System.out.println("Storing data to Cache:" + cacheKey );
					CacheManager.set(cacheKey, tsData);
			}
		} else {			
			tsData = (TimeSeries)obj;
			System.out.println("Found #datapoints at Cache:" + cacheKey + "  " + tsData.entrySet().size());
		}		
		return tsData;
	}
	
	public static class TableDataOutFormat {
		public String iTotalRecords; 
		public String iTotalDisplayRecords;
		public List<List<String>> aaData = new ArrayList<List<String>>();		
	}

	public static TableDataOutFormat toDatatableFormat(TimeSeries dataMap, int iDisplayStart, int iDisplayLength)  {
		
		// validate iDisplayStart and iDisplayLength		
		TableDataOutFormat out = new TableDataOutFormat();
		
		if(dataMap == null || dataMap.size() == 0 ) {
			out.iTotalRecords = "0";
			out.iTotalDisplayRecords = "0";
			//List<String> one = new ArrayList<String>();	
			//out.aaData.add(one);
			return out;
		}
		
		out.iTotalRecords = dataMap.entrySet().size() + "";
		out.iTotalDisplayRecords = dataMap.entrySet().size() + "";

		int index = 0;
		int limit = iDisplayStart + iDisplayLength;
		for(DateTime keyTime : dataMap.keySet()) {
			
			if(index >= iDisplayStart ) {
				
				List<String> one = new ArrayList<String>();			
				one.add( keyTime.toString());
				//one.add(keyTime.toInstant().getMillis()+"");
				one.add(dataMap.get(keyTime)+"");				
				out.aaData.add(one);
				//System.out.println(keyTime.toString());
			}
			
			if(index > limit ) {
				break;
			}			
			++index;
		}
		
		// System.out.println(JsonUtil.json1.toJson(out));		
		return out;
	}

	public static TableDataOutFormat toDatatableFormatAll(int iDisplayStart, int iDisplayLength)  {
		
		
		Map<String, TimeSeries> dataAll = Application.trainingDataNew;
		
		Double[][] allData = new Double[dataAll.size()][];
		
		int index = 0;
		// get the first map
		TimeSeries dataMap = null;		
		for(String key: dataAll.keySet()) {
			System.out.println(key);			
			dataMap = dataAll.get(key);			
			List<Double> dd =  new ArrayList<Double>();			
			for(DateTime dtt: dataMap.keySet()) {
				dd.add(dataMap.get(dtt));
			}			
			allData[index++] = dd.toArray(new Double[0]);
			//break;
		}		
		System.out.println("size " + allData.length);
		for(Double[] a1 : allData) {
			System.out.println("..size " + a1.length);
		}
		
		// validate iDisplayStart and iDisplayLength		
		TableDataOutFormat out = new TableDataOutFormat();
		
		if(dataMap == null || dataMap.size() == 0 ) {
			out.iTotalRecords = "0";
			out.iTotalDisplayRecords = "0";
			//List<String> one = new ArrayList<String>();	
			//out.aaData.add(one);
			return out;
		}
		
		out.iTotalRecords = dataMap.entrySet().size() + "";
		out.iTotalDisplayRecords = dataMap.entrySet().size() + "";

		index = 0;
		int limit = iDisplayStart + iDisplayLength;
		for(DateTime keyTime : dataMap.keySet()) {
			
			if(index >= iDisplayStart ) {
				
				List<String> one = new ArrayList<String>();			
				one.add( keyTime.toString());
				
				for(int kk=0; kk<allData.length; ++kk) {
					Double dd = allData[kk][index];					
					if(dd.intValue() < 2 ) {
						one.add((1000*dd.doubleValue())+"");
					} else {
						one.add(dd.doubleValue()+"");	
					}
					
					
				}				
				//one.add(keyTime.toInstant().getMillis()+"");
				//one.add(dataMap.get(keyTime)+"");
				//one.add((dataMap.get(keyTime).intValue()+2)+"");
				
				out.aaData.add(one);
				//System.out.println(keyTime.toString());
			}
			
			if(index > limit ) {
				break;
			}			
			++index;
		}
		
		// System.out.println(JsonUtil.json1.toJson(out));		
		return out;
	}


}
