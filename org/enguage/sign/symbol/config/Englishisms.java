package org.enguage.sign.symbol.config;

import java.util.Locale;

import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;


public class Englishisms {  // English-ism!
	
	//private static Audit audit = new Audit( "Language" );

	public static final Strings      headers = new Strings( "( { [" );
	public static final Strings      tailers = new Strings( ") } ]" );
	public static final String APOSTROPHE    = "'";
	public static final char   APOSTROPHE_CH = APOSTROPHE.charAt( 0 );
	
	private static String Apostrophed = APOSTROPHE + "s";
	public  static String Apostrophed() { return Apostrophed; }
	public  static void   Apostrophed( String s ) { Apostrophed = s; }
	
	public  static boolean isQuoted(String a) { // universal?
		int len;
		return (null != a) &&
			   ((len = a.length())>1) &&
			   (   ((a.charAt( 0 )==Strings.DOUBLE_QUOTE) && (a.charAt( len-1 )==Strings.DOUBLE_QUOTE))
			    || ((a.charAt( 0 )==Strings.SINGLE_QUOTE) && (a.charAt( len-1 )==Strings.SINGLE_QUOTE)) );
	}
	public  static boolean isQuote(String a) { // universal?
		return (null!=a) && (a.equals( ""+Strings.SINGLE_QUOTE )
				          || a.equals( ""+Strings.DOUBLE_QUOTE ));
	}
	private static String asString( Strings ans ) {
		StringBuilder str = new StringBuilder();
		apostropheContraction( ans, "tag" );
		apostropheContraction( ans, "s" );
		if (ans != null)
			for (int i=0; i<ans.size(); i++) {
				if (i > 0 &&
					          !headers.contains( ans.get( i-1)) &&
					!Shell.terminators.contains( ans.get(  i )) &&
					          !tailers.contains( ans.get(  i )))
					str.append( " " );
				str.append( ans.get( i ));
			}
		return str.toString();
	}
	public  static Strings asStrings( Strings ans ) {
		return new Strings( asString( ans ));
	}
	public  static String capitalise( String a ) {
		return a.length()>0 ? a.toUpperCase(Locale.getDefault()).charAt(0) + a.substring( 1 ) : "";
	}
	public  static Strings sentenceCapitalisation( Strings a ) {
		if (a != null && !a.isEmpty())
			a.set( 0, capitalise( a.get( 0 ))); // ... if so, start with capital
		return a;
	}
	// replace [ x, ', "y" ] with "x'y" -- or /dont/ or /martins/ if vocalised
	public  static void apostropheContraction( Strings a, String letter ) {
		if (null != a)
			for (int i=0, sz=a.size(); i<sz-2; i++)
				if ( a.get( i+1 ).equals( APOSTROPHE ) && a.get( i+2 ).equalsIgnoreCase(letter)) {
					a.set( i, a.get( i ) +APOSTROPHE+ letter);
					a.remove( i+1 ); // remove apostrophe
					a.remove( i+1 ); // remove lettter
				}
	}

	private static boolean isVowel( char ch ) {
		return  ('a'==ch) || ('e'==ch) || ('i'==ch) || ('o'==ch) || ('u'==ch)  
		     || ('A'==ch) || ('E'==ch) || ('I'==ch) || ('O'==ch) || ('U'==ch); 
	}
	public  static Strings indefiniteArticleVowelSwap( Strings ans ) {
		for (int i=0, sz=ans.size(); i<sz-1; ++i)
			if (   ans.get( i ).equalsIgnoreCase(  "a" )
			    || ans.get( i ).equalsIgnoreCase( "an" ))
				ans.set( i, isVowel( ans.get( 1+i ).charAt( 0 )) ? "an" : "a" );
		return ans;
	}
	public static String spell( String a ) { return Englishisms.spell( a, false ); }
	public static String spell( String a, boolean slowly ) {
		String b = "";
		for (int i=0; i<a.length(); i++)
			b += ( " "+ ( slowly && i>0 ? ", ":"" )+ a.charAt( i ));
		return b;
	}
	public  static String nthEnding( int n ) {
		int th = n%100;
		if (th==11 || th==12 || th==13) return "th";
		n = n%10;
		if (n==1) return "st";
		if (n==2) return "nd";
		if (n==3) return "rd";
		return "th";
}	}
