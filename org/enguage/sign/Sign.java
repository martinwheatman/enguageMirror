package org.enguage.sign;

import java.util.Iterator;
import java.util.ListIterator;

import org.enguage.repertoires.Repertoires;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Intentions;
import org.enguage.sign.interpretant.Intentions.Insertion;
import org.enguage.sign.object.Temporal;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.pattern.Frag;
import org.enguage.sign.symbol.pattern.Frags;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Sign {
	
	public  static final String   NAME = "sign";
	private static final Audit   audit = new Audit( NAME );
	public  static final int        ID = 340224; //Strings.hash( NAME )
	
	public  static final String USER_DEFINED = "OTF"; // concept name for signs created on-the-fly
	private static final String UNDER_CONSTR = "TBD"; // concept name for signs under construction
	private static final String         THAT = "that"; // always(?) refers to a variable
	
	public  static class Builder {

		// Builds a sign from a string text: 
		public static final String    SIGN_START_TOKEN = "On";
		public static final String  PATTERN_COMMA_TERM = ",";
		//		'On "hello", reply "hello to you too".'
		
		public static final String  PATTERN_COLON_TERM = ":";
		public static final String INTENTION_SEPARATOR = ";";
		/*	 	'On "i need QUOTED-THINGS":
		 *			add THINGS to my needs list;
		 *			if not, perform "do this"  ;
		 *			if so, run "ls -l"         ;
		 *			reply "ok, you need ...".'
		 */
		 //		'what do i need.' => returns null: this is an utterance!
				
		public Builder( Strings sa ) {utterance = new Strings( sa );}
		public Builder( String   s ) {this( new Strings( s ));}
		
		private final Strings utterance; // already stripped of '.'

		private static String getCond( Strings sa ) {
			if (sa.begins( Config.soPrefix() )) {
				sa.remove( 0, Config.soPrefix().size()); // e.g. 'if', 'so', ','
				return Intention.POS;
			} else if (sa.begins( Config.noPrefix() )) {
				sa.remove( 0, Config.noPrefix().size()); // e.g.'if', 'so', ','
				return Intention.NEG;
			}
			return Intention.NEU;
		}
		private static int getType(Strings sa) {
			// [ "if", "so", ",", "think", "something" ] => 
			//   N_THEN_THINK + [ "think", "something" ]
			// [ "if", "not", ",", "say", "so" ] =>
			//   N_ELSE_REPLY + []
			int rc = Intention.UNDEFINED;
			String cond = getCond( sa );
			boolean neg = cond.equals( Intention.NEG );
			boolean pos = cond.equals( Intention.POS );
			
			
			if (sa.equals( Config.propagateReplys()) ||
				sa.equals( Config.accumulateCmds()) )
			{
				// don't remove these 'specials', pass on & define in config.xml
				rc = pos ? Intention.N_THEN_REPLY :
					(neg ? Intention.N_ELSE_REPLY : Intention.N_REPLY);
				
			} else {
				int len = sa.size();
				String one = len>0?sa.get(0):"";
				String two = len>1?sa.get(1):"";
				
				if (Strings.isQuoted( two )) {
					boolean found = true;
					if (one.equals(Intention.DO_HOOK))
						rc = pos ? Intention.N_THEN_DO   :
							neg ? Intention.N_ELSE_DO    :
								Intention.N_DO;
					
					else  if (one.equals(   Intention.RUN_HOOK ))
						rc = pos ? Intention.N_THEN_RUN   :
							neg ? Intention.N_ELSE_RUN   : Intention.N_RUN;
					
					else  if (one.equals( Intention.REPLY_HOOK ))
						rc = pos ? Intention.N_THEN_REPLY :
							(neg ? Intention.N_ELSE_REPLY : Intention.N_REPLY);
						
					else  if (one.equals( Intention.FNLLY_HOOK ))
						rc = Intention.N_FINALLY;
						
					else
						found = false;
					
					if (found) {
						sa.remove(0); // ["perform" | "run" | "reply" | "finally" ]
						sa.set( 0, Strings.trim( sa.get(0), '"' ));
			}	}	}
			
			if (rc == Intention.UNDEFINED) {
				rc = pos ? Intention.N_THEN_THINK :
					neg ? Intention.N_ELSE_THINK : Intention.N_THINK;
			}
			
			return rc;
		}

		private Sign doIntentions(Iterator<String> utti, Sign sign) {
			Strings sa = new Strings();
			while (utti.hasNext()) {
				String s = utti.next();
				if (s.equals( INTENTION_SEPARATOR )) {
					int type = getType( sa );
					sign.append( new Intention( type, sa ));
					sa = new Strings();
				} else
					sa.append( s );
			}
			
			// backwards compatibility:
			if (!sa.isEmpty()) {
				// getType affects sa!!! be explicit in ordering
				int type = getType( sa );
				sign.append( new Intention( type, sa ));
			}
			return sign;
		}
		private Sign doPattern(Iterator<String> si) {
			if (si.hasNext()) {
				String s = si.next();
				Frags frags = new Frags( Strings.trim( s, '"' ));
				if (si.hasNext()) {
					s = si.next();
					if ((s.equals(PATTERN_COMMA_TERM) ||
						 s.equals(PATTERN_COLON_TERM))
						&& si.hasNext())
					{
						return doIntentions( si,
									new Sign()
										.pattern( frags )
										.concept( Intention.concept()
								)	);
					}
			}	}
			return null;
		}
		public  Sign toSign() {
			ListIterator<String> si = utterance.listIterator();
			if (si.hasNext()) {
				String s = si.next();
				if (s.equals(SIGN_START_TOKEN))
					return doPattern( si );
				else
					si.previous();
			}
			return null;
	}	} // SignBuilder - builds sign from 'On "xyz": ...' text.
	
	public Sign() {super();}
	public Sign( Frag  patte  ) {this(); pattern( patte );}
	public Sign( String prefix ) {this( new Frag( prefix ));}
	public Sign( String prefix, Frag variable, String postfix ) {
		this( variable.prefix( prefix ).postfix( postfix ));
	}
	public Sign( String prefix1, Frag variable1,
	             String prefix2, Frag variable2 )
	{	this();
		pattern( variable1.prefix( prefix1 ));
		pattern( variable2.prefix( prefix2 ));
	}
	
	/* Member - PATTERN
	 */
	private Frags pattern = new Frags();
	public  Frags pattern() {return pattern;}
	public  Sign  pattern( String s ) {pattern = new Frags(s); return this;}
	public  Sign  pattern( Frags ta ) {pattern = ta; return this;}
	public  Sign  pattern( Frag child ) {
		if (!child.isEmpty())
			pattern.add( child );
		return this;
	}
	public  Sign  pattern( String prefix, String name ) {
		pattern( new Frag( prefix, name ));
		return this;
	}
	public  Sign  pattern( String prefix, String name, String postfix ) {
		pattern( new Frag( prefix, name, postfix ));
		return this;
	}
	public  Sign  split( String word ) {pattern( pattern.split( word )); return this;} 

	/* Member - Intentions - THOUGHTS and REFERENCES
	 */
	private Intentions intentions = new Intentions();
	public  Intentions intentions() {return intentions;}
	public  Sign       intentions( Intentions is ) {intentions = is; return this;}
	public  Sign       insert( Intentions.Insertion ins, Intention intent ) {intentions.insert( ins, intent ); return this;}
	public  Sign       append( Intention intent ) {intentions.add( intent ); return this;}
	public  Sign       append( int type, String pattern ) {intentions.append( new Intention( type, pattern )); return this;}
	
	private static Sign voiced = null;
	public  static void reset() {voiced = null;}
	
	private static Sign latest = null;
	public  static Sign latest() {return latest;}
	public  static Sign latest(Sign s) {latest = s; return latest;}
	
	// Set during autopoiesis - replaces 'id' attribute 
	private String  concept = "";
	public  String  concept() { return concept; }
	public  Sign    concept( String name ) { concept = name; return this; }
	
	/* Members - spatio-temporal
	 */
	private boolean temporalSet = false;
	private boolean temporal = false;
	private void    temporalIs( boolean b ) {temporal = b; temporalSet = true;}
	public  boolean isTemporal() {
		if (!temporalSet && !concept.equals( "" )) {
			temporal = Temporal.isConcept( concept );
			temporalSet = true;
		}
		return temporal;
	}
	private boolean spatialSet = false;
	private boolean spatial = false;
	public  boolean isSpatial() {
		if (!spatialSet && !concept.equals( "" )) {
			spatial = Where.isConcept( concept );
			spatialSet = true;
		}
		return spatial;
	}
	
	public int cplex() {return pattern().cplex( concept().equals( USER_DEFINED ));}
	
	public String toXml( int n, long complexity ) {
		String ind = Audit.indent();
		return "<"+ NAME
				+" "+ Attribute.asString( "nth" , ""+n )
				+" "+ Attribute.asString( "complexity", ""+complexity )
				+" "+ Attribute.asString( "repertoire", concept())
				+    (isTemporal()?" "+Attribute.asString( "temporal", "true"):"")
				+ intentions.toXml() +">\n"
				+ ind + ind + pattern().toString() + "</"+ NAME +">";
	}
	public String toStringIndented( boolean auditIndents ) {
		return Audit.indent() + toString(auditIndents);
	}
	public String toString( boolean auditIndents ) {
		return "On \""+ pattern().toString()+ "\""
				+ intentions.toStringIndented( auditIndents ) +".";
	}
	
	public String  toLine() {return toString( false ) + "\n";}
	public boolean toFile( String fname ){return Fs.stringAppendFile( fname, toLine());}
	public void    toFile() {Fs.stringToFile( pattern.toFilename(), toLine());}
	
	/*
	 * This will handle:
	 *  	"sign [next|prev|...]
	 *  			[else|then]
	 *  			[create|imply|run|perform|finally] <PHRASE-X>"
	 * as found in interpret.txt
	 */

	private static Strings markedUppercaser( String marker, Strings vars, Strings implication ) {
		// takes a marker and replaces that following with upper case...
		// takes a marker and replaces   FOLLOWING    with upper case...
		Strings modified = new Strings();
		
		for (int i=0; i<implication.size(); i++)
			if (i<implication.size()-1 &&
					implication.get( i ).equals( marker ) &&
					vars.contains( implication.get( i + 1 )))
			
				modified.append( implication.get( ++i ).toUpperCase());
				
			else
				modified.add( implication.get( i ));
		
		return modified;
	}

	private static int condType( int base, boolean isThen, boolean isElse ) {
		if (isThen) return base | Intention.N_THEN;
		if (isElse) return base | Intention.N_ELSE;
		return base;
	}
	private static void imply( String intention, boolean isThen, boolean isElse ) {
		voiced.insert(
				Insertion.PREPEND,
				new Intention(
						condType( Intention.N_THINK, isThen, isElse ),
						Frags.toPattern(
								markedUppercaser( // we need to replace "that someone" with "SOMEONE"
										THAT, voiced.pattern().names(), new Strings( intention ) 
						)		)
		)		);
	}

	public static Strings perform( Strings args ) {
		// This is a "Brain Method" according to Sonar Lint -- refactoring TBD
		// It simply takes commands to construct a voiced sign, e.g. create, append etc.
		// DOH! Brain dead static analysis detected :-) 
		// Releasing this version as its holding up large set of changes.
		// N.B. the 'payload' (e.g. the intention) is passed as a string.
		
		audit.in( "perform", args.toString());
		String rc = Perform.S_FAIL;
		
		if (!args.isEmpty()) {
			
			rc = "ok";
			
			String cmd = args.remove( 0 );
			Insertion ins = Intentions.getInsertionType( cmd );
			if (ins != Insertion.UNKNOWN)
				cmd = args.remove( 0 );
			
			boolean isElse = false;
			boolean isThen = false;
			if (cmd.equals( "else" )) {
				isElse = true;
				cmd = args.remove( 0 );
			} else if (cmd.equals( "then" )) {
				isThen = true;
				cmd = args.remove( 0 );
			}
				
			if (cmd.equals( "create" )) {
				
				// rename previously voiced sign
				Repertoires.signs().rename( UNDER_CONSTR, USER_DEFINED );
				
				// create new WIP sign
				voiced = new Sign()
						.pattern( new Frags( args.toString() ))
						.concept( UNDER_CONSTR );
				Repertoires.signs().insert( voiced );
				
			} else if (cmd.equals( "split" )) {
				
				if (args.size() == 1) {
					voiced.pattern(
							voiced.pattern().split(args.get( 0 ))
					);
					
				} else 	if (args.size() > 1) {
					voiced.pattern(
							voiced.pattern().split(args.get( 0 ), args.get( 1 ))
					);
					
				} else {
					audit.error( "split: missing parameter(s)" );
				}
				
				// splitting will change the pattern complexity
				// therefore it needs to be reinserted...
				Repertoires.signs().remove( UNDER_CONSTR );
				Repertoires.signs().insert( voiced );
				
			} else if (cmd.equals( "perform" )) {
				voiced.insert( ins, 
					new Intention(
						isElse ? Intention.N_ELSE_DO :
							isThen ? Intention.N_THEN_DO :
								Intention.N_DO,
						Frags.toPattern( args )
				)	);
				
				
			} else if (cmd.equals( "show" )) {
				if (voiced != null)
					// w/o Audit indents!
					Audit.log( voiced.toString( false ));
				else
					rc = Perform.S_FAIL + ", nothing to see here";
						
						
			} else if (cmd.equals( "reply" )) {
				// we need to replace "that someone" with "SOMEONE"
				Strings modified = markedUppercaser(
						THAT, voiced.pattern().names(), new Strings( args.toString())
				);

				voiced.insert( ins,
					new Intention(
						isElse? Intention.N_ELSE_REPLY :
							isThen ? Intention.N_THEN_REPLY :
								Intention.N_REPLY, 
						Frags.toPattern( modified )
				)	);

				
			} else if (cmd.equals( "think" )) {
				//audit.debug( "adding a thought "+ args.toString() )
				
				if (voiced != null) { //BUG: sign think called w/o voiced
					voiced.insert( ins, 
						new Intention(
							isElse? Intention.N_ELSE_THINK :
								isThen ? Intention.N_THEN_THINK :
									Intention.N_THINK,
							Frags.toPattern( new Strings( args.toString() ))
					)	);
				}
				
			} else if (cmd.equals( "imply" )) {
				
				imply( args.get( 0 ), isThen, isElse );
				
				
			} else if (cmd.equals( "run" )) {
				//audit.debug( "appending a script to run: '"+ args.toString() +"'")
				voiced.insert(
						Insertion.PREPEND, // "implies that you run"
						new Intention(
								isElse? Intention.N_ELSE_RUN : isThen ? Intention.N_THEN_RUN : Intention.N_RUN,
								Frags.toPattern( new Strings( args.toString() ))
				)		);
				
			} else if (cmd.equals( "temporal")) {
				voiced.temporalIs( true );
				
			} else if (cmd.equals( "finally" )) {
				cmd = args.get( 0 ); // don't remove
				Intention intn;
				//audit.debug( "adding a final clause? "+ args.toString() )
				if (cmd.equals( Intention.DO_HOOK ))
					intn = new Intention(
							isElse ? Intention.N_ELSE_DO    : isThen ? Intention.N_THEN_DO : Intention.N_DO,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				
				else if (cmd.equals( Intention.REPLY_HOOK )) {
					Strings vals = Frags.toPattern( new Strings( args.toString() )); 
					intn = new Intention(
							isElse ? Intention.N_ELSE_REPLY : isThen ? Intention.N_THEN_REPLY : Intention.N_REPLY,
							vals
						 );
				
				} else // THINK_HOOK
					intn = new Intention(
							isElse ? Intention.N_ELSE_THINK : isThen ? Intention.N_THEN_THINK : Intention.N_THINK,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				voiced.append( intn ); // all finals at the end :)
				//voiced = null; -- maybe more finallys
			
			} else {
				rc = Perform.S_FAIL;
				audit.error( "Unknown Sign.interpret() command: "+ cmd );
		}	}
		audit.out( new Strings( rc ));
		return new Strings( rc ); 
}	}
