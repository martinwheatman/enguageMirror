package org.enguage.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Assets {
	private Assets() {}
	
	public  static final String     NAME = "assets";
	
	private static Object    context = null; // if null, not on Android
	public  static Object    context() {return context;}
	public  static void      context( Object   activity ) {context = activity;}
	
	public static InputStream getStream( String name ) {
		InputStream is = null;
		try {
			is = new FileInputStream( name );
		} catch (IOException ignore) {
			//audit.error( "Assets.getStream(): gone missing: "+ name );
		}
		return is;
	}
	public static String[] list( String path ) {
		//if (path.endsWith("/.")) only needed in android -- to fix!
		String[] names = null;
		names = new File( path ).list();
		return names;
}	}
