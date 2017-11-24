package org.enguage.vehicle.when;

import org.enguage.util.Audit;

import org.enguage.vehicle.when.Day;
import org.enguage.vehicle.when.Duration;
import org.enguage.vehicle.when.Moment;
import org.enguage.vehicle.when.Time;
import org.enguage.vehicle.when.When;

public class Duration {
	private static String NAME = "Duration";
	private static Audit audit = new Audit( NAME );

	/* Keep this private as it is not generally applicable
	 * only called currently, if years not equal... 
	 */
	private int leapDaysSpanned( long start, long end ) {
		audit.in( "leapDaysSpanned", "start="+ start +", end="+ end );
		int leapDaysSpanned = 0;
		if (start < end) {
			if (Day.leapYear( start ) && Day.dayOfYear( start ) <= 59) leapDaysSpanned++;
			if (Day.leapYear( end   ) && Day.dayOfYear( end   ) >= 59) leapDaysSpanned++;
		}
		return audit.out( leapDaysSpanned );
	}
	
	private int borrowed = 0;
	private int difference( int start, int end, int base) {
		int result;
		//int start = Moment.secondValue( begin ), end = Moment.secondValue( end );
		if (end >= start) {
			result = end - start;
			borrowed = 0;
		} else {
			result = base - start + end;
			borrowed = 1; // 60 <base>
		}
		return result;
	}
	
	private long duration = 0;
	public  long duration() { return duration; }
	private long duration( long start, long stop ) {
		long begin = start,
		       end = stop;
		if (begin > end) {
			begin = stop;
			end = start;
		}
		
		borrowed = 0;
		int days,
		seconds = difference( Moment.secondValue( begin ), Moment.secondValue( end ), 60 ),
		minutes = difference( Moment.minuteValue( begin ), Moment.minuteValue( end ), 60 ),
		hours   = difference( Moment.hourValue(   begin ), Moment.hourValue(   end ), 24 );

		int startDay = Day.dayOfYear( begin ),
		      endDay = Day.dayOfYear( end ) - borrowed;
		if (Moment.yearValue(begin) == Moment.yearValue( end )) {
			days = Day.dayOfYear( end ) - Day.dayOfYear( begin );
		} else {
			if (endDay >= startDay + leapDaysSpanned( begin, end )) {
				days = Day.dayOfYear( end ) - Day.dayOfYear( begin );// - leapDaysSpanned( begin, end );
				borrowed = 0;
			} else {
				days = Day.daysToEndOfYear( begin ) + Day.dayOfYear( end );// + leapDaysSpanned( begin, end ); // leap days in calc!
				borrowed = 1; // 1 year
		}	}
		
		long years = Moment.yearValue( end - begin - borrowed * Time.YEAR );
		
		return years*Time.YEAR
			+   days*Time.DAY
			+  hours*Time.HOUR
			+minutes*Time.MINUTE
			+seconds;
	}
	
	private Moment from = null;
	public Moment from() { return from; }
	
	private Moment to = null;
	public Moment to() { return to; }
	
	private boolean unknown = false;
	public boolean unknown() { return unknown; }
	
	public Duration( Moment fr, Moment t ) { 
		audit.in( "new Duration", (fr==null?"":fr.moment()) +" to "+ (t==null?"":t.moment()) );
		from = fr;
		to   = t;
		if (from == null && to == null) {
			unknown = true;
		} else if (from == null || to == null) {
			unknown = false;
			duration = 0;
		} else {
			if (from.timeUnassigned()) from.time( 0 );
			if (  to.timeUnassigned())   to.time( 0 );
			if (from.dateUnassigned()) from.date( 0 );
			if (  to.dateUnassigned())   to.date( 0 );
			
			long start = from.moment(), stop  = to.moment();
			if (stop != start) 
				duration = duration( start, stop );
		}
		audit.out( duration );
	}
	public Duration( long start, long stop ) { this( new Moment( start ), new Moment( stop ));}
	public Duration( When w ) { this( w.from(), w.to() ); }

	private static int dayValue( long date ) {
		/* Duration days should range up to 365 (in a leap year)
		 * Moment's month/day range represents this as 0365
		 */
		return (int)((date/Time.DAY)%10000);
	}
	public String toString() {
		audit.in( "duration", ""+duration );
		String rc = "a moment in time";
		if (unknown)
			rc = "unknown";
		else if (0 != duration) {
			long years  = Moment.yearValue( duration ),
				days    = dayValue( duration ),
				hours   = Moment.hourValue( duration ),
				minutes = Moment.minuteValue( duration ),
				seconds = Moment.secondValue( duration );
			
			//audit.debug("y="+ years +", d="+ days +",h="+ hours +", m="+ minutes +", s="+ seconds );
			
			rc = "";
			if (years == 1) rc += "a year";
			if (years  > 1) rc += years + " years";
		
			if (years != 0 && days != 0)
				             rc += (hours == 0 && minutes == 0 && seconds == 0 ? " and " : " " );
			
			if (days == 1) 	 rc += "a day";
			if (days  > 1) 	 rc += days +" days";

			if ((years != 0 || days != 0) && hours != 0)
				             rc += (minutes == 0 && seconds == 0 ? " and " : " " );

			if (hours == 1)  rc += "an hour";
			if (hours  > 1)  rc += hours +" hours";

			if ((years != 0 || days != 0 || hours != 0 ) && minutes != 0)
				             rc += (seconds == 0 ? " and " : " ");

			if (minutes == 1)rc += "a minute";
			if (minutes  > 1)rc += minutes +" minutes";

			if ((years != 0 || days != 0 || hours != 0 || minutes != 0) && seconds != 0)
				             rc += " and ";

			if (seconds == 1)rc += "a second";
			if (seconds  > 1)rc += seconds +" seconds";
		}
		return audit.out( rc );
	}
	public String valueOf() {
		return //duration +"("+
				(from==null?"":from.valueOf())
				+" -> "+ 
				(  to==null?"":  to.valueOf())
				//+")"
				;
	}
	// test code...
	private static void durationTest( long from, long to ) {
		Duration d = new Duration( from==0? null : new Moment( from ),
				                     to==0? null : new Moment(   to ) );
		audit.log( d.valueOf() +", this duration is "+ d.toString() +"." );
	}
	public static void main( String args[] ) {
		/* This class contains a few bugs - see tests below.
		 * e.g. 17/07/63 -> 17/07/64 == "a year and a day", should be "a year"
		 * * * * Leaving this for now as this is not critical path. * * *
		 */
		//audit.on();
		//audit.tracing = true;
		//* Date tests...
		//durationTest( 20160107073042L, 20160107073042L ); // a moment in time
		//durationTest( 20160107073042L, 20160108073042L ); // a day
		// */
		/* Leap year tests...
		durationTest( 19630717073042L, 19640227073042L ); // x+n days
		durationTest( 19630717073042L, 19640228073042L ); // x+n days
		durationTest( 19630717073042L, 19640229073042L ); // x+n days
		durationTest( 19630717073042L, 19640301073042L ); // x+n days
		durationTest( 19630717073042L, 19640302073042L ); // x+n days
		// -- */
		durationTest( 19630717073042L, 19640717073042L ); // a year ('64 was a leap year!)
		durationTest( 19630717073042L, 19640717073043L ); // a year and a second.
		//*
		durationTest( 19630717073042L, 19640718083143L ); // 1y, 1d, 1h, 1m, 1s
		durationTest( 19630717073042L, 19640718083143L ); // 1y, 1d, 1h, 1m, 1s
		durationTest(               0, 19630717073042L ); // 1y, 1d, 1h, 1m, 1s
		// */
}	}
