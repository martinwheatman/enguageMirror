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
	public String get() { return cache.get( name ); }
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
	static public void   set( String name, String val ) { new Variable( name ).set( val );}
	static public void unset( String name ) { new Variable( name ).unset(); }
	static public String get( String name ) { return cache==null ? "" : cache.get( name ); }
	static public String get( String name, String def ) {
		String value = cache==null? null : cache.get( name );
		return value==null || value.equals("") ? def : value;
	}
	static public boolean isSet( String name ) {
		String val = get( name );
		return val != null && !val.equals("");
	}
	
	static public Strings deref( String name ) {
		// must return strings for case where variable value is 'hello world'
		// must contract( "=" ) for case where 'name' is "SUBJECT='fred'"
		// this should ignore $SUBJECT
		/* TODO need to deref values: x='$VAR' to return x='val' ?
		 */
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
		String rc = Shell.IGNORE;
		if (args.get( 0 ).equals( "set" ) && args.size() > 2)
			set( args.get( 1 ), args.copyAfter( 1 ).toString( Strings.SPACED ));
		else if (args.get( 0 ).equals( "unset" ) && args.size() > 1)
			unset( args.get( 1 ));
		else if (args.get( 0 ).equals( "exists" ) && args.size() > 1)
			isSet( args.get( 1 ));
		else if (args.get( 0 ).equals( "get" ) && args.size() > 1)
			rc = get( args.copyAfter( 1 ).toString( Strings.SPACED ));
		else if (args.get( 0 ).equals( "show" )) {
			audit.log( "printing cache" );
			printCache();
			audit.log( "printed" );
			rc = Shell.SUCCESS;
		} else
			rc = Shell.FAIL;
		return audit.out( rc );
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
			spk.set( "fred" );
			tmp = spk.get();
			audit.log( "now="+ (tmp==null?"<null>":tmp));
			printCache();
			//*			
			Variable.set( "HELLO", "there" );
			audit.log( "hello is "+ Variable.get( "HELLO" ) +" (there=>pass)" );
			Variable.unset( "HELLO" );
			audit.log( "hello is "+ Variable.get( "HELLO" ) +" (null=>pass)" );
}	}	}
