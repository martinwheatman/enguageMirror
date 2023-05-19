package org.enguage.signs;

import org.enguage.repertoires.Repertoires;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Intentions;
import org.enguage.signs.interpretant.Intentions.Insertion;
import org.enguage.signs.objects.Temporal;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.pattern.Frag;
import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.signs.symbol.where.Where;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

public class Sign {
	public  static final String   NAME = "sign";
	private static final Audit   audit = new Audit( NAME );
	public  static final int        ID = 340224; //Strings.hash( NAME )
	
	public  static final String USER_DEFINED = "OTF"; // concept name for signs created on-the-fly

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
	public String toLine() {return toString() + "\n";}
	public String toStringIndented() {return Audit.indent() + toString();}
	public String toString() {
		return "On \""+ pattern().toString()+ "\""
				+ intentions.toStringIndented() 
				+ ".";
	}
	
	public boolean toFile( String fname ){return Fs.stringAppendFile( fname, toLine());}
	public void    toFile() {Fs.stringToFile( pattern.toFilename(), toLine());}
	public void    toVariable() {Variable.set( pattern.toFilename(), toLine());}
	
	/*
	 * This will handle "sign create X", found in interpret.txt
	 */
	public static Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString());
		String rc = Shell.FAIL;
		
		if (!args.isEmpty()) {
			
			rc = "ok";
			
			String cmd = args.remove( 0 );
			Insertion ins = Intentions.getInsertionType( cmd );
			if (ins != Insertion.UNKNOWN)
				cmd = args.remove( 0 );
			
			boolean isElse = false, isThen = false;
			if (cmd.equals( "else" )) {
				isElse = true;
				cmd = args.remove( 0 );
			} else if (cmd.equals( "then" )) {
				isThen = true;
				cmd = args.remove( 0 );
			}
				
			if (cmd.equals( "create" )) {
				voiced = new Sign()
						.pattern( new Frags( args.toString() ))
						.concept( USER_DEFINED );
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
				
			} else if (cmd.equals( "perform" )) {
				voiced.insert( ins, 
					new Intention(
						isElse ? Intention.N_ELSE_DO : isThen ? Intention.N_THEN_DO : Intention.N_DO,
						Frags.toPattern( args )
				)	);
				
				
			} else if (cmd.equals( "reply" )) {
				voiced.insert( ins, 
					new Intention(
						isElse? Intention.N_ELSE_REPLY : isThen ? Intention.N_THEN_REPLY : Intention.N_REPLY, 
						Frags.toPattern( new Strings( args.toString() ))
				)	);

				
			} else if (cmd.equals( "think" )) {
				//audit.debug( "adding a thought "+ args.toString() )
				
				if (voiced != null) { //BUG: sign think called w/o voiced
					voiced.insert( ins, 
						new Intention(
							isElse? Intention.N_ELSE_THINK : isThen ? Intention.N_THEN_THINK : Intention.N_THINK,
							Frags.toPattern( new Strings( args.toString() ))
					)	);
				}
				
			} else if (cmd.equals( "imply" )) {
				//audit.debug( "prepending an implication '"+ args.toString() +"'")
				voiced.insert(
						Insertion.PREPEND,
						new Intention(
								isElse? Intention.N_ELSE_THINK : isThen ? Intention.N_THEN_THINK : Intention.N_THINK,
								Frags.toPattern( new Strings( args.toString() ))
				)		);
				
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
				Intention intn;
				//audit.debug( "adding a final clause? "+ args.toString() )
				if (cmd.length() > 7 && cmd.substring( 0, 7 ).equals( "perform" ))
					intn = new Intention(
							isElse ? Intention.N_ELSE_DO    : isThen ? Intention.N_THEN_DO : Intention.N_DO,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				
				else if (cmd.equals( "reply" )) {
					Strings vals = Frags.toPattern( new Strings( args.toString() )); 
					intn = new Intention(
							isElse ? Intention.N_ELSE_REPLY : isThen ? Intention.N_THEN_REPLY : Intention.N_REPLY,
							vals
						 );
				
				} else
					intn = new Intention(
							isElse ? Intention.N_ELSE_THINK : isThen ? Intention.N_THEN_THINK : Intention.N_THINK,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				voiced.append( intn ); // all finals at the end :)
				voiced = null;
			
			} else {
				rc = Shell.FAIL;
				audit.error( "Unknown Sign.interpret() command: "+ cmd );
		}	}
		audit.out( new Strings( rc ));
		return new Strings( rc ); 
}	}
