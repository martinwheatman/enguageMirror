package org.enguage;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.interp.intention.Redo;
import org.enguage.interp.repertoire.Autoload;
import org.enguage.interp.repertoire.Concepts;
import org.enguage.interp.sign.Signs;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Proc;
import org.enguage.util.sys.Shell;
import org.enguage.util.tag.Tag;
import org.enguage.vehicle.reply.Answer;
import org.enguage.vehicle.reply.Reply;

public class Config {
	static       private Audit audit = new Audit( "Config" );
	static final private String NAME = "config";
	
	static private String welcome = "welcome";
	static public  String welcome() { return welcome; }
	static public  String welcome( String w ) { return welcome = w; }

	static private boolean firstRun = true;
	static public  boolean firstRun() { return firstRun; }
	static public  void    firstRun( boolean b ) { firstRun = b; }
	
	private static void setContext( ArrayList<Attribute> aa ) {
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
				else if (name.equals( "REFERENCERS" )) Reply.referencers(   new Strings( value ));
				else if (name.equals( "CLASSPATH" )) Proc.classpath( value );
				else if (name.equals( "LOCATION"  )) Fs.location( value );
				else if (name.equals( "HPREFIX" )) Reply.helpPrefix( value );
				else if (name.equals( "SUCCESS" )) Reply.success( value );
				else if (name.equals( "FAILURE" )) Reply.failure( value );
				else if (name.equals( "NOTVARS" )) Variable.exceptionAdd( new Strings( value ));
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
				else
					Variable.set( name,  value );
	}	}	}

	public int load( String content ) {
		int rc = -1;
		audit.in( "load", content );
		Audit.allOff();
		if (Audit.startupDebug) Audit.allOn();
		
		long then = new GregorianCalendar().getTimeInMillis();
		Redo.undoEnabledIs( false );
		
		if (Enguage.verbose)
			Audit.log(
				welcome(
					Enguage.shell().copyright() +
					"\nEnguage main(): odb root is: " + Fs.root()
			)	);

		Tag t = new Tag( content );
		if (t != null && (t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			Concepts.load( t.findByName( "concepts" ));
			rc = content.length();
		}

		Redo.undoEnabledIs( true );
		long now = new GregorianCalendar().getTimeInMillis();
		
		if (Enguage.verbose) {
			Audit.log( "Initialisation in: " + (now - then) + "ms" );
			Audit.log( Signs.stats() );
		}
		
		Audit.allOff();
		if (Audit.runtimeDebug) Audit.allOn();
		return audit.out( rc );
	}
	
	public static void main( String args[]) {
		Overlay.Set( Overlay.Get());
		if (!Overlay.attach( NAME ))
			audit.ERROR( "Ouch!" );
		Config c = new Config();
		c.load( "" );
}	}
