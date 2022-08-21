package org.enguage.signs.interpretant;

import java.util.ArrayList;

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
		String intentions = "";
		for (Intention in : this)
			intentions += "\n      " + Attribute.asString( Intention.typeToString( in.type() ), in.value() );
		return intentions;
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
