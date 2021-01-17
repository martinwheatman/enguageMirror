package org.enguage.util.web;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.enguage.Enguage;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Server extends Thread {
	
	static class User {
		static public String delim = ":";
		
		public User( String n ) {name = n;}
		private final String name;
		public        String name() {return name;}
		
		private String passwd = "";
		public  String passwd() {return passwd;}
		public  User   passwd( String pwd ) {passwd = pwd; return this;}
		
		private boolean admin = false;
		public  boolean admin() {return admin;}
		public  User    admin( boolean b ) {admin = b; return this;}
		
		public  boolean equals( User u ) {
			return name.equals( u.name() )
				&& passwd.equals( u.passwd() );
		}
		public  boolean matches( User u ) {
			return name.equals( u.name() );
		}
		public  String toString() {
			return "["+name+delim+passwd+"]";
	}	}
	
	static class Users extends ArrayList<User> {
		
		static final long serialVersionUID = 0l;
		
		private Users append( User u ) { add( u ); return this;}
		
		static private Users users = null;
		static private Users get() {
			if (null == users) {
				users = new Users();
				try {
					String[] data;
					Scanner fp = new Scanner( new File( "passwd" ));
					while (fp.hasNextLine()) {
						data = fp.nextLine().split( User.delim );
						if (data.length == 3)
							users.add( new User( data[ 0 ])
											.passwd( data[ 1 ])
											.admin( data[ 2 ].equals( "admin" )));
					}
					fp.close();
				} catch (Exception e) {
					users.add( new User( "admin" ).passwd( "admin99" ).admin( true ));
			}	}
			return users;
		}
		static private void put( Users us ) {
			try {
				FileWriter fw = new FileWriter( "passwd" );
				if (us != null) {
					for (User u : us)
						if (!u.name().equals(""))
							fw.write( u.name()   +User.delim+
									  u.passwd() +User.delim+
									  (u.admin?"admin":"user") +"\n"
									);
				} else
					fw.write( "admin" +User.delim+ "admin99" +User.delim+ true );
				fw.close();
			} catch (Exception ex) {
				audit.ERROR( "singleton put" );
				ex.printStackTrace();
			}
			users = null;
		}
		static private boolean contains( User u ) {
			get();
			if (users != null) for (User user : users)
				if (u.equals( user ))
					return true;
			return false;
		}
		static private boolean containsName( String s ) {
			for (User u : get())
				if (u.name().equals( s ))
					return true;
			return false;
		}
		static private boolean containsAdmin( User u ) {
			for (User user : get())
				if (u.equals( user ) && user.admin())
					return true;
			return false;
		}
		static public boolean validUser( String uname, String pwd ) {
			return contains( new User( uname ).passwd( pwd ));
		}
		static public boolean isUser( String uname ) {
			return containsName( uname );
		}
		static public boolean isAdmin( String name, String pwd ) {
			get();
			return containsAdmin( new User( name ).passwd( pwd ));
		}
		static public boolean addUser( String n, String p, boolean adm ) {
			User user = new User( n ).passwd( p ).admin( adm );
			get();
			if (!contains( user )) put( users.append( user ));
			return true;
		}
		static public void delUser( String n ) {
			Users newUsers = new Users();
			for (User u : get())
				if (!u.name().equals( n ))
					newUsers.add( u );
			put( newUsers );
		}
		static public void setPwd( String n, String p ) {
			Users newUsers = new Users();
			get();
			for (User u : users) {
				if (u.name().equals( n ))
					u.passwd( p );
				newUsers.append( u );
			}
			put( newUsers );
			users = newUsers;
	}	}
	
	static final private String  name = "WebServer";
	static final private Audit  audit = new Audit( name );


	// HTML pages
	static final String engPage( String username ) {
		return	"<fieldset>\n"
				+ "		<legend>Say:</legend>\n"
				+ "		<form action='/enguage'>\n"
				+ "			<label for='utterance'>Utterance:</label>\n"
				+ "			<input type='text' id='utterance'>&nbsp;\n"
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
					+ "		<label for='password'>Password:&nbsp;</label>\n"
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
	
	static private int  port = 8080;
	static public  int  port() {return port;}
	static public  void port( int p ) {port = p;}
	
	static private String sID = "";
	static private String sID() {return sID;}
	static private void   sID( String s ) {sID = s;}

	static private String param = "";
	static private String param() {return param;}
	static private void   param( String s ) {param = s;}

	static private void parseSID( Scanner header ) {
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

	static private boolean validAttr( String param ) {
		// each param must be named, e.g. name="value"
		String[] components = param.split( "=" );
		return components.length == 2
				&& !components[ 0 ].equals( "" )
				&& !components[ 1 ].equals( "" );
	}

	static String processRequest( String request ) {
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
					
					sID( uname + User.delim + passwd );
					reply += (Users.isAdmin( uname, passwd ) ? adminPage:engPage( uname )) + logoutButton;
					
				} else
					reply += loginScreen + "<br><strong>Login failed</strong>";
			} else
				reply += loginScreen;
			reply += end;
			
		} else if (cmd.equals( "logout" )) {
			sID( "" );
			reply = begin + loginScreen + end;
				
		} else if (cmd.equals( "/index.html" )) {
			reply = begin
					+ adminPage
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
			Audit.log( "Unknown request" );
			reply = begin + "Unknown request: cmd="+ cmd +": ";
			for (String s : params) reply += " "+ s;
			reply += logoutButton + end;
		}
		return audit.out( reply );
	}

	static public void server( String port ) {
		ServerSocket server = null;

		Enguage.init( Enguage.RW_SPACE );

		try {
			server = new ServerSocket( Integer.valueOf( port ));
			Audit.LOG( "Server listening on port: "+ port );
			while (true) {
				
				Socket    connection = server.accept();
				Scanner   in  = null;
				DataOutputStream out = null;
				
				try {
					String reply;
					in  = new Scanner( connection.getInputStream());
					out = new DataOutputStream( connection.getOutputStream());
					
					// parse request
					if (in.hasNextLine()) {
						String request = in.nextLine();   // "GET /login HTTP/2.0"
						Audit.log( "Request is: "+ request );
							
						sID("");
						parseSID( in );
						
						reply = processRequest( request );
						
						//if (reply.startsWith( begin )) // it's a page
							reply = "HTTP/2.0\n"
									+ "Content-type: text/html\n"
									+ "Set-cookie: sessionID='"+ sID() +"'\n"
									+ "\n"
									+ reply;
						
						//Audit.LOG( "Replying with:\n"+ reply +"\n" );
						out.writeBytes( reply + "\n" );
					}
					
				} catch (Exception e) {
					audit.ERROR( "Error in child socket");
					e.printStackTrace();
				} finally {
					try {
						if (null != in) in.close();
						if (null != out) out.close();
						if (null != connection) connection.close();
					} catch (IOException e) {
						audit.ERROR( "error in closing stream: "+ e );
			}	}	}
		} catch (IOException e) {
			audit.ERROR( name +":IO error in TCP socket operation" );
			e.printStackTrace();
		} finally {
			try {
				if (null != server) server.close();
			} catch (IOException e) {
				audit.ERROR( "Net.server():IO error in closing TCP server socket" );
	}	}	}
	public static void main( String args[]) {
		server( args.length == 1 ? args[ 0 ] : "8080" );
}	}
