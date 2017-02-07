package com.yagadi.enguage.object;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Attribute {
	static private Audit audit = new Audit( "Attribute" );
	
	private static String valueFromAttribute( String s ) {
		// takes value from name='value' -- 
		String stripped = s;
		int n = s.indexOf( "=" );
		if (-1 != n) {
			stripped = s.substring( 1+n );
			char quoteCh = stripped.charAt( 0 );
			if (quoteCh == stripped.charAt( stripped.length() - 1) &&
				(	quoteCh == '"' ||
					quoteCh == ':' ||
					quoteCh == '\''   )
				)
				stripped = Strings.trim( stripped, quoteCh );
		}
		return stripped;
	}

	private char quote ='\'';
	private char quote() { return quote; }
	private void quote( char ch ) { quote = ch; }
	
	protected String name;
	public    String name() { return name; }
	public    Attribute name( String nm ) { name = nm; return this;}
	
	protected String    value;
	public    String    value() { return value; }
	public    String    value( boolean expand ) { return expand ? expandValues( value ).toString( Strings.SPACED ) : value; }
	public    Attribute value( String s ) {
		value = s;
		quote( value.indexOf( "'") == -1 ? '\'' : value.indexOf( "\"") == -1 ? '"' : ':' );
		return this;
	}
	public Attribute( String nm, String val ) { name( nm ).value( val ); }
	public Attribute( String s ) { this( nameFromAttribute( s ), isAttribute( s ) ? valueFromAttribute( s ) : "" );}
	
	public String toString() { return toString( quote() ); }
	public String toString( char quote ) {
		String chars = name +"="+ quote;
		for (int i=0, sz=value.length(); i<sz; i++)
			if ('\n' == value.charAt( i ))
				chars += quote +"\n      "+ quote;
			else
				chars += Character.toString( value.charAt( i ));
		return chars += quote;
	}
	public boolean equals( String s ) { return name.equals( s );}
	
	/* In these strip helpers - we may have a value "to x='go to town'"
	 * so we need to strip the value of x within this value...
	 */
	static public String nameFromAttribute( String s ) {
		int n;
		return -1 != (n = s.indexOf("=")) ? s.substring( 0, n ) : s;
	}
	static public Strings expandValues( String s ) {
		//audit.traceIn( "expandValues", "s="+s );
		Strings rc = new Strings();
		for (String item : new Strings( s ).contract( "=" ))
			if (isAttribute( item ))
				rc.addAll( expandValues( new Attribute( item ).value() ));
			else
				rc.add(  item );
		//audit.traceOut( rc );
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
//	private static boolean isQuotedString( String s ) {
//		char ch = s.charAt( 0 );
//		return (s.charAt( s.length()-1 ) == ch) && (ch == '\'' || ch=='"' || ch==':');
//	}
	static public boolean isAttribute( String s ) {
		// this defines attribute as:xxxxx='y y y y y'
		// this, ultimately,  may be:xxxxxx="y y y y xxxx='y y y's y'"
		Strings sa = new Strings( s );
		//audit.audit("sa is "+ sa.toString());
		return (sa.size() == 3)
				&& isAlphabetic( sa.get( 0 ))
				&& sa.get( 1 ).equals( "=" );
				//&& isQuotedString( sa.get( 2 ));
	}
	static public String attrTest( String s ) {
		return s +" "+ (isAttribute( s ) ? "IS":"is NOT") +" an attribute";
	}
	static public void main( String argv[]) {
		//Audit.turnOn();
		/* --- */
		//audit.audit( attrTest( "fred" ) );
		audit.log( attrTest( "fred=bill" ));
		//audit.audit( attrTest( "f ed=\"bill\"" ));
		//audit.audit( attrTest( "fred=\"bill\"" ));
		audit.log( attrTest( "fred='bill'" ));
		Attribute a = new Attribute( "fred=\"bill\"" );
		audit.log( "a is "+ a.toString());
		a = new Attribute( "fred='bill'" );
		audit.log( "a is "+ a.toString());
		a = new Attribute( "fred=bi'll" );
		audit.log( "a is "+ a.toString());
		//audit.audit( attrTest( "fred=:bill:" ));
		 /* 
		audit.audit( "===" );
		Attribute a = new Attribute( "name", "value" );
		audit.audit( "a is:"+ a.toString() +":");
		audit.audit( attrTest( a.toString() ));
		audit.audit( "value is "+ expandValues( a.toString()));
		audit.audit( "===" );
		audit.audit( "a is "+ a.value( "to x='go to town' y='for a coffee'" ).toString());
		audit.audit( "value is '"+ a.value( false ) +"'");
		audit.audit( "---" );
		audit.audit( "value is '"+ a.value(  true ) +"'");
		// */
		Strings sa = new Strings("list get user='martin' attr='needs to x=\"go to town\" y=\"for martin's coffee\"'");
		sa.contract( "=" );
		audit.log("Sofa.doCall() => sa is "+ sa.toString());
		for (int i=0; i<4 && i<sa.size(); i++)
			sa.set( i, Attribute.expandValues( sa.get( i )).toString( Strings.SPACED ));
		audit.log("Sofa.doCall() => sa is "+ sa.toString());
		sa = sa.normalise();
		audit.log("Sofa.doCall() => sa is "+ sa.toString());
		
}	}