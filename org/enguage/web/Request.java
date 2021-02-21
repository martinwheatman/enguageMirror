package org.enguage.web;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

import org.enguage.util.Audit;
import org.enguage.web.users.User;
import org.enguage.web.users.Users;

public class Request {
	
	static final private String  name = "Request";
	static       private Audit  audit = new Audit( name );

	static final private String
			begin   = "<!DOCTYPE=html>\n"
					+ "<html>\n"
					+ "<head>\n"
					+ "<style>\n"
					+ "	fieldset {display : inline-block;}\n"
					+ "</style>"
					+ "</head>"
					+ "<body>\n",
					
			end     = "</body></html>\n",
						
			outputArea = "<p id='output'></p>\n",
			
			adminPage = UserAdd.widget
						+ UserDelete.widget
						+ UserPwd.widget
						+ outputArea,
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

	private String response = "200 OK";
	private String response() {return response;}
	private void   response( String s ) {response = s;}

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
	private boolean validAttrs( String[] params, int n ) {
		if (params.length != n)
			return false;
		for (int i=0; i < n; i++)
			if (!validAttr( params[ i ]))
				return false;
		return true;
	}

	private String processContent( String request ) {
		String reply = "";

		Audit.LOG( "processRequest: request="+ request );
		// request="GET /addUser?uname=martin&pwd=s3cret HTTP/2.0"
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
				&& validAttrs( params, 1 ))
			{
				String  uname  = params[ 0 ].split("=")[ 1 ],
						passwd = param();
				
				if (Users.validUser( uname, passwd )) {
					sID( uname, passwd );
					reply += (Users.isAdmin( uname, passwd ) ?
							adminPage
							: EnguagePage.engPage( uname )) + logoutButton;
				} else
					reply += Login.widget + "<br><strong>Login failed</strong>";
			} else
				reply += Login.widget;
			reply += end;
			
		} else if (cmd.equals( "logout" )) {
			sID( "" );
			reply = begin + Login.widget + end;
				
		} else if (cmd.equals( "login" )) {
			reply = begin
					+ (Users.isAdmin( uid(), passwd() ) ?
							adminPage
							: EnguagePage.engPage( uid() ))
					+ logoutButton
					+ end;
		/*
		 * The reset of these screens are actions
		 */
		} else if (cmd.equals( "enguage" )) {
			reply = validAttrs( params, 2 ) ?
						EnguagePage.form( params )
						: ("<center>"
							+ "(Try setting the value of utterance to something)"
							+ "</center>");
				
		} else if (cmd.equals( "Enguage" )) { // verbal interaction
			
			if (   validAttrs( params, 1 )
				&& uid().length() > 0)
			
				reply = EnguagePage.direct( uid(), params );
			else
				response( "404" );
			
		} else if (cmd.equals( "addUser" )) {
			
			reply = params.length > 2  && Users.isAdmin( uid(), passwd() ) ?
						UserAdd.operation( params )
						: "<strong>Permission Denied</strong>";
							
		} else if (params.length == 1 && cmd.equals( "delUser" )) {
			
			reply = Users.validUser( uid(), passwd() ) ?
				UserDelete.operation( params )
				: "<strong>Permission Denied</strong>";
			
		} else if (cmd.equals( "setPwd" )) {
			
			reply = params.length == 2 &&
					Users.validUser( uid(), passwd() ) ?
						UserPwd.operation( params ) 
						: "<strong>Permission Denied</strong>";
			
		} else if (cmd.equals( "megan" )) {
			reply = "<input type='text' id='filter' placeholder='filter'></input><br/>\n"
					+ "<button id='scan'>Scan</button>";
			
		} else {
			Audit.log( "Unknown request: cmd='"+ cmd +"':" );
			for (String s : params) Audit.log( " '"+ s +"'" );
			response( "404" );
		}
		return audit.out( reply );
	}
	public void doRequest( Socket conn ) {
		try  (	Scanner           in = new Scanner( conn.getInputStream());
				DataOutputStream out = new DataOutputStream( conn.getOutputStream());
		) {
			// parse request
			if (in.hasNextLine()) {
				String request = in.nextLine();   // "GET /login HTTP/2.0"
				Audit.log( "Request is: "+ request );
					
				sID("");
				parseSID( in );
				
				response( "200 OK" );
				String reply = processContent( request );
				
				out.writeBytes(
						"HTTP/2.0 "+ response() +"\n"
						+ "Content-type: text/html\n"
						+ "Set-cookie: sessionID='"+ sID() +"'\n"
						+ "\n"
						+ reply + "\n"
				);
			}
			conn.close();
			
		} catch (Exception e) {
			audit.ERROR( "Error in child socket");
			e.printStackTrace();
}	}	}
