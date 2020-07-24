package org.enguage.interp;

import java.util.ArrayList;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Shell;
/** Context: a list of attributes, so a list of list of attribute
 *  [ [ one=>123, two=>456,  thr=>789  ],
 *    [ thr=>123, four=>456, five=>789 ]
 *  ]
 *  We request one and get 123, five and get 789, or thr and get 789.
 */
public class Context {
	static Audit audit = new Audit( "Context" );
	
	static public final int id = 42758506; // "context"???
	
	static private ArrayList<Attributes> contexts = new ArrayList<Attributes>();
	static public void  push( Attributes ctx ){ contexts.add( 0, ctx ); }
	static public void  pop() { contexts.remove( 0 );}
	static public Attributes context() {
		if (contexts.size() == 0) push( new Attributes() );
		return contexts.get( 0 );
	}
	static private void append( Attribute a ) { contexts.get( 0 ).add( a );}
	
	// these need to be words? [ "hello", "there" ] -> ["hi", "martin"]
	static public Strings deref( Strings words ) { return deref( words, false ); }
	static public Strings deref( Strings words, boolean expand ) {
		for (Attributes context : contexts)
			context.deref( words, expand );
		return words;
	}
	static public String get( String word ) {
		for (Attributes context : contexts) {
			String deref = context.value( word );
			if (!deref.equals(""))
				return deref;
		}
		return "";
	}
	static public Strings interpret( Strings a ) {
		audit.in( "interpret", "a="+ a );
		Strings rc = new Strings( Shell.FAIL );
		if (a.size() > 1) {
			String cmd = a.remove( 0 );
			if (cmd.equals( "add" )) {
				String name = a.remove( 0 );
				append( new Attribute( name, a.toString() ));
				rc = new Strings( Shell.SUCCESS );
		}	}
		return (Strings) audit.out( rc );
	}
	static public String valueOf() {
		String s = "";
		for (Attributes context : contexts)
			s += context.toString();
		return s;
	}
	public static void main( String args[]) {
		Audit.log( "hello there, "+ get( "martin" ) );
		push( new Attributes().add( "martin", "world" ));
		Audit.log( "hello there, "+ get( "martin" ) );
		push( new Attributes().add( "hello", "there" ));
		Audit.log( "ctx>>>"+ valueOf() );
}	}
