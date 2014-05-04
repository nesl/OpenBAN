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
 * Name: DataSourceAdapter.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import edu.pc3.openban.data.adapter.sensoract.SensorActAdapter;
import edu.pc3.openban.data.adapter.xively.XivelyClientService;
import edu.pc3.openban.util.Const;

public class DataSourceAdapter {
	
	
	public static Map<String, List<String>> fetchDatastreamList(String reposource, String reponame, String repourl, String userid, String key) {
		
		if(reposource.equals(Const.XIVELY)) {
			return fetchXivelyDatastreamList(key, userid);
		}
		
		if(reposource.equals(Const.SENSORACT)) {
			return SensorActAdapter.channelList(repourl, key);
		}

		return null;
	}
	
	private static Map<String, List<String>> fetchXivelyDatastreamList(String key, String username) {		
		Map<String, List<String>> feedList = new XivelyClientService().feedList(key, username );		
		return feedList;		
		//return null;
	}
	
	public static Map<DateTime, Double> fetchXivelyData(String key, String feed, String datastream, String sdate, String edate) {
		
		Map params = new HashMap();
		params.put("key", key);
		params.put("feed", feed);
		params.put("datastream", datastream);
		params.put("start", sdate);
		params.put("end", edate);
		
		String options = "&timezone=-8&interval=300&function=average";
		//String options = "&timezone=-7&interval=0";
		params.put("options", options);
		
		System.out.println(params.toString());

		return new XivelyClientService().getData(params);
		
	}

	public static Map<DateTime, Double> fetchSensorActData(String hosturl, String key, String device, String sensor, String channel, String sdate, String edate) {
		
		String options = "&timeformat=iso8601";		
		return SensorActAdapter.fetchData(hosturl, key, device, sensor, channel, sdate, edate, options);
		
		//return null;
		//return new XivelyClientService().getData(params);
		
	}

	public static boolean storeSensorActData(String hosturl, String key, String device, 
				String sensor, String channel, Map<DateTime, Double> tsData) {
		
		return SensorActAdapter.storeData(hosturl, key, device, sensor, channel, tsData);
		//return null;
		//return new XivelyClientService().getData(params);
		
	}

	
}
