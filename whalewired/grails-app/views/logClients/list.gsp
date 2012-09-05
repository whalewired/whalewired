<!doctype html>
<html>
	<head>
		<meta name="layout" content="main" />
	</head>
	<body>
		<a href="#list-clients" class="skip" tabindex="-1"><g:message
				code="default.link.skip.label" default="Skip to content&hellip;" /></a>
		<div id="#list-clients" class="content scaffold-list" role="main">
			<h1>Client setup</h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">
					${flash.message}
				</div>
			</g:if>
			
			<h2>Log4j</h2>
			<div id="clients" style="clear: left; float: left; margin: 10px; border-radius: 5px; height: auto; width: 98.8%; padding-left: 10px; ">
			<g:each in="${fileResourceInstanceList}">
				<p>
					<g:formatDate format="dd-MM-yyyy HH:mm:ss" date="${new Date(it.lastModified())}"/>
					<a href="${resource(dir: 'client-files', file: it.getName())}" type="application/xml" >${it.getName()}</a>
			    </p>
			</g:each>
			<p>
				<pre style="margin-top: 15px; border-color: grey; border: 1px solid; width: 500px; padding: 5px;">
&lt;log4j:configuration&gt;

  &lt;appender name=&quot;WhaleWiredAppender&quot; class=&quot;com.whalewired.client.log4j.WhaleWiredAppender&quot;&gt;
  	&lt;param name=&quot;whalewired_es&quot; value=&quot;172.17.33.54&quot;/&gt;
  	&lt;param name=&quot;whalewired_es_port&quot; value=&quot;9200&quot;/&gt;
  	&lt;param name=&quot;log_account&quot; value=&quot;DEV_WL&quot;/&gt;
  	&lt;param name=&quot;log_application&quot; value=&quot;clfr.udv.optagelse.dk&quot;/&gt;
  	&lt;param name=&quot;log_host&quot; value=&quot;DEV_WL&quot;/&gt;
  	&lt;param name=&quot;max_threads&quot; value=&quot;1&quot;/&gt;
  &lt;/appender&gt;

  &lt;appender name=&quot;ConsoleAppender&quot; class=&quot;org.apache.log4j.ConsoleAppender&quot;&gt;
    &lt;param name=&quot;Threshold&quot; value=&quot;ERROR&quot;/&gt;
    &lt;layout class=&quot;org.apache.log4j.PatternLayout&quot;&gt;
      &lt;param name=&quot;ConversionPattern&quot; value=&quot;%d{yyyy-MM-dd HH:mm:ss} %-5p %c{2}(%M:%L) - %m%n&quot;/&gt;
    &lt;/layout&gt;
  &lt;/appender&gt;
  
  &lt;logger name=&quot;com.whalewired&quot;&gt;
    &lt;level value=&quot;DEBUG&quot;/&gt;
	&lt;appender-ref ref=&quot;WhaleWiredAppender&quot;/&gt;
  &lt;/logger&gt;

  &lt;root&gt; 
    &lt;appender-ref ref=&quot;ConsoleAppender&quot;/&gt;
  &lt;/root&gt;
  
&lt;/log4j:configuration&gt;
				</pre>
			</p>			
			
			
			</div>
		</div>
	
	</body>
</html>
