package com.yagadi.enguage.expression;

import java.util.ArrayList;

import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
/*  A difficult concept? With:
 *  [ [ one=>123, two=>456,  thr=>789  ],
 *    [ thr=>123, four=>456, five=>789 ]
 *  ]
 *  We may request one and get 123, five and get 456, or thr and get 789.
 *  
 *  Can this be done in a single Attributes list - returning the first value? What about
 *  removing a layer of context?
 */
public class Context {
	static Audit audit = new Audit( "Context" );
	static private ArrayList<Attributes> contexts = new ArrayList<Attributes>();
	static public void  push( Attributes ctx ){ contexts.add( 0, ctx ); }
	static public void  pop() { contexts.remove( 0 );}
	static public Attributes context() {
		if (contexts.size() == 0) push( new Attributes() );
		return contexts.get( 0 );
	}
	static public Strings deref( Strings words ) { return deref( words, false ); }
	static public Strings deref( Strings words, boolean expand ) {
		for (Attributes context : contexts)
			context.deref( words, expand );
		return words;
	}
	static public String get( String word ) {
		for (Attributes context : contexts) {
			String deref = context.get( word );
			if (!deref.equals(""))
				return deref;
		}
		return "";
	}
	static public String valueOf() {
		String s = "";
		for (Attributes context : contexts)
			s += context.toString();
		return s;
	}
}
