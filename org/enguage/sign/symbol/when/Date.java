package org.enguage.sign.symbol.when;

import java.util.regex.Pattern;

import org.enguage.util.strings.Strings;

public class Date {
	
	private static final Strings months = Month.names;
	
	private static Pattern digits = Pattern.compile("\\d+");

	private static boolean isDate( String str ) {
		return isDate( new Strings( str ));
	}
	private static boolean isDate( Strings strs ) {
		
		// This removes the age qualifiers on death dates.
		Strings sa = Strings.copyUntil( strs.listIterator(), "(" );
		
		switch (sa.size()) {
		case 3: // 23 April 1642
			return 	 digits.matcher( sa.get( 0 )).matches() &&
					months.contains( sa.get( 1 )) &&
					 digits.matcher( sa.get( 2 )).matches();
		case 4: // April 23, 1642
			return months.contains( sa.get( 0 )) &&
					digits.matcher( sa.get( 1 )).matches() &&
					                sa.get( 2 ).equals( "," ) &&
					digits.matcher( sa.get( 3 )).matches();
		default: // c. 1642-1643
			return (sa.size() > 1 &&
					sa.get( 0 ).equals( "c" )) &&
					digits.matcher( sa.get( sa.size()-1 )).matches(); // c. 878-879
		}
	}
	public static String getDate( Strings strs ) {
		for (String s : strs)
			if (isDate( s ))
				return s;
		return "Unknown";
}	}
