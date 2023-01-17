package org.enguage.signs;

import org.enguage.repertoire.Repertoire;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Intentions;
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
	private static final String INDENT = "    ";

	public Sign() {super();}
	public Sign( Frag  patte  ) {this(); pattern( patte );}
	public Sign( String prefix ) {this( new Frag( prefix ));}
	public Sign( String prefix, Frag variable ) {this( variable.prefix( prefix ));}
	public Sign( String prefix, Frag variable, String postfix ) {
		this( variable.prefix( prefix ).postfix( postfix ));
	}
	public Sign( String prefix1, Frag variable1,
	             String prefix2, Frag variable2 )
	{	this();
		pattern( variable1.prefix( prefix1 ));
		pattern( variable2.prefix( prefix2 ));
	}
	
	/* 
	 * Member - PATTERN
	 */
	private Frags pattern = new Frags();
	public  Frags pattern() {return pattern;}
	public  Sign  pattern( Frags ta ) { pattern = ta; return this; }
	public  Sign  pattern( Frag child ) {
		if (!child.isEmpty())
			pattern.add( child );
		return this;
	}
	public Sign   pattern( String prefix, String name ) {
		pattern( new Frag( prefix, name ));
		return this;
	}
	public Sign   pattern( String prefix, String name, String postfix ) {
		pattern( new Frag( prefix, name, postfix ));
		return this;
	}
	public Sign split( String word ) {pattern( pattern.split( word )); return this;} 

	/*
	 * Member - Intentions - THOUGHTS and REFERENCES
	 */
	private Intentions intentions = new Intentions();
	public Sign  insert( int n, Intention intent ) {intentions.insert( n, intent ); return this;}
	public Sign  append( Intention intent ) {intentions.add( intent ); return this;}
	public Sign  appendIntention( int type, String pattern ) {intentions.appendIntention( type, pattern ); return this;}
	public Intentions intentions() {return intentions;}
	
	public static Sign voiced = null;
	
	// Set during autopoiesis - replaces 'id' attribute 
	private String  concept = "";
	public  String  concept() { return concept; }
	public  Sign    concept( String name ) { concept = name; return this; }
	
	/*
	 * Members - spatio-temporal
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
	
	/* To protect against being interpreted twice on resetting the iterator
	 * after a DNU is returned on co-modification of the iterator by
	 * autoloading repertoires.
	 * 
	 * We may see this pattern in a list of signs following comodification:
	 * 	t t t t f t t t f f t t f f f t t T f f f f f f f f f f f f f f
	 *                                    ^ 
	 * 'cos some new signs will be peppered through out the list. We need
	 * to start again at the sign following "^".
	 * N.B. Don't really want to unload those autoloaded signs as they may well
	 * be needed by the eventually understood utterance.  Autounloading (aging) 
	 * will manage the list if not.
	 * In fact, there should only be one T, the other t's are tidied as 
	 * processing progresses, so there is no need to determine when the...
	 */
	public int interpretation = Signs.NO_INTERPRETATION;
	
	public int cplex() {return pattern().cplex();}
	
	public String toXml( int n, long complexity ) {
		return  INDENT +"<"+ NAME
				+" "+ Attribute.asString( "n" , ""+n )
				+" "+ Attribute.asString( "complexity", ""+complexity )
				+" "+ Attribute.asString( "repertoire", concept())
				+    (isTemporal()?" "+Attribute.asString( "temporal", "true"):"")
				+ intentions.toXml()
				+ ">\n"+ INDENT + INDENT + pattern().toString() + "</"+ NAME +">";
	}
	public String toStringIndented() {
		return Audit.indent()
				+ "On \""+ pattern().toString()+ "\""
				+ intentions.toStringIndented() 
				+ ".";
	}
	public String toString() {
		return "On \""+ pattern().toString()+ "\""
				+ intentions.toStringIndented() 
				+ ".\n";
	}
		
	public boolean toFile( String fname ){return Fs.stringAppendFile( fname, toString());}
	public void    toFile() {Fs.stringToFile( pattern.toFilename(), toString());}
	public void    toVariable() {Variable.set( pattern.toFilename(), toString());}
	
	/*
	 * This will handle "sign create X", found in interpret.txt
	 */
	public static Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString());
		String rc = Shell.FAIL;
		
		if (!args.isEmpty()) {
			
			rc = "ok";
			
			boolean  header = false,
			        prepend = false,
			         tailer = false, // should be the default?
			         append = false; 
			
			String cmd = args.remove( 0 );
			if (cmd.equals( "header" ))
				header = true;
			else if (cmd.equals( "prepend" ))
				prepend = true;
			else if (cmd.equals( "tailer" ))
				tailer = true;
			else if (cmd.equals( "append" ))
				append = true;
			
			if (header || prepend || tailer || append)
				cmd = args.remove( 0 );
			
			boolean isElse = false;
			if (cmd.equals( "else" )) {
				isElse = true;
				cmd = args.remove( 0 );
			}
				
			if (cmd.equals( "create" )) {
				
				voiced = new Sign()
						.pattern( new Frags( args.toString() ))
						.concept( Repertoire.AUTOPOIETIC );
				Repertoire.signs.insert( voiced );
				
			} else if (cmd.equals( "split" )) {
				
				if (args.size() == 1) {
					
					Sign.voiced.pattern(
							Sign.voiced.pattern().split(args.get( 0 ))
					);
				} else 	if (args.size() > 1) {
					
					Sign.voiced.pattern(
							Sign.voiced.pattern().split(args.get( 0 ), args.get( 1 ))
					);
				} else {
					audit.ERROR( "missing" );
				}
				
			} else if (cmd.equals( "perform" )) {
				Intention intn = 
						new Intention(
							isElse ? Intention.elseDo : Intention.thenDo,
							Frags.toPattern( new Strings( args.toString() ))
						);
				
				if (header)
					voiced.insert( 1, intn );
				else if (prepend)
					voiced.insert( 0, intn );
				else if (append)
					voiced.insert( voiced.intentions.size(), intn );
				else
					voiced.insert( voiced.intentions.size()-1, intn );
				
				
			} else if (cmd.equals( "reply" )) {
				Intention intn = 
						new Intention(
							isElse? Intention.elseReply : Intention.thenReply, 
							Frags.toPattern( new Strings( args.toString() ))
						);

				if (header)
					voiced.insert( 1, intn );
				else if (prepend)
					voiced.insert( 0, intn );
				else if (append)
					voiced.insert( voiced.intentions.size(), intn );
				else
					voiced.insert( voiced.intentions.size()-1, intn );

				
			} else if (cmd.equals( "think" )) {
				//audit.debug( "adding a thought "+ args.toString() )
				Intention intn = new Intention(
							isElse? Intention.elseThink : Intention.thenThink,
							Frags.toPattern( new Strings( args.toString() ))
						);
				
				if (voiced != null) { //BUG: sign think called w/o voiced
					if (header)
						voiced.insert( 1, intn );
					else if (prepend)
						voiced.insert( 0, intn );
					else if (append)
						voiced.insert( voiced.intentions.size(), intn );
					else
						voiced.insert( voiced.intentions.size()-1, intn );
				}
				
			} else if (cmd.equals( "imply" )) {
				//audit.debug( "prepending an implication '"+ args.toString() +"'")
				voiced.insert(
						0,
						new Intention(
								isElse? Intention.elseThink : Intention.thenThink,
								Frags.toPattern( new Strings( args.toString() ))
				)		);
				
			} else if (cmd.equals( "run" )) {
				//audit.debug( "appending a script to run: '"+ args.toString() +"'")
				voiced.insert(
						0, // "implies that you run"
						new Intention(
								isElse? Intention.elseRun : Intention.thenRun,
								Frags.toPattern( new Strings( args.toString() ))
				)		);
			} else if (cmd.equals( "temporal")) {
				voiced.temporalIs( true );
				
			} else if (cmd.equals( "finally" )) {
				Intention intn;
				//audit.debug( "adding a final clause? "+ args.toString() )
				if (cmd.length() > 7 && cmd.substring( 0, 7 ).equals( "perform" ))
					intn = new Intention(
							isElse ? Intention.elseDo    : Intention.thenDo,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				
				else if (cmd.equals( "reply" ))
					intn = new Intention(
							isElse ? Intention.elseReply : Intention.thenReply,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				
				else
					intn = new Intention(
							isElse ? Intention.elseThink : Intention.thenThink,
							Frags.toPattern( new Strings( args.toString() ))
						 );
				voiced.append( intn ); // all finals at the end :)
			
			} else {
				rc = Shell.FAIL;
				audit.ERROR( "Unknown Sign.interpret() command: "+ cmd );
		}	}
		audit.out( new Strings( rc ));
		return new Strings( rc ); 
}	}
