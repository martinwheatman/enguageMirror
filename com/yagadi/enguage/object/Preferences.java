package com.yagadi.enguage.object;

/* so this class is for the bit bucket, but until it is implemented
 * in the appcode, we need to retain it 'cos it's what needs implementing
 * or its what needs restoring here...!
 */

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

//import android.content.SharedPreferences;

/* Preferences are implemented in the SofA, but are of device structures.
 * The reason for this is so that enguage can also access values. 
 */
public class Preferences {
	static private Audit audit = new Audit( "Preferences" );
	
//	static SharedPreferences shPref = null; // can't set yet -- causes null ptr exception
//	public Preferences( SharedPreferences s ) { shPref = s; }

	// singleton
	static private Preferences preferences = null;
	static public  void        setPreferences( Preferences p ){ preferences = p; }
	static public  Preferences getPreferences(){ return preferences; }
	
	public String get( String name, String defVal ) {
		if (defVal.equalsIgnoreCase( "true" ) || defVal.equalsIgnoreCase( "false" ))
			return get( name, defVal.equalsIgnoreCase( "true" )) ? "true" : "false";
		else
			return ""; // shPref.getString( name, defVal );
	}
	public void set( String name, String value ) {
		if (value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "false" ))
			set( name, value.equalsIgnoreCase( "true" ));
		else {
//			SharedPreferences.Editor editor = shPref.edit();
//			editor.putString( name, value );
//			editor.commit();
	}	}
	public boolean get( String name, boolean defVal ) {
		return false; //shPref.getBoolean( name, defVal );
	}
	public void set( String name, boolean value ) {
//		SharedPreferences.Editor editor = shPref.edit();
//		editor.putBoolean( name, value );
//		editor.commit();
	}
/*	public float get( String NAME, float defVal ) {
		return shPref.getFloat( NAME, defVal ); // defer to SharedPreference version
	}
	public void set( String NAME, float value ) {
		SharedPreferences.Editor editor = shPref.edit();
		editor.putFloat( NAME, value );
		editor.commit();
}*/
	static public String interpret( Strings a ) {
		String rc = Shell.FAIL;
		audit.in( "interpret", a.toString( Strings.CSV ));
		if (preferences != null && null != a && a.size() >= 2) {
			if (a.get( 0 ).equals( "set" )) {
				preferences.set( a.get( 1 ), a.copyAfter( 1 ).toString( Strings.SPACED ) ); // default value?
				rc = Shell.SUCCESS;
			} else if (a.get( 0 ).equals( "get" )) {
				rc = preferences.get( a.get( 1 ), true ) ? Shell.SUCCESS : Shell.FAIL; // default value true?
		}	}
		audit.out( rc );
		return rc;
}	}
