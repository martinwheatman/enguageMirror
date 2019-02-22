package org.enguage.util.attr;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;

public class Attribute {
	
	// TODO: these don't seem to swap yet :( -- see colloquia.txt
	static public final char   DEF_QUOTE_CH  = '\''; // '"';  //
	static public final String DEF_QUOTE_STR = "'"; // "\""; //
	static public final char   ALT_QUOTE_CH  = '"'; // '\''; //
	static public final String ALT_QUOTE_STR = "\""; //  "'";  //
	
	//static private      Audit  audit         = new Audit( "Attribute" );
	
	private static String valueFromAttribute( String s ) {
		// takes value from name='value' -- 
		String stripped = s;
		int n = s.indexOf( "=" );
		if (-1 != n) {
			stripped = s.substring( 1+n );
			char quoteCh = stripped.charAt( 0 );
			if (quoteCh == stripped.charAt( stripped.length() - 1) &&
				(	quoteCh == ALT_QUOTE_CH
				 ||	quoteCh == DEF_QUOTE_CH   
				)	)
				stripped = Strings.trim( stripped, quoteCh );
		}
		return stripped;
	}

	private char quote = DEF_QUOTE_CH;
	private char quote() { return quote; }
	private void quote( char ch ) { quote = ch; }
	static private char quote( String value ) {
		return value.indexOf( DEF_QUOTE_STR ) == -1 ? DEF_QUOTE_CH : ALT_QUOTE_CH;
	}
	
	protected String name;
	public    String name() { return name; }
	public    Attribute name( String nm ) { name = nm; return this;}
	
	protected String    value;
	public    String    value() { return value; }
	public    Strings   values() { return new Strings( value ); }
	public    String    value( boolean expand ) { return expand ? getValue( value ) : value; }
	public    Attribute value( String s ) {
		value = s; // TODO: TBC - what if "martin" -> '"martin"' => :'"martin"': ???
		quote( quote( value )); // set the quote value against the content
		return this;
	}
	
	public Attribute( String nm, String val ) { name( nm ).value( val ); }
	public Attribute( String s ) { this( getName( s ), isAttribute( s ) ? valueFromAttribute( s ) : "" );}
	static public Attribute getAttribute( ListIterator<String> si ) {
		return new Attribute( si.hasNext() ? si.next() : "" );
	}
	
	static public String asString( String name, String value ) {
		return asString(      // quotes are this way round for a reason!
				name,

				value.indexOf( ALT_QUOTE_STR ) == -1 ? ALT_QUOTE_CH : DEF_QUOTE_CH,
				value );
	}
	static public String asString( String name, char quote, String value ) {
		return name +"="+ quote + value + quote;
	}
	public String toString() { return toString( quote() ); }
	public String toString( char quote ) { return asString( name, quote, value );}
	
	/* In these strip helpers - we may have a value "to x='go to town'"
	 * so we need to strip the value of x within this value...
	 */
	static public String getName( String s ) {
		int n;
		return -1 != (n = s.indexOf("=")) ? s.substring( 0, n ) : s;
	}
	static public  String  getValue(  String s ) { return getValues( s ).toString(); }
	static public  Strings getValues( String s ) {
		// "fred" => "fred" || "name='value'" => "value" || name='v1 n2="v2" v3' => "v1 v2 v3"
		Strings rc = new Strings();
		for (String item : new Strings( s ).contract( "=" ))
			if (isAttribute( item ))
				rc.addAll( getValues( new Attribute( item ).value() ));
			else
				rc.add(  item );
		return rc;
	}
	private static boolean isAlphabetic( String s ) {
		int sz = s.length();
		for (int i=0; i<sz; i++) {
			char ch = s.charAt( i ); 
			if (Character.getType( ch ) != Character.LOWERCASE_LETTER
				&& Character.getType( ch ) != Character.UPPERCASE_LETTER )
				return false;
		}
		return true;
	}
	static public boolean isAttribute( String s ) {
		// this defines attribute as:xxxxx='y y y y y'
		// this, ultimately,  may be:xxxxxx="y y y y xxxx='y y y's y'"
		Strings sa = new Strings( s );
		return (sa.size() == 3)
				&& isAlphabetic( sa.get( 0 ))
				&& sa.get( 1 ).equals( "=" );
	}
	// --- test code
	static private String attrTest( String s ) {
		return s +" "+ (isAttribute( s ) ? "IS":"is NOT") +" an attribute";
	}
	static public void main( String argv[]) {
		//Audit.turnOn();
		Audit.log( attrTest( "fred=bill" ));
		Audit.log( attrTest( "fred='bill'" ));
		Attribute a = new Attribute( "fred=\"bill\"" );
		Audit.log( "a is "+ a.toString());
		a = new Attribute( "fred='bill'" );
		Audit.log( "a is "+ a.toString());
		a = new Attribute( "fred=bi'll" );
		Audit.log( "a is "+ a.toString());
		Strings sa = new Strings("list get user='martin' attr='needs to x=\"go to town\" y=\"for martin's coffee\"\'");
		sa.contract( "=" );
		Audit.log("Sofa.doCall() => sa is "+ sa.toString());
		for (int i=0; i<4 && i<sa.size(); i++)
			sa.set( i, Attribute.getValues( sa.get( i )).toString( Strings.SPACED ));
		Audit.log("Sofa.doCall() => sa is "+ sa.toString());
		sa = sa.normalise();
		Audit.log("Sofa.doCall() => sa is "+ sa.toString());
		
}	}