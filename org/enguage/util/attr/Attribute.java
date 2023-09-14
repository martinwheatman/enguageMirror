package org.enguage.util.attr;

import java.util.ListIterator;

import org.enguage.sign.symbol.config.Plural;
import org.enguage.sign.symbol.when.Moment;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.token.Token;
import org.enguage.util.token.TokenStream;

public class Attribute {
	
	static private      Audit  audit         = new Audit( "Attribute" );
	
	// TODO: these don't seem to swap yet :( -- see colloquia.txt
	static public final char   DEF_QUOTE_CH  = '\''; // '"';  //
	static public final String DEF_QUOTE_STR = "'"; // "\""; //
	static public final char   ALT_QUOTE_CH  = '"'; // '\''; //
	static public final String ALT_QUOTE_STR = "\""; //  "'";  //
	
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
		if (value != null) quote( quote( value )); // set the quote value against the content
		return this;
	}
	// this retrieves a parameter value from a name parameter
	// e.g. parameter="value" => "value"
	//      value             => "value"
	static public Strings value( Strings from ) {
		if (from.size() == 1 && Attribute.isAttribute( from.get( 0 ) ))
			from = new Strings( new Attribute( from.get( 0 )).value() );
		return from;
	}
	//
	public boolean contains(String nm, String val) {
		return name.equals( nm ) && value.contains( val );
	}
	
	// ---
	public static boolean isAttribute( String s ) {
		// this defines attribute as:xxxxx='y y y y y'
		// this, ultimately,  may be:xxxxxx="y y y y xxxx='y y y's y'"
		Strings sa = new Strings( s );
		return (sa.size() == 3)
				&& Strings.isAlphabetic( sa.get( 0 ))
				&& sa.get( 1 ).equals( "=" );
	}
	private static String valueFromAttribute( String s ) {
		// takes "value" from: "name='value'"
		int n = s.indexOf( "=" );
		return -1 != n ? Strings.stripAttrQuotes( s.substring( 1+n )) : s;
	}
	// --- c'tors --- 
	public Attribute( String s ) { this( getName( s ), isAttribute( s ) ? valueFromAttribute( s ) : "" );}
	public Attribute( String nm, String val ) {
		name( nm );
		if (nm.equals( When.ID )) { // this code is linked to Item::getFormatComponentValue()
			When w = Moment.valid( val ) ?
					new When( new Moment( Long.valueOf( val ))) : // e.g. 2020012888888
					When.getWhen( new When(), new Strings( val )); //e.g. 'yesterday'
			value( w.toString() );
		} else
			value( val );
	}
	/* --- GET NAME:  name = "value"
	 * name ::= <alphas>['-'<alphas>|] -> '=' (or '/' or '>' ?)
	 * 
	 * Using real data from wikipedia (from TokenStream) attribute names
	 * often have hypenated names, particularly aria names. 
	 * Separating this out here, into getName for both ListIterator and
	 * TokenStreams.
	 */
	private static String getName( TokenStream ts ) {
		Token token;
		StringBuilder sb = new StringBuilder();
		while (ts.hasNext()) {
			token = ts.getNext();
			if (token.string().matches( "[a-zA-Z_]+")) {
				sb.append( token.string() );
				
				if (ts.hasNext()) { // do '-' and loop around
					token = ts.getNext();
					if (token.string().equals( "-" )) {
						sb.append( token.string() );
					} else { // '=' ???
						ts.putBack();
						break;
				}	}
				
			} else { // '/' OR '>' ???
				ts.putBack();
				break;
		}	}
		return sb.toString();
	}
	private static String getName( ListIterator<String> si ) {
		// name ::= <alphas>['-'<alphas>|] -> '=' (or '/' or '>' ?)
		StringBuilder sb = new StringBuilder();
		while (si.hasNext()) {
			String name = si.next();
			if (name.matches( "[a-zA-Z_]+")) {
				sb.append( name );
				
				if (si.hasNext()) {
					name = si.next();
					if (name.equals( "-" )) {
						sb.append( name );
					} else { // '=' ???
						si.previous();
						break;
				}	}
				
			} else {
				si.previous(); // '/' OR '>'
				break;
		}	}
		return sb.toString();
	}
	public static Attribute next( ListIterator<String> si ) {
		Attribute a = null;
		String name = getName( si );
		if (!name.equals( "" )) {
			
			if (si.hasNext()) {
				if (!si.next().equals( "=" ))
					si.previous();
			
				else if (si.hasNext())
					a = new Attribute(
							name,
							Strings.trim(
									Strings.trim( si.next(), Attribute.DEF_QUOTE_CH ),
									Attribute.ALT_QUOTE_CH
					)		);
		}	}
		return a;
	}
	static public Attribute next( TokenStream ts ) {
		Attribute a = null;
		String name = getName( ts );
		if (!name.equals( "" )) {
				
			if (ts.hasNext()) {
				Token token = ts.getNext();
				if (!token.string().equals( "=" ))
					ts.putBack();
				
				else if (ts.hasNext())
					a = new Attribute(
							name, 
							Strings.trim(
									Strings.trim( ts.getNext().string(), Attribute.DEF_QUOTE_CH ),
									Attribute.ALT_QUOTE_CH
					)		);
		}	}
		return a;
	}
	
	// -- toString
	static private String asString( String name, char quote, String value ) {
		return name + (value==null?"":"="+ quote + value + quote);
	}
	static public String asString( String name, String value ) {
		return asString(      // quotes are this way round for a reason!
				name,
				value.indexOf( ALT_QUOTE_STR ) == -1 ? ALT_QUOTE_CH : DEF_QUOTE_CH,
				value );
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
	static public Strings expand23( Strings a ) {
		// expand 2nd and 3rd attribute parameters
		ListIterator<String> ci = a.listIterator();
		if (ci.hasNext()) {
			ci.next();                          // ignore 0
			if (ci.hasNext()) {
				String attr = ci.next();               // 1
				ci.set( Attribute.getValue( attr ));
				if (ci.hasNext()) {
					attr = ci.next();                  // 2
					ci.set( Attribute.getValue( attr ));
		}	}	}
		return a;
	}
	static public Strings expand( Strings a ) {
		// expand all attribute parameters
		ListIterator<String> ci = a.listIterator();
		while (ci.hasNext())
			ci.set( Attribute.getValue( ci.next() ));
		return a;
	}
	public boolean equalsIgnoreCase( Attribute cmp ) {
		return this.name.equalsIgnoreCase( cmp.name ) &&
						Plural.singular( this.value )
				.equalsIgnoreCase(
						Plural.singular( cmp.value )
					);
	}
	// --- test code
	static private String attrTest( String s ) {
		return s +" "+ (isAttribute( s ) ? "IS":"is NOT") +" an attribute";
	}
	static public void main( String argv[]) {
		//Audit.turnOn();
		audit.debug( attrTest( "fred=bill" ));
		audit.debug( attrTest( "fred='bill'" ));
		Attribute a = new Attribute( "fred=\"bill\"" );
		audit.debug( "a is "+ a.toString());
		a = new Attribute( "fred='bill'" );
		audit.debug( "a is "+ a.toString());
		a = new Attribute( "fred=bi'll" );
		audit.debug( "a is "+ a.toString());
		Strings sa = new Strings("list get user='martin' attr='needs to x=\"go to town\" y=\"for martin's coffee\"\'");
		sa.contract( "=" );
		audit.debug("Sofa.doCall() => sa is "+ sa.toString());
		for (int i=0; i<4 && i<sa.size(); i++)
			sa.set( i, Attribute.getValues( sa.get( i )).toString( Strings.SPACED ));
		audit.debug("Sofa.doCall() => sa is "+ sa.toString());
		sa = sa.normalise();
		audit.debug("Sofa.doCall() => sa is "+ sa.toString());
		
		Audit.on();
		Attribute attr;
		Strings s = new Strings( "martin='heroic' ruth='fab'" );
		audit.debug( "Test string is; ["+ s.toString()+"]");
		audit.debug( "Test strings are; ["+ s.toString( Strings.CSV )+"]");
		ListIterator<String> si = s.listIterator();
		while (null != (attr = Attribute.next( si )))
			audit.debug( "Copy is: >"+ attr.toString() +"<");
}	}
