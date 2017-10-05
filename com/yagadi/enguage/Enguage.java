package com.yagadi.enguage;

import com.yagadi.enguage.interpretant.Allopoiesis;
import com.yagadi.enguage.interpretant.Autoload;
import com.yagadi.enguage.interpretant.Concepts;
import com.yagadi.enguage.interpretant.Repertoire;
import com.yagadi.enguage.object.Overlay;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Net;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Question;
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
	private static void testInterpret( String cmd, String expected ) {
		if (expected != null)
			audit.log( "enguage> "+ cmd );
			
		String answer = Enguage.interpret( cmd );
		
		if (expected != null) {
			int len = expected.length();
			if (len > 0 && expected.charAt( len - 1 ) != '.') expected += ".";
			if (!Reply.understood() && !Repertoire.prompt().equals( "" ))
				audit.log( "Hint is:" + Repertoire.prompt() );
			else if (   !expected.equals( "" )
			         && !new Strings( answer )
			         		.equalsIgnoreCase( new Strings( expected )))
				audit.FATAL("reply:"+ answer +",\n    expected:"+ expected );
			else
				audit.log( answer +"\n" );
	}	}
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
			Question.primedAnswer( "yes" );
			testInterpret( "i don't need anything", null );

			testInterpret( "what do i need",
						   "you don't need anything." );
			testInterpret( "i need 2 cups of coffee and a biscuit",
						   "ok, you need 2 cups of coffee, and a biscuit.");
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
			
			Question.primedAnswer( "yes" );
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
			
			/* Ideally, we want:
			 * - the factorial of 1 is 1;
			 * - the factorial of n is n times the factorial of n - 1;
			 * - what is the factorial of 3.
			 */
			testInterpret( "to the phrase what is the factorial of 1 reply 1", "go on." );
			testInterpret( "ok", "ok." );
			testInterpret( "what is the factorial of 1",  "1." );
			
			testInterpret( "interpret multiply numeric phrase variable a by numeric phrase variable b thus", "go on." );
			testInterpret( "first perform numeric evaluate variable a times numeric phrase variable b",      "go on." );
			testInterpret( "ok", "ok." );
			
			testInterpret( "multiply 2 by 3", "6." );
			
			testInterpret( "interpret subtract numeric phrase variable c from numeric phrase variable d thus", "go on." );
			testInterpret( "first perform numeric evaluate variable d - variable c",                           "go on." );
			testInterpret( "ok", "ok." );
			
			testInterpret( "subtract 2 from 3", "1." );
			
			testInterpret( "interpret what is the factorial of numeric phrase variable n thus", "go on." );
			testInterpret( "first subtract 1 from variable n",                                  "go on." );
			testInterpret( "then what is the factorial of whatever",                            "go on." );
			testInterpret( "then multiply whatever by variable n",                              "go on." );
			testInterpret( "then reply whatever the factorial of variable n is whatever",       "go on." );
			testInterpret( "ok", "ok." );
			
			//Repertoire.signs.show( "OTF" );
			//testInterpret("tracing on");
			testInterpret( "what is the factorial of 4",  "24 the factorial of 4 is 24." );
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
			Question.primedAnswer( "yes" );
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
			// Issue here: on DNU, we need to advance this on "the eagle has landed"
			// i.e. w/o "no ..."
		}
		if ( level == 0 || level == 7 ) {
			audit.title( "Temporal interpret" );
			testInterpret( "what day is christmas day" );
			//testInterpret( "what day is it today" );

			audit.title( "Temporospatial concept MEETING" );
			
			//Where.locatorIs( "at" ); is the same as...
			// new Sofa().interpret( new Strings( "spatial locator at" )); is the same as...
			testInterpret( "interpret spatially something can be phrase variable locator thus", "go on." );
			testInterpret( "first perform spatial locator variable locator", "go on." );
			testInterpret( "ok", "ok." );
			
			testInterpret( "spatially something can be to the left of",  "ok." );
			testInterpret( "spatially something can be to the right of", "ok." );
			testInterpret( "spatially something can be in front of",     "ok." );
			testInterpret( "spatially something can be on top of",       "ok." );
			testInterpret( "spatially something can be behind",          "ok." );
			testInterpret( "spatially something can be in",              "ok." );
			testInterpret( "spatially something can be on",              "ok." );
			testInterpret( "spatially something can be under",           "ok." );
			testInterpret( "spatially something can be underneath",      "ok." );
			testInterpret( "spatially something can be over",            "ok." );
			testInterpret( "spatially something can be at",              "ok." );
			
			/* TODO: interpret think of a variable entity thus.  // see sofa for particular details!
			 * first create a class variable entity.             // mkdir pub; touch pub/isa 
			 * then  create an anonymous entity variable entity. // mkdir pub/a
			 * then  set the context of the variable entity to a variable entity // ln -s pub/the pub/a
			 * ok.
			 * new Sofa().interpret( new Strings( "entity create pub" ));
			 */
			// Creating a pub not needed
			//new Sofa().interpret( new Strings( "entity create pub" ));

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
			
			/* TODO:
			 *  create a queen called elizabeth the first  (eliz = woman's name, a queen is a monarch => person)
			 *  she died in 1603
			 * she reigned for 45 years (so she ascended/came to the throne in 1548!)
			 */
		}
		if (level == 0 || level == 8) {
			testInterpret( "tcpip localhost 999 \"999 is a test value for port address\"",   "ok." );
			testInterpret( "tcpip localhost 5678 \"this is a test, which will fail\"",    "Sorry." );
		}
		if (level == 0 || level == 9) {
			audit.title( "On-the-fly Langauge Learning" );
			/* TODO: create filename from pattern:
			 *    "i need phrase variable objects" => i_need-.txt (append? create overlay)
			 *    "this is part of the need concept" => need.txt (append)
			 *    Enguage.interpret() => overlay
			 *    Conceept.load() => can this outlive Enguage overlay???
			 */
			
			// First, what we can't say yet...
			testInterpret( "my name is martin",                 "I don't understand." );
			testInterpret( "if not  reply i already know this", "I don't understand." );
			testInterpret( "unset the value of name",           "ok." );
			
			// build-a-program...
			testInterpret( "interpret my name is phrase variable name thus", "go on." );
			testInterpret( "first set name to variable name",                "go on." );
			testInterpret( "then get the value of name",                     "go on." ); // not strictly necessary!
			testInterpret( "then reply hello whatever",                      "go on." );
			testInterpret( "and that is it",                                 "ok."    );

			testInterpret( "my name is ruth",    "hello ruth." );
			testInterpret( "my name is martin",  "hello martin." );
			
			//...or to put it another way
			testInterpret( "to the phrase i am called phrase variable name reply hi whatever", "go on." );
			testInterpret( "this implies name gets set to variable name", "go on." );
			testInterpret( "this implies name is not set to variable name", "go on." );
			testInterpret( "if not reply i already know this", "go on." );
			testInterpret( "ok", "ok." );
			
			testInterpret( "i am called martin", "i already know this." );
			
			// ...means.../...the means to...
			// 1. from the-means-to repertoire
			testInterpret( "to the phrase phrase variable x the means to phrase variable y reply i really do not understand", "go on." );
			testInterpret( "that is it", "ok." );

			testInterpret( "do we have the means to become rich", "I really don't understand." );

			// 2. could this be built thus?
			testInterpret( "to phrase variable this means phrase variable that reply ok", "go on." );
			testInterpret( "this implies perform sign think variable that",               "go on." );
			testInterpret( "this implies perform sign create variable this",              "go on." );
			testInterpret( "ok", "ok." );
			
			testInterpret( "just call me phrase variable name means i am called variable name", "ok." );
			testInterpret( "just call me martin", "i already know this." );
		}
		if ( level == 0 || level == 10 ) {
			audit.title( "Ask: Confirmation" );
			
			Question.primedAnswer( "yes" );
			testInterpret( "i have everything", "ok , you don't need anything." );
			
			Question.primedAnswer( "no" );
			testInterpret( "i have everything", "ok , let us leave things as they are." );

			Question.primedAnswer( "i do not understand" );
			testInterpret( "i have everything", "Ok , let us leave things as they are." );
			
			/* TODO:
			 * To the phrase: i am p v name       => set user name NAME
			 *                my name is p v name => set user name NAME
			 *                p v name            => set user name NAME
			 * Ask: what is your name?
			 */
		}
		audit.log( "PASSED" );
}	}