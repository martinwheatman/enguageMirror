package org.enguage.signs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.written.Autoload;
import org.enguage.repertoires.written.Load;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.pattern.Frag;
import org.enguage.signs.symbol.pronoun.Pronoun;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;

public class Signs extends TreeMap<Integer,Sign> {
	        static final long serialVersionUID = 0l;
	private static       Audit           audit = new Audit( "Signs" );
	
	private final String name;
	
	public Signs( String nm ) {super(); name=nm;}

	public Signs add( Sign[] signs ) {
		for (Sign sign: signs)
			insert( sign );
		return this;
	}

	private static int   total = 0;
	private static int clashes = 0;

	public Signs insert( Sign insertMe ) {
		int c = insertMe.cplex(),
			i = 0;
		while (i < 99 && containsKey( c + i )) {clashes++; i++;}
		if (i < 99) {
			total++;
			put( c + i, insertMe );
		} else
			audit.error( "failed to find place for sign:" );
		return this;
	}
	
	public static String stats() { return clashes +" clashes in a total of "+ total +" signs"; }
	
	public void remove( String id ) {
		ArrayList<Integer> removes = new ArrayList<>();
		
		// to prevent co-mod errors, load a list with the keys of those to be removed...
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			if ( id.equals( me.getValue().concept() ))
				removes.add( me.getKey() );
		}
		// ...and then remove them
		ListIterator<Integer> ri = removes.listIterator();
		while( ri.hasNext())
			remove( ri.next() );
	}
	public void show() {show( null );}
	public void show( String simpleFilter ) {
		int n=0;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			Sign s = me.getValue();
			if (simpleFilter == null || s.concept().contains(simpleFilter))
				Audit.LOG( s.toXml( n++, me.getKey() ));
	}	}
	public boolean save( String simpleFilter ) {return saveAs( simpleFilter, null );}
	public boolean saveAs( String simpleFilter, String cname ) {
		boolean rc = false;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			Sign s = me.getValue();
			if (s.concept().equals(simpleFilter)) {
				String fname = cname==null ? s.pattern().toFilename() : cname;
				if (s.toFile( Load.spokenName( fname ))) {
					s.concept( fname );
					rc = true;
		}	}	}
		return rc;
	}	
	/*
	 * remember which sign we interpreted last
	 */
	private int       posn = Integer.MAX_VALUE;
	public  void    foundAt( int i ) { posn = i; }
	public  int lastFoundAt() { return posn; }
	
	// ---------------------------------------------
	// used to save the positions of signs to ignore - now keys of signs to ignore
	private ArrayList<Integer> ignore = new ArrayList<>();
	public       List<Integer> ignore() {return ignore;}
	public  void               ignore( int i ) {
		audit.debug("Ignoring: "+ i );
		ignore.add( i );
	}
	public  void               ignoreNone() {ignore.clear();}
	// ---------------------------------------------

	public  static final int NO_INTERPRETATION = 0;
	private static       int interpretation   = NO_INTERPRETATION;
	private static       int interpretation() { return ++interpretation; }
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			str.append( me.getValue().toString());
			if (i.hasNext()) str.append( "\n" );
		}
		return str.toString();
	}

	public Reply mediate( Utterance u ) {
		if (Audit.allAreOn()) {
			audit.in( "mediate",
				"("+ name +"="+ size() +") "
				+ "'"+ u.toString() +"' "
		 		+ (ignore.isEmpty() ? "" : "avoiding "+ignore ));
			audit.debug( "concepts: ["+ Autoload.loaded().toString(Strings.CSV) +"]");
		}
		int here = interpretation(); // an ID for this interpretation
		
		Reply       r = new Reply();
		String answer = "";
		
		boolean done = false;
		Set<Map.Entry<Integer,Sign>> entries = entrySet();
		Iterator<Map.Entry<Integer,Sign>> ei = entries.iterator();
		while( ei.hasNext() && !done) {
			Map.Entry<Integer,Sign> e = ei.next();
			int complexity = e.getKey();

			if (ignore.contains( complexity ))
				audit.debug( "Skipping ignored: "+ complexity );
			
			else {

				Sign s = e.getValue(); // s knows if it is temporal!	
				// do we need to check if we're repeating ourselves?
				Attributes match = u.match( s );
				if (null == match) {
					;//	audit.debug( "NO match: "+ s.pattern().toString() )
				} else { // we have found a meaning! So I do understand...!
					
					Pronoun.update( match );
					
					if (Audit.allAreOn()) {
						// here: match=[ x="a", y="b+c+d", z="e+f" ]
						audit.debug( "matched: "+ complexity +"\n"+ s.toStringIndented() );
						audit.debug( "Concept: "+s.concept() +"," );
						if (match.isEmpty()) audit.debug( "   with: "+ match.toString() +"," );
						if (Context.context().isEmpty()) audit.debug( "    and: "+ Context.valueOf());
					}
					
					//audit.debug("setting "+ i +" to "+ here )
					s.interpretation = here; // mark here first as this understanding may be toxic!
					//audit.debug( "interpreting i="+ i +": "+ s.toText())
					if (Enguage.skipNo()) {
						audit.debug( ">>>>>>>SKIPPING: 'No PHRASE-X'" );
						Enguage.skipNo( false );
					
					} else if (Enguage.firstMatch()) {
						audit.debug( ">>>>>>Saving FIRST MATCH: "+ complexity );
						foundAt( complexity );
						Enguage.firstMatch( false );
						
					} else
						audit.debug( ">>>>>>not saving MATCH: "+ match.toString());

					//save the context here, for future use... before interp
					if (!Repertoires.transformation())
						match.toVariables();
					
					// if we've matched we must have understood/recognised
					r = new Reply()
							.answer( "ok" )
							.response( Response.N_OK );
							
					
					Context.push( match );
					r = s.intentions().mediate( r ); // may pass back DNU
					Context.pop();
					
					r.a.appendingIs( true );
					
					
					// if reply is DNU, this meaning is not appropriate!
					if (r.response() == Response.N_DNU) {
						audit.debug( "Signs.interpretation() returned DNU" );
						/* Comodification error?
						 * If, during interpretation, we've modified the repertoire
						 * by autoloading and we've not understood this we've 
						 * screwed the repertoire we're currently half-way through.
						 */
						//s = reassign( here );
					} else {
						s.interpretation = NO_INTERPRETATION; // tidy as we go
						answer = r.a.toString();
						done = true;
					}
					r.a.appendingIs( false );
				} // matched	
			}
		} // while more signs and not done
		if (Audit.allAreOn())
			audit.out( answer );
		return r.answer( answer );  
	}	
	
	public static void main( String[] args ) {
		Audit.on();
		Signs r = new Signs( "test" );
		r.insert(
				new Sign().pattern( new Frag(  "debug ", "x" ))
					.concept( "test" )
					.append( Intention.N_ALLOP, "debug X" )
			);
		r.insert(
				new Sign().pattern( new Frag(  "describe ", "x" ))
					.concept( "test" )
					.append( Intention.N_ALLOP, "describe X" )
			);
		r.insert(
			new Sign().pattern( new Frag(  "list repertoires ", "" ))
				.concept( "test" )
				.append( Intention.N_ALLOP, "list repertoires" )
		);
}	}
