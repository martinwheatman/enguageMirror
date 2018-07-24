package org.enguage.vehicle.where;

import java.util.ListIterator;

import org.enguage.object.space.Overlay;
import org.enguage.object.space.Sofa;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Shell;
import org.enguage.util.Strings;

import org.enguage.vehicle.where.Where;

import java.util.ArrayList;

public class Where {
	/* in the bath - 
	 * at the pub  - class pub pub/the assigned (or unassigned?)
	 * in the pub  - create pub as a location
	 * in Paris    - do we need to know paris is a place? Ask where is Paris: Paris is the capital of France.
	 * in the capital - the capital of which country?
	 * from the dairy aisle - i need milk
	 */

	public static final String     NAME = "where";
	public static final String  LOCATOR = "LOCATOR";
	public static final String LOCATION = "LOCATION";
	public static       Audit     audit = new Audit( NAME );

	public Where() {}
	public Where( String tor, String tion ) {
		locator( tor );
		location( new Strings( tion ));
		assigned( tor != null && tion != null );
	}
	public Where( String tor, Strings tion ) {
		locator( tor);
		location( tion );
		assigned( tor  != null && tion != null && tion.size()>0 );
	}
	public Where( Attributes a ) { this( a.get( LOCATOR ), a.get( LOCATION )); }

	// e.g. "in", "at", "on", "in front of"
	static private ArrayList<Strings> locators = new ArrayList<Strings>();
	static public  boolean isLocator( String l ) { return locators.contains( new Strings( l )); }
	static public  void    locatorIs( String l ) { locatorIs( new Strings( l )); }
	static public  void    locatorIs( Strings l ){ if (l.size() > 0) locators.add( l ); }

	private boolean assigned = false;
	public  boolean assigned() { return assigned; }
	public  Where   assigned( boolean l ) { assigned = l; return this; }

	private Strings location = new Strings(); //-- e.g. ["the", "pub"]
	public  Strings location() { return location; }
	private Where   location( Strings l ) { location = l; return this; }
	private boolean location( ListIterator<String> si ) {
		if (si.hasNext()) {
			String t = si.next();  // TODO: Entity.getName( si );?
			if (new Sofa().interpret( new Strings( "entity exists "+ t )).equals( Shell.SUCCESS ))
			{	location.add( t );
				si.remove();
				return true;
			} else if (t.equals( "the" )) {
				String p = si.next();
				if (new Sofa().interpret( new Strings( "entity exists "+ p ))
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
	private Where  locator( String l ) { locator = l; return this; }
	private void   locator( ListIterator<String> si ) {
		//audit.in( "locator", " '"+ locators.size() +"', "+ si );
		int n = 0;
		for (Strings pattern : locators ) {
			//audit.debug( "Checking pattern: "+ pattern.toString( Strings.SPACED ));
			if (0 != (n = pattern.matches( si ))) {
				locator = Strings.getString( si, n );
				//audit.debug( "locator candidate: "+ locator );
				
				assigned( location( si ));
				if (assigned()) {
					// remove locator...
					Strings.previous( si, n );
					Strings.removes( si, n );
					//audit.out();
					return; // ...and we're done!
				}
				// replace locator
				Strings.previous( si, n );
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
	public static Where getWhere( String uttered, String term, ListIterator<String> ui ) {
		Where w = null;
		if (Where.isLocator( uttered )) {
			String locator = uttered;
			Strings locs = new Strings();
			if (ui.hasNext()) {
				uttered = ui.next(); // typically "the"
				locs.add( uttered );
				boolean dontStop = null == term;
				while (ui.hasNext()) {
					uttered = ui.next();
					if (dontStop || !uttered.equals( term )) {
						locs.add( uttered );
					} else {
						ui.previous();
						break;
				}	}
				if (( dontStop && !ui.hasNext()) ||
				    (!dontStop &&  ui.hasNext())    )
					w = new Where( locator, locs.toString()).assigned( true );
		}	}		
		return w;
	}

	// --
	public String toString() {
		return assigned() ? locator +" "+ location.toString( Strings.SPACED ) : "";
	}
	public Attributes toAttributes() {
		return new Attributes()
					.add( LOCATOR, locator())
					.add( LOCATION, location().toString( Strings.SPACED ));
	}
	static public void doLocators( String locators ) {
		Strings locs = new Strings( locators, '/' );
		for (String l : locs) 
			locatorIs( l );
	}
	static private void doLocators() {
		// locators need to be in decreasing length...
		locatorIs( "to the left of" );
		locatorIs( "to the right of" );
		locatorIs( "in front of" );
		locatorIs( "on top of" );
		locatorIs( "behind" );
		locatorIs( "in" );
		locatorIs( "on" );
		locatorIs( "from" );
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
						" '"+ sa.toString() +"' "	: " just that!")
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
			
			audit.log( "Creating paris:"+ new Sofa().interpret( new Strings( "entity create paris" )));
			testGet( "i am meeting my brother in paris at 10", "i am meeting my brother at 10" );
			
			audit.log( "Creating a pub:"+ new Sofa().interpret( new Strings( "entity create pub" )));
			testGet( "i am meeting my brother at the pub at 10", "i am meeting my brother at 10" );
			
			testGet( "underneath" );
}	}	}
