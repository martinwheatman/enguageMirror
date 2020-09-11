package org.enguage.interp.intention;

import org.enguage.interp.Context;
import org.enguage.interp.pattern.Patterns;
import org.enguage.interp.repertoire.Repertoire;
import org.enguage.interp.sign.Sign;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Sofa;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Proc;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.reply.Reply;
import org.enguage.vehicle.where.Where;

import java.util.ArrayList;
import java.util.Iterator;

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
	public Intention( int nm, Strings vals ) { this( nm, vals.toString()); }
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
	
	static private Sign s = null, voiced = null;
	static public  void printSign() { Audit.LOG( "Autop().printSign:\n"+ s.pattern().toXml()); }
	
	static private String concept = "";
	static public    void concept( String name ) { concept = name; }
	static public  String concept() { return concept; }
	
	public Reply autopoiesis( Reply r ) {
		//audit.in( "autopoiesis", "NAME="+ Repertoire.AUTOP +", value="+ value +", "+ Context.valueOf());
		
		switch (intent) { // manually adding a sign
		case  create:
			Repertoire.signs.insert(
				voiced = new Sign()
					.pattern( new Patterns( value ))
					.concept( Repertoire.AUTOPOIETIC )
			);
			break;
		case append:
			if (null != voiced)
				voiced.append( new Intention( type, Patterns.toPattern( new Strings( value ))));
			break;
		case prepend :
			if (null != voiced)
				voiced.prepend( new Intention( type, Patterns.toPattern( new Strings( value ))));
			break;
		case headAppend:
			if (null != voiced) 
				voiced.insert( 1, new Intention( type, Patterns.toPattern( new Strings( value ))));
			break;
		// following these are trad. autopoiesis...this need updating as above!!!
		default:
			Strings sa = Context.deref( new Strings( value ));
			switch (type) {
			case create: {
				String attr    = sa.remove( 0 ),
				       pattern = sa.remove( 0 ),
					   val     = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
				Repertoire.signs.insert(
						s = new Sign()
								.pattern( new Patterns( new Strings( Strings.trim( pattern, Strings.DOUBLE_QUOTE ))) )
								.concept( concept() )
								.append( new Intention( Intention.nameToType( attr ), val )));
				break;
			}
			case append :
			case prepend:
				if (null == s)
					// this should return DNU...
					audit.ERROR( "adding to sign before creation" );
				else {
					String attr = sa.remove( 0 ),
					       val = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
					if (type == append )
						s.append( new Intention( nameToType(  attr ), val ));
					else
						s.prepend( new Intention( nameToType( attr ), val ));
		}	}	}
		//return (Reply) audit.out( r.answer( Reply.yes().toString() ));
		return r.answer( "go on"/*Reply.yes().toString()*/ );
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
		//audit.in( "think", "value='"+ value +"', previous='"+ r.a.toString() +"'" );
		Strings thought  = formulate( r.a.toString(), false ); // dont expand, UNIT => cup NOT unit='cup'

		audit.debug( "Thinking: "+ thought.toString( Strings.CSV ));
		Reply tmpr = Repertoire.mediate( new Utterance( thought, new Strings(r.a.toString()) )); // just recycle existing reply
		
		if (r.a.isAppending())
			r.a.add( tmpr.a.toString() );
		else
			r = tmpr;
		
		r.type( new Strings( r.toString()) )
		 .conclude( thought );
		
		// If we've returned DNU, we want to continue
		r.doneIs( Strings.isUpperCaseWithHyphens( value ) // critical!
		          && r.type() == Reply.FAIL );

		return r; //(Reply) audit.out( r );
	}
	
	private Reply perform( Reply r ) { return perform( r, false ); }
	private Reply andFinally( Reply r ) { return perform( r, true ); }
	
	private Reply perform( Reply r, boolean ignore ) {
		//audit.in( "perform", "value='"+ value +"', ignore="+ (ignore?"yes":"no"));
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
					cmd.append( Attribute.asString( Where.LOCTR, locator  ));
					cmd.append( Attribute.asString( Where.LOCTN, location ));
		}	}	}

		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()==1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attribute( cmd.get(0) ).value());
		
		audit.debug( "performing: "+ cmd.toString());
		// deref first 4 params before sofa
		for (int i=0; i<4 && i<cmd.size(); i++)
			if (cmd.get( i ).equals( ":" )) {
				cmd.remove( i );
				break;
			} else
				cmd.set( i, Attribute.getValue( cmd.get( i ) ));

		Strings rawAnswer = new Sofa().doCall( new Strings( cmd ));
		if (!ignore) r.rawAnswer( rawAnswer.toString(), cmd.get( 1 ));

		return r; //(Reply) audit.out( r ); //
	}
	private Reply reply( Reply r ) {
		//audit.in( "reply", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		r.format( value.equals( "" ) ? r.toString() : value );
		r.type( new Strings( value ));
		r.doneIs( r.type() != Reply.DNU );
		return r; //(Reply) audit.out( r );
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
	public String toString() {
		String type = Intention.typeToString( type() );
		if (type.equals(      REPLY )) return         "reply \""+   value +"\"";
		if (type.equals( ELSE_REPLY )) return "if not, reply \""+   value +"\"";
		if (type.equals(      DO    )) return         "perform \""+ value +"\"";
		if (type.equals( ELSE_DO    )) return "if not, perform \""+ value +"\"";
		if (type.equals(      RUN   )) return         "run \""+     value +"\"";
		if (type.equals( ELSE_RUN   )) return "if not, run \""+     value +"\"";
		if (type.equals(      THINK )) return                       value;
		if (type.equals( ELSE_THINK )) return "if not, "+           value;
		if (type.equals(    FINALLY )) return "finally, "+          value;
		return Attribute.asString( type, value() );
	}
	// ---
	public static Reply test(Reply r, ArrayList<Intention> intents) {
		Iterator<Intention> ins = intents.iterator();
		while (!r.isDone() && ins.hasNext()) {
			Intention in = ins.next();
			Audit.log( typeToString( in.type )  +"='"+ in.value +"'" );
			r = new Intention( in, false, false ).autopoiesis( r );
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
		
		audit.title( "manual sign creation... add each intention individually" );
		r = new Reply();
		r = new Intention( create,    "b variable pattern z", create ).autopoiesis( r );
		r = new Intention( thenThink, "one two three four"  , append ).autopoiesis( r );
		r = new Intention( elseReply, "two three four"      , append ).autopoiesis( r );
		r = new Intention( thenReply, "three four"          , append ).autopoiesis( r );
		Audit.log( Repertoire.signs.toString() );
		Audit.log( r.toString());
		
		
		audit.title( "sign self-build II... add pairs of attributes" );
		// now built like this...
		// To PATTERN reply TYPICAL REPLY
		r = new Reply();
		s = new Sign()
				.pattern( new Patterns( "c variable pattern z" ))
				.concept( concept() );
		String reply = "three four";
		s.append( new Intention( thenReply, reply ));
		// ...This implies COND
		s.prepend( new Intention( thenThink, "one two three four" ));
		// ...if not reply EXECP REPLY
		s.insert( 1, new Intention( elseReply, "two three four" ));
		
		Repertoire.signs.insert( s );
		r.answer( Reply.yes().toString() );

		
		Audit.log( Repertoire.signs.toString() );
		Audit.log( r.toString());
}	}
