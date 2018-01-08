package org.enguage;

import java.io.File;

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

public class Enguage extends Shell {

	public static final String DNU = "DNU";

	public Enguage() { super( "Enguage" ); }
	
	static private  Audit audit = new Audit( "Enguage" );
	
	/* Enguage is a singleton, so that its internals can refer
	 * to the outer instance. Enguage is therefore instantiated
	 * at runtime.
	 */
	static public Enguage e = new Enguage();

	/*
	 * Enguage should be independent of Android, but...
	 */
	static private Object context = null; // if null, not on Android
	static public  Object context() { return context; }
	static public  void   context( Object activity ) { context = activity; }
    
	static public String location() { return Fs.location();}
	static public void   location( String loc ) {
		if(!Fs.location( loc ))
			audit.FATAL(loc +": not found");
		else if(!Overlay.autoAttach())
			audit.FATAL(">>>>>>>>Ouch! Cannot autoAttach() to object space<<<<<<");
	}

	static public void   root( String rt ) {Fs.root( rt );}
	static public String root() { return Fs.root();}

	static public  Enguage get() { return e; }
	static public  void    init( String location ) {
		audit.in( "Enguage", "location=" + location );
		location( location );
		Redo.spokenInit();
		Repertoire.primeUsedInit();
		audit.out();
	}
	static public void concepts( String[] names ) { Concepts.names( names ); }

	public Overlay o = Overlay.Get();
		
	private        Config     config = new Config();
	public  static int    loadConfig( String content ) { return get().config.load( content ); }

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
		return Enguage.get().interpret( new Strings( utterance ));
	}
	
	// ==== test code =====
	private static void usage() {
		audit.LOG( "Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | [--server <port>] -t ]" );
		audit.LOG( "where: -d <configDir>" );
		audit.LOG( "          config directory, default=\"./src/assets\"\n" );
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
		
		String location = "./src/assets";
		if (cmds.size() > 0 && cmd.equals( "-d" )) {
			location = cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
		}

		Enguage.init( location );
		Enguage.concepts( new File( location + "/concepts" ).list() );
		Enguage.loadConfig( Fs.stringFromFile( Fs.location() + "/config.xml" ) );
		
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