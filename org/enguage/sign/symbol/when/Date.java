package org.enguage.sign.symbol.when;

import java.util.regex.Pattern;

import org.enguage.util.strings.Strings;

public class Date {
	
	//private static final Audit audit = new Audit( "Date" )
	private static final Strings months = Month.names;
	
	private static Pattern digits = Pattern.compile("\\d+");

	public static boolean isDate( String str ) {
		return isDate( new Strings( str ));
	}
	private static boolean isDate( Strings strs ) {
		boolean rc = false;
		// This removes the age qualifiers on death dates.
		Strings sa = Strings.copyUntil( strs.listIterator(), "(" );
		
		if (sa.size() >= 3 && // 23 April 1642
			 	 digits.matcher( sa.get( 0 )).matches() &&
				months.contains( sa.get( 1 ))           &&
				 digits.matcher( sa.get( 2 )).matches()    )
		{
			rc = true;
		}
		if (sa.size() >= 4 && // April 23, 1642
			months.contains( sa.get( 0 )) &&
			 digits.matcher( sa.get( 1 )).matches() &&
			                 sa.get( 2 ).equals( "," ) &&
			 digits.matcher( sa.get( 3 )).matches())
		{
			rc = true;
		} 
		if (sa.size() > 1 && // c. 1642-1643  c...nnnn
			sa.get( 0 ).equals( "c" ) &&
			digits.matcher( sa.get( sa.size()-1 )).matches()) // c. 878-879
		{
			rc = true;
		}
		return rc;
	}
	public static String getDate( Strings strs, String onUnknown ) {
		for (String s : strs)
			if (isDate( s ))
				return s;
		return onUnknown;
}	}
