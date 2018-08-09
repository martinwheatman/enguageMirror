package org.enguage.veh.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.veh.Language;
import org.enguage.veh.when.Day;
import org.enguage.veh.when.Moment;
import org.enguage.veh.when.Relative;
import org.enguage.veh.when.Time;
import org.enguage.veh.when.When;

public class Day {

	private static String NAME = "Day";
	private static Audit audit = new Audit( NAME );
	
	public static final String MON = "Monday";
	public static final String TUE = "Tuesday";
	public static final String WED = "Wednesday";
	public static final String THU = "Thursday";
	public static final String FRI = "Friday";
	public static final String SAT = "Saturday";
	public static final String SUN = "Sunday";
	
	public static final int quarterday = 250000;
	public static final int morning    = 260000;
	public static final int afternoon  = 270000;
	public static final int evening    = 280000;
	public static final int night      = 290000;

	static boolean isDayName( String s ) {
		return s.equalsIgnoreCase( MON ) || s.equalsIgnoreCase( TUE ) ||
			   s.equalsIgnoreCase( WED ) || s.equalsIgnoreCase( THU ) ||
			   s.equalsIgnoreCase( FRI ) || s.equalsIgnoreCase( SAT ) ||
			   s.equalsIgnoreCase( SUN  );
	}
	static public int dayInWeek( String day ) {
		     if (day.equalsIgnoreCase( MON )) return 0;
		else if (day.equalsIgnoreCase( TUE )) return 1;
		else if (day.equalsIgnoreCase( WED )) return 2;
		else if (day.equalsIgnoreCase( THU )) return 3;
		else if (day.equalsIgnoreCase( FRI )) return 4;
		else if (day.equalsIgnoreCase( SAT )) return 5;
		else if (day.equalsIgnoreCase( SUN )) return 6;
		else return -1;
	}
	public	static boolean leapYear( long year ) {
		return year%4 == 0 && year != 0 && (year<=1752 || year%100 != 0 || year%400 == 0);
	}
	public	static boolean leapDate(long date) {
		return leapYear( date /= Time.YEAR );
	}
	static int daysInYear( long year ) {
		return Moment.yearValue( year ) == 1752 ? 355 :
			Moment.yearValue( year ) == 1751 ? 282 :
			Day.leapYear( year ) ? 366 : 365;
	}
	static int daysInMonth( int month, long year ) {
		switch( month ) {
		case	9:
			return year==1752 ? 15 : 30;
		case	4: case	6: case 11:
			return 30; // 30 days hath September...
		case	2:
			return Day.leapYear( year ) ? 29 : 28;
		default: // all the rest...
			return 31;
	}	}
	static public int dayOfYear( long year, int month, int day ) {
		audit.in( "dayOfYear", "year="+ year +", month="+ month +", day="+ day );
		int days = day;
		for (int i=1; i<month; i++)
			days += daysInMonth( i, year );
		return audit.out( days );
	}
	static public int dayOfYear( long date ) {
		return dayOfYear(
				Moment.yearValue( date ),
				Moment.monthValue( date ),
				Moment.dayValue( date )
		);
	}
	static public int daysToEndOfYear( long year, int month, int day ) {
		return daysInYear( year ) - dayOfYear( year, month, day );
	}
	static public int daysToEndOfYear( long date ) {
		return daysToEndOfYear(
				Moment.yearValue( date ),
				Moment.monthValue( date ),
				Moment.dayValue( date )
		);
	}
	public static String name( long moment ) {
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd", Moment.locale() );
		try {
			Date dt1=sdf.parse( ""+ moment/Time.DAY );
			return new SimpleDateFormat( "EEEE", Moment.locale() ).format( dt1 );
		} catch( java.text.ParseException pe) {
			return "" ;
	}	}
	static boolean doDayName( When w, ListIterator<String> si ) {
		Relative.audit.in( "doDayName", "w="+ w.toString() +", si="+ si.nextIndex());
		// adjusts w to the given day, e.g. "Thursday" of this week
		boolean found = false;
		if (si.hasNext()) {
			String dayName = si.next();
			if (isDayName( dayName )) {
				
				found = true;
				
				// adjust for this week
				w.thisWeek( dayName );
				
				/* here, it is a Monday
				 * and we have said "i MET my brother on Thursday"
				 */
				if (When.isPast()) // e.g. I MET
					w.dayShift( -7 );
				
				/* here, it is a Thursday
				 * and we have said "i WILL MEET my brother NEXT Monday"
				 */
				else if (When.isFuture()) // i.e. "I WILL MEET"
					w.dayShift( +7 );
				
				w.shift();
				
			} else
				si.previous();
		}
		Relative.audit.out( found );
		return found;
	}
	static boolean doToday( When w, ListIterator<String> si ) {
		Relative.audit.in( "doToday", "w="+ w.toString() );
		boolean found = false;
		if (si.hasNext()) {
			if (si.next().equals( "today" )) {
				audit.debug( "doToday(): found today" );
				found = true;
				w.today();
			} else
				si.previous();
		}
		return Relative.audit.out( found );
	}

	static boolean doMornAftEve( When w, ListIterator<String> si ) {
		boolean found = false;
		if (si.hasNext()) {
			found = true;
			String s = si.next();
			if (s.equals( "morning" )) {
				audit.debug( "doMornAftEve(): found morning" );
				w.scale( quarterday ).time( morning );
			} else if (s.equals( "afternoon" )) {
				audit.debug( "doMornAftEve(): found afternoon" );
				w.scale( quarterday ).time( afternoon );
			} else if (s.equals( "evening" )) {
				audit.debug( "doMornAftEve(): found evening" );
				w.scale( quarterday ).time( evening );
			} else {
				si.previous();
				found = false;
		}	}
		return found;
	}
	static boolean doLastNight( When w, ListIterator<String> si ) {
		boolean found = false;
		if (Strings.doString( "night", si )) {
			audit.debug( "doLastNight(): found last + night" );
			w.yesterday().scale( quarterday ).time( night );
			found = true;
		}
		return found;
	}
	static boolean doTomorrowNight( When w, ListIterator<String> si ) {
		boolean found = false;
		if (Strings.doString( "night", si )) {
			audit.debug( "doNight(): found night" );
			w.tomorrow().scale( quarterday ).time( night );
			found = true;
		}
		return found;
	}
	static boolean doTonight( When w, ListIterator<String> si ) {
		boolean found = false;
		if (Strings.doString( "tonight", si )) {
			audit.debug( "doTonight(): found tonight" );
			w.scale( quarterday ).today().time( night );
			found = true;
		}
		return found;
	}
	static boolean doYesterday( When w, ListIterator<String> si ) {
		boolean found = false;
		if (Strings.doString( "yesterday", si )) {
			audit.debug( "doYeaterday(): found yesterday" );
			w.yesterday();
			found = true;
		}
		return found;
	}
	static boolean doTomorrow( When w, ListIterator<String> si ) {
		boolean found = false;
		if (Strings.doString( "tomorrow", si )) {
			audit.debug( "doTomorrow(): found tomorrow" );
			found = true;
			w.tomorrow();
		}
		return found;
	}
	// ---------------------------------------------------------------------------
	public static When getWhen( Strings sa ) { // [ "1225" ]
		When w = new When().today();
		if (sa.size() > 0) {
			try {
				When.audit.debug( "setting day:"+ sa.toString( Strings.SPACED ));
				w.anniversary( Integer.valueOf( sa.get( 0 )));
				When.audit.debug("w="+w.toString());
			} catch( Exception e ) {
				When.audit.log( "When.day(): exception: "+ e.toString() );
		}	}
		return w;
	}
	public static String toString( long time ) {
		int day = Moment.dayValue( time );
		return (day == Time.unassigned) ? "" : String.format( "the %d%s ", day, Language.nthEnding( day ));
	}
	public static void main( String args[]) {
		//audit.log("days to end of year: "+ daysToEndOfYear( 20161231000000L ));
		//audit.tracing = true;
		audit.log("day of year: "+ dayOfYear( 20160227000000L ));
		audit.log("day of year: "+ dayOfYear( 20160228000000L ));
		audit.log("day of year: "+ dayOfYear( 20160229000000L ));
		audit.log("day of year: "+ dayOfYear( 20160301000000L ));
		audit.log("day of year: "+ dayOfYear( 20160302000000L ));
}	}
