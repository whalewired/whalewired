<!doctype html>
<html>
<head>
	<meta name="layout" content="main" />
		<script type="text/javascript">
			var interval = 'LAST_1_HOUR';
			$(document).ready(function() {
		   		setTimeout(function() {
			   		createTimeFilter();
		    		createChart(interval);
		    			// Initialize the chart with a delay to make sure
			    		// the initial animation is visible
			    		}, 400);	    			

		    		$(document).bind("kendo:skinChange", function(e) {
			    		createChart(lastNumberOfDays);
			    	});
		    	}
	    	);
	    	
			function refreshChart(interval) {
	   			/**
	   				Would like to configure url on the fly like:
	   			
			   		$("#chart").data("kendoChart").dataSource.transport.read.url = "..."

		    		but cannot get it to work. Creating a new chart every time lastNumberOfDays is changed in the text field seems a bit 
		    		overkilled, but is so far the only working solution.  	    		
	   			*/	    		
	   			// createChart(getLastNumberOfDays());
				createChart(interval);				
	   			$("#chart").data("kendoChart").refresh();
			}

			
			$(window).resize(function() {
				//on resizing window, then refresh chart
				$("#chart").data("kendoChart").refresh();
			});
			
			function getLastNumberOfDays() {
				return document.getElementById('lastNumberOfDays').value;
			}
			
			
			function createChart(interval) {
				$("#chart").kendoChart({
					theme: $(document).data("kendoSkin") || "default",
					dataSource: {
						transport: {
							read: {
								url: "../occurrencesAsJSON?id=${logEventInstance?._id}&interval=" + interval,
								dataType: "json"
							}
                    	}
					},
					/*
					title: {
						text: "Occurrences of the same event pattern using pattern"
		        	},
		        	*/
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
						
			
			var intervalItems = new kendo.data.DataSource({data:[ 
			        
				{ value: "LAST_1_HOUR", text: "1 hour" }, 
				{ value: "LAST_3_HOURS", text: "3 hours" },  
				{ value: "LAST_6_HOURS", text: "6 hours" }, 
				{ value: "LAST_12_HOURS", text: "12 hours" },  
				{ value: "LAST_1_DAY", text: "1 day" },
				{ value: "LAST_2_DAYS", text: "2 days" },
				{ value: "LAST_3_DAYS", text: "3 days" },
				{ value: "LAST_5_DAYS", text: "5 days" } 
				
				/*
				{ value: "1 hour", text: "1 hour" }, 
				{ value: "3 hours", text: "3 hours" },  
				{ value: "6 hours", text: "6 hours" }, 
				{ value: "12 hours", text: "12 hours" },  
				{ value: "1 day", text: "1 day" },
				{ value: "2 days", text: "2 days" },
				{ value: "3 days", text: "3 days" },
				{ value: "5 days", text: "5 days" }
				*/
			]});

			function createTimeFilter() {
				$("#timeFilter").kendoDropDownList({
	                optionLabel: "Select An Option",
	                dataSource: intervalItems,
	                dataTextField: "text",
	                dataValueField: "value",	                
	                change: function() {
	                    interval = this.value();
	                    refreshChart(interval);	                	 
	                }		            
	            });
			}

            					
		</script>
		<g:set var="entityName" value="${message(code: 'logEvent.label', default: 'log event')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>
	<div id="show-logEvent" class="content scaffold-show" role="main">
		<div>
			<h1>
				<g:message code="default.show.label" args="[entityName]" />
			</h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">
					${flash.message}
				</div>
			</g:if>

			<g:if test="${!externalIssue}">
				<button id="createIssueButton" class="k-button"
					style="float: right; position: relative; bottom: 28px; right: 13px;">Create
					issue in JIRA</button>
			</g:if>

			<g:if test="${!logEventInstance?.bookmarkId}">
				<button id="createBookmarkButton" class="k-button"
					style="float: right; position: relative; bottom: 28px; right: 30px;">Bookmark</button>
			</g:if>
			
		</div>
		<div style="width: 100%; display: table;">
			<ol class="property-list logEvent"
				style="display: inline-block; width: 60%;">
				<g:if test="${logEventInstance?.accountName}">
					<li class="fieldcontain">
						<span id="accountName-label" class="property-label"><g:message
								code="logEvent.accountName.label" default="Account Name" /></span> 
						<span class="property-value" aria-labelledby="accountName-label">
							${logEventInstance?.accountName}</span>
					</li>
				</g:if>

				<g:if test="${logEventInstance?.applicationName}">
					<li class="fieldcontain"><span id="applicationName-label"
						class="property-label"><g:message
								code="logEvent.applicationName.label" default="Application Name" /></span>

						<span class="property-value"
						aria-labelledby="applicationName-label">
							${logEventInstance?.applicationName}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.hostName}">
					<li class="fieldcontain"><span id="hostName-label"
						class="property-label"><g:message
								code="logEvent.hostName.label" default="Host Name" /></span> <span
						class="property-value" aria-labelledby="hostName-label">
							${logEventInstance?.hostName}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logLevel}">
					<li class="fieldcontain"><span id="logLevel-label"
						class="property-label"><g:message
								code="logEvent.logLevel.label" default="Level" /></span> <span
						class="property-value" aria-labelledby="logLevel-label">
							${logEventInstance?.logLevel}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logTime}">
					<li class="fieldcontain"><span id="logTime-label"
						class="property-label">Time</span> <span class="property-value"
						aria-labelledby="logTime-label"><g:formatDate
								date="${new Date(logEventInstance?.logTime)}"
								format="dd-MM-yyyy HH:mm:ss" /></span></li>
				</g:if>

				<g:if test="${logEventInstance?.loggerName}">
					<li class="fieldcontain"><span id="loggerName-label"
						class="property-label"><g:message
								code="logEvent.loggerName.label" default="Logger Name" /></span> <span
						class="property-value" aria-labelledby="loggerName-label">
							${logEventInstance?.loggerName}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logThrowableType}">
					<li class="fieldcontain"><span id="logThrowableType-label"
						class="property-label"><g:message
								code="logEvent.logThrowableType.label" default="Throwable type" /></span>

						<span class="property-value"
						aria-labelledby="logThrowableType-label">
							${logEventInstance?.logThrowableType}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logThrowableLocation}">
					<li class="fieldcontain"><span id="logThrowableLocation-label"
						class="property-label"><g:message
								code="logEvent.logThrowableLocation.label"
								default="Throwable Location" /></span> <span class="property-value"
						aria-labelledby="logThrowableLocation-label">
							${logEventInstance?.logThrowableLocation}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logMessage}">
					<li class="fieldcontain"><span id="logMessage-label"
						class="property-label"><g:message
								code="logEvent.logMessage.label" default="Message" /></span> <span
						class="property-value" aria-labelledby="logMessage-label">
							${logEventInstance?.logMessage}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logLocation}">
					<li class="fieldcontain"><span id="logLocation-label"
						class="property-label"><g:message
								code="logEvent.logLocation.label" default="Location" /></span> <span
						class="property-value" aria-labelledby="logLocation-label">
							${logEventInstance?.logLocation}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logThread}">
					<li class="fieldcontain"><span id="logThread-label"
						class="property-label"><g:message
								code="logEvent.logThread.label" default="Thread" /></span> <span
						class="property-value" aria-labelledby="logThread-label">
							${logEventInstance?.logThread}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.logThrowableTrace}">
					<li class="fieldcontain"><span id="logThrowableTrace-label"
						class="property-label"><g:message
								code="logEvent.logThrowableTrace.label" default="Trace" /></span> <span
						class="property-value" aria-labelledby="logThrowableTrace-label">
							${logEventInstance?.logThrowableTrace}
					</span></li>
				</g:if>

				<g:if test="${logEventInstance?.clientVersion}">
					<li class="fieldcontain"><span id="logThrowableTrace-label"
						class="property-label">Client version</span> <span
						class="property-value">
							${logEventInstance?.clientVersion}
					</span></li>
				</g:if>
			</ol>

			<g:if test="${externalIssue}">
				<r:script disposition="head">
						$(document).ready(function() {
							$('#type').kendoDropDownList();
							$('#priority').kendoDropDownList();
							$('#environment').kendoDropDownList();
							
							$('#createIssuePanel').hide();
							$('#createIssuePanel').show('slow');
						});
					</r:script>
					
				<div id="createIssuePanel" class="createIssuePanel ">
					<g:form name="createIssueForm" action="createIssue">
						<label for="summary">Summary</label>
						<input type="text" name="summary" disabled="disabled"
							value="${externalIssue.summary}" />

						<label for="type">Type</label>
						<g:select name="type" from="${types}" disabled="${true}"
							optionValue="name" value="${externalIssue.type}" />

						<label for="priority">Priority</label>
						<g:select name="priority" from="${priority}" disabled="${true}"
							optionValue="name" value="${externalIssue.priority}" />

						<label for="environment">Environment</label>
						<g:select name="environment" from="${environment}"
							disabled="${true}" optionValue="name"
							value="${externalIssue.environment}" />

						<label for="description">Description</label>
						<textarea name="description" disabled="disabled" rows="4"
							rows="50" required>
							${externalIssue.description}
						</textarea>
					</g:form>
				</div>
			</g:if>
			<g:else>
				<r:script disposition="head">
						$(document).ready(function() {
							$('#createIssuePanel').hide();
							
							$('#createIssueButton').click(function() {
								$('#createIssuePanel').show('slow');
								$('#createIssueButton').hide('slow');
							});
							
							$('#cancelCreateIssueButton').click(function() {
								$('#createIssueButton').show('fast');
								$('#createIssuePanel').hide('fast');
								return false;
							});
							
							var typeDropdown = $('#type').kendoDropDownList().data('kendoDropDownList');
							$('#environment').kendoDropDownList().data('kendoDropDownList');
							typeDropdown.bind("change", function() {
								if ($('#type').val() == 'BUG') {
									$('#environment').closest(".k-widget").show();
									$('#environmentLabel').show();
								} else {
									$('#environment').closest(".k-widget").hide();
									$('#environmentLabel').hide();
								}
							});							
							
							$('#priority').kendoDropDownList();
							
							var validator = $('#createIssueForm').kendoValidator().data('kendoValidator');
							$('#okCreateIssueButton').click(function() {
								return validator.validate();
							});
							
						});
					</r:script>

				<div id="createIssuePanel" class="createIssuePanel">
					<g:form name="createIssueForm" action="createIssue">
						<label for="summary">Summary</label>
						<input type="text" name="summary" required />

						<label for="type">Type</label>
						<g:select name="type" from="${types}" optionValue="name" />

						<label for="priority">Priority</label>
						<g:select name="priority" from="${priority}" optionValue="name" />

						<label id="environmentLabel" for="environment">Environment</label>
						<g:select name="environment" from="${environment}"
							optionValue="name" />

						<label for="description">Description</label>
						<textarea name="description" rows="4" rows="50" required></textarea>

						<g:submitButton name="okCreateIssueButton" value="Ok"
							class="k-button" style="float: right; margin: 5px 5px;" />
						<button id="cancelCreateIssueButton" class="k-button"
							style="float: right; margin: 5px 5px;">Cancel</button>

						<g:hiddenField name="logEventId" value="${logEventInstance?._id}" />
					</g:form>
				</div>
			</g:else>

			<r:script disposition="head">
				$(document).ready(function() {
					$('#createIssuePanel').hide();						
					$('#createBookmarkPanel').hide();
					
					$('#createBookmarkButton').click(function() {
							$('#createBookmarkPanel').show('slow');
							$('#createBookmarkButton').hide('slow');
					});
									
					$('#cancelCreateBookmarkButton').click(function() {
						$('#createBookmarkButton').show('fast');
						$('#createBookmarkPanel').hide('fast');
						return false;
					});																													
				});
			</r:script>

			<div id="createBookmarkPanel" class="createIssuePanel">
				<g:form name="createBookmarkForm"
					url="[action:'create', controller:'bookmarkLogEvent']">
					<label for="description">Description</label>
					<input type="text" name="description" />

					<g:submitButton name="okCreateBookmarkButton" value="Ok"
						class="k-button" style="float: right; margin: 5px 5px;" />
					<button id="cancelCreateBookmarkButton" class="k-button"
						style="float: right; margin: 5px 5px;">Cancel</button>

					<g:hiddenField name="id" value="${logEventInstance?._id}" />
					<g:hiddenField name="currentApplication"
						value="${currentApplication}" />
				</g:form>
			</div>
			
		</div>
	</div>

	<!-- Chart -->
	<div class="chart-wrapper" style="padding: 20px 0px 0px 0px;">
		<label for="lastNumberOfDays" style="margin-left: 10px">
			Select number of last hours or days to see occurrences for the same event: </label>
		<!-- 
		<input id="lastNumberOfDays" type="text" size="2"
			onkeyup="refreshChart();" value="7" />
		-->
		<!-- <div id="timeFilter"></div>-->
		<input id="timeFilter" name="timeFilter" value="LAST_1_HOUR"/>
		<div id="chart"></div>
		
	</div>

</body>
</html>
