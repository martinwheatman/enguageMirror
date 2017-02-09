package com.yagadi.enguage.interpretant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeSet;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.util.Audit;

public class Concepts {
	static private Audit audit = new Audit( "Concept" );

    static private      TreeSet<String> names = null;
    static public final TreeSet<String> names() { return names; }
	static public                  void names(String location ) {
		names = new TreeSet<String>();
		File dir = new File( location );
		for ( String fname : dir.list() ) {
			String[] components = fname.split( "\\." );
			if (components.length > 1 && components[ 1 ].equals("txt")) {
				//audit.LOG( "adding concept: "+ components[ 0 ]);
				names.add( components[ 0 ]);
	}	}	}

	static private TreeSet<String> loaded = new TreeSet<String>();
	static public  TreeSet<String> loaded() { return loaded; }

	static public boolean loadConcept( String name ) { // to Repertoires
		Enguage e = Enguage.get();
		boolean wasLoaded = false,
		        wasSilenced = false,
		        wasAloud = e.isAloud();
	
		// silence on inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend(); // <<<<<<<<< miss this out for debugging
			e.aloudIs( false );
		}
		
		Autopoiesis.concept( name );
		if (name.equals( Repertoire.DEFAULT_PRIME ))
			Repertoire.defaultConceptLoadedIs( true );
		
		// ...add content from file...
		try {
			FileInputStream fis = new FileInputStream( Repertoire.location() + name +".txt" );
			e.interpret( fis );
			fis.close();
			wasLoaded = true; 
		} catch (IOException e1) {}
		
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			e.aloudIs( wasAloud );
		}
		return wasLoaded;
	}
	// backwards compatibility... STATICally load a repertoire file
	static private void load( String name ) {
		audit.in( "load", "name="+ name );

		// as with autoloading, make sure it is singular..
		//if (Plural.isPlural( name ))
		//	name = Plural.singular( name );

		// add in name as to what is loaded.
		if (!loaded.contains( name )) {
			// loading repertoires won't use undo - disable
			Allopoiesis.undoEnabledIs( false );
			if ( loadConcept( name )) {
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
			Repertoire.initialisingIs( true );
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
			Repertoire.initialisingIs( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
		audit.out();
	}
	
}
