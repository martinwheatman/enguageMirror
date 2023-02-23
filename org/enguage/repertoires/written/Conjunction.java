package org.enguage.repertoires.written;

import java.util.ArrayList;
import java.util.List;

import org.enguage.signs.symbol.reply.Reply;
import org.enguage.util.Strings;

public class Conjunction {
	
	// "I need fish and chips"
	// "I need coffee and biscuits"
	// "I need some gas and I want a Ferrari" << concept conjunction

	public static List<Strings> conjuntionAlley( Strings s ) {
		return conjuntionAlley( s, Reply.andConjunction() );
	}
	
	public static List<Strings> conjuntionAlley( Strings s, String conj ) {
		ArrayList<Strings> ls = new ArrayList<>();
		boolean found = false;
		Strings ss = new Strings();
		List<Strings> tmps = s.nonNullSplit( conj );
		for (Strings tmp : tmps) {
			if (!found) {
				if (!ss.isEmpty()) ss.add( conj );
				ss.addAll( tmp );
				found = !Names.match(tmp).isEmpty();
			
			} else if (Names.match(tmp).isEmpty()) {
				ss.add( conj );
				ss.addAll( tmp );
				
			} else {
				ls.add( ss );
				ss = tmp;
				found = false;
		}	}
		if (!ss.isEmpty()) ls.add( ss );
		
		return ls;
}	}