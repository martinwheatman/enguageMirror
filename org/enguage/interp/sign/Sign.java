package org.enguage.interp.sign;

import java.util.Iterator;

import org.enguage.interp.Interpretant;
import org.enguage.interp.intention.Intention;
import org.enguage.interp.pattern.Patte;
import org.enguage.interp.pattern.Pattern;
import org.enguage.interp.repertoire.Engine;
import org.enguage.interp.repertoire.Repertoire;
import org.enguage.objects.Temporal;
import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.reply.Reply;
import org.enguage.vehicle.where.Where;

public class Sign {
	public  static final String   NAME = "sign";
	private static       Audit   audit = new Audit( NAME );
	public  static final int        id = 340224; //Strings.hash( NAME );
	private static final String indent = "    ";

	public Sign() { super(); }
	public Sign( Patte  patte  ) { this(); pattern( patte );}
	public Sign( String prefix ) { this( new Patte( prefix )); }
	public Sign( String prefix, Patte variable ) { this( variable.prefix( prefix ));}
	public Sign( String prefix, Patte variable, String postfix ) {
		this( variable.prefix( prefix ).postfix( postfix ));
	}
	public Sign( String prefix1, Patte variable1,
	             String prefix2, Patte variable2 )
	{	this();
		pattern( variable1.prefix( prefix1 ));
		pattern( variable2.prefix( prefix2 ));
	}
	
	private Pattern pattern = new Pattern();
	public  Pattern pattern() {return pattern;}
	public  Sign    pattern( Pattern ta ) { pattern = ta; return this; }
	public  Sign    pattern( Patte child ) {
		if (!child.isEmpty())
			pattern.add( child );
		return this;
	}

	public Interpretant intents = new Interpretant();
	public Sign  insert( int n, Intention intent ) {intents.insert( n, intent ); return this;}
	public Sign  append( Intention intent ) {intents.add( intent ); return this;}
	public Sign  appendIntention( int type, String pattern ) {intents.appendIntention( type, pattern ); return this;}
 	
	public static Sign voiced = null;
		
	// Set during autopoiesis - replaces 'id' attribute 
	private String  concept = "";
	public  String  concept() { return concept; }
	public  Sign    concept( String name ) { concept = name; return this; }
	
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

	private String help = null; // "" is valid output
	public  String help() { return help; }
	public  Sign   help( String str ) { help = str; return this; }
	
	
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
	public int interpretation = Signs.noInterpretation;
	
	public int cplex() { return pattern().cplex(); }
	
	public String toXml( int n, long complexity ) {
		
		String intentions = "";
		for (Intention in : intents)
			intentions += "\n      " + Attribute.asString( Intention.typeToString( in.type() ), in.value() );
		
		return  indent +"<"+ NAME
				+" "+ Attribute.asString( "n" , ""+n )
				+" "+ Attribute.asString( "complexity", ""+complexity )
				+" "+ Attribute.asString( "repertoire", concept())
				+    (isTemporal()?" "+Attribute.asString( "temporal", "true"):"")
				+ intentions
				+ ">\n"+ indent + indent + pattern().toString() + "</"+ NAME +">";
	}
	public String toStringIndented() {
		Audit.incr();
		String sign = Audit.indent() +"On \""+ pattern().toString() +"\"";
		int sz = intents.size();
		if (sz == 1)
			sign += ", "+ intents.get(0);
		else if (sz > 1) {
			int line = 0;
			for (Intention in : intents)
				sign += (line++ == 0 ? ":" : ";") + "\n" + Audit.indent() +"    "+ in;
		}
		Audit.decr();
		return sign +".";
	}
	public String toString() {return toStringIndented();}
		
	public boolean toFile( String fname ){return Fs.stringAppendFile( fname, toString());}
	public void    toFile() {Fs.stringToFile( pattern.toFilename(), toString());}
	public void    toVariable() {Variable.set( pattern.toFilename(), toString());}
	
	public Reply interpret( Reply r ) {
		//audit.in( "interpret", pattern().toString() );
		Iterator<Intention> ai = intents.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();
			switch (in.type()) {
				case Intention.allop :
					r = Engine.interp( in, r );
					break;
				case Intention.create:
				case Intention.prepend:
				case Intention.append:
					r.answer( in.autopoiesis());
					break;
				default: // thenFinally, think, do, say...
					r = in.mediate( r );
		}	}
		return r; // (Reply) audit.out( r );
	}
	/*
	 * This will handle "sign create X", found in interpret.txt
	 */
	public static Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString());
		String rc = Shell.FAIL;
		
		if (args.size() > 0) {
			
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
				
				Repertoire.signs.insert(
					Sign.voiced = new Sign()
						.pattern( new Pattern( args.toString() ))
						.concept( Repertoire.AUTOPOIETIC )
				);
				
			} else if (cmd.equals( "perform" )) {
				Intention intn = 
						new Intention(
							isElse ? Intention.elseDo : Intention.thenDo,
							Pattern.toPattern( new Strings( args.toString() ))
						);
				
				if (header)
					voiced.insert( 1, intn );
				else if (prepend)
					voiced.insert( 0, intn );
				else if (append)
					voiced.insert( voiced.intents.size(), intn );
				else
					voiced.insert( voiced.intents.size()-1, intn );
				
				
			} else if (cmd.equals( "reply" )) {
				Intention intn = 
						new Intention(
							isElse? Intention.elseReply : Intention.thenReply, 
							Pattern.toPattern( new Strings( args.toString() ))
						);

				if (header)
					voiced.insert( 1, intn );
				else if (prepend)
					voiced.insert( 0, intn );
				else if (append)
					voiced.insert( voiced.intents.size(), intn );
				else
					voiced.insert( voiced.intents.size()-1, intn );

				
			} else if (cmd.equals( "think" )) {
				audit.debug( "adding a thought "+ args.toString() );
				Intention intn = new Intention(
							isElse? Intention.elseThink : Intention.thenThink,
							Pattern.toPattern( new Strings( args.toString() ))
						);
				
				if (voiced != null) { //BUG: sign think called w/o voiced
					if (header)
						voiced.insert( 1, intn );
					else if (prepend)
						voiced.insert( 0, intn );
					else if (append)
						voiced.insert( voiced.intents.size(), intn );
					else
						voiced.insert( voiced.intents.size()-1, intn );
				}
				
			} else if (cmd.equals( "imply" )) {
				audit.debug( "prepending an implication '"+ args.toString() +"'");
				voiced.insert(
						0,
						new Intention(
								isElse? Intention.elseThink : Intention.thenThink,
								Pattern.toPattern( new Strings( args.toString() ))
				)		);
				
			} else if (cmd.equals( "run" )) {
				audit.debug( "appending a script to run: '"+ args.toString() +"'");
				voiced.insert(
						0, // "implies that you run"
						new Intention(
								isElse? Intention.elseRun : Intention.thenRun,
								Pattern.toPattern( new Strings( args.toString() ))
				)		);
			} else if (cmd.equals( "temporal")) {
				voiced.temporalIs( true );
				
			} else if (cmd.equals( "finally" )) {
				Intention intn;
				audit.debug( "adding a final clause? "+ args.toString() );
				if (cmd.length() > 7 && cmd.substring( 0, 7 ).equals( "perform" ))
					intn = new Intention(
							isElse ? Intention.elseDo    : Intention.thenDo,
							Pattern.toPattern( new Strings( args.toString() ))
						 );
				
				else if (cmd.equals( "reply" ))
					intn = new Intention(
							isElse ? Intention.elseReply : Intention.thenReply,
							Pattern.toPattern( new Strings( args.toString() ))
						 );
				
				else
					intn = new Intention(
							isElse ? Intention.elseThink : Intention.thenThink,
							Pattern.toPattern( new Strings( args.toString() ))
						 );
				voiced.append( intn ); // all finallys at the end :)
			
			} else {
				rc = Shell.FAIL;
				audit.ERROR( "Unknown Sign.interpret() command: "+ cmd );
		}	}
		audit.out( new Strings( rc ));
		return new Strings( rc ); 
}	}
