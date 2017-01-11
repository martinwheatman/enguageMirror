package com.yagadi.enguage;

import java.util.GregorianCalendar;

import com.yagadi.enguage.concept.Allopoiesis;
import com.yagadi.enguage.concept.Autoload;
import com.yagadi.enguage.concept.Concept;
import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.concept.Signs;
import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.expression.Utterance;
import com.yagadi.enguage.expression.where.Where;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.sofa.Sofa;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Enguage extends Shell {
	static private Audit audit = new Audit( "Enguage" );
	static final public String name = "enguage";
	static final public String configFilename = "config.xml";

	static public boolean silentStartup() { return !Audit.startupDebug; }

	/* This singleton is managed here because the Engine code needs to know about
	 * the state that the e is in, but won't have access to it unless the
	 * calling code sets up that access.
	 */
	static public Enguage e = null; // can't remove this: interpret()
	// overrides the method in Shell
	// TODO: enguage need to CONTAIN a Shell
	static public Overlay o = null; // TODO: ditto

	static public void log( String s ) { audit.log( s ); }

	public Enguage( String location ) {
		super( "Enguage" );
		audit.in( "Engauge", "location=" + location );

		if (!Filesystem.location( location ))
			audit.FATAL( location + ": not found" );

		if (!Overlay.autoAttach())
			audit.FATAL( "Ouch! Cannot autoAttach() to object space" );
		else {
			Variable.encache( o = Overlay.Get() );
			Allopoiesis.spokenInit();
			Repertoire.primeUsedInit();
		}
		Concept.names( location );
		audit.out();
	}

	/*
	* This is separate from the c'tor as it uses itself to 
	* read the config file's txt files.
	*/
	static public void loadConfig( String fname ) {
		audit.in( "loadConfig", fname );
		Audit.allOff();
		if (Audit.startupDebug) Audit.allOn();
		
		long then = new GregorianCalendar().getTimeInMillis();
		Allopoiesis.undoEnabledIs( false );
		
		Tag t = Tag.fromFile( Repertoire.location() + fname );
		if (t != null && (t = t.findByName( "config" )) != null) {
			Config.setContext( t.attributes() );
			Concept.load( t.findByName( "concepts" ) );
		}

		Allopoiesis.undoEnabledIs( true );
		long now = new GregorianCalendar().getTimeInMillis();
		
		audit.log( "Initialisation in: " + (now - then) + "ms" );
		audit.log( Signs.stats() );

		Audit.allOff();
		if (Audit.runtimeDebug) Audit.allOn();
		audit.out();
	}

	@Override
	public String interpret( Strings utterance ) {
		audit.in( "interpret", utterance.toString() );
		if (!audit.tracing && !Audit.allTracing) audit.log( utterance.toString( Strings.SPACED ) );

		if (Reply.understood()) // from previous interpretation!
			o.startTxn( Allopoiesis.undoIsEnabled() ); // all work in this new overlay

		Reply r = Repertoire.interpret( new Utterance( utterance ) );

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
		if (!Repertoire.isInitialising() && !Autoload.ing()) Autoload.unload();

		return audit.out( reply );
	}

	// ==== test code =====
	private static void TestInit() {
		if (Config.firstRun() || !Config.visualMode()) {
			Config.firstRun( false );
			audit.log(
				Config.welcome(
					e.copyright() +
						"\nEnguage main(): overlay is: " + Overlay.Get().toString()
			)	);
		}
		Config.directionToSpeak( "press the button and speak" );
		Config.helpOnHelp( "just say help" );
	}

	private static void testInterpret( String cmd, String expected ) {
		String answer = e.interpret( new Strings( cmd ));
		if (!Reply.understood() && !Repertoire.prompt().equals( "" ))
			audit.log( "Hint is:" + Repertoire.prompt() );
		else if ( !expected.equals( "" ) && !new Strings( answer ).equalsIgnoreCase( new Strings( expected )))
			audit.FATAL("reply:"+ answer +",\n    expected:"+ expected );
		else
			audit.log( answer );
	}
	private static void testInterpret( String cmd ) { testInterpret( cmd, "" );}
	
	public static void main( String args[] ) {
		
		e = new Enguage( "./src/assets" );
		
		loadConfig( configFilename );
		TestInit();

		if ( args.length == 1 && args[ 0 ].equals( "run" )) {
			audit.title( "Over to you..." );
			e.aloudIs( true );
			e.run();
		} else {

			//Repertoire.signs.show();
			//testInterpret( "detail on" );
			//testInterpret( "tracing on" );
	
			int level = 9;
	
			if ( level == 0 || level == 1 ) {
				audit.title( "The Non-Computable concept of NEED" );
				testInterpret( "i don't need anything",
						       "ok, you don't need anything." );
				testInterpret( "i need a cup of coffee and a biscuit",
						       "ok, you need a cup of coffee, and a biscuit.");
				testInterpret( "what do i need",
						       "you need a cup of coffee, and a biscuit.");
				testInterpret( "i don't need any coffee",
						       "ok, you don't need any coffee." );
				testInterpret( "what do i need",
						       "you need a biscuit." );
				testInterpret( "i don't need anything",
						       "ok, you don't need anything."  );
			}
	
			if ( level == 0 || level == 2 ) {
				audit.title( "Semantic Thrust" );
				testInterpret( "i don't need anything",
						       "ok, you don't need anything." );
				testInterpret( "i need a cup of coffee",
						       "ok, you need a cup of coffee." );
				testInterpret( "i need to go to town",
							   "ok, you need to go to town." );
				testInterpret( "what do i need",
							   "you need a cup of coffee, and to go to town." );
				testInterpret( "i have a coffee",
						       "ok, you don't need a coffee." );
				testInterpret( "i have to go to town",
						       "I know." );
				testInterpret( "i don't need to go to town",
						       "ok, you don't need to go to town." );
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
				testInterpret( "i need a cup of tea",
						       "ok, you need a cup of tea." );
				testInterpret( "and another coffee",
					           "ok, you need 1 more coffee." );
				testInterpret( "what do i need",
						       "You need 3 coffees , and a cup of tea." );
				testInterpret( "i don't need anything",
						       "ok, you don't need anything." ); // ok??
			}
	
			if ( level == 0 || level == 5 ) {
				audit.title( "Correction" );
				testInterpret( "i need a coffee",
						       "ok, you need a coffee.");
				testInterpret( "and another",
						       "ok, you need 1 more coffee.");
				testInterpret( "no i need another 3",
						       "ok, you need 3 more coffees.");
				testInterpret( "what do i need",
						       "you need 4 coffees.");
				testInterpret( "i don't need anything",
					           "ok, you don't need anything." );
			}
	
			if ( level == 0 || level == 6 ) {
				audit.title( "Disambiguation" );
				testInterpret( "the eagle has landed" //,
						       //"Are you an ornitholgist."
						);
				testInterpret( "no the eagle has landed" //,
						       //"So , you're talking about the novel."
						);
				testInterpret( "no the eagle has landed" //, 
						       //"So you're talking about apollo 11."
						);
				testInterpret( "no the eagle has landed" //,
						       //"I don't understand"
						);
			}
	
			if ( level == 0 || level == 7 ) {
				audit.title( "Temporal interpret" );
				testInterpret( "what day is christmas day" );
				testInterpret( "what day is it today" );
			}
	
			if ( level == 0 || level == 8 ) {
				audit.title( "Langauge Learning (non-autopoietic)" );
				testInterpret( "I want a Ferrari", "I don't understand" );
				testInterpret( "want means need", "ok." );
				testInterpret( "I want a Ferrari", "ok, you want a ferrari.");
				testInterpret( "I don't need anything", "Ok , you don't want anything." );
				//audit.title( "Misunderstanding" );
				//testInterpret( "i don't understand" );
			}
	
			if ( level == 0 || level == 9 ) {
				audit.title( "Temporospatial concept MEETING" );
	
				Where.doLocators();
				new Sofa().interpret( new Strings( "entity create pub" ));
	
				testInterpret( "I'm not meeting anybody",
						"Ok , you're not meeting anybody." );
				testInterpret( "At 7 I'm meeting my brother at the pub",
						"Ok , you're meeting your brother at 7 at the pub." );
				testInterpret( "tracing on" );
				testInterpret( "When  am I meeting my brother",
						"You're meeting your brother at 7." );
				testInterpret( "Where am I meeting my brother",
						"You're meeting your brother at the pub." );
				testInterpret( "Am I meeting my brother",
						"Yes , you're meeting your brother." );
				
				//testInterpret( "I'm meeting my sister at the pub" );
				//testInterpret( "When am I meeting my sister" );
				
				//testInterpret( "tracing on" );
				//testInterpret( "When am I meeting my dad" );
			}
			
			//testInterpret( "i don't need anything" );
			audit.log( "PASSED" );
		}
	}
}