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
 * Name: UserProfileManager.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;

import controllers.RepoProfile;
import edu.pc3.openban.cache.CacheManager;
import edu.pc3.openban.data.DataSourceAdapter;
import edu.pc3.openban.datastore.DropboxDataStore;

public class UserProfileManager {
	
	private String userId;
	private static final String REPO_PROFILE = ":REPO_PROFILE"; 
	private static final String APP_INFO = ":APP_INFO";
	private static final String USERNAME = ":USERNAME";
	
	public UserProfileManager(String userId) {	
		this.userId = userId;
	}
	
	// load user profile from Dropbox
	// fetch the list of data repositories
	// fetch the list of datastreams	
	public void loadProfile() throws Exception {
		
		System.out.println(userId + " Loading repolist.. ");
		List<RepoProfile> repoProfiles = DropboxDataStore.getInstance(userId).getRepoList();
		System.out.println(userId + " Loading applist.. ");
		List<String> apps = DropboxDataStore.getInstance(userId).getAppList();
		
		System.out.println(userId + " Gettting user name.. ");
		String username = DropboxDataStore.getInstance(userId).getDisplayName();
		
		String key1 = userId + REPO_PROFILE;
		String key2 = userId + APP_INFO;
		String key3 = userId + USERNAME;
		
		//System.out.println("loadProfile apps " + apps);
		
		CacheManager.set(key1, repoProfiles);
		CacheManager.set(key2, apps);
		CacheManager.set(key3, username);
		//System.out.println("testing........................");
		//List<RepoProfile> xx = (List<RepoProfile>)CacheManager.get(key1);
	}
	
	public String getDisplayName() {
		String key = userId + USERNAME;
		return (String)CacheManager.get(key);
	}

	public RepoProfile getRepoProfile(String reponame) {
		String key = userId + REPO_PROFILE;
		List<RepoProfile> repoProfiles  = (List<RepoProfile>)CacheManager.get(key);
		
		for(RepoProfile rp : repoProfiles) {
			if(rp.getReponame().equals(reponame))
				return rp;
		}
		return null;
	}

	
	public List<RepoProfile> getRepoProfiles() {
		String key = userId + REPO_PROFILE;
		List<RepoProfile> repoProfiles  = (List<RepoProfile>)CacheManager.get(key);
		return repoProfiles;
	}

	public List<String> getAppNames() {
		String key = userId + APP_INFO;		
		return (List<String>)CacheManager.get(key);
	}

	public List<String> getRepoProfileNames() {
		
		List<RepoProfile> repoProfiles  = getRepoProfiles();
		
		List<String> repoNames = new ArrayList<String>();		
		if(repoProfiles != null ) {			
			for(RepoProfile p : repoProfiles) {
				repoNames.add(p.getReponame());
			}			
		}
		if(repoNames.size() == 0 ) {
			repoNames = null;
		}
		return repoNames;
	}

	public static class Node {
		public String label;
		public List<Node> children = null;
		
		public Node(String label) {
			this.label = label;
		}		
	}
	
	// edu.pc3.openban.model for jq tree
	// http://mbraak.github.io/jqTree/#node-functions-getdata
	public Node toTreeNode(RepoProfile repo) {
		
		Map<String, List<String>> dsMap = repo.getDatastreams();

		Node repoNode = new Node(repo.getReponame());
		repoNode.children = new ArrayList<Node>();
		
		if(dsMap == null) {
			return repoNode;
		}
		
		for(String key : dsMap.keySet()) {			
			Node node = new Node(key);
			node.children = new ArrayList<Node>();
			
			List<String> dsList = (List<String>) dsMap.get(key);				
			for(String ds:dsList) {				
				node.children.add(new Node(ds));
			}
			
			repoNode.children.add(node);
		}
			
		//String json = new GsonBuilder().setPrettyPrinting().create().toJson(repoNode);
		//System.out.println(json);
		
		return repoNode;
	}
	
	public String getDatastreamListAsJson() {

		List<RepoProfile> repoProfiles = getRepoProfiles();		
		List<Node> nodes = new ArrayList<Node>();
		
		// convert to tree edu.pc3.openban.model
		if(repoProfiles != null ) {
			for(RepoProfile p : repoProfiles) {
				System.out.println("processing " + p.getReponame());
				nodes.add(toTreeNode(p));
			}
		}
		
		String json = new GsonBuilder().create().toJson(nodes);
		//System.out.println(json);
		
		return json;
	}
	
	
	public String registerRepo(String reposource, String reponame, String repourl, String userid, String key) {
		
		System.out.println("fetching data stream list....");
		
		Map<String, List<String>> datastreams = null;
		
		try {
			datastreams = DataSourceAdapter.fetchDatastreamList(reposource, reponame, repourl, userid, key);			
		} catch(Exception e) {
			return e.getMessage();
		}
		
		RepoProfile repoProfile = new RepoProfile(reposource, reponame, repourl, userid, key);
		repoProfile.setDatastreams(datastreams);
		
		String res = DropboxDataStore.getInstance(userId).storeRepoProfile(repoProfile);
		
		return res;
	}	
}
