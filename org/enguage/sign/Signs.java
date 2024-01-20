package org.enguage.sign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.pattern.Frag;
import org.enguage.sign.symbol.pronoun.Pronoun;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

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
	
	private int   total = 0;
	private int clashes = 0;
	public  Signs insert( Sign insertMe ) {
		int i = 0;
		int c = insertMe.cplex();
		// crikey - decending order to put newset first! From old C coding!!
		while (i > -99 && containsKey( c + i )) {clashes++; i--;}
		if (i < 99) { // Arbitrary limit...
			total++;
			put( c + i, insertMe );
		} else
			audit.error( "failed to find place for sign:" );// not tested
		return this;
	}
	
	public void remove( String id ) {
		// to prevent co-mod errors, load a list with the keys of those to be removed...
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			if (id.equals( me.getValue().concept()))
				i.remove();
	}	}
	public boolean saveAs( String simpleFilter, String cname ) {
		boolean rc = false;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			Sign s = me.getValue();
			if (s.concept().equals(simpleFilter)) {
				String fname = cname==null ? s.pattern().toFilename() : cname;
				if (s.toFile( Concept.spokenName( fname ))) {
					s.concept( fname );
					rc = true;
		}	}	}
		return rc;
	}	

	// -----------------------------
	// -----------------------------
	// SKIP SIGNS Begin 
	// remember which sign we interpreted last
	private int       posn = Integer.MAX_VALUE;
	public  void    foundAt( int i ) { posn = i; }
	public  int lastFoundAt() { return posn; }
	
	// used to save the positions of signs to ignore - now keys of signs to ignore
	private ArrayList<Integer> ignore = new ArrayList<>();
	public       List<Integer> ignore() {return ignore;}
	public  void               ignore( int i ) {
		audit.debug("Ignoring: "+ i );
		ignore.add( i );
	}
	public  void               ignoreNone() {ignore.clear();}
	
	private boolean firstMatch = true;
	public  void    firstMatch( boolean b ) {firstMatch = b;}
	public  boolean firstMatch() {return firstMatch;}

	public  void reset( Strings reply ) { // called from Enguage.mediateSingle()
		audit.in( "reset", "r="+ reply );
		
		if (reply.begins( Config.dnu() ))
			ignoreNone();
		
		audit.out();
	}

	public  void    ignore( Strings cmd ) { // called from Engine.mediate("disamb")!
		audit.in( "ignore", "cmd="+ cmd );
		
		if (       Utterance.previous()               .equals( cmd  ) // first time around
			|| (   Utterance.previous().get(       0 ).equals( "no" ) // subsequent times
			   	&& Utterance.previous().copyAfter( 0 ).equals( cmd  ) // no, ...
			)
			&& Repertoires.signs().lastFoundAt() != Integer.MAX_VALUE)
		{
			firstMatch( true );
			Repertoires.signs().ignore( Repertoires.signs().lastFoundAt() );
		}
		audit.out( "Now avoiding: "+ Repertoires.signs().ignore() );
	}
	
	private void saveForIgnore( int complexity ) {
		if (firstMatch()) {
			audit.debug( "REDO: Remembering interpreted sign: "+ complexity );
			foundAt( complexity );
			firstMatch( false );
			
		} else
			audit.debug( "REDO: Nominal operation" );
	}
	// SKIP SIGNS End
	// -----------------------------
	// -----------------------------

	// -----------------------------
	// -- Audit begin
	private void auditIn( Utterance u ) {
		if (Audit.allAreOn()) {
			audit.in( "mediate",
				"("+ name +"="+ size() +") "
				+ "'"+ u.toString() +"' "
		 		+ (ignore.isEmpty() ? "" : "avoiding "+ignore ));
			audit.debug( "concepts: ["+ Autoload.loaded().toString(Strings.CSV) +"]");
	}	}
	private void auditMatch( int complexity, Sign s, Attributes match ) {
		if (Audit.allAreOn()) {
			// here: match=[ x="a", y="b+c+d", z="e+f" ]
			audit.debug( "matched: "+ complexity +"\n"+ s.toStringIndented() );
			audit.debug( "Concept: "+s.concept() +"," );
			if (match.isEmpty())
				audit.debug( "   with: "+ match.toString() +"," );
			if (Context.context().isEmpty())
				audit.debug( "    and: "+ Context.valueOf());
	}	}
	private void auditOut( String answer ) {
		if (Audit.allAreOn())
			audit.out( answer );
	}
	// --- Audit end
	// -----------------------------
	
	private Reply contextualMediate( Attributes match, Sign s ) {
		Context.push( match );
		Reply r = s.intentions().mediate(); // may pass back DNU
		Context.pop();
		return r;
	}

	public Reply mediate( Utterance u ) {

		auditIn( u );
		
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
					//audit.debug( "NO match: "+ s.pattern() +"("+ s.pattern().notMatched()+")" );
				} else { // we have found a meaning! So I do understand...!
					
					Pronoun.update( match );
					auditMatch( complexity, s, match );
					saveForIgnore( complexity );
						
					match.toVariables();
					r = contextualMediate( match, s );
					
					// if reply is DNU, this meaning is not appropriate!
					audit.debug( "Signs.interpretation() returned "+ r.type() );
					if (r.type() != Reply.Type.E_DNU) {
						answer = r.answer().toString();
						done = true;
				}	}	
			}
		} // while more signs and not done
		auditOut( answer +" (reply="+ r.toString() +")");
		return r.answer( answer ); 
	}
	
	// -----------------------------
	// --------- TEST CODE ---------
	// -----------------------------
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
	public String stats() {
		return clashes +" clashes in a total of "+ total +" signs";
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
				Audit.log( s.toXml( n++, me.getKey() ));
	}	}
	
	// UNUSED ---
	@Override
	public boolean equals(Object o) {return false;}
	@Override
	public int hashCode() {return 0;}
	// UNUSED ---
	
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
