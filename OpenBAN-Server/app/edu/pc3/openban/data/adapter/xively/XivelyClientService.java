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
 * Name: XivelyClientService.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.data.adapter.xively;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import play.Logger;
import play.PlayPlugin;

public class XivelyClientService extends PlayPlugin {

	private static XivelyClientService service;

	public void onApplicationStart() {
		Logger.info("Yeeha, firstmodule started");
	}

	private static class FeedListFormat {
		List<Feed> results = new ArrayList<Feed>(); 
	}	
	
	public Map<String, List<String>> feedList(String key, String user) throws Exception {		
		
		String url = "https://api.xively.com/v2/feeds.json?user=" + user;		
		String jsonResponse = null;
		FeedListFormat feedListFormat = null;
		
		Map<String, List<String>> feedList = new HashMap<String, List<String>>();
		
		System.out.println(url);
		
		try {
			jsonResponse = HTTPClient.get(url, key);
			feedListFormat = JsonUtil.fromJson(jsonResponse, FeedListFormat.class);
			// System.out.println(jsonResponse);
			
			System.out.println(JsonUtil.json1.toJson(feedList));
			
			for(Feed feed:feedListFormat.results) {
				
				List<String> dsList = new ArrayList<String>();				
				for(Datastream ds : feed.datastreams) {
					dsList.add(ds.id);
				}				
				
				String fkey = feed.id + "$" + feed.title;
				feedList.put(fkey, dsList);
			}
			
		} catch (Exception e) {
			jsonResponse = e.getMessage();
			//e.printStackTrace();
			throw e;
		}
		
		return feedList;
	}

	public Map<DateTime, Double> getData(Map params) {

		if (params == null)
			return null;

		String key = null;
		String feed = null;
		String datastream = null;
		String start = null;
		String end = null;
		
		String options = null;

		if (params.containsKey("key"))
			key = params.get("key").toString();

		if (params.containsKey("feed"))
			feed = params.get("feed").toString();

		if (params.containsKey("datastream"))
			datastream = params.get("datastream").toString();

		if (params.containsKey("start"))
			start = params.get("start").toString();

		if (params.containsKey("end"))
			end = params.get("end").toString();
		
		if (params.containsKey("options"))
			options = params.get("options").toString();
		
		System.out.println(params);
		
		return DatapointDowloader.fetchDatapoints(key, feed, datastream, start,
				end, options);
	}

}

