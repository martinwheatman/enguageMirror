package org.enguage.signs.interpretant;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.repertoires.Engine;
import org.enguage.repertoires.written.AtpRpt;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.util.Audit;
import org.enguage.util.attr.Attribute;

public class Intentions extends ArrayList<Intention> {
	static final long serialVersionUID = 0L;

	public  Intentions append(        Intention in ){add( in ); return this;}
	public  Intentions insert( int i, Intention in ){add( i==-1 ? 0 : i, in ); return this;}

	// used in autopoiesis:
	public  Intentions appendIntention( int typ, String val ) {
		add( new Intention( typ, val ));
		return this;
	}
	public  Intentions tailPrepend( Intention in ){add( size(), in );return this;}

	public String toXml() {
		StringBuilder intentions = new StringBuilder();
		Audit.incr();

		String indent = Audit.indent();
		for (Intention in : this)
			intentions.append( "\n"+ indent + Attribute.asString( Intention.typeToString( in.type() ), in.value() )) ;
		
		Audit.decr();
		return intentions.toString();
	}
	public Reply mediate( Reply r ) {
		Iterator<Intention> ai = this.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();
			switch (in.type()) {
				case Intention.allop  : r = Engine.interp( in, r ); break;
				case Intention.atpRptApp :
				case Intention.atpRptCre : r = AtpRpt.interp( in, r ); break;
				default: r = in.mediate( r ); // thenFinally, think, do, say...
		}	}
		return r;
	}
	public String toStringIndented() {
		String intents = "";
		int sz = size();
		if (sz == 1)
			intents = ", "+ get(0);
		else if (sz > 1) {
			int line = 0;
			for (Intention in : this)
				intents += (line++ == 0 ? ":" : ";") + "\n" + Audit.indent() +"    "+ in;
		}
		return intents;
}	}
