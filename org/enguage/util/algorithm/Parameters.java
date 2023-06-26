package org.enguage.util.algorithm;

import java.util.ListIterator;

import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Parameters {
	
	static private Audit audit = new Audit( "Parameters" );
	
	static public Strings getFormal( ListIterator<String> si, String term, Strings rep ) {
		// this just does "a b and c" (need to be consecutive?)
		Strings params = new Strings();
		String s = si.hasNext() ? si.next() : null;
		while (s != null && !s.equalsIgnoreCase( term )) { // read single words TYPICALLY letters...
			rep.add( s ); // remember all strings
			if (s.equals( "and" )) {
				// get the last parameter...
				s = si.hasNext() ? si.next() : null;
				if (s != null) {
					rep.add( s );
					params.add( s );
				}
			} else if (s.length() != 1) {
				return null;
			} else
				params.add( s ); // only remember non-and words as params
			s = si.hasNext() ? si.next() : null;
		}
		if (s != null) rep.add( s );
		return params;
	}
	static public Strings getActual( ListIterator<String> li, String term, Strings rep ) {
		audit.in( "getParams", Strings.peek( li ) +", term="+ term +", rep="+ rep.toString());
		// the number of coffees and the number of teas.
		// TODO: needs access to reply = enguage( utterance )
		Strings params = new Strings(), param = new Strings();
		String s = null;
		while (li.hasNext() && null != (s = li.next() ) && !s.equals( term )) {
			rep.add( s );
			if (s.equals( "and" )) {
				if (param.size() > 0) {
					params.add( param.toString() );
					param = new Strings();
				}
			} else
				param.add( s );
		}
		if (s != null && s.equals( term ))
			rep.add( s );
		if (param.size() > 0)
			params.add( param.toString() );
		return audit.out( params );
	}
	public static void test( String s, boolean formal ) {
		Strings rep = new Strings();
		ListIterator<String> li = new Strings( s ).listIterator();
		Strings params = formal ? getFormal( li, "is", rep ) : getActual( li, "is", rep );
		audit.debug( (formal ? "Form":"Actu")+ "als: ["+ params.toString( Strings.DQCSV ) +"]" );
		audit.debug( "from: ["+ rep.toString( Strings.CSV ) +"], peek:"+ Strings.peek( li ));
	}
	public static void main( String[] args) {
		test( "1 is 1", true );
		test( "a b and c is 1", true );
		test( "a b and c is 1", false );
}	}
