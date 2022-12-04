package com.yagadi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.repertoire.ConceptNames;
import org.enguage.util.Strings;

public class Assets {
	
	private Assets() {}
	
	public  static final String     NAME = "assets";
	public  static final String LOCATION = "etc" + File.separator;
	
	private static Object  context = null; // if null, not on Android
	public  static Object  context() { return context; }
	public  static void    context( Object activity ) { context = activity; }
	
	public static InputStream getStream( String name ) {
		InputStream is = null;
		try {
			is = new FileInputStream( name );
		} catch (IOException ignore) {
			/* ignore ERROR "gone missing: "+ name  */
		}
		return is;
	}
	public static String[] listConcepts() {
		Strings names = ConceptNames.tree( ConceptNames.roRpts( "" ), "." );
		if (names.isEmpty()) { // try flatpak location
			names = ConceptNames.tree( ConceptNames.roRpts( "/app/" ), "." );
			ConceptNames.isFlatpak( !names.isEmpty() );
		}
		String[] array = new String[ names.size() ];
		return names.toArray( array );
}	}
