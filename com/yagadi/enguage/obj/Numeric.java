package com.yagadi.enguage.obj;

import java.util.ListIterator;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Number;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Numeric extends Value {
	static private Audit audit = new Audit( "Numeric" );
	//static private boolean debug = false;
	public static final String NAME = "numeric";
	
	public Numeric( String e, String a ) { super( e, a ); }
	
	public void  set( Float val ) {
		Fs.stringToFile(
				name( ent, attr, Overlay.MODE_WRITE ),
				Float.toString( val ));
	}
	public Float get( Float def ) {
		Float val = def;
		try {
			val = Float.valueOf( 
					Fs.stringFromFile( name( ent, attr, Overlay.MODE_READ ))
			);
		} catch (Exception e) {
			set( val ); // set a default
		}
		return val;
	}
	public boolean increase( String value ) {
		boolean rc = true;
		Float v = get( 0f );
		if (Float.isNaN( v )) {
			rc = false;
			audit.ERROR("Numeric.increase(): NaN found in "+ ent +"/"+ attr );
		} else {
			v += Float.valueOf( value );
			set( v );
		}
		return rc;
	}
	public boolean decrease( String value ) {
		boolean rc = true;
		Float v = get( 0f );
		if (Float.isNaN( v )) {
			rc = false;
			audit.ERROR("Numeric.decrease(): NaN found in "+ ent +"/"+ attr );
		} else {
			v -= Float.valueOf( value );
			set( v );
		}
		return rc;
	}
	static private String deref( String s ){
		try {
			s = Number.floatToString( Float.parseFloat( s ));
		} catch ( Exception e ) {
			if (s.equals( "-" )) s = "minus";
			else if (s.equals( "+" )) s = "plus";
			else if (s.equals( "*" )) s = "times";
			else if (s.equals( "/" )) s = "divided by";
		} // otherwise fail silently!
		return s;
	}
	static public Strings deref( Strings sa ){
		ListIterator<String> i = sa.listIterator();
		while ( i.hasNext())
			i.set( deref( i.next()));
		return sa;
	}
	static private String usage( Strings a ) {
		System.out.println(
				"Usage: numeric [set|get|remove|increase|decrease|exists|equals|delete] <ent> <attr>[ / <attr> ...] [<values>...]\n"+
				"given: "+ a.toString( Strings.CSV ));
		return Shell.FAIL;
	}
	static public String interpret( Strings a ) {
		// interpret( ["increase", "device", "textSize", "4"] )
		audit.in( "interpret", a.toString( Strings.DQCSV ));
		String rc = Shell.SUCCESS;
		if (a.size() > 1) {
			String cmd = a.get( 0 );
			
			if (cmd.equals("isAbs")) { // => Numeric.java?
				char firstChar = a.get( 1 ).charAt( 0 ); 
				rc = firstChar == '-' || firstChar == '+' ? Shell.FAIL : Shell.SUCCESS;
				
			} else if (cmd.equals( "evaluate" )) {
				ListIterator<String> ai = a.normalise().listIterator();
				if (ai.hasNext()) {
					ai.next(); // read over command
					Number number = Number.getNumber( ai );
					rc = number.valueOf() + a.copyAfter( 1 + number.representamen().size()).toString( Strings.SPACED );
				} else
					rc = Shell.FAIL;
				
			} else if (a.size() > 2) {
				int i = 2;
				String entity = a.get( 1 ), attribute = null;
				if (i<a.size()) { // components? martin car / body / paint / colour red
					attribute = a.get( i );
					while (++i < a.size() && a.get( i ).equals( "/" ))
						attribute += ( "/"+ a.get( i ));
				}
				Numeric n = new Numeric( entity, attribute );

				// [ "4", "+", "3" ] => [ "7" ] ???? at some point!
				ListIterator<String> ai = a.listIterator();
				String value = Number.getNumber( ai ).valueOf(); /* <<<< this could 
				 * have "another", coz it has context. In practice, this is a single figure for 
				 * increase or decrease.
				 */
				
				if (cmd.equals( "increase" )) 
					rc = n.increase( value ) ? Shell.SUCCESS : Shell.FAIL;
				else if (cmd.equals( "decrease" ))
					rc = n.decrease( value ) ? Shell.SUCCESS : Shell.FAIL;
				else
					rc = usage( a );
			}
		} else
			rc = usage( a );
		audit.out( rc );
		return rc;
}	}