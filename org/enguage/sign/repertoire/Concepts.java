package org.enguage.sign.repertoire;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import org.enguage.sign.intention.Redo;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.Tag;

public class Concepts {
	/* Concepts is a list of names, TODO: pre-processed into a list of Concept(pattern+name)
	 * and the list of concepts currently loaded
	 * and a name-to-concept function.
	 */
	static private Audit audit = new Audit( "Concepts" );

	static private       TreeSet<String> names = new TreeSet<String>();
	static private final TreeSet<String> names() { return names; }
	static public                   void names( String location ) {
		audit.in( "names", location );
		for ( String fname : new File( location ).list() ) {
			String[] components = fname.split( "\\." );
			if (components.length > 1 && components[ 1 ].equals("txt")) {
				audit.debug( "adding concept: "+ components[ 0 ]);
				names.add( components[ 0 ]);
		}	}
		audit.out();
	}

	static private TreeSet<String> loaded = new TreeSet<String>();
	static public  TreeSet<String> loaded() { return loaded; }
	
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
		for (String candidate : names() ) { // e.g. "is_a+has_a" OR "to_the_phrase-reply_with"
			Strings candid = new Strings( candidate, '+' );
			// matching: "to my name is martin reply hello martin" with "to-reply-"
			for (String c : candid) { // e.g. c="to_the_phrase-reply-"
				if (matchesHyphenatedPattern(utterance, c))
					matches.add( candidate );
		}	}
		//return audit.out( matches );
		return matches;
	}

	// backwards compatibility... STATICally load a repertoire file
	static public void load( String name ) {
		audit.in( "load", "name="+ name );
		// add in name as to what is loaded.
		if (!loaded.contains( name )) {
			// loading repertoires won't use undo - disable
			Redo.undoEnabledIs( false );
			if ( Concept.load( name )) {
				loaded.add( name );
				audit.debug( "LOADED>>>>>>>>>>>>:"+ name );
			} else
				audit.debug( "LOAD skipping already loaded:"+ name );
			Redo.undoEnabledIs( true );
		}
		audit.out();
	}

	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	static public void load( Tag concepts ) {
		//audit.in( "load", "" );
		if (null != concepts) {
			Repertoire.inductingIs( true );
			audit.log( "Found: "+ concepts.content().size() +" concept(s)" );
			for (int j=0; j<concepts.content().size(); j++) {
				String name = concepts.content().get( j ).name();
				if (name.equals( "concept" )) {
					String op = concepts.content().get( j ).attribute( "op" ),
							 id = concepts.content().get( j ).attribute( "id" );

					// get default also from config file: ensure def is at least set to last rep name
					audit.log( "id="+ id +", op="+ op );
					if (op.equals( "prime" )) {
						audit.log( "Prime repertoire is '"+ id +"'" );
						Repertoire.prime( id );
					}

					if (!Audit.startupDebug) Audit.suspend();

					if (op.equals( "load" ) || op.equals( "prime" ))
						load( id ); // using itself!!
					else if (!op.equals( "ignore" ))
						audit.ERROR( "unknown op "+ op +" on reading concept "+ name );

					if (!Audit.startupDebug) Audit.resume();
			}	}
			Repertoire.inductingIs( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
		//audit.out();
	}
	private static void test( String s, boolean matchesToReply ) {
		Strings sa = matches( new Strings( s ));
		audit.log( "matches: " + sa.toString( Strings.DQCSV ) + (matchesToReply ? " should":" shouldn't") + " match to-reply-");
	}
	public static void main( String args[]) {
		names("./src/assets/concepts" );
		test( "i need a coffee",false );
		test( "to the phrase my name is variable name reply hello variable name", true );
		test( "to reply hello variable name", false );
		test( "to hello reply", false );
		test( "hello to fred reply way", false );
}	}
