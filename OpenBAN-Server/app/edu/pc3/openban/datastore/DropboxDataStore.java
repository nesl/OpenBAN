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
 * Name: DropboxDataStore.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.datastore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;

import controllers.DropboxAuthentication;
import controllers.RepoProfile;
import edu.pc3.openban.util.Const;

public class DropboxDataStore {

	private static String REPO_PROFILE_DIR = "/RepoProfiles";
	private static String REPO_PROFILE_EXT = ".profile.json";

	private static String RECENT_APP = ".recent";

	private static String APPS_DIR = "/Apps";
	private static String APP_INFO_EXT = ".app.json";
	private static String APP_DATA_EXT = ".data.json";

	private static String DATASTORE_DIR = "/Datastore";
	private static String DATASTORE_FILE_EXT = ".csv";

	// store different files
	public static enum DataType {

		RAW("raw"), MODAL("model"), FEATURE("feature"), TRAINING("training"), EXECUTION(
				"execution");

		private DataType(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}
	}

	private DbxClient dropboxClient = null;

	public static DropboxDataStore getInstance(String userId) {

		String token = DropboxSessionManager.getAccessToken(userId);

		if (userId == null || token == null)
			return null;
		return new DropboxDataStore(token);
	}

	private DropboxDataStore(String token) {
		dropboxClient = new DbxClient(DropboxAuthentication.requestConfig,
				token, DropboxAuthentication.dropboxAppInfo.host);
	}

	public String getDisplayName() {

		String name = null;

		try {
			// dropboxClient.getAccountInfo();
			name = dropboxClient.getAccountInfo().displayName;
		} catch (DbxException e) {
			// e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return name;
	}

	private RepoProfile readRepoProfile(String filename) {

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dropboxClient.getFile(filename, null, os);
			String profile = os.toString();
			// System.out.println(filename + "  " + profile);
			return RepoProfile.fromJson(profile);
		} catch (Exception ex) {
			// ex.printStackTrace();
			System.err.println("Error in getFile(): " + ex.getMessage());
		}
		return null;
	}

	public List<String> getFileList(String dir, String fileExt) {

		DbxEntry.WithChildren dbxEntry;

		List<String> filenames = new ArrayList<String>();

		try {
			dbxEntry = dropboxClient.getMetadataWithChildren(dir);

			if (dbxEntry == null) {
				return null;
			}
			for (DbxEntry d : dbxEntry.children) {
				if (d.isFile()) {
					if (d.name.endsWith(fileExt)) {
						filenames.add(d.name.replace(fileExt, ""));
					}
				}
			}

		} catch (DbxException ex) {
			ex.printStackTrace();
			System.err.println("Error in getAccountInfo(): " + ex.getMessage());
			return null;
		}

		if (filenames.size() == 0)
			filenames = null;

		return filenames;
	}

	public List<String> getDirList(String dir) {

		DbxEntry.WithChildren dbxEntry;

		List<String> dirList = new ArrayList<String>();

		try {
			dbxEntry = dropboxClient.getMetadataWithChildren(dir);

			if (dbxEntry == null) {
				return null;
			}

			for (DbxEntry d : dbxEntry.children) {
				if (d.isFolder()) {
					dirList.add(d.name);
				}
			}

		} catch (DbxException ex) {
			ex.printStackTrace();
			System.err.println("Error in getAccountInfo(): " + ex.getMessage());
			return null;
		}

		if (dirList.size() == 0)
			dirList = null;

		return dirList;
	}

	public List<String> getAppList() {

		DbxEntry.WithChildren dbxEntry;

		List<String> apps = new ArrayList<String>();

		try {
			dbxEntry = dropboxClient.getMetadataWithChildren(APPS_DIR);

			if (dbxEntry == null) {
				return null;
			}

			// System.out.println("# items in /: " + dbxEntry.children.size());

			for (DbxEntry d : dbxEntry.children) {
				if (d.isFile()) {
					System.out.println("fetching .... " + d.name + "   "
							+ d.path);
					if (d.name.endsWith(APP_INFO_EXT)) {
						apps.add(d.name.replace(APP_INFO_EXT, ""));
					}
				}
			}

		} catch (DbxException ex) {
			ex.printStackTrace();
			System.err.println("Error in getAccountInfo(): " + ex.getMessage());
			return null;
		}

		if (apps.size() == 0)
			apps = null;

		return apps;
	}

	public List<RepoProfile> getRepoList() {

		System.out.println("inside getRepoList..");

		DbxEntry.WithChildren dbxEntry;
		List<RepoProfile> repoProfiles = new ArrayList<RepoProfile>();
		System.out.println("befoer ggf....................");

		RepoProfile dbxRepo = getDropboxRepoProfile();
		System.out.println("befoer if....................");
		if (dbxRepo != null) {
			System.out.println("inside....................");
			repoProfiles.add(0, dbxRepo);
		}

		try {
			dbxEntry = dropboxClient.getMetadataWithChildren(REPO_PROFILE_DIR);
			
			if (dbxEntry != null) {
				System.out.println("# items in /: " + dbxEntry.children.size());
				for (DbxEntry d : dbxEntry.children) {
					if (d.isFile()) {
						System.out.println("fetching .... " + d.name + "   "
								+ d.path);
						RepoProfile profile = readRepoProfile(d.path);
						if (profile != null) {
							System.out.println(" name      "
									+ profile.getReponame());
							repoProfiles.add(profile);
						}

					}
				}
			}
		} catch (DbxException ex) {
			ex.printStackTrace();
			System.err.println("Error in getAccountInfo(): " + ex.getMessage());
			//return null;
		}
		return repoProfiles;
	}

	public RepoProfile getDropboxRepoProfile() {

		RepoProfile dbxProfile = new RepoProfile(Const.DROPBOX, Const.DROPBOX, null, null, null);

		// check the datastream list in dropbox
		Map<String, List<String>> datastreamMap = new LinkedHashMap<String, List<String>>();

		List<String> dirList = getDirList(DATASTORE_DIR);
		
		if (dirList != null) {
			for (String dir : dirList) {
				System.out.println("looking at " + DATASTORE_DIR + "/" + dir);
				List<String> fileList = getFileList(DATASTORE_DIR + "/" + dir,
						DATASTORE_FILE_EXT);
				if (fileList != null) {
					datastreamMap.put(dir, fileList);
				}
			}
		}

		if (!datastreamMap.isEmpty()) {
			dbxProfile.setDatastreams(datastreamMap);
		}

		return dbxProfile;
	}

	public String storeRepoProfile(RepoProfile repoProfile) {

		String filename = REPO_PROFILE_DIR + "/" + repoProfile.getReponame()
				+ REPO_PROFILE_EXT;
		String profileJson = repoProfile.toJson();

		try {
			InputStream is = new ByteArrayInputStream(profileJson.getBytes());
			dropboxClient.uploadFile(filename, DbxWriteMode.update(null),
					is.available(), is);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dropboxClient.getFile(filename, null, os);
			System.out.println("File content " + os.toString());

		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			return parseError(e.getMessage());
		}

		return Const.SUCCESS;
	}

	public String storeAppData(String appName, String filename,
			String dataJson, DataType dataType) {

		String folderPath = APPS_DIR + "/" + appName;
		try {
			dropboxClient.createFolder(folderPath);
			folderPath = folderPath + "/" + dataType.toString();
			dropboxClient.createFolder(folderPath);

			String filePath = folderPath + "/" + filename + APP_DATA_EXT;
			System.out.println("Writing to " + filePath);

			InputStream is = new ByteArrayInputStream(dataJson.getBytes());
			dropboxClient.uploadFile(filePath, DbxWriteMode.update(null),
					is.available(), is);

		} catch (Exception e) {
			System.out.println("Exception : uploading file " + e.getMessage());
			return parseError(e.getMessage());
		}
		return "Success";
	}

	public String getModalInfo(String appName, String filename,
			DataType dataType) {

		String folderPath = APPS_DIR + "/" + appName;
		try {
			folderPath = folderPath + "/" + dataType.toString();

			System.out.println("Listing " + folderPath);

			List<String> files = getFileList(folderPath, APP_DATA_EXT);

			for (String fn : files) {
				if (fn.startsWith(filename)) {
					return fn;
				}
			}

		} catch (Exception e) {
			System.out.println("Exception : uploading file " + e.getMessage());
			return parseError(e.getMessage());
		}
		return null;
	}

	public String loadAppData(String appName, String filename, DataType dataType) {

		String folderPath = APPS_DIR + "/" + appName + "/"
				+ dataType.toString();
		String filePath = folderPath + "/" + filename + APP_DATA_EXT;
		System.out.println("Reading from  " + filePath);

		try {

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dropboxClient.getFile(filePath, null, os);

			// System.out.println("file data:" + os.toString());
			return os.toString();

		} catch (Exception e) {
			System.out.println("Exception : uploading file " + e.getMessage());
			return parseError(e.getMessage());
		}
	}

	public String parseError(String response) {

		// check https://www.dropbox.com/developers/core/docs
		// https://github.com/dropbox/dropbox-sdk-java/blob/master/src/com/dropbox/core/DbxRequestUtil.java
		if (response != null && response.contains("507")) {
			return "Dropbox quota limit exceeded";
		}

		return response;
	}

	public String storeAppInfo(String appName, String appInfoJson) {

		String filename = APPS_DIR + "/" + appName + APP_INFO_EXT;
		try {
			InputStream is = new ByteArrayInputStream(appInfoJson.getBytes());
			dropboxClient.uploadFile(filename, DbxWriteMode.update(null),
					is.available(), is);
			// ByteArrayOutputStream os = new ByteArrayOutputStream();
			// dropboxClient.getFile(filename, null, os);
			// System.out.println("File content " + os.toString());

		} catch (Exception e) {
			return parseError(e.getMessage());
		}

		return Const.SUCCESS;
	}

	public String setRecentApp(String appName) {

		String filename = APPS_DIR + "/" + RECENT_APP;
		try {
			InputStream is = new ByteArrayInputStream(appName.getBytes());
			dropboxClient.uploadFile(filename, DbxWriteMode.update(null),
					is.available(), is);
		} catch (Exception e) {
			return parseError(e.getMessage());
		}
		return Const.SUCCESS;
	}

	public String getRecentApp() {
		String filename = APPS_DIR + "/" + RECENT_APP;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dropboxClient.getFile(filename, null, os);
			return os.toString();
		} catch (Exception e) {
			return parseError(e.getMessage());
		}
	}

	public String loadAppInfo(String appName) {

		String filename = APPS_DIR + "/" + appName + APP_INFO_EXT;
		System.out.println("fetching " + filename);
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dropboxClient.getFile(filename, null, os);
			return os.toString();
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			return parseError(e.getMessage());
		}
	}

	public String fetchDatastream(String dsFolder, String dsFile) {

		String filePath = DATASTORE_DIR + "/" + dsFolder + "/" + dsFile
				+ DATASTORE_FILE_EXT;
		System.out.println("Reading from  " + filePath);

		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dropboxClient.getFile(filePath, null, os);

			// System.out.println("file data:" + os.toString());
			return os.toString();

		} catch (Exception e) {
			System.out.println("Exception : uploading file " + e.getMessage());
			return null;
		}
	}

}