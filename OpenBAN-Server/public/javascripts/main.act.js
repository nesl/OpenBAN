$(function() {	
	$( "#act_from_datepicker" ).datepicker({
	    defaultDate: "+1w",
	    dateFormat : "yy-mm-dd",
	    changeMonth: true,
	    numberOfMonths: 1,
	    onClose: function( selectedDate ) {
	      $( "#act_to_datepicker" ).datepicker( "option", "minDate", selectedDate );
	    },
	    onSelect: function( selectedDate ) {
	    	appProfileChanged();
	    }
	  });

	  $( "#act_to_datepicker" ).datepicker({
	    defaultDate: "+1w",
	    dateFormat : "yy-mm-dd",
	    changeMonth: true,
	    numberOfMonths: 1,
	    onClose: function( selectedDate ) {
	      $( "#act_from_datepicker" ).datepicker( "option", "maxDate", selectedDate );
	    },
	    onSelect: function( selectedDate ) {
	    	appProfileChanged();
	    }
	  });

	$(document).on("click", "#act_execute_now", function(e) {
		// save the app first		
		execute_now();
	});

	$(document).on("click", "#act_schedule", function(e) {
		// save the app first
		schedule_app();		
	});
	
	//var add_to = "";
	$(document).on("click", "#add_consumer_DataStream", function(e) {	
		$.getJSON(
			    '/repo/list',
			    function(data) {		    	
			        $('#tree1').tree({			        	
			            data: data,
			            autoOpen: 0
			        });
			    }
			);	
		add_to = "act_consumer";
  	  	//alert(add_to);
		$("#add-datastream-dialog").dialog("open");
	});
	
	
}); // jquery

function check_saved() {

	var $cur_app = $("#current_app_name")
	var appname = $.trim($cur_app.text());
			
			if(appname === "newapp") {
				bootbox.prompt("Enter App name? (max. length of 20)", function(result) {
					var newname = $.trim(result);					
					if(newname.length >= 1 && newname.length <= 20 ) {						
						//var ht = '<i class="glyphicon glyphicon-tasks"></i> ' + newname + ' <b class="caret"></b>';
						//$cur_app.html(ht);
						//alert(newname);
						saveApp(newname,true); // its a new app
					} else if(newname.length > 20){
						FlashAlert.show("Invalid app name: "+ newname);
					}
				});				
			} 
			
			if(isAppProfileChanged == true) {
				//alert("going to save the app");
				saveApp(appname,false);	
			}

}

function check_dates() {
	var act_from_date = $.trim($('#act_from_datepicker').val());
	var act_to_date = $.trim($('#act_to_datepicker').val());

	if(act_from_date.length == 0 ) {
		FlashAlert.show("Invalid from/to date range");
		return false;
	}

	if(act_to_date.length == 0 ) {
		FlashAlert.show("Invalid from/to date range");
		return false;
	}
	return true;
}

function check_schedule_params() {
	var act_from_date = $.trim($('#act_from_datepicker').val());
	var act_to_date = $.trim($('#act_to_datepicker').val());
	var act_time_duration = $.trim($('#act_time_duration').val());
	
	if(act_from_date.length == 0 ) {
		FlashAlert.show("Invalid date name");
		return false;
	}

	if(act_time_duration.length == 0 ) {
		FlashAlert.show("Invalid schedule duration");
		return false;
	}
	
	if($.isNumeric(act_time_duration) == false){
		FlashAlert.show("Invalid schedule duration");
        return false;
    }
	
	return true;
	/*
	if(act_to_date.length == 0 ) {
		FlashAlert.show("Invalid to name");
		return false;
	}*/
	
}

function execute_now() {

	if(false == check_dates()) {
		return;
	}	
	
	//alert("going to check");
	//alert("everything is fine");
	check_saved();
	
	//alert("checked..")
	

	msg = "Please wait while executing the app...\nThis may take several minutes!"
	blockUImsg(msg);
	
	var appname = $.trim($("#current_app_name").text());
	var parm = "appname="+appname;


	$.ajax({
        type: 'POST',              
        url: '/execute',
		cache: false,
        data: parm
	})
	.done( function (response) { 			
        	if(response == "Success") {
        		FlashAlert.show("App " + $("#current_app_name").text() + " executed successfully!");
        		updateActTable("execute_now");
        	} else {
        		bootbox.alert("Error while executing the app..." + response);
        	}        		
	})
	.always(function() { 
		$.unblockUI(); 
	})
	.fail(function(jqxhr, textStatus, error) { 
		bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
	});
	
}

function schedule_app() {
	
	if(false == check_schedule_params()) {
		return;
	}	
	
	alert("going to schedule the app");	
	check_saved();
	
	msg = "Please wait while scheduling the app...\nThis may take several minutes!"
	blockUImsg(msg);
	
	var appname = $.trim($("#current_app_name").text());
	var parm = "appname="+appname;

	$.ajax({
        type: 'POST',              
        url: '/app/schedule',
		cache: false,
        data: parm
	})
	.done( function (response) { 			
        	if(response == "Success") {
        		FlashAlert.show("App " + $("#current_app_name").text() + " scheduled successfully!");
        		updateActTable("execute_now");
        	} else {
        		bootbox.alert("Error while scheduling the app..." + response);
        	}
	})
	.always(function() { 
		$.unblockUI(); 
	})
	.fail(function(jqxhr, textStatus, error) { 
		bootbox.alert( "Error occured : " + jqxhr.status + ", " + error + " Try again later!");
	});
}

function updateActTable(instanceName) {

	var appname = $.trim($("#current_app_name").text());	
	var param = "?appname="+appname + "&instance=" + instanceName;
	var url = "/data/json3" + param;
	
	var dataPoints = null;	
	msg = "Please wait while fetching data for " + instanceName + "...\nThis may take several minutes!"
	//alert(msg);
	blockUImsg(msg);
		
	var oTable = $('#act_container_table').dataTable( {
		"bDestroy": true,
		"bProcessing": true,
        "bServerSide": true,			
        "bFilter": false,
        "bSort": false,
        //"bJQueryUI": true,
        //http://www.datatables.net/examples/basic_init/language.html
        //http://stackoverflow.com/questions/10630853/change-values-of-select-box-of-show-10-entries-of-jquery-datatable
        "aLengthMenu": [[10, 30, 60, 120, 300, 600, 900, 1440, 1800, 2880], [10, 30, 60, 120, 300, 600, 900, 1440, 1800, 2880 ]],
        "sPaginationType": "full_numbers",
        // http://datatables.net/forums/discussion/510/showing-the-paging-controls-at-the-top-and-bottom-of-the-datatable/p1
        "sDom": '<"top"lfip>rt<"bottom"ip<"clear">',
	//	"bDeferRender": true,
		"oLanguage": {
	           "sProcessing": "<h3>Fetching datapoints...</h3>"
	         },
		"fnDrawCallback": function( oSettings ) {
		      updateActHighCharts();			      
		    },
		"sAjaxSource": url,
		//"aaData": dataPoints,
		"fnServerData": function ( sSource, aoData, fnCallback ) {				
			
			$.getJSON( sSource, aoData, function (json) {
				
				//alert( JSON.stringify(json.aaData));
				var points = json.aaData;
				
				var offset_mins = new Date().getTimezoneOffset();
				//console.log("ofset mins " + offset_mins);
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

function updateActHighCharts() {
	
	//alert("updating charts...");
	
    $('#act_container_plot').highcharts({
        data: {
            table: document.getElementById('act_container_table')
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
        legend: {enabled : false},
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
            /*
        	formatter: function() {
                return '<b>'+ this.series.name +'</b><br/>'+
                    this.y +' '+ this.x;
            }
            */
        }
    });
	
}

