package org.enguage.vehicle.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.when.Absolute;
import org.enguage.vehicle.when.Day;
import org.enguage.vehicle.when.Duration;
import org.enguage.vehicle.when.Moment;
import org.enguage.vehicle.when.Month;
import org.enguage.vehicle.when.Relative;
import org.enguage.vehicle.when.Time;
import org.enguage.vehicle.when.When;

public class Moment {
	private        boolean CE = true; // current epoch (western?)
	private static String NAME = "Moment";
	private static Audit audit = new Audit( NAME );
	
	public Moment( long n ) { moment = n; }
	public Moment() {}
	public Moment( Moment m ) { this( m.moment ); }
	public Moment( String s ) { if (valid( s )) moment = Long.valueOf( s ); }
	static public Moment getNow() {
		return new Moment(
				Long.valueOf(
						new SimpleDateFormat( "yyyyMMddHHmmss", locale ).format( new Date().getTime())
					)	);
	}

	public static boolean valid( String s ) {
		if (s != null && s.length() == 14) {
			char[] buffer = s.toCharArray();
			for (Character ch : buffer )
				if (!Character.isDigit( ch ))
					return false;
			return true;
		}
		return false;
	}
	
	private long   moment = Absolute.unassignedDate * Time.DAY + Time.unassignedTime;
	public	long   moment() { return moment; }
	public	Moment moment( long l ) { moment = l; return this; }
	
	public boolean isUnassigned() {
		return date() == Absolute.unassignedDate
		    && time() == Time.unassignedTime;
	}
	public Moment unassign() {
		date( Absolute.unassignedDate );
		time( Time.unassignedTime );
		return this;
	}
	
	private static Locale locale = Locale.UK;
	public  static Locale locale() { return locale; }
	public  static void   locale( Locale l ) { locale = l; }

	public boolean equals( Moment m ) { return moment == m.moment(); }
	public long	compareTo( Moment m ) { return moment - m.moment(); }
	public long	compareToNow() { return getNow().moment() - moment; }
	
	// used to record if am/pm is used in the description
	private boolean pm = false;
	public  boolean pm() { return pm; }
	public  Moment  pm( boolean b ) { pm = b; return this; }
	private boolean am = false;
	public  boolean am() { return am; }
	public  Moment  am( boolean b ) { am = b; return this; }
	
	private Strings representamen = new Strings();
	public	Moment  representamen( String  s ) { representamen.add( s ); return this; }
	public	Moment  representamen( Strings s ) { if (s != null) representamen = s; return this; }
	public	Strings representamen() { return representamen; }
	
	public	static long  yearValue( long date ) {return date/Time.YEAR ; }
	public  static int  monthValue( long date ) {return (int)((date/Time.MONTH)%100);}
	public  static int    dayValue( long date ) {return (int)((date/Time.DAY)%100);}
	public  static int   hourValue( long date ) {return (int)((date/Time.HOUR)%100);}
	public  static int minuteValue( long date ) {return (int)((date/Time.MINUTE)%100);}
	public  static int secondValue( long date ) {return (int)(date%100);}
	public	static int   timeValue( long date ) {return (int)(date%Time.DAY);}
	public	static long  dateValue( long date ) {return       date/Time.DAY;}
	public long year()   { return   yearValue( moment ); }
	public int	month()  { return  monthValue( moment ); } // 100 covers a 2 char month 01..12
	public int	day()    { return    dayValue( moment ); } // 100 covers a 2 char day 01..31
	public int	hour()   { return   hourValue( moment ); }
	public int	minute() { return minuteValue( moment ); }
	public int	second() { return secondValue( moment ); }
	
	public void year(  long  year) { moment += (year   - year())   * Time.YEAR; }
	public void month( int  month) { moment += (month  - month())  * Time.MONTH; }
	public void day(   int    day) { moment += (day    - day())    * Time.DAY; }
	public void hour(  int   hour) { moment += (hour   - hour())   * Time.HOUR; }
	public void minute(int minute) { moment += (minute - minute()) * Time.MINUTE; }
	public void second(int second) { moment += (second - second()) * Time.SECOND; }
	
	// aniversary e.g. 1225 (i.e. Christmas day)
	public long anniversary() {return (moment%Time.YEAR) / Time.DAY;}
	public void anniversary( int date ){ moment += (date - anniversary()) * Time.DAY; }
	
	// date portion of a moment e.g. 19630717
	public int  date() { return (int)( moment / Time.DAY); }
	public void date( long date) { moment = date * Time.DAY + time(); }
	public void date( Moment m ) { date( m.moment()/Time.DAY ); }
	public boolean dateUnassigned() { return date() == Absolute.unassignedDate;}
	
	// time portion of a moment, e.g. 235959
	public int	time() { return (int)( moment % Time.DAY); }
	public void time(long time) { moment = date() * Time.DAY + time%Time.DAY; }
	public void time( Moment m ) { time( m.moment()); }
	public boolean timeUnassigned() { return time() == Time.unassignedTime;}
	
	// this needs to support quaterdays... try below
	private long scale = Time.SECOND; // second, minute, ... etc
	public	long scale() { return scale; }
	public	Moment scale( long l ) { scale = l; return this; }

	/*
	public void subtractYears( Moment m ) {
		for (int i=0; i<m.year(); i++)
			prevYear();
	}
	public void subtractMonths( Moment m ) {
		for (int i=0; i<m.month(); i++)
			prevMonth();
	}
	public void subtractDays( Moment m ) {
		for (int i=0; i<m.day(); i++)
			prevDay();
	}
	public void subtractHours( Moment m ) {
		for (int i=0; i<m.hour(); i++)
			prevHour();
	}
	public void subtractMinutes( Moment m ) {
		for (int i=0; i<m.minute(); i++)
			prevMinute();
	}
	public void subtractSeconds( Moment m ) {
		for (int i=0; i<m.second(); i++)
			prevSecond();
	}
	// */
	//public String dayName() { return dayName( moment );	}

	private void checkEra() { // flip epoch
		if (0==year()) { // no year 0!
			moment += Time.YEAR; // add one year - either way!
			CE = !CE;	// reverse Era
	}	}
	public Moment prevYear() {
		if (date() != Absolute.unassignedDate) {
			moment -= CE ? Time.YEAR : -Time.YEAR;
			checkEra();
		}
		return this;
	}
	public Moment nextYear() {
		if (date() != Absolute.unassignedDate) {
			moment += CE ? Time.YEAR : -Time.YEAR;
			checkEra();
		}
		return this;
	}
	
	private void correctDaysInMonth( int month ) {
		int days = Day.daysInMonth( month, year());
		if (day() > days)
			day( days );
	}
	public Moment prevMonth() {
		if (date() != Absolute.unassignedDate) {
			int month = month();
			if (0 != month) { // check against prevMonth() on "10:30" 
				if (1 == month) { // no zeroth month!
					moment += 11 * Time.MONTH; // month to 12
					prevYear();
				} else {
					moment -= Time.MONTH;
					correctDaysInMonth( month-1 );
		}	}	}
		return this;
	}
	public Moment nextMonth() {
		if (date() != Absolute.unassignedDate) {
			int month = month();
			if (0 != month) {
				if (12 == month()) {
					moment -= 11 * Time.MONTH; // month to 1
					nextYear();
				} else {
					moment -= Time.MONTH;
					correctDaysInMonth( month+1 );
		}	}	}
		return this;
	}

	public Moment prevDay() {
		if (date() != Absolute.unassignedDate) {
			int day = day();
			if (day != 0) {
				if (year() == 1752 && month()==9 && day()==14) moment -= Time.DAY*11; 
				moment -= Time.DAY;
				if (--day == 0) {
					moment -= Time.MONTH;
					int	month = month();
					if (month == 0)
						moment += ( -Time.YEAR + 12 * Time.MONTH + 31 * Time.DAY);
					else
						moment += Time.DAY * Day.daysInMonth( month, year() );
		}	}	}
		return this;
	}
	public Moment nextDay() {
		if (date() != Absolute.unassignedDate) {
			int day = day();
			if (day != 0) {
				if (year() == 1752 && month()==9 && day()==2) moment += Time.DAY*11; 
				moment += Time.DAY;
				int mnth = month();
				if (++day > Day.daysInMonth( mnth, year() )) {
					day( 1 );
					if (mnth == 12) {
						month( 1 );
						moment += Time.YEAR;
					} else
						moment += Time.MONTH;
		}	}	}
		return this;
	}
	public Moment prevHour() {
		if (time() != Time.unassignedTime) {
			if (0 == hour()) {
				moment += 23 * Time.HOUR;
				prevDay();
			} else
				moment -= Time.HOUR;
		}
		return this;
	}
	public Moment nextHour() {
		if (time() != Time.unassignedTime) {
			if (23 == hour()) {
				moment -= 23 * Time.HOUR;
				nextDay();
			} else
				moment += Time.HOUR;
		}
		return this;
	}
	public Moment prevMinute() { 
		if (time() != Time.unassignedTime) {
			if (0==minute()) {
				moment += 59 * Time.MINUTE;
				prevHour();
			} else
				moment -= Time.MINUTE;
		}
		return this;
	}
	public Moment nextMinute() { 
		if (time() != Time.unassignedTime) {
			if (59==minute()) {
				moment -= 59 * Time.MINUTE;
				nextHour();
			} else
				moment += Time.MINUTE;
		}
		return this;
	}
	public Moment prevSecond() { 
		if (time() != Time.unassignedTime) {
			if (0==second()) {
				moment += 59;
				prevMinute();
			} else
				moment -= Time.SECOND;
		}
		return this;
	}
	public Moment nextSecond() {
		if (time() != Time.unassignedTime) {
			if (59==second()) {
				moment -= 59;
				nextMinute();
			} else
				moment += Time.SECOND;
		}
		return this;
	}
	public String dayName() {return Day.name( moment );}
	
	public String valueOf(){
		/*//OK - always pass back a full string, 888...88 if needs be!
		 * String rc = "";
		 * if (moment / Time.DAY != Absolute.unassignedDate) {
		 * 	rc += String.format( "%d%02d%02d", year(), month(), day());
		 * 	if ((moment % Time.DAY != Time.unassignedTime ))
		 * 		rc += "";
		 * }
		 * if (moment % Time.DAY != Time.unassignedTime)
		 * 	rc += String.format( "%02d%02d%02d", hour(), minute(), second());
		 * return rc; // -> 19991231235959
		 */
		return String.format( "%d%02d%02d%02d%02d%02d", year(), month(), day(), hour(), minute(), second());
	}

	private String dateToString( String prefix ) {
		String rc = "";
		long dval = date();
		if (dval != Absolute.unassignedDate && dval != 0) {
			long today     = dateValue( getNow().moment() ),
				 tomorrow  = dateValue( getNow().nextDay().moment() ),
				 yesterday = dateValue( getNow().prevDay().moment() );
			if (dval == today ) {
				rc = "today";
			} else if (dval == tomorrow ) {
				rc = "tomorrow";
			} else if (dval == yesterday ) {
				rc = "yesterday";
			} else {
				String	day = Day.toString( moment ),
						month = Month.toString( moment ),
						date = day + (month.equals("") || day.equals("")?"":"of ") + month;
				if (prefix==null) // obtain default
					prefix = day.equals( "" ) ? Absolute.approxPrefix() : Absolute.exactPrefix();
				if (!prefix.equals("")) prefix += " ";
				rc = String.format( prefix +"%s%s%d ", date, (date.equals("") ? "" : ", "), year());
		}	}
		return rc;
	}
	private static String minutesToString( int mins ) {
		return mins == Time.unassigned || mins == 0 ? "" : String.format( locale(), " %02d", mins );
	}
	private static String secondsToString( int secs ) {
		return secs == Time.unassigned || secs == 0 ? "" : String.format( locale(), " and %02d seconds", secs );
	}
	private String timeToString( String prefix, boolean am, boolean pm ) {
		String rc = "";
		if (time() != Time.unassignedTime) {
			prefix = (prefix==null || prefix.equals("") ? "at":prefix) + " ";
			rc = prefix;
			if (time() == 235959)
				rc += "midnight";
			else if (time() == Day.night)
				rc += "the night of ";  //// this needs to be part of day/date!!!
			else if (time() == Day.evening)
				rc += "the evening of ";
			else if (time() == Day.afternoon)
				rc += "the afternoon of ";
			else if (time() == Day.morning)
				rc += "the morning of ";
			else {
				int hour = hour(),
					mins = minute(),
					secs = second();
				if (hour > 12) {
					pm = true;
					hour -= 12;
				}
				rc += hour +
						((mins==0 && secs==0) ? "" :
							((secs==0) ?
								String.format( locale(), "%s ", minutesToString( mins ))
							  : String.format( locale(), "%s%s ", minutesToString( mins ), secondsToString( secs ) )));
				     if (pm) rc += "pm ";
				else if (am) rc += "am ";
		}	}
		return rc;
	}
	public String toString( String initialPrefix ) {
		/* Typically:
		 * at X
		 * on Y
		 * at X on Y
		 * until X
		 * until Y
		 * until X on Y
		 * until 7 p m on the evening of the 27th of April
		 */
		String rc = timeToString( initialPrefix, am, pm );
		rc +=  dateToString( rc.equals("")?initialPrefix:initialPrefix.equals("")?null:"" );
		return rc;
	}
	
	static boolean doMoment( When w, ListIterator<String> si ) {
		return Relative.doOn( w, si ) 
				|| Time.doAt( w, si )
				|| Relative.doIn( w, si )
				|| Relative.doRelativeDay( w, si );
	}
	// --- testing...
	private static void momentTest( long fr, long to ) {
		Moment m = new Moment( fr );
		Moment n = new Moment( to );
		audit.debug( m.toString( "from")
				+", "+
				n.toString( "until")
				+", duration is "+
				new Duration( m, n ).toString()
				+".");
	}
	public static void main(String[] args) {
		Audit.allOn();
		//String params[] = new String[0];
		//Duration.main( params );
		//*
		momentTest( 19630717073000L, 19630717073000L );
		momentTest( 19630717073000L, 19630718073000L );
		momentTest( 19630717073000L, 19640716073000L );
		momentTest( 19630717073000L, 19640717073000L );
		momentTest( 19630717073000L, 19640718073000L );
		momentTest( 19630717073000L, 19650717073000L );
		momentTest( 20151217073000L, 20151217073000L );
		momentTest( 20151216073000L, 20151216073000L );
		momentTest( 20151215073000L, 20151215073000L );
		// */
}	}
