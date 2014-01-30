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
 * Name: TemporalFeatures.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.features;

import java.util.List;

import org.joda.time.DateTime;

import edu.pc3.openban.util.TimeSeries;
import edu.pc3.openban.util.TimeWindow;

public class TemporalFeatures {

	private TimeSeries tsMinuteOfTheDay = new TimeSeries();
	private TimeSeries tsMinuteOfTheHour = new TimeSeries();

	private TimeSeries tsHourOfTheDay = new TimeSeries();
	private TimeSeries tsDayOfTheWeek = new TimeSeries();
	private TimeSeries tsDayOfTheMonth = new TimeSeries();

	private List<TimeWindow> timeWindowList;

	private TemporalFeatures() {
	}

	public TemporalFeatures(List<TimeWindow> twList) {
		timeWindowList = twList;
	}

	public TimeSeries getMinuteOfTheHourSeries() {
		return tsMinuteOfTheHour;
	}

	public TimeSeries getMinuteOfTheDaySeries() {
		return tsMinuteOfTheDay;
	}

	public TimeSeries getHourOfTheDaySeries() {
		return tsHourOfTheDay;
	}

	public TimeSeries getDayOfTheWeekSeries() {
		return tsDayOfTheWeek;
	}

	public TimeSeries getDayOfTheMonthSeries() {
		return tsDayOfTheMonth;
	}

	private void updateSeries(DateTime time) {
		tsMinuteOfTheDay.put(time, new Double(time.minuteOfDay().get()));
		tsMinuteOfTheHour.put(time, new Double(time.minuteOfHour().get()));
		tsHourOfTheDay.put(time, new Double(time.hourOfDay().get()));
		tsDayOfTheWeek.put(time, new Double(time.dayOfWeek().get()));		
		tsDayOfTheMonth.put(time, new Double(time.dayOfMonth().get()));
	}

	public void compute() {

		DateTime windowTime;
		for (TimeWindow tw : this.timeWindowList) {

			// get the last time slot
			Object obj[] = tw.keySet().toArray();
			windowTime = (DateTime) obj[obj.length - 1];

			windowTime = TimeWindow
					.ceilDateTime(windowTime, tw.getWindowSize());

			updateSeries(windowTime);
		}
	}

}
