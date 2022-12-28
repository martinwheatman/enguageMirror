package org.enguage.repertoire;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.repertoire.concept.Autoload;
import org.enguage.repertoire.concept.Load;
import org.enguage.signs.Signs;
import org.enguage.signs.interpretant.Commands;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.objects.space.Overlay;
import org.enguage.signs.symbol.reply.Answer;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;
import org.enguage.util.tag.Tag;

import com.yagadi.Assets;

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
				else if (name.equals( "CLASSPATH" )) Commands.classpath( value );
				else if (name.equals( "LOCATION"  )) Fs.location( value );
				else if (name.equals( "SUCCESS" )) Response.success( value );
				else if (name.equals( "FAILURE" )) Response.failure( value );
				else if (name.equals(  "ANSWER" )) Answer.placeholder( value );
				else if (name.equals(   "SHELL" )) Commands.shell( value );
				else if (name.equals(    "TERMS")) Shell.terminators( new Strings( value ));
				else if (name.equals(    "SOFA" )) Commands.java( value );
				else if (name.equals(     "TTL" )) Autoload.ttl( value );
				else if (name.equals(     "DNU" )) Response.dnu( value );
				else if (name.equals(     "DNK" )) Response.dnk( value );
				else if (name.equals(     "YES" )) Response.yes( value );
				else if (name.equals(      "NO" )) Response.no(  value );
				//else if (name.equals(      "IK" )) Response.ik(  value );
				else
					Variable.set( name,  value );
	}	}	}

	public static int load( String fname ) {
		int rc = -1;
		audit.in( "load", fname );
		String content = Fs.stringFromStream(
				Assets.getStream( Enguage.RO_SPACE+ File.separator + fname )
		);
		if (content.equals( "" )) {
			content = Fs.stringFromFile( "/app/etc/"+ fname );
			if (content.equals( "" ))
				audit.ERROR( "config not found" );
		}
		Audit.allOff();
		if (Audit.startupDebug) Audit.allOn();
		
		long then = new GregorianCalendar().getTimeInMillis();
		Redo.undoEnabledIs( false );
		
		if (Enguage.isVerbose())
			Audit.log(
				welcome(
					Enguage.shell().copyright() +
					"\nEnguage main(): odb root is: " + Fs.root()
			)	);

		Tag t = new Tag( new Strings( content ).listIterator());

		if ((t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			Load.loadTag( t.findByName( "concepts" ));
			rc = content.length();
		}

		Redo.undoEnabledIs( true );
		long now = new GregorianCalendar().getTimeInMillis();
		
		if (Enguage.isVerbose()) {
			Audit.log( "Initialisation in: " + (now - then) + "ms" );
			Audit.log( Signs.stats() );
		}
		
		Audit.allOff();
		if (Audit.runtimeDebug) Audit.allOn();
		return audit.out( rc );
	}
	public static void main( String args[]) {
		Overlay.Set( Overlay.Get());
		Overlay.attach( NAME );
		Config.load( "" );
}	}
