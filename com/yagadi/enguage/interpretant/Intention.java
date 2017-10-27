package com.yagadi.enguage.interpretant;

import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.object.Sofa;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Proc;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;
import com.yagadi.enguage.vehicle.when.Moment;
import com.yagadi.enguage.vehicle.when.When;

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
	public static final String ALLOP      = "engine";
	public static final String AUTOP      = "autopoiesis";
	
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
			case allop     : return ALLOP;
			case autop     : return AUTOP;
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
		else if ( name.equals( ALLOP ))
			return allop;
		else if ( name.equals( AUTOP ))
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
		
		r.setType( new Strings( r.toString()) )
		 .conclude( thought );
		
		// If we've returned DNU, we want to continue
		r.doneIs( false );

		return (Reply) audit.out( r );
	}
	
	private Reply perform( Reply r ) {
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
		String rc = new Sofa().doCall( new Strings( cmd )); // was interpret(), now doCall()
		
		// de-conceptualise raw answer
		String method = cmd.get( 1 );
		rc =  Moment.valid( rc ) ?                 // 88888888198888 -> 7pm
			new When( rc ).rep( Reply.dnk() ).toString()
			: (method.equals( "get" ) || method.equals( "attributeValue" ))
			  && (rc.equals( "" )) ?
				Reply.dnk()
				: rc.equals( Shell.FAIL ) ?
					Reply.failure()
					:	rc.equals( Shell.SUCCESS ) ?
							Reply.success()
							: rc;
	
		return (Reply) audit.out( r.answer( rc ));
	}
	private Reply reply( Reply r ) {
		audit.in( "reply", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		/* TODO: 
		 * if reply on its own, return reply from previous/inner reply -- imagination!
		 */
		r.format( value.equals( "" ) ? Reply.success() : value );
		r.setType( new Strings( value ));
		r.doneIs( r.type() != Reply.DNU && r.type() != Reply.FAIL );
		return (Reply) audit.out( r );
	}
	
	public Reply mediate( Reply r ) {
		audit.in( "mediate", typeToString( type ) +"='"+ value +"', ctx =>"+ Context.valueOf());
		
		if (type == thenFinally )
			perform( r ); // ignore result of finally

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
	public static void main( String argv[]) {
		Reply r = new Reply().answer( "world" );
		audit.log( new Intention( thenReply, "hello ..." ).mediate( r ).toString() );
}	}
