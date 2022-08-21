package org.enguage.signs.interpretant;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.objects.Variable;
import org.enguage.objects.space.Sofa;
import org.enguage.repertoire.Repertoire;
import org.enguage.signs.Sign;
import org.enguage.signs.vehicle.Utterance;
import org.enguage.signs.vehicle.pattern.Pattern;
import org.enguage.signs.vehicle.reply.Reply;
import org.enguage.signs.vehicle.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;

public class Intention {
	
	private static Audit audit = new Audit( "Intention" );
	
	public static final String UNDEF      = "u";
	public static final String NEW        = "w";
	public static final String APPEND     = "a";
	public static final String PREPEND    = "p";
	
	public static final String      REPLY = "r";
	public static final String ELSE_REPLY = "R";
	public static final String      THINK = "t";
	public static final String ELSE_THINK = "T";
	public static final String      DO    = "d";
	public static final String ELSE_DO    = "D";
	public static final String      RUN   = "n";
	public static final String ELSE_RUN   = "N";
	public static final String FINALLY    = "f";
	public static final int _then         = 0x00; // 0000
	public static final int _else         = 0x01; // 0001
	public static final int _think        = 0x00; // 0000
	public static final int _do           = 0x02; // 0010
	public static final int _run          = 0x04; // 0100 -- TODO: combine with tcpip and do!!!
	public static final int _say          = 0x06; // 0110
	
	public static final int undef         = -1;

	public static final int thenThink     = _then | _think; // =  0
	public static final int elseThink     = _else | _think; // =  1
	public static final int thenDo        = _then | _do;    // =  2
	public static final int elseDo        = _else | _do;    // =  3
	public static final int thenRun       = _then | _run;   // =  4
	public static final int elseRun       = _else | _run;   // =  5
	public static final int thenReply     = _then | _say;   // =  6
	public static final int elseReply     = _else | _say;   // =  7
	public static final int allop         =  0x8;           // =  8
	public static final int autop         =  0x9;           // =  9
	public static final int thenFinally   =  0xf; // 1111      = 16

	public static final int  create       =  0xa;
	public static final int  prepend      =  0xb;
	public static final int  append       =  0xc;
	public static final int  headAppend   =  0xd;
	public static final int  head         =  0xe;

	public Intention( int t, Strings vals ) { this( t, vals.toString()); }
	public Intention( int t, String v ) { type=t; value=v; values=new Strings(v);}
	public Intention( Intention in, boolean temp, boolean spatial ) {
		this( in.type(), in.value() );
		temporalIs( temp );
		spatialIs( spatial );
	}
	
	protected final int    type;
	public          int    type() {return type;}
	
	protected final String value;
	public          String value() {return value;}
	
	protected final Strings values;
	public          Strings values() {return values;}
	
	public static String typeToString( int type ) {
		switch (type) {
			case thenReply   : return REPLY;
			case elseReply   : return ELSE_REPLY;
			case thenThink   : return THINK;
			case elseThink   : return ELSE_THINK;
			case thenDo      : return DO;
			case elseDo      : return ELSE_DO;
			case thenRun     : return RUN;
			case elseRun     : return ELSE_RUN;
			case allop       : return Repertoire.ALLOP;
			case autop       : return Repertoire.AUTOP;
			case create      : return NEW;
			case prepend     : return PREPEND;
			case append      : return APPEND;
			case thenFinally : return FINALLY;
			default:
				audit.FATAL( "Intention: still returning undefined" );
				return UNDEF;
	}	}
	
	public static int nameToType( String name ) {
		     if (name.equals( REPLY            )) return thenReply;
		else if (name.equals( ELSE_REPLY       )) return elseReply;
		else if (name.equals( THINK            )) return thenThink;
		else if (name.equals( ELSE_THINK       )) return elseThink;
		else if (name.equals( DO               )) return thenDo; 
		else if (name.equals( ELSE_DO          )) return elseDo; 
		else if (name.equals( RUN              )) return thenRun; 
		else if (name.equals( ELSE_RUN         )) return elseRun;
		else if (name.equals( FINALLY          )) return thenFinally;
		else if (name.equals( Repertoire.ALLOP )) return allop;
		else if (name.equals( Repertoire.AUTOP )) return autop;
		else if (name.equals( NEW              )) return create;
		else if (name.equals( APPEND           )) return append;
		else if (name.equals( PREPEND          )) return prepend;
		else {
			audit.FATAL( "typing undef" );
			return undef;
	}	}
	
	public  boolean   temporal = false;
	public  boolean   isTemporal() {return temporal;}
	public  Intention temporalIs( boolean b ) {temporal = b; return this;}

	public  boolean   spatial = false;
	public  boolean   isSpatial() {return spatial;}
	public  Intention spatialIs( boolean s ) {spatial = s; return this;}

	private static Sign latest = null;
	
	private static String concept = "";
	public  static void   concept( String name ) { concept = name; }
	public  static String concept() { return concept; }

	public String create() {
		Strings sa      = Context.deref( new Strings( values ));
		String  attr    = sa.remove( 0 ),
		        pattern = sa.remove( 0 ),
			    val     = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
			Repertoire.signs.insert(
					latest = new Sign()
							.pattern( new Pattern( new Strings( Strings.trim( pattern, Strings.DOUBLE_QUOTE ))) )
							.concept( concept() )
							.append( new Intention( Intention.nameToType( attr ), val )));
		return "ok";
	}
	public String append() {
		if (null == latest)
			audit.ERROR( "adding to sign before creation" );
		else {
			Strings  sa = Context.deref( new Strings( values ));
			String attr = sa.remove( 0 ),
			       val  = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
			latest.append( new Intention( nameToType(  attr ), val ));
		}
		return "ok";
	}
	public String prepend() {
		if (null == latest)
			audit.ERROR( "adding to sign before creation" );
		else {
			Strings  sa = Context.deref( new Strings( values ));
			String attr = sa.remove( 0 ),
			       val  = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
			latest.insert( 0, new Intention( nameToType( attr ), val ));
		}
		return "ok";
	}
	private Strings formulate( String answer, boolean expand ) {
		return 	Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
					Context.deref( // X => "coffee", singular-x="80s" -> "80"
						new Strings( values )
								.replace( Strings.ellipsis, answer ),
						expand
				)	);
	}
	private Reply think( Reply r ) {
		//audit.in( "think", "value='"+ value +"', previous='"+ r.a.toString() +"'" );
		Strings thought  = formulate( r.a.toString(), false ); // dont expand, UNIT => cup NOT unit='cup'

		if (Audit.allAreOn()) audit.debug( "Thinking: "+ thought.toString( Strings.CSV ));
		Reply tmpr = Repertoire.mediate( new Utterance( thought, new Strings(r.a.toString()) )); // just recycle existing reply
		
		if (r.a.isAppending())
			r.a.add( tmpr.a.toString() );
		else
			r = tmpr;
		
		// If we've returned FAIL (DNU), we want to continue
		return r.response( new Strings( r.toString()) )
		        .conclude( thought.toString() )
		        .doneIs( Strings.isUCwHyphUs( value ) // critical!
		                 && r.response() == Response.FAIL );
	}
	
	private Reply perform( Reply r ) { return perform( r, false ); }
	private Reply andFinally( Reply r ) { return perform( r, true ); }
	
	private Reply perform( Reply r, boolean ignore ) {
		//audit.in( "perform", "value='"+ value +"', ignore="+ (ignore?"yes":"no"));
		String answer = r.a.toString();
		Strings cmd = formulate( answer, true ); // DO expand, UNIT => unit='non-null value'
		
		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()==1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attribute( cmd.get(0) ).value());
		
		Strings rawAnswer = new Sofa().doCall( new Strings( cmd ));
		if (!ignore) r.rawAnswer( rawAnswer.toString(), cmd.get( 1 ));

		return r; //(Reply) audit.out( r ); //
	}
	private Reply reply( Reply reply ) {
		return reply.format( value.equals( "" ) ? reply.toString() : value )
		        .doneIs( reply.response() != Response.DNU )
		        .response( values );
	}
	private Reply run( Reply r ) {
		return new Commands( formulate( r.a.toString(), false ).toString())
				.run( r );
	}
	public Reply mediate( Reply r ) {
		if (Audit.allAreOn())
			audit.in( "mediate", typeToString( type ) +"='"+ value +"'" );
		
		if (type == thenFinally)
			andFinally( r );

		else if (r.isDone()) {
			if (Audit.allAreOn())
				audit.debug( "skipping >"+ value +"< reply already found" );
		
		} else if (r.felicitous())
 			switch (type) {
				case thenThink:	r = think(   r ); break;
				case thenDo: 	r = perform( r ); break;
				case thenRun:	r = run(     r ); break;
				case thenReply:	r = reply(   r ); break;
			}
 		else
			switch (type) {
				case elseThink: r = think(   r ); break;
				case elseDo:	r = perform( r ); break;
				case elseRun:	r = run(     r ); break;
				case elseReply:	r = reply(   r ); break;
			}

		if (Audit.allAreOn())
			audit.out( r );
		return r;
	}
	public String toString() {
		switch (type) {
			case thenReply  : return         "reply \""+   value +"\"";
			case elseReply  : return "if not, reply \""+   value +"\"";
			case thenDo     : return         "perform \""+ value +"\"";
			case elseDo     : return "if not, perform \""+ value +"\"";
			case thenRun    : return         "run \""+     value +"\"";
			case elseRun    : return "if not, run \""+     value +"\"";
			case thenThink  : return                       value;
			case elseThink  : return "if not, "+           value;
			case thenFinally: return "finally, "+          value;
			default : return Attribute.asString( typeToString( type ), value() );
	}	}
	/*
	 * Test code...
	 */
	public String autopoiesis() {
		switch (type) {
			case create : create();  break;
			case append : append();  break;
			case prepend: prepend(); break;
		}
		return "ok";
	}
	public static Reply test(Reply r, ArrayList<Intention> intents) {
		Iterator<Intention> ins = intents.iterator();
		while (!r.isDone() && ins.hasNext()) {
			Intention in = ins.next();
			Audit.log( typeToString( in.type )  +"='"+ in.value +"'" );
			r.answer( new Intention( in, false, false ).autopoiesis() );
		}
		return r;
	}
	public static void main( String argv[]) {
		Reply r = new Reply().answer( "world" );
		Audit.log( new Intention( thenReply, "hello ..." ).mediate( r ).toString() );
		
		audit.title( "trad autopoiesis... add to a list and then add that list" );
		r = new Reply();
		ArrayList<Intention> a = new ArrayList<Intention>();
		a.add( new Intention( create, THINK      +" \"a PATTERN z\" \"one two three four\""   ));
		a.add( new Intention( append, ELSE_REPLY +" \"two three four\""   ));
		a.add( new Intention( append, REPLY      +" \"three four\"" ));
		test( r, a );
		Audit.log( Repertoire.signs.toString() );
		Audit.log( r.toString());
		
		audit.title( "sign self-build II... add pairs of attributes" );
		// now built like this...
		// To PATTERN reply TYPICAL REPLY
		r = new Reply();
		latest = new Sign()
				.pattern( new Pattern( "c variable pattern z" ))
				.concept( concept() );
		String reply = "three four";
		latest.append( new Intention( thenReply, reply ));
		// ...This implies COND
		latest.insert( 0, new Intention( thenThink, "one two three four" ));
		// ...if not reply EXECP REPLY
		latest.insert( 1, new Intention( elseReply, "two three four" ));
		
		Repertoire.signs.insert( latest );
		r.answer( Response.yes().toString() );

		
		Audit.log( Repertoire.signs.toString() );
		Audit.log( r.toString());
}	}
