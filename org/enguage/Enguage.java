package org.enguage;

import org.enguage.object.Overlay;
import org.enguage.sign.intention.Redo;
import org.enguage.sign.repertoire.Autoload;
import org.enguage.sign.repertoire.Concepts;
import org.enguage.sign.repertoire.Repertoire;
import org.enguage.util.Audit;
import org.enguage.util.Fs;
import org.enguage.util.Net;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;
import org.enguage.vehicle.Utterance;

import java.io.File;

public class Enguage extends Shell {

	public static final String    DNU = "DNU";
	public static final String defLoc = "./src/assets";

	public Enguage() { super( "Enguage" ); }

	static private  Audit audit = new Audit( "Enguage" );

	/* Enguage is a singleton, so that its internals can refer
	 * to the outer instance.
	 */
	static public Enguage e = null;
	static public Enguage get() { return e; }

	public Overlay o = Overlay.Get();

	private static Config     config = new Config();
	public  static int    loadConfig( String content ) { return Enguage.config.load( content ); }

	/*
	 * Enguage should be independent of Android, but...
	 */
	private Object  context = null; // if null, not on Android
	public  Object  context() { return context; }
	public  Enguage context( Object activity ) { context = activity; return this; }

	public  Enguage concepts( String[] names ) { Concepts.names( names ); return this; }

	public  Enguage root( String rt ) { Fs.root( rt ); return this; }
	public  String  root() { return Fs.root();}

	public  void location( String location ) {
		audit.in( "Enguage", "location=" + location );
		if(!Fs.location( location ))
			audit.FATAL(location +": not found");

		Redo.spokenInit();
		Repertoire.primeUsedInit();
		audit.out();
	}

	public String interpret( Strings utterance ) {
		audit.in( "interpret", utterance.toString() );

		if (Reply.understood()) // from previous interpretation!
			o.startTxn( Redo.undoIsEnabled() ); // all work in this new overlay

		Reply r = Repertoire.interpret( new Utterance( utterance ));

		// once processed, keep a copy
		Utterance.previous( utterance );

		String reply = r.toString( utterance );
		if (Reply.understood()) {
			o.finishTxn( Redo.undoIsEnabled() );
			Redo.disambOff();
			Redo.spoken( true );
		} else {
			// really lost track?
			audit.debug( "Enguage:interpret(): not understood, forgeting to ignore: "
			             +Repertoire.signs.ignore().toString() );
			Repertoire.signs.ignoreNone();
			aloudIs( true ); // sets aloud for whole session if reading from fp
		}

		// auto-unload here - autoloading() in Repertoire.interpret() 
		// asymmetry: load as we go; tidy-up once finished
		if (!Repertoire.isInducting() && !Autoload.ing()) Autoload.unload();

		return audit.out( reply );
	}
	
	// === public static calls ===
	public static String interpret( String utterance ) {
		return e.interpret( new Strings( utterance ));
	}
	
	// ==== test code =====
	private static void usage() {
		audit.LOG( "Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | [--server <port>] -t ]" );
		audit.LOG( "where: -d <configDir>" );
		audit.LOG( "          config directory, default=\""+ defLoc +"\"\n" );
		audit.LOG( "       -p <port>, --port <port>" );
		audit.LOG( "          listens on local TCP/IP port number\n" );
		audit.LOG( "       -c, --client" );
		audit.LOG( "          runs Engauge as a shell\n" );
		audit.LOG( "       -s, --server <port>" );
		audit.LOG( "          switch to send test commands to a server." );
		audit.LOG( "          This is only a test, and is on localhost." );
		audit.LOG( "          (Needs to be initialised with -p nnnn)\n" );
		audit.LOG( "       -t, --test" );
		audit.LOG( "          runs a sanity check" );
	}
	public static void main( String args[] ) {

		//Audit.startupDebug = true;

		Strings cmds = new Strings( args );
		String  cmd  = cmds.size()==0 ? "":cmds.remove( 0 );

		e = new Enguage();
		
		String location = defLoc;
		if (cmds.size() > 0 && cmd.equals( "-d" )) {
			location = cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
		}
		e.location( location );
		e.root( null );
		e.context( null );


		if (null == Enguage.e.o || !Enguage.e.o.attached() )
			if (!Overlay.autoAttach())
				audit.FATAL(">>>>>>>>Ouch! Cannot autoAttach() to object space<<<<<<" );

		e.concepts( new File( location + "/concepts" ).list() );

		loadConfig( Fs.stringFromFile( location + "/config.xml" ));

		boolean serverTest = false;
		if (cmds.size() > 0 && (cmd.equals( "-s" ) || cmd.equals( "--server" ))) {
			serverTest = true;
			cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
			Test.portNumber( cmds.remove( 0 ));
			cmd = cmds.size()==0 ? "":cmds.remove(0);
		}
				
		if (cmd.equals( "-c" ) || cmd.equals( "--client" ))
			e.aloudIs( true ).run();
		
		else if (cmds.size()>0 && (cmd.equals( "-p" ) || cmd.equals( "--port" )))
			Net.server( cmds.remove( 0 ));
		
		else if (cmd.equals( "-t" ) || cmd.equals( "--test" ))
			Test.sanityCheck( serverTest, location );
		
		else
			usage();
}	}