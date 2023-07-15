package org.enguage.sign.symbol.where;

import org.enguage.sign.symbol.when.Date;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Address {
	
	private static final String         NAME = "Address";
	private static final Audit         audit = new Audit( NAME );
	
	/*
	 *  e.g. "26 Dover Hedge, Aylesbury, Bucks", or, "Mayfair, London"
	 */
	
	public static boolean isAddress( String candidate ) {
		return candidate.contains( "," ) && !Date.isDate( candidate );
	}
	public static String getAddress( Strings strs, String onUnknown ) {
		audit.in("getAddress", "strs=["+ strs.toString( Strings.DQCSV ) +"]");
		for (String s : strs)
			if (isAddress( s ))
				return audit.out( s );
		return audit.out( onUnknown );
}	}
