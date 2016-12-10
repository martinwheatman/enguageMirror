package com.yagadi.enguage;

import java.util.ArrayList;
import java.util.ListIterator;

import com.yagadi.enguage.concept.Autoload;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Config {
	/*
	 * This class brings together several features used in the iNeed app, anda are useful
	 * for making the engine more transparent to the app...
	 */
	static public String welcome = "welcome";
	static public String welcome() { return welcome; }
	static public String welcome( String w ) { return welcome = w; }

	static public boolean firstRun = true;
	static public boolean firstRun() { return firstRun; }
	static public void    firstRun( boolean b ) { firstRun = b; }
	
	static public boolean visualMode = true;
	static public boolean visualMode() { return visualMode; }
	static public boolean visualMode( boolean b ) { return visualMode = b; }
	
	static public boolean verboseMode = true;
	static public boolean verboseMode() { return verboseMode; }
	static public boolean verboseMode( boolean b ) { return verboseMode = b; }
	
	static public boolean previewMode = true;
	static public boolean previewMode() { return previewMode; }
	static public boolean previewMode( boolean b ) { return previewMode = b; }
	
	static public String directionToSpeak = "direction To Speak";
	static public String directionToSpeak() { return directionToSpeak; }
	static public String directionToSpeak( String s ) { return  directionToSpeak= s; }

	static public String helpOnHelp = "help on help";
	static public String helpOnHelp() { return helpOnHelp; }
	static public String helpOnHelp( String s ) { return  helpOnHelp= s; }
	
	public static void setContext( ArrayList<Attribute> aa ) {
		if (null != aa) {
			ListIterator<Attribute> pi = aa.listIterator();
			while (pi.hasNext()) {
				Attribute a = pi.next();
				String name = a.name(), value=a.value();
				if (name.equals("LISTFORMATSEP")) Reply.listSep(       value); else
				if (name.equals("ANDCONJUNCTIONS")) Reply.andConjunctions(  new Strings( value )); else
				if (name.equals("ORCONJUNCTIONS")) Reply.orConjunctions(  new Strings( value )); else
				if (name.equals("REFERENCERS"  )) Reply.referencers(   new Strings( value )); else
				if (name.equals("ANDLISTFORMAT")) Reply.andListFormat( value); else
				if (name.equals("ORLISTFORMAT" )) Reply.orListFormat(  value ); else
				if (name.equals("REPEATFORMAT" )) Reply.repeatFormat(  value ); else
				if (name.equals("LOCATION"  )) Filesystem.location( value ); else
				if (name.equals("HPREFIX")) Reply.helpPrefix( value ); else
				if (name.equals("SUCCESS")) Reply.success( value ); else
				if (name.equals("FAILURE")) Reply.failure( value ); else
				if (name.equals("TERMS")) Shell.terminators( new Strings( value )); else
				if (name.equals( "TTL" )) Autoload.ttl( value ); else
				if (name.equals( "DNU" )) Reply.dnu( value ); else
				if (name.equals( "DNK" )) Reply.dnk( value ); else
				if (name.equals( "YES" )) Reply.yes( value ); else
				if (name.equals(  "NO" )) Reply.no(  value ); else
				if (name.equals(  "IK" )) Reply.ik(  value );
}	}	}	}