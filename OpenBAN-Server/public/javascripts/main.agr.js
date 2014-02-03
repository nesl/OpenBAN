var add_to = "";

$(function() {
	
 	// aggregate container
	aggregateLayout = $("#aggregate_container").layout({ // DO NOT use "var pageLayout" here
		west__size : .25,
		east__size : .50,
		south__initClosed : true,
		north__initClosed : true,
		west__onresize : $.layout.callbacks.resizePaneAccordions // west accordion a child of pane
		,
		east__onresize : $.layout.callbacks.resizePaneAccordions
	// east accordion nested inside a tab-panel
	});
	
 	//console.log(aggregateLayout);

	var tdata = [{label: '', id:1}];
	var gdata = [{label: '', id:1}];
	
	//$('#aggregate_training_tree').tree({data: tdata});	
	//$('#aggregate_groundtruth_tree').tree({data: gdata});
		
	$( "#aggregate_from_datepicker" ).datepicker({
	    defaultDate: "+1w",
	    dateFormat : "yy-mm-dd",
	    changeMonth: true,
	    numberOfMonths: 1,
	    onClose: function( selectedDate ) {
	      $( "#aggregate_to_datepicker" ).datepicker( "option", "minDate", selectedDate );
	    },
	    onSelect: function( selectedDate ) {
	    	appProfileChanged();
	    }
	  });
	  $( "#aggregate_to_datepicker" ).datepicker({
	    defaultDate: "+1w",
	    dateFormat : "yy-mm-dd",
	    changeMonth: true,
	    numberOfMonths: 1,
	    onClose: function( selectedDate ) {
	      $( "#aggregate_from_datepicker" ).datepicker( "option", "maxDate", selectedDate );
	    },
	    onSelect: function( selectedDate ) {
	    	appProfileChanged();
	    }
	  });

	$(document).on("click", "#add_training_DataStream", function(e) {	
		$.getJSON(
			    '/repo/list',
			    function(data) {		    	
			        $('#tree1').tree({			        	
			            data: data,
			            autoOpen: 0
			        });
			    }
			);
	
		add_to = "training";
		$("#add-datastream-dialog").dialog("open");
	});
	
	$(document).on("click", "#remove_training_DataStream", function(e) {
  	  	var $tree = $('#aggregate_training_tree');
		var node = $tree.tree('getSelectedNode');
		
		if(node && node.getLevel() != 3 ) {
			FlashAlert.show("Select a datastream and then click on 'Remove'");
			return;
		}
			
		var fdsname = node.name;
		var freponame = node.parent.name;			
		//remove the corresponding nodes from features tree as well			
		var $ftree = $('#analyze_features_tree');    		  
		root = $ftree.tree('getNodeById', 1);
		for (var i=0; i < root.children.length; i++) {				
			var arepo = root.children[i];
			if(arepo.name == freponame) {
				for (var j=0; j < arepo.children.length; j++) {
					if(arepo.children[j].name == fdsname) {
						alert("removing " + arepo.children[j].name + " from " + arepo.name);
						if(arepo.children.length == 1) {
							$ftree.tree("removeNode", arepo);	
						} else {
							$ftree.tree("removeNode", arepo.children[j]);
						}						
					}
				}
			}
		} // for i
		if(node.parent.children.length == 1) {
			$tree.tree("removeNode", node.parent);	
		} else {
			$tree.tree("removeNode", node);
		}	
		FlashAlert.show("You removed datastream " + fdsname + " from " + freponame);
		appProfileChanged();	
	});
	
	
	$(document).on("click", "#add_groundtruth_DataStream", function(e) {	
		$.getJSON(
			    '/repo/list',
			    function(data) {		    	
			        $('#tree1').tree({			        	
			            data: data,
			            autoOpen: 0
			        });
			    }
			);
		add_to = "groundtruth";
		$("#add-datastream-dialog").dialog("open");
	});
	
	
	$('#aggregate_training_tree').bind(
		    'tree.click',
		    function(event) {
		        // The clicked node is 'event.node'
		        var node = event.node;
	    	  	// leaf node 
	    	  	if(node.getLevel() == 3 ) {
		        	//alert(node.name + " is leaf");	        	
		        	//$('#example').dataTable( { "sAjaxSource": "/data/json1"} );
		        	updateAggregateTable(node.parent.name, node.name);
		        }
		    }
	);

	$('#aggregate_groundtruth_tree').bind(
		    'tree.click',
		    function(event) {
		        // The clicked node is 'event.node'
		        var node = event.node;
		        
	    	  	if(node.getLevel() == 3 ) {
		        	// alert(node.name + " is leaf");	        	
		        	// $('#example').dataTable( { "sAjaxSource": "/data/json1"} );
	    	  		updateAggregateTable(node.parent.name, node.name);
		        }
		       // $('#smwizard').smartWizard('showMessage', node.name );
		    }
	);

	$("input[name='plotType']").on("change", function () {
	    //alert(this.value);
		//alert($("input[name='plotType']:checked").val());
		  charttype = $("input[name='plotType']:checked").val();
	      updateAggregateHighCharts(charttype);	    
	});
	
	
	$( "#add-datastream-dialog" ).dialog({
	    autoOpen: false,
	    modal: true,
	    resizable: false,
	    height: 'auto',
	    width: 'auto',
	    buttons: {
	      "Add": function() {
	    	  
	    	  $tree = $('#tree1');
	    	  
	    	  var node = $tree.tree('getSelectedNode');
	    	  
	    	  if(!node) {
	    		  return;
	    	  }
	    	  
	    	  // is't a non leaf node? 
	    	  if(node.children.length != 0 ) {
	    		  //alert("opening...");
	    		  $tree.tree('openNode', node);
	    		  return;    	  
	    	  }
	    	  
	    	  var p1 = node.parent;
	    	  var data_service = p1.parent;
	    	  
	    	  var full_name = p1.name + " " + node.name;
	    	  //alert(full_name);
	    	  
	    	  //alert(add_to);
	    	  if(add_to == "training" || add_to == "act_consumer" ) {
	    		  
	    		  var root;
	    		  if(add_to == "training") {
		    		  var $t = $('#aggregate_training_tree');    		  
		        	  root = $t.tree('getNodeById', 1);        	  
		        	  // update root
		        	  root.name = "Training data streams";	    			  
	    		  } else if (add_to == "act_consumer") {
		    		  var $t = $('#act_consumer_tree');    		  
		        	  root = $t.tree('getNodeById', 1);        	  
		        	  // update root
		        	  root.name = "Result consumer data streams";
	    		  }	    		  
	        	  
	        	  var ds_node = null;        	  
	        	  for (var i=0; i < root.children.length; i++) {
	          		  if(root.children[i].name == data_service.name) {
	        			  ds_node = root.children[i];
	        			  break;
	        		  }
	        		}        	  
	        	  if(ds_node) {
	        		  // is the datastream already selected?
	        		  var isExist = false;
	        		  for (var i=0; i < ds_node.children.length; i++) {
		          		  if(ds_node.children[i].name == full_name) {
		          			  isExist = true;
		        		  }
	        		  }
	        		  
	        		  if(isExist) {
	          			  FlashAlert.show(full_name + " already addeded!");
	        		  } else {
	        			  $t.tree( 'appendNode', { label: full_name }, ds_node );
	        		  }	        		  
	        		  
	        	  } else {
	            	  ds_node = $t.tree( 'appendNode', { label: data_service.name }, root );
	            	  $t.tree( 'appendNode', { label: full_name }, ds_node );        		  
	        	  }
	        	  
	        	  //alert(root.name);    	  
	        	  //alert(n.name);
	    		  
	    	  } else if(add_to == "groundtruth") {

	    		  // re-initialize the tree
	    		  var $t = $('#aggregate_groundtruth_tree');
        		  var tdata = [{label: 'Groundtruth data stream', id:1}];
        		  $t.tree('loadData', tdata);
	        	  var root = $t.tree('getNodeById', 1);

	        	  var ds_node = $t.tree( 'appendNode', { label: data_service.name }, root );
	        	  $t.tree( 'appendNode', { label: full_name }, ds_node );
	        	  //alert(root.name);    	  
	        	  //$t.tree( 'appendNode', { label: full_name }, root );    		  
	    	  }
	    	
	    	  appProfileChanged();
	  	   
	    	  /*
	    	  for (var key in node) {
	    		  alert(key + "=" + node[key]);
				}
	    	  */
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


function checkAggregateDateRange() {
	var from_date = $('#aggregate_from_datepicker');
	var to_date = $('#aggregate_to_datepicker');

	var d1 = new Date(from_date.val());
	var d2 = new Date(to_date.val());	
	//alert(d1+d2);	
	if(isNaN(d1.getTime()) || isNaN(d2.getTime())) {
		FlashAlert.show('Select valid aggregation date range to display the data points');
		return false;
	}	
	return true;
}


function updateAggregateTable(service, datastream) {
	
	if(!checkAggregateDateRange()){
		return;
	}
	
	if(!isAppSaved()) {
		return;
	}
	
	var from_date = $('#aggregate_from_datepicker');
	var to_date = $('#aggregate_to_datepicker');

	
	//TODO: validate the date range
	
	if(!from_date.val() || !to_date.val()) {
		//$('#smwizard').smartWizard('showMessage','Select date range to display the data points');
		//$('#smwizard').smartWizard('showMessage','Select date range to display the data points');
		
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
	
	//alert(d1+d2);
	
	//$('#smwizard').smartWizard('showMessage',null);
	//alert(from_date.val() + " " + to_date.val());

	var appname = $.trim($("#current_app_name").text());
	
	var param = "?appname="+appname + "&service=" + service+"&datastream="+datastream 
				+ "&from=" + from_date.val() + "&to="+to_date.val();
	var url = "/data/json1" + param;
	
	var dataPoints = null;
	
	msg = "Please wait while fetching " + datastream + " from " + service  + "...\nThis may take several minutes!"
	blockUImsg(msg);		
		
	var oTable = $('#aggregate_container_table').dataTable( {
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
			 charttype = $("input[name='plotType']:checked").val();
		      updateAggregateHighCharts(charttype);			      
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

function updateAggregateHighCharts(charttype) {
	
	//alert("updating charts...");
	
    $('#aggregate_container_plot').highcharts({
        data: {
            table: document.getElementById('aggregate_container_table')
        },
        chart: {
            zoomType: 'x',
            type: charttype
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
