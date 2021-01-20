package org.enguage.util.web.users;

public class User {

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
