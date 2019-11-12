package org.enguage.vehicle;

import org.enguage.interp.Context;
import org.enguage.interp.pattern.Pattern;
import org.enguage.interp.pattern.Patternette;
import org.enguage.interp.sign.Sign;
import org.enguage.objects.Numeric;
import org.enguage.objects.Spatial;
import org.enguage.objects.Temporal;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Overlay;
import org.enguage.objects.space.Sofa;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attributes;
import org.enguage.vehicle.reply.Answer;
import org.enguage.vehicle.when.When;
import org.enguage.vehicle.where.Where;

public class Utterance {
	static private Audit audit = new Audit( "Utterance" );

	static private Strings previous = null;
	static public  Strings previous() { return previous; }
	static public  Strings previous( Strings sa ) { return previous = sa; }

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
	public Utterance( Strings orig ) { this( orig, null ); }
	public Utterance( Strings orig, Strings prevAnswer ) {
		representamen = orig;
		expanded =	Colloquial.applyIncoming(       // I'm => I am => _user is
						new Strings( orig ).normalise() // "i am/." becomes "i/am/."
					).decap()                          // deref anything in the environment?
					.contract( "=" );                  // [ name, =, "value" ] => [ name="value" ]

		// insert answer, e.g. "abc whatever xyz" + prevAnswer='42' => "abc 42 xyz"
		if (null != prevAnswer && expanded.contains( Answer.placeholder()))
			expanded.replace( Answer.placeholderAsStrings(), new Strings( prevAnswer ));
			
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
	static public boolean sane( Utterance u ) {return u != null && u.representamen.size() > 0;	}
	static public Strings  externalise( String  reply, boolean verbatim ) {
		return externalise( new Strings( reply ), verbatim );
	}
	static public Strings  externalise( Strings reply, boolean verbatim ) {
		
		reply = Variable.derefOrPop( reply.listIterator());

		// outbound and general colloquials
		if (!verbatim)
			reply = Colloquial.applyOutgoing( reply );

		// ...deref any context...
		
		// English-dependent processing...
		reply = Language.indefiniteArticleVowelSwap(
					Language.pronunciation( reply ));
		
		// TODO: to Strings, or move up into derefOrPop?
		Strings tmp = new Strings();
		for (String r : reply)
			tmp.add( Strings.fromCamelCase( r ));
		reply = tmp;
		
		return Language.asStrings( Numeric.deref( reply )).contract( Language.APOSTROPHE );
	}

	// test code...
	public static void test( Sign s, String utterance ) { test( s, utterance, null ); }
	public static void test( Sign s, String utterance, Strings prevAns ) {
		Utterance u = new Utterance( new Strings( utterance ), prevAns );
		Attributes a = u.match(s);
		Audit.log( "utterance is: "
				+ u.toString() +"\n"
				+ (null == a ? "NOT matched." : "matched attrs is: "+ a ));
	}
	
	public static void main( String arg[]) {
		
		Overlay.Set( Overlay.Get());
		if (!Overlay.attachCwd( "Utternace" ))
			audit.FATAL(">>>>Ouch! Cannot autoAttach() to object space<<<<" );

		Where.doLocators( "to the left of/to the right of/in front of/on top of");
		Where.doLocators( "behind/in/on/under/underneath/over/at" );
		
		Audit.log("Creating a pub:" +
						new Sofa().interpret(new Strings("entity create pub"))
		);

		// create a meeting repertoire
		Pattern ts = new Pattern();
		ts.add( new Patternette( "i am meeting", "WHOM" ).phrasedIs() );
		Sign s = new Sign().concept("meeting").pattern( ts );
		Spatial.addConcept("meeting");
		Temporal.addConcept("meeting");
		s.isSpatial();
		s.isTemporal();
		
		test( s, "i am meeting my brother at the pub at 7" );
		test( s, "i am meeting my sister  at the pub" );
		
		ts = new Pattern();
		ts.add( new Patternette( "what is the factorial of", "N" ).numericIs() );
		s = new Sign().concept("meeting").pattern( ts );

		test( s, "what is the factorial of whatever", new Strings( "3" ));
}	}
