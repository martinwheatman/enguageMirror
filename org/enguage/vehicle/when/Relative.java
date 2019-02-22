package org.enguage.vehicle.when;

import java.util.ListIterator;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.number.Number;
import org.enguage.vehicle.when.Absolute;
import org.enguage.vehicle.when.Day;
import org.enguage.vehicle.when.Month;
import org.enguage.vehicle.when.Relative;
import org.enguage.vehicle.when.Time;
import org.enguage.vehicle.when.When;

public class Relative {
	// "... a week ago last Monday ..."
	// "... from 1558 until 1603 ..."
	// "... in December 2010 ..." etc
	// "... from last monday to a week ago yesterday..."
	
	static Audit audit = new Audit( "Relative" );

	static boolean doThisLastNext( When w, ListIterator<String> si ) {
		audit.in( "doThisLastNext",", w="+ w.toString() +", <si>" );
		boolean found = false;
		if (si.hasNext()) {
			String s = si.next();
			audit.debug( "Read: "+ s );
			if ( s.equals( "this" )) {
				found = true;
				When.presentIs();
			} else if ( s.equals( "next" )) {
				found = true;
				When.futureIs();
			} else if ( s.equals( "last" )) {
				found = true;
				When.pastIs();
			} else
				si.previous();
		}
		return audit.out( found );
	}
	static boolean doWmy( When w, ListIterator<String> si ) {
		audit.in( "doWmy", "" );
		boolean rc = false;
		if (si.hasNext()) {
			String s = Plural.singular( si.next());
			rc = true;
			if (s.equals( "week" ))
				w.dayShift( When.isPast() ? -7 : 7 );
			else if (s.equals( "month" ))
				w.monthShift( When.isPast() ? -1 : 1 );
			else if (s.equals( "year" ))
				w.yearShift( When.isPast() ? -1 : 1 );
			else if (s.equals( "decade" ))
				w.yearShift( When.isPast() ? -10 : 10 );
			else if (s.equals( "century" ))
				w.yearShift( When.isPast() ? -100 : 100 );
			else if (s.equals( "millenium" ))
				w.yearShift( When.isPast() ? -1000 : 1000 );
			else {
				rc = false;
				si.previous();
		}	}
		return audit.out( rc );
	}
	static boolean doAwmyAgo( When w, ListIterator<String> si ) {
		// removes "...a week ago..."
		/* 
		 * [[NUMBER w|m|y [ago]] last|next|this]
		 */
		audit.in( "doAwmyAgo", "<si>" );
		boolean found = false;
		int shift = 0, start = si.nextIndex();
		Number number = Number.getNumber( si );
		if (number.representamen().size() > 0 && si.hasNext()) {
			shift = Math.round( number.magnitude());
			audit.debug( "found shift="+ shift );
			String s = Plural.singular( si.next());
			if ( (s.equals( "day" ) || s.equals( "week" ))) {
				found = true;
				if (s.equals( "week" )) shift *= 7;
				if (si.hasNext()) {
					int tmp = si.nextIndex();
					s = si.next();
					if ( s.equals( "ago" )) {
						Audit.log( "found ago at "+ tmp );
						shift *= -1; // negate shift
					} else
						si.previous();
				}
				w.dayShift( shift );
				
			} else if ( (s.equals( "month" ))) {
				audit.debug( "found month" );
				found = true;
				if (si.hasNext()) {
					s = si.next();
					if ( s.equals( "ago" ))
						shift *= -1;
					else
						si.previous();
				}
				w.monthShift( shift );
				
			} else if ( (s.equals( "year" )
					|| s.equals( "decade" )
					|| s.equals( "century" )
					|| s.equals( "millenium" )))
			{	audit.debug( "found "+ s );
				found = true;
	
				if (s.equals( "decade" )) shift *= Time.DECADE / Time.YEAR;
				if (s.equals( "century" )) shift *= Time.CENTURY / Time.YEAR;
				if (s.equals( "millenium" )) shift *= Time.MILLENIUM / Time.YEAR;
				if (si.hasNext()) {
					s = si.next();
					if ( s.equals( "ago" ))
						shift *= -1;
					else // just put this back
						si.previous();
				}
				w.yearShift( shift );
				
			} else
				found = false; // we've not found anything significant
		}
		if (!found) while (si.previousIndex() >= start) si.previous();
		return audit.out( found );
	}
	static boolean doOn( When w, ListIterator<String> si ) {
		audit.in( "doOn", "w="+ w.toString() +", si="+ si.nextIndex());
		boolean rc = false;
		// on already found
		if (Day.doDayName( w, si ))
			rc = true; // done enough, following here is speculative

		if (Strings.doString( "the", si ))
			rc = Absolute.doFullDate( w, si );
		
		if (Strings.doString( "at", si) && !Time.doTime( w, si ))
			si.previous(); // replace "at"
			
		return audit.out( rc );
	}
	static boolean doIn( When w, ListIterator<String> si ) {
		audit.in( "doIn", "w="+ w.toString() +", si="+ si.nextIndex());
		boolean rc = false;
		// in already found
		if (Month.doMonth( w, si )) { // optional -- scale <- month
			rc = true;
			Absolute.doYear( w, si ); // scale <- year
		} else if( Absolute.doYear( w, si )) { // scale <- year
			rc = true;
		}
		return audit.out( rc );
	}
	static boolean doRelativeDay( When w, ListIterator<String> si ) {
		audit.in( "doRelativeDay", "w="+ w.toString() +", si="+ si.nextIndex());
		/* d    [NUMBER d/w/m/y/c/m [ago]] yesterday [morning/afternoon/evening]| // last night 
		 *                                 tomorrow [morning/afternoon/evening/night] |
		 *                                 today | this morning/afternoon/evening | tonight
		 *                                 [at TIME]
		 * e    [NUMBER d/w/m/y/c/m [ago]] last [night]/next/this DAY [at TIME] | w/m/y/c/m | MONTH | YEAR
		 */
		boolean rc = Relative.doAwmyAgo( w, si ); // this is enough on its own
		
		// this time last night
		if (Strings.doString( "last", si )) {
			if (!(rc = Day.doLastNight(   w, si )
					|| Relative.doWmy(  w, si )
					|| Day.doDayName( w, si )
					|| Month.doMonth( w, si )))
				si.previous(); // replace "last"*/
			
		} else if (Strings.doString( "this", si )) {
			if (!(rc = Day.doMornAftEve( w, si )
					|| Relative.doWmy(  w, si )
					|| Day.doDayName( w, si )
					|| Month.doMonth( w, si )))
				si.previous(); // replace "this"
		} else if (Day.doTonight( w, si )) { // i.e. "this night"
			rc = true;
			
		} else if (Strings.doString( "next", si )) {
			if (!(rc = Day.doMornAftEve( w, si ) // ???
					|| Relative.doWmy(  w, si )
					|| Day.doDayName( w, si )
					|| Month.doMonth( w, si )))
				si.previous(); // replace "next"
			
		// yesterday
		} else if (Day.doYesterday( w, si )) {
			rc = true;
			Day.doMornAftEve( w, si ); // also see "last night" above

		// today
		} else if (Day.doToday( w, si )) {
			rc = true;
			
		// tomorrow
		} else if (Day.doTomorrow( w, si )) {
			rc = true;
			if (!Day.doMornAftEve( w, si ))
				Day.doTomorrowNight( w, si );
		}
		
		// finally, at TIME
		if (Strings.doString( "at", si ) && !Time.doTime( w, si ))
			si.previous();

		return audit.out( rc );
}	}