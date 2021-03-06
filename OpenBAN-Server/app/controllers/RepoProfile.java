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
 * Name: RepoProfile.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package controllers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;

public class RepoProfile implements Serializable{

	private String reposource;
	private String reponame;
	private String repourl;	
	private String userid;
	private String key;
	
	// list of data streams
	private Map<String, List<String>> datastreams;
	
	public RepoProfile(String reposource, String reponame, String repourl, String username, String key) {
		this.reposource = reposource;
		this.reponame = reponame;
		this.repourl = repourl;
		this.userid = username;
		this.key = key;
	}
	
	public Map<String, List<String>> getDatastreams() {
		return datastreams;
	}

	public void setDatastreams(Map<String, List<String>> datastreams) {
		this.datastreams = datastreams;
	}

	public String getReposource() {
		return reposource;
	}

	public void setReposource(String reposource) {
		this.reposource = reposource;
	}

	public String getRepourl() {
		return repourl;
	}

	public void setRepourl(String repourl) {
		this.repourl = repourl;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getReponame() {
		return reponame;
	}

	public void setReponame(String reponame) {
		this.reponame = reponame;
	}

	public String toJson() {
		return new GsonBuilder().create().toJson(this);
	}

	public static RepoProfile fromJson(String json) {

		RepoProfile reqObj = null;
		try {
			reqObj = new GsonBuilder().create().fromJson(json,
					RepoProfile.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reqObj;
	}

}
