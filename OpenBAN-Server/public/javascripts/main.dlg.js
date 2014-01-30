$(function() {
	
	var reposource = $( "#repoSourceList" ),
	userId = $( "#inputUser" ),
	reponame = $( "#inputRepoName" ), // custom name for the user
	repourl = $( "#inputRepoUrl" ), // custom name for the user
	key = $( "#inputKey" ),
	progress = $("#registerRepoProgress"),
	tips = $( ".validateTips" );
	
	progress.css("visibility", "hidden");

	function updateTips( t ) {
	      tips
	        .text( t )
	        .addClass( "ui-state-highlight" );
	      setTimeout(function() {
	        tips.removeClass( "ui-state-highlight", 1500 );
	      }, 2000 );
	    }
	 
	    function checkLength( o, n, min, max ) {
	      if ( o.val().length > max || o.val().length < min ) {
	        o.addClass( "ui-state-error" );
	        updateTips( "Length of " + n + " must be between " +
	          min + " and " + max + "." );
	        return false;
	      } else {
	        return true;
	      }
	    }
	    
$( "#register-repo-dialog" ).dialog({
		autoOpen: false,
		modal: true,
		resizable: false,
		height: 'auto',
		width: 'auto',
		buttons: {
		  "Register": function() {			  
			tips.text("");
			reponame.removeClass( "ui-state-error" );
		  	userId.removeClass( "ui-state-error" );
		    key.removeClass( "ui-state-error" );
		    
		    var bValid = true;
		    bValid = bValid && checkLength( reponame, "Repo Name", 3, 16 );		    
		    if(reposource == "SensorAct") {
		    	bValid = bValid && checkLength( repourl, "Repo URL", 3, 16 );
		    }		    
		    bValid = bValid && checkLength( userId, "User Id", 3, 16 );
		    bValid = bValid && checkLength( key, "key", 32, 48 );
		    
		    if ( bValid ) {
		    	progress.css("visibility", "inline");
				//name.attr("disabled", true);
		    	registerRepo(reposource.val(), reponame.val(), repourl.val(), userId.val(), key.val());        		
				$( this ).dialog( "close" );          	
		    }
		    
		  },
		  Cancel: function() {
		    $( this ).dialog( "close" );
		  }
		},
		close: function() {
		  //allFields.val( "" ).removeClass( "ui-state-error" );
			reponame.val("").removeClass( "ui-state-error" );
			userId.val("").removeClass( "ui-state-error" );
		    key.val("").removeClass( "ui-state-error" );
		    //tips.text("");
		}
});




});