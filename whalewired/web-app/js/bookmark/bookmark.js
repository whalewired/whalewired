/**
 *	This JavaScript file is used to render KendoGrid and ApplicationDropDown for showing booksmarks.
 *	It uses the BookmarkController.
 *	
 *	Call the createBookmarkTable function to use code.
 */




/**
 * Creates the Bookmark Table and ApplicationDropDown field
 * 
 * @param gridId		A string with the id of the div which shall be the Bookmark table
 * @param dropDownId 	A string with the id of the input which shall be the ApplicationDropDown
 * @param currentApplication	String with name of the application which shall shall be shown in grid
 * @param columnArray	Array with configuration of columns in the Bookmark table
 */
function createBookmarkTable(gridId, dropDownId, currentApplication, columns) {
	
	
	var grid = $("#"+gridId).kendoGrid({
		scrollable: false,
		
	    dataSource: {
	    	transport: {
	            // read: "allBookmarksAsJSON",
	            read: "../bookmarkLogEvent/bookmarksAsJSON?currentApplication="+currentApplication,                            
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
	    columns: columns,
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
	
	
	var dropdown = $("#"+dropDownId).kendoDropDownList({
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
	
}

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
					url: "../bookmarkLogEvent/bookmarkOccurrencesAsJSON?id=" + bookmarkId,
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