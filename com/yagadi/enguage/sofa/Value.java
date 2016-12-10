package com.yagadi.enguage.sofa;

import java.io.File;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

class ValuesTest extends Shell {
	ValuesTest( Strings args ) { super( "ValuesTes", args ); }
	public String interpret( Strings a ) { return Value.interpret( a ); }
}

public class Value {
	static private Audit audit = new Audit( "Value" );
	public static final String NAME = "value";
	private static boolean debug = false; // Enguage.runtimeDebugging;
	
	protected String ent, attr;

	// constructor
	public Value( String e, String a ) {
		ent  = e;
		attr = a;
	}

	// statics
	static public String name( String entity, String attr, String rw ) {
		return Overlay.fsname( entity +(attr==null?"":"/"+ attr), rw );
	}
	
	// members
	// TODO: cache items[]? Lock file - this code is not suitable for IPC? Exists in constructor?
	// set() methods return change in number of items
	public boolean exists() {    return Filesystem.exists(         name( ent, attr, Overlay.MODE_READ )); }
	public void    set( String val ) {  Filesystem.stringToFile(   name( ent, attr, Overlay.MODE_WRITE ), val ); }
	public void    unset() {            Filesystem.destroyEntity(  name( ent, attr, Overlay.MODE_WRITE )); }
	public String  getAsString(){return Filesystem.stringFromFile( name( ent, attr, Overlay.MODE_READ )); }
	
	private boolean equals( String val ) { return getAsString().equals( val ); }
	private boolean contains( String val ) { return getAsString().contains( val ); }
	
	// this works..
	private static final String marker = "this is a marker file";
	public void  ignore() {
		if (Filesystem.exists( name( ent, attr, Overlay.MODE_READ ))) {
			String writeName = name( ent, attr, Overlay.MODE_WRITE );
			String deleteName = Entity.deleteName( writeName );
			if ( Filesystem.exists( writeName )) { // rename
				File oldFile = new File( writeName ),
				     newFile = new File( deleteName );
				if (debug) audit.debug( "Value.ignore(): Moving "+ oldFile.toString() +" to "+ deleteName );
				oldFile.renameTo( newFile );
			} else { // create
				if (debug) audit.debug( "Value.ignore(): Creating marker file "+ deleteName );
				Filesystem.stringToFile( deleteName, marker );
	}	}	}
	public void restore() {
		String writeName = name( ent, attr, Overlay.MODE_WRITE ); // if not overlayed - simply delete!?!
		String deletedName = Entity.deleteName( writeName );
		File deletedFile = new File( deletedName );
		String content = Filesystem.stringFromFile( deletedName );
		if (content.equals( marker )) {
			if (debug) audit.debug( "Value.restore(): Deleting marker file "+ deletedFile.toString());
			deletedFile.delete();
		} else {
		    File restoredFile = new File( writeName );
		    if (debug) audit.debug( "Value.restore(): Moving "+ deletedFile.toString() +" to "+ restoredFile.toString());
			deletedFile.renameTo( restoredFile );
	}	}
	
	static private String usage( Strings a ) {
		System.out.println(
				"Usage: [set|get|add|removeFirst|removeAll|exists|equals|delete] <ent> <attr>[ / <attr> ...] [<values>...]\n"+
				"given: "+ a.toString( Strings.CSV ));
		return Shell.FAIL;
	}
	static public String interpret( Strings a ) {
		// sa might be: [ "add", "_user", "need", "some", "beer", "+", "some crisps" ]
		audit.in( "interpret", a.toString( Strings.CSV ));
		a = a.normalise();
		String rc = Shell.SUCCESS;
		if (null != a && a.size() > 2) {
			int i = 2;
			String cmd = a.get( 0 ), entity = a.get( 1 ), value = null, attribute = null;
			if (i<a.size()) { // components? martin car / body / paint / colour red
				attribute = a.get( i );
				while (++i < a.size() && a.get( i ).equals( "/" ))
					attribute += ( "/"+ a.get( i ));
			}
			//audit.debug( "entity => '"+ entity +"'" );
			//audit.debug( "attr => '"+ attribute +"'" );
			// [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
			//Strings values = Strings.rejig( Strings.copyAfter( a, i-1 ), sep );
			String val = a.copyAfter( i-1 ).toString( Strings.SPACED ); 
			//audit.debug( "values => ["+ Strings.toString( values,  Strings.DQCSV ) +"]" );
			
			Value m = new Value( entity, attribute ); //attribute needs to be composite: dir dir dir file values
			if (cmd.equals( "set" )) {
				m.set( val );
			} else if (null == value && cmd.equals( "get" )) {
				rc = m.getAsString();
			} else if (cmd.equals( "unset" )) {
				rc = Shell.SUCCESS;
				m.unset();
			} else if (cmd.equals( "exists" )) {
				rc = (null==val || 0 == val.length()) ?
					m.exists() ? Shell.SUCCESS : Shell.FAIL :
					m.contains( val ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "equals" )) {
				rc = m.equals( val ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "delete" )) {
				m.ignore();
			} else if (cmd.equals( "undelete" )) {
				m.restore();
			} else
				rc = usage( a );
		} else
			rc = usage( a );
		audit.out( rc );
		return rc;
	}
	public static void main( String args[] ) {
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			System.out.println( "Ouch!" );
		else
			new ValuesTest( new Strings( args )).run();
}	}