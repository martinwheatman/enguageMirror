package org.enguage;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.sign.Signs;
import org.enguage.sign.intention.Redo;
import org.enguage.sign.repertoire.Autoload;
import org.enguage.sign.repertoire.Concepts;
import org.enguage.util.Attribute;
import org.enguage.util.Audit;
import org.enguage.util.Fs;
import org.enguage.util.Proc;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.util.Tag;
import org.enguage.vehicle.Answer;
import org.enguage.vehicle.Language;
import org.enguage.vehicle.Reply;

public class Config {
	static       private Audit audit = new Audit( "Config" );
	static final private String NAME = "config";
	
	static private String welcome = "welcome";
	static public  String welcome() { return welcome; }
	static public  String welcome( String w ) { return welcome = w; }

	static private boolean firstRun = true;
	static public  boolean firstRun() { return firstRun; }
	static public  void    firstRun( boolean b ) { firstRun = b; }
	
	public static void setContext( ArrayList<Attribute> aa ) {
		if (null != aa) {
			ListIterator<Attribute> pi = aa.listIterator();
			while (pi.hasNext()) {
				Attribute a = pi.next();
				String name = a.name().toUpperCase( Locale.getDefault()), value=a.value();
				     if (name.equals("LISTFORMATSEP")) Reply.listSep(       value);
				else if (name.equals("ANDCONJUNCTIONS")) Reply.andConjunction( value );
				else if (name.equals("ORCONJUNCTIONS")) Reply.orConjunctions(  new Strings( value ));
				else if (name.equals("ANDLISTFORMAT" )) Reply.andListFormat( value);
				else if (name.equals( "ORLISTFORMAT" )) Reply.orListFormat(  value );
				else if (name.equals( "REPEATFORMAT" )) Reply.repeatFormat(  value );
				else if (name.equals( "APOSTROPHES" )) Language.possessive(   new Boolean( value ));
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

	public int load( String content ) {
		int rc = -1;
		audit.in( "load", content );
		Audit.allOff();
		if (Audit.startupDebug) Audit.allOn();
		
		long then = new GregorianCalendar().getTimeInMillis();
		Redo.undoEnabledIs( false );
		
		audit.log(
				welcome(
					Enguage.get().copyright() +
					"\nEnguage main(): overlay is: " + Overlay.Get().toString()
			)	);

		Tag t = new Tag( content );
		if (t != null && (t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			Concepts.load( t.findByName( "concepts" ));
			rc = content.length();
		}

		Redo.undoEnabledIs( true );
		long now = new GregorianCalendar().getTimeInMillis();
		
		audit.log( "Initialisation in: " + (now - then) + "ms" );
		audit.log( Signs.stats() );

		Audit.allOff();
		if (Audit.runtimeDebug) Audit.allOn();
		return audit.out( rc );
	}
	
	public static void main( String args[]) {
		Enguage.e.location( "./src/assets" );
		Config c = new Config();
		c.load( "" );
}	}