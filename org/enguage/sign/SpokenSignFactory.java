package org.enguage.sign;

import org.enguage.repertoires.Repertoires;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Intentions;
import org.enguage.sign.interpretant.Intentions.Insertion;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.pattern.Frags;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class SpokenSignFactory {
	
	private SpokenSignFactory() {}
	
	public  static final String   NAME = "voiced";
	private static final Audit   audit = new Audit( NAME );
	
	private static final String UNDER_CONSTR = "TBD"; // concept name for signs under construction
	private static final String         THAT = "that"; // always(?) refers to a variable

	private static Sign voiced = null;
	public  static void delete() {voiced = null;}

	private static void doCreate( Strings args ) {
		// rename previously voiced sign
		Repertoires.signs().rename( UNDER_CONSTR, Sign.USER_DEFINED );
		
		// create new WIP sign
		voiced = new Sign()
				.pattern( new Frags( args.toString() ))
				.concept( UNDER_CONSTR );
		Repertoires.signs().insert( voiced );
	}
	private static Strings markedUppercaser( String marker, Strings vars, Strings implication ) {
		// takes a marker and replaces that following with upper case...
		// takes a marker and replaces   FOLLOWING    with upper case...
		Strings uc = new Strings();
		
		for (int i=0; i<implication.size(); i++)
			if (i<implication.size()-1 &&
					implication.get( i ).equals( marker ) &&
					vars.contains( implication.get( i + 1 )))
				
				// increment counter to skip marker
				uc.append( implication.get( ++i ).toUpperCase());
		
			else
				uc.add( implication.get( i ));
		
		return uc;
	}
		
	/*
	 * Voiced management routines
	 */
	private static void doImply( String intention, boolean isThen, boolean isElse ) {
		if (voiced != null)
			voiced.insert(
				Insertion.PREPEND,
				new Intention(
						Intention.condType( Intention.N_THINK, isThen, isElse ),
						Frags.toPattern(
								// replace "that someone"...
								// ...with "SOMEONE"
								markedUppercaser(
										THAT,
										voiced.pattern().names(), 
										new Strings( intention ) 
						)		)
		)		);
	}
	private static void doFinally( Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null) {
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
							Frags.toPattern( new Strings( args.toString() ))
					)
			);
	}	}
	private static void doSplit( Strings args ) {
		if (voiced != null) {
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
	}	}
	private static void doPerform( Insertion ins, Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null)
			voiced.insert( ins, 
				new Intention(
					Intention.condType( Intention.N_DO, isThen, isElse ),
					Frags.toPattern( args )
			)	);
	}
	private static void doReply( Insertion ins, Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null)
			voiced.insert(
				ins,
				new Intention(
					Intention.condType( Intention.N_REPLY, isThen, isElse ),
					Frags.toPattern(
						// we need to replace "that someone" with "SOMEONE"
						markedUppercaser(
							THAT,
							voiced.pattern().names(),
							new Strings( args.toString() )
				)	)
		)	);	
	}
	private static void doThink( Insertion ins, Strings args, boolean isThen, boolean isElse ) {
		//audit.debug( "adding a thought "+ args.toString() )
		if (voiced != null) { //BUG: sign think called w/o voiced
			voiced.insert( ins, 
				new Intention(
					Intention.condType( Intention.N_THINK, isThen, isElse ),
					Frags.toPattern( new Strings( args.toString() ))
			)	);
	}	}
	private static void doRun( Strings args, boolean isThen, boolean isElse ) {
		//audit.debug( "appending a script to run: '"+ args.toString() +"'")
		if (voiced != null)
			voiced.insert(
				Insertion.PREPEND, // "implies that you run"
				new Intention(
						Intention.condType( Intention.N_RUN, isThen, isElse ),
						Frags.toPattern( new Strings( args.toString() ))
		)		);
	}
	private static String doShow() {
		if (voiced != null)
			// w/o Audit indents!
			Audit.log( voiced.toString( false ));
		else
			return Perform.S_FAIL + ", nothing to see here";
		return Perform.S_SUCCESS;
	}
	
	private static boolean isElse( String cmd ) {return cmd.equals( "else" );}
	private static boolean isThen( String cmd ) {return cmd.equals( "then" );}

	/*
	 * This will handle:
	 *  	"sign [next|prev|...]
	 *  			[else|then]
	 *  			[create|imply|run|perform|finally] <PHRASE-X>"
	 * as found in interpret.txt
	 */
	public static Strings perform( Strings args ) {
		// It simply takes commands to construct a voiced sign, e.g. create, append etc.
		// N.B. the 'payload' (e.g. the intention) is passed as a string.
		
		if (args.isEmpty()) return new Strings( Perform.S_FAIL);
		
		audit.in( "perform", args.toString() );
		
		String     rc = Perform.S_SUCCESS;			
		String    cmd = args.remove( 0 );
		Insertion ins = Intentions.getInsertionType( cmd );
		if (ins != Insertion.UNKNOWN)
			cmd = args.remove( 0 );
		
		boolean isElse = isElse( cmd );
		boolean isThen = isThen( cmd );
		if (isThen || isElse) cmd = args.remove( 0 );
			
		if (cmd.equals( "create" ))
			doCreate( args );
			
		else if (cmd.equals( "split" ))
			doSplit( args );
			
		else if (cmd.equals( "perform" ))
			doPerform( ins, args, isThen, isElse );
			
		else if (cmd.equals( "show" ))
			rc = doShow();
						
		else if (cmd.equals( "reply" ))
			doReply( ins, args, isThen, isElse );
			
		else if (cmd.equals( "think" ))
			doThink( ins, args, isThen, isElse );
			
		else if (cmd.equals( "imply" ))
			doImply( args.get( 0 ), isThen, isElse );
			
		else if (cmd.equals( "run" ))
			doRun( args, isThen, isElse );
			
		else if (cmd.equals( "temporal"))
			voiced.temporalIs( true );
			
		else if (cmd.equals( "finally" ))
			doFinally( args, isThen, isElse );
			
		else {
			rc = Perform.S_FAIL;
			audit.error( "Unknown Sign.interpret() command: "+ cmd );
		}
		audit.out( rc );
		return new Strings( rc ); 
}	}
