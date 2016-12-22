package com.yagadi.enguage.expression.where;

import java.util.ListIterator;
import java.util.ArrayList;

import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.sofa.Sofa;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Where {
	/* in the bath - 
	 * at the pub  - class pub pub/the assigned (or unassigned?)
	 * in the pub  - create pub as a location
	 * in Paris    - do we need to know paris is a place? Ask where is Paris: Paris is the capital of France.
	 * in the capital - the capital of which country?
	 */

	public static final String NAME = "where";
	public static       Audit audit = new Audit( NAME );

	public Where() {}
	public Where( String tor, String tion ) { locator( tor); location( new Strings( tion ));}
	public Where( Attributes a ) { this( a.get( "locator" ), a.get( "location" )); }

	// e.g. "in", "at", "on", "in front of"
	static private ArrayList<Strings> locators = new ArrayList<Strings>();
	static public  boolean isLocator( String l ) { return locators.contains( new Strings( l )); }
	static public  void    locatorIs( String l ) { locators.add( new Strings( l )); }

	private boolean assigned = false;
	public  boolean assigned() { return assigned; }
	public  Where   assigned( boolean l ) { assigned = l; return this; }

	private Strings location = new Strings(); //-- e.g. ["the", "pub"]
	public  Strings location() { return location; }
	public  Where   location( Strings l ) { location = l; return this; }
	public  boolean location( ListIterator<String> si ) {
		if (si.hasNext()) {
			String t = si.next();
			if (new Sofa( null ).interpret( new Strings( "entity exists "+ t )).equals( Shell.SUCCESS ))
			{	location.add( t );
				si.remove();
				return true;
			} else if (t.equals( "the" )) {
				String p = si.next();
				if (new Sofa( null ).interpret( new Strings( "entity exists "+ p ))
						.equals( Shell.SUCCESS )) {
					location.add( "the" );
					location.add( p );
					si.remove();
					si.previous();
					si.remove();
					return true;
		}	}	}
		return false;
	}
	
	private String locator = new String(); //-- e.g. "in", "at", "in  front of"
	public  String locator() { return locator; }
	public  Where  locator( String l ) { locator = l; return this; }
	private void   locator( ListIterator<String> si ) {
		//audit.in( "locator", "'"+ locators.size() +"', "+ si );
		int n = 0;
		for (Strings pattern : locators ) {
			//audit.debug( "Checking pattern: "+ pattern.toString( Strings.SPACED ));
			if (0 != (n = pattern.matches( si ))) {
				locator = Strings.getString( si, n );
				//audit.debug( "locator candidate: "+ locator );
				
				assigned( location( si ));
				if (assigned()) {
					// remove locator...
					Strings.previousN( si, n );
					Strings.removeN( si, n );
					//audit.out();
					return; // ...and we're done!
				}
				// replace locator
				Strings.previousN( si, n );
		}	}
		//audit.out();
	}

	// --
	private static Where getWhere( Where w, ListIterator<String> si ) {
		while (!w.assigned() && si.hasNext()) {
			w.locator( si );
			if (!w.assigned() && si.hasNext()) si.next();
		}
		return w;
	}
	private static Where getWhere( Where w, Strings sa ) {
		if (sa != null) getWhere( w, sa.listIterator() );
		return w;
	}
	public static Where getWhere( Strings sa ) { return getWhere( new Where(), sa ); }
	
	// --
	public String toString() {
		return assigned() ? locator +" "+ location.toString( Strings.SPACED ) : "";
	}
	public Attributes toAttributes() {
		return new Attributes()
					.add( "locator", locator())
					.add( "location", location().toString( Strings.SPACED ));
	}
	static public void doLocators() {
		// locators need to be in decreasing length...
		locatorIs( "to the left of" );
		locatorIs( "to the right of" );
		locatorIs( "in front of" );
		locatorIs( "on top of" );
		locatorIs( "behind" );
		locatorIs( "in" );
		locatorIs( "on" );
		locatorIs( "under" );
		locatorIs( "underneath" );
		locatorIs( "over" );
		locatorIs( "at" );
	}
	//
	// -- test code
	//
	private static void testGet( String s ) { testGet( s, s ); } // default to true
	private static void testGet( String request, String result ) {
		boolean shouldBeAssigned = !request.equals( result );
		Strings sa = new Strings( request );
		Where w = Where.getWhere( sa );
		if (shouldBeAssigned && !w.assigned())
			audit.ERROR( "w should be assigned!! >"+ w.toString() +"<" );
		else if (!shouldBeAssigned && w.assigned())
			audit.FATAL( "w is assigned when shouldn't be!" );
		else if (!result.equals("") && !result.equals( sa.toString( Strings.SPACED )))
			audit.FATAL( "result is incorrect: "+ sa.toString( Strings.SPACED ) +" (result="+ result +")!" );
		else
			audit.log( "'"+ request  +"' means "+
					(w.assigned() || shouldBeAssigned?
						"'"+ sa.toString( Strings.SPACED ) +"'"	: " just that!")
			        +"\n  where='"+ w.toString() +"', assigned="+ w.assigned() );
		audit.log( "" );
	}
	public static void main( String args[]) {
		//Audit.allOn();
		//Audit.traceAll( true );
		
		// This should go into SofA
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			doLocators();
			
			// ok, let's do some testing...
			audit.log( "Sofa: Ovl is: "+ Overlay.Get().toString());
			
			audit.log( "Creating paris:"+ new Sofa( null ).interpret( new Strings( "entity create paris" )));
			testGet( "i am meeting my brother in paris at 10", "i am meeting my brother at 10" );
			
			audit.log( "Creating a pub:"+ new Sofa( null ).interpret( new Strings( "entity create pub" )));
			testGet( "i am meeting my brother at the pub at 10", "i am meeting my brother at 10" );
			
			testGet( "underneath" );
}	}	}
