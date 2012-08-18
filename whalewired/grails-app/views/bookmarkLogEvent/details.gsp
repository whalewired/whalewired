<!doctype html>
<html>
	<head>
		<script type="text/javascript">
		var lastNumberOfDays = 7
		$(document).ready(function() {
		   	setTimeout(function() {
		    	createChart();
		    		// Initialize the chart with a delay to make sure
			    	// the initial animation is visible
			    	}, 400);		    	
		    });
	
			
		function createChart() {
			$("#chart").kendoChart({
				theme: $(document).data("kendoSkin") || "default",
				dataSource: {
					transport: {
						read: {
							url: "bookmarkOccurrencesAsJSON?id=${bookmarkId}",
							dataType: "json"
						}
                    },
                    schema: {
                        data: function(data) {
                            return data.result;
                        }
                    }
				},
				title: {
					text: "Occurrences of the same event pattern during the last 12 hours"
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
		            name: "Occurrences"
		        }],
		        valueAxis: {
		        	labels: {
		        		format: "{0}"
					}
		        },
		        categoryAxis: {
		        	field: "date"
		        }
		    });
		}
		</script>
	</head>
	<body>
		<div id="chart"></div>
	</body>
</html>
