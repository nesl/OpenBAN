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
 * Name: DataFomatter.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import org.joda.time.DateTime;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import edu.pc3.openban.util.TimeSeries;

public class DataFomatter {

	
	private static CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] {
				null, // Timestamp
				null
                //new ParseDouble() // values 
        };        
        return processors;
	}

	public static class TimeSeriesFormat {
		public DateTime timestamp;
		public Double value;
		
		public DateTime getTimestamp() {
			return timestamp;
		}
		
		public void setTimestamp(String timestamp) {
			
			//System.out.println(timestamp);
			
			if(timestamp.length() == 10) {
				long sec = Integer.parseInt(timestamp);
				this.timestamp = new DateTime(sec*1000);				
				//System.out.println( timestamp + "  " + (sec * 1000) +  "  " +  this.timestamp.toString());				
			} else if(timestamp.length() == 13) {
				long sec = Integer.parseInt(timestamp);
				this.timestamp = new DateTime(sec);
			} else {
				this.timestamp = DateTime.parse(timestamp);	
			}
		}
		
		public Double getValue() {
			return value;
		}
		
		public void setValue(String val) {			
			double d;
			try {
				d = Double.parseDouble(val);				
			} catch(Exception e) {
				return;
			}

			this.value = d;
		}
	}

	public static TimeSeries toTimeSeries(String csvData, String from, String to) {
		
		String csv = "timestamp,value\n2013-07-01T00:00:00.000-07:00, -0.41582599";
		TimeSeries tsData = new TimeSeries();
		
		long fromDate = DateTime.parse(from).getMillis();		
		long toDate = DateTime.parse(to).plusDays(1).minusMillis(1). getMillis(); // end of the day
		
		//System.out.println(DateTime.parse(from).toString());
		//System.out.println(DateTime.parse(to).toString());
		
        try {
    		InputStream is = new ByteArrayInputStream(csvData.getBytes());
    		
    		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

    		ICsvBeanReader beanReader = null;
    		beanReader = new CsvBeanReader(br, CsvPreference.STANDARD_PREFERENCE);
                
                // the header elements are used to map the values to the bean (names must match)
                final String[] header = beanReader.getHeader(true);
                
                final CellProcessor[] processors = getProcessors();
                
                System.out.println(header[0] + header[1]);
                System.out.println(beanReader.getLineNumber() + "  " + beanReader.getUntokenizedRow());
                
                TimeSeriesFormat ts;
                long time;
                while( (ts = beanReader.read(TimeSeriesFormat.class, header, processors)) != null ) {
                	//System.out.println(ts.getTimestamp() + " --- " + ts.getValue());                	
                	time = ts.getTimestamp().getMillis();                	
                	if(time >= fromDate && time <= toDate) {
                		tsData.put(ts.getTimestamp(), ts.getValue());
                	}
                }
                
                System.out.println("#size : " + tsData.size());
            
        }catch(Exception e) {
        	e.printStackTrace();
        }

		return tsData;
	}
	
	
	public static String makeCSV(Map<String, Collection<Double>> dataMap) {

		if (dataMap == null || dataMap.entrySet().isEmpty()) {
			return null;
		}

		StringBuilder header = new StringBuilder();
		StringBuilder dataStr = new StringBuilder();

		int index = 0;
		int col_size = dataMap.keySet().size();
		int row_size = 0;

		Double[][] allData = new Double[col_size][];

		for (String key : dataMap.keySet()) {
			header.append(key);
			header.append(",");
			allData[index++] = dataMap.get(key).toArray(new Double[0]);
		}
		
		header.deleteCharAt(header.length()-1);
		
		header.append("\n");

		// get the maximum size
		for (Double[] d : allData) {
			if (d.length > row_size) {
				row_size = d.length;
			}
		}

		for (int r = 0; r < row_size; r++) {
			for (int c = 0; c < col_size; ++c) {
				if (r < allData[c].length) {
					dataStr.append(allData[c][r]);
				}
				dataStr.append(",");
			}
			dataStr.deleteCharAt(dataStr.length()-1);
			dataStr.append("\n");
		}
		return header.toString().concat(dataStr.toString());
	}

	public static String makeCSV1(Map<String, Collection<Double>> dataMap) {

		if (dataMap == null || dataMap.entrySet().isEmpty()) {
			return null;
		}

		StringBuilder header = new StringBuilder();
		StringBuilder dataStr = new StringBuilder();

		int index = 0;
		int col_size = dataMap.keySet().size();
		int row_size = 0;

		Double[][] allData = new Double[col_size][];

		for (String key : dataMap.keySet()) {
			header.append(key);
			header.append(",");
			allData[index++] = 	dataMap.get(key).toArray(new Double[0]);
		}
		
		header.deleteCharAt(header.length()-1);
		
		header.append("\n");

		// get the maximum size
		for (Double[] d : allData) {
			if (d.length > row_size) {
				row_size = d.length;
			}
		}

		for (int r = 0; r < row_size; r++) {
			for (int c = 0; c < col_size; ++c) {
				if (r < allData[c].length) {					
					//to convert timestamp
					if(allData[c][r].doubleValue()>1000000) {
						dataStr.append( new DateTime(allData[c][r].longValue()).toString());
					} else {
						dataStr.append(allData[c][r].intValue());	
					}
				}
				dataStr.append(",");
			}
			dataStr.deleteCharAt(dataStr.length()-1);
			dataStr.append("\n");
		}
		return header.toString().concat(dataStr.toString());
	}

	
}
