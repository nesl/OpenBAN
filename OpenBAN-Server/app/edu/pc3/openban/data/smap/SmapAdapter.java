package edu.pc3.openban.data.smap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat;
import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat.DeviceChannel;
import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat.DeviceFormat;
import edu.pc3.openban.data.adapter.sensoract.DeviceProfileFormat.DeviceSensor;
import edu.pc3.openban.data.adapter.sensoract.SensorActAdapter.DeviceListFormat;
import edu.pc3.openban.util.JsonUtil;

public class SmapAdapter {

	public static Map<String, SmapDevice> deviceMap = null;
	private static File device_map_file = Play.getFile("./conf/devicemap.json");
	
	public static Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
			.setPrettyPrinting().create();

	public static Map<String, SmapDevice> loadDeviceMap() throws FileNotFoundException {	
			
		Type type = new TypeToken<Map<String, SmapDevice>>() {}.getType();
		FileReader fr = new FileReader(device_map_file);
		deviceMap = gson.fromJson(fr, type);
		
		return deviceMap;
	}
	
	public static Map<String, List<String>> channelList(String host, String secretkey) 
			throws Exception {		
		
		if(!host.endsWith("/")) {
			host = host + "/";
		}
		
		try {

			Map<String, SmapDevice>  deviceMap = loadDeviceMap();
		
			if(deviceMap == null) {
				System.out.println("deviceMap is empty");
			}
			
			if(deviceMap != null) {
				
				Map<String, List<String>> channelMap = new HashMap<String, List<String>>();
				
				for(String uuid : deviceMap.keySet()) {			
					SmapDevice sd = (SmapDevice)deviceMap.get(uuid);
				
					List<String> channelList = channelMap.get(sd.Device) ;
					if(channelList == null) {
						channelList = new ArrayList<String>();
					}					
					channelList.add(sd.Sensor + "$" + sd.Channel);					
					channelMap.put(sd.Device, channelList);
				}
				return channelMap;
			}			
			//System.out.println(JsonUtil.json1.toJson(deviceList));
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}		
		return null;
	}

	private static String downloadSmapData(String hosturl, 
			String uuid, String starttime, String endtime ) throws Exception {
				
		if(!hosturl.contains("/")) {
			hosturl = hosturl + "/";
		}

		// http://energy.iiitd.edu.in:9102/backend/api/data/uuid/cc5c78a3-722a-5a54-9a99-c08d71de44e7?starttime=1411204320000&endtime=1411290720000&
		String url = hosturl + "backend/api/data/uuid/";
		url = url + uuid + "?starttime=" + starttime + "&endtime=" + endtime;
			
		System.out.println("requesting.. " + url);		
		
		WSRequest wsReq =  WS.url(url);
		
		HttpResponse response = wsReq.get();
		
		if (response.getStatus() != 200) {
			throw new Exception("Invalid response : " + response.getStatus()
					+ " " + response.getString());
		}		
		return response.getString();
	}	

	private static Map<DateTime, Double> parseSmapDataFormat(String smapData) {

		List<SmapDataFormat> smapDataFormat = null;
		
		Type type1 = new TypeToken<List<SmapDataFormat>>() {}.getType();
		//FileReader fr1 = new FileReader(Play.getFile("./conf/smapdata.json"));
		//smapDataFormat = SmapStreamList.gson.fromJson(fr1, type1);
		smapDataFormat = gson.fromJson(smapData, type1);
		
		Map<DateTime, Double> outData = new LinkedHashMap<DateTime, Double>();
		
		if(smapDataFormat.size() == 0 ) {
			return null;
		}
		
		SmapDataFormat sd = smapDataFormat.get(0);
		
		for(Double[] a : sd.Readings) {
			DateTime dt = new DateTime(a[0].longValue());
			outData.put(dt, a[1]);
			//System.out.println( a[0].longValue() + " " + a[1]);
		}

		return outData;
	}
	
	private static String getUUID(String device, String sensor, String channel) throws FileNotFoundException {
		
		Map<String, SmapDevice> deviceMap = loadDeviceMap();
		
		if(deviceMap == null) {
			return null;
		}
		
		for(String uuid : deviceMap.keySet()) {			
			SmapDevice sd = (SmapDevice)deviceMap.get(uuid);
			
			if(sd.Device.equals(device) && sd.Sensor.equals(sensor) 
					&& sd.Channel.equals(channel)) {
				return uuid;
			}
		}
		//String uuid = "cc5c78a3-722a-5a54-9a99-c08d71de44e7";
		return null;
	}

	public static Map<DateTime, Double> fetchData(String hosturl, String key, String device, 
			String sensor, String channel, String sdate, String edate) {
		
		DateTime start = new DateTime(sdate);
		DateTime end = new DateTime(edate);
		end = end.plusDays(1).minusMillis(1);

		String starttime = ""+start.getMillis();
		String endtime = ""+end.getMillis();
		
		try {
			String uuid = getUUID(device, sensor, channel);			
			if(uuid == null) return null;
			
			String smapData = downloadSmapData(hosturl, uuid, starttime, endtime);
			return parseSmapDataFormat(smapData);			
		} catch(Exception e) {
			return null;
		}
	}

}
