package org.enguage.util.http;

import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;

public class Html {
	
	public  static final int      ID = 154478; // "html"
	
	private String name = "";
	public  String name() {return name;}
	public  Html   name( String nm ) {name = nm; return this;}
	
	private Attributes attributes = new Attributes();
	public  Attributes attributes() {return attributes;}
	public  Html add( Attribute a ) {attributes.add( a ); return this;}
	
	private boolean end = false;
	public  boolean end() {return end;}
	public  Html    end( boolean b ) {end = b; return this;}
	
	private boolean standAlone = false;
	public  boolean standAlone() {return standAlone;}
	public  Html    standAlone( boolean b ) {standAlone = b; return this;}
	
	public  boolean isEmpty() {return name.equals("");}
	
	public String toString() {
		return "<"+(end?"/":"")+name+attributes+(standAlone?"/":"")+">";
	}
	public static void main( String [] args) {
		Html html = new Html();
		html.name( "hello" );
		html.standAlone( true );
		html.add( new Attribute( "name", "martin" ));
		Audit.log( "text="+ html );
}	}
