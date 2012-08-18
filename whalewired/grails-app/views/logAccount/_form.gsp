<%@ page import="com.whalewired.LogAccount" %>



<div class="fieldcontain ${hasErrors(bean: logAccountInstance, field: 'accountName', 'error')} ">
	<label for="accountName">
		<g:message code="logAccount.accountName.label" default="Account Name" />
		
	</label>
	<g:textField name="accountName" value="${logAccountInstance?.accountName}"/>
</div>

