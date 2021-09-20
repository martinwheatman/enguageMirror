package org.enguage.interp.sign;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.interp.intention.Intention;
import org.enguage.interp.pattern.Patterns;
import org.enguage.interp.pattern.Pattern;
import org.enguage.interp.repertoire.Engine;
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
	public Sign( Pattern  patte  ) { this(); pattern( patte );}
	public Sign( String prefix ) { this( new Pattern( prefix )); }
	public Sign( String prefix, Pattern variable ) { this( variable.prefix( prefix ));}
	public Sign( String prefix, Pattern variable, String postfix ) {
		this( variable.prefix( prefix ).postfix( postfix ));
	}
	public Sign( String prefix1, Pattern variable1,
	             String prefix2, Pattern variable2 )
	{	this();
		pattern( variable1.prefix( prefix1 ));
		pattern( variable2.prefix( prefix2 ));
	}
	
	private Patterns pattern = new Patterns();
	public  Patterns pattern() {return pattern;}
	public  Sign     pattern( Patterns ta ) { pattern = ta; return this; }
	public  Sign     pattern( Pattern child ) {
		if (!child.isEmpty())
			pattern.add( child );
		return this;
	}

	
	private ArrayList<Intention> programme = new ArrayList<Intention>();
	
	public  Sign append(        Intention intent ){ programme.add(    intent ); return this;}
	//public Sign headAppend(   Intention intent ){ intentions.add( 1, intent ); return this;}
	public  Sign prepend(       Intention intent ){ programme.add( 0, intent ); return this;}
	public  Sign insert( int i, Intention intent ){ programme.add( i, intent ); return this;}
	public  Sign appendIntention( int typ, String val ) {programme.add( new Intention(typ,val));return this;}

	
	
	// Set during autopoiesis - replaces 'id' attribute 
	private String  concept = "";
	public  String  concept() { return concept; }
	public  Sign    concept( String name ) { concept = name; return this; }
	
	private boolean temporalSet = false;
	private boolean temporal = false;
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
		for (Intention in : programme)
			intentions += "\n      " + Attribute.asString( Intention.typeToString( in.type() ), in.value() );
		
		return  indent +"<"+ NAME
				+" "+ Attribute.asString( "n" , ""+n )
				+" "+ Attribute.asString( "complexity", ""+complexity )
				+" "+ Attribute.asString( "repertoire", concept() )
				+ intentions
				+ ">\n"+ indent + indent + pattern().toString() + "</"+ NAME +">";
	}
	public String toString() {
		String sign = "";
		int sz = programme.size();
		if (sz > 0) {
			sign = "On \""+ pattern().toString() +"\"";
			if (sz == 1)
				sign += ", "+ programme.get(0);
			else {
				int line = 0;
				for (Intention in : programme)
					sign += (line++ == 0 ? ":" : ";") + "\n" + indent + in;
		}	}	
		return sign +".\n";
	}
	public boolean toFile( String fname ){
		//Audit.log( "creating: "+ loc + pattern.toFilename() +".txt" );
		return Fs.stringAppendFile( fname, toString());
	}
	public void toFile() {Fs.stringToFile( pattern.toFilename(), toString());}
	public void toVariable() {Variable.set( pattern.toFilename(), toString());}
	
	public Reply interpret( Reply r ) {
		//audit.in( "interpret", pattern().toString() );
		Iterator<Intention> ai = programme.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();
			switch (in.type()) {
				case Intention.allop :
					r = Engine.interp( in, r );
					break;
				case Intention.create:
				case Intention.prepend:
				case Intention.append:
					r = in.autopoiesis( r );
					break;
				default: // thenFinally, think, do, say...
					r = in.mediate( r );
		}	}
		return r; // (Reply) audit.out( r );
	}
	/*
	 * This will handle "sign create X", found in interpret.txt
	 */
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString());
		String rc = Shell.FAIL;
		
		if (args.size() > 0) {
			String var1 = Variable.get( "prepending" ),
			       var2 = Variable.get( "headAppending" );
			boolean prepending    = var1 != null && var1.equals( "true" ),
					headAppending = var2 != null && var2.equals( "true" );
			
			boolean isElse = false;
			Reply r = new Reply();
			String cmd = args.remove( 0 );
			if (cmd.equals( "else" )) {
				isElse = true;
				cmd = args.remove( 0 );
			}
				
			if (cmd.equals( "create" )) {
				audit.debug( "creating sign with: " +   args.toString());
				rc = new Intention( Intention.create, args.toString(), Intention.create ).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "perform" )) {
				audit.debug( "adding a conceptual "+    args.toString() );
				rc = new Intention(
							isElse ? Intention.elseDo : Intention.thenDo,
							args.toString(),
						   	prepending ?
								Intention.prepend :
							   	headAppending ?
									Intention.headAppend :
									Intention.append
						).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "reply" )) {
				rc = new Intention(
							isElse? Intention.elseReply : Intention.thenReply, 
							args.toString(), 
							prepending ?
								Intention.prepend :
								headAppending ?
								Intention.headAppend :
								Intention.append
						  ).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "think" )) {
				audit.debug( "adding a thought "+ args.toString() );
				rc = new Intention(
							isElse? Intention.elseThink : Intention.thenThink,
							args.toString(), 
							prepending ?
								Intention.prepend :
								headAppending ?
									Intention.headAppend :
									Intention.append
					  ).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "imply" )) {
				audit.debug( "prepending an implication '"+ args.toString() +"'");
				rc = new Intention(
						isElse? Intention.elseThink : Intention.thenThink,
								args.toString(),
						Intention.prepend
					 ).autopoiesis( r ).toString();
				
			} else if (cmd.equals( "finally" )) {
				audit.debug( "adding a final clause? "+ args.toString() );
				if (cmd.length() > 7 && cmd.substring( 0, 7 ).equals( "perform" ))
					rc = new Intention( isElse ? Intention.elseDo    : Intention.thenDo,    args.toString(), Intention.append ).autopoiesis( r ).toString();
				else if (cmd.equals( "reply" ))
					rc = new Intention( isElse ? Intention.elseReply : Intention.thenReply, args.toString(), Intention.append ).autopoiesis( r ).toString();
				else
					rc = new Intention( isElse ? Intention.elseThink : Intention.thenThink, args.toString().toString(), Intention.append ).autopoiesis( r ).toString();
			
			} else
				audit.ERROR( "Unknown Sign.interpret() command: "+ cmd );
		}
		return audit.out( new Strings( rc ));
	}
	// --- test code below
	public static void complexityTest( Patterns t ) {
		Sign container = new Sign();
		container.pattern( t );
		Audit.log( "Complexity of "+ container.toXml( 0, container.cplex() ) +"\n" );
	}
	public static void main( String argv[]) {
		Sign s = new Sign();
		s.append( new Intention( Intention.thenDo, "person create martin" ));
		s.append( new Intention( Intention.elseReply, "no, somethings gone wrong" ));
		s.append( new Intention( Intention.thenReply, "ok, thank goodness" ));
		s.pattern( new Pattern().prefix( new Strings( "hello" )));
		Reply r = new Reply();
		Intention intent = new Intention( Intention.thenReply, "hello world" );
		r = intent.mediate( r );
		Audit.log( "r="+ r.toString());
		
		Patterns ts = new Patterns();
		ts.add( new Pattern( "this is a", "x" ).phrasedIs() );
		complexityTest( ts );
		s.pattern( new Pattern( "this is a", "x" ).phrasedIs() );
		
		ts = new Patterns();
		ts.add( new Pattern( "this is a", "test" ));
		complexityTest( ts );
		
		ts = new Patterns();
		ts.add( new Pattern( "this is a test", "x" ).phrasedIs() );
		complexityTest( ts );
		
		ts = new Patterns();
		ts.add( new Pattern( "one small step for man", "" ));
		complexityTest( ts );
		
		Audit.log( s.toString());
		s.toFile();
		
		Sign s2 = new Sign();
		s2.pattern( ts );
		s2.append( new Intention( Intention.thenReply, "ok, thank goodness" ));
		Audit.log( s2.toString());
		s2.toVariable();
		
		interpret( new Strings( "this won't work!" ));
		interpret( new Strings( "create variable whom needs phrase variable object" ));
		interpret( new Strings( "reply  ok variable whom needs variable object" ));
		interpret( new Strings( "imply  is variable object in variable name needs list" ));
		//interpret( new Strings( "reply  i know" ));
		//interpret( new Strings( "perform add variable object to variable name needs list" ));
		Intention.printSign();
}	}
