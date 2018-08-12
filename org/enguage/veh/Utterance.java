package org.enguage.veh;

import org.enguage.obj.Numeric;
import org.enguage.obj.Spatial;
import org.enguage.obj.Temporal;
import org.enguage.obj.Variable;
import org.enguage.obj.space.Sofa;
import org.enguage.sgn.Sign;
import org.enguage.sgn.ctx.Context;
import org.enguage.sgn.ptt.Pattern;
import org.enguage.sgn.ptt.Patternette;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;
import org.enguage.veh.Colloquial;
import org.enguage.veh.Language;
import org.enguage.veh.Utterance;
import org.enguage.veh.reply.Answer;
import org.enguage.veh.when.When;
import org.enguage.veh.where.Where;

public class Utterance {
	static private Audit audit = new Audit( "Utterance" );

	static private Strings previous = null;
	static public  Strings previous() { return previous; }
	static public  Strings previous( Strings sa ) { return previous = sa; }

	// members
	private Strings representamen;
	private Strings temporal;
	private Strings expanded;
	public  Strings expanded() { return expanded; }
	private When    when;
	private Attributes a;
	// ... extensible?

	/* Utterance stores all the versions of what has been said, recognising snippets:
	 * - typically learnt in primary, are ctx free: e.g. in/at/on/from/until <time>
	 * - extensible - at runtime. See sgai 2016 paper...
	 * Further, taught word phrases to determine tense: will ~, ~s, ~ed; plurality?
	 */
	public Utterance( Strings orig ) { this( orig, null ); }
	public Utterance( Strings orig, Strings prevAnswer ) {
		//audit.in( "ctor", orig.toString( Strings.SPACED ));
		representamen = orig;
		expanded =	Colloquial.applyIncoming(       // I'm => I am => _user is
						new Strings( orig ).normalise() // "i am/." becomes "i/am/."
					).decap()                          // deref anything in the environment?
					.contract( "=" );                  // [ name, =, "value" ] => [ name="value" ]
		//audit.LOG( "Utterance: expanded: "+ expanded.toString( Strings.SPACED ));

		if (null != prevAnswer && expanded.contains( Answer.placeholder())) {
			//audit.LOG( ">>>>>replacing "+ Ans.placeholderAsStrings() +" with "+ new Strings( prevAnswer ));
			expanded.replace( Answer.placeholderAsStrings(), new Strings( prevAnswer ));
		}
			
		temporal = new Strings( expanded );
		when = When.getWhen( temporal );
		
		a = when.toAttributes();
		//audit.out( "when: "+ when +", where: "+ where +", t/s: "+ whenWhere );
	}
	
	public Attributes match( Sign s ) {
		// This is a high value method so could do with being fast!
		Attributes match = null;

		Context.push( a );
		
		if (s.isTemporal()
			&& !when.isUnassigned()
			&& (null != (match = s.pattern().matchValues( temporal, s.isSpatial() ))))
			match.addAll( a ); // add it, don't not pop it.

		Context.pop();  // do pop it! Keep context clean!
		
		// if no qualified match, attempt an expanded match
		return match == null ? s.pattern().matchValues( expanded, s.isSpatial() ) : match;
	}
		
	public String toString( int layout ) { return expanded.toString( layout );}
	public String toString() { return toString( Strings.SPACED );}
	
	// helpers
	static public boolean sane( Utterance u ) {return u != null && u.representamen.size() > 0;	}
	static public String  externalise( Strings reply, boolean verbatim ) {
		// if not terminated, add first terminator -- see Tag.c::newTagsFromDescription()
		if (!Shell.isTerminator( reply.get( reply.size() -1))
		 && !((reply.size() > 1) && Shell.isTerminator( reply.get( reply.size() -2))
		 && Language.isQuote( reply.get( reply.size() -1))))
			reply.add( Shell.terminators().get( 0 ));
		
		reply = Variable.deref(reply);

		// outbound and general colloquials
		if (!verbatim)
			reply = Colloquial.applyOutgoing( reply );

		// ...deref any context...
		
		// English-dependent processing...
		reply = Language.indefiniteArticleVowelSwap(
						Language.sentenceCapitalisation( 
							Language.pronunciation( reply )));
		
		return Language.asString( Numeric.deref( reply )).replace( " '' ", Language.APOSTROPHE );
	}

	// test code...
	public static void test( Sign s, String utterance ) { test( s, utterance, null ); }
	public static void test( Sign s, String utterance, Strings prevAns ) {
		Utterance u = new Utterance( new Strings( utterance ), prevAns );
		Attributes a = u.match(s);
		audit.log( "utterance is: "
				+ u.toString() +"\n"
				+ (null == a ? "NOT matched." : "matched attrs is: "+ a ));
	}
	
	public static void main( String arg[]) {
		
		Where.doLocators( "to the left of/to the right of/in front of/on top of");
		Where.doLocators( "behind/in/on/under/underneath/over/at" );
		
		audit.log("Creating a pub:" +
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
