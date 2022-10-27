package com.yagadi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.repertoire.Concepts;
import org.enguage.util.Strings;

public class Assets {
	
	static public final String     NAME = "assets";
	static public final String LOCATION = "etc" + File.separator;
	
	static private Object  context = null; // if null, not on Android
	static public  Object  context() { return context; }
	static public  void    context( Object activity ) { context = activity; }
	
	static public InputStream getStream( String name ) {
		InputStream is = null;
		try {
			is = new FileInputStream( name );
		} catch (IOException ignore) {
			//audit.ERROR( "gone missing: "+ name );
		}
		return is;
	}
	static public String[] listConcepts() {
		Strings names = Concepts.tree( Concepts.roRpts( "" ), "." );
		if (names.size() == 0) { // try flatpak location
			names = Concepts.tree( Concepts.roRpts( "/app/" ), "." );
			Concepts.isFlatpak( names.size() != 0 );
		}
		String[] array = new String[ names.size() ];
		return names.toArray( array );
}	}
