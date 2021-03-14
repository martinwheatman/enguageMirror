package opt.web;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

import org.enguage.Enguage;
import org.enguage.util.Audit;

import opt.web.actions.Eng;
import opt.web.actions.Login;
import opt.web.admin.Admin;
import opt.web.admin.users.User;

public class Request extends Thread {
	
	static final private String  name = "Request";
	static       private Audit  audit = new Audit( name );

	private Socket connection;
	
	public  Request( Socket conn ) {connection = conn;}

	private String sID = "";
	public  String sID() {return sID;}
	public  void   sID( String s ) {sID = s;}
	public  void   sID( String uid, String pwd ) {sID( uid +User.delim+ pwd );}
	public  String uid() {return sID.split(User.delim)[ 0 ];}
	public  String passwd() {return sID.split(User.delim)[ 1 ];}
	
	private String param = "";
	public  String param() {return param;}
	private void   param( String s ) {param = s;}

	private String response = "200 OK";
	private String response() {return response;}
	public  void   response( String s ) {response = s;}

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

	private static boolean validAttr( String param ) {
		// each param must be named, e.g. name="value"
		String[] components = param.split( "=" );
		return components.length == 2
				&& !components[ 0 ].equals( "" )
				&& !components[ 1 ].equals( "" );
	}
	public  static boolean validAttrs( String[] params, int n ) {
		if (params.length != n)
			return false;
		for (int i=0; i < n; i++)
			if (!validAttr( params[ i ]))
				return false;
		return true;
	}

	private String getReply( String request ) {
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
			
		if (sID().equals(""))
			reply = Login.getLogin( this, cmd, params );
		
		else if (cmd.equals( "megan" ))
			reply = "<input type='text' id='filter' placeholder='filter'></input><br/>\n"
					+ "<button id='scan'>Scan</button>";
			
		else if (
			"".equals(reply = Login.getReply( this, cmd, params )) &&
			"".equals(reply = Admin.getReply( this, cmd, params )) &&
			"".equals(reply = Eng  .getReply( this, cmd, params )) )
		{
			Audit.log( "Unknown request: cmd='"+ cmd +"':" );
			for (String s : params) Audit.log( " '"+ s +"'" );
			reply = "404 : page not found :(";
			response( "404" );
		}
		return audit.out( reply );
	}
	public void run() {
		try (Scanner           in = new Scanner( connection.getInputStream());
			 DataOutputStream out = new DataOutputStream(
					                             connection.getOutputStream());) 
		{	// parse request
			if (in.hasNextLine()) {
				String request = in.nextLine();   // "GET /login HTTP/2.0"
				Audit.log( "Request is: "+ request );
					
				sID("");
				parseSID( in );
				
				response( "200 OK" );
				String reply = getReply( request );
				
				out.writeBytes(
						"HTTP/2.0 "+ response() +"\n"
						+ "Content-type: text/html\n"
						+ "Set-cookie: sessionID='"+ sID() +"'\n"
						+ "\n"
						+ reply + "\n"
				);
			}
			connection.close();
			
		} catch (Exception e) {
			audit.ERROR( "Error in child socket");
			e.printStackTrace();
}	}	}
