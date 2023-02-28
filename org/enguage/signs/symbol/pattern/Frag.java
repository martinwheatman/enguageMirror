package org.enguage.signs.symbol.pattern;

import java.util.ListIterator;
import java.util.Locale;

import org.enguage.signs.symbol.config.Englishisms;
import org.enguage.signs.symbol.config.Plural;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.util.Audit;
import org.enguage.util.Indentation;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;

public class Frag {
	
	// -- constructors...
	public Frag() {}
	public Frag( Strings pre, String nm ) {
		this();
		prefix( pre ).name( nm );
	}
	public Frag( Strings pre, String nm, Strings post ) {
		this( pre, nm );
		postfix( new Strings( post ));
	}
	//just a helper ctor for hardcoded Patternettes
	public Frag( String pre ) {this( new Strings( pre ), "" );}
	public Frag( String pre, String nm ) {this( new Strings( pre ), nm );}
	public Frag( String pre, String nm, String pst ) {this( new Strings( pre ), nm, new Strings( pst ) );}

	public Frag( Frag orig ) {// copy constructor
		prefix = new Strings( orig.prefix());
		postfix = new Strings( orig.postfix());
		name    = new String( orig.name());
		isNumeric = orig.isNumeric();
		isExpr    = orig.isExpr();
		isSign    = orig.isSign();
		isList    = orig.isList();
		isQuoted  = orig.isQuoted();
		isPlural  = orig.isPlural();
		isPhrased  = orig.isPhrased();
		isGrouped   = orig.isGrouped();
		conjunction  = orig.conjunction();
		isApostrophed = orig.isApostrophed;
	}
	
	/*
	 *  Members:
	 */
	public  int nconsts() {return preSz + postSz;}
	
	private int     preSz  = 0;
	private Strings prefix = new Strings();
	public  Strings prefix() {return prefix;}
	public  Frag    prefix( Strings s ) {prefix = s.toLowerCase(); preSz = prefix.size(); return this;}
	public  Frag    prefix( String str ) {
		prefix = new Strings();
		for (String s : new Strings( str ))
			prefix.append( s.toLowerCase() );
		preSz = prefix.size();
		return this;
	}
	public void prefixAppend( String word ) {prefix.append( word.toLowerCase() ); preSz++;}

	private int     postSz = 0;
	private Strings postfix = new Strings();
	public  Strings postfix() {return postfix;}
	public  Frag    postfix( Strings ss ) {postfix = ss.toLowerCase(); postSz = postfix.size(); return this;}
	public  Frag    postfix( String str ) {return postfix( new Strings( str ));}

	private boolean named = false;
	private String  name = "";
	public  String  name() {return name;}
	public  Frag    name( String nm ) {if (null != nm) {name = nm; named = !nm.equals("");} return this;}
	public  boolean named() {return named;}

	// -- mutually exclusive attributes --:
	private boolean isNumeric = false;
	public  boolean isNumeric() {return isNumeric;}
	public  Frag    numericIs( boolean nm ) {isNumeric = nm; return this;}
	public  Frag    numericIs() {isNumeric = true; return this;}

	private boolean isExpr = false;
	public  boolean isExpr() {return isExpr;}
	public  Frag    exprIs( boolean ex ) {isExpr = ex; return this;}
	public  Frag    exprIs() {isExpr = true; return this;}

	private boolean isQuoted = false;
	public  boolean isQuoted() {return isQuoted;}
	public  Frag    quotedIs( boolean b ) {isQuoted = b; return this;}
	public  Frag    quotedIs() {isQuoted = true; return this;}
	
	private boolean isPlural = false;
	public  boolean isPlural() {return isPlural;}
	public  Frag    pluralIs( boolean b ) {isPlural = b; return this;}
	public  Frag    pluralIs() {isPlural = true; return this;}
	
	private boolean isPhrased = false;
	public  boolean isPhrased() {return isPhrased;}
	public  Frag    phrasedIs() {isPhrased = true; return this;}
	
	private boolean isGrouped = false;
	public  boolean isGrouped() {return isGrouped;}
	public  Frag    groupedIs() {isGrouped = true; return this;}
	
	private boolean isSign = false;
	public  boolean isSign() {return isSign;}
	public  Frag    signIs() {isSign = true; return this;}
	
	private String  isApostrophed = null;
	public  boolean isApostrophed() {return isApostrophed != null;}
	public  Frag    apostrophedIs( String s ) {isApostrophed = s; return this;}
	
	private boolean isList = false;
	public  boolean isList() {return isList;}
	public  Frag    listIs() {isList = true; return this;}
	// --
	
	private String conjunction = "";
	public  String conjunction() {return conjunction;}
	public  Frag   conjunction( String c ) {conjunction = c; return this;}
	
	public Attribute matchedAttr( String val ) {
		return new Attribute(
				name,
				Attribute.getValue( // prevents X="x='val'"
					name.equals("unit") ? Plural.singular( val ) : val
				));
	}
	
	public boolean isEmpty() {return name.equals("") && prefix().size() == 0;}

	public boolean invalid( ListIterator<String> ui ) {
		boolean rc = false;
		if (ui.hasNext()) {
			String candidate = ui.next();
			rc = (  isQuoted() && !Englishisms.isQuoted( candidate ))
			  || (isPlural() && Plural.isSingular(  candidate ));
			if (ui.hasPrevious()) ui.previous();
		}
		return rc;
	}
	
	public String toXml( Indentation indent ) {
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
					(isNumeric()? Frags.numericPrefix : "")
					+ (isPhrased()? Frags.phrasePrefix : "")
					+ (isQuoted()? Frags.quotedPrefix : "")
					+ (isList()? Reply.andConjunction().toUpperCase( Locale.getDefault())
							+"-"+ Frags.list.toUpperCase( Locale.getDefault()) +"-" : "")
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
	public String toLine() {return prefix().toString() +" "+ name +" "+ postfix().toString();}
	public static Frag peek( ListIterator<Frag> li ) {
		Frag s = new Frag();
		if (li.hasNext()) {
			s = li.next();
			li.previous();
		}
		return s;
	}
	
	// -- test code
	public static void main( String[] argv ) {
		Audit.on();
}	}
