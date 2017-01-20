package com.yagadi.enguage.concept;

import com.yagadi.enguage.expression.Context;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.expression.Utterance;
import com.yagadi.enguage.expression.when.Moment;
import com.yagadi.enguage.expression.when.When;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.sofa.Sofa;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Intention extends Attribute {
	private static Audit audit = new Audit( "Intention" );
	
	public static final String      REPLY = "r";
	public static final String ELSE_REPLY = "R";
	public static final String      THINK = "t";
	public static final String ELSE_THINK = "T";
	public static final String      DO    = "d";
	public static final String ELSE_DO    = "D";
	public static final String FINALLY    = "f";

	public boolean   temporal = false;
	public boolean   isTemporal() { return temporal; }
	public Intention temporalIs( boolean b ) { temporal = b; return this; }

	public boolean   spatial = false;
	public boolean   isSpatial() { return spatial; }
	public Intention spatialIs( boolean s ) { spatial = s; return this; }


	public Intention( String name, String value ) { super( name, value ); }	
	
	// processes: think="... is a thought".
	private Reply think( Reply r /*String answer*/ ) {
		audit.in( "think", "value='"+ value +"', previous='"+ r.a.toString() +"', ctx =>"+ Context.valueOf());

		// pre-process value to get an utterance...
		// we don't know the state of the intentional value
		Strings u =
			Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
				Context.deref( // X => "coffee", singular-x="80s" -> "80"
					new Strings( value ).replace( // replace "..." with answer
							Strings.ellipsis,
							r.a.toString() ) // was answer
					//false - is default don't expand, UNIT => cup NOT unit='cup'
			)	);
		
		audit.debug( "Thinking: "+ u.toString( Strings.CSV ));

		// This is mediation...
		Reply tmpr = Repertoire.interpret( new Utterance( u )); // just recycle existing reply
		if (r.a.isAppending())
			r.a.add( tmpr.a.toString() );
		else
			r = tmpr;
		// pass out was done value was!
		r.doneIs( false );

		Reply.strangeThought(""); // ??? will this clear it on subsequent thoughts?
		if ( Reply.DNU == r.type()) {
			/* TODO: At this point do I want to cancel all skipped signs? 
			 * Or just check if we've skipped any signs and thus report 
			 * this as simply a warning not an ERROR?
			 */
			// put this into reply via Reply.strangeThought()
			audit.ERROR( "Strange thought: I don't understand: '"+ u.toString( Strings.SPACED ) +"'" );
			Reply.strangeThought( u.toString( Strings.SPACED ));
			// remove strange thought from Reply - just say DNU
			if (Allopoiesis.disambFound()) {
				audit.ERROR( "Previous ERROR: maybe just run out of meanings?" );
				Reply.strangeThought("");
			}
			r.doneIs( true );
		
		} else if ( Reply.NO == r.type() && r.a.toString().equalsIgnoreCase( Reply.ik()))
			r.answer( Reply.yes());

		return (Reply) audit.out( r );
	}
	// ---
		private Strings conceptualise( String answer ) {
			Strings cmd = // Don't Strings.normalise() coz sofa requires "1" parameter
					Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
						Context.deref(
							new Strings( value ).replace( Strings.ellipsis, answer ),
							true // DO expand, UNIT => unit='non-null value'
					)	);
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
			return cmd;
		}
		private String deconceptualise( String rc, String cmd, String answer ) {
			return Moment.valid( rc ) ?
				new When( rc ).rep( Reply.dnk() ).toString()
				: (cmd.equals( "get" ) || cmd.equals( "attributeValue" )) && (null == rc || rc.equals( "" )) ?
					Reply.dnk()
					: rc.equals( Shell.FAIL ) ?
						Reply.no()
						:	rc.equals( Shell.SUCCESS ) ?
								(   answer.equals( "" )
								 || answer.equals( Reply.no() )) ?
								Reply.success()
								: answer
							: rc;
		}
	private Reply perform( Reply r ) {
		audit.in( "perform", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		String answer = r.a.toString();
		Strings cmd = conceptualise( answer );
		String rc = new Sofa().interpret( cmd );
		rc = deconceptualise( rc, cmd.get( 1 ), answer );
		return (Reply) audit.out( r.answer( rc ));
	}
	
	private Reply reply( Reply r ) {
		audit.in( "reply", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		r.format( value.equals( "" ) ? "ok" : value ); // TODO: NOT previous(), inner()!
		r.doneIs( r.type() != Reply.NK && r.type() != Reply.DNU );
		audit.out( "a="+ r.a.toString() + (r.isDone()?" (we're DONE!)" : "(keep looking...)" ));
		return r;
	}
	
	public Reply mediate( Reply r ) {
		//if (audit.tracing) audit.in( "mediate", name +"='"+ value +"', r='"+ r.asString() +"', ctx =>"+ Context.valueOf());
		
		if (r.isDone()) { 
			audit.debug( "skipping "+ name() +": reply already found" );
		
		} else if (name.equals( "finally" )) {
			perform( r ); // ignore result of finally

		} else {
			
			if (r.negative()) {
				if (name.equals( ELSE_THINK ))
					r = think( r );
				else if (name.equals( ELSE_DO ))
					r = perform( r );
				else if (name.equals( ELSE_REPLY ))
					r = reply( r );
 					
			} else { // train of thought is positive
				if (name.equals( THINK ))
					r = think( r );
				else if (name.equals( DO ))
					r = perform( r );
				else if (name.equals( REPLY )) // if Reply.NO -- deal with -ve replies!
					r = reply( r );
		}	}
		
		//if (audit.tracing) audit.out( "r='"+ r.toString() +"' ("+ r.asString() +")");
		return r;
	}
	public static void main( String argv[]) {
		Reply r = new Reply().answer( "world" );
		Variable.encache( Overlay.Get() );
		audit.log( new Intention( REPLY, "hello ..." ).mediate( r ).toString() );
}	}
