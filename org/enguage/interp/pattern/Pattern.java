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

public class Pattern {
	private static Audit audit = new Audit( "Patternette" );
	
	
	// -- constructors...
	public Pattern() {}
	public Pattern( Strings pre, String nm ) {
		this();
		prefix( pre ).name( nm );
	}
	public Pattern( Strings pre, String nm, Strings post ) {
		this( pre, nm );
		postfix( new Strings( post ));
	}
	//just a helper ctor for hardcoded Patternettes
	public Pattern( String pre ) { this( new Strings( pre ), "" ); }
	public Pattern( String pre, String nm ) { this( new Strings( pre ), nm );}
	public Pattern( String pre, String nm, String pst ) { this( new Strings( pre ), nm, new Strings( pst ) );}

	private int  preSz  = 0;
	private int  postSz = 0;
	public  int  nconsts() {return preSz + postSz;}
	
	private Strings     prefix = new Strings();
	public  Strings     prefix() { return prefix; }
	public  Pattern prefix( Strings s ) { prefix = s.toLowerCase(); preSz = prefix.size(); return this; }
	public  Pattern prefix( String str ) {
		prefix = new Strings();
		for (String s : new Strings( str ))
			prefix.append( s.toLowerCase() );
		preSz = prefix.size();
		return this;
	}
	public void prefixAppend( String word ) {prefix.append( word.toLowerCase() ); preSz++;}

	private Strings     postfix = new Strings();
	public  Strings     postfix() { return postfix; }
	public  Pattern postfix( Strings ss ) { postfix = ss.toLowerCase(); postSz = postfix.size(); return this; }
	public  Pattern postfix( String str ) {return postfix( new Strings( str ));}

	private boolean     named = false;
	private String      name = "";
	public  String      name() { return name; }
	public  Pattern name( String nm ) { if (null != nm) {name = nm; named = !nm.equals("");} return this; }
	public  boolean     named() { return named;}

	// -- mutually exclusive attributes --:
	private boolean     isNumeric = false;
	public  boolean     isNumeric() { return isNumeric; }
	public  Pattern numericIs( boolean nm ) { isNumeric = nm; return this; }
	public  Pattern numericIs() { isNumeric = true; return this; }

	private boolean     isExpr = false;
	public  boolean     isExpr() { return isExpr; }
	public  Pattern exprIs( boolean ex ) { isExpr = ex; return this; }
	public  Pattern exprIs() { isExpr = true; return this; }

	private boolean     isQuoted = false;
	public  boolean     quoted() { return isQuoted;}
	public  Pattern quotedIs( boolean b ) { isQuoted = b; return this; }
	public  Pattern quotedIs() { isQuoted = true; return this; }
	
	private boolean     isPlural = false;
	public  boolean     isPlural() { return isPlural; }
	public  Pattern pluralIs( boolean b ) { isPlural = b; return this; }
	public  Pattern pluralIs() { isPlural = true; return this; }
	
	private boolean     isPhrased = false;
	public  boolean     isPhrased() { return isPhrased; }
	public  Pattern phrasedIs() { isPhrased = true; return this; }
	
	private boolean     isGrouped = false;
	public  boolean     isGrouped() { return isGrouped; }
	public  Pattern groupedIs() { isGrouped = true; return this; }
	
	private boolean     isSign = false;
	public  boolean     isSign() { return isSign; }
	public  Pattern signIs() { isSign = true; return this; }
	
   // TODO: Apretrophed
	private String      isApostrophed = null;
	public  boolean     isApostrophed() { return isApostrophed != null; }
	public  Pattern apostrophedIs( String s ) { isApostrophed = s; return this; }
	
	private boolean     isList = false;
	public  boolean     isList() { return isList; }
	public  Pattern listIs() { isList = true; return this; }
	// --
	
	private String      conjunction = "";
	public  String      conjunction() { return conjunction; }
	public  Pattern conjunction( String c ) { conjunction = c; return this; }
	
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
					(isNumeric()? Patterns.numericPrefix : "")
					+ (isPhrased()? Patterns.phrasePrefix : "")
					+ (isList()? Reply.andConjunction().toUpperCase( Locale.getDefault())
							+"-"+ Patterns.list.toUpperCase( Locale.getDefault()) +"-" : "")
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
	static public Pattern peek( ListIterator<Pattern> li ) {
		Pattern s = new Pattern();
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
