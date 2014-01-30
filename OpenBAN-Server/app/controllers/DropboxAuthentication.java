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
 * Name: DropboxAuthentication.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package controllers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import play.Logger;
import play.Play;
import play.mvc.Controller;

import com.dropbox.core.DbxAccountInfo;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxWebAuth.BadRequestException;
import com.dropbox.core.DbxWebAuth.BadStateException;
import com.dropbox.core.DbxWebAuth.CsrfException;
import com.dropbox.core.DbxWebAuth.NotApprovedException;
import com.dropbox.core.DbxWebAuth.ProviderException;
import com.dropbox.core.DbxWriteMode;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.http.StandardHttpRequestorNew;

import edu.pc3.openban.datastore.DropboxSessionManager;

public class DropboxAuthentication extends Controller {
	
	// app name
	public static String APP_NAME = "OpenBAN";
	
	// refer /routes file
	public static String SIGNIN_ENDPOINT = "/auth/dropbox/request";	
	public static String RESPONSE_ENDPOINT = "/auth/dropbox/response";
	
	//public static String dropbox_folder_list = "http://localhost:9000/dropbox/list";
	public static DbxAppInfo dropboxAppInfo = null;
	public static DbxRequestConfig requestConfig = null;
	
	
	static {		
		//System.out.println("reading.. dropbox.conf");
		String APP_KEY = Play.configuration.getProperty("dropbox.appkey");
		String APP_SECRET = Play.configuration.getProperty("dropbox.appsecret");
		
		//System.out.println( APP_KEY + APP_SECRET);		
		if(APP_KEY == null || APP_KEY.length() != 15 || 
				APP_SECRET == null || APP_SECRET.length() != 15 ) {			
			Logger.error("Invalid dropbox.appkey or dropbox.appsecret. Update dropbox.conf");
			System.exit(-1);
		}
		
		dropboxAppInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		
		// for google app engine
		HttpRequestor nonHttpsRequestor = StandardHttpRequestorNew.Instance;
		
		String userLocale = Locale.getDefault().toString();		
		requestConfig = new DbxRequestConfig(APP_NAME, userLocale);
		
	}

	public static void dropboxAuthRequest() {
		// ref :
		// https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/web-file-browser/src/com/dropbox/core/examples/web_file_browser/DropboxAuth.java

		HttpSession httpSession = new HttpSessionImpl();
		String sessionKey = "dropbox-auth-csrf-token";

		// session.getId();
		DbxSessionStore sessionStore = new DbxStandardSessionStore(httpSession,sessionKey);
		
		String dropbox_response_url = request.getBase().toString() + RESPONSE_ENDPOINT;
		
		System.out.println("DropboxAuthentication " + dropbox_response_url );
		
		DbxWebAuth dropboxWebAuth = null;
		
		// webAuth = new DbxWebAuthNoRedirect(requestConfig, appInfo);
		dropboxWebAuth = new DbxWebAuth(requestConfig, dropboxAppInfo, dropbox_response_url,
				sessionStore);
		
		System.out.println("Req Session : " + session.getAuthenticityToken()+ "  " + session.getId());
		
		String authorizeUrl = dropboxWebAuth.start();
		//System.out.println(authorizeUrl);
		
		DropboxSessionManager.putDropboxWebAuth(session, dropboxWebAuth);
		
		//DropboxSessionManager.put(session.getAuthenticityToken(), dropboxWebAuth);

		// System.out.println( "server domain " + request.domain );

		// prepare to receive the access token automatically
		// authorizeUrl = authorizeUrl.replace("response_type=code",
		// "response_type=token");
		// authorizeUrl =
		// authorizeUrl.concat("&redirect_uri="+dropbox_response_uri);

		System.out.println("Redirecting to " + authorizeUrl);
		redirect(authorizeUrl);

		// https://www.dropbox.com/1/oauth2/authorize?locale=en_US&client_id=o7c3gfu3g7r034s&response_type=code
		//String code = "V0EZ5D4UV9kAAAAAAAAAAT4S1Va6CL5HBFMMT-W9fyQ";

		// Map<String, String[]> param = new HashMap<String, String[]>();

		// param.put("", new String[]{"ss", "dd"});
		//render();
	}

	public static void dropboxAuthResponse(String code, String state, String error,
			String error_description) throws IOException, BadRequestException,
			BadStateException, CsrfException, NotApprovedException,
			ProviderException, Exception{

		//System.out.println(" reqeust url : " + request.get().url);
		//System.out.println(" reqeust path : " + request.path);
		//System.out.println(" reqeust body : " + request.body);
		// System.out.println(" reqeust action : " + request.get().action );
		//System.out.println(" reqeust path : " + request.getBase().toString());

		for (String s : request.get().args.keySet()) {
			System.out.println("dddd   " + s);
		}

		int size = request.body.available();
		byte buf[] = new byte[size];

		request.body.read(buf);

		//System.out.println(new String(buf));
		//System.out.println("code : " + code);
		//System.out.println("state : " + state);
		//System.out.println("error : " + error);
		//System.out.println("error_description : " + error_description);

		//System.out.println(new Gson().toJson(request.params.data));
		 
		//DbxAuthFinish  aa = new DbxAuthFinish("Dd", "Dd");

		//System.out.println("Res Session : " + session.getAuthenticityToken()+ "  " + session.getId());
		
		DbxWebAuth dropboxWebAuth = null;		
		//dropboxWebAuth = (DbxWebAuth)DropboxSessionManager.get(session.getAuthenticityToken());
		dropboxWebAuth = DropboxSessionManager.getDropboxWebAuth(session);
		
		if(dropboxWebAuth == null ) {
			System.out.println("dropboxAuthResponse   dropboxWebAuth not found!" );

			session.put(Application.FLASH_MSG, "Invalid Dropbox session! Please relogin");
			Application.index();
		}
		
		DbxAuthFinish authFinish = null;
		try {			
			authFinish = dropboxWebAuth.finish(request.params.data);			
		} 
		catch (NotApprovedException ex) {
			System.err.println("Error in NotApprovedException: " + ex.getMessage());			
			String error_msg = "Ooops! You denied to access your Dropbox!";			
			session.put(Application.FLASH_MSG, error_msg);
			Application.index();
		}
		catch (Exception ex) {
			System.err.println("Error in DbxWebAuth.start: " + ex.getMessage());
			session.put(Application.FLASH_MSG, "Error while authenticating.. Please relogin");
			Application.index();
		}
		
		// success
		System.out.println("Authorization complete.");
		System.out.println("- User ID: " + authFinish.userId);
		//System.out.println("- Access Token: " + authFinish.accessToken);
		
		session.put(Application.SESSION_KEY, authFinish.userId);
		DropboxSessionManager.putAccessToken(authFinish.userId, authFinish.accessToken);
		
		String tt = (String)DropboxSessionManager.getAccessToken(authFinish.userId);
		System.out.println("...............check " + tt);
		
		edu.pc3.openban.cache.CacheManager.set(authFinish.userId, authFinish.accessToken);
		String k = (String)edu.pc3.openban.cache.CacheManager.get(authFinish.userId);
		System.out.println("...............check " + k);
		
		//DbxAuthInfo authInfo = new DbxAuthInfo(authFinish.accessToken,
			//	dropboxAppInfo.host);
				
		// Create a DbxClient, which is what you use to make API calls.
		// String userLocale = Locale.getDefault().toString();
		// DbxRequestConfig requestConfig = new
		// DbxRequestConfig("examples-account-info", userLocale);

		//dropboxClient = new DbxClient(requestConfig, authInfo.accessToken,
			//	authInfo.host);

		//redirect(dropbox_folder_list);
		
		//new CacheMonitor(authFinish.userId).start();

		redirect("/home");
		//Application.signinSuccess();
	}
	
	
	public static void dbxFolders() throws DbxException, IOException {

		DbxClient dropboxClient = null;
		
		// Make the /account/info API call.
		DbxAccountInfo dbxAccountInfo;
		DbxEntry.WithChildren dbxEntry;
		try {
			dbxAccountInfo = dropboxClient.getAccountInfo();
			dbxEntry = dropboxClient.getMetadataWithChildren("/");

		} catch (DbxException ex) {
			ex.printStackTrace();
			System.err.println("Error in getAccountInfo(): " + ex.getMessage());
			System.exit(1);
			return;
		}
		
		System.out.println("User's account info: "
				+ dbxAccountInfo.toStringMultiline());

		System.out.println("# items in /: " + dbxEntry.children.size());

		for (DbxEntry d : dbxEntry.children) {
			System.out.println("Meta data: " + d.toStringMultiline());
		}

		String filename = "/hello.txt";
		String data = "hello file new data";

		InputStream is = new ByteArrayInputStream(data.getBytes());

		try {
			dropboxClient.uploadFile(filename, DbxWriteMode.update(null), is.available(), is);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			dropboxClient.getFile(filename, null, os);
			
			System.out.println("File context " + os.toString() );	

		} catch(Exception e) {
			
			System.out.println("Exception : " + e.getMessage());
		}
		

	}

	
}
