package com.yagadi;
/* ANDROID --
import android.app.Activity;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;
import org.enguage.util.sys.Fs;
// */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.interp.intention.Intention;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Ospace;
import org.enguage.util.Audit;

public class Enguage {
	
	static public final String LOADING = "concept";
	static private       Audit     audit = new Audit( "yagadi" );
	
	/* ANDROID --
	public static Tag fromAsset( String fname, Activity ctx ) {
	 	audit.in( "fromAsset", fname );
	 	Tag t = null;
	 	AssetManager am = ctx.getAssets();
	 	try {
	 		InputStream is = am.open( fname );
	 		t = new Tag( Fs.stringFromStream( is ));
	 		is.close();
	 	} catch (IOException e) {
	 		audit.ERROR( "no tag found in asset "+ fname );
	 	}
	 	audit.out();
	 	return t;
	}
	// */
	static public boolean loadConcept( String name, String from, String to ) {
		boolean wasLoaded   = false,
		        wasSilenced = false,
		        wasAloud    = org.enguage.Enguage.shell().isAloud();
		
		Variable.set( LOADING, name );
		
		// silence inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend();
			org.enguage.Enguage.shell().aloudIs( false );
		}
		
		Intention.concept( name );
		InputStream  is = null;
		
		try { // ...add concept from user space...
			is = new FileInputStream( org.enguage.interp.repertoire.Concept.name( name ));
			org.enguage.Enguage.shell().interpret( is, from, to );
			wasLoaded = true;
		} catch (IOException e1) {
			InputStream is2 = null;
			try { // ...or add concept from asset...
				String fname = org.enguage.interp.repertoire.Concept.name( name );
				// ANDROID --
				//Activity a = (Activity) Enguage.context(); //*/
				is2 =   //a == null ?//*/
								new FileInputStream( Ospace.location() + fname )
						/*      : a.getAssets().open( fname ); //*/ ;
				org.enguage.Enguage.shell().interpret( is2, from, to );
				wasLoaded = true;
			} catch (IOException e2) { audit.ERROR( "name: "+ name +", not found" );
			} finally {if (is2 != null) try{is2.close();} catch(IOException e2) {} }
		} finally { if (is != null) try{is.close();} catch(IOException e2) {} }
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			org.enguage.Enguage.shell().aloudIs( wasAloud );
		}
		
		Variable.unset( LOADING );
		return wasLoaded;
}	}
