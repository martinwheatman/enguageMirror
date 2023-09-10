package org.enguage.sign.object.sofa;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.repertoires.concepts.Similarity;
import org.enguage.sign.Sign;
import org.enguage.sign.object.Numeric;
import org.enguage.sign.object.Temporal;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.expr.Function;
import org.enguage.sign.object.list.Item;
import org.enguage.sign.object.list.Items;
import org.enguage.sign.object.list.Transitive;
import org.enguage.sign.symbol.config.Colloquial;
import org.enguage.sign.symbol.config.Plural;
import org.enguage.sign.symbol.pronoun.Pronoun;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.http.Html;
import org.enguage.util.http.Http;
import org.enguage.util.http.InfoBox;
import org.enguage.util.http.SapGraph;
import org.enguage.util.strings.Strings;

public class Perform {
	private Perform() {}
	
	private static final Audit audit = new Audit( "Sofa" );
	
	public  static Strings interpret( Strings a ) {
		if (a.isEmpty()) {
			audit.error("perform interpret: not enough params: "+ a.toString());
		} else {
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Perform.interpret() typically deals with:
			 * 		["entity", "get", "martin", "name"]
			 * 		["colloquial", "both", "'I have'", "'I've'"]
			 */			
			String  type = a.remove( 0 );
			switch (Strings.hash( type )) {
				case Item       .id: return        Item.interpret( Attribute.expand23( a ));
				case Link       .id: return        Link.interpret(                     a  );
				case Sign       .ID: return        Sign.interpret( Attribute.expand(   a ));
				case Http       .ID: return        Http.interpret( Attribute.expand(   a ));
				case Audit      .ID: return       Audit.interpret( Attribute.expand(   a ));
				case Items      .id: return       Items.interpret(                     a  );
				case Value      .id: return       Value.interpret( Attribute.expand23( a ));
				case Where      .id: return       Where.interpret( Attribute.expand23( a ));
				case Plural     .id: return      Plural.interpret( Attribute.expand23( a ));
				case Entity     .ID: return      Entity.interpret( Attribute.expand23( a ));
				case Concept    .ID: return     Concept.interpret( Attribute.expand23( a ));
				case InfoBox    .ID: return     InfoBox.interpret( Attribute.expand(   a ));
				case Pronoun    .ID: return     Pronoun.interpret( Attribute.expand23( a ));
				case Context    .id: return     Context.interpret( Attribute.expand23( a ));
				case Numeric    .id: return     Numeric.interpret( Attribute.expand23( a ));
				case Overlay    .id: return     Overlay.interpret( Attribute.expand23( a ));
				case Temporal   .id: return    Temporal.interpret( Attribute.expand23( a ));
				case Function   .id: return    Function.interpret( Attribute.expand23( a ));
				case Variable   .id: return    Variable.interpret( Attribute.expand23( a ));
				case SapGraph   .ID: return    SapGraph.interpret( Attribute.expand23( a ));
				case Similarity .ID: return  Similarity.interpret( Attribute.expand23( a ));
				case Colloquial .id: return  Colloquial.interpret(                     a  );
				case Transitive .id: return  Transitive.interpret( Attribute.expand23( a ));
				case Repertoires.ID: return Repertoires.interpret(                     a  );
				default :
					audit.error( "Perform:Strings.hash(): "+ type +".id should be: "+ Strings.hash( type ));
					return Response.Fail;
		}	}
		return Response.Fail;
}	}
