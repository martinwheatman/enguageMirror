package com.yagadi.enguage.interpretant;

import com.yagadi.enguage.object.Attribute;
import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.object.Sofa;
import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Context;
import com.yagadi.enguage.vehicle.Reply;
import com.yagadi.enguage.vehicle.Utterance;
import com.yagadi.enguage.vehicle.when.Moment;
import com.yagadi.enguage.vehicle.when.When;

public class Intention extends Attribute {
	private static Audit audit = new Audit( "Intention" );
	
	public static final String      REPLY = "r";
	public static final String ELSE_REPLY = "R";
	public static final String      THINK = "t";
	public static final String ELSE_THINK = "T";
	public static final String      DO    = "d";
	public static final String ELSE_DO    = "D";
	public static final String      RUN   = "n";
	public static final String ELSE_RUN   = "N";
	public static final String FINALLY    = "f";

	public boolean   temporal = false;
	public boolean   isTemporal() { return temporal; }
	public Intention temporalIs( boolean b ) { temporal = b; return this; }

	public boolean   spatial = false;
	public boolean   isSpatial() { return spatial; }
	public Intention spatialIs( boolean s ) { spatial = s; return this; }


	public Intention( String name, String value ) { super( name, value ); }	
	
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
		Strings u = formulate( r.a.toString(), false ); // dont expand, UNIT => cup NOT unit='cup'
		audit.debug( "Thinking: "+ u.toString( Strings.CSV ));
		// This is mediation...
		Reply tmpr = Repertoire.interpret( new Utterance( u )); // just recycle existing reply
		if (r.a.isAppending())
			r.a.add( tmpr.a.toString() );
		else
			r = tmpr;
		r.conclude( u, new Strings( r.toString()) );
		return (Reply) audit.out( r );
	}
	// ---
		private Strings conceptualise( String answer ) {
			
			// Don't Strings.normalise() coz sofa requires "1" parameter
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
			return cmd;
		}
		private String deconceptualise( String rc, String cmd, String answer ) {
			return Moment.valid( rc ) ?                 // 88888888198888 -> 7pm
				new When( rc ).rep( Reply.dnk() ).toString()
				: (cmd.equals( "get" ) || cmd.equals( "attributeValue" ))
				  && (rc.equals( "" )) ?
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
		
		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()== 1 && cmd.get(0).length() > 5 && cmd.get(0).substring(0,5).equals( "args=" ))
			cmd=new Strings( new Attributes( cmd.get(0) ).get( "args" ));
	
		audit.debug( "performing: "+ cmd.toString());
		String rc = new Sofa().interpret( cmd );
		rc = deconceptualise( rc, cmd.get( 1 ), answer );
		return (Reply) audit.out( r.answer( rc ));
	}
	private Reply reply( Reply r ) {
		audit.in( "reply", "value='"+ value +"', ["+ Context.valueOf() +"]" );
		// TODO: NOT previous(), inner()!
		r.format( value.equals( "" ) ? Reply.success() : value );
		r.doneIs( true /*r.type() != Reply.NK && r.type() != Reply.DNU*/ );
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
				else if (name.equals( ELSE_RUN ))
					r = new Proc( value ).run( r );
				else if (name.equals( ELSE_REPLY ))
					r = reply( r );
 					
			} else { // train of thought is neutral/positive
				if (name.equals( THINK ))
					r = think( r );
				else if (name.equals( DO ))
					r = perform( r );
				else if (name.equals( RUN ))
					r = new Proc( value ).run( r );
				else if (name.equals( REPLY )) // if Reply.NO -- deal with -ve replies!
					r = reply( r );
		}	}
		
		//if (audit.tracing) audit.out( "r='"+ r.toString() +"' ("+ r.asString() +")");
		return r;
	}
	public static void main( String argv[]) {
		Reply r = new Reply().answer( "world" );
		audit.log( new Intention( REPLY, "hello ..." ).mediate( r ).toString() );
}	}
