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
 * Name: FeatureManager.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.features;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;
import edu.pc3.openban.util.TimeSeries;
import edu.pc3.openban.util.TimeWindow;

public class FeatureManager {

	private static PythonInterpreter interp = new PythonInterpreter();

	// http://stackoverflow.com/questions/471000/jython-and-python-modules
	static {
		interp = new PythonInterpreter(null, new PySystemState());

		PySystemState sys = Py.getSystemState();

		System.out.println(sys.path);
		// sys.path.append(new PyString(rootPath));
		sys.path.append(new PyString("/usr/lib/python2.7"));
		sys.path.append(new PyString("/usr/lib/python2.7/dist-packages"));
		sys.path.append(new PyString("/usr/local/lib/python2.7/dist-packages"));

		// sys.path.remove(0);
		// sys.path.remove(0);
		// sys.path.remove(0);
		System.out.println(sys.path);
	}

	public static TimeSeries computeFeature1(TimeSeries tsData, String feature,
			int window_size, String from, String to) {
		
		TimeSeries featureData = new TimeSeries();
		
		DateTime fromDt = new DateTime(from);
		DateTime toDt = new DateTime(to);
		toDt = toDt.plusDays(1).minusMillis(1);
		
		System.out.println("from datte " + fromDt.toString());
		System.out.println("tooo datte " + toDt.toString());
		
		while(!fromDt.isAfter(toDt)){			
			if(fromDt.getHourOfDay() >= 6 && fromDt.getHourOfDay() < 22){				
				Double val = new Double(0);
//				/System.out.println(fromDt.toString() + "  " + val);
				featureData.put(fromDt, val);
			}
			fromDt = fromDt.plusMinutes(1);
		}
		
		// over wirte the data from the original data
		for(DateTime dt : tsData.keySet()) {
			Double val = tsData.get(dt);			
			if(dt.getHourOfDay() >= 6 && dt.getHourOfDay() < 7){
				//System.out.println(dt.toString() + "  " + val);			
				if(val.doubleValue()>0) {				
					dt = dt.withSecondOfMinute(0).withMillisOfSecond(0);
					featureData.put(dt, val);
					//System.out.println("...." + dt.toString() + "  " + val);
				}
			}
		}		
		return featureData;
	}
	
	public static TimeSeries computeFeature(TimeSeries tsData, String feature,
			int window_size) {

		List<TimeWindow> tw = TimeWindow.split(tsData, window_size);
		if (tw == null || tw.isEmpty()) {
			return null;
		}

		try {
			File file = new File("timewindow.json");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(JsonUtil.json1.toJson(tw));
			bw.close();
		}catch(Exception e) {
			
		}
		
		
		
		TimeWindow tt;

		System.out.println("feature " + feature);
		System.out.println("#window_size " + window_size);
		System.out.println("#data " + tsData.size());
		System.out.println("#window " + tw.size());

		StatisticalFeatures sf = new StatisticalFeatures(tw);
		sf.compute();

		TimeSeries featureData = null;

		if (feature.equals(Const.COUNT)) {
			featureData = sf.getCountSeries();
			System.out.println("counting.....................");
		}
		if (feature.equals(Const.MINIMUM)) {
			featureData = sf.getMinimumSeries();
		}
		if (feature.equals(Const.MAXIMUM)) {
			featureData = sf.getMaximumSeries();
		}
		if (feature.equals(Const.RANGE)) {
			featureData = sf.getRangeSeries();
		}

		if (feature.equals(Const.SUM)) {
			featureData = sf.getSumSeries();
		}
		if (feature.equals(Const.SUMSQUARE)) {
			featureData = sf.getSumSquareSeries();
		}
		if (feature.equals(Const.MEDIAN)) {
			featureData = sf.getMedianSeries();
		}
		if (feature.equals(Const.MEAN)) {
			featureData = sf.getMeanSeries();
		}		
		if (feature.equals(Const.MEAN_DIFFERENCE)) {
			featureData = sf.getMeanDifferenceSeries();
		}
		if (feature.equals(Const.GEOMETRIC_MEAN)) {
			featureData = sf.getGeometricMeanSeries();
		}
		if (feature.equals(Const.STANDARD_DEVIATION)) {
			featureData = sf.getStandardDeviationSeries();
		}
		if (feature.equals(Const.VARIANCE)) {
			featureData = sf.getStandardDeviationSeries();
		}

		TemporalFeatures tf = new TemporalFeatures(tw);
		tf.compute();

		if (feature.equals(Const.MIN_OF_THE_HOUR)) {
			featureData = tf.getMinuteOfTheHourSeries();
		}
		if (feature.equals(Const.MIN_OF_THE_DAY)) {
			featureData = tf.getMinuteOfTheDaySeries();
		}
		if (feature.equals(Const.HOUR_OF_THE_DAY)) {
			featureData = tf.getHourOfTheDaySeries();
		}
		if (feature.equals(Const.DAY_OF_THE_WEEK)) {
			featureData = tf.getDayOfTheWeekSeries();
		}
		if (feature.equals(Const.DAY_OF_THE_MONTH)) {
			featureData = tf.getDayOfTheMonthSeries();
		}
		if (feature.equals(Const.EPOCH_IN_MILLIS)) {
			featureData = tf.getEpochMillisSeries();
		}

		if (featureData == null) {
			featureData = tsData;
		}

		return featureData;
	}

	// var feature_sample_intput =
	// "2013-12-01T00:00:00.000-08:00, 1.0\n" +
	// "2013-12-01T00:00:01.000-08:00, 2.0\n" +
	// "2013-12-01T00:00:02.000-08:00, 3.0";

	public static Map<DateTime, Double> toMap(String input) {

		Map<DateTime, Double> inputMap = new HashMap<DateTime, Double>();
		
		StringTokenizer st = new StringTokenizer(input, ", \n");
		while (st.hasMoreTokens()) {
			String sdt = st.nextToken();
			String sva = st.nextToken();

			DateTime dt = new DateTime(sdt);
			Double va = Double.parseDouble(sva);

			inputMap.put(dt, va);
		}

		return inputMap;
	}


	public static String testNewFeature(String jsonData) {

		try {
			FeatureObjFormat fo = FeatureObjFormat.toObj(jsonData);
			return "" + FeatureComputer.runScript(fo.script, toMap(fo.input));
		} catch (PyException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			String err = baos.toString();
			return err;
		}
		catch (Exception e) {
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//PrintStream ps = new PrintStream(baos);
			//e.printStackTrace(ps);
			String err = e.getMessage();
			return err;
		}
	}

}
