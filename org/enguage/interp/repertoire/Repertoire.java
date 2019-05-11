package org.enguage.interp.repertoire;

import org.enguage.interp.sign.Signs;
import org.enguage.objects.Variable;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.reply.Reply;

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
	static public Signs signs = new Signs( "user"  );
	static public Signs autop = new Signs( "autop" ).add( Autopoiesis.written )
			                                        .add( Autopoiesis.spoken );
	static public Signs allop = new Signs( "allop" ).add( Engine.commands );
	
	/* A persistent Induction is used in the repertoire.
	 */
	private final static String FALSE = Boolean.toString( false );
	private final static String  TRUE = Boolean.toString( true  );
	
	static private Variable transformation = new Variable( "transformation", FALSE );
	static public  boolean  transformation() {
		return transformation.get().equalsIgnoreCase( TRUE );
	}
	static public  boolean transformation( boolean b ) {
		transformation.set( b ? TRUE : FALSE );
		return b;
	}

	static private Variable translation = new Variable( "translation", FALSE );
	static public  boolean  translation() {
		return translation.get().equalsIgnoreCase( TRUE );
	}
	static public  boolean translation( boolean b ) {
		translation.set( b ? TRUE : FALSE );
		return b;
	}

	//
	// Repertoire Management -- above
	// *********************************************************** 

	// entry point for Enguage, re-entry point for Intention
	static public Reply mediate( Utterance u ) {
		// Ordering of repertoire:
		// 1. check through autop first, at startup
		// 2. during runtime, do user signs first
		Reply r = new Reply();
		if (transformation() || translation() || Autoload.ing()) {
			r = autop.mediate( u );
			if (Reply.DNU == r.type()) {
				r = signs.mediate( u );
				if (Reply.DNU == r.type())
					r = allop.mediate( u );
			}
		} else {
			Autoload.load( u.representamen() ); // unloaded up in Enguage.interpret()
			
			/* At this point we need to rebuild utterance with the (auto)loaded concept,
			 * with any colloquialisms it may have loaded...
			 * Needs to be expanded in case we've expanded any parameters (e.g. whatever)
			 */
			u = new Utterance( u.expanded() );
			
			r = signs.mediate( u );
			if (Reply.DNU == r.type()) {
				r = allop.mediate( u );
				if (Reply.DNU == r.type())
					r = autop.mediate( u );
		}	}
		return r;
}	}
