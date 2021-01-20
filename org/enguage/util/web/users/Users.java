package org.enguage.util.web.users;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.enguage.util.Audit;

public class Users extends ArrayList<User> {
		
	static final long serialVersionUID = 0l;
	static       private Audit  audit = new Audit( "Users" ); 
	
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
								  (u.admin()?"admin":"user") +"\n"
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
	