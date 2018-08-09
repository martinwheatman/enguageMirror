package org.enguage.sgn.itn;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.obj.Variable;
import org.enguage.obj.space.Sofa;
import org.enguage.sgn.Sign;
import org.enguage.sgn.ctx.Context;
import org.enguage.sgn.ptt.Pattern;
import org.enguage.sgn.rep.Repertoire;
import org.enguage.util.Attribute;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Proc;
import org.enguage.util.Strings;
import org.enguage.veh.Utterance;
import org.enguage.veh.reply.Reply;
import org.enguage.veh.where.Where;

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

	public Intention( int type, String value, int intnt ) { this( type, value ); intent = intnt;}	
	public Intention( int nm, String val ) { type=nm; value = val; }
	public Intention( Intention in, boolean temp, boolean spatial ) {
		this( in.type(), in.value() );
		temporalIs( temp );
		spatialIs( spatial );
	}
	
	protected int    type  = 0;
	public    int    type() { return type; }
	
	protected String value = "";
	public    String value() { return value; }
	
	static public String typeToString( int type ) {
		switch (type) {
			case thenReply : return REPLY;
			case elseReply : return ELSE_REPLY;
			case thenThink : return THINK;
			case elseThink : return ELSE_THINK;
			case thenDo    : return DO;
			case elseDo    : return ELSE_DO;
			case thenRun   : return RUN;
			case elseRun   : return ELSE_RUN;
			case allop     : return Repertoire.ALLOP;
			case autop     : return Repertoire.AUTOP;
			case create    : return NEW;
			case prepend   : return PREPEND;
			case append    : return APPEND;
			case thenFinally : return FINALLY;
			default:
				audit.FATAL( "Intention: still returning undefined" );
				return UNDEF;
	}	}
	
	static public int nameToType( String name ) {
		if ( name.equals( REPLY ))
			return thenReply;
		else if ( name.equals( ELSE_REPLY ))
			return elseReply;
		else if ( name.equals( THINK ))
			return thenThink;
		else if ( name.equals( ELSE_THINK ))
			return elseThink;
		else if ( name.equals( DO))
			return thenDo; 
		else if ( name.equals( ELSE_DO ))
			return elseDo; 
		else if ( name.equals( RUN ))
			return thenRun; 
		else if ( name.equals( ELSE_RUN ))
			return elseRun;
		else if ( name.equals( FINALLY ))
			return thenFinally;
		else if ( name.equals( Repertoire.ALLOP ))
			return allop;
		else if ( name.equals( Repertoire.AUTOP ))
			return autop;
		else if ( name.equals( NEW ))
			return create;
		else if ( name.equals( APPEND ))
			return append;
		else if ( name.equals( PREPEND ))
			return prepend;
		else {
			audit.FATAL( "typing undef" );
			return undef;
	}	}
	
	public boolean   temporal = false;
	public boolean   isTemporal() { return temporal; }
	public Intention temporalIs( boolean b ) { temporal = b; return this; }

	public boolean   spatial = false;
	public boolean   isSpatial() { return spatial; }
	public Intention spatialIs( boolean s ) { spatial = s; return this; }

	private int intent = undef;
	public  int intent() { return intent;}
	
	static private Sign s = null;
	static public  void printSign() { audit.LOG( "Autop().printSign:\n"+ s.pattern().toXml()); }
	
	static private String concept = "";
	static public    void concept( String name ) { concept = name; }
	static public  String concept() { return concept; }
	
	public Reply autopoiesis( Reply r ) {
		audit.in( "autopoiesis", "NAME="+ Repertoire.AUTOP +", value="+ value +", "+ Context.valueOf());
		Strings sa = Context.deref( new Strings( value ));
		
		// needs to switch on type (intent)
		if (intent == create ) { // manually adding a sign
			
			//audit.debug( "autop: creating new sign: ["+ value +"]");
			Repertoire.signs.insert(
				s = new Sign()
					.pattern( new Pattern( value )) // manual Pattern
					.concept( Repertoire.AUTOPOIETIC )
			);
			
		} else if (intent == append ) { // add intent to end of interpretant
			if (null != s) s.append( new Intention( type, Pattern.toPattern( value ).toString()));
			
		} else if (intent == prepend ) { // add intent to start of interpretant
			if (null != s) s.prepend( new Intention( type, Pattern.toPattern( value ).toString() ));
			
		} else if (intent == headAppend ) { // add intent to first but one...  
			if (null != s) s.insert( 1, new Intention( type, Pattern.toPattern( value ).toString() ));
			
		// following these are trad. autopoiesis...this need updating as above!!!
		} else if (type == append || type == prepend ) {
			if (null == s)
				// this should return DNU...
				audit.ERROR( "adding to non existent concept: ["+ sa.toString( Strings.CSV )+"]");
			else {
				String attr = sa.get( 0 ),
					    val = Strings.trim( sa.get( 1 ), Strings.DOUBLE_QUOTE );
				if (type == append )
					s.append( new Intention( nameToType(  attr ), val ));
				else
					s.prepend( new Intention( nameToType( attr ), val ));
			}
			
		} else if (type == create ) { // autopoeisis?
			String attr    = sa.get( 0 ),
			       pattern = sa.get( 1 ),
			       val     = Strings.trim( sa.get( 2 ), Strings.DOUBLE_QUOTE );
			/* TODO: need to differentiate between
			 * "X is X" and "X is Y" -- same shape, different usage.
			 * At least need to avoid this (spot when "X is X" happens)
			 */
			audit.debug( "Adding >"+ value +"< ["+ sa.toString( Strings.CSV )+"]");
			if ( pattern.equals( "help" ))
				s.help( val ); // add: help="text" to cached sign
			else // create then add a new cached sign into the list of signs
				Repertoire.signs.insert(
					s = new Sign()
						.pattern( new Pattern( new Strings( Strings.trim( pattern, Strings.DOUBLE_QUOTE ))) )
						.concept( concept() )
						.append( new Intention( Intention.nameToType( attr ), val )));
		}
		return (Reply) audit.out( r.answer( Reply.yes().toString() ));
	}
	private Strings formulate( String answer, boolean expand ) {
		return 	Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
					Context.deref( // X => "coffee", singular-x="80s" -> "80"
						new Strings( value ).replace(
								Strings.ellipsis,
								answer ),
						expand
				)	);
	}
	private Reply think( Reply r ) {
		audit.in( "think", "value='"+ value +"', previous='"+ r.a.toString() +"'" );
		Strings thought = formulate( r.a.toString(), false ); // dont expand, UNIT => cup NOT unit='cup'
		audit.debug( "Thinking: "+ thought.toString( Strings.CSV ));
		
		Reply tmpr = Repertoire.interpret( new Utterance( thought, new Strings(r.a.toString()) )); // just recycle existing reply
		
		if (r.a.isAppending())
			r.a.add( tmpr.a.toString() );
		else
			r = tmpr;
		
		r.type( new Strings( r.toString()) )
		 .conclude( thought );
		
		// If we've returned DNU, we want to continue
		r.doneIs( false );

		return (Reply) audit.out( r );
	}
	
	private Reply perform( Reply r ) { return perform( r, false ); }
	private Reply andFinally( Reply r ) { return perform( r, true ); }
	
	private Reply perform( Reply r, boolean ignore ) {
		audit.in( "perform", "value='"+ value +"', ignore="+ (ignore?"yes":"no"));
		String answer = r.a.toString();
		Strings cmd = formulate( answer, true ); // DO expand, UNIT => unit='non-null value'
		
		{ // Add tempro/spatial awareness if it has been added. 
			String when = Context.get( "when" );
			if (!when.equals(""))
				cmd.append( Attribute.asString( "WHEN", when ) );
			String locator = Context.get( Where.LOCTR );
			if (!locator.equals("")) {
				String location = Context.get( Where.LOCTN );
				if (!location.equals("")) {
					if (cmd.size() < 5) cmd.append( ":" ); // TODO: fix SOFA & scripts to accept n='v'
					cmd.append( Attribute.asString( Where.LOCTR,  locator  ));
					cmd.append( Attribute.asString( Where.LOCTN, location ));
		}	}	}

		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()== 1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attributes( cmd.get(0) ).get( "args" ));
		
		audit.debug( "performing: "+ cmd.toString());
		String rawAnswer = new Sofa().doCall( new Strings( cmd ));
		if (!ignore) r.rawAnswer( rawAnswer, cmd.get( 1 ));

		return (Reply) audit.out( r );
	}
	private Reply reply( Reply r ) {
		audit.in( "reply", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		/* TODO: 
		 * if reply on its own, return reply from previous/inner reply -- imagination!
		 */
		r.format( value.equals( "" ) ? Reply.success() : value );
		r.type( new Strings( value ));
		r.doneIs( r.type() != Reply.DNU && r.type() != Reply.FAIL );
		return (Reply) audit.out( r );
	}
	
	public Reply mediate( Reply r ) {
		audit.in( "mediate", typeToString( type ) +"='"+ value +"'" );
		
		if (type == thenFinally)
			andFinally( r );

		else if (r.isDone())
			audit.debug( "skipping >"+ value +"< reply already found" );
		
		else if (r.negative())
			switch (type) {
				case elseThink: r = think( r );					break;
				case elseDo:	r = perform( r );				break;
				case elseRun:	r = new Proc( value ).run( r ); break;
				case elseReply:	r = reply( r ); // break;
			}
 		else // train of thought is neutral/positive
			switch (type) {
				case thenThink:	r = think( r );					break;
				case thenDo: 	r = perform( r );				break;
				case thenRun:	r = new Proc( value ).run( r );	break;
				case thenReply:	r = reply( r ); // break;
			}
		return (Reply) audit.out( r );
	}
	// ---
	public static Reply test(Reply r, ArrayList<Intention> intents) {
		Iterator<Intention> ins = intents.iterator();
		while (!r.isDone() && ins.hasNext()) {
			Intention in = ins.next();
			audit.log( typeToString( in.type )  +"='"+ in.value +"'" );
			r = new Intention( in, false, false ).autopoiesis( r );
		}
		return r;
	}
	public static void main( String argv[]) {
		Reply r = new Reply().answer( "world" );
		audit.log( new Intention( thenReply, "hello ..." ).mediate( r ).toString() );
		//Audit.allOn();
		//audit.trace( true );
		
		audit.title( "trad autopoiesis... add to a list and then add that list" );
		r = new Reply();
		ArrayList<Intention> a = new ArrayList<Intention>();
		a.add( new Intention( create, THINK      +" \"a PATTERN z\" \"one two three four\""   ));
		a.add( new Intention( append, ELSE_REPLY +" \"two three four\""   ));
		a.add( new Intention( append, REPLY      +" \"three four\"" ));
		test( r, a );
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
		
		audit.title( "manual sign creation... add each intention individually" );
		r = new Reply();
		r = new Intention( create,    "b variable pattern z", create ).autopoiesis( r );
		r = new Intention( thenThink, "one two three four"  , append ).autopoiesis( r );
		r = new Intention( elseReply, "two three four"      , append ).autopoiesis( r );
		r = new Intention( thenReply, "three four"          , append ).autopoiesis( r );
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
		
		
		audit.title( "sign self-build II... add pairs of attributes" );
		// now built like this...
		// To PATTERN reply TYPICAL REPLY
		r = new Reply();
		s = new Sign()
				.pattern( new Pattern( "c variable pattern z" ))
				.concept( concept() );
		String reply = "three four";
		s.append( new Intention( thenReply, reply ));
		// ...This implies COND
		s.prepend( new Intention( thenThink, "one two three four" ));
		// ...if not reply EXECP REPLY
		s.insert( 1, new Intention( elseReply, "two three four" ));
		
		Repertoire.signs.insert( s );
		r.answer( Reply.yes().toString() );

		
		audit.log( Repertoire.signs.toString() );
		audit.log( r.toString());
}	}
