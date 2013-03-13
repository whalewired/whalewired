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
				           		url: function (app) {
					           		return "update"
					           	},
				            	dataType: "json",
				            	type: "POST"
				            },
				            destroy: {
				            	url: function (app) {
					           		return "delete"
					           	},
				                dataType: "json",
				                type: "POST"
				            },
				            create: {
				            	url: function (app) {
					           		return "create"
					           	},
				                dataType: "json",
					            type: "POST"
				            },
				            parameterMap: function(options, operation) {
				            	if (operation !== "read" && options.models) {
                                    return {model: kendo.stringify(options.models)};
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
                               id: "id",
                               fields: {
                                   id: {
                                	   editable: false,
                                   },
                                   name: {  
                                	   validation: { required: true },
                                       editable: true, 
                                   	   nullable: false 
                                   },
                                   ttl: { 
                                	   validation: { required: true },
                                	   editable: true,
                                       nullable: false 
                               	   } 
                               }
                           }
                       },
                       batch: true,   
                       pageSize: 20,
                       sort: { field: "name", dir: "asc" },
                       error: function(e) {
                           alert(e)
                           // handle event
                       }
                   },
                   height: 400,
                   editable: "popup",
                   edit:function(e) {
                       if(!e.model.isNew()) {
                    	   $('input[name=name]').attr('readonly','readonly');
                       }
                   },
                   sortable: true,
                   pageable: true,
                   toolbar: [{name: "create", text: "Create application" }],
                   columns: [{field:"name",title:"Name"},
                             {field:"ttl",title:"TTL"},
                   			 {command: ["edit", "destroy"], title: " ", width: "165px"}]
               });

				grid = $("#applicationTable").data("kendoGrid");
				
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
