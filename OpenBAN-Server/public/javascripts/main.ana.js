$(function() {

	// TODO: layout initialization is done at main.js
	// $("#analyze_container").layout(});

	$(document).on("click", "#analyze_add_feature", function(e) {
		
  	  	var $tree = $('#analyze_features_tree');
		var node = $tree.tree('getSelectedNode');
		
		var validNode = false;
		if(node) {
			level = node.getLevel();
			if(level == 3) {
				validNode = true;
			}
		}
		
		if(!validNode) {
	  		FlashAlert.show("Select a datastrem below and then click on Add feature");
	  		return;
	  	}
	  	
		$.getJSON(
			    '/feature/list',
			    function(data) {
			    	
			    	var html = "";
			    	$.each(data, function(index,item) {
			    		//alert(index  + " " + item);
			    		
			            html = html + '<optgroup label="' + index + '">';
			            
				    	for (var i = 0; i < item.length; i++) {
				            //alert(item[i]);
				    		html = html + '<option value="' + item[i] + '">' + item[i] + '</option>';
				        }
				    	html = html + '</optgroup>';
			    	});
			    	
			    	
			    	//jsondata = JSON.stringify(data);
			    	//alert(jsondata);
			    	
			    	//alert(html);
			    	$("#feature_names").html(html);
			    	$("#feature_names").multiselect({
			    			   header: "Select feature names"
			    	});
			    	//$("#feature_names").multiselect("open");
			    	$("#select-featurename-dialog").dialog("open");
			    }
			);
		
	});

	$(document).on("click", "#analyze_remove_feature", function(e) {
			
	  	  	var $tree = $('#analyze_features_tree');
			var node = $tree.tree('getSelectedNode');
			
			var validNode = false;
			if(node) {
				level = node.getLevel();
				if(level == 4) { // leaf node - feature name
					validNode = true;
				}
			}
			
			if(validNode) {
				FlashAlert.show("You removed feature " + node.name + " from " + node.parent.name);
				$tree.tree("removeNode", node);
			} else {
				FlashAlert.show("Select a feature name below and then click on Remove feature");
			}
	});

	$(document).on("click", "#analyze_train_the_model", function(e) {
		
		var classifier_name = $('#classifier_name').val();

		//TODO: all the validations..
		// and save the project..
		msg = "Learning  model " + classifier_name + "... Please wait. This may take several minutes";
		blockUImsg(msg);
		
		var appname = $.trim($("#current_app_name").text());
		var parm = "appname="+appname;

		$.ajax({
            type: 'POST',              
            url: '/train',
    		cache: false,
            data: parm
		})
		.done( function (response) { 			
            	if(response == "Success") {
            		FlashAlert.show("Learnt modal " + classifier_name + " successfully!");
            	} else {
            		bootbox.alert("Error while learning modal " + classifier_name + ": " + response);
            	}
		})
		.always(function() { 
			$.unblockUI(); 
		})
		.fail(function(jqxhr, textStatus, error) { 
			bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
		});

	});
	
	$('#analyze_features_tree').bind(
		    'tree.click',
		    function(event) {
		        var node = event.node;
		        var level = node.getLevel();
		        if(level == 4) {
		        	//FlashAlert.show("you clicked on a feature " + node.name);
		        	updateAnalyzeTable(node.parent.parent.name, node.parent.name, node.name);
		        }
		    }
	);

	var feature_sample_intput = 
			"2013-12-01T00:00:00.000-08:00, 1.0\n" +
			"2013-12-01T00:00:01.000-08:00, 2.0\n" +
			"2013-12-01T00:00:02.000-08:00, 3.0";
    
	$( "#add-featurename-dialog" ).dialog({
	    autoOpen: false,
	    modal: true,
	    resizable: false,
	    height: 'auto',
	    width: 500,
	    buttons: {
	      "Test": function() {	
	    	  
	    	var featureName = $('#newFeatureName').val();
	  		var script = $('#featureCode').val();
	  		var input = $('#featureSampleInput').val();
	  		
	  		var featureData = new Object(); //declare object
	  		featureData.script = script;
	  		featureData.input = input;

	  		msg = "Testing feature... " + featureName;
			blockUImsg(msg);
			
			jsondata = JSON.stringify(featureData);
			alert(jsondata);

			$.ajax({
	            type: 'POST',              
	            url: '/feature/test',
	    		cache: false,
	            data: jsondata
			})
			.done( function (response) {	
				alert(response);
				$('#featureResult').text(response);            
			})
			.always(function() { 
				$.unblockUI();
			})
			.fail(function(jqxhr, textStatus, error) { 
				bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
			});          	
	      },
	      "Save": function() {
	    	  $( this ).dialog( "close" );          	
	      },
	      Cancel: function() {
	        $( this ).dialog( "close" );
	      }
	    },
	    close: function() {
	    }	    
	});
	
	$( "#select-featurename-dialog" ).dialog({
	    autoOpen: false,
	    modal: true,
	    resizable: false,
	    height: 'auto',
	    width: 300,
	    buttons: {
	    	"Add new feature": function() {
	    		$( this ).dialog( "close" );
	    		$("#featureSampleInput").val(feature_sample_intput);
	    		$("#add-featurename-dialog").dialog("open");
	    	},
	      "Add": function() {

	    	  var $selected = $("#feature_names").multiselect("getChecked");	    	  
	    	  var features = $selected.map(function () { 
	    		  return this.value; }).get();

	    	  var $ftree = $('#analyze_features_tree');
	    	  var froot = $ftree.tree('getNodeById', 1);	    	  
	    	  froot.name = "Features"; // update root name
	    	  
	    	  // currently selected node..
	    	  var fds_node = $ftree.tree('getSelectedNode');
	    	  
        	  var duplicate_features = [];
	    	  //alert(features.length);	    	  
    		  // check duplicate feature names
        	  for (var i = 0; i < features.length; i++) {	    		  
        		  var is_duplicate = false;
        		  // alert(fds_node.name + "--" + fds_node.children + "--" + fds_node.children.length);
        		  for (var j=0; j < fds_node.children.length; j++) {	        		  
	          		  if(fds_node.children[j].name == features[i]) {
	          			is_duplicate = true;	          			
	        			break;
	        		  }
	        	  }
	        	  
	    		  if(is_duplicate) {
	    			  duplicate_features.push(features[i]);  
	    		  } else {
	    			  $ftree.tree( 'appendNode', { label: features[i] }, fds_node );  
	    		  }
	    	  }
        	  
        	  //alert(duplicate_features.length);
        	  if(duplicate_features.length > 0) {
        			FlashAlert.show("You had already selected these features: " + duplicate_features);
        	  } else {
            	  var msg = "Feature names " + features + " added to " + fds_node.name;
            	  FlashAlert.show(msg);        		  
        	  }
	    	  
	    	  $( this ).dialog( "close" );          	
	      },
	      Cancel: function() {
	        $( this ).dialog( "close" );
	      }
	    },
	    close: function() {
	    }
    });
	
	
}); // jquery

function updateAnalyzeTable(service,datastream,feature) {
	
	var from_date = $('#aggregate_from_datepicker');
	var to_date = $('#aggregate_to_datepicker');
	var window_size = $.trim($('#feature_window_size').val());
	
	var from_date = $('#aggregate_from_datepicker');
	var to_date = $('#aggregate_to_datepicker');

	// TODO: validate the date range
	
	if(!from_date.val() || !to_date.val()) {
		FlashAlert.show('Select valid aggregation date range to display the data points');
		return;
	}
	
	var d1 = new Date(from_date.val());
	var d2 = new Date(to_date.val());
	
	//alert(d1+d2);
	
	if(isNaN(d1.getTime()) || isNaN(d2.getTime())) {
		FlashAlert.show('Select valid aggregation date range to display the data points');
		return;
	}
	
	
	i_window_size = parseInt(window_size, 10);
	//alert(i_window_size);
	
	if(isNaN(i_window_size) || i_window_size <= 0 || i_window_size >3600) {
		FlashAlert.show('Invalid feature window size. Valid range is 1-3600');
		return;		
	}	
	$('#feature_window_size').val(i_window_size);
	
	//$('#smwizard').smartWizard('showMessage',null);
	//alert(from_date.val() + " " + to_date.val());

	var appname = $.trim($("#current_app_name").text());
	
	var param = "?appname="+appname + "&service=" + service+"&datastream="+datastream 
				+ "&from=" + from_date.val() + "&to="+to_date.val()			
				+ "&feature=" + feature + "&window_size="+i_window_size;		
				
	var url = "/data/json2" + param;
	
	var dataPoints = null;
	
	msg = "Please wait while fetching feature " + feature + " for " + datastream 
		 + "...\nThis may take several minutes!"
		 
	blockUImsg(msg);		
		
	var oTable = $('#analyze_container_table').dataTable( {
		"bDestroy": true,
		"bProcessing": true,
        "bServerSide": true,			
        "bFilter": false,
        "bSort": false,
        //http://www.datatables.net/examples/basic_init/language.html
        //http://stackoverflow.com/questions/10630853/change-values-of-select-box-of-show-10-entries-of-jquery-datatable
        "aLengthMenu": [[10, 30, 60, 120, 300, 600, 900, 1440, 1800, 2880], [10, 30, 60, 120, 300, 600, 900, 1440, 1800, 2880]],
        "sPaginationType": "full_numbers",
        // http://datatables.net/forums/discussion/510/showing-the-paging-controls-at-the-top-and-bottom-of-the-datatable/p1
        "sDom": '<"top"lfip>rt<"bottom"ip<"clear">',
	//	"bDeferRender": true,
		"oLanguage": {
	           "sProcessing": "<h3>Fetching datapoints...</h3>"
	         },
		"fnDrawCallback": function( oSettings ) {
			updateAnalyzeHighCharts();			      
		    },
		"sAjaxSource": url,
		//"aaData": dataPoints,
		"fnServerData": function ( sSource, aoData, fnCallback ) {				
			
			$.getJSON( sSource, aoData, function (json) {
				//alert( JSON.stringify(json.aaData));
				var points = json.aaData;
				
				var offset_mins = new Date().getTimezoneOffset();
				console.log("ofset mins " + offset_mins);
				var offset_ms = offset_mins * 60000; // #ms elapsed since epoch
				
	        	for (var i=0; i < points.length; i++) {
	        		//console.log(points[i][0]);
	        		var t1 = new Date(points[i][0]).getTime();
	        		var t2 = t1 - offset_ms;	        		
	        		points[i][0] = new Date(t2).getTime();
	        		//console.log(points[i][0]);
	        	}  
				//var jsonObj = jQuery.parseJSON( json );

				fnCallback(json);
			})
			.always(function() { 
					$.unblockUI(); 
			})
			.fail(function(jqxhr, textStatus, error) {
				if(jqxhr.status == 200) {
					FlashAlert.show('No datapoints found!');
				} else {
					bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
				}
			});
		} //fnServerData 
	} );
	
}

function updateAnalyzeHighCharts() {
	
	//alert("updating charts...");
	
    $('#analyze_container_plot').highcharts({
        data: {
            table: document.getElementById('analyze_container_table')
        },
        chart: {
            zoomType: 'x',
            type: 'spline'
        },
        title: {
            text: ''
        },
        xAxis: {
//        	tickInterval: 10,
        	type: 'datetime',
            title: {
                text: 'Time'
            },
            tickWidth: 5
        },
        yAxis: {
            //allowDecimals: false,
            title: {
                text: 'Value'
            }
        },
        legend: {enabled : true},
        plotOptions: {
            spline: {
                lineWidth: 2,
                states: {
                    hover: {
                        lineWidth: 3
                    }
                },
                marker: {
                    enabled: false
                }
                //pointInterval: 3600000, // one hour
                //pointStart: Date.UTC(2009, 9, 6, 0, 0, 0)
            }
        },
        tooltip: {
        }
    });
	
}


/*
  	    	  var $selected = $("#feature_names").multiselect("getChecked");	    	  
	    	  var features = $selected.map(function () { 
	    		  return this.value; }).get();
	    	  
	    	  var $itree = $('#analyze_input_datastream_tree');
	    	  var inode = $itree.tree('getSelectedNode');
	    	  var idatastream = inode.name;
	    	  var irepo = inode.parent.name;
	    	  
	    	  //alert("selected " + irepo + " " + idatastream);

	    	  var $ftree = $('#analyze_features_tree');
	    	  var froot = $ftree.tree('getNodeById', 1);	    	  
	    	  froot.name = "Selected features"; // update root name
	    	 
        	  var frepo_node = null;    	  
        	  for (var i=0; i < froot.children.length; i++) {
          		  if(froot.children[i].name == irepo) {
          			  frepo_node = froot.children[i];
        			  break;
        		  }
        	  }
        	  
        	  // add repo node, if not already exists
        	  if(!frepo_node) {
        		  frepo_node = $ftree.tree( 'appendNode', { label: irepo }, froot );
        	  }

        	  var fds_node = null;    	  
        	  for (var i=0; i < frepo_node.children.length; i++) {
          		  if(frepo_node.children[i].name == idatastream) {
          			  fds_node = frepo_node.children[i];
        			  break;
        		  }
        	  }
        	  
        	  // add datastream node, if not already exists
        	  if(!fds_node) {
        		  fds_node = $ftree.tree( 'appendNode', { label: idatastream }, frepo_node );
        	  }
        	  
        	  
        	  var duplicate_features = [];
	    	  //alert(features.length);	    	  
    		  // check duplicate feature names
        	  for (var i = 0; i < features.length; i++) {	    		  
        		  var is_duplicate = false;
        		  // alert(fds_node.name + "--" + fds_node.children + "--" + fds_node.children.length);
        		  for (var j=0; j < fds_node.children.length; j++) {	        		  
	          		  if(fds_node.children[j].name == features[i]) {
	          			is_duplicate = true;	          			
	        			break;
	        		  }
	        	  }
	        	  
	    		  if(is_duplicate) {
	    			  duplicate_features.push(features[i]);  
	    		  } else {
	    			  $ftree.tree( 'appendNode', { label: features[i] }, fds_node );  
	    		  }
	    	  }
        	  
        	  //alert(duplicate_features.length);
        	  if(duplicate_features.length > 0) {
        			FlashAlert.show("You had already selected these features: " + duplicate_features);
        	  } else {
            	  var msg = "Feature names " + features + " added to " + fds_node.name;
            	  FlashAlert.show(msg);        		  
        	  }
 */