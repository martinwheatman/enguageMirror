package org.enguage.sign;

import android.app.Activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Assets {
	private Assets() {}
	
	public  static final String     NAME = "assets";
	
	private static Activity  context = null; // if null, not on Android
	public  static Activity  context() {return context;}
	public  static void      context( Activity activity ) {context = activity;}
	
	public static InputStream getStream( String name ) {
		InputStream is = null;
		try {
			is = context().getAssets().open( name );
		} catch (IOException ignore) {
			//audit.error( "Assets.getStream(): gone missing: "+ name );
		}
		return is;
	}
	public static String[] list( String path ) {
		if (path.endsWith("/.")) path = path.substring(0,path.length()-2);
		String[] names = null;
		try {
			names = context().getAssets().list( path );
		} catch (IOException iox) {
		}
		return names;
}	}
