package opt.web.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import opt.web.Request;
import opt.web.Server;
import opt.web.admin.Admin;
import opt.web.admin.users.Users;

public class Login {
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
				
	logoutButton =
			"<form action='/logout'>"+
				"<input type='submit' value='Logout'>"+
			"</form>\n";
	
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
	
	public static String getLogin( Request r, String cmd, String[] params) {
		String reply = begin;
		
		if (cmd.equals( "login" )
			&& Request.validAttrs( params, 2 ))
		{
			String  uname  = params[ 0 ].split("=")[ 1 ],
					passwd = r.param();
			
			if (Users.validUser( uname, passwd )) {
				r.sID( uname, passwd );
				return getReply( r, cmd, params );
				
			} else
				reply += Login.widget + "<br><strong>Login failed</strong>";
		} else
			reply += Login.widget;
		reply += end;
		
		return reply;
	}
	public static String getReply( Request r, String cmd, String[] params) {
		String reply = "";
		
		if (cmd.equals( "logout" )) {
			r.sID( "" );
			reply = begin + Login.widget + end;
			
		} else if (cmd.equals( "login" ))
			reply = begin
					+ (Users.isAdmin( r.uid(), r.passwd() ) ?
							Admin.userAdmin
							: Eng.engPage( r.uid() ))
					+ logoutButton
					+ end;
		
		else if (!Server.root().equals("")) {
			File f = new File( Server.root() +File.separator+ cmd );
			try (Scanner s = new Scanner( f )) {
				while (s.hasNextLine())
					reply += s.nextLine();
			} catch(FileNotFoundException ignore) {}
		}
		return reply;
}	}
