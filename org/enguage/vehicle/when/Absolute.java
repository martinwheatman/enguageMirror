package org.enguage.vehicle.when;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.when.Month;
import org.enguage.vehicle.when.Time;
import org.enguage.vehicle.when.When;

public class Absolute {

	static private Audit audit = new Audit( "Absolute" );
	public static final long unassignedDate = 88888888;
	
	static private String exactPrefix = "on";
	static public  String exactPrefix() {return exactPrefix;}
	static public  void   exactPrefix(String s) {exactPrefix = s;}
	
	static private String approxPrefix = "in";
	static public  String approxPrefix() {return approxPrefix;}
	static public  void   approxPrefix(String s) {approxPrefix = s;}
	
	static boolean doDate( When w, ListIterator<String> si ) {
		// remove "...on the] 25th December 2015..." and return When
		audit.in( "doDate", "w="+ w.toString() +", si="+ si.nextIndex());
		boolean rc = false;
		if (si.hasNext()) {
			try {
				int day = Integer.valueOf( si.next());
				audit.debug( "found day "+ day );
				if (day>0 && (   Strings.doString( "st", si ) || Strings.doString( "nd", si )
						       || Strings.doString( "rd", si ) || Strings.doString( "th", si )))
				{
					w.day( day );
					rc = true;
				} else
					si.previous();
			} catch (NumberFormatException nfe) {
				si.previous();
			}
			if (rc && w.scale() > Time.DAY ) {
				w.scale( Time.DAY );
		}	}
		return audit.out( rc );
	}
	static boolean doYear( When w, ListIterator<String> si ) {
		audit.in( "doYear", "w="+ w.toString() +", si="+ si.nextIndex());
		boolean rc = false;
		if ( si.hasNext()) {
			try {
				int year = Integer.valueOf( si.next());
				if (year != 0) {
					w.year( year );
					rc = true;
				}
			} catch (NumberFormatException nfe) {
				si.previous();
		}	}
		return audit.out( rc );
	}
	static boolean doFullDate( When w, ListIterator<String> si ) {
		boolean rc = false;
		audit.in( "doDate", "w="+ w.toString() +", si="+ si.nextIndex());
		if (doDate( w, si )) {
			rc = true; // done enough, following here is speculative
			if (Strings.doString( "of", si ))
				if (Month.doMonth( w, si ))
					doYear( w, si );
				else
					si.previous(); // replace "of"
		}
		return audit.out( rc );
	}	}
