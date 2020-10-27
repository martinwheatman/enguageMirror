package com.yagadi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.interp.intention.Intention;
import org.enguage.interp.repertoire.Concepts;
import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;
import org.enguage.Enguage;

public class Assets {
	
	static public final String  LOADING = "concept";
	//static public final String LOCATION = "assets";
	static private     Audit    audit = new Audit( "Assets" );
	
	static private Object  context = null; // if null, not on Android
	static public  Object  context() { return context; }
	static public  void    context( Object activity ) { context = activity; }
	
	static public String getConfig() {
		String rc = Fs.stringFromFile( Enguage.RO_SPACE + "/config.xml" );
		if (rc.equals( "" )) {
			rc = Fs.stringFromFile( "/app/etc/config.xml" );
			if (rc.equals( "" ))
				audit.ERROR( "config not found" );
		}
		return rc;
	}
	static public String[] listConcepts() {
		String[] names = new File( Concepts.roRpts() ).list();
		if (names == null) { // try flatpak location
			Concepts.isFlatpakLocation();
			names = new File( Concepts.roRpts() ).list();
		}
		return names;
	}
	static public String loadConcept( String name, String from, String to ) {
		boolean wasLoaded   = false,
		        wasSilenced = false,
		        wasAloud    = org.enguage.Enguage.shell().isAloud();
		String conceptName = to==null ? name : name.replace( from, to );
		
		Variable.set( LOADING, name );
		
		// silence inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend();
			org.enguage.Enguage.shell().aloudIs( false );
		}
		
		Intention.concept( conceptName );
		InputStream  is = null;
		
		try { // ...add concept from user space...
			is = new FileInputStream( org.enguage.interp.repertoire.Concepts.spokenName( name ));
			org.enguage.Enguage.shell().interpret( is, from, to );
			wasLoaded = true;
		} catch (IOException e1) {
			InputStream is2 = null;
			try { // ...or add concept from asset...
				String fname = org.enguage.interp.repertoire.Concepts.writtenName( name );
				is2 = new FileInputStream( fname );
				org.enguage.Enguage.shell().interpret( is2, from, to );
				wasLoaded = true;
			} catch (IOException e2) {
			} finally {if (is2 != null) try{is2.close();} catch(IOException e2) {} }
		} finally { if (is != null) try{is.close();} catch(IOException e2) {} }
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			org.enguage.Enguage.shell().aloudIs( wasAloud );
		}
		
		Variable.unset( LOADING );
		return wasLoaded ? conceptName : "";
}	}
