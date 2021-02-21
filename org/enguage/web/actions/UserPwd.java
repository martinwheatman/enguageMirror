package org.enguage.web.actions;

import org.enguage.util.Audit;
import org.enguage.web.users.Users;

public class UserPwd {
	public static final String widget =
			"<fieldset>\n"
			+ 	"<legend>Set Password</legend>\n"
			+ 	"<form action='/setPwd'>\n"
			+ 		"<label for='setuid'>Username:</label>\n"
			+ 		"<input type='text' id='setuid' name='setuid'><br>\n"
			+ 		"<label for='setpwd'>Password:</label>\n"
			+ 		"<input type='text' id='setpwd' name='setpwd'>\n"
			+ 		"<input type='button' id='setbut' onclick='setPwd()' value='Set'>\n"
			+ "	</form>\n"
			+ "</fieldset>\n"
			+ "<script>function setPwd() {\n"
			+ "	var request = '/setPwd?'\n"
			+ "			    +document.getElementById('setuid').value"
			+ "			+'&'+document.getElementById('setpwd').value;\n"
			+ "	var xhttp = new XMLHttpRequest();\n"
			+ "	xhttp.onreadystatechange = function() {\n"
			+ "		if (this.readyState == 4 && this.status == 200) {\n"
			+ "			document.getElementById('output').innerHTML = this.responseText;\n"
			+ "		}\n"
			+ "	};\n"
			+ "	xhttp.open('GET', request, true);\n"
			+ "	xhttp.send();\n"
			+ "}\n"
			+ "</script><br>\n";
	
	public static String operation( String[] params) {
		Audit.log( "action... setPwd" );
		String reply = "<strong>Change Password</strong><p>";
		if (Users.isUser( params[ 0 ])) {
			Users.setPwd( params[ 0 ], params[ 1 ]);
			reply += "OK: Password for "+ params[ 0 ]
					 + " changed</p>";
		} else
			reply += "Sorry: Password for "+ params[ 0 ] 
					 + " NOT changed</p>";
		return reply;
}	}
