package org.enguage.web;

import org.enguage.util.Audit;
import org.enguage.web.users.Users;

public class UserDelete {
	public static final String widget = 
			"<fieldset>\n"
			+"<legend>\n"
			+	"<label for='action'>Delete User</label>\n"
			+ "</legend>\n"
			+	"<form action='/deluser'>\n"
			+		"<label for='deluser'>Username:</label>\n"
			+		"<input type='text' id='deluser'>\n"
			+		"<input type='button' id='delbut' onclick='delUser()' value='Delete'>\n" 
			+	"</form>"
			+ "</fieldset>\n"
			+	"<script>function delUser() {"
			+		"var request =   '/delUser'+"
			+						"'?'+document.getElementById('deluser').value;"
			+		"var xhttp = new XMLHttpRequest();"
			+		"xhttp.onreadystatechange = function() {"
			+			"if (this.readyState == 4 && this.status == 200) {"
			+				"document.getElementById('output').innerHTML = this.responseText;"
			+			"}"
			+		"};"
			+		"xhttp.open('GET', request, true);"
			+		"xhttp.send();"
			+	"}"
			+ "</script><br>\n";

	public static String operation ( String[] params ) {
		
		Audit.log( "action... delUser" );
		
		String reply = "<strong>Delete User</strong><p>";
		if (Users.isUser( params[ 0 ])) {
			Users.delUser( params[ 0 ]);
			reply += "OK: Username "+ params[ 0 ] +" deleted";
		} else
			reply += "Sorry: Username "+ params[ 0 ]
					 + " not found</p>";
		return reply;
	}
}
