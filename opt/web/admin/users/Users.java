package opt.web.admin.users;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.enguage.util.Audit;

public class Users extends ArrayList<User> {
		
	private static final long serialVersionUID = 0l;
	private static final Audit           audit = new Audit( "Users" ); 
	
	private static final String  defaultUser   = "admin";
	private static final String  defaultPasswd = "admin99";
	
	private Users append( User u ) {add( u ); return this;}
		
	private static Users users = null;
	private static Users get() {
		if (null == users) {
			users = new Users();
			try (Scanner fp = new Scanner( new File( "passwd" ))) {
				String[] data;
				while (fp.hasNextLine()) {
					data = fp.nextLine().split( User.delim );
					if (data.length == 3)
						users.add(
								new User( data[ 0 ])
										.passwd( data[ 1 ])
										.admin( data[ 2 ]
												.equals( defaultUser )
						)				);
				}
				
			} catch (Exception e) {
				users.add(
						new User( defaultUser )
								.passwd( defaultPasswd )
								.admin( true )
				);
		}	}
		return users;
	}
	private static void put( Users us ) {
		try {
			FileWriter fw = new FileWriter( "passwd" );
			if (us != null) {
				for (User u : us)
					if (!u.name().equals(""))
						fw.write( u.name()   +User.delim+
								  u.passwd() +User.delim+
								  (u.admin()?defaultUser:"user") +"\n"
								);
			} else
				fw.write( defaultUser +User.delim+ defaultPasswd +User.delim+ true );
			fw.close();
		} catch (Exception ex) {
			audit.error( "singleton put" );
			ex.printStackTrace();
		}
		users = null;
	}
	private static boolean contains( User u ) {
		get();
		if (users != null) for (User user : users)
			if (u.equals( user ))
				return true;
		return false;
	}
	private static boolean containsName( String s ) {
		for (User u : get())
			if (u.name().equals( s ))
				return true;
		return false;
	}
	private static boolean containsAdmin( User u ) {
		for (User user : get())
			if (u.equals( user ) && user.admin())
				return true;
		return false;
	}
	public static boolean validUser( String uname, String pwd ) {
		return contains( new User( uname ).passwd( pwd ));
	}
	public static boolean isUser( String uname ) {
		return containsName( uname );
	}
	public static boolean isAdmin( String name, String pwd ) {
		get();
		return containsAdmin( new User( name ).passwd( pwd ));
	}
	public static boolean addUser( String n, String p, boolean adm ) {
		User user = new User( n ).passwd( p ).admin( adm );
		get();
		if (!contains( user )) put( users.append( user ));
		return true;
	}
	public static void delUser( String n ) {
		Users newUsers = new Users();
		for (User u : get())
			if (!u.name().equals( n ))
				newUsers.add( u );
		put( newUsers );
	}
	public static void setPwd( String n, String p ) {
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
	
