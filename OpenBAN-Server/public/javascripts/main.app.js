$(function() {	
	// register change/input events for all input fields	
	$('#act_time_duration').on('input', function() {
		appProfileChanged();
	})
	$('#act_time_unit').on('change', function() {
		appProfileChanged();
	})
});

isAppProfileChanged = false; 
function appProfileChanged() {
	//alert("something changed.. save your app first");
	// change the title color of the appname
	
	var $cur_app = $("#current_app_name");
	//var ht = '<i class="glyphicon glyphicon-tasks"></i> ' + $cur_app.text() + ' <b class="caret"></b>';
	//$cur_app.html(ht);
	
	$cur_app.css('color', 'Coral');	
	//$cur_app.animate({padding: '20px'});	
	//<a id="current_app_name" href="#" class="navbar-brand dropdown-toggle" data-toggle="dropdown"> 
	//<i class="glyphicon glyphicon-tasks"></i> ${appname} <b class="caret"></b>
   //</a>

	isAppProfileChanged = true;
}

function isAppSaved() {
	var $cur_app = $("#current_app_name")
	var appname = $.trim($cur_app.text());
	
	if(appname === "newapp" || isAppProfileChanged == true) {
		FlashAlert.show("You changed the application configurations. Please save your app first!");
		return false;
	}
	return true;
}

function parseApp(appObj) {
	
	if(appObj.appname) {
		var $cur_app = $("#current_app_name");
		var ht = '<i class="glyphicon glyphicon-tasks"></i> ' + appObj.appname + ' <b class="caret"></b>';
		$cur_app.html(ht);
	}
	
	if(appObj.aggregate) {
		var aggregate = appObj.aggregate;
		var from_date = aggregate.from_date;
		var to_date = aggregate.to_date;		
		$('#aggregate_from_datepicker').val(from_date);
		$('#aggregate_to_datepicker').val(to_date);
		
		//alert(JSON.stringify(aggregate.training));
		
		$('#aggregate_training_tree').tree('loadData', aggregate.training);	
		$('#aggregate_groundtruth_tree').tree('loadData', aggregate.groundtruth);
		
	} else {
		FlashAlert.show("invalid aggregate class...")
	}
	
	FlashAlert.show("App loaded...")
}
/*
function loadApp(appname) {	
	msg = "Loading app... " + appname;
	blockUImsg(msg);

	$.ajax({
        type: 'GET',              
        url: '/app/load?appname='+appname,
		cache: false
        //data: jsondata
	})
	.done( function (response) {
		if(response==null) {
			bootbox.alert("Unable to load app");
		} else {
			parseApp(response);
		}
	})
	.always(function() { 
		$.unblockUI(); 
	})
	.fail(function(jqxhr, textStatus, error) { 
		bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
	});
}
*/

function saveApp(appname, isNewApp) {
		// what are all the items to save?
		/*
			1. aggregate 
				from-to date
				training and groundtruth data
			2. analyze
				list of features
				feature vector
				model name
			3. action
				??
		*/
		
		//var appname = $.trim($("#current_app_name").text());	
		var agr_from_date = $('#aggregate_from_datepicker').val();
		var agr_to_date = $('#aggregate_to_datepicker').val();
		
		var act_from_date = $('#act_from_datepicker').val();
		var act_to_date = $('#act_to_datepicker').val();
		
		var act_time_duration = $('#act_time_duration').val();
		var act_time_unit = $('#act_time_unit').val();

		
		var feature_window_size = $('#feature_window_size').val();
		var classifier_name = $('#classifier_name').val();
		
		
		var ag_tr_tr = $('#aggregate_training_tree');
		var ag_gt_tr = $('#aggregate_groundtruth_tree');
		
  	  	var features_tree = $('#analyze_features_tree');
  	  	var act_consumer_tree = $('#act_consumer_tree');
  	  	
  	  	var model_options_str = $.trim($('#model_options').val());
  	  	var model_options = "";
  	  	
  	  	try {
  	  		t = "{" + model_options_str +"}"
  	  		//alert(t);
  	  		model_options = JSON.parse(t);
  	  		//alert(JSON.stringify(model_options));
  	  		model_options = model_options_str;
  		} catch (e) {
  			FlashAlert.show("Invalid model options.. It should be a valid Json string");
  			return;
  		}
  				
		var appJsonObj = new Object(); //declare object 
	    var aggregateJsonObj = new Object(); //declare object
	    var analyzeJsonObj = new Object(); //declare object
	    var actJsonObj = new Object(); //declare object
	    
	    aggregateJsonObj.from_date = agr_from_date;
	    aggregateJsonObj.to_date = agr_to_date;	    	    
	    aggregateJsonObj.training = jQuery.parseJSON( ag_tr_tr.tree("toJson") );
	    aggregateJsonObj.groundtruth = jQuery.parseJSON( ag_gt_tr.tree("toJson") );
	    
	    analyzeJsonObj.features = jQuery.parseJSON( features_tree.tree("toJson") );	    
	    analyzeJsonObj.feature_window_size = feature_window_size;
	    analyzeJsonObj.classifier = classifier_name;
	    analyzeJsonObj.options = model_options

	    actJsonObj.from_date = act_from_date;
	    actJsonObj.to_date = act_to_date;
	    
	    actJsonObj.time_duration = act_time_duration;
	    actJsonObj.time_unit = act_time_unit;
	    actJsonObj.consumers = jQuery.parseJSON( act_consumer_tree.tree("toJson") );
	    
	    appJsonObj.appname = appname;
	    appJsonObj.aggregate = aggregateJsonObj;
	    appJsonObj.analyze = analyzeJsonObj;
	    appJsonObj.act = actJsonObj;
	    
	    jsondata = JSON.stringify(appJsonObj); 
	    //alert(jsondata);
	    
		msg = "Please wait while saving your app... " + appname;
		blockUImsg(msg);

		$.ajax({
            type: 'POST',              
            url: '/app/save',
    		cache: false,
            data: jsondata
		})
		.done( function (response) { 			
            	if(response == "Success") {
            		FlashAlert.show("App " + $("#current_app_name").text() + " saved successfully!");
            		$("#current_app_name").removeAttr('style');
            		isAppProfileChanged = false;
            		//$("#current_app_name").removeAttr('color');            		
            		//$("#current_app_name").css('color', 'None');
            		if(isNewApp) { // to refresh and update the title bar
            			window.location.href = '/home/'+appname;	
            		}
            	} else {
            		bootbox.alert("Error while saving app " + appname + ": " + response);
            	}
		})
		.always(function() { 
			$.unblockUI();
		})
		.fail(function(jqxhr, textStatus, error) { 
			bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
		});
}
