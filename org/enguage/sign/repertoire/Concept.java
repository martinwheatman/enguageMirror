package org.enguage.sign.repertoire;

//import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.Enguage;
import org.enguage.object.Ospace;
import org.enguage.sign.intention.Intention;
import org.enguage.util.Audit;
import org.enguage.util.Fs;

public class Concept {
	//static private Audit audit = new Audit( "Concept" );
	static public boolean load( String name ) {
		//audit.in( "load", name );
		Enguage e = Enguage.get();
		boolean wasLoaded = false,
		        wasSilenced = false,
		        wasAloud = e.isAloud();
		
		// silence on inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend(); // <<<<<<<<< comment this out for debugging
			e.aloudIs( false );
		}
		
		Intention.concept( name );
		if (name.equals( Repertoire.DEFAULT_PRIME ))
			Repertoire.defaultConceptLoadedIs( true );
		
		// ...add content from file...
		//String fname = Ospace.location() + name +".txt";
		//audit.log( "fname is "+ fname );
		try {
			//Activity a = Enguage.context();
			InputStream is =
					//a == null ?
							new FileInputStream( Ospace.location() + "/concepts/" +name +".txt" );
					//      : a.getAssets().open( "concepts"+ File.separator + name + ".txt" );
			e.interpret( is );
			is.close();
			wasLoaded = true; 
		} catch (IOException e1) {}
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			e.aloudIs( wasAloud );
		}
		//return audit.out( wasLoaded );
		return wasLoaded;
}	}
