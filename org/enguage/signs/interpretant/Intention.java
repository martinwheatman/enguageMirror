package org.enguage.signs.interpretant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.enguage.repertoires.Engine;
import org.enguage.repertoires.Repertoires;
import org.enguage.signs.Sign;
import org.enguage.signs.object.Variable;
import org.enguage.signs.object.sofa.Perform;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.signs.symbol.when.Moment;
import org.enguage.signs.symbol.when.When;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;
import org.enguage.util.sys.Shell;

public class Intention {
	
	private static final String NAME = "Intention";
	private static Audit       audit = new Audit( NAME );

	public  static final String  AUTOP_STR = "autopoiesis";
	
	public  static final String   RUN_HOOK = "run";
	public  static final String    DO_HOOK = "perform";
	public  static final String REPLY_HOOK = "reply";
	public  static final String FNLLY_HOOK = "finally";
	

	public  static final String      UNDEF = "u";
	public  static final String        NEW = "w";
	public  static final String     APPEND = "a";
	public  static final String    PREPEND = "p";
	
	private static final String        POS = "+";
	private static final String        NEG = "-";
	
	public  static final String      THINK = "t";
	public  static final String THEN_THINK = THINK + POS;
	public  static final String ELSE_THINK = THINK + NEG;
	
	public  static final String         DO = "d";
	public  static final String    THEN_DO = DO    + POS;
	public  static final String    ELSE_DO = DO    + NEG;

	public  static final String        RUN = "n";
	public  static final String   THEN_RUN = RUN   + POS;
	public  static final String   ELSE_RUN = RUN   + NEG;
	
	public  static final String      REPLY = "r";
	public  static final String THEN_REPLY = REPLY + POS;
	public  static final String ELSE_REPLY = REPLY + NEG;
	
	// 'finally' intentions are run irrespective of outcome
	public  static final String   FINALLY = "f";
	
	private static final int N_THEN       = 0x01; // 0000 0001
	private static final int N_ELSE       = 0x03; // 0000 0011
	
	public  static final int N_THINK      = 0x00; // 0000 00xx
	public  static final int N_DO         = 0x04; // 0000 01xx
	public  static final int N_RUN        = 0x08; // 0000 10xx
	public  static final int N_REPLY      = 0x0c; // 0000 11xx
	
	// written types
	private static final int UNDEFINED     = -1;

	public  static final int N_THEN_THINK  = N_THEN | N_THINK; // =   1
	public  static final int N_ELSE_THINK  = N_ELSE | N_THINK; // =   3
	public  static final int N_THEN_DO     = N_THEN | N_DO;    // =   4
	public  static final int N_ELSE_DO     = N_ELSE | N_DO;    // =   7
	public  static final int N_THEN_RUN    = N_THEN | N_RUN;   // =   9
	public  static final int N_ELSE_RUN    = N_ELSE | N_RUN;   // =  11
	public  static final int N_THEN_REPLY  = N_THEN | N_REPLY; // =  13
	public  static final int N_ELSE_REPLY  = N_ELSE | N_REPLY; // =  15
	
	public  static final int N_ALLOP       = 0x10;             // =  16
	private static final int N_AUTOP       = 0x11;             // =  17
	private static final int N_FINALLY     = 0xff;             // = 255
	
	public Intention( int t, Strings vals ) {this( t, vals.toString());}
	public Intention( int t, String v ) {type=t; value=v; values=new Strings(v);}
	public Intention( int t, int intId, String v ) {id = intId; type=t; value=v; values=new Strings(v);}
	public Intention( Intention in, boolean temp, boolean spatial ) {
		this( in.type(), in.value() );
		temporalIs( temp );
		spatialIs( spatial );
	}
	
	private int id = UNDEFINED;
	public  int id() {return id;}
	
	private int    type;
	public  int    type() {return type;}
	
	private String value;
	public  String value() {return value;}
	
	private Strings values;
	public  Strings values() {return values;}
	
	public static String typeToString( int type ) {
		switch (type) {
			case N_REPLY      : return REPLY;
			case N_THEN_REPLY : return THEN_REPLY;
			case N_ELSE_REPLY : return ELSE_REPLY;
			case N_THINK      : return THINK;
			case N_THEN_THINK : return THEN_THINK;
			case N_ELSE_THINK : return ELSE_THINK;
			case N_DO         : return DO;
			case N_THEN_DO    : return THEN_DO;
			case N_ELSE_DO    : return ELSE_DO;
			case N_RUN        : return RUN;
			case N_THEN_RUN   : return THEN_RUN;
			case N_ELSE_RUN   : return ELSE_RUN;
			case N_ALLOP      : return Engine.NAME;
			case N_AUTOP      : return AUTOP_STR;
			case N_FINALLY    : return FINALLY;
			default:
				audit.FATAL( "Intention: returning undefined for: "+ type );
				return UNDEF;
	}	}

	public static int getType(Strings sa) {
		// [ "if", "so", ",", "think", "something" ] => 
		//   N_THEN_THINK + [ "think", "something" ]
		// [ "if", "not", ",", "say", "so" ] =>
		//   N_ELSE_REPLY + []
		int rc = UNDEFINED;
		
		int len = sa.size();
		String one = len>0?sa.get(0):"";
		String two = len>1?sa.get(1):"";
		String thr = len>2?sa.get(2):"";
		
		boolean neg = false;
		boolean pos = false;
		if (one.equals( "if" ) && thr.equals( "," )) {
			// Looking for: 'if' [ 'so' | 'not'] ','
			neg = two.equals( "not" ); // not/so
			pos = two.equals(  "so" ); // not/so
			if (pos || neg) {
				sa.remove(0); // if
				sa.remove(0); // not/so
				sa.remove(0); // ,
				len = sa.size();
				one = len>0?sa.get(0):"";
				two = len>1?sa.get(1):"";
		}	}
		
		if (len == 2          &&
			one.equals("say") &&
			two.equals("so"))
		{
			rc = pos ? N_THEN_REPLY : (neg ? N_ELSE_REPLY : N_REPLY);
			sa.remove( 0 ); // say
			sa.remove( 0 ); // so
			
		} else if (Strings.isQuoted( two )) {
			
			boolean found = true;
			if (one.equals(DO_HOOK)) {
				rc = pos ? N_THEN_DO    : (neg ? N_ELSE_DO    : N_DO);
			
			} else  if (one.equals(   RUN_HOOK )) {
				rc = pos ? N_THEN_RUN   : (neg ? N_ELSE_RUN   : N_RUN);
			
			} else  if (one.equals( REPLY_HOOK )) {
				rc = pos ? N_THEN_REPLY : (neg ? N_ELSE_REPLY : N_REPLY);
				
			} else  if (one.equals( FNLLY_HOOK )) {
				rc = N_FINALLY;
				
			} else
				found = false;
			
			if (found) {
				sa.remove(0); // ["perform" | "run" | "reply" | "finally" ]
				sa.set( 0, Strings.trim( sa.get(0), '"' ));
		}	}
		
		if (rc == UNDEFINED) {
			rc = pos ? N_THEN_THINK : neg ? N_ELSE_THINK : N_THINK;
		}
		
		return rc;
	}

	
	private boolean   temporal = false;
	public  boolean   isTemporal() {return temporal;}
	public  Intention temporalIs( boolean b ) {temporal = b; return this;}

	private boolean   spatial = false;
	public  boolean   isSpatial() {return spatial;}
	public  Intention spatialIs( boolean s ) {spatial = s; return this;}

	private static String concept = "";
	public  static void   concept( String name ) { concept = name; }
	public  static String concept() { return concept; }

	private Strings formulate( String answer, boolean expand ) {
		return 	Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
					Context.deref( // X => "coffee", singular-x="80s" -> "80"
						new Strings( values )
								.replace( Strings.ellipsis, answer ),
						expand
				)	);
	}
	private Reply think( Reply r ) {
		//audit.in( "think", "value='"+ value +"', previous='"+ r.a.toString() +"'" )
		Strings thought  = formulate( r.a.toString(), false ); // dont expand, UNIT => cup NOT unit='cup'

		if (Audit.allAreOn()) audit.debug( "Thinking: "+ thought.toString( Strings.CSV ));
		Reply tmpr = Repertoires.mediate( new Utterance( thought )); // just recycle existing reply
		
		if (r.a.isAppending())
			r.a.add( tmpr.a.toString() );
		else
			r = tmpr;
		
		// If we've returned FAIL (DNU), we want to continue
		return r.response( new Strings( r.toString()) )
		        .conclude( thought.toString() )
		        .doneIs( Strings.isUCwHyphUs( value ) // critical!
		                 && r.response() == Response.N_FAIL );
	}
	
	private String formatAnswer( String rc ) {
		return Moment.valid( rc ) ? // 88888888198888 -> 7pm
				new When( rc ).rep( Response.dnkStr() ).toString()
				: rc.equals( Shell.FAIL ) ?
					Response.failureStr()
					: rc.equals( Shell.SUCCESS ) ?
						Response.successStr()
						: rc;
	}

	private Reply andFinally( Reply r ) {return perform( r, true );}
	private Reply perform( Reply r ) {return perform( r, false );}
	private Reply perform( Reply r, boolean ignore ) {
		//audit.in( "perform", "value='"+ value +"', ignore="+ (ignore?"yes":"no"))
		Strings cmd = formulate( r.a.toString(), true ); // DO expand, UNIT => unit='non-null value'
		
		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()==1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attribute( cmd.get(0) ).value());
		
		Strings rawAnswer = Perform.interpret( new Strings( cmd ));
		if (!ignore) {
			String method = cmd.get( 1 );
			if (rawAnswer.isEmpty() &&
				(method.equals( "get" ) ||
				 method.equals( "getAttrVal" )) )
			{
				r.format( Response.dnkStr());
				r.response( Response.N_DNK );
			} else
				r.answer( formatAnswer( rawAnswer.toString()));
		}

		return r; //(Reply) audit.out( r )//
	}
	private Reply reply( Reply reply ) {
		return reply
				.format( value.equals( "" ) ? reply.toString() : value )
				.response( values )
				.doneIs( reply.response() != Response.N_DNU ); // so reply "I don't understand" is like an exception?
	}
	private Reply run( Reply r ) {
		return new Commands( formulate( r.a.toString(), false ).toString())
				.run( r.a.toString() );
	}
	public Reply mediate( Reply r ) {
		if (Audit.allAreOn())
			audit.in( "mediate", typeToString( type ) +"='"+ value +"' fel="+ r.felicitous() );
		
		if (type == N_FINALLY)
			andFinally( r );

		else if (r.isDone()) {
			if (Audit.allAreOn())
				audit.debug( "skipping >"+ value +"< reply already found" );
		
		} else if (r.felicitous()) {
 			switch (type) {
				case N_THEN_THINK: r = think(   r ); break;
				case N_THEN_DO:        perform( r ); break;
				case N_THEN_RUN:   r = run(     r ); break;
				case N_THEN_REPLY: r = reply(   r ); break;
				case N_THINK:      r = think(   r ); break;
				case N_DO: 	           perform( r ); break;
				case N_RUN:        r = run(     r ); break;
				case N_REPLY:      r = reply(   r ); break;
				default: break;
			}
		} else {
			switch (type) {
				case N_ELSE_THINK: r = think(   r ); break;
				case N_ELSE_DO:	       perform( r ); break;
				case N_ELSE_RUN:   r = run(     r ); break;
				case N_ELSE_REPLY: r = reply(   r ); break;
				case N_THINK:      r = think(   r ); break;
				case N_DO:             perform( r ); break;
				case N_RUN:        r = run(     r ); break;
				case N_REPLY:      r = reply(   r ); break;
				default: break;
		}	}

		if (Audit.allAreOn())
			audit.out( r );
		return r;
	}
	public String toString() {
		switch (type) {
			case N_THINK      : return value;
			case N_DO         : return "perform \""+ value +"\"";
			case N_RUN        : return "run \""+ value +"\"";
			case N_REPLY      : return "reply \""+ value +"\"";
			case N_THEN_THINK : return "if so, "+  value;
			case N_THEN_DO    : return "if so, perform \""+ value +"\"";
			case N_THEN_RUN   : return "if so, run \""+ value +"\"";
			case N_THEN_REPLY : return "if so, reply \""+ value +"\"";
			case N_ELSE_THINK : return "if not, "+ value;
			case N_ELSE_DO    : return "if not, perform \""+ value +"\"";
			case N_ELSE_RUN   : return "if not, run \""+ value +"\"";
			case N_ELSE_REPLY : return "if not, reply \""+ value +"\"";
			case N_FINALLY    : return "finally \""+ value +"\"";
			default : return Attribute.asString( typeToString( type ), value() );
	}	}
	/*
	 * Test code...
	 */
	public static Reply test(Reply r, List<Intention> intents) {
		Iterator<Intention> ins = intents.iterator();
		while (!r.isDone() && ins.hasNext()) {
			Intention in = ins.next();
			Audit.log( typeToString( in.type )  +"='"+ in.value +"'" );
		}
		return r;
	}
	public static void main( String[] argv ) {
		Reply r = new Reply().answer( "world" );
		Audit.log( new Intention( N_THEN_REPLY, "hello ..." ).mediate( r ).toString() );
		
		Audit.title( "trad autopoiesis... add to a list and then add that list" );
		r = new Reply();
		ArrayList<Intention> a = new ArrayList<>();
//		a.add( new Intention( N_CREATE, THINK      +" \"a PATTERN z\" \"one two three four\""   ));
//		a.add( new Intention( N_APPEND, ELSE_REPLY +" \"two three four\""   ));
//		a.add( new Intention( N_APPEND, REPLY      +" \"three four\"" ));
		test( r, a );
		Audit.log( Repertoires.signs().toString() );
		Audit.log( r.toString());
		
		Audit.title( "sign self-build II... add pairs of attributes" );
		// now built like this...
		// To PATTERN reply TYPICAL REPLY
		r = new Reply();
		Sign.latest(
				new Sign()
					.pattern( new Frags( "c variable pattern z" ))
					.concept( concept() )
		);
		String reply = "three four";
		Sign.latest().append( Intention.N_THEN_REPLY, reply );
		// ...This implies COND
		Sign.latest().insert( Intentions.Insertion.PREPEND, new Intention( N_THEN_THINK, "one two three four" ));
		// ...if not reply EXECP REPLY
		Sign.latest().insert( Intentions.Insertion.HEADER, new Intention( N_ELSE_REPLY, "two three four" ));
		
		Repertoires.signs().insert( Sign.latest() );
		r.answer( Response.yes().toString() );

		
		Audit.log( Repertoires.signs().toString() );
		Audit.log( r.toString());
}	}
