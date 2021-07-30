package org.enguage;

import java.io.File;

import org.enguage.interp.intention.Redo;
import org.enguage.interp.repertoire.Autoload;
import org.enguage.interp.repertoire.Concepts;
import org.enguage.interp.repertoire.Repertoire;
import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Server;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.reply.Reply;
import org.enguage.vehicle.where.Where;

import com.yagadi.Assets;

public class Enguage {
	
	private static String copyright = "Martin Wheatman, 2001-4, 2011-21";

	public  static final String      RO_SPACE = Assets.LOCATION;
	public  static final String      RW_SPACE = "var"+ File.separator;
	
	public  static final String           DNU = "DNU";
	private static final boolean startupDebug = false;
	
	private static Audit     audit = new Audit( "Enguage" );
	public  static Overlay       o = Overlay.Get();

	private static Shell   shell   = new Shell( "Enguage", copyright );
	public  static Shell   shell() {return shell;}
	
	public  static boolean verbose = false;
	
	public  static void init() {init( RW_SPACE );}
	public  static void init( String root ) {
		Fs.root( root );
		Concepts.addConcepts( Assets.listConcepts() );
		Config.load( "config.xml" );
	}
	
	public static Strings mediate( Strings said ) { return mediate( "uid", said );}
	public static Strings mediate( String uid, Strings utterance ) {
				
		Strings reply;
		audit.in( "mediate", utterance.toString() );
		
		Overlay.attach( uid );
			
		if (Server.serverOn()) Audit.log( "Server  given: " + utterance.toString() );
		
		// locations contextual per utterance
		Where.clearLocation();
		
		if (Reply.isUnderstood()) // from previous interpretation!
			Overlay.startTxn( Redo.undoIsEnabled() ); // all work in this new overlay
		
		Reply r = Repertoire.mediate( new Utterance( utterance ));

		// once processed, keep a copy
		Utterance.previous( utterance );

		if (Reply.isUnderstood()) {
			Overlay.finishTxn( Redo.undoIsEnabled() );
			Redo.disambOff();
		} else {
			// really lost track?
			audit.debug( "Enguage:interpret(): not understood, forgetting to ignore: "
			             +Repertoire.signs.ignore().toString() );
			Repertoire.signs.ignoreNone();
			shell.aloudIs( true ); // sets aloud for whole session if reading from fp
		}

		// auto-unload here - autoloading() in Repertoire.interpret() 
		// asymmetry: load as we go; tidy-up once finished
		Autoload.unload();

		reply = Reply.say().appendAll( r.toStrings());
		Reply.say( null );
		
		if (Server.serverOn()) Audit.log( "Server replied: "+ reply );
			
		Overlay.detach();
		
		return audit.out( reply );
	}
	
	// test code....
	
	public static void usage() {
		Audit.LOG(
			 "Usage: java [-jar enguage.jar|org.enguage.Enguage]\n"  // program
			+"            --verbose --data <path> --server <port>\n" // switches
			+"            [-h | --port <port> | --httpd <port> | [<utterance>] | --test ]" //optis
		);
		Audit.LOG( "Switches are:" );
		Audit.LOG( "       -v, --verbose\n" );
		Audit.LOG( "       -d, --data <path> specifies the data volume to use\n" );
		Audit.LOG( "       -s, --server <port>" );
		Audit.LOG( "          switch to send test commands to a server." );
		Audit.LOG( "          This is only a test, and is on localhost." );
		Audit.LOG( "          (Needs to be initialised with -p nnnn);\n" );
		Audit.LOG( "Options are:" );
		Audit.LOG( "       -h, --help" );
		Audit.LOG( "          displays this message\n" );
		Audit.LOG( "       -p, --port <port>" );
		Audit.LOG( "          listens on local TCP/IP port number\n" );
		Audit.LOG( "       -H, --httpd [<port>]" );
		Audit.LOG( "          webserver on port number, default to 8080\n" );
		Audit.LOG( "       -t, --test <n>, -T <name>" );
		Audit.LOG( "          runs a self test, where" );
		Audit.LOG( "           n is the test number, or" );
		Audit.LOG( "          -n excludes a test, or" );
		Audit.LOG( "          -T <name> is part of the test name.\n" );
		Audit.LOG( "       [<utterance>]" );
		Audit.LOG( "          with an utterance it runs one-shot;" );
		Audit.LOG( "          with no utterance it runs as a shell," );
		Audit.LOG( "             requiring full stops (periods) to" );
		Audit.LOG( "             terminate utterances." );
	}
	
	public static void main( String args[] ) {
		
		Audit.startupDebug = startupDebug;
		Strings    cmds = new Strings( args );
		String     cmd,
		           fsys = RW_SPACE;
		int i = 0;
		while (i < cmds.size()) {
			cmd = cmds.get( i );
			if (cmd.equals( "-v" ) || cmd.equals( "--verbose" )) {
				verbose = true;
				cmds.remove( i );
			} else if (cmd.equals( "-s" ) || cmd.equals( "--server" )) {
				Example.serverTest = true;
				cmds.remove( i );
				cmd = cmds.size()==0 ? "8080":cmds.remove( i );
				Example.portNumber( cmd );
			} else if (cmd.equals( "-d" ) || cmd.equals( "--data" )) {
				cmds.remove( i );
				fsys = cmds.size()==0 ? fsys : cmds.remove( i );
			} else
				i++;
		}

		init( fsys );
				
		cmd = cmds.size()==0 ? "":cmds.remove( 0 );
		if (cmd.equals( "-p" ) || cmd.equals( "--port" ))
			Server.server( cmds.size() == 0 ? "8080" : cmds.remove( 0 ));
		
		else if (cmd.equals( "-H" ) || cmd.equals( "--httpd" ))
			Server.httpd( cmds.size() == 0 ? "8080" : cmds.remove( 0 ));
		
		else if (cmd.equals( "-t" )
			  || cmd.equals( "--test" )
			  || cmd.equals( "-T" ))
			Example.selfTest( cmd, cmds );
		
		else if (cmd.equals( "-h" ) || cmd.equals( "--help" ))
			Enguage.usage();
		
		else if (cmd.equals( "" )) {
			Overlay.attach( "uid" );
			shell.aloudIs( true ).run();
		
		} else {
			// Command line parameters exists...
			// reconstruct original commands and interpret...

			// - remove full stop, if one given -
			cmds = new Strings( cmds.toString() );
			if (cmds.get( cmds.size()-1 ).equals( "." )) cmds.remove( cmds.size()-1 );

			// ...reconstruct original commands and interpret
			Example.testRun( cmds.prepend( cmd ));
	}	}
}
