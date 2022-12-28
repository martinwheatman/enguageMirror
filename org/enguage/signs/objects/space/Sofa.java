package org.enguage.signs.objects.space;

import org.enguage.repertoire.Similarity;
import org.enguage.signs.Sign;
import org.enguage.signs.objects.Numeric;
import org.enguage.signs.objects.Temporal;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.objects.expr.Function;
import org.enguage.signs.objects.list.Item;
import org.enguage.signs.objects.list.Items;
import org.enguage.signs.objects.list.Transitive;
import org.enguage.signs.symbol.config.Colloquial;
import org.enguage.signs.symbol.config.Plural;
import org.enguage.signs.symbol.where.Where;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;
import org.enguage.util.sys.Shell;

public class Sofa extends Shell {
	static private Audit audit = new Audit( "Sofa" );

	public Sofa(){
		super( "Sofa" );
//		if (!Overlay.autoAttach())
//			audit.ERROR( "Ouch! in sofa" );
	}
	private static final int lSuccess  = 235397545; //Strings.hash( Shell.SUCCESS );
	private static final int lFail     = 106378;    //Strings.hash( Shell.FAIL );

	public Strings doCall( Strings a ) {
		//audit.in( "doCall", a.toString( Strings.CSV ));
		if (a.size() > 0) {
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Sofa.interpret() typically deals with:
			 * 		["string", "get", "martin", "name"]
			 * 		["colloquial", "both", "'I have'", "'I've'"]
			 */			
			String  type = a.remove( 0 );
			switch (Strings.hash( type )) {
				case lFail    :      return Shell.Fail;
				case lSuccess :      return Shell.Success;
				case Sign.id  :      return        Sign.interpret( Attribute.expand(   a ));
				case Link.id  :      return        Link.interpret(                     a );
				case Item.id  :      return        Item.interpret( Attribute.expand23( a ));
				case Items.id :      return       Items.interpret(                     a  );
				case Value.id :      return       Value.interpret( Attribute.expand23( a ));
				case Where.id :      return       Where.interpret( Attribute.expand23( a ));
				case Plural.id :     return      Plural.interpret( Attribute.expand23( a ));
				case Entity.ID :     return      Entity.interpret( Attribute.expand23( a ));
				case Context.id :    return     Context.interpret( Attribute.expand23( a ));
				case Numeric.id  :   return     Numeric.interpret( Attribute.expand23( a ));
				case Overlay.id  :   return     Overlay.interpret( Attribute.expand23( a ));
				case Temporal.id :   return    Temporal.interpret( Attribute.expand23( a ));
				case Function.id :   return    Function.interpret( Attribute.expand23( a ));
				case Variable.id :   return    Variable.interpret( Attribute.expand23( a ));
				case Similarity.id:  return  Similarity.interpret( Attribute.expand23( a ));
				case Colloquial.id:  return  Colloquial.interpret(                     a  );
				case Transitive.id:  return  Transitive.interpret( Attribute.expand23( a ));
				case Transaction.id: return Transaction.interpret( Attribute.expand23( a ));
				default :
					audit.ERROR( "Sofa.hash(): "+ type +".id should be: "+ Strings.hash( type ));
					return Fail;
		}	}
		audit.ERROR("doCall() fails - "+ (a==null?"no params":"not enough params: "+ a.toString()));
		return Fail;
	}
	
	// perhaps need to re-think this? Do we need this stage - other than for relative concept???
//	private Strings xdoSofa( Strings prog ) {
//		String cmd = prog.get( 0 );
//		char firstCh = cmd.charAt( 0 );
//		return (Strings.DOUBLE_QUOTE == firstCh ||
//				Strings.SINGLE_QUOTE == firstCh) ?
//					Strings.stripQuotes( cmd )
//					: doCall( prog );
//	}

	private Strings doNeg( Strings prog ) {
		//audit.traceIn( "doNeg", prog.toString( Strings.SPACED ));
		boolean negated = prog.get( 0 ).equals( "!" );
		Strings rc = doCall( prog.copyAfter( negated ? 0 : -1 ) ); // was do sofa
		if (negated) rc = rc.equals( Success ) ? Fail : rc.equals( Fail ) ? Success : rc;
		return rc; // */audit.traceOut( rc );
	}

/*private static String doAssign( Strings prog ) { // x = a b .. z
	TRACEIN1( "'%s'", arrayAsChars( prog, SPACED ));
	int assignment = 0 == .compareTo( prog[ 1 ], "=" );
	Strings e = copyStringsAfter( prog, assignment ? 1 : -1 );
	long rc = doNeg( e );
	if (assignment) {
		if (0 == .compareTo( "value", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning STRING %s = %s", prog.get( 0 ), rc ? (String )rc : "" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? (String )rc : "" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? (String )rc : "" );
		} else if (0 == .compareTo( "exists", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning BOOLEAN %s = %s", prog.get( 0 ), rc ? "true" : "false" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? "true" : "false" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? "true" : "false" );
		} else {
			printf( "type conversion error in '%s'\n", arrayAsChars( prog, SPACED ));
	}	}
	deleteStrings( &e, KEEP_ITEMS );
	TRACEOUTint( rc );
	return rc ;
}// */

	// a b .. z {| a b .. z}
	private Strings doOrList( Strings a ) {
		//audit.traceIn( "doOrList", a.toString( Strings.SPACED ));
		Strings rc = Fail;
		for (int i = 0, sz = a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "||" );
			i += cmd.size(); // left pointing at "|" or null
			if (rc.equals( Fail )) rc = doNeg( cmd ); // only do if not yet succeeded -- was doAssign()
		}
		//return audit.traceOut( rc );
		return rc;
	}

	private Strings doAndList( Strings a ) {
		//audit.traceIn( "doAndList", a.toString( Strings.SPACED ));
		Strings rc = Success;
		for (int i=0, sz=a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "&&" );
			//audit.debug( "cmd=" + cmd +", i="+ i );
			i += cmd == null ? 0 : cmd.size();
			if (rc.equals( Success )) rc = doOrList( cmd );
		}
		return rc; // */ audit.traceOut( rc );
	}

	private Strings doExpr( Strings a ) {
		//audit.traceIn( "doExpr", a.toString( Strings.SPACED ));
		Strings cmd = new Strings(); // -- build a command...
		while (0 < a.size() && !a.get( 0 ).equals( ")" )) {
			if (a.get( 0 ).equals( "(" )) {
				a.remove( 0 );
				cmd.addAll( doExpr( a ));
			} else {
				cmd.add( a.get( 0 ));
				a.remove( 0 ); // KEEP_ITEMS!
			}
			//audit.debug( "a="+ a.toString() +", cmd+"+ cmd.toString() );
		}
		Strings rc = doAndList( cmd );
		if ( 0 < a.size() ) a.remove( 0 ); // remove ")"
		return rc; // */audit.traceOut( rc );
	}
	public Strings interpret( Strings sa ) {
		Strings a = new Strings( sa );
		for (String s : sa) {
			if (   s.equals("&&") 
				|| s.equals("||")
				|| s.equals("(")
				|| s.equals("!")
			   ) {
				return doCall( a ); //doSofa( a );
		}	}
		return new Strings( doExpr( a )); // still need to check if it is a constant
	}
	
	public static void main( String[] argv ) { // sanity check...
		Sofa cmd = new Sofa();
		if (argv.length > 0) {
			cmd.interpret( new Strings( argv ));
		} else {
			Audit.allOn();
			Audit.traceAll( true );
			Audit.log( "Sofa: Ovl is: "+ Overlay.Get().toString());
			cmd.run();
}	}	}
