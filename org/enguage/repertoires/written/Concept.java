package org.enguage.repertoires.written;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;

/** Concepts is: the list of concept names; and,
 *               a name-to-concept 'match' function.
 */
public class Concept {
	private Concept() {}
	
	public  static final String     NAME = "concepts";
	
	private static final String      DOT = ".";
	private static final String      TXT = "txt";
	private static final String      RPT = "rpt";
	private static final String      DEL = "del";
	
	public  static final String TEXT_EXT = DOT + TXT;
	public  static final String REPT_EXT = DOT + RPT;
	public  static final String DELT_EXT = DOT + DEL;
	
	private static TreeSet<String> names = new TreeSet<>();
	public  static  void  remove( String name ) {names.remove( name );}
	public  static  void     add( String name ) {names.add(    name );}
	public  static  void  addAll( Strings nms ) {names.addAll(  nms );}
	
	private static boolean isFlatpak = false;
	public  static void    isFlatpak( boolean b ) {isFlatpak = b;}

	private static final String rwRpts() {return Fs.root() +Repertoires.LOC+ File.separator;}
	public  static final String roRpts( String prefix ) {
		return prefix+ Enguage.RO_SPACE +Repertoires.LOC+ File.separator;
	}
	public  static  String writtenName( String name ) {
		return roRpts( isFlatpak ? "/apps/":"" )+ name +TEXT_EXT;
	}
	public  static  String writtenRepName( String name ) {
		return roRpts( isFlatpak ? "/apps/":"" )+ name +REPT_EXT;
	}
	public  static  String spokenName(    String s ) {return rwRpts()+ s +TEXT_EXT;}
	public  static  String spokenRepName( String s ) {return rwRpts()+ s +REPT_EXT;}
	public  static  String deleteName(    String s ) {return rwRpts()+ s +DELT_EXT;}
	
	public  static Strings tree( String base, String location ) {
		Strings  names = new Strings();
		String[] files = new File( base + location ).list();
		for (String file : files)
			if (file.endsWith( TEXT_EXT ) || file.endsWith( REPT_EXT ))
				names.add( (!location.equals(".") ? location+"/" : "")+ file );
			else if (new File( base + location +File.separator+ file ).isDirectory())
				names.addAll( tree( base, file ));
		return names;
	}
	public static void addNames( String[] names ) {
		if (names != null) for ( String name : names ) { // e.g. name="rpt/hello.txt"
			String[] components = name.split( "\\." );
			if (components.length > 1 &&
					(components[ 1 ].equals( TXT )||
					 components[ 1 ].equals( RPT )  ))
				add( components[ 0 ]);
	}	}
	private static boolean matchAnyBoilerplate( Strings utt, Strings bplt, char sep ) {
		Iterator<String> ui  = utt.iterator();
		Iterator<String> bi = bplt.iterator();
		boolean first = true;
		while (ui.hasNext() && bi.hasNext()) { // read through the utterance & pattern
			Iterator<String> pwi = new Strings( bi.next(), sep ).iterator();
			if (pwi.hasNext()) {
				
				String pw = pwi.next();
				String ut = ui.next();
				
				// find the first matching token
				if (!first)
					while (!ut.equals(pw) && ui.hasNext())
						ut = ui.next();
				
				//now go thru' matching tokens
				while (ut.equals(pw) && ui.hasNext() && pwi.hasNext()) {
					ut = ui.next();
					pw = pwi.next();
				}
				
				// return if we have a mismatch
				if (!pw.equals(ut)) return false;
				
				// hyphen represents at least 1 utterance string, just read over it
				if (bi.hasNext())
					if (ui.hasNext())
						ui.next();
					else
						return false; // hyphen but no text...
				else
					return !ui.hasNext();
				first = false;
		}	}
		return true;
	}
	private static boolean matchSign( Strings utt, String sign ) {
		Strings allBplate = new Strings( sign, '-' ); // ["to_the_phrase", "reply", ""]
		switch (allBplate.size()) {
		case 0: return true;
		case 1: return utt.contains( new Strings( allBplate.iterator().next(), '_' ));
		default:return matchAnyBoilerplate( utt, allBplate, '_' );
	}	}
	private static boolean matchConceptNames( Strings utterance, String cfname ) {
		// matching: "to my name is martin reply hello martin" with "to-reply-"
		for (String sign : new Strings( cfname, '+' )) // e.g. c="to_the_phrase-reply-"
			if (matchSign(utterance, sign))
				return true;
		return false;
	}
	private static boolean matchConcepts( Strings utterance, String concept ) {
		// Strip dir name from concept filename
		String[] tmp = concept.split( "/" ); // OR closeAirSupport/egress
		return matchConceptNames( utterance, tmp[ tmp.length-1 ]);
	}
	public static Strings match( Strings utterance ) {
		/* match([martin is a wally]):
		 *     names=[ "is_a+has_a" ] 
		 * } => ( is_a+has_a )
		 */
		Strings matches = new Strings();
		for (String concept : names ) // e.g."is_a+has_a" OR "to_the_phrase-reply_with"
			if (matchConcepts( utterance, concept ))
				matches.add( concept );
		return matches;
}	}
