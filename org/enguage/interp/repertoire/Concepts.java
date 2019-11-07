package org.enguage.interp.repertoire;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import org.enguage.interp.intention.Redo;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.tag.Tag;

/** Concepts is: the list of concept names;
 *               the list of loaded concepts; and,
 *               a name-to-concept function.
 */
public class Concepts {
	static public  final String     NAME = "concepts";
	static public  final String LOCATION = NAME + File.separator;
	static private       Audit     audit = new Audit( NAME );
	
	static private TreeSet<String> names = new TreeSet<String>();
	static public  void  remove( String name ) { names.remove( name );}
	static public  void     add( String name ) { names.add(    name );}
	static public  void  addAll( Strings nms ) { names.addAll(  nms );}
	static public  void addFrom( String location ) { // just "concepts"
		String[] names = new File( location ).list();
		if (names != null) for ( String name : names ) { // e.g. name="hello.txt"
			String[] components = name.split( "\\." );
			if (components.length > 1 && components[ 1 ].equals("txt"))
				add( components[ 0 ]);
			else if (components.length == 1)
				addFrom( location+"/"+components[ 0 ]);
	}	}
	
	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	static private TreeSet<String> loaded = new TreeSet<String>();
	static public  TreeSet<String> loaded() { return loaded; }
	static public void load( String name ) {
		if (!loaded.contains( name )) {
			// loading won't use undo - disable
			Redo.undoEnabledIs( false );
			if (com.yagadi.Assets.loadConcept( name, null, null ))
				loaded.add( name );
			Redo.undoEnabledIs( true );
	}	}
	static public void load( Tag concepts ) {
		if (null != concepts) {
			Repertoire.transformation( true );
			Audit.log( "Found: "+ concepts.content().size() +" concept(s)" );
			for (int j=0; j<concepts.content().size(); j++) {
				String name = concepts.content().get( j ).name();
				if (name.equals( "concept" )) {
					String op = concepts.content().get( j ).attribute( "op" ),
							 id = concepts.content().get( j ).attribute( "id" );

					if (!Audit.startupDebug) Audit.suspend();

					if (!op.equals( "ignore" ))
						load( id ); // using itself!!
					
					if (!Audit.startupDebug) Audit.resume();
			}	}
			Repertoire.transformation( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
	}
	
	static private boolean matchesHyphenatedPattern( Strings s, String pattern ) {
		// where pattern may be "to_the_phrase-reply_with"
		Strings pcomp = new Strings( pattern, '-' );
		Iterator<String> ui  = s.iterator();
		Iterator<String> phi = pcomp.iterator();
		if (pcomp.size()>1) { // hyphens found...
			boolean first = true;
			while (ui.hasNext() && phi.hasNext()) { // read through the utterance & pattern
				Iterator<String> pwi = new Strings( phi.next(), '_' ).iterator();
				if (pwi.hasNext()) {
					String pw = pwi.next(),
					       ut = ui.next();
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
					if (phi.hasNext())
						if (ui.hasNext())
							ui.next();
						else
							return false; // hyphen but no text...
					else
						return !ui.hasNext();
					first = false;
			}	}
		} else if(pcomp.size() == 1) // no hyphens found - simple match
			return s.contains( new Strings( phi.next(), '_' ));
		
		return true;
	}
	public static Strings matches( Strings utterance ) {
		//audit.in( "matches", utterance.toString() );
		// matches: utt=[martin is a wally], candiates=[ "is_a+has_a" ] => add( is_a+has_a )
		Strings matches = new Strings();
		for (String candidate : names ) { // e.g. "is_a+has_a" OR "to_the_phrase-reply_with"
			Strings candid = new Strings( candidate, '+' );
			// matching: "to my name is martin reply hello martin" with "to-reply-"
			for (String c : candid) { // e.g. c="to_the_phrase-reply-"
				if (matchesHyphenatedPattern(utterance, c))
					matches.add( candidate );
		}	}
		//return audit.out( matches );
		return matches;
	}

	/*
	 *  --- test code
	 */
	private static void test( String s, boolean matchesToReply ) {
		Strings sa = matches( new Strings( s ));
		Audit.log( "matches: " + sa.toString( Strings.DQCSV ) + (matchesToReply ? " should":" shouldn't") + " match to-reply-");
	}
	public static void main( String args[]) {
		addFrom( "./src/assets/concepts"  );
		test( "i need a coffee",false );
		test( "to the phrase my name is variable name reply hello variable name", true );
		test( "to reply hello variable name", false );
		test( "to hello reply", false );
		test( "hello to fred reply way", false );
}	}
