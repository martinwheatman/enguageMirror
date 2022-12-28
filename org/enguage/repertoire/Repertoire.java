package org.enguage.repertoire;

import org.enguage.repertoire.concept.Autoload;
import org.enguage.signs.Sign;
import org.enguage.signs.Signs;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.pattern.Phrase;
import org.enguage.signs.symbol.pattern.Quote;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;

public class Repertoire {

	private static final Sign[] autopoiesis = {
		// 3 x 3 signs (think/do/say * start/subseq/infelicit) + 1 "finally"
		new Sign( "On ", new Quote( "x" ), ",", new Phrase( "y" ))
			.append( new Intention( Intention.create, Intention.THINK +" X Y" )),
			
		new Sign( "On ",new Quote( "x" ), ", perform ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.DO +" X Y" )),
			
		new Sign( "On ", new Quote( "x" ), ", reply ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.REPLY +" X Y" )),
			
		new Sign( "Then, ", new Phrase( "y" ))
			.append( new Intention( Intention.append, Intention.THINK +" Y" )),
			
		new Sign( "Then, perform ",  new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.DO +" Y" )),
			
		new Sign( "Then, reply   ", new Quote("y" ))
			.append( new Intention( Intention.append, Intention.REPLY+" Y" )),
			
		new Sign( "Then, if not, ",  new Phrase( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_THINK +" Y" )),
			
		new Sign( "Then, if not, perform ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_DO +" Y" )),
					
		new Sign( "Then, if not, reply ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_REPLY +" Y")),
			
		new Sign( "Then, if not, say so" )
			.append( new Intention( Intention.append, Intention.ELSE_REPLY +" \"\" ")),
			
		new Sign( " Finally,   perform ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.FINALLY+" Y" )),
			
		//	Added 3 new signs for the running of applications external to enguage...
		new Sign( "On ", new Quote( "x" ), ", run ", new Quote( "y" ))
			.append( new Intention( Intention.create, Intention.RUN +" X Y" )),
		
		new Sign( "Then, run ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.RUN +" Y" )),
	
		new Sign( "Then, if not, run ", new Quote( "y" ))
			.append( new Intention( Intention.append, Intention.ELSE_RUN +" Y" ))
	};
	public  static final String PRONUNCIATION = "repper-to-are";  // better than  ~wah	
	public  static final String PLURALISATION = "repper-to-wahs"; // better than ~ares
	public  static final String          NAME = "repertoire";
	public  static final String           LOC = "rpt";

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
	public static Signs signs = new Signs( "user"  );
	public static Signs autop = new Signs( "autop" ).add( autopoiesis );
	public static Signs allop = new Signs( "allop" ).add( Engine.commands );
	
	/* A persistent Induction is used in the repertoire.
	 */
	private static final String FALSE = Boolean.toString( false );
	private static final String  TRUE = Boolean.toString( true  );
	
	private static Variable transformation = new Variable( "transformation", FALSE );
	public  static  boolean  transformation() {
		return transformation.get().equalsIgnoreCase( TRUE );
	}
	public static  boolean transformation( boolean b ) {
		transformation.set( b ? TRUE : FALSE );
		return b;
	}

	//
	// Repertoire Management -- above
	// *********************************************************** 

	// entry point for Enguage, re-entry point for Intention
	public static Reply mediate( Utterance u ) {
		//audit.in( "mediate", "utterance="+ u );
		// Ordering of repertoire:
		// 1. check through autop first, at startup
		// 2. during runtime, do user signs first
		Reply r = new Reply();
		r = autop.mediate( u );
		if (Response.DNU == r.response()) {
			
			if (!transformation()) {
				Autoload.load( u.representamen() ); // unloaded up in Enguage.interpret()
				
				/* At this point we need to rebuild utterance with the (auto)loaded concept,
				 * with any colloquialisms it may have loaded...
				 * Needs to be expanded in case we've expanded any parameters (e.g. whatever)
				 */
				u = new Utterance( u.expanded() );
			}
			
			r = autop.mediate( u );
			if (Response.DNU == r.response()) {
				r = signs.mediate( u );
				if (Response.DNU == r.response())
					r = allop.mediate( u );
		}	}
		//audit.out( r );
		return r;
}	}
