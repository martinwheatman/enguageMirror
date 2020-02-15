package com.yagadi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.interp.intention.Intention;
import org.enguage.interp.repertoire.Concepts;
import org.enguage.objects.Variable;
import org.enguage.util.Audit;

public class Assets {
	
	static public final String  LOADING = "concept";
	static public final String     NAME = "assets";
	//static private     Audit    audit = new Audit( NAME );
	//static public final String LOCATION = NAME + File.separator;
	
	static public void addConcepts( String loc ) {
		String[] names = new File( loc + Concepts.NAME ).list();
		if (names != null) for ( String name : names ) { // e.g. name="hello.txt"
			String[] components = name.split( "\\." );
			if (components.length > 1 && components[ 1 ].equals("txt"))
				org.enguage.interp.repertoire.Concepts.add( components[ 0 ]);
	}	}
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
			is = new FileInputStream( org.enguage.interp.repertoire.Concepts.name( name ));
			org.enguage.Enguage.shell().interpret( is, from, to );
			wasLoaded = true;
		} catch (IOException e1) {
			InputStream is2 = null;
			try { // ...or add concept from asset...
				String fname = org.enguage.interp.repertoire.Concepts.name( name );
				is2 = new FileInputStream( NAME + File.separator + fname );
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
		return wasLoaded;
}	}
