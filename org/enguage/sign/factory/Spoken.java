package org.enguage.sign.factory;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.pattern.Pattern;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Spoken {
	
	private Spoken() {}
	
	public  static final String   NAME = "Spoken";
	private static final Audit   audit = new Audit( NAME );
	
	private static final String UNDER_CONSTR = "TBD"; // concept name for signs under construction
	private static final String         THAT = "that"; // always(?) refers to a variable
	private static final String     VAR_NAME = "VOICED";

	private static Sign voiced = null;
	public  static void delete() {voiced = null;}

	private static void saveVoicedAsVariable() {Variable.set( VAR_NAME, voiced.toString() );}
	private static Sign rereadVoiceFromVariable() {
		String savedConcept = Concept.concept();
		Concept.concept( UNDER_CONSTR );
		Repertoires.signs().remove( UNDER_CONSTR );

		voiced = new Written(
				Variable.get( VAR_NAME )
		).toSign();
		
		Repertoires.signs().insert( voiced );
		Concept.concept( savedConcept );
		return voiced;
	}
	
	/*
	 * Pattern creation routines
	 */
	private static void doCreate( Strings args ) {
		// rename previously voiced sign
		Repertoires.signs().rename( UNDER_CONSTR, Sign.USER_DEFINED );
		
		// create new WIP sign
		voiced = new Sign()
				.pattern( new Pattern( args.toString() ))
				.concept( UNDER_CONSTR );
		Repertoires.signs().insert( voiced );
		
		saveVoicedAsVariable();
	}
	private static void doSplit( Strings args ) {
		if (voiced != null) {
			voiced = rereadVoiceFromVariable();
			if (args.size() == 1)
				voiced.pattern(
						voiced.pattern().split(args.get( 0 ))
				);
				
			else if (args.size() > 1)
				voiced.pattern(
						voiced.pattern().split(args.get( 0 ), args.get( 1 ))
				);
				
			else
				audit.error( "split: missing parameter(s)" );
			
			// splitting will change the pattern complexity
			// therefore it needs to be reinserted...
			Repertoires.signs().remove( UNDER_CONSTR );
			Repertoires.signs().insert( voiced );
			saveVoicedAsVariable();
	}	}
	/*
	 * Voiced management routines
	 */
	private static void doImply( String intention, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromVariable();
			voiced.insert(
				new Intention(
						Intention.condType( Intention.N_THINK, isThen, isElse ),
						Pattern.toPattern(
								// replace "that someone"...
								// ...with "SOMEONE"
								Strings.markedUppercaser(
										THAT,
										voiced.pattern().names(), 
										new Strings( intention ) 
						)		)
			)	);
			saveVoicedAsVariable();
		}
	}
	private static void doPerform( Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromVariable();
			voiced.insert(
				new Intention(
					Intention.condType( Intention.N_DO, isThen, isElse ),
					Pattern.toPattern( args )
			)	);
			saveVoicedAsVariable();
		}
	}
	private static void doThink( Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null) { //BUG: sign think called w/o voiced
			voiced = rereadVoiceFromVariable();
			voiced.insert(
				new Intention(
					Intention.condType( Intention.N_THINK, isThen, isElse ),
					Pattern.toPattern( new Strings( args.toString() ))
			)	);
			saveVoicedAsVariable();
	}	}
	private static void doRun( String intention, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromVariable();
			Strings s = Strings.markedUppercaser(
					THAT,
					voiced.pattern().names(), 
					new Strings( intention ) 
			);
			
			voiced.insert(
					new Intention(
							Intention.condType( Intention.N_RUN, isThen, isElse ),
							Pattern.toPattern( s )
			)		);
			saveVoicedAsVariable();
	}	}
	private static void doReply( Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromVariable();
			// we need to replace "that someone" with "SOMEONE"
			Intention intent = new Intention(
					Intention.condType( Intention.N_REPLY, isThen, isElse ),
					Pattern.toPattern(
							Strings.markedUppercaser(
									THAT,
									voiced.pattern().names(),
									new Strings( args.toString() )
					)		)
			);
			voiced.insert( intent );
			saveVoicedAsVariable();
	}	}
	private static void doFinally( Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromVariable();
			// N.B. this method is not currently exercised!!!
			String cmd = args.get( 0 ); // don't remove it
			int base = Intention.N_THINK;
			if (cmd.equals( Intention.DO_HOOK ))
				base = Intention.N_DO;
			else if (cmd.equals( Intention.REPLY_HOOK ))
				base = Intention.N_REPLY;
			
			voiced.append( // all finals at the end :)
					new Intention(
							Intention.condType( base, isThen, isElse ),
							Pattern.toPattern( new Strings( args.toString() ))
					)
			);
			// write out here
			saveVoicedAsVariable();
	}	}
	private static String doShow() {
		if (voiced != null) {
			rereadVoiceFromVariable();
			// w/o Audit indents!
			Audit.log( voiced.toString( false ));
		} else
			return Perform.S_FAIL + ", nothing to see here";
		return Perform.S_SUCCESS;
	}
	
	private static boolean isElse( String cmd ) {return cmd.equals( "else" );}
	private static boolean isThen( String cmd ) {return cmd.equals( "then" );}

	/*
	 * This will handle:
	 *  	"sign [else|then] create|imply|run|perform|finally <PHRASE-X>"
	 * as found in interpret.txt can_say.txt first+then.txt
	 */
	public static Strings perform( Strings args ) {
		// It simply takes commands to construct a voiced sign, e.g. create, append etc.
		// N.B. the 'payload' (e.g. the intention) is passed as a string.
		
		audit.in( "perform", args.toString() );
		
		String      rc = Perform.S_SUCCESS;			
		String     cmd = args.remove( 0 );
		boolean isElse = isElse( cmd );
		boolean isThen = isThen( cmd );
		
		if (isThen || isElse)
			cmd = args.remove( 0 );
			
		if (cmd.equals( "create" ))
			doCreate( args );
			
		else if (cmd.equals( "split" ))
			doSplit( args );
			
		else if (cmd.equals( "perform" ))
			doPerform( args, isThen, isElse );
			
		else if (cmd.equals( "show" ))
			rc = doShow();
						
		else if (cmd.equals( "think" ))
			doThink( args, isThen, isElse );
			
		else if (cmd.equals( "imply" ))
			doImply( args.get( 0 ), isThen, isElse );
			
		else if (cmd.equals( "run" ))
			doRun( args.get( 0 ), isThen, isElse );
			
		else if (cmd.equals( "temporal"))
			voiced.temporalIs( true );
			
		else if (cmd.equals( "reply" ))
			doReply( args, isThen, isElse );
			
		else if (cmd.equals( "finally" ))
			doFinally( args, isThen, isElse );
			
		else {
			rc = Perform.S_FAIL;
			audit.error( "Unknown Sign.interpret() command: "+ cmd );
		}
		audit.out( rc );
		return new Strings( rc ); 
}	}
