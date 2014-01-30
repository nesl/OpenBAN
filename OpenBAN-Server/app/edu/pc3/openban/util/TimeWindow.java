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
 * Name: TimeWindow.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class TimeWindow extends TimeSeries {

	// in seconds
	private int windowSize = 0;

	public TimeWindow(int ws) {
		windowSize = ws;
	}

	public int getWindowSize() {
		return windowSize;
	}

	// windowSize in seconds -
	// must divide 86400 without remainder.
	// to round up to the whole day
	public static List<TimeWindow> split(TimeSeries series, int windowSize) {

		long millis;
		long curSlot, prevSlot, bucket;
		long windowSizeInMillis;

		if (series == null || series.isEmpty() || windowSize < 1) {
			return null;
		}

		// TODO: error handling
		if (86400 % windowSize != 0) {
			return null;
		}

		List<TimeWindow> twList = new ArrayList<TimeWindow>();
		TimeWindow twTemp = new TimeWindow(windowSize);

		windowSizeInMillis = windowSize * 1000; // convert into milliseconds

		//TODO: some problem with grouping??
		prevSlot = 0;
		for (DateTime time : series.keySet()) {

			millis = time.getMillisOfDay();
			curSlot = (int) millis / windowSizeInMillis;

			bucket = millis % windowSizeInMillis;

			// put the margin time in to the previous slot
			if ( bucket == 0 && curSlot == bucket ) {
				curSlot = prevSlot;
			}

			if (curSlot != prevSlot) { // next slot
				if (!twTemp.isEmpty()) {
					twList.add(twTemp);
				}
				twTemp = new TimeWindow(windowSize);
				prevSlot = curSlot;
			}
			twTemp.put(time, series.get(time));
		}

		twList.add(twTemp);

		return twList;
	}

	public static DateTime ceilDateTime(DateTime time, int windowSize) {

		if (windowSize == 0)
			return time;

		// if(86400 % windowSize != 0 ) return time;

		int seconds, remainder, secondsToAdd;
		long millis = time.getMillis();

		seconds = time.getSecondOfDay();
		remainder = seconds % windowSize;

		// if already ceiled up
		if (remainder == 0 && millis % 1000 == 0) {
			secondsToAdd = 0;
		} else {
			secondsToAdd = windowSize - remainder;
		}

		time = time.millisOfSecond().setCopy(0); // remove millis
		time = time.plusSeconds(secondsToAdd);

		return time;
	}

}
