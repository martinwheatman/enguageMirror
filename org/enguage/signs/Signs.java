package org.enguage.signs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.repertoire.Concepts;
import org.enguage.repertoire.Repertoire;
import org.enguage.signs.intention.Intention;
import org.enguage.signs.vehicle.Utterance;
import org.enguage.signs.vehicle.pattern.Patte;
import org.enguage.signs.vehicle.pronoun.Pronoun;
import org.enguage.signs.vehicle.reply.Reply;
import org.enguage.signs.vehicle.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;

public class Signs extends TreeMap<Integer,Sign> {
	        static final long serialVersionUID = 0l;
	private static       Audit           audit = new Audit( "Signs" );
	
	//private final String name;
	
	public Signs( String nm ) { super(); /*name=nm;*/ }

	public Signs add( Sign[] signs ) {
		for (Sign sign: signs)
			insert( sign );
		return this;
	}

	static private int   total = 0;
	static private int clashes = 0;

	public Signs insert( Sign insertMe ) {
		int c = insertMe.cplex(),
			i = 0;
		while (i < 99 && containsKey( c + i )) {clashes++; i++;}
		if (i < 99) {
			total++;
			put( c + i, insertMe );
		} else
			audit.ERROR( "failed to find place for sign:" );
		return this;
	}
	
	static public String stats() { return clashes +" clashes in a total of "+ total +" signs"; }
	
	private void swap( int a, int b) {
		if (a<0 || b<0 || a>=size() || b>=size()) {
		} else if (a == b) { // nothing to swap
		} else if (a > b) {  // inverting swap
			swap( b, a );
		} else {
			Sign tmp = get( a );
			put( a, get( b ));
			put( b, tmp );
	}	}
	public void reorder() {
		if (ignore().size() > 0) { // not needed unless we've no signs
			/* OK, here we've said "tiat", foundAt=35
			 * AND...
			 * THEN we've said "No, tiat" - ignoring [35], foundAt=42
			 * SO to tidy up:
			 * SWAP SIGNS
			 * FROM	sign order=..., 35, ..., 42, ..., 53
			 */
			int swap = ignore().get( 0 ), // 35
				with = lastFoundAt();     // 42
			//audit.debug( "OK SWAPPING "+ swap +" WITH "+ with );
			swap( swap, with );	
			/*
			 * TO	sign order=..., 42, ..., 35, ..., 53,
			 * 
			 * BUT ignore remains as [35]?
			 * Therefore replace INGORE val 35 with 42.
			 */
			//audit.debug("Ignores was "+ ignore().toString());
			ignore().set( ignore().indexOf( swap ), with );
			//audit.debug("Ignores now "+ Enguage.e.signs.ignore().toString());
			
			// readjust where this was found too!
			foundAt( swap );
	}	}
	
	public void remove( String id ) {
		ArrayList<Integer> removes = new ArrayList<Integer>();
		
		// to prevent co-mod errors, load a list with the keys of those to be removed...
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			if ( id.equals( me.getValue().concept() ))
				removes.add( me.getKey() );
		}
		// ...and then remove them
		ListIterator<Integer> ri = removes.listIterator();
		while( ri.hasNext())
			remove( ri.next() );
	}
	public void show() {
		int n=0;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			Sign s = me.getValue();
			Audit.LOG( s.toXml( n++, me.getKey() ));
	}	}
	public void show( String simpleFilter ) {
		int n=0;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			Sign s = me.getValue();
			if (s.concept().equals(simpleFilter))
				Audit.LOG( s.toXml( n++, me.getKey() ));
	}	}
	public boolean save( String simpleFilter ) {return saveAs( simpleFilter, null );}
	public boolean saveAs( String simpleFilter, String cname ) {
		boolean rc = false;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			Sign s = me.getValue();
			if (s.concept().equals(simpleFilter)) {
				String fname = cname==null ? s.pattern().toFilename() : cname;
				if (s.toFile( Concepts.spokenName( fname ))) {
					s.concept( fname );
					rc = true;
		}	}	}
		return rc;
	}	
	/*
	 * remember which sign we interpreted last
	 */
	private static final int listStart = -1;
	private              int      posn = listStart;
	public void    foundAt( int i ) { posn = i; }
	public int lastFoundAt() { return posn; }
	
	// ---------------------------------------------
	// used to save the positions of signs to ignore - now keys of signs to ignore
	private ArrayList<Integer> ignore = new ArrayList<Integer>();
	public  ArrayList<Integer> ignore() { return ignore; }
	public  void               ignore( int i ) {
		if (i == -1)
			ignoreNone();
		else {
			//audit.debug("Sign.numToAvoid( "+ i +" )");
			ignore.add( i );
	}	}
	public  void               ignoreNone() { ignore.clear(); }
	// ---------------------------------------------

	static public  final int noInterpretation = 0;
	static private       int interpretation   = noInterpretation;
	static private       int interpretation() { return ++interpretation; }
	
	public String toString() {
		String str = "";
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			str += me.getValue().toString();
			if (i.hasNext()) str += "\n";
		}
		return str;
	}
	private Sign reassign( int here ) {
		/* Comodification error?
		 * If, during interpretation, we've modified the repertoire
		 * by autoloading and we've not understood this we've 
		 * screwed the repertoire we're currently half-way through.
		 */
		// Reassign s here!
		/* Then find our way back "here". The sign-scape will be 
		 * peppered with new signs, so there may not be a complete 
		 * trail of signs where skipme == true. We can work our way 
		 * back to this point as ahead of us there will be a complete 
		 * trail of signs where skipme == noId.
		 * N.B. There is no "jump to the end", so this alg involves 
		 * one read through of the whole list.
		 */
		Sign s = null;
		Set<Map.Entry<Integer,Sign>> entries = entrySet();
		Iterator<Map.Entry<Integer,Sign>> ei = entries.iterator();
		while( ei.hasNext()) {
			Map.Entry<Integer,Sign> e = (Map.Entry<Integer,Sign>)ei.next();
			s = e.getValue();
			if( s.interpretation == here ) { // we are back "here"
				s.interpretation = noInterpretation; // tidy up this sign.
				break; // return to processing the list...
		}	}
		return s;
	}

	// a simple cognitive model ?
	public Reply mediate( Utterance u ) {
		/*
			audit.in( "mediate",
				" ("+ name +"="+ size() +") "
				+ " '"+ u.toString() +"' "
		 		+ (ignore.size()==0?"":("avoiding "+ignore)));
			audit.debug( "concepts: ["+ Autoload.loaded().toString(Strings.CSV) +"]");
		// -- */
		int here = interpretation(); // an ID for this interpretation
		
		Reply       r = new Reply();
		String answer = new String();
		
		boolean done = false;
		Set<Map.Entry<Integer,Sign>> entries = entrySet();
		Iterator<Map.Entry<Integer,Sign>> ei = entries.iterator();
		while( ei.hasNext() && !done) {
			Map.Entry<Integer,Sign> e = (Map.Entry<Integer,Sign>)ei.next();
			int complexity = e.getKey();

			if (!ignore.contains( complexity )) {
				Sign s = e.getValue(); // s knows if it is temporal!	
				//TODO: removed noInter check -- need to check if we're repeating ourselves?
				Attributes match = u.match( s );
				if (null == match) {
					;//	audit.debug( "NO match: "+ s.pattern().toString() );
				} else { // we have found a meaning! So I do understand...!
					
					Pronoun.update( match );
					
					// here: match=[ x="a", y="b+c+d", z="e+f" ]
					audit.debug( "matched:\n"+ s.toStringIndented() );
					audit.debug( "Concept: "+s.concept() +"," );
					audit.debug( "   with: "+ match.toString() +"," );
					audit.debug( "    and: "+ Context.valueOf());
					
					//audit.debug("setting "+ i +" to "+ here );
					s.interpretation = here; // mark here first as this understanding may be toxic!
					//audit.debug( "interpreting i="+ i +": "+ s.toText());
					
					foundAt( complexity ); /* This MUST be recorded before interpretation,
					 * below: this will be overwritten during interpretation, eventually
					 * being left with the last in the chain of signs in an interpretation.
					 */
					//audit.debug( "Found@ "+ i +":"+ get( complexity ).content().toLine() +":"+ match.toString() +")");

					//save the context here, for future use... before interp
					if (!Repertoire.transformation())
						match.toVariables();
					
					
					r = new Reply();
					r.response( Response.OK ); // if we've matched we must have understood/recognised
					r.answer( "ok" );
					
					// TODO: No need for context, now? read from (cached) variables?
					Context.push( match );
					r = s.interpret( r ); // may pass back DNU
					Context.pop();
					
					r.a.appendingIs( true );
					
					/* May have modified repertoire by autoloading.
					 * ignores now works on key (complexity)
					 * 
					 * 1  2  3  4  5              original
					 * 1  2 -1  3 -1 -1  4 -1  5  comodified
					 * 1  2  3  4  5  6  7  8  9  eventual
					 * 
					 * So Ignores got from 2, 4 to 2, 7.
					 */
					
					// if reply is DNU, this meaning is not appropriate!
					if (r.response() == Response.DNU) {
						audit.debug( "Signs.interpretation() returned DNU" );
						/* Comodification error?
						 * If, during interpretation, we've modified the repertoire
						 * by autoloading and we've not understood this we've 
						 * screwed the repertoire we're currently half-way through.
						 */
						s = reassign( here );
					} else {
						s.interpretation = noInterpretation; // tidy as we go
						answer = r.a.toString();
						done = true;
					}
					r.a.appendingIs( false );
				} // matched	
			}	
		} // while more signs and not done
		return r.answer( answer ); //(Reply) audit.out( r.answer( answer )); // 
	}	
	// help...

	public static void main( String[] args ) {
		// help test...
		audit.tracing = true;
		Signs r = new Signs( "test" );
		r.insert(
				new Sign().pattern( new Patte(  "debug ", "x" ))
					.concept( "test" )
					.append( new Intention( Intention.allop, "debug X" ))
			);
		r.insert(
				new Sign().pattern( new Patte(  "describe ", "x" ))
					.concept( "test" )
					.append( new Intention( Intention.allop, "describe X" ))
					.help( "where x is a repertoire" )
			);
		r.insert(
			new Sign().pattern( new Patte(  "list repertoires ", "" ))
				.concept( "test" )
				.append( new Intention( Intention.allop, "list repertoires" ))
				.help( "" )
		);
//		Audit.log( r.helpedToString( "test" ));
//		Audit.log( r.helpedToHtml(   "test" ));
}	}
