package org.enguage.sign.interpretant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.enguage.repertoires.Engine;
import org.enguage.repertoires.Repertoires;
import org.enguage.sign.Config;
import org.enguage.sign.Sign;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.pattern.Frags;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.sign.symbol.when.Moment;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Intention {
	
	private static final String NAME = "Intention";
	private static       Audit audit = new Audit( NAME );

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
	public  static final int N_FINALLY     = 0xff;             // = 255
	
	public Intention( int t, Strings vals ) {this( t, vals.toString());}
	public Intention( int t, String v ) {type=t; value=v; values=new Strings(v);}
	public Intention( Intention in, boolean temp, boolean spatial ) {
		this( in.type(), in.value() );
		temporalIs( temp );
		spatialIs( spatial );
	}
	
	private final int    type;
	public  final int    type() {return type;}
	
	private final String value;
	public  final String value() {return value;}
	
	private final Strings values;
	public  final Strings values() {return values;}
	
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
		
		// TODO: de-hardcode these prefixes in Config.xml
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
		
		if (sa.equals( Config.propagateReplys()) ||
			sa.equals( Config.accumulateCmds()) )
		{
			// don't remove these 'specials', pass on & define in config.xml
			rc = pos ? N_THEN_REPLY : (neg ? N_ELSE_REPLY : N_REPLY);
			
		} else if (Strings.isQuoted( two )) {
			
			boolean found = true;
			if (one.equals(DO_HOOK))
				rc = pos ? N_THEN_DO    : (neg ? N_ELSE_DO    : N_DO);
			
			else  if (one.equals(   RUN_HOOK ))
				rc = pos ? N_THEN_RUN   : (neg ? N_ELSE_RUN   : N_RUN);
			
			else  if (one.equals( REPLY_HOOK ))
				rc = pos ? N_THEN_REPLY : (neg ? N_ELSE_REPLY : N_REPLY);
				
			else  if (one.equals( FNLLY_HOOK ))
				rc = N_FINALLY;
				
			else
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
	private static  Strings strange = new Strings( "" );
	public  static  void    strange( Strings thought ) { strange = thought; }
	public  static  Strings strange(){ return strange; }

	private Reply think( String idea ) {
		// Don't expand, UNIT => cup NOT unit='cup'
		Strings thought = formulate( idea, false );

		if (Audit.allAreOn())
			audit.debug( "Thinking: "+ thought.toString( Strings.CSV ));
		
		Reply r = Repertoires.mediate( new Utterance( thought )); // just recycle existing reply

		// If we've returned FAIL (DNU), we want to continue
		r.type( Reply.stringsToResponseType( new Strings( r.toString()) ));
		if (Reply.Type.E_DNU == r.type()) {
			// put this into reply via Reply.strangeThought()
			audit.error( "Strange thought: I don't understand: '"+ thought +"'" );
			strange( thought );

			// Construct the DNU format
			r.dnu( thought );
		}
		r.doneIs(
				r.type() == Reply.Type.E_SOZ   &&
				Strings.isUCwHyphUs( value ) // critical!
		);
		
		return r;
	}
	
	private String formatAnswer( String rc ) {
		return Moment.valid( rc ) ? // 88888888198888 -> 7pm
				new When( rc ).rep( Config.dnkStr() ).toString()
				: rc.equals( Perform.S_FAIL ) ?
						Config.notOkayStr()
					: rc.equals( Perform.S_SUCCESS ) ?
							Config.okayStr()
						: rc;
	}

	/*
	 * Perform works at the code level to obtain/set an answer.
	 * This was initially the object model, but now includes any code.
	 */
	public  void andFinally( Reply r ) {perform( r, true );}
	private void perform( Reply r ) {perform( r, false );}
	private void perform( Reply r, boolean ignore ) {
		//audit.in( "perform", "value='"+ value +"', ignore="+ (ignore?"yes":"no"))
		Strings cmd = formulate( r.answer(), true ); // DO expand, UNIT => unit='non-null value'
		
		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()==1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attribute( cmd.get(0) ).value());
		
		Strings rawAnswer = Perform.interpret( new Strings( cmd ));
		
		if (!ignore) {
			String method = cmd.get( 1 );
			if (rawAnswer.isEmpty() &&
				(method.equals( "get" ) ||
				 method.equals( "getAttrVal" )) )
			
				r.idk();
				
			else {
				r.answer( formatAnswer( rawAnswer.toString()));
				r.type( Reply.stringToResponseType( formatAnswer( rawAnswer.toString()) ));
			}
		}
		//audit.out( r )
	}
	private Reply reply( Reply r ) {
		audit.in( "reply", "value="+ value +", r="+ (value.equals("")?"SAY SO":r.toString() ));
		
		// Accumulate reply - currently "say this"
		// TODO: incorporate this into "say" ??
		if (value.equals( Config.accumulateCmdStr() )) // say so -- possibly need new intention type?
			Reply.say( r.sayThis());
		
		// propagate reply and return - currently "say so"
		else if (value.equals( Config.propagateReplyStr() ))
			r.doneIs( true ); // just pass out this reply
		
		else // reply "I don't understand" is like an exception?
			r.format( value )
				.type( Reply.stringsToResponseType( values ))
				.doneIs( r.type() != Reply.Type.E_DNU );
		
		audit.out( r.toString() );
		return r;
	}
	private Reply run( Reply r ) {
		return new Commands()
				.command( formulate( r.answer(), false ).toString())
				.injectParameter( r.answer() )
				.run();
	}
	private boolean skip( Reply r ) {return type != N_FINALLY && r.isDone();}
	
	public Reply mediate( Reply r ) {
		if (Audit.allAreOn())
			audit.in( "mediate", typeToString( type ) +"='"+ value +"'"+(skip( r )?" >skipping<":"" ));
		
		switch (type) {
 			case N_THINK:      r = think(   r.answer() ); break;
 			case N_DO: 	           perform( r ); break;
 			case N_RUN:        r = run(     r ); break;
 			case N_REPLY:          reply(   r ); break;
 			case N_ALLOP:      r = Engine.interp( this, r ); break;
 			default:
 				if (r.isFelicitous()) {
		 			switch (type) {
						case N_THEN_THINK: r = think(   r.answer() ); break;
						case N_THEN_DO:        perform( r ); break;
						case N_THEN_RUN:   r = run(     r ); break;
						case N_THEN_REPLY:     reply(   r ); break;
						default: break;
		 			}
	 			} else { // check for is not meh! ?
					switch (type) {
						case N_ELSE_THINK: r = think(   r.answer() ); break;
						case N_ELSE_DO:	       perform( r ); break;
						case N_ELSE_RUN:   r = run(     r ); break;
						case N_ELSE_REPLY:     reply(   r ); break;
						default: break;
		}		}	}

		if (Audit.allAreOn())
			audit.out( r );
		return r;
	}
	/* ------------------------------------------------------------------------
	 * Printy bits
	 */
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
			audit.debug( typeToString( in.type )  +"='"+ in.value +"'" );
		}
		return r;
	}
	public static void main( String[] argv ) {
		Reply r = new Reply().answer( "world" );
		r.type( Reply.stringToResponseType( "world" ));
		audit.debug( new Intention( N_THEN_REPLY, "hello ..." ).mediate( r ).toString() );
		
		Audit.title( "trad autopoiesis... add to a list and then add that list" );
		r = new Reply();
		ArrayList<Intention> a = new ArrayList<>();
//		a.add( new Intention( N_CREATE, THINK      +" \"a PATTERN z\" \"one two three four\""   ));
//		a.add( new Intention( N_APPEND, ELSE_REPLY +" \"two three four\""   ));
//		a.add( new Intention( N_APPEND, REPLY      +" \"three four\"" ));
		test( r, a );
		audit.debug( Repertoires.signs().toString() );
		audit.debug( r.toString());
		
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
		r.answer( Config.yes().toString() );

		
		audit.debug( Repertoires.signs().toString() );
		audit.debug( r.toString());
}	}
