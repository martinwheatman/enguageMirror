package com.yagadi.enguage.concept;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.concept.Autopoiesis;
import com.yagadi.enguage.concept.Signs;
import com.yagadi.enguage.concept.Allopoiesis;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.expression.Utterance;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Repertoire {
	static private Audit audit = new Audit( "Repertoire" );

	        static final String        PREFIX = Reply.helpPrefix();
	public  static final String PRONUNCIATION = "repper-to-are";  // better than  ~wah	
	public  static final String PLURALISATION = "repper-to-wahs"; // better than ~ares
	public  static final String          NAME = "repertoire";

	/* This class maintains three repertoire groups - signs, autop and engine
	 * Each, well signs, contains signs from all runtime loaded repertoires and
	 * all autoloaded repertoires. Perhaps runtime loaded repertoires could go 
	 * in engine?
	 */
	static public Signs signs = new Signs( "users" );
	static public Signs autop = new Signs( "autop", Autopoiesis.autopoiesis );
	static public Signs allop = new Signs( "engin", Allopoiesis.commands );
	
	// ----- read concepts used in main e
	static private boolean initialising = false;
	static public  boolean isInitialising() { return initialising; }
	static public  boolean initialisingIs( boolean b ) { return initialising = b; }

	static public  final String  DEFAULT_PRIME = "need";
	static public  final String       NO_PRIME = "";
	
	static private       String prime = NO_PRIME;
	static public        void   prime( String name ) { prime = name; }
	static public        String prime() { return prime; }
	
	static private       String prompt = "";
	static public        void   prompt( String s ) { prompt = s; }
	static public        String prompt() { return prompt; }
	
	// record whether the user has figured it out...
	// run in conjunction with the main intepreter and the app...
	static private final String  primeUsedVar = "PRIME_USED";
	static private       boolean primeUsed    = false;
	static public        boolean primeUsed() { return primeUsed; }
	static public        void    primeUsed( boolean spk ) {
		if (!Repertoire.isInitialising() && !Autoload.ing() && spk != primeUsed) {
			Variable.set( primeUsedVar, spk ? Shell.SUCCESS : Shell.FAIL );
			primeUsed = spk;
	}	}
	static public  void    primeUsedInit() { // called after encaching vars
		primeUsed = Variable.get( primeUsedVar, Shell.FAIL ).equals( Shell.SUCCESS );
	}
	
	static private boolean loadedDefaultConcept = false;
	static public  boolean defaultConceptIsLoaded() { return loadedDefaultConcept; }
	static private void defaultConceptLoadedIs(boolean c ) { loadedDefaultConcept = c; }

	static public String location() {
		return (
			Filesystem.location().equals("") ?
 				Filesystem.root + File.separator+ "yagadi.com" :
 				Filesystem.location()
 			) + File.separator;
	}
	
	static public boolean loadSigns( String name ) { // to Repertoires
		boolean wasLoaded = false;
		
		Autopoiesis.concept( name );
		if (name.equals( DEFAULT_PRIME ))
			defaultConceptLoadedIs( true );
		
		//...silence on inner thought
		boolean wasSilenced = false;
		boolean wasAloud = Enguage.e.isAloud();
		if (!Audit.startupDebug && Enguage.silentStartup()) {
			wasSilenced = true;
			Audit.suspend(); // <<<<<<<<< miss this out for debugging
			Enguage.e.aloudIs( false );
		}
		
		// ...add content from file
		String aname = name +".txt",
		       fname = location() + aname;
		try {
			FileInputStream fis = new FileInputStream( fname );
			Enguage.e.interpret( fis );
			fis.close();
			wasLoaded = true; 
		} catch (IOException e1) {}
		
		//...un-silence after inner thought
		if (!Audit.startupDebug && wasSilenced) {
			Audit.resume();
			Enguage.e.aloudIs( wasAloud );
		}
		
		return wasLoaded;
	}

	//
	// Repertoire Management -- above
	// *********************************************************** 

	// entry point for Enguage, re-entry point for Intention
	static public Reply interpret( Utterance u ) {
		Reply r = new Reply();
		if (Utterance.sane( u )) {
			audit.in( "interpret", u.toString( Strings.SPACED ));
			if (isInitialising() || Autoload.ing()) {
				// check through autop first, at startup
				r = autop.interpret( u );
				if (Reply.DNU == r.type()) {
					r = signs.interpret( u );
					if (Reply.DNU == r.type())
						r = allop.interpret( u );
				}
			} else {
				// during runtime, do user signs first
				// unloaded up in enguage.interpret()
				Autoload.load( u.expanded() );// ...autoload from expanded utterance...
				r = signs.interpret( u );
				if (Reply.DNU == r.type()) {
					r = allop.interpret( u ); // ...then e signs
					if (Reply.DNU == r.type())
						r = autop.interpret( u ); // ...just in case
			}	}
			audit.out( r.toString() );
		}
		return r;
}	}

