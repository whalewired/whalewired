/*
 * Create ChartOnErrors
 * */

var errorCharts = []

function createErrorChart(interval) {
	errorCharts.push("chartErrorOnApp");
	$(".chartErrorOnApp").each(function(index){
		renderErrorChart(this, $(this).prev("input").attr("val"));
	});
}

function renderErrorChart(div, appName){
	$(div).kendoChart({		
		theme: $(document).data("kendoSkin") || "default",
		dataSource: {
			transport: {
				read: {
					url: "errorOnApplicationAsJSON?appName="+appName,
					dataType: "json"
				}
        	},             		
            schema: {
                data: function(data) {
                    return data.result;
                }
            }  
		},
		chartArea: {
			height: 407
		},		
    	legend: {
    		position: "bottom"
    	},
		seriesDefaults: {
			type: "line",
			labels: {
				visible: true,
				format: "{0}"
			}
		},
		series: [{
			field: "occurs",
        	name: "Error occurrences"
    	}],
    	valueAxis: [{
    		labels: {
    			format: "{0}"
			}
    	}],		        	
    	categoryAxis: {
    		field: "date",
        	labels: {
				format: "{0}"
	        }
    	}
	});
}


$(window).resize(function() {
	//on resizing window, then refresh chart
	$(".chartErrorOnApp").each(function(index){
		$(this).data("kendoChart").refresh();
	});
});
