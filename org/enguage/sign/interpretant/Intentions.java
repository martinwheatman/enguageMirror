package org.enguage.sign.interpretant;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.sign.Config;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;

public class Intentions extends ArrayList<Intention> {
	
	//private static final Audit audit = new Audit("Intentions"
	private static final long  serialVersionUID = 0L;

	public  Intentions append(        Intention in ){add( in ); return this;}
	public  Intentions insert( int i, Intention in ){add( i==-1 ? 0 : i, in ); return this;}

	public enum Insertion {
		UNKNOWN, PREPEND, HEADER, TAILER, APPEND, NEXT, PREV
	}
	
	private int  lastInsertion = 0;
	public  int  lastInsertion() {return lastInsertion;}
	private void lastInsertion( int i ) {lastInsertion = i;}
	
	public Intentions insert( Insertion ins, Intention intn ) {
		switch (ins) {
			case TAILER:  // default!
			case UNKNOWN: lastInsertion( size()-1 ); break;
			case PREPEND: lastInsertion(        0 ); break;
			case HEADER:  lastInsertion(        1 ); break;
			case APPEND:  lastInsertion( size()   ); break;
			case NEXT:    lastInsertion( lastInsertion() + 1 ); break;
			case PREV:    lastInsertion( lastInsertion()     ); break;
		}
		insert( lastInsertion(), intn ); 
		return this;
	}
	public static Insertion getInsertionType( String cmd ) {
		if (cmd.equals(  "header" )) return Insertion.HEADER;
		if (cmd.equals( "prepend" )) return Insertion.PREPEND;
		if (cmd.equals(  "tailer" )) return Insertion.TAILER;
		if (cmd.equals(  "append" )) return Insertion.APPEND;
		if (cmd.equals(    "next" )) return Insertion.NEXT;
		if (cmd.equals(    "prev" )) return Insertion.PREV;
		if (cmd.equals( "finally" )) return Insertion.APPEND;
		return Insertion.UNKNOWN;
	}

	// used in autopoiesis:
	public  Intentions appendIntention( int typ, String val ) {
		add( new Intention( typ, val ));
		return this;
	}
	public  Intentions tailPrepend( Intention in ){add( size(), in ); return this;}

	public String toXml() {
		StringBuilder intentions = new StringBuilder();
		Audit.incr();

		String indent = Audit.indent();
		for (Intention in : this)
			intentions.append(
					"\n"+ indent +
					Attribute.asString( 
							Intention.typeToString( in.type() ), in.value() )
			);
		
		Audit.decr();
		return intentions.toString();
	}
	public String toStringIndented() {
		StringBuilder sb = new StringBuilder();
		int sz = size();
		if (sz == 1)
			sb.append( ", "+ get( 0 ));
		else if (sz > 1) {
			int line = 0;
			for (Intention in : this)
				sb.append(
						(line++ == 0 ? ":" : ";") + "\n"
						+ Audit.indent() +"    "+ in
				);
		}
		return sb.toString();
	}
	
	// ------------------------------------------------------------------------
	public Reply mediate() {
		
		// "ok" -- in case there are no intentions (e.g. i can say X")
		Reply r = new Reply().answer( Config.successStr() );
		
		Iterator<Intention> ai = this.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();

			if (in.type() == Intention.N_FINALLY)
				in.andFinally( r );

			else if (!r.isDone())
				r = in.mediate( r ); // thenFinally, think, do, say...
		}
		return r;
}	}
