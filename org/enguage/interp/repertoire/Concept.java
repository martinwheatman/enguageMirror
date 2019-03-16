package org.enguage.interp.repertoire;

/* ANDROID --
import android.app.Activity;
// */
import org.enguage.Enguage;
import org.enguage.interp.intention.Intention;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Ospace;
import org.enguage.util.Audit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;



public class Concept {
	static public final String LOADING = "CONCEPT";
	static public boolean load( String name ) {return load( name, null, null );}
	static public boolean load( String name, String from, String to ) {
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
		
		// ...add content from file/asset...
		try {
			String fname = "concepts"+ File.separator + name + ".txt";
			// ANDROID --
			//Activity a = (Activity) Enguage.context(); //*/
			InputStream is =
					//a == null ?//*/
							new FileInputStream( Ospace.location() + fname )
					/*      : a.getAssets().open( fname ); //*/ ;
			Enguage.shell().interpret( is, from, to );
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
