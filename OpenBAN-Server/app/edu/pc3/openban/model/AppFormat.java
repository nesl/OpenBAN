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
 * Name: AppFormat.java
 * Project: OpenBAN-Server
 * Version: 1.0
 * Date: 2013-08-15
 * Authors: Pandarasamy Arjunan
 */
package edu.pc3.openban.model;

import java.util.List;

public class AppFormat {

	public class Node {
		public String name;
		public List<Node> children;
	}		

	// its the edu.pc3.openban.model for jqTree http://mbraak.github.io/jqTree/#tutorial
	public class DatastreamTree {		
		public String name;
		public int id;
		public List<Node> children;		
	}
	
	public class Aggregate {
		public String from_date;
		public String to_date;
		public List<DatastreamTree> training;
		public List<DatastreamTree> groundtruth;
	}

	public class Analyze {
		public String feature_window_size;
		public List<DatastreamTree> features;		
		public String classifier;
		public String modelid;
		public String options;
	}

	public class Act {
		public String from_date;
		public String to_date;		
		public String time_duration;
		public String time_unit;
		public List<DatastreamTree> consumers;		

	}
		
	public String appname;
	public Aggregate aggregate;
	public Analyze analyze;
	public Act act;
}
