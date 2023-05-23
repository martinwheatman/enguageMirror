package org.enguage.signs.symbol;

import org.enguage.signs.Sign;
import org.enguage.signs.object.Numeric;
import org.enguage.signs.object.Temporal;
import org.enguage.signs.object.Variable;
import org.enguage.signs.object.sofa.Overlay;
import org.enguage.signs.object.sofa.Perform;
import org.enguage.signs.symbol.config.Colloquial;
import org.enguage.signs.symbol.config.Englishisms;
import org.enguage.signs.symbol.pattern.Frag;
import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.signs.symbol.when.When;
import org.enguage.signs.symbol.where.Where;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;

public class Utterance {
	static private      Audit  audit         = new Audit( "Utterance" );
		
	private static Strings previous = new Strings();
	public  static Strings previous() { return previous; }
	public  static Strings previous( Strings sa ) { return previous = sa; }

	// members
	private Strings representamen;
	public  Strings representamen() { return representamen; }
	private Strings temporal;
	private Strings expanded;
	public  Strings expanded() { return expanded; }
	private When    when;
	private Attributes whenAttrs;
	// ... extensible?

	/* Utterance stores all the versions of what has been said, recognising snippets:
	 * - typically learnt in primary, are ctx free: e.g. in/at/on/from/until <time>
	 * - extensible - at runtime. See sgai 2016 paper...
	 * Further, taught word phrases to determine tense: will ~, ~s, ~ed; plurality?
	 */
	public Utterance( Strings orig ) {
		representamen = orig;
		expanded  =	Colloquial.applyIncoming(       // I'm => I am => _user is
						new Strings( orig ).normalise() // "i am/." becomes "i/am/."
					).decap()                          // deref anything in the environment?
					.contract( "=" );                  // [ name, =, "value" ] => [ name="value" ]
		temporal  = new Strings( expanded );
		when      = When.getWhen( temporal ); // removes temporal content
		whenAttrs = when.toAttributes();
	}
	
	public Attributes match( Sign s ) {
		// This is a high value method so could do with being fast!
		Attributes match = null;

		if (s.isTemporal()) {
			Context.push( whenAttrs );
			if (!when.isUnassigned() &&
			    (null != (match = s.pattern().matchValues( temporal, s.isSpatial() ))))
				match.addAll( whenAttrs ); // add it, don't not pop it.
			Context.pop();  // do pop it! Keep context clean!
		}
		// if no qualified match, attempt an expanded match
		return match == null ? s.pattern().matchValues( expanded, s.isSpatial() ) : match;
	}
		
	public String toString( int layout ) { return expanded.toString( layout );}
	public String toString() { return toString( Strings.SPACED );}
	
	// helpers
	public  static  boolean sane( Utterance u ) {return u != null && !u.representamen.isEmpty();}
	public  static  Strings  externalise( String  reply, boolean verbatim ) {
		return externalise( new Strings( reply ), verbatim );
	}
	public  static  Strings  externalise( Strings reply, boolean verbatim ) {
		
		reply = Variable.derefOrPop( reply.listIterator());
		
		// outbound and general colloquials
		if (!verbatim)
			reply = Colloquial.applyOutgoing( reply );

		// ...deref any context...
		
		// English-dependent processing...
		reply = Englishisms.indefiniteArticleVowelSwap( reply );
		
		// TODO: to Strings, or move up into derefOrPop?
		Strings tmp = new Strings();
		for (String r : reply)
			tmp.add( Strings.fromCamelCase( r ));
		reply = tmp;
		
		return Englishisms.asStrings( Numeric.deref( reply ))
				.contract( Englishisms.APOSTROPHE );
	}

	// test code...
	public static void test( Sign s, String utterance ) { test( s, utterance, null ); }
	public static void test( Sign s, String utterance, Strings prevAns ) {
		Utterance u = new Utterance( new Strings( utterance ));
		Attributes a = u.match(s);
		audit.log( "utterance is: "
				+ u.toString() +"\n"
				+ (null == a ? "NOT matched." : "matched attrs is: "+ a ));
	}
	
	public static void main( String[] arg) {
		
		Overlay.set( Overlay.get());
		Overlay.attach( "Utternace" );

		Where.doLocators( "to the left of/to the right of/in front of/on top of");
		Where.doLocators( "behind/in/on/under/underneath/over/at" );
		
		audit.log("Creating a pub:" +
						Perform.interpret(new Strings("entity create pub"))
		);

		// create a meeting repertoire
		Frags ts = new Frags();
		ts.add( new Frag( "i am meeting", "WHOM" ).phrasedIs() );
		Sign s = new Sign().concept("meeting").pattern( ts );
		Where.addConcept("meeting");
		Temporal.addConcept("meeting");
		s.isSpatial();
		s.isTemporal();
		
		test( s, "i am meeting my brother at the pub at 7" );
		test( s, "i am meeting my sister  at the pub" );
		
		ts = new Frags();
		ts.add( new Frag( "what is the factorial of", "N" ).numericIs() );
		s = new Sign().concept("meeting").pattern( ts );

		test( s, "what is the factorial of whatever", new Strings( "3" ));
}	}
