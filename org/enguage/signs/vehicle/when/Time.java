package org.enguage.signs.vehicle.when;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Time {
	static private Audit audit = new Audit( "Time" );
	
	// a few statics to show representation of dates
	// Some time constants relating to the way time is represented as a long/string 
	final	 static long SECOND    = 1;
	final	 static long MINUTE    = 100 * SECOND;
	final	 static long HOUR      = 100 * MINUTE;
	final	 static long DAY       = 100 * HOUR;
	final	 static long MONTH     = 100 * DAY;
	final	 static long YEAR      = 100 * MONTH;
	final	 static long DECADE    =  10 * YEAR;
	final	 static long CENTURY   =  10 * DECADE;
	final	 static long MILLENIUM =  10 * CENTURY; // 10, 000, 000, 000 / 10B / 1 00/00, 00:00:00 // 1000000	

	public static final long unassignedTime  = 888888;
	public static final long unassigned      =     88; // hour, minute or second
	
	public static int time( long t ) {return (int)(t % Time.DAY); }
	
	static boolean doHour( When w, ListIterator<String> si ) {
		//audit.in( "doHour", "w="+ w.toString() +", si="+ si.nextIndex() );
		boolean rc = false;
		if (si.hasNext())
			try {
				int h = Integer.valueOf( si.next());
				if (h<0 || h>24)
					w.spurious( true );
				else
					rc = true;
				if (Strings.doString( "pm", si )) {
					w.pm();
					if (h>12)
						w.spurious( true );
					else if (h<12)
						h += 12;
					rc = true;
				} else if (Strings.doString( "am", si )) {
					w.am();
					if (h>12)
						w.spurious( true );
					rc = true;
				} else if (Strings.doString( "o'clock", si )) {
					if (h>12) w.spurious( true );
					rc = true;
				}
				if (rc) {
					w.hour( h );
				} else {
					w.spurious( false );
					si.previous();
				}
			} catch (NumberFormatException nfe) {
				si.previous();
			}
		return rc; //audit.out( rc );
	}

	static boolean doMinute( When w, ListIterator<String> si ) {
		//audit.in( "doMinute", "w="+ w.toString() +", si="+ si.nextIndex() );
		boolean rc = false;
		if (si.hasNext())
			try {
				int m = Integer.valueOf( si.next());
				if (m<0 || m>59) w.spurious( true );
				if (Strings.doString( "pm", si )) {
					w.pm();
					if (w.hour()>12)
						w.spurious( true );
					else if (w.hour()<12)
						w.hour( w.hour() + 12 );
				} else if (Strings.doString( "am", si )) {
					w.am();
					if (w.hour()>12) w.spurious( true );
				}
				w.minute( m );
				rc = true;
			} catch (NumberFormatException nfe) {
				si.previous();
			}
		return rc; //audit.out( rc );
	}

	static boolean doSecond( When w, ListIterator<String> si ) {
		//audit.in( "doSecond", "w="+ w.toString() +", si="+ si.nextIndex() );
		boolean rc = false;
		if (si.hasNext())
			try {
				int s = Integer.valueOf( si.next() );
				if (s<0 || s>60) w.spurious( true );
				if (Strings.doString( "pm", si )) {
					w.pm();
					if (w.hour()>12)
						w.spurious( true );
					else if (w.hour()<12)
						w.hour( w.hour() + 12 );
				} else if (!(Strings.doString( "am", si ))) {
					w.am();
					if (w.hour()>12)
						w.spurious( true );
				}
				w.second( s );
				rc = true;
			} catch (NumberFormatException nfe) {
				si.previous();
			}
		return rc; //audit.out( rc );
	}

	static boolean doTime( When w, ListIterator<String> si ) {
		//audit.in( "doTime", ""+ si.nextIndex() );
		boolean rc = false;
		if (doHour( w, si )) {
			rc = true;
			if (Strings.doString( ":", si )) {
				if (doMinute( w, si )) {
					if (Strings.doString( ":", si )) {
						if (!(doSecond( w, si )))
							si.previous(); // replace ":" after minute
					}
				} else
					si.previous(); // replace ":" after hour
		}	}
		return rc; //audit.out( rc );
	}
	static boolean doAt( When w, ListIterator<String> si ) {
		//Relative.audit.in( "doAt", "w="+ w.toString() +", si="+ si.nextIndex());
		boolean rc = false;
		// at already found
		if (doTime( w, si )) { // optional -- scale <- month
			rc = true;
			if (Strings.doString( "on", si )) {
				if (Day.doDayName( w, si ))
					rc = true; // done enough, following here is speculative
				// no else, could be "sunday the 5th November"
				if (Strings.doString( "the", si )) 
					rc = Absolute.doFullDate( w, si );
			} else
				Relative.doRelativeDay( w, si );
		}
		return rc; //Relative.audit.out( rc );
	}

	// --
	private static void testGet( String s ) {
		audit.in( "testGet", s );
		Strings sa = new Strings( s );
		ListIterator<String> si = sa.listIterator();
		When w = new When();
		if (doTime( w, si ))
			Audit.log( "'"+ s +"' means '"+ sa.toString( Strings.SPACED ) +"' ("+ w.toString() +")" );
		else
			Audit.log( "'"+ s +"' means just that!" );
		audit.out();
	}
	public static void main( String args[]) {
		/*
		testGet( "i met my brother at the pub" );
		testGet( "i met my brother at 5pm at the pub" );
		testGet( "i am meeting my brother at 17:07:63" );
		testGet( "i will be with my brother from 5 pm to 10 pm" );
		testGet( "i am with my brother until 5 o'clock tomorrow in the pub" );
		// -- */
		audit.tracing = true;
		audit.on();
		testGet( "5pm" );
		testGet( "7:30am" );
		testGet( "7:30pm" );
}	}
