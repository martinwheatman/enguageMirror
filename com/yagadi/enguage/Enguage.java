package com.yagadi.enguage;

import com.yagadi.enguage.interpretant.Allopoiesis;
import com.yagadi.enguage.interpretant.repertoire.Autoload;
import com.yagadi.enguage.interpretant.repertoire.Concepts;
import com.yagadi.enguage.interpretant.repertoire.Repertoire;
import com.yagadi.enguage.object.Overlay;
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
			Allopoiesis.spokenInit();
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
			o.startTxn( Allopoiesis.undoIsEnabled() ); // all work in this new overlay

		Reply r = Repertoire.interpret( new Utterance( utterance ));

		// once processed, keep a copy
		Utterance.previous( utterance );

		String reply = r.toString( utterance );
		if (Reply.understood()) {
			o.finishTxn( Allopoiesis.undoIsEnabled() );
			Allopoiesis.disambOff();
			Allopoiesis.spoken( true );
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
		audit.LOG( "Usage: java -jar enguage.jar [-c <configDir>] [-p <port> | -s | -t ]" );
		audit.LOG( "where: default config dir=\".src/assets\"" );
		audit.LOG( "     : -p <port> listens on a TCP/IP port" );
		audit.LOG( "     : -s runs Engauge as a shell" );
		//audit.LOG( "     : -t runs a test sanity check" );
	}
	public static void main( String args[] ) {
		if (args.length == 0)
			usage();
		else {
			
			int argc = 0;
			String location = "./src/assets";
			if (args.length > 1 && args[ argc ].equals( "-c" )) {
				argc++;
				location = args[ argc++ ];
			}
			Enguage.loadConfig( location );
			
			if ( args.length == argc + 1 && args[ argc ].equals( "-s" ))
				e.aloudIs( true ).run();				
			else if (args.length == argc + 2 && args[ argc ].equals( "-p" ))
				Net.server( args[ ++argc ]);
			//else if (args.length == argc + 1 && args[ argc ].equals( "-t" ))
			//	sanityCheck();
			else
				usage();
}	}	}