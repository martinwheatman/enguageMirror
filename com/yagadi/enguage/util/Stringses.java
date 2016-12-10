package com.yagadi.enguage.util;

import java.util.ArrayList;
import java.util.ListIterator;

public class Stringses extends ArrayList<Strings> {
	static final long serialVersionUID = 0;
	static private Audit audit = new Audit( "Stringses" );
	
	public String toString() {
		String rc = "";
		ListIterator<Strings> li = listIterator();
		if (li.hasNext()) {
			rc = li.next().toString();
			while (li.hasNext()) {
				String item = li.next().toString();
				rc += "; " + (li.hasNext() ? "" : "and, ") + item;
		}	}
		return rc;
	}
	public static void main( String argv[]) {
		Stringses s = new Stringses();
		
		s.add( new Strings( "you need a cup of black coffee" ));
		s.add( new Strings( "you are meeting your brother on Tuesday at 7pm at the pub" ));
		
		audit.log( ">>"+ s.toString() );
}	}
