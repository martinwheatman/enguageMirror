package org.enguage.interp.pattern;

import java.util.ListIterator;
import java.util.Locale;

import org.enguage.util.Audit;
import org.enguage.util.Indent;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.vehicle.Language;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.reply.Reply;

public class Patte {
	private static Audit audit = new Audit( "Patternette" );
	
	
	// -- constructors...
	public Patte() {}
	public Patte( Strings pre, String nm ) {
		this();
		prefix( pre ).name( nm );
	}
	public Patte( Strings pre, String nm, Strings post ) {
		this( pre, nm );
		postfix( new Strings( post ));
	}
	//just a helper ctor for hardcoded Patternettes
	public Patte( String pre ) { this( new Strings( pre ), "" ); }
	public Patte( String pre, String nm ) { this( new Strings( pre ), nm );}
	public Patte( String pre, String nm, String pst ) { this( new Strings( pre ), nm, new Strings( pst ) );}

	private int  preSz  = 0;
	private int  postSz = 0;
	public  int  nconsts() {return preSz + postSz;}
	
	private Strings     prefix = new Strings();
	public  Strings     prefix() { return prefix; }
	public  Patte prefix( Strings s ) { prefix = s; preSz = prefix.size(); return this; }
	public  Patte prefix( String str ) {
		for (String s : new Strings( str ))
			prefix.append( s );
		preSz = prefix.size();
		return this;
	}

	private Strings     postfix = new Strings();
	public  Strings     postfix() { return postfix; }
	public  Patte postfix( Strings ss ) { postfix = ss; postSz = postfix.size(); return this; }
	public  Patte postfix( String str ) {
		for (String s : new Strings( str ))
			postfix.append( s );
		postSz = postfix.size();
		return this;
	}

	private boolean     named = false;
	private String      name = "";
	public  String      name() { return name; }
	public  Patte name( String nm ) { if (null != nm) {name = nm; named = !nm.equals("");} return this; }
	public  boolean     named() { return named;}

	// -- mutually exclusive attributes --:
	private boolean     isNumeric = false;
	public  boolean     isNumeric() { return isNumeric; }
	public  Patte numericIs( boolean nm ) { isNumeric = nm; return this; }
	public  Patte numericIs() { isNumeric = true; return this; }

	private boolean     isExpr = false;
	public  boolean     isExpr() { return isExpr; }
	public  Patte exprIs( boolean ex ) { isExpr = ex; return this; }
	public  Patte exprIs() { isExpr = true; return this; }

	private boolean     isQuoted = false;
	public  boolean     quoted() { return isQuoted;}
	public  Patte quotedIs( boolean b ) { isQuoted = b; return this; }
	public  Patte quotedIs() { isQuoted = true; return this; }
	
	private boolean     isPlural = false;
	public  boolean     isPlural() { return isPlural; }
	public  Patte pluralIs( boolean b ) { isPlural = b; return this; }
	public  Patte pluralIs() { isPlural = true; return this; }
	
	private boolean     isPhrased = false;
	public  boolean     isPhrased() { return isPhrased; }
	public  Patte phrasedIs() { isPhrased = true; return this; }
	
	private boolean     isGrouped = false;
	public  boolean     isGrouped() { return isGrouped; }
	public  Patte groupedIs() { isGrouped = true; return this; }
	
	private boolean     isSign = false;
	public  boolean     isSign() { return isSign; }
	public  Patte signIs() { isSign = true; return this; }
	
   // TODO: Apretrophed
	private String      isApostrophed = null;
	public  boolean     isApostrophed() { return isApostrophed != null; }
	public  Patte apostrophedIs( String s ) { isApostrophed = s; return this; }
	
	private boolean     isList = false;
	public  boolean     isList() { return isList; }
	public  Patte listIs() { isList = true; return this; }
	// --
	
	private String      conjunction = "";
	public  String      conjunction() { return conjunction; }
	public  Patte conjunction( String c ) { conjunction = c; return this; }
	
	public Attribute matchedAttr( String val ) {
		return new Attribute(
				name,
				Attribute.getValue( // prevents X="x='val'"
					name.equals("unit") ? Plural.singular( val ) : val
				));
	}
	
	public boolean isEmpty() { return name.equals("") && prefix().size() == 0; }

	public boolean invalid( ListIterator<String> ui  ) {
		boolean rc = false;
		if (ui.hasNext()) {
			String candidate = ui.next();
			rc = (  quoted() && !Language.isQuoted( candidate ))
			  || (isPlural() && Plural.isSingular(  candidate ));
			if (ui.hasPrevious()) ui.previous();
		}
		return rc;
	}
	
	public String toXml( Indent indent ) {
		indent.incr();
		String s = prefix().toString( Strings.OUTERSP )
				+ (name.equals( "" ) ? "" :
					("<"+ name
							+ (isPhrased() ? " phrased='true'":"")
							+ (isNumeric() ? " numeric='true'":"")
							+ "/>"
				  ) )
				+ postfix().toString();
		indent.decr();
		return s;
	}
	public String toPattern() {
		return prefix().toString( Strings.UNDERSC )
				+(name.equals( "" ) ? (postSz == 0?"":"_") : "-")
				+(postfix().toString( Strings.UNDERSC ));
	}
	public String toString() {
		String prefix = prefix().toString();
		String postfix = postfix().toString();
		return prefix + (prefix.equals( "" ) ? "" : name.equals( "" ) ? "" : " ")
				+(name.equals( "" ) ? "" :
					(isNumeric()? Pattern.numericPrefix : "")
					+ (isPhrased()? Pattern.phrasePrefix : "")
					+ (isList()? Reply.andConjunction().toUpperCase( Locale.getDefault())
							+"-"+ Pattern.list.toUpperCase( Locale.getDefault()) +"-" : "")
					+ name.toUpperCase( Locale.getDefault()) ) 
				+ (postfix.equals( "" ) ? "" : " ") + postfix();
	}
	public String toText() {
		return prefix().toString()
			+ (prefix().toString()==null||prefix().toString().equals("") ? "":" ")
			+ (name.equals( "" ) ? "" :
				( name.toUpperCase( Locale.getDefault() ) +" "))
			+ postfix().toString();
	}
	public String toLine() { return prefix().toString() +" "+ name +" "+ postfix().toString(); }
	static public Patte peek( ListIterator<Patte> li ) {
		Patte s = new Patte();
		if (li.hasNext()) {
			s = li.next();
			li.previous();
		}
		return s;
	}
	
	// -- test code
	public static void main( String argv[]) {
		Audit.allOn();
		audit.tracing = true;
}	}
