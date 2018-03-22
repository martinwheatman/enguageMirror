package org.enguage.sign.repertoire;

import java.io.File;
/*import android.app.Activity;
 *
 *import java.io.File;
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.Enguage;
import org.enguage.object.Variable;
import org.enguage.object.space.Ospace;
import org.enguage.sign.intention.Intention;
import org.enguage.util.Audit;

public class Concept {
	static public final String LOADING = "CONCEPT";
	static public boolean load( String name ) {
		Enguage e = Enguage.get();
		boolean wasLoaded = false,
		        wasSilenced = false,
		        wasAloud = e.isAloud();
		
		Variable.set( LOADING, name );
		
		// silence on inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend(); // <<<<<<<<< comment this out for debugging
			e.aloudIs( false );
		}
		
		Intention.concept( name );
		if (name.equals( Repertoire.DEFAULT_PRIME ))
			Repertoire.defaultConceptLoadedIs( true );
		
		// ...add content from file/asset...
		try {
			String fname = "concepts"+ File.separator + name + ".txt";
			//Activity a = (Activity) Enguage.context();
			InputStream is =
					//a == null ?
							new FileInputStream( Ospace.location() + fname );
					//      : a.getAssets().open( fname );
			e.interpret( is );
			is.close();
			
			wasLoaded = true;
			
		} catch (IOException e1) {}
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			e.aloudIs( wasAloud );
		}
		
		Variable.unset( LOADING );
		return wasLoaded;
}	}
