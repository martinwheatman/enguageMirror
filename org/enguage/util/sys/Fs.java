package org.enguage.util.sys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.enguage.util.Audit;

public class Fs {
	static Audit audit = new Audit( "Fs" );
	
	static private String  location = "";
	static public  String  location() { return location; }
	static public  boolean location( String s ) {
		location = s;
		return s != null && new File( s ).exists();
	}

	static private String rootDir = ".";
	static public  String rootDir() { return rootDir; }
	static public  void   rootDir( String name ) {
		new File( rootDir = name + File.separator ).mkdirs();
	}

	// Composite specific
	static public boolean createEntity( String name ) { return new File( name ).mkdirs(); }
	static public boolean renameEntity( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	static public boolean existsEntity( String name ) { return new File( name ).isDirectory(); }
	static public boolean destroyEntity( String name ) { return new File( name ).delete(); }
	// General
	static public boolean create( String name ) { if (name == null) name = "."; return new File( name ).mkdirs(); }
	static public boolean rename( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	//static public boolean exists( String NAME ) { return new File( NAME ).isDirectory() || new File( NAME ).isFile(); }
	static public boolean exists( String fname ) { return fname != null && new File( fname ).exists(); } // ultimately less File() creation!
	static public boolean destroy( String name ) {
		File dir = new File( name );
		String[] list = dir.list();
		if (list != null) for (int i=0; i<list.length; i++)
			destroy( name+ File.separator +list[ i ]);
		return dir.delete();
	}
	static public boolean stringToFile( String fname, String value ) {
		boolean rc = true;
		create( new File( fname ).getParent());
		try {
			PrintWriter pw = new PrintWriter( fname );
			pw.println( value );
			pw.close();
		} catch (FileNotFoundException e ) {
			rc = false;
		}
		return rc;
	}
	static public boolean stringAppendFile( String fname, String value ) {
		boolean rc = true;
		create( new File( fname ).getParent());
		try {
			BufferedWriter pw = new BufferedWriter( new FileWriter( fname, true ));
			pw.append( value );
			pw.close();
		} catch (IOException e) {rc = false;}
		return rc;
	}
	static public String stringFromFile( String fname ) {
		//audit.IN("stringFromFile", "name="+fname );
		String value = ""; // need to check elsewhere if file exists
		try {
			FileInputStream fis = new FileInputStream( fname );
			value = stringFromStream( fis );
			fis.close();
		} catch (Exception e) {} // just ignore non-existant files...
		//audit.OUT( value );
		return value;
	}
	static private String stringFromStream( InputStream is ) {
		//audit.in( "stringFromStream" );
		String value = "";
		try {
			int n;
			byte buf[] = new byte[ 1024 ];
			while (-1 != (n = is.read(buf))) {
				for (int i=n; i<1024; i++) buf[ i ] = ' ';
				value += new String( buf );
			}
			value = value.trim(); // remove trailing blanks?
		} catch (IOException e) {
			// just ignore non-existent files...
			//audit.log( "Fs::stringFromInputStream(): IO exception: "+ e );
			value = "";
		}
		//audit.out( value );
		return value;
}	}
