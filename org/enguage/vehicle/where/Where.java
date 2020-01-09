package org.enguage.vehicle.where;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.objects.Variable;
import org.enguage.objects.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Where {
	/* e.g. i need milk 'from' "the dairy aisle"
	 */

	static public final String  NAME = "where";
	static public       Audit  audit = new Audit( NAME );
	
	static public final String LOCTR = "LOCATOR";
	static public final String LOCTN = "LOCATION";
	static public void clearLocation() {
		Variable.unset( Where.LOCTN );
		Variable.unset( Where.LOCTR );
	}

	private Where( Strings locr, Strings locn ) {
		addLocator( locr );
		addLocation( locn );
		assigned( locr != null && locn != null );
	}
	// all possible locators: spatially something can be ... .
	// e.g. [ ["in"], ["at"], ["in", "front", "of"], ...
	static private ArrayList<Strings> locators = new ArrayList<Strings>();
	static private Strings isLocator( ListIterator<String> li ) {
		Strings rc = new Strings();
		for (Strings locator : locators)
			if (0 != (rc = locator.extract( li )).size())
				return rc;
		return null;
	}
	static private void    locatorIs( String l ) { locatorIs( new Strings( l )); }
	static public  void    locatorIs( Strings l ){ if (l.size() > 0) locators.add( l ); }

	private boolean assigned = false;
	public  boolean assigned() { return assigned; }
	public  Where   assigned( boolean l ) { assigned = l; return this; }

	// Was: location=["the", "pub"]
	// Now: location=[ ["the", pub"] ]
	private ArrayList<Strings> location = new ArrayList<Strings>();
	public  ArrayList<Strings> location() { return location; }
	private Where           addLocation( Strings l ) { location.add( l ); return this; }
	public  String             locationAsString( int n ) {return location.get( n ).toString();}
	
	// Was: locator="at" -- not "in front of"!!!
	// 2be: locator=[ ["at"] ]
	private ArrayList<Strings> locator = new ArrayList<Strings>(); //-- e.g. "in", "at", "in front of"
	public  ArrayList<Strings> locator() { return locator; }
	private Where           addLocator( Strings l ) { locator.add( l ); return this; }
	public  String             locatorAsString( int n ) {return locator.get( n ).toString();}

	// --
	public static Where getWhere( ListIterator<String> ui, String term ) {
		Where w = null;
		if (ui.hasNext()) {
			Strings locr;
			if (null != (locr = Where.isLocator( ui ))) { // << see this -- only works on single length locr
				Strings locn = new Strings();
				if (ui.hasNext()) {
					String uttered = ui.next(); // typically "the"
					locn.add( uttered );
					boolean dontStop = null == term;
					while (ui.hasNext()) {
						uttered = ui.next();
						if (dontStop || !uttered.equals( term )) {
							locn.add( uttered );
						} else {
							ui.previous();
							break;
					}	}
					if (( dontStop && !ui.hasNext()) ||
					    (!dontStop &&  ui.hasNext())    )
						w = new Where( new Strings( locr ), locn ).assigned( true );
				}
				// undo changes to ui...
				if (w==null) Strings.previous( ui, locr.size() + locn.size() );
		}	}
		return w;
	}

	// --
	public String toString() {
		return assigned() ? locatorAsString( 0 ) +" "+ locationAsString( 0 ) : "";
	}
	static public void doLocators( String locators ) {
		Strings locs = new Strings( locators, '/' );
		for (String l : locs) 
			locatorIs( l );
	}
	//
	// -- test code
	//
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
	public static void main( String args[]) {
		//Audit.allOn();
		//Audit.traceAll( true );
		
		// This should go into SofA
		Overlay.Set( Overlay.Get());
		if (!Overlay.attach( NAME ))
			audit.ERROR( "Ouch!" );
		else {
			testDoLocators();
			
			// ok, let's do some testing...
//			testGet( "i am meeting my brother in paris at 10", "i am meeting my brother at 10" );
//			testGet( "i am meeting my brother at the pub at 10", "i am meeting my brother at 10" );
//			testGet( "underneath" );
}	}	}
