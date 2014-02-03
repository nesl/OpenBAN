 var getAppName = (function() {
  		alert("name");    	
		bootbox.prompt("Enter App name? (max. length of 20)", function(result) {		
			// do some validataion.. max app length		
				if (result.length > 20) {
					return null;
				}
				return result;
			});
  	});

$(function() {	
	
	FlashAlert.init( {"selector": ".bb-flash-alert"} );

 	// aggregate container
	analyzeLayout = $("#analyze_container").layout({ // DO NOT use "var pageLayout" here
		west__size : .25,
		east__size : .50,
		south__initClosed : true,
		north__initClosed : true,
		west__onresize : $.layout.callbacks.resizePaneAccordions // west accordion a child of pane
		,
		east__onresize : $.layout.callbacks.resizePaneAccordions
	// east accordion nested inside a tab-panel
	});

 	// aggregate container
	actLayout = $("#act_container").layout({ // DO NOT use "var pageLayout" here
		west__size : .25,
		east__size : .50,
		south__initClosed : true,
		north__initClosed : true,
		west__onresize : $.layout.callbacks.resizePaneAccordions // west accordion a child of pane
		,
		east__onresize : $.layout.callbacks.resizePaneAccordions
	// east accordion nested inside a tab-panel
	});

	
/// 	console.log(analyzeLayout);
	//alert("ana layout..." + analyzeLayout);

	// Smart Wizard 	
	$('#smwizard').smartWizard({
			transitionEffect: 'slideleft',
			onLeaveStep: leaveAStepCallback,
			onFinish: onFinishCallback,
			enableFinishButton:true
			});
  
  	function onFinishCallback(){
    	$('#smwizard').smartWizard('showMessage','Finish Clicked');
  	}  
  	
  	function leaveAStepCallback(obj){
        var step_num= obj.attr('rel');
        //alert(step_num);
        //$('#smwizard').smartWizard('showMessage',step_num);
        
        //var div1Html = $('#step-1').html();
        //$('#step-2').html(div1Html);
        
		var $ttree = $('#aggregate_training_tree');
		var troot = $ttree.tree('getNodeById', 1);
		
		var $ftree = $('#analyze_features_tree')		
		var froot = $ftree.tree('getNodeById', 1);
		
		//alert(troot.children.length + "--" + froot.children.length + "--");
		
		// update only when there some data in training tree and no data in feature tree
		if(troot.children.length > 0 && froot.children.length == 0 ) {
			var tdata = jQuery.parseJSON( $ttree.tree("toJson") );
			$ftree.tree("loadData", tdata);
			var froot = $ftree.tree('getNodeById', 1);
			$ftree.tree('updateNode', froot, 'Features');
		}

		return obj;
        //return validateSteps(step_num);
  	}
  	
	$("#appmenu li").not(".noapp").click( function(e) {	
		//FlashAlert.show("Confirm result: "+this.id);
		
		if(this.id == "appmenuNew") {
    	    window.location.href = '/home/newapp';
		}
		if(this.id == "appmenuSave") {			
			var $cur_app = $("#current_app_name")
			var appname = $.trim($cur_app.text());
			
			if(appname === "newapp") {
				bootbox.prompt("Enter App name? (2-30 chars in length and not 'newap')", function(result) {
					var newname = $.trim(result);					
					if(newname != "newapp" && newname.length >= 2 && newname.length <= 30 ) {						
						//var ht = '<i class="glyphicon glyphicon-tasks"></i> ' + newname + ' <b class="caret"></b>';
						//$cur_app.html(ht);
						//alert(newname);
						saveApp(newname,true); // its a new app
					} else {
						FlashAlert.show("Invalid app name: "+ newname);
					}
				});				
			} else {
				saveApp(appname,false);
			}
		} else if(this.id == "appmenuRename") { /* disabled now*/
			//renameApp();
		}		
		//alert(this.id);
	});

	$("#registeredRepolist li").not(".norepo").click( function(e) {		
		if(this.id == "registerNewRepo") {
			$( "#register-repo-dialog" ).dialog("open");		
		} else {
			//alert(this.id);
			FlashAlert.show("Confirm result: "+this.id);
		}
	});

	$("#myAppList li").not(".noapp").click( function(e) {	
		//FlashAlert.show("Confirm result: "+this.id);
		//alert(this.id);
		// TODO: check whether current app is saved or not? 
		//loadApp(this.id);
		//FlashAlert.show("about to load : "+this.id);
		//alert(this.id);
	    window.location.href = '/home/'+this.id;
	});
});

	/**  http://bootboxjs.com/js/example.js */
var FlashAlert = (function() {
 	    "use strict";

 	    var elem,
 	        hideHandler,
 	        that = {};

 	    that.init = function(options) {
 	        elem = $(options.selector);
 	    };

 	    that.show = function(text) {
 	        clearTimeout(hideHandler);

 	        elem.find("span").html(text);
 	        elem.delay(200).fadeIn().delay(4000).fadeOut();
 	    };

 	    return that;
 	}());


function blockUImsg(msg) {
		
		var prog = '<div class="progress progress-striped active"> <div class="progress-bar" style="width: 100%"></div> </div>';
		var msg =  '<h3>' + msg + '</h3>' + prog;
		
		$.blockUI( { message: msg, 
			css:{border: 'none', 
	            padding: '15px', 
	            backgroundColor: '#eee', 
	            '-webkit-border-radius': '10px', 
	            '-moz-border-radius': '10px', 
	            opacity: 1, 
	            color: '#008' } });
	}

// reponame is custom user defined name for the repo
function registerRepo(reposource, reponame, repourl, userid, key) {
	var param = "reposource=" + reposource + "&reponame=" + reponame + "&repourl=" + repourl + "&userid=" + userid + "&key=" + key;	
	//$.blockUI({ message: '<h1>Fetching Feeds...</h1>' });
	blockUImsg("Please wait while registering new repo " + reponame);
	$.ajax({
		type: "PUT",
		url: "/repo/register",
		data: param,
		cache: false,
		success: function (response) {
		    $.unblockUI();
			//alert("ajax success " + response);
			if(response === "Success" ) {
				var lihtml = '<li><a href="#" class = "' + repo + '" >' + repo + '</a></li>';				
				$( ".repolist" ).prepend(lihtml);				
				$(".norepo").remove();
		    	bootbox.alert("New repo " + repo + " registered successfully!");
			} else {
				bootbox.alert( "Dropbox Error " + response);
			}
		},
		complete: function (xhr, ajaxOptions, thrownError) {
			//alert("ajax completed");
			$.unblockUI();
		},		
		error: function (xhr, ajaxOptions, thrownError) {			
			bootbox.alert( "Error occured : " + xhr.status + thrownError + " Try again later!");
			$( "#register-repo-dialog" ).dialog("open");
		}
	});
}





