package com.yagadi.enguage;

import com.yagadi.enguage.object.Overlay;
import com.yagadi.enguage.sign.intention.Redo;
import com.yagadi.enguage.sign.repertoire.Autoload;
import com.yagadi.enguage.sign.repertoire.Concepts;
import com.yagadi.enguage.sign.repertoire.Repertoire;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Net;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;

public class Enguage extends Shell {
	
	public Enguage() { super( "Enguage" ); }

	static private  Audit audit = new Audit( "Enguage" );
	
	/* Enguage is a singleton, so that its internals can refer to the outer instance.
	 */
	static private Enguage e = new Enguage();
	static public  Enguage get() { return e; }
	static public  void    set( String location ) {
		audit.in( "Engauge", "location=" + location );

		if (!Fs.location( location ))
			audit.FATAL( location + ": not found" );
		else if (!Overlay.autoAttach())
			audit.FATAL( "Ouch! Cannot autoAttach() to object space" );
		else {
			Concepts.names( location );
			Redo.spokenInit();
			Repertoire.primeUsedInit();
		}
		audit.out();
	}

	public Overlay o = Overlay.Get();
	
	public void  log( String s ) { audit.log( s ); }
	
	private Config      config = new Config();
	public  Enguage loadConfig() { config.load(); return this; }

	@Override
	public String interpret( Strings utterance ) {
		audit.in( "interpret", utterance.toString() );
		//if (!audit.tracing && !Audit.allTracing) audit.log( utterance.toString( Strings.SPACED ));

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
			audit.debug( "Enguage:interpret(): not understood, forgeting to ignore: " + Repertoire.signs.ignore().toString() );
			Repertoire.signs.ignoreNone();
			aloudIs( true ); // sets aloud for whole session if reading from fp
		}

		// autoload() in Repertoire.interpret() -- there is a reason for this asymmetry
		if (!Repertoire.isInducting() && !Autoload.ing()) Autoload.unload();

		return audit.out( reply );
	}
	
	// === public static calls ===
	public static String interpret( String utterance ) {
		return Enguage.get().interpret( new Strings( utterance ));
	}
	public static void loadConfig( String location ) {
		set( location );
		Enguage.get().loadConfig();
	}
	
	// ==== test code =====
	private static void usage() {
		audit.LOG( "Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | [--server [<port>]] -t ]" );
		audit.LOG( "where: -d <configDir>" );
		audit.LOG( "          config directory, default=\"./src/assets\"\n" );
		audit.LOG( "       -p <port>, --port <port>" );
		audit.LOG( "          listens on local TCP/IP port number\n" );
		audit.LOG( "       -s, --shell" );
		audit.LOG( "          runs Engauge as a shell\n" );
		audit.LOG( "       --server [<port>]" );
		audit.LOG( "          switch to send test commands to a server." );
		audit.LOG( "          This is only a test, and is on localhost." );
		audit.LOG( "          (Needs to be initialised with -p nnnn)\n" );
		audit.LOG( "       -t, --test" );
		audit.LOG( "          runs a sanity check" );
	}
	public static void main( String args[] ) {
		
		Strings cmds = new Strings( args );
		String  cmd  = cmds.size()==0 ? "":cmds.remove( 0 );
		
		String location = "./src/assets";
		if (cmds.size() > 0 && cmd.equals( "-d" )) {
			location = cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
		}
		Enguage.loadConfig( location );
		
		boolean serverTest = false;
		if (cmds.size() > 0 && cmd.equals( "--server" )) {
			serverTest = true;
			cmds.remove(0);
			cmd = cmds.size()==0 ? "":cmds.remove(0);
			if (!cmd.equalsIgnoreCase( "-t" ) && !cmd.equalsIgnoreCase( "--test" )) {
				Test.portNumber( cmds.remove( 0 ));
				cmd = cmds.size()==0 ? "":cmds.remove(0);
			}
		}
				
		if (cmd.equals( "-s" ) || cmd.equals( "--shell" ))
			e.aloudIs( true ).run();
		
		else if (cmds.size()>0 && (cmd.equals( "-p" ) || cmd.equals( "--port" )))
			Net.server( cmds.remove( 0 ));
		
		else if (cmd.equals( "-t" ) || cmd.equals( "--test" ))
			Test.sanityCheck( serverTest, location );
		
		else
			usage();
}	}