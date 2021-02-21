package org.enguage.web;

import org.enguage.web.users.Users;

public class UserAdd {
	public static final String widget =
			"<fieldset>\n"
			+ "<legend>Add User</legend>\n"
			+ "	<form action='/addUser'>\n"
			+ "		<label for='adduid'>Username:</label> "
			+ "		<input type='text' id='adduid'>&nbsp;"
			+ "		<input type='checkbox' id='addadm'  value='admin'>"
			+ "		<label for='addadm'>Admin</label><br>"
			+ "		<label for='addpwd'>Password:</label>&nbsp;"
			+ "		<input type='text' id='addpwd''>"
			+ "		<input type='button' id='add' onclick='addUser()' value='Add User'>\n" 
			+ "	</form>"
			+ "</fieldset>\n"
			+"<script>"
			+ 	"function addUser() {"
			+ 		"var request =   '/addUser'"
			+ 						"+ '?'+document.getElementById('adduid').value"
			+ 						"+ '&'+document.getElementById('addpwd').value"
			+ 						"+ '&'+document.getElementById('addadm').checked;"
			+ 		"document.cookie=\"param=\"+document.getElementById('addpwd').value;"
			+ 		"var xhttp = new XMLHttpRequest();"
			+ 		"xhttp.onreadystatechange = function() {"
			+ 			"if (this.readyState == 4 && this.status == 200) {"
			+ 				"document.getElementById('output').innerHTML = this.responseText;"
			+ 			"}"
			+ 		"};"
			+ 		"xhttp.open('GET', request, true);"
			+ 		"xhttp.send();"
			+ 	"}"
			+ "</script><br>\n";
	
	public static String operation( String[] params ) {
		//Audit.log( "adduser" );
		return "<strong>Add User</strong><p>"
			+ (Users.addUser(
				params[ 0 ],
				params[ 1 ],
				params[ 2 ].equals( "true" )
			 ) ?
				"OK: "   +(params[ 2 ].equals( "true" )?"admin":"user")+" "+ params[ 0 ] +" added" :
				"Sorry: Username "+ params[ 0 ] +" already exists")
			+ "</p>";
}	}
