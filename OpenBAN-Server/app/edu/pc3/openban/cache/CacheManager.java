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
 * Name: CacheManager.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import play.cache.Cache;

public class CacheManager {

	//private static HashMap<String, Object> sessionMap = new HashMap<String, Object>();
    //public static final MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
    public static boolean isGoogleAppEngine = false;

	public static String getHashCode(final String message) {

		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			//throw e;
		}
		md.update(message.getBytes());

		byte bytes[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public static void init() {
		
		/*
		memCache.put("k", "v");
		String v = (String) memCache.get("k");
		
		if(v.equals("v")) {
			isGoogleAppEngine = true;
			System.out.println("Using MemcacheService at Google App Engine");
		} else {
			Cache.init();
			System.out.println("Using MemcacheService at localhost");
		}
		*/
		
		// TODO: use local memcached
		isGoogleAppEngine = false;
		
	}
	
	public static String verifyKey(String key) {		
		// Memcached does not support key containing specical chars
		// http://stackoverflow.com/questions/6891617/memcached-client-throw-java-lang-illegalargumentexception-key-contains-invalid
		if(key.contains(" ")) {
			key = getHashCode(key);
//			/System.out.println("************** CacheManager: new key " + key );			
		}
		return key;
	}
	
	public static Object get(String key) {
		//System.out.println("************** CacheManager: get key " + key );
		if(isGoogleAppEngine) {
			//return memCache.get(verifyKey(key));
		}
		return Cache.get(verifyKey(key));		
	}

	public static void delete(String key) {
		if(isGoogleAppEngine) {
			//memCache.delete(verifyKey(key));
			return;
		}
		Cache.delete(verifyKey(key));
	}
	
	public static void set(String key, Object obj) {
		//System.out.println("************** CacheManager: set key " + key );
		if(isGoogleAppEngine) {
			//memCache.put(verifyKey(key),obj);
			return;
		}
		Cache.set(verifyKey(key), obj);
	}

}
