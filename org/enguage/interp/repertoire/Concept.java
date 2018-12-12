package org.enguage.interp.repertoire;

/*import android.app.Activity;*/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.Enguage;
import org.enguage.interp.intention.Intention;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Ospace;
import org.enguage.util.Audit;

public class Concept {
	static public final String LOADING = "CONCEPT";
	static public boolean load( String name ) {
		boolean wasLoaded   = false,
		        wasSilenced = false,
		        wasAloud    = Enguage.shell().isAloud();
		
		Variable.set( LOADING, name );
		
		// silence inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend();
			Enguage.shell().aloudIs( false );
		}
		
		Intention.concept( name );
		if (name.equals( Repertoire.DEFAULT_PRIME ))
			Repertoire.defaultConceptLoadedIs( true );
		
		// ...add content from file/asset...
		try {
			String fname = "concepts"+ File.separator + name + ".txt";
			/*Activity a = (Activity) Enguage.e.context();*/
			InputStream is =
					/*a == null ?*/
							new FileInputStream( Ospace.location() + fname )
					/*      : a.getAssets().open( fname )*/;
			Enguage.shell().interpret( is );
			is.close();
			
			wasLoaded = true;
			
		} catch (IOException e1) {}
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			Enguage.shell().aloudIs( wasAloud );
		}
		
		Variable.unset( LOADING );
		return wasLoaded;
}	}
