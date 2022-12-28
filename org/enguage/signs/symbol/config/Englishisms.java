package org.enguage.signs.symbol.config;

import java.util.ListIterator;
import java.util.Locale;

import org.enguage.repertoire.Repertoire;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;


public class Englishisms {  // English-ism!
	
	//static private Audit audit = new Audit( "Language" );

	static public final Strings      headers = new Strings( "( { [" );
	static public final Strings      tailers = new Strings( ") } ]" );
	final static public String APOSTROPHE    = "'";
	final static public char   APOSTROPHE_CH = APOSTROPHE.charAt( 0 );
	
	static private String Apostrophed   = "s";
	static public  String Apostrophed() { return Apostrophed; }
	static public  void   Apostrophed( String s ) { Apostrophed = s; }
	
	static public boolean isQuoted(String a) { // universal?
		int len;
		return (null != a) &&
			   ((len = a.length())>1) &&
			   (   ((a.charAt( 0 ) == Strings.DOUBLE_QUOTE) && (a.charAt( len-1 ) == Strings.DOUBLE_QUOTE))
			    || ((a.charAt( 0 ) == Strings.SINGLE_QUOTE) && (a.charAt( len-1 ) == Strings.SINGLE_QUOTE)) );
	}
	static public boolean isQuote(String a) { // universal?
		return (null!=a) && (a.equals( ""+Strings.SINGLE_QUOTE )
				          || a.equals( ""+Strings.DOUBLE_QUOTE ));
	}
	static private String asString( Strings ans ) {
		String str = "";
		ans = apostropheContraction( ans, "tag" );
		ans = apostropheContraction( ans, "s" );
		for (int i=0; i<ans.size(); i++) {
			if (i > 0 &&
				          !headers.contains( ans.get( i-1)) &&
				!Shell.terminators.contains( ans.get(  i )) &&
				          !tailers.contains( ans.get(  i )))
				str += " ";
			str += ans.get( i );
		}
		return str;
	}
	static public Strings asStrings( Strings ans ) {
		return new Strings( asString( ans ));
	}
	static public String capitalise( String a ) {
		return a.length()>0 ? a.toUpperCase(Locale.getDefault()).charAt(0) + a.substring( 1 ) : "";
	}
	static public Strings sentenceCapitalisation( Strings a ) {
		if (a != null && a.size() > 0)
			a.set( 0, capitalise( a.get( 0 ))); // ... if so, start with capital
		return a;
	}
	static public Strings pronunciation( Strings a ) {
		if (a != null) {
			for(ListIterator<String> ai = a.listIterator(); ai.hasNext();) {
				String s = ai.next();
				if (s.equals( Repertoire.NAME ))
					ai.set( Repertoire.PRONUNCIATION );
				else if (s.equals( Plural.plural( Repertoire.NAME )))
					ai.set( Repertoire.PLURALISATION );
		}	}
		return a;
	}
	// replace [ x, ', "y" ] with "x'y" -- or /dont/ or /martins/ if vocalised
	static public Strings apostropheContraction( Strings a, String letter ) {
		if (null != a) for (int i=0, sz=a.size(); i<sz-2; i++)
			if ( a.get( i+1 ).equals( APOSTROPHE ) && a.get( i+2 ).equalsIgnoreCase(letter)) {
				a.set( i, a.get( i ) +APOSTROPHE+ letter);
				a.remove( i+1 ); // remove apostrophe
				a.remove( i+1 ); // remove lettter
			}
		return a;
	}

	
	static private boolean isVowel( char ch ) {
		return  ('a' == ch) || ('e' == ch) || ('i' == ch) || ('o' == ch) || ('u' == ch)  
		     || ('A' == ch) || ('E' == ch) || ('I' == ch) || ('O' == ch) || ('U' == ch); 
	}
	static public Strings indefiniteArticleVowelSwap( Strings ans ) {
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
	static public String nthEnding( int n ){
		return n==1 || n==21 || n==31 ? "st" :
			n == 2 || n == 22 ? "nd" : n==3 || n==23 ? "rd" : "th";
}	}