package org.enguage.signs.objects.space;

import java.io.File;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

public class Value {
	static private Audit audit = new Audit( "Value" );
	
	static public  final String   NAME = "value";
	static public  final int        id = 10079711; //Strings.hash( NAME );
	static private       boolean debug = false; // Enguage.runtimeDebugging;
	
	protected String ent, attr;
	public String name() { return attr; }

	// constructor
	public Value( String e, String a ) {
		ent  = e;
		attr = a;
	}

	// statics
	static public String name( String entity, String attr, int rw ) {
		return Overlay.fname( entity +(attr==null?"":"/"+ attr), rw );
	}
	
	// members
	// TODO: cache items[]? Lock file - this code is not suitable for IPC? Exists in constructor?
	// set() methods return change in number of items
	public boolean exists() {        return Fs.exists(         name( ent, attr, Overlay.MODE_READ )); }
	public boolean set( String val ){return Fs.stringToFile(   name( ent, attr, Overlay.MODE_WRITE ), val );}
	public void    unset() {                Fs.destroyEntity(  name( ent, attr, Overlay.MODE_WRITE )); }
	public String  getAsString(){    return Fs.stringFromFile( name( ent, attr, Overlay.MODE_READ )); }
	
	public  boolean equals( String val ) { return getAsString().equals( val ); }
	private boolean contains( String val ) { return getAsString().contains( val ); }
	
	// this works..
	private static final String marker = "this is a marker file";
	public  boolean  ignore() {
		boolean rc = false;
		if (Fs.exists( name( ent, attr, Overlay.MODE_READ ))) {
			rc = true;
			String writeName = name( ent, attr, Overlay.MODE_WRITE );
			String deleteName = Overlay.deleteName( writeName );
			if ( Fs.exists( writeName )) { // rename
				File oldFile = new File( writeName ),
				     newFile = new File( deleteName );
				oldFile.renameTo( newFile );
			} else { // create
				Fs.stringToFile( deleteName, marker );
		}	}
		return rc;
	}
	public void restore() {
		String writeName = name( ent, attr, Overlay.MODE_WRITE ); // if not overlayed - simply delete!?!
		String deletedName = Overlay.deleteName( writeName ); //dir/file => dir/!file
		File deletedFile = new File( deletedName );
		String content = Fs.stringFromFile( deletedName );
		if (content.equals( marker )) {
			if (debug) audit.debug( "Value.restore(): Deleting marker file "+ deletedFile.toString());
			deletedFile.delete();
		} else {
		    File restoredFile = new File( writeName );
		    if (debug) audit.debug( "Value.restore(): Moving "+ deletedFile.toString() +" to "+ restoredFile.toString());
			deletedFile.renameTo( restoredFile );
	}	}
	
	static private String usage( String cmd, String entity, String attr, Strings a ) {
		return usage( cmd +" "+ entity +" "+ attr +" "+ a.toString() );
	}
	static private String usage( String a ) {
		Audit.log( "Usage: [set|get|add|exists|equals|delete] <ent> <attr>{/<attr>} [<values>...]\n"+
				   "given: "+ a );
		return Shell.FAIL;
	}
	static public Strings interpret( Strings a ) {
		// sa might be: [ "add", "_user", "need", "some", "beer", "+", "some crisps" ]
		audit.in( "interpret", a.toString( Strings.CSV ));
		a = a.normalise();
		String rc = Shell.SUCCESS;
		if (null != a && a.size() > 2) {
			String cmd = a.remove( 0 ), 
			    entity = a.remove( 0 ),
			 attribute = a.remove( 0 );
			// components? martin car / body / paint / colour red
			while (a.size()>1 && a.get( 0 ).equals( "/" )) {
				a.remove( 0 );
				attribute += ( "/"+ a.remove( 0 ));
			}
			Value v = new Value( entity, attribute ); //attribute needs to be composite: dir dir dir file values
			
			if (a.size()>0) {
				// [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
				String value = a.contract("=").toString();
				if (Attribute.isAttribute( value ))
					value = new Attribute( value ).value();
				
				if (cmd.equals( "set" ))
					v.set( value );
				
				else if (cmd.equals( "equals" )) 
					rc = v.equals( value ) ? Shell.SUCCESS : Shell.FAIL;
				
				else if (cmd.equals( "contains" ))
					rc = v.contains( value ) ? Shell.SUCCESS : Shell.FAIL;
				
				else
					usage( cmd, entity, attribute, a );
				
			} else {
				if (cmd.equals( "get" ))
					rc = v.getAsString();
				
				else if (cmd.equals( "unset" ))
					v.unset();
				
				else if (cmd.equals( "delete" ))
					v.ignore();
				
				else if (cmd.equals( "undelete" ))
					v.restore();
				
				else if (cmd.equals( "exists" ))
					// could check to see if it contains <attribute>?
					rc = v.exists() ? Shell.SUCCESS : Shell.FAIL ;
				
				else 
					rc = usage( cmd, entity, attribute, a );
			}
		} else
			rc = usage( a.toString() );
		audit.out( rc );
		return new Strings( rc );
	}
	static private void test( String cmd, String expected ) {
		Strings answer = interpret( new Strings( cmd ));
		if (!answer.equals( new Strings( expected )))
			audit.error( "expecting:"+ expected +", but got: "+ answer );
		else
			audit.passed();
	}
 	public static void main( String args[] ) {
		Overlay.set( Overlay.get());
		Overlay.attach( NAME );
		test( "set martin name martin j wheatman", "TRUE" );
		test( "get martin name",      "martin j wheatman" );
		audit.PASSED();
}	}
