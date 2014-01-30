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
 * Name: SensorActAdapter.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.data.adapter.sensoract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat.DeviceChannel;
import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat.DeviceFormat;
import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat.DeviceSensor;
import edu.pc3.openban.data.adapter.sensoract.SensorActAdapter.QueryDataOutputFormat.Datapoint;
import edu.pc3.openban.util.JsonUtil;

public class SensorActAdapter {	
	
	public static final Logger LOG = Logger.getLogger(SensorActAdapter.class.getName());
	
	private static String makeGetRequest(String url, Map<String, String> headers) throws Exception {
		
		WSRequest wsReq =  WS.url(url);
		
		if(headers != null ) {
			wsReq = wsReq.headers(headers);
		}

		HttpResponse response = wsReq.get();
		
		if (response.getStatus() != 200) {
			throw new Exception("Invalid response : " + response.getStatus()
					+ " " + response.getString());
		}		
		return response.getString();
		
	}	

	private static String makePostRequest(String url, String body, Map<String, String> headers) throws Exception {		
		
		WSRequest wsReq =  WS.url(url);
		
		if(headers != null ) {
			wsReq = wsReq.headers(headers);
		}
		if(body != null ) {
			wsReq = wsReq.body(body);
		}
		
		HttpResponse response = wsReq.post();
		
		if (response.getStatus() != 200) {
			throw new Exception("Invalid response : " + response.getStatus()
					+ " " + response.getString());
		}		
		return response.getString();
	}
	
	public static class DeviceListFormat {
		public String secretkey = null;
	}
	
	public static Map<String, List<String>> channelList(String host, String secretkey) {		
		
		if(!host.endsWith("/")) {
			host = host + "/";
		}
		
		String api = "device/list";
		String url = host + api;
		
		DeviceListFormat req = new DeviceListFormat();
		req.secretkey = secretkey;		
		String reqJson = JsonUtil.toJson(req);
		
		try {
			String deviceJson = makePostRequest(url, reqJson, null);
			
			DeviceProfileFormat deviceList = JsonUtil.fromJson(deviceJson, DeviceProfileFormat.class);
			
			if(deviceList != null) {
				
				Map<String, List<String>> channelMap = new HashMap<String, List<String>>();
				
				for(DeviceFormat device : deviceList.devicelist ) {
					List<String> channelList = new ArrayList<String>();					
					for(DeviceSensor sensor : device.sensors) {
						for(DeviceChannel channel : sensor.channels) {
							channelList.add(sensor.name + "$" + channel.name);
						}	
					}					
					channelMap.put(device.devicename, channelList);
				}				
				return channelMap;
			}			
			//System.out.println(JsonUtil.json1.toJson(deviceList));
		} catch(Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	
	public static class QueryDataOutputFormat {

		public static class Datapoint {
			
			public String time;
			public String value;
			
			public Datapoint(String t, String v) {
				time = t;
				value = v;
			}
		}
		
		public String device;
		public String sensor;
		public String channel;
		public List<Datapoint> datapoints = new ArrayList<Datapoint>();
	}
	
	public static Map<DateTime, Double> fetchData(String hosturl, String key, String device, 
			String sensor, String channel, String sdate, String edate, String options) {
		
		DateTime start = new DateTime(sdate);
		DateTime end = new DateTime(edate);
		end = end.plusDays(1).minusMillis(1);
		
		if(!hosturl.contains("/")) {
			hosturl = hosturl + "/";
		}
		
		String params = "?start=" + start.getMillis() + "&end=" + end.getMillis() + options;
		String url = hosturl + device + "/" + sensor + "/" + channel + params;
		System.out.println("requesting.. " + url);		
		
		try {			
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("x-apikey", key);
			
			String dataJson = makeGetRequest(url, headers);
			
			QueryDataOutputFormat data = JsonUtil.fromJson(dataJson, QueryDataOutputFormat.class);			
			if(data != null) {		
				Map<DateTime, Double> outData = new LinkedHashMap<DateTime, Double>();				
				for(Datapoint dp : data.datapoints) {
					DateTime dt = new DateTime(dp.time);
					double val = Double.parseDouble(dp.value);					
					outData.put(dt,  val);
				}
				return outData;
			}			
			//System.out.println(JsonUtil.json1.toJson(deviceList));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
    public static void sendToSensorAct(String hosturl, String key, String device, String sensor,
			String channel, long time, String value) {

    	String url= hosturl + device + "/" + sensor + "/" + channel;
		url = url + "?time="+time + "&value="+value;			
		
		try {
			Map<String,String> header = new HashMap<String,String>();
			header.put("x-apikey", key);			
			LOG.info(url);			
			WSRequest wsr = WS.url(url).headers(header).timeout("5s");		
			HttpResponse trainRes = wsr.put();	
		} catch (Exception e) {
			LOG.info("sendtoAnother.. " + url + " " + e.getMessage());
		}
	}
    
	public static boolean storeData(String hosturl, String key, String device, 
			String sensor, String channel, Map<DateTime, Double> tsData) {
		
		if(!hosturl.contains("/")) {
			hosturl = hosturl + "/";
		}
		
		try {
			for (DateTime dtKey : tsData.keySet()) {
				double value = (double)tsData.get(dtKey);		
				sendToSensorAct(hosturl, key, device, sensor, channel, dtKey.getMillis(), value+"");
			}
		} catch(Exception e) {
			LOG.info("storeData.. " + e.getMessage());
			//e.printStackTrace();
		}
		
		return true;
	}


}
