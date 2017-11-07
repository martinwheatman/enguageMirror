package com.yagadi.enguage;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.object.Attribute;
import com.yagadi.enguage.object.Ospace;
import com.yagadi.enguage.object.Overlay;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.sign.Signs;
import com.yagadi.enguage.sign.intention.Allopoiesis;
import com.yagadi.enguage.sign.repertoire.Autoload;
import com.yagadi.enguage.sign.repertoire.Concepts;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Proc;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.util.Tag;
import com.yagadi.enguage.vehicle.Answer;
import com.yagadi.enguage.vehicle.Reply;

public class Config {
	static       private Audit audit = new Audit( "Config" );
	static final private String NAME = "config";
	
	static public String welcome = "welcome";
	static public String welcome() { return welcome; }
	static public String welcome( String w ) { return welcome = w; }

	static public boolean firstRun = true;
	static public boolean firstRun() { return firstRun; }
	static public void    firstRun( boolean b ) { firstRun = b; }
	
	/* this needs to go into the app...
	static public boolean visualMode = true;
	static public boolean visualMode() { return visualMode; }
	static public boolean visualMode( boolean b ) { return visualMode = b; }
	
	static public boolean verboseMode = true;
	static public boolean verboseMode() { return verboseMode; }
	static public boolean verboseMode( boolean b ) { return verboseMode = b; }
	
	static public boolean previewMode = true;
	static public boolean previewMode() { return previewMode; }
	static public boolean previewMode( boolean b ) { return previewMode = b; }
	// as does this...
	static public String directionToSpeak = "direction To Speak";
	static public String directionToSpeak() { return directionToSpeak; }
	static public String directionToSpeak( String s ) { return  directionToSpeak = s; }

	static public String helpOnHelp = "help on help";
	static public String helpOnHelp() { return helpOnHelp; }
	static public String helpOnHelp( String s ) { return  helpOnHelp = s; }
	// */
	public static void setContext( ArrayList<Attribute> aa ) {
		if (null != aa) {
			ListIterator<Attribute> pi = aa.listIterator();
			while (pi.hasNext()) {
				Attribute a = pi.next();
				String name = a.name().toUpperCase( Locale.getDefault()), value=a.value();
				     if (name.equals("LISTFORMATSEP")) Reply.listSep(       value);
				else if (name.equals("ANDCONJUNCTIONS")) Reply.andConjunctions(  new Strings( value ));
				else if (name.equals("ORCONJUNCTIONS")) Reply.orConjunctions(  new Strings( value ));
				else if (name.equals("ANDLISTFORMAT" )) Reply.andListFormat( value);
				else if (name.equals( "ORLISTFORMAT" )) Reply.orListFormat(  value );
				else if (name.equals( "REPEATFORMAT" )) Reply.repeatFormat(  value );
				else if (name.equals( "REFERENCERS" )) Reply.referencers(   new Strings( value ));
				else if (name.equals( "CLASSPATH" )) Proc.classpath( value );
				else if (name.equals( "LOCATION"  )) Fs.location( value );
				else if (name.equals( "HPREFIX" )) Reply.helpPrefix( value );
				else if (name.equals( "SUCCESS" )) Reply.success( value );
				else if (name.equals( "FAILURE" )) Reply.failure( value );
				else if (name.equals(  "ANSWER" )) Answer.placeholder( value );
				else if (name.equals(   "SHELL" )) Proc.shell( value );
				else if (name.equals(    "TERMS")) Shell.terminators( new Strings( value ));
				else if (name.equals(    "SOFA" )) Proc.java( value );
				else if (name.equals(     "TTL" )) Autoload.ttl( value );
				else if (name.equals(     "DNU" )) Reply.dnu( value );
				else if (name.equals(     "DNK" )) Reply.dnk( value );
				else if (name.equals(     "YES" )) Reply.yes( value );
				else if (name.equals(      "NO" )) Reply.no(  value );
				else if (name.equals(      "IK" )) Reply.ik(  value );
				else {
					audit.LOG( "Saving name='"+ name +"', value='"+ value +"'");
					Variable.set( name,  value );
				}
	}	}	}

	
	public void load() { load( "" );}
	public void load( String name ) {
		audit.in( "load", name = name.equals("") ? NAME : name );
		Audit.allOff();
		if (Audit.startupDebug) Audit.allOn();
		
		long then = new GregorianCalendar().getTimeInMillis();
		Allopoiesis.undoEnabledIs( false );
		
		audit.log(
				welcome(
					Enguage.get().copyright() +
					"\nEnguage main(): overlay is: " + Overlay.Get().toString()
			)	);
		//directionToSpeak( "press the button and speak" );
		//helpOnHelp( "just say help" );

		Tag t = Tag.fromFile( Ospace.location() + name +".xml" );
		if (t != null && (t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			Concepts.load( t.findByName( "concepts" ));
		}

		Allopoiesis.undoEnabledIs( true );
		long now = new GregorianCalendar().getTimeInMillis();
		
		audit.log( "Initialisation in: " + (now - then) + "ms" );
		audit.log( Signs.stats() );

		Audit.allOff();
		if (Audit.runtimeDebug) Audit.allOn();
		audit.out();
	}
	
	public static void main( String args[]) {
		Enguage.set( "./src/assets" );
		Config c = new Config();
		c.load();
}	}