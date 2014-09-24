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
 * Name: OpenBanAppJob.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.scheduler;

import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;

import edu.pc3.openban.analyze.ExecutionService;

//@org.quartz.DisallowConcurrentExecution
@org.quartz.PersistJobDataAfterExecution
public class OpenBanAppJob implements InterruptableJob {
	
	//public static String APP_INFO = "APP_INFO";
	public static String APP_NAME = "APP_NAME";
	public static String USER_ID = "USER_ID";
	
	public static final Logger LOG = Logger.getLogger(AppScheduler.class.getName());

	public void execute(JobExecutionContext context) {
		
		
		JobKey jobKey = context.getJobDetail().getKey();
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		
		LOG.info(jobKey.toString() + " started " );
		
		
		// Get the params for event based tasklet
		String userId  = (String) dataMap.get(USER_ID);
		String appname  = (String) dataMap.get(APP_NAME);
		
		//TODO: validate the tasklet
		
		if(userId != null && appname != null) {
			LOG.info(jobKey.toString() + " parameters " + userId + " " + appname);	
		} else {
			LOG.info(jobKey.toString() + " invalid parameters " + userId + " " + appname);
			return;
		}
		
		try {			
			String res = new ExecutionService(userId, appname).executeAppInstance(jobKey.toString());
			LOG.info(jobKey.toString() + " result " + res );
		} catch (Exception e) {
			//System.out.println("Error while running lua script for *********** " + jobKey.getName());
			//LOG.info(jobKey.toString() + " execute " + e.fillInStackTrace() );
			e.printStackTrace();
		}		
		LOG.info(jobKey.toString() + " ended...." );
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		// TODO Auto-generated method stub
		System.out.println("Interrupted..");
	}

}
