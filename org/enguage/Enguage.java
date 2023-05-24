package org.enguage;

import java.io.File;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.signs.Config;
import org.enguage.signs.object.list.Item;
import org.enguage.signs.object.sofa.Overlay;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.where.Where;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Server;
import org.enguage.util.sys.Shell;

import com.yagadi.Assets;

public class Enguage {
	
	private static final String COPYRIGHT = "Martin Wheatman, 2001-4, 2011-23";
	public  static final String RO_SPACE  = Assets.LOCATION;
	public  static final String RW_SPACE  = "var"+ File.separator;

	private static Enguage enguage;
	public  static Enguage get() {return enguage;}
	public  static void    set( Enguage e ) {enguage = e;}
	
	private static Audit   audit   = new Audit( "Enguage" );

	private static Shell   shell   = new Shell( "Enguage", COPYRIGHT );
	public  static Shell   shell() {return shell;}
	
	private static boolean verbose = false;
	public  static boolean isVerbose() {return verbose;}
	public  static void    verboseIs(boolean b) {verbose = b;}
	
	private boolean imagined = false;
	public  boolean imagined() {return imagined;}
	public  void    imagined( boolean img ) {imagined = img;}
		
	public  Enguage() {this( RW_SPACE );}
	public  Enguage( String root ) {
		Fs.root( root );
		Concept.addNames( Assets.listConcepts() );
		Config.load( "config.xml" );
		Audit.resume();
	}
	
	private Strings mediateSingle( String uid, Strings utterance ) {
		audit.in("mediateSingle", "uid="+ uid +", utt="+ utterance );
		Strings reply;
	
		imagined( false );
		Overlay.attach( uid );
		Where.clearLocation();
		Item.resetFormat();
		Repertoires.signs().firstMatch( true );
		
		if (Reply.isUnderstood()) // from previous interpretation!
			Overlay.startTxn(); // all work in this new overlay
		
		Reply r = Repertoires.mediate( new Utterance( utterance ));

		// once processed, keep a copy
		Utterance.previous( utterance );

		if (imagined()) {
			Overlay.abortTxn();
			Repertoires.signs().reset( r.toStrings() );
			
		} else if (Reply.isUnderstood()) {
			Overlay.commitTxn();
			Repertoires.signs().reset( r.toStrings() );
			
		} else {
			// really lost track?
			audit.debug( "utterance is not understood, forgetting to ignore: "
			             +Repertoires.signs().ignore().toString() );
			Repertoires.signs().ignoreNone();
			shell.aloudIs( true ); // sets aloud for whole session if reading from fp
		}

		// auto-unload here - autoloading() in Repertoire.interpret() 
		// asymmetry: load as we go; tidy-up once finished
		Autoload.unloadAged();

		reply = Reply.say().appendAll( r.toStrings());
		Reply.say( null );
		Overlay.detach();
			
		audit.out();
		return reply;
	}
	public Strings mediate( Strings said ) { return mediate( Overlay.DEFAULT_USERID, said );}
	public Strings mediate( String uid, Strings said ) {
		audit.in( "mediate", "uid="+uid+", said="+said );
		Strings reply = new Strings();
		for (Strings conj : Concept.conjuntionAlley( said )) {
			if (!reply.isEmpty()) reply.add( "and" );
			Strings tmp = mediateSingle( uid, conj );
			reply.addAll( tmp );
		}
		audit.out( reply );
		return reply;
	}
	
	/*
	 *  test code....
	 */
	public static void usage() {
		Audit.LOG( "Usage: java [-jar enguage.jar|org.enguage.Enguage]" );
		Audit.LOG( "            --help |" );
		Audit.LOG( "            --verbose --data <path>" ); 
		Audit.LOG( "            --port <port> [--httpd [--server <name>]] |" );
		Audit.LOG( "            --test | [<utterance>]" );
		Audit.LOG( "Options are:" );
		Audit.LOG( "       -h, --help" );
		Audit.LOG( "          displays this message\n" );
		Audit.LOG( "       -v, --verbose\n" );
		Audit.LOG( "       -d, --data <path> specifies the data volume to use\n" );
		Audit.LOG( "       -p, --port <port>" );
		Audit.LOG( "          defines a TCP/IP port number\n" );
		Audit.LOG( "       -H, --httpd" );
		Audit.LOG( "          use webserver protocols\n" );
		Audit.LOG( "       -s, --server <host> <port>" );
		Audit.LOG( "          switch to send speech to a server." );
		Audit.LOG( "          (Needs to be initialised with -p nnnn);\n" );
		Audit.LOG( "       [<utterance>]" );
		Audit.LOG( "          with an utterance it runs one-shot;" );
		Audit.LOG( "          with no utterance it runs as a shell," );
		Audit.LOG( "             requiring full stops (periods) to" );
		Audit.LOG( "             terminate utterances." );
	}
	
	public static void main( String[] args ) {
		
		Strings    cmds = new Strings( args );
		String     cmd;
		String     fsys = RW_SPACE;
		boolean useHttp = false;
		int port = 0;
		String server = "";
		
		// traverse args and strip switches: -v -d -H -p -s
		int i = 0;
		while (i < cmds.size()) {
			
			cmd = cmds.get( i );
			
			if (cmd.equals( "-h" ) || cmd.equals( "--help" )) {
				Enguage.usage();
				System.exit( 0 );
			
			} else if (cmd.equals( "-v" ) || cmd.equals( "--verbose" )) {
				cmds.remove( i );
				verbose = true;
					
			} else if (cmd.equals( "-d" ) || cmd.equals( "--data" )) {
				cmds.remove( i );
				fsys = cmds.isEmpty() ? fsys : cmds.remove( i );
				
			} else if (cmd.equals( "-p" ) || cmd.equals( "--port" )) {
				cmds.remove( i );
				port = cmds.isEmpty() ? 8080 : Integer.parseInt( cmds.remove( i ));
				Audit.LOG( "Using port: "+ port );
		
			} else if (cmd.equals( "-H" ) || cmd.equals( "--httpd" )) {
				cmds.remove( i );
				useHttp = true;

			} else if (cmd.equals( "-s" ) || cmd.equals( "--server" )) {
				cmds.remove( i );
				server = cmds.isEmpty() ? "localhost" : cmds.remove( i );
				Audit.LOG( "Sending to server: "+ server );
				
			} else
				i++;
		}

		enguage = new Enguage( fsys );
				
		cmd = cmds.isEmpty() ? "":cmds.remove( 0 );
		
		if (port != 0 && server.equals( "" )) { // run as local server
			if (useHttp) Server.httpd( ""+ port );
			Server.server( cmds.isEmpty() ? "8080" : cmds.remove( 0 ));
		
		} else if (cmd.equals( "" )) {
			Overlay.attach( "uid" );
			shell.aloudIs( true ).run();
		
		} else {
			// Command line parameters exists...
			// reconstruct original commands
			cmds.prepend( cmd );
			audit.in( "CLI:", ""+cmds );

			// - remove full stop, if one given -
			if (cmds.get( cmds.size()-1 ).equals( "." ))
				cmds.remove( cmds.size()-1 );

			// ...and interpret
			audit.out( Enguage.get().mediate( cmds ));
}	}	}
