package org.enguage.interp.sign;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.interp.intention.Intention;
import org.enguage.interp.pattern.Pattern;
import org.enguage.interp.pattern.Patternette;
import org.enguage.interp.repertoire.Engine;
import org.enguage.objects.Spatial;
import org.enguage.objects.Temporal;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.vehicle.reply.Reply;

public class Sign {
	private static final String   NAME = "sign";
	private static       Audit   audit = new Audit( NAME );
	private static final String indent = "    ";

	public Sign() { super(); }
	public Sign( Patternette variable ) { this();       pattern( variable );}
	public Sign( String prefix )        { this( new Patternette( prefix )); }
	public Sign( String prefix, Patternette variable ) { this( variable.prefix( prefix ));}
	public Sign( String prefix, Patternette variable, String postfix ) {
		this( variable.prefix( prefix ).postfix( postfix ));
	}
	public Sign( String prefix1, Patternette variable1,
	             String prefix2, Patternette variable2 )
	{	this();
		pattern( variable1.prefix( prefix1 ));
		pattern( variable2.prefix( prefix2 ));
	}
	
	private Pattern pattern = new Pattern();
	public  Pattern pattern() {return pattern;}
	public  Sign    pattern( Pattern ta ) { pattern = ta; return this; }
	public  Sign    pattern( Patternette child ) {
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
			spatial = Spatial.isConcept( concept );
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
	
	public int complexity() { return pattern().complexity(); }
	
	public String toXml( int n, long complexity ) {
		
		String intentions = "";
		for (Intention in : programme)
			intentions += "\n      " + Attribute.asString( Intention.typeToString( in.type() ), in.value() );
		
		return  indent +"<"+ NAME
				+" "+ Attribute.asString( "n" , ""+n )
				+" "+ Attribute.asString( "complexity", ""+complexity )
				+" "+ Attribute.asString( "repertoire", concept() )
				+ intentions
				+ ">\n"+ indent + indent + pattern().toString() + "</"+ NAME +">"
				+ "\n";
	}
	public String toString() {return pattern().toString();}
	
	public Reply mediate( Reply r ) {
		audit.in( "mediate", pattern().toString() );
		Iterator<Intention> ai = programme.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();
			if (!r.isDone()) {
				switch (in.type()) {
					case Intention.allop :
						r = Engine.getReply( in, r );
						break;
					case Intention.create:
					case Intention.prepend:
					case Intention.append:
						r = in.autopoiesis( r );
						break;
					default: // thenFinally, think, do, say...
						r = in.mediate( r );
				}
			} else if (Intention.thenFinally == in.type())
				r = in.mediate( r );
		}
		return (Reply) audit.out( r );
	}
	// ---
	public static void complexityTest( Pattern t ) {
		Sign container = new Sign();
		container.pattern( t );
		audit.log( "Complexity of "+ container.toXml( 0, container.complexity() ) +"\n" );
	}
	public static void main( String argv[]) {
		Sign p = new Sign();
		p.append( new Intention( Intention.thenReply, "hello world" ));
		p.pattern( new Patternette().prefix( new Strings( "hello" )));
		Reply r = new Reply();
		Intention intent = new Intention( Intention.thenReply, "hello world" );
		r = intent.mediate( r );
		audit.log( "r="+ r.toString());
		
		Pattern ts = new Pattern();
		ts.add( new Patternette( "one small step for man", "" ));
		complexityTest( ts );
		
		ts = new Pattern();
		ts.add( new Patternette( "this is a", "test" ));
		complexityTest( ts );
		
		ts = new Pattern();
		ts.add( new Patternette( "this is a test", "x" ).phrasedIs() );
		complexityTest( ts );
		
		ts = new Pattern();
		ts.add( new Patternette( "this is a", "x" ).phrasedIs() );
		complexityTest( ts );
}	}
