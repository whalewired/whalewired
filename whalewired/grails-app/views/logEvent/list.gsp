<!doctype html>
<html>
<head>
<meta name="layout" content="main" />
<title>Events</title>
<script type="text/javascript">
			var grid;
			var tailWindow;
			var tailWindowTimer;
			var applicationValue = '${currentApplication}';
			var levelValue = '${currentLogLevel}';
			var pollingTimer;
		
		    function triggerPolling() {
				if (pollingTimer) {
					window.clearInterval(pollingTimer);
	            	$("#pollingButton").text("Start");
				} else {
					pollingTimer = window.setInterval(triggerFetch, $("#pollingInterval").val() * 1000);
	              	$("#pollingButton").text("Stop");
				}
		    }

		    function triggerFetch() {
			   
			   	grid.dataSource.page(1);
		    }

		    function triggerSearch() {
		    	triggerFetch();
		    }

		    function closeTail() {
		    	window.clearInterval(tailWindowTimer);
		    }

		    function showTail() {
		    	tailWindow = $("#tail").kendoWindow({
			    	draggable: true,
		    	    resizable: true,
		    	    width: "1000px",
		    	    height: "800px",
		    	    title: "Show log tail",
		    	    close: closeTail,
		    	    modal: false,
		    	    actions: ["Maximize", "Close"], 
		    	    content: "tail"
		    	}).data("kendoWindow");
		    	tailWindow.center();	    	     
		    	tailWindow.open();
				tailWindowTimer = window.setInterval("tailWindow.refresh()", $("#pollingInterval").val() * 1000);
		    }

			$(document).ready(function() {
				$("#eventTable").kendoGrid({
                	dataSource: {
                    	type: "json",
                       	transport: {
                        	read: {
                            	url: "listJSON"
                           	},
                           	parameterMap: function(options) {
 			               		return {
 			               			page: options.page,
	 			                    pageSize: options.pageSize,
	 			                    take: options.take,
	 			                    skip: options.skip,
	 			                    sortField: options.sort && options.sort[0] ? options.sort[0].field : "",
	 		 			            sortOrder: options.sort && options.sort[0] ? options.sort[0].dir : "",
	 			                    logApplication: applicationValue, 
	          						logLevel: levelValue, 
	          						logSearch: $("#searchFilter").val()
 			                 	}
 			               	}
                       },
                       schema: {
                           data: function(data) {
                               return data.result;
                           },
                           total: function(data) {
                               return data.total;
                           }
                       },                       
                       pageSize: 20,
                       serverPaging: true,
                       serverSorting: true,
                       sort: { field: "logTime", dir: "desc" },
                       serverFiltering: true
                   },
                   dataBound: function(e) {
		            	  $('span[tooltip]').each(function()  {
		            		  
       			           $(this).qtip(
            		      {
            		         content: $(this).attr('tooltip'),
            		         position: {
                		         target: 'mouse',
                		         adjust: {	
                		        	 mouse: false
                		         },
            		            viewport: $(window), 
            		            effect: false 
            		         },
            		         show: {
            		            event: 'click',
            		            solo: true 
            		         },
            		         hide: 'unfocus',
            		         style: {
                		        width: { min: 0, max: 500 },		             
            		            classes: 'k-tooltip',
            		            tip: 'bottomLeft'
            		         }
            		      }) 
            		   }); 	 			               
                   },
                   scrollable: {
                       virtual: false
                   },
                   sortable: true,
                   groupable: true,
                   pageable: true,
                   columns: [{field:"logTime",title:"Time",width:"47px",template:'<a href="show/#=escape(data._id)#">#=(kendo.toString(new Date(logTime),"dd-MM-yyyy HH:mm:ss"))#</a>'},
                             {field:"hostName",title:"Host", width:"30px"},
                             {field:"logLevel",title:"Level", width:"30px"},
                             {field:"logMessage",title:"Message", width:"250px", template:'<span tooltip="#=data.logThrowableTrace ? logThrowableTrace.substring(0, 500) + \"...\" : \" No trace \"#" style="#=data.logThrowableTrace ? \"cursor: pointer;\": \"\"#">#=logMessage#</span>'},
                             {title:"Issue", width:"15px", template:'<img src="#=data.issueId ? \"${resource(dir: 'images', file: 'jira.png')}\" : \"${resource(dir: 'images', file: 'Blank.png')}\" #" alt="Jira">'},
                             {title:"Mark", width:"15px", template:'<img align="center" valign="center" src="#=data.bookmarkId ? \"${resource(dir: 'images', file: 'bookmark.jpg')}\" : \"${resource(dir: 'images', file: 'Blank.png')}\" #" alt="Bookmark">'}]
               });				

				grid = $("#eventTable").data("kendoGrid");
				$("#eventTable").height("auto"); // IE9 fix
				
	            $("#applicationFilter").kendoDropDownList({
	           		dataSource: {
	                	type: "json",
	                    transport: {
	                    	read: "../logApplication/listJSON"
	                    },
	                    schema: {
	                        data: function(data) {
	                    		return data.result;
	                        }
	                    }
	                },
	                dataTextField: "name",
	                dataValueField: "name",
	           	   	change: function() {
	           	   		applicationValue = this.value();
	           	   		triggerFetch();	
	                }
	                
	            });
				
				var logLevelItems = new kendo.data.DataSource({data:[ 
					{ value: "TRACE", text: "TRACE" }, 
					{ value: "DEBUG", text: "DEBUG" }, 
					{ value: "INFO", text: "INFO" }, 
					{ value: "WARN", text: "WARN" }, 
					{ value: "ERROR", text: "ERROR" }, 
					{ value: "FATAL", text: "FATAL" }, 
					{ value: "UNKNOWN", text: "UNKNOWN" } 
				]});
					              
              $("#logLevelFilter").kendoDropDownList({
            	  	optionLabel: "Select An Option",
            	  	dataSource: logLevelItems,
                    dataTextField: "text",
                    dataValueField: "value",
	                template: '# if (data.value) { #<img src="${createLink(uri: '/images/')}#=data.value#.png" style="margin-right: 5px;"/>#}# #= data.text #',
           	   		change: function() {
           	   			levelValue = this.value();
           	   			triggerFetch();	 
                	}
              });

              
              
           });
		</script>

</head>
<body>
	<a href="#list-logEvent" class="skip" tabindex="-1"><g:message
			code="default.link.skip.label" default="Skip to content&hellip;" /></a>
	<div id="list-logEvent" class="content scaffold-list" role="main">
		<h1>Events</h1>
		<g:if test="${flash.message}">
			<div class="message" role="status">
				${flash.message}
			</div>
		</g:if>
		<div>
			<div class="optionPanel">
				<label for="applicationFilter" style="margin-left: 10px">Application</label><br />
				<input id="applicationFilter" value="${currentApplication}" />
			</div>
			<div class="optionPanel">
				<label for="logLevelFilter" style="margin-left: 10px">Level</label><br />
				<input id="logLevelFilter" value="${currentLogLevel}" />
			</div>
			<div class="optionPanel">
				<label for="searchFilter" style="margin-left: 10px">Query</label><br /> 
				<input
					id="searchFilter" type="text" onkeyup="triggerSearch();"
					value="${currentLogSearch}" />
			</div>
		</div>


		<div style="float: right; margin: 0px 11px 10px;">
			<label for="pollingInterval">Polling interval (seconds): </label><br /> 
			<input
				id="pollingInterval" type="text" value="10"
				onkeypress="if (event.keyCode == 13) $('#pollingButton').click();"
				style="position: relative; top: 4px; text-align: right; width: 30px;" />
			<button id="pollingButton" onclick="triggerPolling();"
				class="k-button">Start</button>
			<button id="tailButton" onclick="showTail();" class="k-button">Tail</button>
		</div>
		<div id="eventTable"
			style="clear: left; float: left; margin: 5px; border-radius: 5px; height: auto; width: 98.8%;"></div>
	</div>
	<div id="detail"></div>
	<div id="tail" style="width: 100%; word-wrap: break-word;"></div>
	<div id="traceTeaser"></div>

</body>
</html>
