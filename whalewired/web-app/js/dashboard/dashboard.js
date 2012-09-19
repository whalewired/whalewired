/* This page contain scripts for dashboard page
 * The code is taken from Simplenso template
 */

var closedBoxesCookiePrefix = "SimplensoClosedBoxes_";
var boxPositionCookiePrefix = "SimplensoColumnBoxes_";
var deletedBoxesCookiePrefix = "SimplensoDeletedBoxes_";
var cookieExpiration = 365;


$(document).ready(function(){
	
	/*
	 * Portlet code start here 
	 */
	
	// Control funtion for portlet (box) buttons clicks
	function setControls(ui) {		
		//$('[class="box-btn"][title="toggle"]').click(function() {
		$('.box-btn').click(function() {
			var e = $(this);
			//var p = b.next('a');
			// Control functionality
			switch(e.attr('title').toLowerCase()) {
				case 'config':
					widgetConfig(b, p);
					break;
				
				case 'toggle':
					widgetToggle(e);
					break;
				
				case 'close':
					widgetClose(e);
					break;
			}
		});
	}
	
	// Toggle button widget
	function widgetToggle(e) {
		// Make sure the bottom of the box has rounded corners
		e.parent().toggleClass("round-all");
		e.parent().toggleClass("round-top");
		
		// replace plus for minus icon or the other way around
		if(e.html() == "<i class=\"icon-plus\"></i>") {
			e.html("<i class=\"icon-minus\"></i>");
		} else {
			e.html("<i class=\"icon-plus\"></i>");
		}
		
		// close or open box	
		e.parent().next(".box-container-toggle").toggleClass("box-container-closed");
		
		// store closed boxes in cookie
		var closedBoxes = [];
		var i = 0;
		$(".box-container-closed").each(function() 
		{
				closedBoxes[i] = $(this).parent(".box").attr("id");
				i++;		
		});
		$.cookie(closedBoxesCookiePrefix + $("body").attr("id"), closedBoxes, { expires: cookieExpiration });
	    
		//Prevent the browser jump to the link anchor
		return false; 
		
	}
	
	// Close button widget with dialog
	function widgetClose(e) {
		// get box element
		var box = e.parent().parent();
		
		// prompt user to confirm
		//bootbox.confirm("Are you sure?", function(confirmed) {
			// remove box
			box.remove();
			
			// store removal in cookie
			$.cookie(deletedBoxesCookiePrefix + $("body").attr("id") + "_" + box.attr('id'), "yes", { expires: cookieExpiration });
		//	});	
	}
	
	$('#box-close-modal .btn-success').click(function(e) {
		   // e is the element that triggered the event
		   console.log(e.target); // outputs an Object that you can then explore with Firebug/developer tool.
		   // for example e.target.firstChild.wholeText returns the text of the button
		});
	
	// Modify button widget
	function widgetConfig(w, p) {		
		$("#dialog-config-widget").dialog({
			resizable: false,
			modal: true,
			width: 500,
			buttons: {
				"Save changes": function(e, ui) {
					/* code the functionality here, could store in a cookie */					
					$(this).dialog("close");
				},
				Cancel: function() {					
					$(this).dialog("close");
				}
			}
		});
	}$('#tab').tab('show');
	
	
	// set portlet comtrols
	setControls();
	


	// Portlets (boxes)
    $(".column").sortable({
        connectWith: '.column',
		iframeFix: false,
		items:'div.box',	
		opacity:0.8,
		helper:'original',
		revert:true,
		forceHelperSize:true,	
		placeholder: 'box-placeholder round-all',
		forcePlaceholderSize:true,
		tolerance:'pointer'
    });
    
	// Store portlet update (move) in cookie
    $(".column").bind('sortupdate', function() {
        $('.column').each(function() {
            $.cookie(boxPositionCookiePrefix + $("body").attr("id") + ($(this).attr('id')), $(this).sortable('toArray'), { expires: cookieExpiration });
        });
    });
    
	// Portlets | INIT | check for closed portlet cookie
	var ckie = $.cookie(closedBoxesCookiePrefix+$("body").attr("id"));
	if (ckie && ckie != '')	{
		// get cookie and split in array
		var list = ckie.split(',');
		
		// loop over boxes in cookie and do actions
		for (var x = 0; x < list.length; x++) {	
		 	var box = $("#"+list[x]);
			// close box
			box.find(".box-container-toggle").toggleClass("box-container-closed");
			// make closed box round
			box.find(".box-header").toggleClass("round-top").toggleClass("round-all");
			// find toggle button and change icon
			box.find('a[title="toggle"]').html("<i class=\"icon-plus\"></i>");
		}
	}
	
	/* Portlets | INIT | check for porlet order cookies	*/
    for (var i = 0; i < $(".box").size(); i++) {
		// get the cooke containing the 
        var ckie = $.cookie(boxPositionCookiePrefix+ $("body").attr("id") + "col"+i);

        if (ckie && ckie != ''){
            var list = ckie.split(',');

            for (var x = 0; x < list.length; x++) {
               $('#'+list[x]).appendTo('#col' + i);			  
            }
        }
    }

	/* Portlets | INIT | Deleted boxes stored in cookie*/
	$(".box").each(function() {	
		// get id for each box	
		var id = $(this).attr("id");

		// get cookie for that box
		var ckie = $.cookie(deletedBoxesCookiePrefix + $("body").attr("id" )+ "_" + id);
		
		// check if cookie exist if so delete box from screen
		if (ckie && ckie != '') {
			$(this).remove();
		}
	});
	
	
	
});	
