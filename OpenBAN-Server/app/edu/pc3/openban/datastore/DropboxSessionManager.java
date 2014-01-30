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
 * Name: DropboxSessionManager.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.datastore;

import java.util.HashMap;

import play.mvc.Scope;

import com.dropbox.core.DbxWebAuth;

import edu.pc3.openban.cache.CacheManager;

public class DropboxSessionManager {

	private static String DROPBOX_ACCESS_TOKEN = "DROPBOX-ACCESS-TOKEN:";

	private static HashMap<String, Object> sessionMap = new HashMap<String, Object>();

	public static void put(String key, Object obj) {
		sessionMap.put(key, obj);
	}

	public static Object get(String key) {
		return sessionMap.get(key);
	}

	public static void clear(String key) {
		sessionMap.remove(key);
	}

	public static class DbxWebAuthSerializable implements java.io.Serializable {
		public DbxWebAuth webAuth;

		public DbxWebAuthSerializable(DbxWebAuth web) {
			webAuth = web;
		}
	}

	public static String sessionKey(Scope.Session session) {
		return session.getAuthenticityToken() + "::" + session.getId() + "::"
				+ "DROPBOX-WEBAUTH";
	}

	public static void putDropboxWebAuth(Scope.Session session,
			DbxWebAuth webAuth) {
		DbxWebAuthSerializable wa = new DbxWebAuthSerializable(webAuth);
		sessionMap.put(sessionKey(session), wa); // maximum time - 1 month
	}

	public static DbxWebAuth getDropboxWebAuth(Scope.Session session) {
		DbxWebAuthSerializable wa = (DbxWebAuthSerializable) sessionMap
				.get(sessionKey(session));
		if (wa != null)
			return wa.webAuth;
		return null;
	}

	public static void putAccessToken(String userId, String accessToken) {
		CacheManager.set(DROPBOX_ACCESS_TOKEN + userId, accessToken);
	}

	public static String getAccessToken(String userId) {
		return (String) CacheManager.get(DROPBOX_ACCESS_TOKEN + userId);
	}

	public static void deleteAccessToken(String userId) {
		CacheManager.delete(DROPBOX_ACCESS_TOKEN + userId);
	}
}
