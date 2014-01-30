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
 * Name: AppScheduler.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

public class AppScheduler {

	public static Scheduler scheduler = null;
	//public static final Logger LOG = LuaToJavaFunctionMapper.LOG;
	
	public static final Logger LOG = Logger.getLogger(AppScheduler.class.getName());
	
	static {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			scheduler = sf.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private static JobKey toJobKey(final String jobId) {
		StringTokenizer stk = new StringTokenizer(jobId, ".");
		String group = stk.nextToken();
		String name = stk.nextToken();
		JobKey jobKey = new JobKey(name, group);
		return jobKey;
	}

	private static boolean checkTaskletExists(final JobKey jobKey) {
		try {
			return scheduler.checkExists(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean checkTaskletExists(final String taskletid) {
		JobKey jobKey = toJobKey(taskletid);
		return checkTaskletExists(jobKey);
	}

	public static boolean cancelTasklet(final String taskletid) {
		JobKey jobKey = toJobKey(taskletid);
		
		try {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			scheduler.interrupt(jobKey);
			boolean status = scheduler.deleteJob(jobKey);
			if (status == true) {
			}
			return status;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static List<String> listAllTaskletsGroupWise(final String group) {

		List<String> jobKeyList = new ArrayList<String>();
		try {
			// enumerate each job in group
			GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(group);
			for (JobKey jobKey : scheduler.getJobKeys(groupMatcher)) {
				jobKeyList.add(jobKey.toString());
				System.out.println("Found job identified by: " + jobKey);
			}

		} catch (SchedulerException e) {
			e.printStackTrace();
			return null;
		}
		return jobKeyList;
	}

	public static List<JobDetail> getJobDetailList(List<String> jobKeyList) {
		List<JobDetail> jbDList = new ArrayList<JobDetail>();

		try {
			for (int i = 0; i < jobKeyList.size(); i++)
				jbDList.add(scheduler.getJobDetail(toJobKey(jobKeyList.get(i))));
		} catch (SchedulerException e) {
			e.printStackTrace();
			return null;
		}

		return jbDList;
	}

	private static boolean scheduleTasklet(final JobDetail job,
			final Trigger trigger) {
		try {
			return scheduler.scheduleJob(job, trigger) != null;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String scheduleTasklet(final String userId, final String appname) {
		
		JobKey jobKey = new JobKey(appname, userId);
		TriggerKey triggerKey = new TriggerKey(appname, userId);

		if (checkTaskletExists(jobKey)) {
			return "Already scheduled!";
		}

		JobDataMap jobDataMap = new JobDataMap();
		//jobDataMap.put(OpenBanAppJob.APP_INFO, app);
		jobDataMap.put(OpenBanAppJob.USER_ID, userId);
		jobDataMap.put(OpenBanAppJob.APP_NAME, appname);

		JobDetail jobDetail = newJob(OpenBanAppJob.class)
				.withIdentity(jobKey)
				// .usingJobData(LuaScriptTasklet.TASKLETINFO, tasklet)
				// .usingJobData(LuaScriptTasklet.LUASCRIPT, tasklet.execute)
				//.usingJobData("taskletname", tasklet.taskletname)
				//.usingJobData("desc", tasklet.desc)
				// .usingJobData("tasklet_type",
				// tasklet.tasklet_type.toString())
				.usingJobData(jobDataMap).build();
		
		Trigger trigger = newTrigger()
							.withIdentity(triggerKey)
							.withSchedule(cronSchedule("0 0/1 * * * ?"))
							.build();
		
		return scheduleTasklet(jobDetail, trigger) ? jobKey.toString() : null;
	}
}
