package org.enguage.vehicle;

import java.util.ListIterator;
import java.util.Locale;

import org.enguage.sign.repertoire.Repertoire;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

import org.enguage.vehicle.Language;
import org.enguage.vehicle.Plural;


public class Language {  // English-ism!
	
	//static private Audit audit = new Audit( "Language" );

	static public final Strings headers = new Strings( "( { [" );
	static public final Strings tailers = new Strings( ") } ]" );
	
	static public boolean isQuoted(String a) { // universal?
		int len;
		return (null != a) &&
			   ((len = a.length())>1) &&
			   (   ((a.charAt( 0 ) ==  '"') && (a.charAt( len-1 ) ==  '"'))
			    || ((a.charAt( 0 ) == '\'') && (a.charAt( len-1 ) == '\'')) );
	}
	static public boolean isQuote(String a) { // universal?
		return (null!=a) && (a.equals('\'') || a.equals('"'));
	}
	static public String asString( Strings ans ) {
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
	// replace [ x, "'", "y" ] with "x'y" -- or /dont/ or /martins/ if vocalised
	static public Strings apostropheContraction( Strings a, String letter ) {
		if (null != a) for (int i=0, sz=a.size(); i<sz-2; i++)
			if ( a.get( i+1 ).equals( "'" ) && a.get( i+2 ).equalsIgnoreCase(letter)) {
				a.set( i, a.get( i ) +"'"+ letter);
				a.remove( i+1 ); // remove apostrophe
				a.remove( i+1 ); // remove lettter
			}
		return a;
	}
	/*static public String apostropheRemoval( String a ) {
		String b = "";
		if (null != a) for (int i=0; i<a.length(); i++)
			if ( a.charAt( i ) != '\'' )
				b += a.charAt( i );
		return b;
	} // */

	static private boolean possessive = false;
	static public  void    possessive( Boolean b ) { possessive = b; }
	static public  boolean possessive() { return possessive; }
	
	static public Strings expandPossessives( Strings in) {
		int len = in.size(); // so, don't expand last 
		Strings out = new Strings();
		for (String s : in )
			if (s.endsWith( "'s" ) && --len > 0) {
				out.add( s.substring( 0, s.length()-2 ));
				out.add( "his" );
			} else
				out.add( s );
		return out;
	}
	static private boolean isVowel( char ch ) {
		return  ('a' == ch) || ('e' == ch) || ('i' == ch) || ('o' == ch) || ('u' == ch)  
		     || ('A' == ch) || ('E' == ch) || ('I' == ch) || ('O' == ch) || ('U' == ch); 
	}
	/*static public Strings indefiniteArticleVowelSwap( Strings ans ) {
		ListIterator<String> ai = ans.listIterator();
		while (ai.hasNext()) {
			String articleCandidate = ai.next();
			if (ai.hasNext() &&
				(   articleCandidate.equalsIgnoreCase(  "a" )      // ... a  QUANTITY ...
				 || articleCandidate.equalsIgnoreCase( "an" ))) {  // ... an ENGINEER ...
				// look forward to next word...
				String nextWord = new String( ai.next());
				String tmp = new String( ai.previous()); // go back to article
				audit.audit( "art next: "+ tmp +" "+ nextWord );
				ai.set( isVowel( nextWord.charAt( 0 )) ? "an" : "a" );
		}	}
		return ans;
	}*/

	static public Strings indefiniteArticleVowelSwap( Strings ans ) {
		for (int i=0, sz=ans.size(); i<sz-1; ++i)
			if (   ans.get( i ).equalsIgnoreCase(  "a" )
			    || ans.get( i ).equalsIgnoreCase( "an" ))
				ans.set( i, isVowel( ans.get( 1+i ).charAt( 0 )) ? "an" : "a" );
		return ans;
	}
/*	static public Strings indefiniteArticleFlatten( Strings a ) {
		for (int i=0; i<a.size(); i++)
			if (a.get( i ).equalsIgnoreCase( "an" ))
				a.set( i, "a" );
		return a;	
	}
	static public boolean xwordsEqualIgnoreCase( String a, String b ) {
		if ((a.equalsIgnoreCase( "an" ) || a.equalsIgnoreCase( "a" )) &&
		    (b.equalsIgnoreCase( "an" ) || b.equalsIgnoreCase( "a" ))    ) return true;
		return a.equalsIgnoreCase( b );
	} // */
	// ...and finally, terminators... moved to Shell
	public static String spell( String a ) { return Language.spell( a, false ); }
	public static String spell( String a, boolean slowly ) {
		String b = "";
		for (int i=0; i<a.length(); i++)
			b += ( " "+ ( slowly && i>0 ? ", ":"" )+ a.charAt( i ));
		return b;
	}
	static public String nthEnding( int n ){
		return n==1 || n==21 || n==31 ? "st" :
			n == 2 || n == 22 ? "nd" : n==3 || n==23 ? "rd" : "th";
	}
}