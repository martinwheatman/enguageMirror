package com.yagadi.enguage.object;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Variable {
	/* As part of the Sofa library, variable manages persistent values:
	 * /a/b/c/_NAME -> value, which is the persistent equivalent of NAME="value".
	 * Because $ has special significance in the filesystem/shell
	 * prefix variables with '_'
	 */
	static private Audit   audit = new Audit( "Variable" );
	static public  String   NAME = "variable";
	
	static private char intPrefix = '$';
	static public  void intPrefix( char s ) { intPrefix = s; }
	static public  char intPrefix( ) { return intPrefix; }
	
	static private char extPrefix = '_';
	static public  void extPrefix( char s ) { extPrefix = s; }
	static public  char extPrefix( ) { return extPrefix; }

	private static TreeMap<String,String> cache = encache();
	private static TreeMap<String,String> encache() { return encache( Overlay.Get() );}
	private static TreeMap<String,String> encache( Overlay o ) {
		audit.in( "encache", Ospace.location());
		cache = new TreeMap<String,String>();
		for( String name : o.list( NAME ))
			if (name.equals( externalise( name ) )) // if valid variable name
				cache.put( name, new Value( NAME, name ).getAsString());
		audit.out();
		return cache;
	}
	static private void printCache() {
		audit.title( "Printing cache" );
		Set<Map.Entry<String,String>> entries = cache.entrySet();
		Iterator<Map.Entry<String,String>> ei = entries.iterator();
		Audit.incr();
		while (ei.hasNext()) {
			Map.Entry<String,String> e = ei.next();
			audit.log( e.getKey()  +"='"+  e.getValue()  +"'" );
		}
		Audit.decr();
	}

	static private String externalise( String name ) {
		/* Variable should only attempt to set UPPERCASE values
		 * so normal lower case boilerplate does not get dereffed by mistake.
		 * JIC set to upper case anyway!
		 */
		/* This used to be prefixed by "_" 
		 *  if (name.charAt( 0 ) == intPrefix ) // if prefixed
		 *  	name = extPrefix + name.substring( 1 );   // $NAME -> _NAME
		 *  else if (extPrefix != name.charAt( 0 ))
		 *  	name = extPrefix + name;                  //  NAME -> _NAME
		 * to uppercase will do
		 */
		// to prevent $SUBJECT being created
		if (name.charAt( 0 ) == intPrefix) name = name.substring( 1 );
		return name.toUpperCase( Locale.getDefault());
	}
	
	String name;
	Value  value;
	
	public Variable( String nm ) {
		name = externalise( nm );
		value = new Value( NAME, name );
		cache.put( name, value.getAsString());
	}
	public Variable( String nm, String val ) {
		name = externalise( nm );
		value = new Value( NAME, name );
		set( val );
	}
	public String     get() { return cache.get( name ); }
	public boolean  isSet( String value ) { return cache.get( name ).equals( value );}
	public Variable   set( String val ) {
		cache.put( name, val );
		value.set( val );
		return this;
	}
	public Variable unset() {
		cache.remove( name );
		value.ignore();
		return this;
	}
	
	// for backward compatibility, keeping these statics 
	static public String set( String name, String val ) { new Variable( name ).set( val ); return val;}
	static public void unset( String name ) { new Variable( name ).unset(); }
	static public String get( String name ) { return cache.get( name ); } // raw name
	static public String get( String name, String def ) {
		String value = cache.get( name );   // raw name, so "compass" not set, but "COMPASS" is
		return value==null || value.equals("") ? def : value;
	}
	static public boolean isSet( String name, String value ) {
		String val = new Variable( name ).get();
		return  val != null &&
				((value == null && !val.equals( "" )  ) ||
				 (value != null && val.equals( value ))   );
	}
	public static String derefUc( String in ) {
		String out = "", word = "";
		char[] buffer = in.toCharArray();
		for (char ch : buffer) {
			if (Character.isUpperCase( ch ))
				word += ch;
			else {
				if (!word.equals( "" )) {
					word = Variable.get( word );
					if (word == null) word = "";
					out += word;
					word = "";
				}
				out += ch;
		}	}
		return out;
	}

	static public Strings deref( String name ) {
		// must return strings for case where variable value is 'hello world'
		// must contract( "=" ) for case where 'name' is "SUBJECT='fred'"
		// this should ignore $SUBJECT
		Strings rc = ( (null != name
				&& !name.equals("")
				&& name.charAt( 0 ) != intPrefix) ?
						new Strings( get( name, name )) :
						new Strings( name ));
		return rc.contract( "=" );
	}
	static public Strings deref( Strings a ) {
		//audit.traceIn( "deref", a.toString());
		Strings b = new Strings();
		Iterator<String> ai = a.iterator();
		while (ai.hasNext())
			b.addAll( deref( ai.next() )); // preserve name='value'
		//audit.traceOut( b );
		return b;
	}
	
	static public String interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String  rc = Shell.SUCCESS,
		       cmd = args.remove( 0 );
		int sz = args.size();
		if (sz > 0) {
			String name = args.remove( 0 );
			if (sz > 1)
				if (cmd.equals( "set" ))
					rc = set( name, args.toString() );
				else if (cmd.equals( "exists" ))
					rc = isSet( name, args.toString()) ? Shell.SUCCESS : Shell.FAIL;
				else
					rc = Shell.FAIL;
				
			else { // name and no params
				rc = Shell.IGNORE;
				if (cmd.equals( "exists" ))
					rc = isSet( name, null ) ? Shell.SUCCESS : Shell.FAIL;
				else if (cmd.equals( "unset" ))
					unset( name );
				else if (cmd.equals( "get" ))
					rc = get( name.toUpperCase( Locale.getDefault() ));
				else
					rc = Shell.FAIL;
		}	}
		else if (cmd.equals( "show" )) {
			audit.log( "printing cache" );
			printCache();
			audit.log( "printed" );
		} else
			rc = Shell.FAIL;
		return audit.out( rc = rc==null?"":rc );
	}
	
	// --
	public static void test( String cmd, String expected ) {
		String actual = interpret( new Strings( cmd ));
		if (actual.equals( expected ))
			if ( actual.equals( "" ))
				audit.log(   "PASS: "+ cmd );
			else
				audit.log(   "PASS: "+ cmd +" = '"+ actual +"'" );
		else
			audit.FATAL( "FAIL: "+ cmd +" = '"+ actual +"' (expected: "+ expected +")" );
	}
	public static void main( String args[] ) {
		if (!Overlay.autoAttach()) {
			audit.ERROR( "ouch: in Variable.java" );
		} else {
			Audit.allOn();
			Audit.detailedDebug = true;
			
			printCache();
			Variable spk = new Variable( "SPOKEN" );
			String tmp = spk.get();
			audit.log( "was="+ (tmp==null?"<null>":tmp));
			if ( tmp.equals( "fred" ))
				interpret( new Strings( "set SPOKEN billy boy" ));
			else
				spk.set( "fred" );
			tmp = spk.get();
			audit.log( "now="+ (tmp==null?"<null>":tmp));
			if ( spk.isSet( "fred" ))
				audit.log( "spk set to Fred" );
			else
				audit.log( "spk is set to Bill" );
			printCache();
			
			//*		Static test, backwards compat...
			test( "set hello there", Shell.SUCCESS );
			test( "get HELLO", "there" );
			test( "exists HELLO there", Shell.SUCCESS );
			audit.log( "deref: HELLO hello there="+ deref( new Strings( "HELLO hello there" )));
			test( "unset HELLO", "" );
			test( "get HELLO", "" );
			audit.log( "PASSED" );
}	}	}
