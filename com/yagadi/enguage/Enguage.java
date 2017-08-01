package com.yagadi.enguage;

import com.yagadi.enguage.interpretant.Allopoiesis;
import com.yagadi.enguage.interpretant.Autoload;
import com.yagadi.enguage.interpretant.Concepts;
import com.yagadi.enguage.interpretant.Net;
import com.yagadi.enguage.interpretant.Repertoire;
import com.yagadi.enguage.object.Overlay;
import com.yagadi.enguage.object.Sofa;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;
import com.yagadi.enguage.vehicle.where.Where;

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
	private static void testInterpret( String cmds, String expects ) {
		
		Strings cmdlist = new Strings( cmds, '/' );
		Strings explist = new Strings( expects, '/' );
		
		while (cmdlist.size() > 0) {
			// assume 1 expected OR cmds >= expecteds...
			String expected = cmdlist.size() == explist.size() ? explist.remove( 0 ) : "";
			String cmd = cmdlist.remove( 0 );
			
			if (expected != null)
				audit.log( "enguage> "+ cmd );
				
			String answer = Enguage.interpret( cmd );
			
			if (expected != null)
				if (!Reply.understood() && !Repertoire.prompt().equals( "" ))
					audit.log( "Hint is:" + Repertoire.prompt() );
				else if (   !expected.equals( "" )
				         && !new Strings( answer )
				         		.equalsIgnoreCase( new Strings( expected )))
					audit.FATAL("reply:"+ answer +",\n    expected:"+ expected );
				else
					audit.log( answer +"\n" );
		}
	}
	private static void testInterpret( String cmd ) { testInterpret( cmd, "" );}
	private static void usage() {
		audit.LOG( "Usage: java -jar enguage.jar [-c <configDir>] [-p <port> | -s | -t ]" );
		audit.LOG( "where: default config dir=\".src/assets\"" );
		audit.LOG( "     : -p <port> listens on a TCP/IP port" );
		audit.LOG( "     : -s runs Engauge as a shell" );
		audit.LOG( "     : -t runs a test sanity check" );
	}
	public static void main( String args[] ) {
		
		if (args.length == 0)
			usage();
		else {
			
			int argc = 0;
			
			if (args.length > 1 && args[ argc ].equals( "-c" )) {
				argc++;
				Enguage.loadConfig( args[ argc++ ]);
			} else
				Enguage.loadConfig( "./src/assets" );
			
			if ( args.length == argc + 1 && args[ argc ].equals( "-s" ))
				e.aloudIs( true ).run();				
			else if (args.length == argc + 2 && args[ argc ].equals( "-p" ))
				Net.server( args[ ++argc ]);
			else if (args.length == argc + 1 && args[ argc ].equals( "-t" ))
				sanityCheck();
			else
				usage();
	}	}
	private static void sanityCheck() {
		// useful ephemera
		//Repertoire.signs.show();
		//testInterpret( "detail on" );
		//testInterpret( "tracing on" );

		int level = 0;

		if ( level == 0 || level == 1 ) {
			audit.title( "The Non-Computable concept of NEED" );
			
			// silently clear the decks
			testInterpret( "i don't need anything", null );

			testInterpret( "what do i need/i need 2 cups of coffee and a biscuit",
						   "you don't need anything./ok, you need 2 cups of coffee, and a biscuit." );
			//testInterpret( "i need 2 cups of coffee and a biscuit",
			//			   "ok, you need 2 cups of coffee, and a biscuit.");
			testInterpret( "what do i need",
						   "you need 2 cups of coffee, and a biscuit.");
			testInterpret( "how many coffees do i need",
						   "2, you need 2 coffees." );
			testInterpret( "i don't need any coffee",
						   "ok, you don't need any coffee." );
			testInterpret( "what do i need",
						   "you need a biscuit." );

			audit.title( "Semantic Thrust" );
			testInterpret( "i need to go to town",
						   "ok, you need to go to town." );
			testInterpret( "what do i need",
						   "you need a biscuit, and to go to town." );
			testInterpret( "i have a biscuit",
						   "ok, you don't need a biscuit." );
			testInterpret( "i have to go to town",
						   "I know." );
			testInterpret( "i don't need to go to town",
						   "ok, you don't need to go to town." );
			testInterpret( "I have everything",
					       "ok, you don't need anything." );
			testInterpret( "what do i need",
						   "you don't need anything." );
		}

		if ( level == 0 || level == 3 ) {
			audit.title( "Verbal Arithmetical" );
			testInterpret( "what is 1 + 2",
						   "1 plus 2 is 3.");
			testInterpret( "times 2 all squared",
						   "times 2 all squared makes 36.");
			testInterpret( "what is 36 + 4 all divided by 2",
						   "36 plus 4 all divided by 2 is 20." ); 
		}

		if ( level == 0 || level == 4 ) {
			audit.title( "Numerical Context" );
			testInterpret( "i need a coffee",
						   "ok, you need a coffee." );
			testInterpret( "and another",
						   "ok, you need 1 more coffee." );
			testInterpret( "how many coffees do i need",
						   "2, you need 2 coffees." );
			testInterpret( "i need a cup of tea",
						   "ok, you need a cup of tea." );
			testInterpret( "and another coffee",
						   "ok, you need 1 more coffee." );
			testInterpret( "what do i need",
						   "You need 3 coffees , and a cup of tea." );
		}

		if ( level == 0 || level == 5 ) {
			audit.title( "Correction" );
			testInterpret( "i need another coffee",
						   "ok, you need 1 more coffee.");
			testInterpret( "no i need another 3",
						   "ok, you need 3 more coffees.");
			testInterpret( "what do i need",
						   "you need 6 coffees, and a cup of tea.");
			testInterpret( "i don't need anything",
						   "ok, you don't need anything." );
		}

		if ( level == 0 || level == 6 ) {
			audit.title( "Disambiguation" );
			testInterpret( "the eagle has landed" //,
						   //"Are you an ornithologist."
					);
			testInterpret( "no the eagle has landed" //,
						   //"So , you're talking about the novel."
					);
			testInterpret( "no the eagle has landed" //, 
						   //"So you're talking about Apollo 11."
					);
			testInterpret( "no the eagle has landed" //,
						   //"I don't understand"
					);
		}

		if ( level == 0 || level == 7 ) {
			audit.title( "Temporal interpret" );
			testInterpret( "what day is christmas day" );
			//testInterpret( "what day is it today" );
		}

		if ( level == 0 || level == 8 ) {
			audit.title( "Temporospatial concept MEETING" );

			Where.doLocators();
			new Sofa().interpret( new Strings( "entity create pub" ));

			testInterpret( "I'm not meeting anybody",
					"Ok , you're not meeting anybody." );
			testInterpret( "At 7 I'm meeting my brother at the pub",
					"Ok , you're meeting your brother at 7 at the pub." );
			testInterpret( "When  am I meeting my brother",
					"You're meeting your brother at 7." );
			testInterpret( "Where am I meeting my brother",
					"You're meeting your brother at the pub." );
			testInterpret( "Am I meeting my brother",
					"Yes , you're meeting your brother." );
			
			//testInterpret( "tracing on" );
			testInterpret( "I'm meeting my sister at the pub" );
			testInterpret( "When am I meeting my sister",
					"I don't know when you're meeting your sister." );
			
			//testInterpret( "tracing on" );
			testInterpret( "When am I meeting my dad",
					"i don't know if you're meeting your dad." );
			testInterpret( "Where am I meeting my dad" ,
					"i don't know if you're meeting your dad." );
		}
		
		if (level == 0 || level == 9) {
			testInterpret( "tcpip localhost 999 \"999 is a test value for port address\"",   "ok." );
			testInterpret( "tcpip localhost 5678 \"this is a test, which will fail\"",    "Sorry." );
		}
		
		if (level == 0 || level == 10) {
			audit.title( "On-the-fly Langauge Learning" );
			testInterpret( "my name is martin", "I don't understand." );
			Variable.unset( "NAME" );
			
			testInterpret( "to i am called phrase variable name reply hi whatever", "go on." );
			testInterpret( "this implies name gets set to variable name", "go on." );
			testInterpret( "this implies name is not set to variable name if not reply i know" );
			testInterpret( "ok", "ok." );
			
			
			testInterpret( "interpret my name is phrase variable name thus", "go on." );
			testInterpret( "first set name to variable name", "go on." );
			testInterpret( "then get the value of name", "go on." ); // not strictly necessary!
			testInterpret( "then reply hello whatever" );
			testInterpret( "and that is it", "ok." );

			// could this be built thus?
			//testInterpret( "just call me phrase variable name means i am called variable name" );
			
			// testInterpret( "" );
			Repertoire.signs.show( "OTF" );
			
			testInterpret( "my name is ruth",    "hello ruth." );
			testInterpret( "my name is martin",  "hello martin." );
			testInterpret( "i am called martin", "i know." );
		}
		audit.log( "PASSED" );
}	}