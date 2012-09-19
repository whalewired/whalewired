
$(document).ready(function(){
	$('.dashboardButton').click(function() {
		var e = $(this);
		// Control functionality
		switch(e.attr('title')) {
			case 'addErrorGraphBox':
				addErrorGraphBox();
				break;
			case 'addErrorGraph':
				addErrorGraph();
			
		}
	});
	
});


function addErrorGraphBox(){
	$('#box-add-errorchart-dialog').modal();
}

function addErrorGraph(){
	var dropdownlist = $("#createErrorGraphDropdown").data("kendoDropDownList");
	var application = dropdownlist.dataItem();
	
	
//	var application = $("#createErrorGraphDropdown").val();
	var box = "<div class='span4 column' id='col1' style='min-width: 225px;'>" +
			"<div class='box' id='box-0'>" +
			"	<h4 class='box-header round-top'>"+application+
            "		<a class='box-btn' title='toggle'><i class='icon-minus'></i></a>" +
            "	</h4> "+        
            "  <div class='box-container-toggle'>"+
            "       <div class='box-content'>"+
            "		<input type='hidden' val='"+application+"'>" +
            "       <div class='chartErrorOnApp'></div>"+                    
            "   </div>"+
            "  </div>"+
            "</div>";
		
		
	$("#col1").append(box);
}