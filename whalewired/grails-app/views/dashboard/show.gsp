<!doctype html>
<html>
	<head>
		<!-- Loading jQuery UI (is used in dashboard.js) -->
		<g:javascript src="jquery-ui-1.8.23.custom.min.js" />
		
		<!-- Loading jQuery Cookie for remembering boxes positions on dashboard -->
		<g:javascript src="jquery.cookie.js" />
		
		<!-- Custom script for dashboard -->
		<g:javascript src="dashboard.js" />
		
		
		<meta name="layout" content="main">		
		<title>Dashboard</title>
		
		<g:javascript src="bookmark/bookmark.js" />
		<script type="text/javascript">
			$(document).ready(function() {
				var columns = [ 
						    	{field: "description",  
						            title: "Description"},                    	                     	
						    	{field: "graph", 
						            title: "Trend", 
						            width: 150, 
						            template: '<div id="chart#=id#" style="display: table-cell; width: 150px; height: 50px"></div>'}
						    ];
					createBookmarkTable("bookmarkGrid", "applicationDropdown", "${currentApplication}", columns);
			});
		</script>
	</head>
	<body>
	

      <div class="row-fluid">
      	 <!-- Portlet Set 1 -->
         <div class="span4 column" id="col1" style="min-width: 225px;">
         	 <!-- Portlet: Browser Usage Graph -->
             <div class="box" id="box-0">
              <h4 class="box-header round-top">Browser Usage Graph
                  <!-- <a class="box-btn" title="close"><i class="icon-remove"></i></a> -->
                  <a class="box-btn" title="toggle"><i class="icon-minus"></i></a>     
                  <!-- <a class="box-btn" title="config" data-toggle="modal" href="#box-config-modal"><i class="icon-cog"></i></a> -->
              </h4>         
              <div class="box-container-toggle">
                  <div class="box-content">
                    Dashboard box 1
                  </div>
              </div>
            </div><!--/span-->
            

         </div>
         

      <div class="span4 column" id="col2" style="min-width: 225px;">
                      <!-- Portlet: Page Visit Graph -->
             <div class="box" id="box-1">
              <h4 class="box-header round-top">Page Visit Graph
                 <!-- <a class="box-btn" title="close"><i class="icon-remove"></i></a> -->
                  <a class="box-btn" title="toggle"><i class="icon-minus"></i></a>     
                 <!-- <a class="box-btn" title="config" data-toggle="modal" href="#box-config-modal"><i class="icon-cog"></i></a> -->
              </h4>         
              <div class="box-container-toggle">
                  <div class="box-content">
                  	Dashboard box 2
                  </div>
              </div>
            </div><!--/span-->
         </div>
         
         
         <!-- Portlet Set 2 -->
         <div class="span4 column" id="col3" style="min-width: 225px;">
             <!-- Portlet: Site Activity Gauges -->
             <div class="box" id="box-2">
              <h4 class="box-header round-top">Bookmarks
                  <!-- <a class="box-btn" title="close"><i class="icon-remove"></i></a> -->
                  <a class="box-btn" title="toggle"><i class="icon-minus"></i></a>     
                  <!-- <a class="box-btn" title="config" data-toggle="modal" href="#box-config-modal"><i class="icon-cog"></i></a> -->
              </h4>         
              <div class="box-container-toggle">
                  <div class="box-content" style="overflow: hidden;">
                  <g:form url="[action:'show', controller:'dashboard']" style="margin: 0px 0px 0px 0px;">
					<div class="optionPanel" style="margin: 0px 0px 0px 0px;">
						<label for="applicationDropdown" style="margin-left: 10px">Application</label>
						<input id="applicationDropdown" name="currentApplication" 
							value="${currentApplication}" onchange="submit();"/>					
					</div>							
				</g:form>
                    <div id="bookmarkGrid" style="clear: left; float: left; margin: 5px; border-radius: 5px; height: auto; width: 98.8%;"></div>
                  </div>
              </div>
            </div><!--/span-->         
         </div>
	
	</body>
	
</html>