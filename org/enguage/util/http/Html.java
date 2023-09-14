package org.enguage.util.http;

import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;

public class Html {
	public  static final int      ID = 154478; // "html"
	
	public static enum Type {begin, standalone, end};
	private Type type = Type.begin;
	public  Type type() {return type;}
	public  Html type( Type b ) {type = b; return this;}
	
	private String name = "";
	public  String name() {return name;}
	public  Html   name( String nm ) {name = nm; return this;}
	
	private Attributes attributes = new Attributes();
	public  Attributes attributes() {return attributes;}
	public  Html add( Attribute a ) {attributes.add( a ); return this;}
	
	public  boolean isEmpty() {return name.equals("");}
	
	public String toString() {
		return "<"
				+ (type==Type.end?"/":"")
				+ name
				+ attributes
				+ (type() == Type.standalone?"/":"")
				+ ">";
	}
	public static void main( String [] args) {
		Html html = new Html();
		html.name( "hello" );
		html.type( Type.standalone );
		html.add( new Attribute( "name", "martin" ));
		Audit.log( "text="+ html );
}	}
