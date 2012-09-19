<!doctype html>
<html>
	<head>
		<!-- Loading jQuery UI (is used in dashboard.js) -->
		<g:javascript src="jquery-ui-1.8.23.custom.min.js" />
		
		<!-- Loading jQuery Cookie for remembering boxes positions on dashboard -->
		<g:javascript src="jquery.cookie.js" />
		
		<!-- Custom script for dashboard -->
		<g:javascript src="dashboard/chartErrors.js" />
		
		<!-- Custom script for Charting Errors -->
		<g:javascript src="dashboard/dashboard.js" />
		
		<!-- Handling buttons -->
		<g:javascript src="dashboard/buttons.js" />
		
		<meta name="layout" content="main">		
		<title>Dashboard</title>
		
		<!-- Load bookmark list scripts -->
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
					createApplicationDropdown("createErrorGraphDropdown");
					
					createErrorChart(400);
			});
		</script>
	</head>
	<body>
	
	
	<div class="row-fluid">
		<div class="btn-group">
			<button class="btn" type="button" data-toggle="dropdown">
				<i class="icon-plus"></i>Add box<span class="caret"></span>
			</button>
		  <ul class="dropdown-menu">
		    <li><a href="#" class="dashboardButton" title="addErrorGraphBox"><i class="icon-exclamation-sign"></i> Error graph</a></li>
		    <li><a href="#"><i class="icon-bookmark"></i> Bookmarks</a></li>
		  </ul>
		</div>		
	</div><br />
	
	
	 <div id="box-config-modal" class="modal hide fade in" style="display: none;">
      <div class="modal-header">
        <button class="close" data-dismiss="modal">×</button>
        <h3>Adjust widget</h3>
      </div>
      <div class="modal-body">
        <p>This part can be customized to set box content specifix settings!</p>
      </div>
      <div class="modal-footer">
        <a href="#" class="btn btn-primary" data-dismiss="modal">Save Changes</a>
        <a href="#" class="btn" data-dismiss="modal">Cancel</a>
      </div>
    </div>
	
	
	
	

      <div class="row-fluid">
      	 <!-- Portlet Set 1 -->
         <div class="span4 column" id="col1" style="min-width: 225px;">
         	 <!-- Portlet: Browser Usage Graph -->
             <div class="box" id="box-0">
              <h4 class="box-header round-top">pisk.udv.optagelse.dk
                  <!-- <a class="box-btn" title="close"><i class="icon-remove"></i></a> -->
                  <a class="box-btn" title="toggle"><i class="icon-minus"></i></a>     
                  <!-- <a class="box-btn" title="config" data-toggle="modal" href="#box-config-modal"><i class="icon-cog"></i></a> -->
              </h4>         
              <div class="box-container-toggle">
                  <div class="box-content">
                    <input type="hidden" val="pisk.udv.optagelse.dk">
                    <div class="chartErrorOnApp"></div>                    
                  </div>
              </div>
            </div><!--/span-->
            

         </div>
         

      	<div class="span4 column" id="col2" style="min-width: 225px;">
                      <!-- Portlet: Page Visit Graph -->
             <div class="box" id="box-1">
              <h4 class="box-header round-top">tthi2.udv.ww.dk
                 <!-- <a class="box-btn" title="close"><i class="icon-remove"></i></a> -->
                  <a class="box-btn" title="toggle"><i class="icon-minus"></i></a>     
                 <!-- <a class="box-btn" title="config" data-toggle="modal" href="#box-config-modal"><i class="icon-cog"></i></a> -->
              </h4>         
              <div class="box-container-toggle">
                  <div class="box-content">
                  	   <input type="hidden" val="tthi2.udv.ww.dk">
                    <div class="chartErrorOnApp"></div>    
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
               
		</div>
		
		
		
		
	<!-- Dialog -->
	
	<!-- Add Error Graph box -->
	<div id="box-add-errorchart-dialog" class="modal hide fade in" style="display: none;">
      <div class="modal-header">
        <button class="close" data-dismiss="modal">×</button>
        <h3>Add Error Graph to Dashboard</h3>
      </div>
      <div class="modal-body">
        <p>This box show a graph with occurences of errors through time.
        	<div class="optionPanel" style="margin: 0px 0px 10px;">
						<label for="createErrorGraphDropdown" style="margin-left: 10px">Application</label>: 
						<input id="createErrorGraphDropdown" name="currentApplication" 
							value="${currentApplication}"/>					
					</div>
        </p>
      </div>
      <div class="modal-footer">
        <a href="#" title="addErrorGraph" class="btn dashboardButton btn-primary" data-dismiss="modal">Add</a>
        <a href="#" class="btn" data-dismiss="modal">Cancel</a>
      </div>
    </div>
		
	
	
	</body>
	
</html>