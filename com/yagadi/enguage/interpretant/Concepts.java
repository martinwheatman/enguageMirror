package com.yagadi.enguage.interpretant;

import java.io.File;
import java.util.TreeSet;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Concepts {
	static private Audit audit = new Audit( "Concepts" );

    static private      TreeSet<String> names = new TreeSet<String>();
    static public final TreeSet<String> names() { return names; }
	static public                  void names( String location ) {
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

	public static Strings matches( Strings utterance ) {
		audit.in( "matches", utterance.toString() );
		// matches: utt=[martin is a wally], candiates=[ "is_a+has_a" ] => add( is_a+has_a )
		// TODO: also match to_the_phrase-reply_with
		Strings matches = new Strings();
		for (String candidate : names() ) { // e.g. "is_a+has_a" OR "to_the_phrase-reply_with"
			Strings candid = new Strings( candidate, '+' );
			// matching: "to the phrase my name is martin reply hello martin" with "to_the_phrase-reply"
			for (String c : candid) { // e.g. c="is_a"
				
				Strings d = new Strings( c, '_' );
				if (utterance.contains( d ))
					matches.add( candidate );
		}	}
		return audit.out( matches );
	}

	// backwards compatibility... STATICally load a repertoire file
	static public void load( String name ) {
		audit.in( "load", "name="+ name );

		// as with autoloading, make sure it is singular..
		//if (Plural.isPlural( name ))
		//	name = Plural.singular( name );

		// add in name as to what is loaded.
		if (!loaded.contains( name )) {
			// loading repertoires won't use undo - disable
			Allopoiesis.undoEnabledIs( false );
			if ( Concept.load( name )) {
				loaded.add( name );
				audit.debug( "LOADED>>>>>>>>>>>>:"+ name );
			} else
				audit.debug( "LOAD skipping already loaded:"+ name );
			Allopoiesis.undoEnabledIs( true );
		}
		audit.out();
	}

	static private void unload( String name ) {
		audit.in( "unload", "name="+ name );
		if (loaded.contains( name )) {
			loaded.remove( name );
			Repertoire.signs.remove( name );
			audit.debug( "UNLOADED<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<:"+ name );
		}
		audit.out();
	}

	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	static public void load( Tag concepts ) {
		audit.in( "load", "" );
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
					else if (op.equals( "unload" ))
						unload( id ); // using itself!!
					else if (!op.equals( "ignore" ))
						audit.ERROR( "unknown op "+ op +" on reading concept "+ name );

					if (!Audit.startupDebug) Audit.resume();
			}	}
			Repertoire.inductingIs( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
		audit.out();
}	}
