
<%@ page import="com.whalewired.BookmarkLogEvent" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">		
		<script type="text/javascript">
			$(document).ready(function() {

				/*
		    	setTimeout(function() {			    									    	
			    	}, 400
			    );
			    */			    

   			    
		    	var grid = $("#bookmarkGrid").kendoGrid({			    	
                    dataSource: {
                    	transport: {
                            // read: "allBookmarksAsJSON",
                            read: "bookmarksAsJSON?currentApplication=${currentApplication}",                            
                            dataType: "json"
                        },
                        schema: {
                            data: function(data) {
                                return data.result;
                            },
                            total: function(data) {
                                return data.total;
                            }
                        },                                            
                        pageSize: 10
                    },                    
                    pageable: true,
                    columns: [ 
						{field: "dateCreated", 
							title: "Date", 
							width: 10, 							
							template: '#=(kendo.toString(new Date(dateCreated),"dd-MM-yyyy HH:mm:ss"))#'},
                    	{field: "description", 
		                    width: 30, 
		                    title: "Description"},                    	                     	
                    	{field: "id", 
		                    title: "Trend", 
		                    width: 10, 
		                    template: '<div id="chart#=id#" style="display: table-cell; width: 150px; height: 50px"></div>'},
		                {field: "id", 
				            title: " ", 
				            width: 5,                     	                    	                    
				            template:'<a style="cursor: pointer; text-decoration: underline; color: blue" onclick="return showDetails(#=parseInt(id)#)">Details</a>'},                        
                        {field: "id", 
		                    title: " ", 
		                    width: 5,                     	                    	                    
		                    template:'<a style="cursor: pointer; text-decoration: underline; color: blue" href="delete?id=#=escape(id)#">Delete</a>'}
                    ],
                    change: function(e) {
                        for (var i=0; i<this.dataSource.total(); i++) {
                            var item = this.dataSource.at(i);                         	
                    		refreshChart(item.id);
                        }                        
                    },
                    dataBound: function(e) {                                                                   	
                    	for (var i=0; i<this.dataSource.total(); i++) {
                        	var item = this.dataSource.at(i);                         	
                        	createChart(item.id);
                        }                        
                    }
                });
                

		    	var dropdown = $("#applicationDropdown").kendoDropDownList({
	                dataTextField: "name",
	                dataValueField: "name",
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
	                }
                	// TODO - want 'change event' to work, so changing application 
                	// can be done without submitting the page
                	/*                	
                    change: function() {
                        var value = this.value();
                        var gridDataSource = grid.data("kendoGrid").dataSource;
                        if (value) {
                        	gridDataSource.filter({
                                field: "indexName", 
                                operator: "eq", 
                                value: value });
                        } else {
                        	gridDataSource.filter({});
                        }                        
                        
                        for (var i=0; i<gridDataSource.total(); i++) {
                        	var item = gridDataSource.at(i);
                        	if (item.indexName == value) {
                        		refreshChart(item.id);
                            }                         	                    		
                        }
                        
                    } 
                	*/               	
                });
                                        		    
		    });		

		    function showDetails(bookmarkId) {
		    	tailWindow = $("#details").kendoWindow({
			    	draggable: true,
		    	    resizable: true,
		    	    width: "1000px",
		    	    height: "800px",
		    	    title: "Details",
		    	    modal: false,
		    	    actions: ["Close"], 
		    	    content: "details/" + bookmarkId
		    	}).data("kendoWindow");
		    	tailWindow.center();	    	     
		    	tailWindow.open();
		    	console.log("bookmarkId: " + bookmarkId);				
		    	return true;
		    }

			function refreshChart(bookmarkId) {				
				$("#chart" + bookmarkId).data("kendoChart").refresh();	    		
			}

			function createChart(bookmarkId) {
				$("#chart" + bookmarkId).kendoChart({
					theme: $(document).data("kendoSkin") || "default",
					dataSource: {
						transport: {
							read: {
								url: "bookmarkOccurrencesAsJSON?id=" + bookmarkId,
								dataType: "json"
							}
                        },                		
                        schema: {
                            data: function(data) {
                                return data.result;
                            }
                        }                    	
					},					
		            legend: {
				        visible: false
				    },				    
				    seriesDefaults: {
					    type: "line",		                
		                markers: {
		                    visible: false
		                },		                
		                opacity: .8,
		                line: { width: 1.5 }
					},
					axisDefaults: {
						majorGridLines: { visible: false }						
	                },					
					series: [{
						field: "occurs"		              
		            }],
		            valueAxis: {
			            labels: {
				            format: "{0}"
					    },
				    	visible: false,
				    	line: {
				            visible: false
				        }
		            },
		            categoryAxis: {
		            	line: {
				            visible: false
				        }
				    }				    				    
		         });
		         	        
		    }
		    					    		    		    			 		   		   
		</script>				
        
		<g:set var="entityName" value="${message(code: 'bookmarkLogEvent.label', default: 'BookmarkLogEvent')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>	
			
		<div id="list-logEvent" class="content scaffold-list" role="main">
			<h1>Bookmarks</h1>
			<div class="message">${flash.message}</div>
		</div>
		<div> 
			<g:form url="[action:'list', controller:'bookmarkLogEvent']">
				<div style="float: left;">
					<label for="applicationDropdown" style="margin-left: 30px">Application</label>
					<input id="applicationDropdown" name="currentApplication" 
						value="${currentApplication}" onchange="submit();"/>					
				</div>							
			</g:form>
			<!-- 
			<div style="float: left;">
				<label for="applicationDropdown" style="margin-left: 30px">Application</label>
				<input id="applicationDropdown" value="${currentApplication}"/>					
			</div>
			-->
						
		</div>		
				 
		<div id="bookmarkGrid"
			style="clear: left; float: left; margin: 5px; border-radius: 5px; height: auto; width: 98.8%;"></div>
			
		<div id="details"></div>																		
	</body>
</html>
