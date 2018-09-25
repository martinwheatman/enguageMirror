package org.enguage.util.attr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.interp.pattern.Pattern;
import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.Plural;

public class Attributes extends ArrayList<Attribute> {
	static private Audit audit = new Audit( "Attributes" );
	static final long serialVersionUID = 0;
	
	public Attributes() { super(); }
	public Attributes( Attributes orig ) { super( orig ); }
	public Attributes( Strings sa ) { this( sa.toString()); }
	public Attributes( String s ) {
		super();
		int i=0, sz=s.length();
		while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
		while (i<sz && Character.isLetter( s.charAt( i ) )) {
			String name = "", value = "";
			
			while (i<sz && Character.isLetterOrDigit( s.charAt( i ) )) name += Character.toString( s.charAt( i++ ));
			while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++ ; // read over spaces
			
			if (i<sz && '=' == s.charAt( i )) { // look for a value
				i++; // read over '='
				while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
				if (i<sz && (Strings.SINGLE_QUOTE == s.charAt( i ) ||
						     Strings.DOUBLE_QUOTE == s.charAt( i )   )) {
					Character quoteMark = '\0';
					do {
						if (i<sz) quoteMark = s.charAt( i++ ); // save and read over " or '
						while ( i<sz && quoteMark != s.charAt( i ))
							value += Character.toString( s.charAt( i++ ));
						i++; //read over end quote
						while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
						if (i<sz && quoteMark == s.charAt( i )) value += "\n"; // split multi-line values
					} while (i<sz && quoteMark == s.charAt( i )); // inline const str found e.g. .."   -->"<--...
				}
				add( new Attribute( name, value )); // was append( NAME, value );
			}
			while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
		}
		nchars = i;
	}
	
	// save the number of chars read in creating attributes...
	private int  nchars = 0;
	public  int  nchars() { return nchars;}
	public  void nchars( int n ) { nchars = n;}
	
	public void toVariables() {
		for (Attribute m : this)
			Variable.set( m.name(), m.value());
	}
	public boolean matches( Attributes pattern ) {
		// Sanity check: pattern will have less content than target.
		if (pattern.size() > size()) return false;
		
		Iterator<Attribute> pi = pattern.iterator();
		while (pi.hasNext()) {
			Attribute a = pi.next();
			if (!a.value().equalsIgnoreCase( get( a.name() )))
				return false;
		}
		return true;
	}
	public boolean has( String name ) {
		Iterator<Attribute> i = iterator();
		while (i.hasNext())
			if (i.next().name().equalsIgnoreCase( name ))
				return true;
		return false;
	}
	public Attributes add( String name, String value ) {
		add( new Attribute( name, value ));
		return this;
	}
	public String get( String name ) {
		Attribute a;
		Iterator<Attribute> i = iterator();
		while (i.hasNext()) {
			a = i.next();
			if (a.name().equalsIgnoreCase( name ))
				return a.value();
		}
		return "";
	}
	public Attributes replace( String name, String value ) {
		remove( name );
		add( new Attribute( name, value ));
		return this;
	}
	public String remove( String name ) {
		Iterator<Attribute> i = iterator();
		while (i.hasNext()) {
			Attribute a = i.next();
			if (a.name().equals( name )) {
				String tmp = a.value();
				i.remove();
				return tmp;
		}	}
		return "";
	}
	public int removeAll( String name ) {
		int rc = 0;
		Iterator<Attribute> i = iterator();
		while (i.hasNext())
			if (i.next().name().equals( name )) {
				rc++;
				i.remove();
			}
		return rc;
	}
	
	public String toString( String sep ) {
		if (null == sep) sep = "";
		String s = "";
		Iterator<Attribute> ai = iterator();
		while (ai.hasNext())
			s += sep + ai.next().toString();
		return s;
	}
	public String toString() { return toString( " " ); }

	public static boolean isUpperCase( String s ) {return s.equals( s.toUpperCase( Locale.getDefault()));}
	public static boolean isAlphabetic( String s ) {
		for ( int i=0; i< s.length(); i++ ) {
			int type = Character.getType( s.charAt( i ));
			if (type == Character.UPPERCASE_LETTER ||
				type == Character.LOWERCASE_LETTER   )
				return true;
		}
		return false;
	}
	public static Strings reflect( Strings values ) {
		values.replaceIgnoreCase( "i", "fgh" );
		values.replaceIgnoreCase( "you",  "i" );
		values.replaceIgnoreCase( "fgh",  "you" );
		Strings first = new Strings( "i are" ),
				second = new Strings( "you are" ),
				tmp = new Strings( "fgh dfg" );
		values.replace( first, tmp );
		values.replace( second, first );
		values.replace( tmp,  second );
		Strings firsta = new Strings( "you am" ),
				seconda = new Strings( "you are" ),
				tmpa = new Strings( "fgh dfg" );
		values.replace( firsta, tmpa );
		values.replace( seconda, firsta );
		values.replace( tmpa,  seconda );

		return values;
	}
	// BEVERAGE -> coffee + [ NAME="martins", beverage="tea" ].deref( "SINGULAR-NAME needs a $BEVERAGE" );
	// => martin needs a coffee.
	private String derefName( String name, boolean expand ) { // hopefully non-blank string
		String value = null;
		if (null != name && name.length() > 0 ) {
			String orig = name;
			// if we have QUOTED-X, retrieve X and leave answer QUOTED
			// [ x="martin" ].derefChs( "QUOTED-X" ) => '"martin"'
			boolean quoted = name.contains( Pattern.quotedPrefix ),
					plural = name.contains( Pattern.pluralPrefix ),
					external = name.contains( Pattern.externPrefix );
			
			// remove all prefixes...
			name = name.substring( name.lastIndexOf( "-" )+1 );
			// do the dereferencing...
			if ( isAlphabetic( name ) && isUpperCase( name )) {
				value = get( name.toLowerCase( Locale.getDefault() ));
				if (expand && !value.equals( "" ))
					value = Attribute.asString( name.toLowerCase( Locale.getDefault() ), value );
			}
			if (value == null || value.equals( "" ))
				value = orig;
			else {
				if (external)
					value = reflect( new Strings(
								Attribute.isAttribute( value ) ? new Attribute( value ).value() : value
							)).toString();
				
				if (plural)   value = Plural.plural( value );
				if (quoted)   value = Attribute.DEF_QUOTE_CH+ value +Attribute.DEF_QUOTE_CH;
				if (Audit.detailedOn) audit.debug( "Attributes.deref( "+ name +"='"+ value +"' )" );
				//I'd like to have:
				//   I'm meeting whom="James" where="home"
				//but returning below is not a good idea.
				//value = name +"='"+ value +"' ";
				// Look to sofa to expand WHOM WHERE
		}	}
		return value;
	}
	public Strings deref( Strings ans ) { return deref( ans, false ); } // backward compatible
	public Strings deref( Strings ans, boolean expand ) {
		if (null != ans) {
			ListIterator<String> i = ans.listIterator();
			while (i.hasNext())
				i.set( derefName( i.next(), expand ));
		}
		return ans;
	}
	public String deref( String value ) { return deref( value, false ); }
	public String deref( String value, boolean expand ) {
		return deref( new Strings( value ), expand ).toString( Strings.SPACED );
	}
	public static Strings expandValues( Strings sa ) {
		ListIterator<String> si = sa.listIterator();
		while (si.hasNext()) {
			String s = si.next();
			si.set( Attribute.getValue( s ));
		}
		return sa;
	}
	// ---- join():  => 
	public ArrayList<ArrayList<Strings>> valuesAsLists( String sep ) {
		/* Called from join -- note plurality of attribute loaded:
		 * ([ SUBJECT="martin and ruth", OBJECTS="coffee and decaf tea" ], "and" ) =>
		 *      [[[martin and ruth], [coffee], [decaf tea]]
		 */
		/* Here we have raw arrays, of values and loaded. To limit the join combinations,
		 * without doing anything too smart, limit dimensions to loaded which appear plural.
		 * This puts into the users hands which values are joined.
		 */
		ArrayList<ArrayList<Strings>> rc = new ArrayList<ArrayList<Strings>>();
		for (Attribute a : this ) {
			Strings ss = new Strings( a.value());
			ArrayList<Strings> als ;
			if (Plural.isPlural( a.name().toLowerCase( Locale.getDefault()) )) {
				als = ss.divide( sep, false ); // seps are "," and "and" -- not too interesting!
			} else {
				als = new ArrayList<Strings>();
				als.add( ss );
			}
			
			rc.add( als );
		}
		return rc;
	}
	public Strings names() {
		Strings na = new Strings();
		Iterator<Attribute> li = this.iterator();
		while (li.hasNext())
			na.add( li.next().name());
		return na;
	}
	public static void main( String argv[]) {
		Audit.allOn();
		Attributes b, a = new Attributes();
		a.add( new Attribute( "martin", "heroic" ));
		a.add( new Attribute( "ruth", "fab" ));
		audit.log( "Initial test: "+ a.toString());
		audit.log( "\tmartin is "+  a.get( "martin" ));
		audit.log( "\truth is "+   a.get( "ruth" ));
		audit.log( "\tjames is "+  a.get( "james" ));
		audit.log( "\tderef martin is "+  a.deref( "what is MARTIN" ));
		
		audit.log( "\tremoving "+ a.remove( new Attribute( "martin" )));
		audit.log( "\ta is now:"+ a.toString());
		audit.log( "\tshould be just ruth='fab'" );
		
		a = new Attributes();
		a.add( new Attribute( "X", "3" ));
		b = new Attributes();
		a.add( new Attribute( "X", "3" ));
		if (a.matches( b ))
			audit.log( "matched" );
		else
			audit.log( "not mathing" );
}	}
