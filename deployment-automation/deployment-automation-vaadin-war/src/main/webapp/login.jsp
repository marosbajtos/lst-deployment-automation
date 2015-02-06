<%@ page contentType="text/html;charset=UTF-8" session="false" import="com.lst.deploymentautomation.vaadin.util.Constants"%><%
	if (request.getRemoteUser() != null) {
		response.sendRedirect("ui");
		return;
	}
%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cookie['l-locale-cookie'].value}"/>
<fmt:setBundle basename="com.whitestein.lsps.vaadin.webapp.localization"/>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
	<title>Process Application</title>

	<link rel="icon" type="image/vnd.microsoft.icon" href="/lsps-vaadin/VAADIN/themes/custom/favicon.ico">
	<link rel="shortcut icon" type="image/vnd.microsoft.icon" href="/lsps-vaadin/VAADIN/themes/custom/favicon.ico">
	<script>
	function loadcssfile(filename){
		  var fileref=document.createElement("link");
		  fileref.setAttribute("rel", "stylesheet");
		  fileref.setAttribute("type", "text/css");
		  fileref.setAttribute("href", filename);
		  document.getElementsByTagName("head")[0].appendChild(fileref);
	}
		var storage = window.localStorage;
		var theme = "<%=Constants.DEFAULT_THEME%>";
		var themes = new Array(<%
				boolean first = true;
				for(String s: Constants.THEMES){
					if(first){
						out.print("\""+s+"\"");
						first = false;
					}
					else out.print(",\""+s+"\"");
				}
		%>);
		if(typeof(Storage)!=='undefined'){
			var item = storage.getItem("deployment-automationlication-theme-name");
			if(item != undefined){
				if(themes.indexOf(item)!=-1){				
					theme = item;
				}
			}
		}
		storage.setItem("deployment-automationlication-theme-name", theme);
		loadcssfile("VAADIN/themes/" + theme + "/styles.css"); //dynamically load and add this .css file
	</script>
	<style>
		html, body {
			margin: 0;
			padding: 0;
			height: 100%;
		}
	</style>
</head>
 
<body>
<script>document.body.className = theme + " login-page v-generated-body"</script>

<!-- Vaadin-Refresh -->
<div class="v-app">
<div class="v-ui fixed">

	<div id="container-head">
		<div id="head">
			<h1>Living Systems Process Application</h1>
			<span></span>
		</div>
	</div>
    
<!-- CONTENT START --> 
<div id="container-content" style="position: absolute; top: 40%; left: 0; margin-top: -50px; width: 100%">
<form name="loginform" class="open" action="j_security_check" method="post" style="margin: 0 auto; width: 310px">
<div id="loginHeader" style="display:none;"></div>
<table class="v-formlayout-margin-top v-formlayout-margin-bottom v-formlayout-spacing">
<tbody>
<tr class="v-formlayout-row v-formlayout-firstrow">
	<td class="v-formlayout-captioncell"><div class="v-caption">
		<label for="username" onclick=""><fmt:message key="user_login"/></label>
		<span class="v-required-field-indicator">*</span>
	</div></td>
	<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator">
		<% if (request.getParameterMap().containsKey("failed")) { %><div class="v-errorindicator" title="Login failed.">&nbsp;</div><% } %>
	</div></td>
	<td class="v-formlayout-contentcell" width="100%">
		<input type="text" placeholder="<fmt:message key="user_login"/>" class="v-textfield v-widget" name="j_username" id="username" autofocus="autofocus" autocapitalize="off" onkeypress="var code=event.keyCode||event.which;if(code===13){document.forms['loginform'].classList.remove('open'); setTimeout(function(){document.forms['loginform'].submit()},200);}return true" />
	</td>
</tr>
<tr class="v-formlayout-row">
	<td class="v-formlayout-captioncell"><div class="v-caption">
		<label for="password" onclick=""><fmt:message key="user_password"/></label>
		<span class="v-required-field-indicator">*</span>
	</div></td>
	<td class="v-formlayout-errorcell"><div class="v-formlayout-error-indicator"></div></td>
	<td class="v-formlayout-contentcell <% if (request.getParameterMap().containsKey("failed")) { %>error<% } %>" width="100%">
		<input type="password" placeholder="<fmt:message key="user_password"/>" class="v-textfield v-widget" name="j_password" id="password" onkeypress="var code=event.keyCode||event.which;if(code===13){document.forms['loginform'].classList.remove('open'); setTimeout(function(){document.forms['loginform'].submit();},200);}return true" />
	</td>
</tr>
<tr class="v-formlayout-row v-formlayout-lastrow">
	<td class="v-formlayout-captioncell"></td>
	<td class="v-formlayout-errorcell"></td>
	<td class="v-formlayout-contentcell" width="100%">
		<div class="v-button" tabindex="0" onclick="setTimeout(function(){document.forms['loginform'].submit();},200); document.forms['loginform'].classList.remove('open'); " onkeypress="var code=event.keyCode||event.which;if(code===13){setTimeout(function(){document.forms['loginform'].submit();},200); document.forms['loginform'].classList.remove('open');}">
			<span class="v-button-wrap">
				<span class="v-button-caption"><fmt:message key="signup"/></span>
			</span>
		</div>
	</td>
</tr>
</tbody>
</table>
	
</form>
</div>

<!-- CONTENT END -->
	<div id="container-foot">
		<div id="foot">
			<fmt:message key="lspsRelease"/> ${lsps.release.version} &#160; (<fmt:message key="lspsBuild"/> ${project.version}) <!-- Revision ${buildNumber} --><br />
			&#0169; ${lsps.copyrightYears} Whitestein Technologies AG. <fmt:message key="allRightsReserved"/>
		</div>
	</div>

</div>
</div>
</body>
</html>
