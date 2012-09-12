
<%@ page import="com.whalewired.BookmarkLogEvent" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">		
		
		<!-- Custom script for bookmark grid -->
		<g:javascript src="bookmark/bookmark.js" />
		<script type="text/javascript">
			$(document).ready(function() {
				var columns = [ 
				  			{field: "dateCreated", 
								title: "Date", 
								width: 150, 							
								template: '#=(kendo.toString(new Date(dateCreated),"dd-MM-yyyy HH:mm:ss"))#'},
					    	{field: "description",  
					            title: "Description"},                    	                     	
					    	{field: "id", 
					            title: "Trend", 
					            width: 160, 
					            template: '<div id="chart#=id#" style="display: table-cell; width: 150px; height: 50px"></div>'},
					        {field: "id", 
					            title: " ", 
					            width: 70,                     	                    	                    
					            template:'<a style="cursor: pointer; text-decoration: underline; color: blue" onclick="return showDetails(#=parseInt(id)#)">Details</a>'},
					        {field: "id", 
					            title: " ", 
					            width: 70,                     	                    	                    
					            template:'<a style="cursor: pointer; text-decoration: underline; color: blue" href="delete?id=#=escape(id)#">Delete</a>'}
					    ];
				createBookmarkTable("bookmarkGrid", "applicationDropdown", "${currentApplication}", columns);
			});
		</script>
		
		<g:set var="entityName" value="${message(code: 'bookmarkLogEvent.label', default: 'BookmarkLogEvent')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>	
			
		<div id="list-logEvent" class="content scaffold-list" role="main">
			<h1>Bookmarks</h1>
			
			<g:if test="${flash.message}">
				<div class="message" role="status">
					${flash.message}
				</div>
			</g:if>
			<div> 
				<g:form url="[action:'list', controller:'bookmarkLogEvent']" style="margin: 0px 0px 0px 0px;">
					<div class="optionPanel" style="margin: 0px 0px 10px;">
						<label for="applicationDropdown" style="margin-left: 10px">Application</label><br />
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
		</div>
	</body>
</html>
