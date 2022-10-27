package org.enguage.signs.symbol.when;

import java.util.ListIterator;

import org.enguage.util.Audit;

public class Month {
	public static Audit audit = new Audit( "Month" );
	
	static public String name( int n ) {
		switch (n) {
		case  1:return "January"; case 2: return "February";case 3: return "March";
		case  4:return "April";   case 5: return "May";		case 6: return "June";
		case  7:return "July";    case 8: return "August";  case 9: return "September";
		case 10:return "October"; case 11:return "November";case 12:return "December";
		default:return "month("+ n +")";
	}	}
	static int number( String name ) {
		if (name.equals(  "January" )) return  1;
		if (name.equals( "February" )) return  2;
		if (name.equals(    "March" )) return  3;
		if (name.equals(    "April" )) return  4;
		if (name.equals(      "May" )) return  5;
		if (name.equals(     "June" )) return  6;
		if (name.equals(     "July" )) return  7;
		if (name.equals(   "August" )) return  8;
		if (name.equals("September" )) return  9;
		if (name.equals(  "October" )) return 10;
		if (name.equals( "November" )) return 11;
		if (name.equals( "December" )) return 12;
		return 0;
	}
	public static boolean doMonth( When w, ListIterator<String> si ) {
		//audit.in( "doMonth", "w="+ w.toString() +", si="+ si.nextIndex());
		boolean rc = false;
		if ( si.hasNext()) {
			int n = number( si.next());
			if (n != 0) {
				w.month( n );
				rc = true;
			} else
				si.previous();
		}
		return rc; //audit.out( rc );
	}
	public static String toString( long time ) {
		int month = Moment.monthValue( time );
		return month == Time.unassigned ? "" : name( month )+" ";
}	}
