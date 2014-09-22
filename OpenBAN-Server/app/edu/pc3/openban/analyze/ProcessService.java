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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.ArrayUtils;

import controllers.ModelRepo;

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

	public String learnModelOpenPy(String classifier, String jsonData)
			throws Exception {

		String baseUrl = "http://127.0.0.1:8080";
		String trainUrl = baseUrl + "/openban/svr/train/";
		// String trainUrl = baseUrl + "/R/pub/openban/svm.train/save";
		// String testUrl = baseUrl + "/R/pub/openban/svm.test/json";

		// String jsonStr = JsonUtil.json.toJson(jsonData);
		String jsonStr = jsonData;

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);

		System.out.println(jsonStr);

		System.out.println("\n\nInvoking training " + trainUrl);

		WSRequest wsr = WS.url(trainUrl).params(param).timeout("10min");

		// System.out.println("time out.,.." + wsr.timeout);

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

	public String executeModelOpenPy(String execUrl, String ModelId, String jsonData, String options) throws Exception {

		//String baseUrl = "http://127.0.0.1:8080";
		//String testUrl = baseUrl + "/openban/svr/test/";

		// String jsonStr = JsonUtil.json.toJson(jsonData);
		String jsonStr = jsonData;

		Map<String, String> header = new HashMap<String, String>();
		header.put("apikey", "ec1b7134-9646-4c95-830f-7b4f0e3f7be6");
		header.put("Content-Type", "application/x-www-form-urlencoded");
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);
		param.put("model", ModelId);
		param.put("options", options);
		
		System.out.println("\n\nInvoking Testing " + execUrl);
		HttpResponse testRes = WS.url(execUrl).headers(header) .params(param).timeout("10min")
				.post();
		String resStr = testRes.getString();
		System.out.println("Response : \n" + resStr);

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

	public String parseOpenCPUOutput(String output) {

		String obj = "";
		StringTokenizer st = new StringTokenizer(output, "/");
		st.nextToken();
		st.nextToken();
		obj = st.nextToken();

		return obj;
	}

	// parse the computing engine output for leanring and return the obj id
	public String handleTrainingOutput(String output, String server) {

		String obj = "";
		if (server.equalsIgnoreCase("OpenCPU")) {
			StringTokenizer st = new StringTokenizer(output, "/");
			st.nextToken();
			st.nextToken();
			obj = st.nextToken();
		}
		return obj;
	}

	public String learnModel(String classifier, String jsonData)
			throws Exception {

		if (classifier.equals(Const.SVR)) {
			return learnModelOpenPy(classifier, jsonData);
		}

		String trainUrl = null;
		ModelRepo.AlgorithmInfo algo = null;
		if (ModelRepo.alogrithms.containsKey(classifier)) {
			algo = ModelRepo.alogrithms.get(classifier);
			trainUrl = algo.train_url;
		} else {
			return "Unknown algorithm: " + classifier;
		}

		// String baseUrl = "http://128.97.93.32";
		// String trainUrl = baseUrl + "/R/pub/openban/"+ classifier +
		// ".train/save";
		// String trainUrl = baseUrl + "/R/pub/openban/svm.train/save";
		// String testUrl = baseUrl + "/R/pub/openban/svm.test/json";

		String jsonStr = JsonUtil.json.toJson(jsonData);

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("data", jsonStr);

		System.out.println("\n\nInvoking training " + trainUrl);

		WSRequest wsr = WS.url(trainUrl).params(param).timeout("10min");

		// System.out.println("time out.,.." + wsr.timeout);

		HttpResponse trainRes = wsr.post();

		String resStr = trainRes.getString();
		System.out.println(resStr);

		String model_obj = handleTrainingOutput(resStr, algo.server);
		// OpenCPU_Response ocRes = JsonUtil.fromJson(resStr,
		// OpenCPU_Response.class);

		// System.out.println("training ...done...");
		return model_obj;
	}

	public String getModelPararm(String ModelId) throws Exception {

		String baseUrl = "http://128.97.93.32";
		String objectUrl = baseUrl + "/R/tmp/" + ModelId + "/encode";

		System.out.println("\n\nDownload Model params " + objectUrl);

		WSRequest wsr = WS.url(objectUrl).timeout("10min");
		HttpResponse trainRes = wsr.get();

		String resStr = trainRes.getString();
		// System.out.println(resStr);

		System.out.println("extracting Model params ...done...");

		return resStr;
	}

	public static class ResultFormat {
		// public double GROUNDTRUTH[];
		public double predicted[];

	}

	// parse the computing engine output for leanring and return the obj id
	public ResultFormat handleExecutionOutput(String output, String server) {

		ResultFormat rf = new ResultFormat();

		List<Double> dataList = new ArrayList<Double>();
		String obj = "";
		if (server.equalsIgnoreCase(Const.OPENCPU)) {
			StringTokenizer st = new StringTokenizer(output);
			// skip the header
			st.nextToken();
			while (st.hasMoreTokens()) {
				dataList.add(new Double(Double.parseDouble(st.nextToken())));
			}
			Double[] ds = dataList.toArray(new Double[dataList.size()]);
			rf.predicted = ArrayUtils.toPrimitive(ds);
		} else if(server.equalsIgnoreCase(Const.OPENPY)) {
			try {
				rf = JsonUtil.fromJson(output, ResultFormat.class);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rf;
	}

	public String getOpenCPUObjectValue(String url, String objId) throws Exception {
		
		String baseUrl = "http://openban.iiitd.edu.in/ocpu/tmp/";
		String objectUrl = baseUrl + objId + "/R/.val/csv";

		System.out.println("\n\nDownload Model params " + objectUrl);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-type", "text/csv");
		
		WSRequest wsr = WS.url(objectUrl).headers(headers).timeout("10min");
		HttpResponse trainRes = wsr.get();

		String resStr = trainRes.getString();
		// System.out.println(resStr);

		return resStr;
		//System.out.println("extracting Model params ...done...");
	}
	
	public ResultFormat executeModel(String classifier, String ModelId,
			String jsonData, String options) throws Exception {

		
		if (classifier.equals(Const.SVR)) {
			// return executeModelOpenPy(classifier, ModelId, jsonData);
		}

		String execUrl = null;
		ModelRepo.AlgorithmInfo algo = null;
		if (ModelRepo.alogrithms.containsKey(classifier)) {
			algo = ModelRepo.alogrithms.get(classifier);
			execUrl = algo.exec_url;
		} else {
			// return "Unknown algorithm: " + classifier;
		}
		
		if(algo == null) {
			// do something
		}
		
		if(algo.server.equals(Const.OPENPY)) {
			String res = executeModelOpenPy(algo.exec_url, ModelId, jsonData, options);
			System.out.println("Response " + res);
			return handleExecutionOutput(res, algo.server);
		}
		
		 if(execUrl.endsWith("/")) { 
			execUrl = execUrl + "R/.val/csv"; 
		} else { 
			execUrl = execUrl + "/R/.val/csv"; 
		}
		 

		// String baseUrl = "http://128.97.93.32";
		// String testUrl = baseUrl + "/R/pub/openban/"+ classifier +
		// ".test/json";

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
		param.put("model", ModelId);
		
		System.out.println("\n\nInvoking execution " + execUrl);
		
		HttpResponse testRes = WS.url(execUrl).params(param).timeout("10min")
				.post();
		
		String resStr = testRes.getString();
		System.out.println(resStr);

		String obj = parseOpenCPUOutput(resStr);
		
		String data = getOpenCPUObjectValue(algo.exec_url, obj);
		
		// writeTestData(resStr);
		System.out.println("testing..... done...");

		return handleExecutionOutput(data, algo.server);
		// return resStr;
	}

	public static class OpenCPU_Response {
		public String object;
	}
}