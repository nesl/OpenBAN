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
 * Name: StatisticalFeatures.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.features;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;

import edu.pc3.openban.util.TimeSeries;
import edu.pc3.openban.util.TimeWindow;

public class StatisticalFeatures {

	private TimeSeries tsCount = new TimeSeries();
	private TimeSeries tsMinimum = new TimeSeries();
	private TimeSeries tsMaximum = new TimeSeries();
	private TimeSeries tsRange = new TimeSeries();

	private TimeSeries tsSum = new TimeSeries();
	private TimeSeries tsSumSquare = new TimeSeries();

	private TimeSeries tsMedian = new TimeSeries();
	private TimeSeries tsMean = new TimeSeries();
	private TimeSeries tsGeometricMean = new TimeSeries();
	private TimeSeries tsStandardDeviation = new TimeSeries();
	private TimeSeries tsVariance = new TimeSeries();
	
	private TimeSeries tsMeanDifference = new TimeSeries();

	private List<TimeWindow> timeWindowList;

	class StatisticsCalculator extends DescriptiveStatistics {
	}

	private StatisticalFeatures() {
	}

	public StatisticalFeatures(List<TimeWindow> twList) {
		timeWindowList = twList;
	}

	public TimeSeries getCountSeries() {
		return tsCount;
	}

	public TimeSeries getMinimumSeries() {
		return tsMinimum;
	}

	public TimeSeries getMaximumSeries() {
		return tsMaximum;
	}

	public TimeSeries getRangeSeries() {
		return tsRange;
	}

	public TimeSeries getSumSeries() {
		return tsSum;
	}

	public TimeSeries getSumSquareSeries() {
		return tsSumSquare;
	}

	public TimeSeries getMedianSeries() {
		return tsMedian;
	}
	
	public TimeSeries getMeanSeries() {
		return tsMean;
	}
	
	public TimeSeries getMeanDifferenceSeries() {
		return tsMeanDifference;
	}

	public TimeSeries getGeometricMeanSeries() {
		return tsGeometricMean;
	}

	public TimeSeries getStandardDeviationSeries() {
		return tsStandardDeviation;
	}

	public TimeSeries getVarianceSeries() {
		return tsVariance;
	}

	private static double prev = 0;
	private void updateSeries(DateTime time, StatisticsCalculator statsCalc) {

		if (statsCalc == null || statsCalc.getN() == 0) {
			return;
		}
		
		tsMeanDifference.put(time, Math.abs(statsCalc.getMean()-prev));
		prev = statsCalc.getMean();
		
		tsCount.put(time, new Double(statsCalc.getN()));
		tsMinimum.put(time, statsCalc.getMin());
		tsMaximum.put(time, statsCalc.getMax());
		tsRange.put(time, statsCalc.getMax()-statsCalc.getMin());

		tsSum.put(time, statsCalc.getSum());
		tsSumSquare.put(time, statsCalc.getSumsq());

		// calculate median
		double []sorted = statsCalc.getSortedValues();
		int mid = (int)statsCalc.getN()/2;		
		tsMedian.put(time, sorted[mid]);
		
		tsMean.put(time, statsCalc.getMean());
		tsGeometricMean.put(time, statsCalc.getGeometricMean());

		tsStandardDeviation.put(time, statsCalc.getStandardDeviation());
		tsVariance.put(time, statsCalc.getVariance());
		
	}
	
	public void compute() {

		DateTime windowTime;
		for (TimeWindow tw : this.timeWindowList) {

			// System.out.println("window size " + tw.getWindowSize());
			StatisticsCalculator statsCal = new StatisticsCalculator();
						
			// get the last time slot
			Object obj[] = tw.keySet().toArray();
			windowTime = (DateTime) obj[obj.length - 1];

			windowTime = TimeWindow
					.ceilDateTime(windowTime, tw.getWindowSize());

			for (Object value : tw.values()) {
				statsCal.addValue((Double) value);
			}
			updateSeries(windowTime, statsCal);
		}
	}

}
