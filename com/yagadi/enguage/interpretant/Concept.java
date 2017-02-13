package com.yagadi.enguage.interpretant;

import java.io.FileInputStream;
import java.io.IOException;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.object.Ospace;
import com.yagadi.enguage.util.Audit;

public class Concept {

	static public boolean load( String name ) {
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
			FileInputStream fis = new FileInputStream( Ospace.location() + name +".txt" );
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
}	}
