package org.enguage.sgn.rep;

import org.enguage.obj.Variable;
import org.enguage.sgn.Signs;
import org.enguage.util.Shell;
import org.enguage.veh.Utterance;
import org.enguage.veh.reply.Reply;

public class Repertoire {
	//static private Audit audit = new Audit( "Repertoire" );

	public  static final String        PREFIX = Reply.helpPrefix();
	public  static final String PRONUNCIATION = "repper-to-are";  // better than  ~wah	
	public  static final String PLURALISATION = "repper-to-wahs"; // better than ~ares
	public  static final String          NAME = "repertoire";

	public  static final String         ALLOP = "engine";
	public  static final String         AUTOP = "autopoiesis";
	public  static final String   AUTOPOIETIC = "OTF"; // repertoire name for signs created on-the-fly
	// TODO: create a method to comb users for signs created on-the-fly, 
	//       and to save them under (append them to) a concept file
	// with the name of the value of the pattern?  Autoload() needs to load
	// these pattern files. Theses need to be sought first(?):
	// i_need_X.txt is searched before need+needs.txt, because is it the users /will/
	// n.B. X_means_X.txt - X is just a placeholder.
	
	/* This class maintains three repertoire groups - signs, autop and allopoetic
	 * Each, well signs, contains signs from all runtime loaded repertoires and
	 * all autoloaded repertoires. Perhaps runtime loaded repertoires could go 
	 * in engine?
	 */
	static public Signs signs = new Signs("user" );
	static public Signs autop = new Signs("autop" ).add( Autopoietic.written ).add( Autopoietic.spoken );
	static public Signs allop = new Signs("allop" ).add( Engine.commands );
	
	// ----- read concepts used in main e
	//static private boolean initialising = false;
	private final static String FALSE = Boolean.toString( false );
	private final static String  TRUE = Boolean.toString( true  );
	static Variable induction = new Variable( "induction", FALSE );
	static public  boolean isInducting() {
		return induction.get().equalsIgnoreCase( TRUE );
	}
	static public  boolean inductingIs( boolean b ) {
		induction.set( b ? TRUE : FALSE );
		return b;
	}

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
		if (!Repertoire.isInducting() && !Autoload.ing() && spk != primeUsed) {
			Variable.set( primeUsedVar, spk ? Shell.SUCCESS : Shell.FAIL );
			primeUsed = spk;
	}	}
	static public  void    primeUsedInit() { // called after encaching vars
		primeUsed = Variable.get( primeUsedVar, Shell.FAIL ).equals( Shell.SUCCESS );
	}
	
	static private boolean loadedDefaultConcept = false;
	static public  boolean defaultConceptIsLoaded() { return loadedDefaultConcept; }
	static public  void    defaultConceptLoadedIs(boolean c ) { loadedDefaultConcept = c; }

	//
	// Repertoire Management -- above
	// *********************************************************** 

	// entry point for Enguage, re-entry point for Intention
	static public Reply interpret( Utterance u ) {
		// Ordering of repertoire:
		// 1. check through autop first, at startup
		// 2. during runtime, do user signs first
		Reply r = new Reply();
		if (isInducting() || Autoload.ing()) {
			r = autop.interpret( u );
			if (Reply.DNU == r.type()) {
				r = signs.interpret( u );
				if (Reply.DNU == r.type())
					r = allop.interpret( u );
			}
		} else {
			Autoload.load( u.expanded() ); // unloaded up in Enguage.interpret()
			r = signs.interpret( u );
			if (Reply.DNU == r.type()) {
				r = allop.interpret( u );
				if (Reply.DNU == r.type())
					r = autop.interpret( u );
		}	}
		return r;
}	}