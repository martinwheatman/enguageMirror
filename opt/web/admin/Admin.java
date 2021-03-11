package opt.web.admin;

import opt.web.Request;
import opt.web.admin.users.Users;

public class Admin {

	static final public String
			outputArea = "<p id='output'></p>\n",
			userAdmin = Add.widget
					+ Delete.widget
					+ Passwd.widget
					+ outputArea;

	public static String getReply(
			Request r,
			String cmd,
			String[] params )
	{
		String reply = "";
		if (params.length == 3 && cmd.equals( "addUser" ))
		
			reply = Users.isAdmin( r.uid(), r.passwd() ) ?
						Add.operation( params )
						: "<strong>Permission Denied</strong>";
							
		else if (params.length == 1 && cmd.equals( "delUser" ))
			
			reply = Users.isAdmin( r.uid(), r.passwd() ) ?
				Delete.operation( params )
				: "<strong>Permission Denied</strong>";
			
		else if (params.length == 2 && cmd.equals( "setPwd" ))
			
			reply = Users.isAdmin( r.uid(), r.passwd() ) ?
						Passwd.operation( params ) 
						: "<strong>Permission Denied</strong>";
		return reply;
}	}
