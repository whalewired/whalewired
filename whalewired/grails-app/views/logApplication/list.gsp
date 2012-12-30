<!doctype html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Applications</title>
		<script type="text/javascript">

	    	var grid;
			
           	$(document).ready(function() {
              $("#applicationTable").kendoGrid({
                   dataSource: {
                       type: "json",
                       transport: {
                       		read: "listJSON",
				           	update: {
				           		url: "update",
				            	dataType: "json",
				            	type: "POST"
				            },
				            destroy: {
				            	url: "delete",
				                dataType: "json",
				                type: "POST"
				            },
				            create: {
				            	url: "create",
				                dataType: "json",
					            type: "POST"
				                 
				            },
				            parameterMap: function(options, operation) {
				            	if (operation !== "read") {
				                	return {name: options.name, ttl: options.ttl};
				                }
				            }                               
                       },
                       schema: {
                           data: function(data) {
                               return data.result;
                           },
                           total: function(data) {
                               return data.total;
                           },
                           model: {
                               id: "name",
                               fields: {
                                   name: {  
                                       editable: {
                                       		create: true,
                                       		update: true
                                   		}, 
                                   		nullable: false 
                                   },
                                   ttl: { 
                                	   editable: {
                                      		create: true,
                                      		update: true
                                       }
                               	   } 
                               }
                           }
                       },
                       batch: false,   
                       autoSync: false,                    
                       pageSize: 20,
                       serverPaging: false,
                       serverSorting: false,
                       sort: { field: "name", dir: "asc" },
                       serverFiltering: false,
                       error: function(e) {
                           // handle event
                       }
                   },
                   editable: {
                       destroy: true, 
                   	   confirmation: "Are you sure you want to remove the application? \n\nRemember to press Save, when finished."
                   },
                   sortable: {
                       mode: "multiple", 
                       allowUnsort: true
               	   },
                   groupable: false,
                   pageable: true,
                   navigatable: true,
                   selectable: "multiple, row",                   
                   toolbar: [{name: "create", text: "Create application" }, {name: "save", text: "Save changes" }, "cancel"],
                   columns: [{field:"name",title:"Name"},
                             {field:"ttl",title:"TTL"},
                   			 {command: "destroy", title: " ", width: "110px"}]
               });

				grid = $("#applicationTable").data("kendoGrid");
				$("#applicationTable").height("auto"); // IE9 fix
              
              });
		</script>
	</head>
	<body>
		<div id="list-Apps" class="content scaffold-list" role="main">
			<h1>Application list</h1>
			<g:if test="${flash.message}">
			 <div class="message" role="status">${flash.message}</div>
			</g:if>
			<div id="applicationTable" style="clear: left; float: left; margin: 5px; border-radius: 5px; height: auto; width: 98.8%;"></div>			
		</div>
	</body>
</html>
