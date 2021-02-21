package org.enguage.web;

public class Login {
	public static String widget =
			"<fieldset>\n"
			+ "	<legend>Login:</legend>\n"
			+ "	<form action='/login'>\n"
			+ "		<label for='user name'>User Name:</label>\n"
			+ "		<input type='text' id='user name' name='username'><br>\n"
			+ "		<label for='password'>Password:&nbsp;&nbsp;&nbsp;</label>\n"
			+ "		<input type='password' id='password' name='password'> &nbsp;\n"
			+ "		<input type='submit' onclick='hidePwd()' value='Login'>\n"
			+ "	</form>\n"
			+ "</fieldset>\n"
			+ "<script>"
			+ "function hidePwd() {"
			+ "	document.cookie=\"param='\"+ document.getElementById('password').value +\"'\";\n"
			+ "	document.getElementById('password').value='*';\n"
			+ "}"
			+ "</script>";
}
