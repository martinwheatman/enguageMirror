package org.enguage.util.web;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.web.users.User;
import org.enguage.util.web.users.Users;

public class Request {
	
	static final private String  name = "Request";
	static       private Audit  audit = new Audit( name );

	// HTML pages
	static final String engPage( String username ) {
		return	"<fieldset>\n"
				+ "		<legend>Say:</legend>\n"
				+ "		<form action='/enguage'>\n"
				+ "			<label for='utterance'>Utterance:</label>\n"
				+ "			<input type='text' id='utterance' placeholder='utterance'>&nbsp;\n"
				+ "			<input type='button' id='say' onclick='i_say()' value='Say'>\n" 
				+ "		</form>\n"
				+ " 	<p id='reply'></p>\n"
				+ "	</fieldset>\n"
				+ "<script>\n"
				+ "	function i_say() {\n"
				+ "		//alert( 'hello' );\n"
				+ "		var request = '/enguage'"
				+ "					+ '?uid=" + username +"'"
				+ "					+ '&utterance='+ document.getElementById('utterance').value;\n"
				+ "		var xhttp = new XMLHttpRequest();\n"
				+ "		xhttp.onreadystatechange = function() {\n"
				+ "			if (this.readyState == 4 && this.status == 200) {\n"
				+ "				document.getElementById('utterance').value = '';\n"
				+ "				document.getElementById('reply').innerHTML = this.responseText;\n"
				+ "			}\n"
				+ "		};\n"
				+ "		xhttp.open('GET', request, true);\n"
				+ "		xhttp.send();\n"
				+ "	}\n"
				+ "</script><br>\n";
	}
			
	
	static final private String
			begin   = "<!DOCTYPE=html>\n"
					+ "<html>"
					+ "<head>"
					+ "<style>\n"
					+ "	fieldset {display : inline-block;}\n"
					+ "</style>"
					+ "</head>"
					+ "<body>\n",
					
			end     = "</body></html>\n",
			
			loginScreen =
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
					+ "</script>",
			
			addPage =
					"<fieldset>\n"
					+ "<legend>Add User:</legend>\n"
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
					+ "</script><br>\n",
					
			delPage = 
					"<fieldset>\n"
					+"<legend>\n"
					+	"<label for='action'>Delete User:</label>\n"
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
					+ "</script><br>\n",
				
			setPage = 
					"<fieldset>\n"
					+ 	"<legend>Set Password:</legend>\n"
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
//					+ "	document.cookie=\"param=\"+document.getElementById('setpwd').value;"
					+ "	var xhttp = new XMLHttpRequest();\n"
					+ "	xhttp.onreadystatechange = function() {\n"
					+ "		if (this.readyState == 4 && this.status == 200) {\n"
					+ "			document.getElementById('output').innerHTML = this.responseText;\n"
					+ "		}\n"
					+ "	};\n"
					+ "	xhttp.open('GET', request, true);\n"
					+ "	xhttp.send();\n"
					+ "}\n"
					+ "</script><br>\n",
					
			outputArea = "<p id='output'></p>\n",
			
			adminPage = addPage + delPage + setPage + outputArea,
					logoutButton =
					"<form action='/logout'>"+
						"<input type='submit' value='Logout'>"+
					"</form>\n";
	
	private String sID = "";
	private String sID() {return sID;}
	private void   sID( String s ) {sID = s;}
	private void   sID( String uid, String pwd ) {sID( uid +User.delim+ pwd );}
	private String uid() {return sID.split(User.delim)[ 0 ];}
	private String passwd() {return sID.split(User.delim)[ 1 ];}
	
	private String param = "";
	private String param() {return param;}
	private void   param( String s ) {param = s;}

	private void parseSID( Scanner header ) {
		// parse header for cookies...
		String headerLine = "";
		while (header.hasNextLine())
			if ((headerLine = header.nextLine()).equals( "" ))
				break;
			else if	(headerLine.startsWith( "Cookie:" )) 
				// parse cookies for sessionId
				for (String cookie : headerLine.split(" "))
					if (cookie.startsWith( "sessionID=" )) {
						String[] tmp = cookie.split( "'" );
						if (tmp.length > 1)	sID( tmp[ 1 ]);
					} else if (cookie.startsWith( "param=" )) {
						String[] tmp = cookie.split( "'" );
						if (tmp.length > 1)	param( tmp[ 1 ]);
	}				}

	private boolean validAttr( String param ) {
		// each param must be named, e.g. name="value"
		String[] components = param.split( "=" );
		return components.length == 2
				&& !components[ 0 ].equals( "" )
				&& !components[ 1 ].equals( "" );
	}

	private String processContent( String request ) {
		Audit.LOG( "processRequest: request="+ request );
		// request="GET /addUser?uname=martin&pwd=s3cret HTTP/2.0"
		String reply = "unknown";
		String[] reqs = request.split(" "); // ["GET", "/..."
		reqs = reqs[ 1 ].split("\\?"); // leading '/' ["", "addUser", "1001", "1234"]
		
		// split this into a command and parameters
		String cmd = reqs[ 0 ].substring( 1 );
		String[] params =
				reqs.length < 2 ?
						new String[0] :
							reqs[ 1 ].split("\\&"); // leading '/' ["", "addUser", "1001", "1234"]

		if (sID().equals("")) {
			
			reply = begin;
			if (cmd.equals( "login" )
					&& params.length > 0
					&& validAttr( params[ 0 ]))
			{
				String  uname  = params[ 0 ].split("=")[ 1 ],
						passwd = param();
				
				if (Users.validUser( uname, passwd )) {
					
					sID( uname, passwd );
					reply += (Users.isAdmin( uname, passwd ) ? adminPage:engPage( uname )) + logoutButton;
					
				} else
					reply += loginScreen + "<br><strong>Login failed</strong>";
			} else
				reply += loginScreen;
			reply += end;
			
		} else if (cmd.equals( "logout" )) {
			sID( "" );
			reply = begin + loginScreen + end;
				
		} else if (cmd.equals( "login" )) {
			reply = begin
					+ (Users.isAdmin( uid(), passwd() ) ? adminPage:engPage( uid() ))
					+ logoutButton
					+ end;
		/*
		 * The reset of these screens are actions
		 */
		} else if (cmd.equals( "enguage" )) {
			
			if (   params.length == 2
				&& validAttr( params[ 0 ])
				&& validAttr( params[ 1 ]) )
				reply = "<center><strong>"
						+ Enguage.mediate(
								params[ 0 ].split( "=" )[ 1 ],
								new Strings( params[ 1 ].split( "=" )[ 1 ].split( "%20" ))
						  )
						+ "</strong></center></P>";
			else
				reply = "<center>(Try setting the value of utterance to something)</center>";
				
		} else if (cmd.equals( "Enguage" )) {
			
			if (   params.length > 0
				&& validAttr( params[ 0 ])
				&& uid().length() > 0)
				reply = Enguage.mediate(
								uid(),
								new Strings( params[ 0 ].split( "=" )[ 1 ].split( "%20" ))
						).toString();
			else {
				Audit.log( "not found" );
				reply = ""; // => 404
			}
		} else if (params.length > 2 && cmd.equals( "addUser" )) {
			
			Audit.log( "adduser" );
			if (sID().split( User.delim ).length == 2) {
				
				String  adminId  = sID().split( User.delim )[ 0 ],
						adminHsh = sID().split( User.delim )[ 1 ];
				
				Audit.LOG( "validating: "+ adminId +"/"+ adminHsh );
				
				if (Users.isAdmin( adminId, adminHsh )) {
					System.out.println( "valid user" );
					reply = "<strong>Add User</strong><p>"
							+ (Users.addUser(
								params[ 0 ],
								params[ 1 ],
								params[ 2 ].equals( "true" )
							 ) ?
								"OK: "   +(params[ 2 ].equals( "true" )?"admin":"user")+" "+ params[ 0 ] +" added" :
								"Sorry: Username "+ params[ 0 ] +" already exists")
							+ "</p>";
				} else {
					audit.ERROR( "invalid user" );
					reply = "<strong>Permission Denied</strong>";
				}
				
			} else
				reply = "Invalid session id: "+ sID();
			
			
		} else if (params.length == 1 && cmd.equals( "delUser" )) {
			Audit.log( "action... delUser" );
			
			if (sID().split( User.delim ).length == 2) {

				String  adminId  = sID.split( User.delim )[ 0 ],
						adminPin = sID.split( User.delim )[ 1 ];
				
				if (Users.validUser( adminId, adminPin )) {
					
					reply = "<strong>Delete User</strong><p>";
					if (Users.isUser( params[ 0 ])) {
						Users.delUser( params[ 0 ]);
						reply += "OK: Username "+ params[ 0 ] +" deleted";
					} else
						reply += "Sorry: Username "+ params[ 0 ]
								 + " not found</p>";
				} else
					reply = "<strong>Permission Denied</strong>";
			} else
				reply = "Invalid session id: "+ sID();
			
			
		} else if (params.length == 2 && cmd.equals( "setPwd" )) {
			Audit.log( "action... setPwd" );
			
			String  adminId  = sID.split( User.delim )[ 0 ],
					adminPin = sID.split( User.delim )[ 1 ];
			
			if (Users.validUser( adminId, adminPin )) {
				
				reply = "<strong>Change Password</strong><p>";
				if (Users.isUser( params[ 0 ])) {
					Users.setPwd( params[ 0 ], params[ 1 ]);
					reply += "OK: Password for "+ params[ 0 ]
							 + " changed</p>";
				} else
					reply += "Sorry: Password for "+ params[ 0 ] 
							 + " NOT changed</p>";
			} else
				reply = "<strong>Permission Denied</strong>";
			
		} else if (cmd.equals( "Select" )) {
			Audit.log( "Action unselected" );
			reply = "Try selecting an action...";
				
		} else {
			Audit.log( "Unknown request: cmd='"+ cmd +"':" );
			for (String s : params) Audit.log( " '"+ s +"'" );
			reply = "";
		}
		return audit.out( reply );
	}
	public void doRequest( Socket conn ) {
		try  (	Scanner           in = new Scanner( conn.getInputStream());
				DataOutputStream out = new DataOutputStream( conn.getOutputStream());
		) {
			String reply;
			
			// parse request
			if (in.hasNextLine()) {
				String request = in.nextLine();   // "GET /login HTTP/2.0"
				Audit.log( "Request is: "+ request );
					
				sID("");
				parseSID( in );
				
				reply = processContent( request );
				
				String response = reply.equals( "" ) ? "404" : "200 OK";
				//if (reply.startsWith( begin )) // it's a page
					reply = "HTTP/2.0 "+ response +"\n"
							+ "Content-type: text/html\n"
							+ "Set-cookie: sessionID='"+ sID() +"'\n"
							+ "\n"
							+ reply;
				
				//Audit.LOG( "Replying with:\n"+ reply +"\n" );
				out.writeBytes( reply + "\n" );
			}
			conn.close();
			
		} catch (Exception e) {
			audit.ERROR( "Error in child socket");
			e.printStackTrace();
}	}	}
