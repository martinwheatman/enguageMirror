package org.enguage.signs.objects.space;

import org.enguage.repertoire.Repertoire;
import org.enguage.repertoire.Similarity;
import org.enguage.repertoire.concept.Ideas;
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

public class Sofa {
	private static final Audit audit = new Audit( "Sofa" );

	private Sofa() {}
	
	public static Strings interpret( Strings a ) {
		if (a.isEmpty()) {
			audit.error("doCall() fails - not enough params: "+ a.toString());
		} else {
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Sofa.interpret() typically deals with:
			 * 		["string", "get", "martin", "name"]
			 * 		["colloquial", "both", "'I have'", "'I've'"]
			 */			
			String  type = a.remove( 0 );
			switch (Strings.hash( type )) {
				case Item.id  :      return        Item.interpret( Attribute.expand23( a ));
				case Link.id  :      return        Link.interpret(                     a );
				case Sign.ID  :      return        Sign.interpret( Attribute.expand(   a ));
				case Audit.ID:       return       Audit.interpret(                     a  );
				case Ideas.ID:       return       Ideas.interpret(                     a  );
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
				case Similarity.ID:  return  Similarity.interpret( Attribute.expand23( a ));
				case Colloquial.id:  return  Colloquial.interpret(                     a  );
				case Repertoire.ID:  return  Repertoire.interpret(                     a  );
				case Transitive.id:  return  Transitive.interpret( Attribute.expand23( a ));
				case Transaction.id: return Transaction.interpret( Attribute.expand23( a ));
				default :
					audit.error( "Sofa.hash(): "+ type +".id should be: "+ Strings.hash( type ));
					return Shell.Fail;
		}	}
		return Shell.Fail;
}	}
