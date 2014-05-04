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
 * Name: DatapointDowloader.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.data.adapter.xively;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class DatapointDowloader {

	public static String BASEURL = "http://api.xively.com/v2/"; // 1220873220/datastreams/web.json?start=2013-06-22T20:10:00.00Z&end=2013-06-22T22:30:00.00Z&interval=300";

	private static final int INTERVAL = 0;
	private static final int LIMIT = 1000;
	private static final String TIME_ZONE = "-7"; //"Arizona, Chihuahua, Mazatlan, Mountain Time (US & Canada)";
	
	private static Datastream fetchDatastream(String url,String key) throws Exception {

		Datastream xds = null;

		String jsonResponse = null;
		int attempts = 0;
		boolean isDone = false;
		
		while (!isDone) {
			
			System.out.println("\tRequesting " + url);
			
			try {
				jsonResponse = HTTPClient.get(url, key);
				System.out.println(jsonResponse.substring(jsonResponse.length()-100));
			} catch (Exception e) {
				jsonResponse = e.getMessage();
				e.printStackTrace();
			}

			if (jsonResponse.contains("too fast")) {				
				System.out.println("\t Too fast. Taking a break for 10 seconds..");
				Thread.sleep(10000);
				attempts++;
				System.out.println("\t" + attempts + " attempts. Retrying now.. ");
			} else {
				isDone = true;
			}

		}

		//System.out.println("response " + jsonResponse);
		xds = JsonUtil.fromJson(jsonResponse, Datastream.class);
		return xds;

		/*
		 * XivelyDatapoint pt1; for (XivelyDatapoint dp : xds.datapoints) {
		 * System.out.println(xds.getId() + xds.getMaxValue()); // DateTime dt =
		 * new DateTime("2004-12-13T21:39:45.618Z"); DateTime dt = new
		 * DateTime(dp.at, DateTimeZone.UTC);
		 * 
		 * dt = dt.plusHours(12); System.out.println(dt.toString() + "   " +
		 * dt.getZone().toString()); }
		 * 
		 * DateTime dt = new DateTime("2013-06-20T21:39:45.618Z");
		 * System.out.println(dt.toString() +"   " + dt);
		 * System.out.println(JsonUtil.json.toJson(xds));
		 */

	}

	private static String makeDatastreanFetchUrl(String feed, String datastream,
			DateTime start, DateTime end, String options) {
		String url = BASEURL + "feeds/" + feed + "/datastreams/" + datastream
				+ ".json?start=" + start + "&end=" + end + "&limit=" + LIMIT + 	options;
			//+ "&interval="
				//+ INTERVAL + 
				//+ "&timezone=" + TIME_ZONE;
				//+ "&function=average";
		return url;
	}

	private static Datastream fetchAllDataPoints(String key, String feed,
			String dataStreamId, String date, String options) {

		// https://api.xively.com/v2/feeds/1220873220/datastreams/web.json?start=2013-06-22T00:00:00.00Z&end=2013-06-22T02:00:00.00Z&interval=0

		DateTime currentDayStart = new DateTime(date); // 00:00:00.000
		DateTime currentDayEnd = currentDayStart.plusDays(1).minusMillis(1); // 23:59:59.999

		System.out.println("Range...." + currentDayStart + "    " + currentDayEnd);

		//TODO: dynamically change this according to interval
		// refer : https://xively.com/dev/docs/api/quick_reference/historical_data/
		int hour_limit = 5;
		// minimum resolution -1 //TODO: changes this accroding wiht the interval value
		
		DateTime start = currentDayStart;
		DateTime end = start.plusHours(hour_limit); 

		Datastream dataStream = null;

		try {
			boolean isDone = false;
			while (!isDone) {

				String url = makeDatastreanFetchUrl(feed, dataStreamId, start,
						end, options);
				Datastream dsTemp = fetchDatastream(url,key);

				// isDone = true;

				if (dsTemp != null
						&& (dsTemp.getDatapoints() == null || dsTemp
								.getDatapoints().size() == 0)) {

					// System.out.println("No data points");

					// if the end is end of the day - We are done!
					if (end.toString().equals(currentDayEnd.toString())) {
						// System.out.println("End of the day!");
						break;
					} else { // advance the start
						start = end.plusMillis(1);
						end = end.plusHours(hour_limit);
						// if end goes past the current day
						if (end.getHourOfDay() < start.getHourOfDay()) {
							end = currentDayEnd;
						}
						continue;
					}
				}

				if (dataStream == null) {
					dataStream = dsTemp;
				} else {
					dataStream.getDatapoints().addAll(dsTemp.getDatapoints());
				}

				Datapoint[] xdp = dsTemp.getDatapoints()
						.toArray(new Datapoint[0]);

				// System.out.println("Fetched " + xdp.length + "  Total: "
				// + dataStream.getDatapoints().size());

				for (Datapoint xdp1 : xdp) {
					// System.out.println(xdp1.at);
				}
				
				DateTime lastPointTime = xdp[xdp.length - 1].at;
				System.out.println("Last timestamp found : " + lastPointTime.plusMillis(1));
				
				//lastPointTime = lastPointTime.toDateTime(DateTimeZone.forOffsetHours(-7));
				start = lastPointTime.plusMillis(1);
				end = lastPointTime.plusHours(hour_limit);

				// if end goes past the current day
				if (end.getHourOfDay() < start.getHourOfDay()) {
					end = currentDayEnd;
				}
			}
		} catch (Exception e) {

			System.out.println(e.getMessage());
			//e.printStackTrace();
		}
		return dataStream;
	}

	/*public static Map<DateTime, Double> fetchDatapoints(String date, Xively xively) {

		int feed = xively.feed;
		String ds = xively.datastream;
		String key = xively.accesskey;

		System.out
				.println("\t Fetching Xively " + date + " " + feed + " " + ds);
		Datastream xds = fetchAllDataPoints(key, feed+"", ds,
				date);

		if (xds != null && xds.getDatapoints() != null) {
			return XivelyUtil.toMap(xds.getDatapoints());
		}

		return null;
	}
*/
	public static Map<DateTime, Double> fetchDatapoints(String key, String feed,
			String datastream, String start, String end, String options) {

		//Feed fd = getFeedbyTitle(feed);

		//int feedId = fd.getId();

		//System.out.println("........" + feedId);

		start = start.substring(0, 10);
		end = end.substring(0, 10);

		DateTime st = new DateTime(start);
		DateTime en = new DateTime(end);
		
		int days = Days.daysBetween(st, en).getDays();

		if (days > 7) {
		//	return "Too many days... maximum is 7";
		}

		Datastream xdsAll = new Datastream();

		int size;
		while (days-- >= 0) {
			Datastream xds = fetchAllDataPoints(key,
					feed, datastream, st.toString(), options);

			size = 0;
			if (xds != null && xds.datapoints != null) {
				xdsAll.datapoints.addAll(xds.datapoints);
				size = xds.datapoints.size();
			}

			System.out.println(st.toString() + " " + size
					+ " datapints. Total : " + xdsAll.datapoints.size());

			st = st.plusDays(1);
			
			 Runtime.getRuntime().gc();

		}
		
		return XivelyUtil.toMap(xdsAll.getDatapoints());
	}

}
