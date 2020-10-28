package com.yagadi;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.enguage.interp.repertoire.Concepts;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;
import org.enguage.Enguage;

public class Assets {
	
	static public final String     NAME = "assets";
	static public final String LOCATION = "etc" + File.separator;
	static private      Audit     audit = new Audit( "Assets" );
	
	static private Object  context = null; // if null, not on Android
	static public  Object  context() { return context; }
	static public  void    context( Object activity ) { context = activity; }
	
	static public InputStream getAsset( String name ) {
		InputStream is = null;
        try {
            is = new FileInputStream( name );
        } catch (IOException ignore) {
            //audit.ERROR( "gone missing: "+ name );
        }
        return is;
    }
	static public String getContent( String name ) {
		String rc = Fs.stringFromFile( Enguage.RO_SPACE + "/" + name );
		if (rc.equals( "" )) {
			rc = Fs.stringFromFile( "/app/etc/"+ name );
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
}	}
