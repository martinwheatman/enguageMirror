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

	// ==== interpret code =====
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

	private static void Title( String title ) {
		String underline = "";
		audit.log( "\n" );
		audit.log( title );
		for (int i = 0; i < title.length(); i++) underline += "=";
		audit.log( underline );
	}

	private static void testInterpret( String cmd, String expected ) {
		String answer = e.interpret( new Strings( cmd ));
		if (!Reply.understood() && !Repertoire.prompt().equals( "" ))
			audit.log( "Hint is:" + Repertoire.prompt() );
		else if ( !expected.equals( "" ) && !new Strings( answer ).equals( new Strings( expected )))
			audit.FATAL("reply:"+ answer +", isn't expected:"+ expected );
		else
			audit.log( answer );
	}
	private static void interpret( String cmd ) { testInterpret( cmd, "" );}
	
	public static void main( String args[] ) {
		
		e = new Enguage( "./src/assets" );
		
		loadConfig( configFilename );
		TestInit();

		//Repertoire.signs.show();
		interpret( "tracing on" );
		//interpret( "detail on" );

		int level = 9;

		if ( level == 0 || level == 1 ) {
			Title( "The Non-Computable concept of NEED" );
			interpret( "i don't need anything");
			interpret( "i need a cup of coffee and a biscuit" );
			interpret( "what do i need" );
			interpret( "i don't need any coffee" );
			interpret( "what do i need" );
			interpret( "i don't need anything" );
		}

		if ( level == 0 || level == 2 ) {
			Title( "Semantic Thrust" );
			interpret( "i need a cup of coffee" );
			interpret( "i need to go to town" );
			interpret( "what do i need" );
			interpret( "i have a coffee" );
			interpret( "i have to go to town" );
			interpret( "i don't need to go to town" );
			interpret( "what do i need" );
		}

		if ( level == 0 || level == 3 ) {
			Title( "Verbal Arithmetical" );
			interpret( "what is 1 + 2" );
			interpret( "times 2 all squared" );
			interpret( "what is 36 + 4 all divided by 2" ); // plus 4 all / 2
		}

		if ( level == 0 || level == 4 ) {
			Title( "Numerical Context" );
			interpret( "i need a coffee" );
			interpret( "and another" );
			interpret( "i need a cup of tea" );
			interpret( "and another coffee" );
			interpret( "what do i need" );
			interpret( "i don't need anything" );
		}

		if ( level == 0 || level == 5 ) {
			Title( "Correction" );
			interpret( "i need a coffee" );
			interpret( "and another" );
			interpret( "no i need another 3" );
			interpret( "what do i need" );
			interpret( "i don't need anything" );
		}

		if ( level == 0 || level == 6 ) {
			Title( "Disambiguation" );
			interpret( "the eagle has landed" );
			interpret( "no the eagle has landed" );
			interpret( "no the eagle has landed" );
			interpret( "no the eagle has landed" );
			interpret( "no the eagle has landed" );
			interpret( "I don't need anything" );
		}

		if ( level == 0 || level == 7 ) {
			Title( "Temporal interpret" );
			interpret( "what day is christmas day" );
			interpret( "what day is it today" );
		}

		if ( level == 0 || level == 8 ) {
			Title( "Langauge Learning (non-autopoietic)" );
			interpret( "I want a Ferrari" );
			interpret( "want means need" );
			interpret( "I want a Ferrari" );
			//Title( "Misunderstanding" );
			//interpret( "i don't understand" );
		}

		if ( level == 0 || level == 9 ) {
			Title( "Temporospatial concept MEETING" );

			Where.doLocators();
			new Sofa().interpret( new Strings( "entity create pub" ));

			testInterpret( "I'm not meeting anybody", "Ok , you're not meeting anybody." );
			testInterpret( "At 7 I'm meeting my brother at the pub", "Ok , you're meeting your brother at 7 at the pub." );
			testInterpret( "When  am I meeting my brother", "You're meeting your brother at 7." );
			testInterpret( "Where am I meeting my brother", "You're meeting your brother at the pub." );
			testInterpret( "Am I meeting my brother", "Yes , you're meeting your brother." );
		}

		if ( level == 0 ) {
			Title( "Over to you..." );
			interpret( "i don't need anything" );
			e.aloudIs( true );
			e.run();
}  }  }
