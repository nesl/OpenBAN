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
 * Name: ProcessService.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.analyze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import edu.pc3.openban.util.Const;
import edu.pc3.openban.util.JsonUtil;

public class ProcessService {

	private static ProcessService instance = null;

	private ProcessService() {
	}

	public static ProcessService getInstance() {
		if (instance == null) {
			instance = new ProcessService();
		}
		return instance;
	}
	
	
	public String learnModalOpenPy(String classifier, String jsonData) throws Exception {

		String baseUrl = "http://127.0.0.1:8080";
		String trainUrl = baseUrl + "/openban/svr/train/";
		//String trainUrl = baseUrl + "/R/pub/openban/svm.train/save";
		//String testUrl = baseUrl + "/R/pub/openban/svm.test/json";

		//String jsonStr = JsonUtil.json.toJson(jsonData);
		String jsonStr = jsonData;

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);
		
		System.out.println(jsonStr);

		System.out.println("\n\nInvoking training " + trainUrl);

		WSRequest wsr = WS.url(trainUrl).params(param).timeout("10min");
		
		
		//System.out.println("time out.,.." + wsr.timeout);

		HttpResponse trainRes = wsr.post();

		String resStr = trainRes.getString();
		System.out.println(resStr);
		
		
		File file = new File("response.html");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(resStr);
		bw.close();
				

		OpenCPU_Response ocRes = JsonUtil.fromJson(resStr,
				OpenCPU_Response.class);
		
		System.out.println("training ...done...");		
		return ocRes.object; 
	}

	public String executeModalOpenPy(String classifier, String modalId, String jsonData) throws Exception {

		String baseUrl = "http://127.0.0.1:8080";
		String testUrl = baseUrl + "/openban/svr/test/";
		
		//String jsonStr = JsonUtil.json.toJson(jsonData);
		String jsonStr = jsonData;


		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);
		param.put("model", modalId);

		System.out.println("\n\nInvoking Testing " + testUrl);
		HttpResponse testRes = WS.url(testUrl).params(param).timeout("10min").post();
		String resStr = testRes.getString();
		// System.out.println(resStr);
		
		File file = new File("test_response.html");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(resStr);
		bw.close();

		
		// writeTestData(resStr);
		System.out.println("testing..... done...");		
		return resStr; 
	}


	public String learnModal(String classifier, String jsonData) throws Exception {
		
		if(classifier.equals(Const.SVR)) {
			return learnModalOpenPy(classifier, jsonData);
		}

		String baseUrl = "http://128.97.93.32";
		String trainUrl = baseUrl + "/R/pub/openban/"+ classifier + ".train/save";
		//String trainUrl = baseUrl + "/R/pub/openban/svm.train/save";
		//String testUrl = baseUrl + "/R/pub/openban/svm.test/json";

		String jsonStr = JsonUtil.json.toJson(jsonData);

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);

		System.out.println("\n\nInvoking training " + trainUrl);

		WSRequest wsr = WS.url(trainUrl).params(param).timeout("10min");
		
		
		//System.out.println("time out.,.." + wsr.timeout);

		HttpResponse trainRes = wsr.post();

		String resStr = trainRes.getString();
		System.out.println(resStr);

		OpenCPU_Response ocRes = JsonUtil.fromJson(resStr,
				OpenCPU_Response.class);
		
		System.out.println("training ...done...");		
		return ocRes.object; 
	}

	public String getModalPararm(String modalId) throws Exception {
		
		String baseUrl = "http://128.97.93.32";
		String objectUrl = baseUrl + "/R/tmp/"+ modalId + "/encode";

		System.out.println("\n\nDownload modal params " + objectUrl);

		WSRequest wsr = WS.url(objectUrl).timeout("10min");
		HttpResponse trainRes = wsr.get();

		String resStr = trainRes.getString();
	//	System.out.println(resStr);

		System.out.println("extracting modal params ...done...");
		
		return resStr; 
	}


	
	public String executeModal(String classifier, String modalId, String jsonData) throws Exception {
		
		if(classifier.equals(Const.SVR)) {
			return executeModalOpenPy(classifier, modalId, jsonData);
		}

		String baseUrl = "http://128.97.93.32";
		String testUrl = baseUrl + "/R/pub/openban/"+ classifier + ".test/json";

		String jsonStr = JsonUtil.json.toJson(jsonData);
		
		File file = new File("execute_data.json");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(jsonStr);
		bw.close();

		

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);
		param.put("model", modalId);

		System.out.println("\n\nInvoking Testing " + testUrl);
		HttpResponse testRes = WS.url(testUrl).params(param).timeout("10min").post();
		String resStr = testRes.getString();
		System.out.println(resStr);

		// writeTestData(resStr);
		System.out.println("testing..... done...");		
		return resStr; 
	}

	public static class OpenCPU_Response {
		public String object;
	}
}