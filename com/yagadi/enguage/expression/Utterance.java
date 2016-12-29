package com.yagadi.enguage.expression;

import com.yagadi.enguage.concept.Sign;
import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.concept.Tags;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.sofa.Spatial;
import com.yagadi.enguage.sofa.Temporal;
import com.yagadi.enguage.sofa.Sofa;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.expression.when.When;
import com.yagadi.enguage.expression.where.Where;

public class Utterance {
	static private Audit audit = new Audit( "Utterance" );

	static private Strings previous = null;
	static public  Strings previous() { return previous; }
	static public  Strings previous( Strings sa ) { return previous = sa; }

	// members
	private Strings representamen;
	private Strings temporal;
	private Strings temporospatial;
	private Strings spatial;
	private Strings expanded;
	public  Strings expanded() { return expanded; }
	private When    when;
	private Where   whenWhere;
	private Where   where;
	private Attributes a;
	// ... extensible?

	/* Utterance stores all the versions of what has been said, recognising snippets:
	 * - typically learnt in primary, are ctx free: e.g. in/at/on/from/until <time>
	 * - extensible - at runtime.
	 * Further, taught word phrases to determine tense: will ~, ~s, ~ed; plurality?
	 */
	public Utterance( Strings orig ) {
		//audit.in( "ctor", orig.toString( Strings.SPACED ));
		representamen = orig;
		expanded =	Colloquial.applyIncoming(       // I'm => I am => _user is
						new Strings( orig ).normalise() // "i am/." becomes "i/am/."
					).decap()                          // deref anything in the environment?
					.contract( "=" );                  // [ name, =, "value" ] => [ name="value" ]
		//audit.log( "expanded: "+ expanded.toString( Strings.SPACED ));

		temporal = new Strings( expanded );
		when = When.getWhen(temporal);

		spatial = new Strings( expanded );
		where = Where.getWhere(spatial);

		temporospatial = new Strings( temporal );
		whenWhere = Where.getWhere( temporospatial );
		
		a = when.toAttributes();
		a.addAll( where.toAttributes() );
		//audit.out();
	}
	
	public Attributes match( Sign s ) {
		/* 
		 * This is a high value method so could do with being fast!
		 */
		Attributes match = null;

		Context.push( a );
		
		if ((s.isTemporal() && !when.isUnassigned()) &&
		    (s.isSpatial()  &&  whenWhere.assigned())      )
		{
			if (null != (match = s.content().matchValues( temporospatial )))
				match.addAll( a ); // add it, don't not pop it.
			
		} else if (s.isTemporal() && !when.isUnassigned()) {
			if (null != (match = s.content().matchValues( temporal )))
				match.addAll( a ); // add it, don't not pop it.
			
		} else if (s.isSpatial()  &&  where.assigned()) {
			if (null != (match = s.content().matchValues( temporal )))
				match.addAll( a ); // add it, don't not pop it.
		}
		
		Context.pop();  // do pop it! Keep context clean!
		
		if (match == null) {
			match = s.content().matchValues( expanded );
			//audit.log( "no temporal match, match is "+ (null != match ? match.toString() : "<null>"));
		}
		return match;
	}
	
	public String toString( int layout ) { return representamen.toString( layout );}
	
	// helpers
	static public boolean sane( Utterance u ) {return u != null && u.representamen.size() > 0;	}
	
	// test code...
	public static void main( String arg[]) {
		//Audit.traceAll( true );
		//Audit.allOn();
		//audit.tracing = true;

		// This should go into SofA
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			//* Spatial test:
			Where.doLocators();
			audit.log("Creating a pub:" +
							new Sofa().interpret(new Strings("entity create pub"))
			);

			// create a new Utterance...
			Utterance u = new Utterance( new Strings( "i am meeting my brother at the pub at 7" ));
			audit.log("u is " + u.toString(Strings.SPACED));

			// autoload/create the meeting repertoire
			Tags ts = new Tags();
			ts.add( new Tag( "i am meeting", "WHOM" ).attribute( "phrase", "phrase" ) );
			Sign s= new Sign().concept("meeting").content( ts );
			Spatial.conceptIs("meeting");
			Temporal.conceptIs("meeting");
			s.isSpatial();
			s.isTemporal();

			// process the utterance
			audit.log( "matching..." );
			Attributes a;
			if (null == (a = u.match(s)))
				audit.log( "NOT matched" );
			else
				audit.log( "matched attributes: "+ a.toString());
			
			/*
			u = new Utterance( new Strings( "and another" ));
			ts = new Tags();
			ts.add( new Tag( "and", "QUANTITY" ).attribute( "numeric", "numeric" ));
			s = new Sign().content( ts );
			a = u.match( s );
			audit.log( "matched attributes: "+ (a == null ? "null" : a.toString()));
			// */
}	}	}
