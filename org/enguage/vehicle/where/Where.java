package org.enguage.vehicle.where;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Where {
	/* e.g. i need milk 'from' "the dairy aisle"
	 */

	public static final String  NAME = "where";
	public static final String LOCTR = "LOCATOR";
	public static final String LOCTN = "LOCATION";
	public static       Audit  audit = new Audit( NAME );

	private Where( String tor, String tion ) {
		locator( tor );
		location( new Strings( tion ));
		assigned( tor != null && tion != null );
	}

	// e.g. [ ["in"], ["at"], ["in", "front", "of"], ...
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
	
	private String  locator = new String(); //-- e.g. "in", "at", "in  front of"
	public  String  locator() { return locator; }
	private Where   locator( String l ) { locator = l; return this; }

	// --
	public static Where getWhere( ListIterator<String> ui, String term ) {
		Where w = null;
		if (ui.hasNext()) {
			String uttered = ui.next();
			int count = 1;
			if (Where.isLocator( uttered )) { // << see this -- only works on single length locr
				String locr = uttered;        // <<
				Strings locn = new Strings();
				if (ui.hasNext()) {
					uttered = ui.next(); // typically "the"
					count++;
					locn.add( uttered );
					boolean dontStop = null == term;
					while (ui.hasNext()) {
						uttered = ui.next();
						count++;
						if (dontStop || !uttered.equals( term )) {
							locn.add( uttered );
						} else {
							ui.previous();
							break;
					}	}
					if (( dontStop && !ui.hasNext()) ||
					    (!dontStop &&  ui.hasNext())    )
						w = new Where( locr, locn.toString()).assigned( true );
			}	}
			if (w==null) Strings.previous( ui, count );
		}
		return w;
	}

	// --
	public String toString() {
		return assigned() ? locator +" "+ location.toString( Strings.SPACED ) : "";
	}
	static public void doLocators( String locators ) {
		Strings locs = new Strings( locators, '/' );
		for (String l : locs) 
			locatorIs( l );
	}
	static private void testDoLocators() {
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
	public static void main( String args[]) {
		//Audit.allOn();
		//Audit.traceAll( true );
		
		// This should go into SofA
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			testDoLocators();
			
			// ok, let's do some testing...
//			testGet( "i am meeting my brother in paris at 10", "i am meeting my brother at 10" );
//			testGet( "i am meeting my brother at the pub at 10", "i am meeting my brother at 10" );
//			testGet( "underneath" );
}	}	}
