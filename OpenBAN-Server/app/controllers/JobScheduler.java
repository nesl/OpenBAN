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
 * Name: JobScheduler.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package controllers;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Map;
import java.util.StringTokenizer;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class JobScheduler {

	public static Scheduler scheduler = null;
	public static JobExecutionEventListener jobEventListener = null;

	static {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			scheduler = sf.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		jobEventListener = new JobExecutionEventListener();
	}

	private static JobKey toJobKey(final String taskletid) {
		StringTokenizer stk = new StringTokenizer(taskletid, ".");
		String group = stk.nextToken();
		String name = stk.nextToken();
		JobKey jobKey = new JobKey(name, group);
		return jobKey;
	}

	public static void scheduleDownloadTask(String script,
			Map<String, String> param) {

		JobDetail downloadJob = newJob(DatastreamDownloaderJob.class)
		// .withIdentity("LuaScript" + id, "group1")
		// .usingJobData(DatastreamDownloaderJob.LUASCRIPT, luaScript)
		// .usingJobData(DatastreamDownloaderJob.TASKLETINFO, script)
				.build();

		JobDataMap luaJobDataMap = downloadJob.getJobDataMap();

		Trigger luaTrigger = newTrigger()
		// .withIdentity("LuaScriptTrigger" + id, "group1").startNow()
				.build();

		// JobExecutionEventListener listener = new JobExecutionEventListener();
		try {
			// scheduler.getListenerManager().addJobListener(listener,
			// keyEquals(jobKey("LuaScript"+id, "group1")));

			scheduler.scheduleJob(downloadJob, luaTrigger);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
