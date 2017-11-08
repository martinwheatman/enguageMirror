package com.yagadi.enguage.sign.intention;

import java.util.ArrayList;
import java.util.Iterator;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.object.Sofa;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.sign.Sign;
import com.yagadi.enguage.sign.Signs;
import com.yagadi.enguage.sign.pattern.Pattern;
import com.yagadi.enguage.sign.repertoire.Concepts;
import com.yagadi.enguage.sign.repertoire.Repertoire;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Net;
import com.yagadi.enguage.util.Proc;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.util.Tags;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Question;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;

public class Intention {
	
	private static Audit audit = new Audit( "Intention" );
	
	public static final String UNDEF      = "und";
	public static final String NEW        = "new";
	public static final String APPEND     = "app";
	public static final String PREPEND    = "prep";
	
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
		audit.in( "mediate", "NAME="+ Repertoire.AUTOP +", value="+ value +", "+ Context.valueOf());
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
					    val = Strings.trim( sa.get( 1 ), '"' );
				if (type == append )
					s.append( new Intention( nameToType(  attr ), val ));
				else
					s.prepend( new Intention( nameToType( attr ), val ));
			}
			
		} else if (type == create ) { // autopoeisis?
			String attr    = sa.get( 0 ),
			       pattern = sa.get( 1 ),
			       val     = Strings.trim( sa.get( 2 ), '"' );
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
						.pattern( new Pattern( new Strings( Strings.trim( pattern, '"' ))) )
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
		audit.in( "think", "value='"+ value +"', previous='"+ r.a.toString() +"', ctx =>"+ Context.valueOf());
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
		audit.in( "perform", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		String answer = r.a.toString();
		Strings cmd = formulate( answer, true ); // DO expand, UNIT => unit='non-null value'
		
		if (isTemporal()) {
			String when = Context.get( "when" );
			if (!when.equals(""))
				cmd.append( "WHEN='"+ when +"'" );
		}		
		if (isSpatial()) {
			String locator = Context.get( "locator" );
			if (!locator.equals("")) {
				String location = Context.get( "location" );
				if (!location.equals("")) {
					cmd.append( "LOCATOR='"+  locator  +"'" );
					cmd.append( "LOCATION='"+ location +"'" );
		}	}	}

		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()== 1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attributes( cmd.get(0) ).get( "args" ));
		
		audit.debug( "performing: "+ cmd.toString());
		String rawAnswer = new Sofa().doCall( new Strings( cmd ));
		if (!ignore) r.rawAnswer( rawAnswer, cmd.get( 1 ) );

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
		audit.in( "mediate", typeToString( type ) +"='"+ value +"', ctx =>"+ Context.valueOf());
		
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
	public Reply getReply( Reply r ) {
		r.answer( Reply.yes()); // bland default reply to stop debug output look worrying
		
		Strings cmds =
				Context.deref(
					/*Variable.deref(*/
						new Strings( value() )
					/*)*/
				);
		cmds = cmds.normalise();
		String cmd = cmds.remove( 0 );

		if ( cmd.equals( "primeAnswer" )) {
			
			Question.primedAnswer( cmds.toString() ); // needs to be tidied up...
			
			
		} else if ( cmd.equals( "removePrimedAnswer" )) {
			
			Question.primedAnswer( null ); // tidy up any primed answer...
			
			
		} else if ( cmd.equals( "ask" )) {
			
			String question = cmds.toString();
			audit.debug( "Question is: "+ question );
			// question => concept
			
			Strings answers = Question.extractPotentialAnswers( cmds );
			audit.debug( "potential ANSWERs are ["+ answers.toString( Strings.DQCSV ) +"]");

			// question => answer
			String answer = new Question( question ).ask();
			Question.primedAnswer( null ); // tidy up any primed answer...
			
			r.format( answer );
			if (!answers.contains( answer ))
				r.userDNU();
			
		} else if ( cmd.equals( "undo" )) {
			Enguage e = Enguage.get();
			r.format( Reply.success() );
			if (cmds.size() == 1 && cmds.get( 0 ).equals( "enable" )) 
				Redo.undoEnabledIs( true );
			else if (cmds.size() == 1 && cmds.get( 0 ).equals( "disable" )) 
				Redo.undoEnabledIs( false );
			else if (cmds.size() == 0 && Redo.undoIsEnabled()) {
				if (e.o.count() < 2) { // if there isn't an overlay to be removed
					audit.debug( "overlay count( "+ e.o.count() +" ) < 2" ); // audit
					r.answer( Reply.no() );
				} else {
					audit.debug("ok - restarting transaction");
					e.o.reStartTxn();
				}
			} else if (!Redo.undoIsEnabled())
				r.format( Reply.dnu() );
			else
				r = Redo.unknownCommand( r, cmd, cmds );
			
		} else if (cmd.equals( Redo.DISAMBIGUATE )) {
			Redo.disambOn( cmds );
		
		} else if (cmd.equals( "load" )) {
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			Strings files = cmds;
			audit.debug( "loading "+ files.toString( Strings.CSV ));
			for(int i=0; i<files.size(); i++)
				Concepts.load( files.get( i ));
/*			 
		} else if (cmd.equals( "unload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++)
				Concept.unload( files.get( i ));

		} else if (cmd.equals( "reload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++) Concept.unload( files.get( i ));
			for(int i=0; i<files.size(); i++) Concept.load( files.get( i ));
/*
		} else if (e.get( 0 ).equals( "save" ) || e.get( 0 ).equalsIgnoreCase( "saveAs" )) {
			if (e.get( 0 ).equalsIgnoreCase( "saveAs" ) && ( e.size() != 2))
				System.err.println( e.get( 0 ) +": NAME required." );
			else {
				if (e.get( 0 ).equalsIgnoreCase( "saveAs" )) {
					//(re)NAME concept
					System.out.println( "renaming concept" );
				}
				//save concept
				System.out.println( "Saving concept" );
			}
*/
		} else if (cmd.equals( "spell" )) {
			r.format( Language.spell( cmds.get( 0 ), true ));
			
		} else if (cmd.equals( "tcpip" )) {
			
			String prefix = Variable.get( "XMLPRE" ),
					suffix = Variable.get( "XMLPOST" );
			
			int port = -1;
			try {
				port = Integer.valueOf( cmds.get( 1 ));
			} catch (Exception e1) {
				try {
					port = Integer.valueOf( Variable.get( "PORT" ));
				} catch (Exception e2) {
					port = 0;
			}	}
			
			r.format( Net.client( cmds.get( 0 ),
								  port,
								  (null==prefix ? "" : prefix) +
										Variable.derefUc( Strings.trim( cmds.get( 2 ), '"' )) +
										(null==suffix ? "" : suffix)
					)			);
			
		} else if (cmd.equals( "timing" )) {
			audit.log( cmd +" "+ cmds.toString());
			if (cmds.get( 0 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
				Audit.detailedOn = true;
				Audit.timings = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "tracing" )) {
			audit.log( cmd +" "+ cmds.toString());
			if (cmds.get( 0 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
				Audit.allTracing = false;
				Audit.detailedOn = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "detailed" )) {
			
			audit.log( cmds.toString());
			if (cmds.get( 0 ).equals("off")) {
				Audit.allOff();
				Audit.timings = false;
				Audit.allTracing = false;
				Audit.detailedOn = false;
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
				Audit.allTracing = true;
				Audit.detailedOn = true;
			}
			r.format( Reply.success() );
			
		} else if (cmd.equals( "debug" )) {
			
			if (cmds.get( 0 ).equals( "off" )) {
				Audit.allOff();
				Audit.allTracing = false;
				Audit.timings = false;
				Audit.runtimeDebug = false;
				Audit.detailedOn = false;
				
			} else if (cmds.get( 1 ).equals( "tags" )) {
				Tags.debug( !Tags.debug() );
				
			} else {
				Audit.allOn();
				Audit.runtimeDebug = true;
			}
			r.format( Reply.success() );
			
			
		} else if (cmd.equals( "show" )) {
			
			//audit.audit( "cmds:"+ cmds +":sz="+ cmds.size() );
			if (1==cmds.size() && cmds.get( 0 ).length()>=4) {
				String option = cmds.get( 0 ).substring(0,4);
				if (option.equals( "auto" )) {
					Repertoire.autop.show();
					r.format( Reply.success() );
				} else if (   option.equals( "sign" )
				           || option.equals( "user" )) {
					Repertoire.signs.show();
					r.format( Reply.success() );
				} else if (option.equals( "engi" )) {
					Repertoire.allop.show();
					r.format( Reply.success() );
				} else if (option.equals( "all" )) {
					Repertoire.autop.show();
					Repertoire.allop.show();
					Repertoire.signs.show();
					r.format( Reply.success() );
				} else if (option.equals( "vari" )) {
					Variable.interpret( new Strings( "show" ));
					r.format( Reply.success());
				} else
					audit.ERROR( "option: "+ option +" doesn't match anything" );
			} else {
				Repertoire.signs.show();
				r.format( Reply.success() );
			}


		} else if ( value().equals( "repeat" )) {
			if (Reply.previous() == null) {
				audit.log("Allop:repeating dnu");
				r.format( Reply.dnu());
			} else {
				audit.log("Allop:repeating: "+ Reply.previous());
				r.repeated( true );
				r.format( Reply.repeatFormat());
				r.answer( Reply.previous());
			}
			
		} else if (cmd.equals( "help" )) {
			Redo.helped( true );
			r.format( Repertoire.allop.helpedToString( Repertoire.ALLOP ));

		} else if (cmd.equals( "welcome" )) {
			Redo.helped( true );
			r.say( new Strings( "welcome" ));
			r.format( Signs.help());

		} else if ( cmd.equals( "list" )) {
			//Strings reps = Enguage.e.signs.toIdList();
			/* This becomes less important as the interesting stuff becomes auto loaded 
			 * Don't want to list all repertoires once the repertoire base begins to grow?
			 * May want to ask "is there a repertoire for needs" ?
			 */
			r.format( "loaded repertoires include "+ new Strings( Concepts.loaded()).toString( Reply.andListFormat() ));
			
		} else if ( cmd.equals( "describe" ) && cmds.size() >= 2) {
			
			String name = cmds.toString( Strings.CONCAT );
			r.format( Repertoire.signs.helpedToString( name ));
			
		} else if ( cmd.equals( "repertoire" )) {
			r.format( Repertoire.signs.helpedToString());
			
		} else {
			
			r = Redo.unknownCommand( r, cmd, cmds );
		}
		return r;
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
