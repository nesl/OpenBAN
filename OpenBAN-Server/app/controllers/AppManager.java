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
 * Name: AppManager.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package controllers;

import edu.pc3.openban.cache.CacheManager;
import edu.pc3.openban.datastore.DropboxDataStore;
import edu.pc3.openban.model.AppFormat;
import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;

public class AppManager {
	
	public static AppFormat toObj(String json) {		
		try {
			return JsonUtil.fromJson(json, AppFormat.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}

	public static AppFormat loadApp(String userId, String appname) {
		
		/*
		String cacheKey = userId + "--" + appname;		
		String appJson = (String)CacheManager.get(cacheKey);
		if(appJson == null) {
			appJson = DropboxDataStore.getInstance(userId).loadAppInfo(appname);
			CacheManager.set(cacheKey, appJson);
		}*/
		 
		String appJson = DropboxDataStore.getInstance(userId).loadAppInfo(appname);
		AppFormat app = null;		
		try {
			app = JsonUtil.fromJson(appJson, AppFormat.class);
		} catch (Exception e) {
			System.out.println(appJson);
			System.out.println(e.getMessage());
			
			//e.printStackTrace();
		}
		
		// load the datastreams on demand
		
/*		AppFormat.DatastreamTree training  = app.aggregate.training.get(0);
		
		if(training != null && training.children != null) {
			
			String date_from = app.aggregate.from_date;
			String date_to = app.aggregate.to_date;
			
			for(AppFormat.Node data_repo : training.children) {
				String data_repo_name = data_repo.name;
				for(AppFormat.Node data_stream : data_repo.children) {
					String data_stream_name = data_stream.name;
					System.out.println(data_repo_name + "  " + data_stream_name);
					
					DatastreamManager.loadData(userId, appname, 
							data_repo_name, data_stream_name, date_from, date_to);
				}
			}			
		}
*/		
		
		// load data
		return app;
	}
	
	public static String getRecentApp(String userId) {		
		return DropboxDataStore.getInstance(userId).getRecentApp();
	}

	public static void setRecentApp(String userId, String appname) {		
		DropboxDataStore.getInstance(userId).setRecentApp(appname);
	}

	public static String saveApp(String userId, String json) {
		

		AppFormat appFormat = toObj(json);		
		//TODO: to necessary validation

		String jsonStr = JsonUtil.json.toJson(appFormat);		
		String response = DropboxDataStore.getInstance(userId).storeAppInfo(appFormat.appname, jsonStr);

		//String cacheKey = userId + "--" + appFormat.appname;
		//CacheManager.set(cacheKey, jsonStr);

		if(response.equals(Const.SUCCESS)) {
			setRecentApp(userId,appFormat.appname);
		}
		
		return response;
	}

}
