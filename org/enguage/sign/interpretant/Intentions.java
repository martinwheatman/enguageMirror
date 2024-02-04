package org.enguage.sign.interpretant;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.sign.Config;
import org.enguage.sign.interpretant.intentions.Reply;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;

public class Intentions extends ArrayList<Intention> {
	
	//private static final Audit audit = new Audit("Intentions"
	private static final long  serialVersionUID = 0L;

	public  Intentions append(        Intention in ){add( in ); return this;}
	public  Intentions insert( int i, Intention in ){add( i==-1 ? 0 : i, in ); return this;}

	public Intentions insert( Intention intn ) {
		int sz = size();
		if (sz == 0)
			insert( 0, intn );
		else {
			// reply always goes at the end
			Intention in = get( sz-1 );
			if (in.type() == Intention.N_REPLY)
				insert( sz-1, intn );
			else // end is not a reply, append list
				add( intn );
		}
		return this;
	}

	public String toXml() {
		StringBuilder intentions = new StringBuilder();
		Audit.incr();

		String indent = Audit.indent();
		for (Intention in : this)
			intentions.append(
					"\n"+ indent +
					Attribute.asString( 
							Intention.typeToString( in.type() ), in.value()
			)		);
		
		Audit.decr();
		return intentions.toString();
	}
	public String toStringIndented( boolean auditIntents ) {
		StringBuilder sb = new StringBuilder();
		int sz = size();
		if (sz == 1)
			sb.append( ", "+ get( 0 ));
		else if (sz > 1) {
			int line = 0;
			for (Intention in : this)
				sb.append(
						(line++ == 0 ? ":" : ";") + "\n"
						+ (auditIntents ? Audit.indent():"") +"    "+ in
				);
		}
		return sb.toString();
	}
	
	// ------------------------------------------------------------------------
	
	public Reply mediate() {
		// It's okay where there are no intentions (e.g. "i can say X")
		Reply r = new Reply().answer( Config.S_OKAY );
		r.type( Response.typeFromStrings( Config.okay() ));
		
		Iterator<Intention> ai = this.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();

			if (in.type() == Intention.N_FINALLY)
				in.andFinally( r );

			else if (!r.isDone()) // 'done' is maintained within r
				// think, do, say...
				// ...think (&run) will replace r
				r = in.mediate( r );
		}
		return r;
}	}
