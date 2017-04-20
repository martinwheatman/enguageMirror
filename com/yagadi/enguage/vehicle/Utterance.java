package com.yagadi.enguage.vehicle;

import java.util.Iterator;
import java.util.Locale;

import com.yagadi.enguage.interpretant.Sign;
import com.yagadi.enguage.interpretant.Tag;
import com.yagadi.enguage.interpretant.Tags;
import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.object.Sofa;
import com.yagadi.enguage.object.Spatial;
import com.yagadi.enguage.object.Temporal;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.when.When;
import com.yagadi.enguage.vehicle.where.Where;

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
	 * - extensible - at runtime. See sgai 2016 paper...
	 * Further, taught word phrases to determine tense: will ~, ~s, ~ed; plurality?
	 */
	public Utterance( Strings orig ) {
		//audit.in( "ctor", orig.toString( Strings.SPACED ));
		representamen = orig;
		expanded =	Colloquial.applyIncoming(       // I'm => I am => _user is
						new Strings( orig ).normalise() // "i am/." becomes "i/am/."
					).decap()                          // deref anything in the environment?
					.contract( "=" );                  // [ name, =, "value" ] => [ name="value" ]
		audit.debug( "expanded: "+ expanded.toString( Strings.SPACED ));

		temporal = new Strings( expanded );
		when = When.getWhen( temporal );
		
		spatial = new Strings( expanded );
		where = Where.getWhere( spatial );
		
		temporospatial = new Strings( temporal );
		whenWhere = Where.getWhere( temporospatial );
		
		a = when.toAttributes();
		a.addAll( where.toAttributes() );
		//audit.out( "when: "+ when +", where: "+ where +", t/s: "+ whenWhere );
	}
	
	public Attributes match( Sign s ) {
		// This is a high value method so could do with being fast!
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
			if (null != (match = s.content().matchValues( spatial )))
				match.addAll( a ); // add it, don't not pop it.
		}
		Context.pop();  // do pop it! Keep context clean!
		
		// if no qualified match, attempt an expanded match
		return match == null ? s.content().matchValues( expanded ) : match;
	}
	static public Strings toPattern( String u ) {
		// my name is variable name => my name is NAME
		Strings in  = new Strings( u ),
		        out = new Strings();
		Iterator<String> wi = in.iterator();
		while ( wi.hasNext() ) {
			String word = wi.next();
			if (word.equals( "variable" )) {
				if (wi.hasNext()
						&& null != (word = wi.next())
						&& !word.equals( "variable" ))
					out.append( word.toUpperCase( Locale.getDefault()) );
				else
					out.append( word );
				
			} else if (word.equals( Tag.numeric )) {
				if (wi.hasNext()) {
					word = wi.next();
					if (word.equals( "variable" )
							&& wi.hasNext()
							&& null != (word = wi.next())
							&& !word.equals( "variable" ))
						out.append( word.toUpperCase( Locale.getDefault()) );
					else // was  "numeric <word>" OR "numeric variable variable" => "numeric" + word
						out.append( Tag.numeric ).append( word );
					
				} else // "numeric" was last word...
					out.append( Tag.numeric );
				
			} else if (word.equals( Tag.phrase )) {
				if (wi.hasNext()) {
					word = wi.next();
					if (word.equals( "variable" )  &&
						wi.hasNext()               &&
						null != (word = wi.next()) &&
						!word.equals( "variable" ))
						out.append( word.toUpperCase( Locale.getDefault()) );
					else
						out.append( Tag.phrase ).append( word );
					
				} else
					out.append( Tag.phrase );
				
			} else
				out.append( word );
		}
		return out;
	}
		
	public String toString( int layout ) { return representamen.toString( layout );}
	
	// helpers
	static public boolean sane( Utterance u ) {return u != null && u.representamen.size() > 0;	}
	
	// test code...
	public static void test( Sign s, String utterance ) {
		Attributes a = new Utterance( new Strings( utterance )).match(s);
		audit.log( "utterance is: "
				+ utterance +"\n"
				+ (null == a ? "NOT matched." : "matched attrs is: "+ a ));
	}
	public static void main( String arg[]) {
		Where.doLocators();
		audit.log("Creating a pub:" +
						new Sofa().interpret(new Strings("entity create pub"))
		);

		// create a meeting repertoire
		Tags ts = new Tags();
		ts.add( new Tag( "i am meeting", "WHOM" ).attribute( "phrase", "phrase" ) );
		Sign s = new Sign().concept("meeting").content( ts );
		Spatial.addConcept("meeting");
		Temporal.addConcept("meeting");
		s.isSpatial();
		s.isTemporal();
		
		test( s, "i am meeting my brother at the pub at 7" );
		test( s, "i am meeting my sister  at the pub" );
		
		String utt = "my name is variable name";
		audit.log( ">"+ utt +"< to pattern is >"+ toPattern( utt ) +"<" );
}	}
