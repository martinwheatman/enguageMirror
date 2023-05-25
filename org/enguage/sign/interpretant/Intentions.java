package org.enguage.sign.interpretant;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.repertoires.Engine;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.attr.Attribute;

public class Intentions extends ArrayList<Intention> {
	
	private static final Audit audit = new Audit("Intentions");
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
	public Reply mediate( Reply r ) {
		audit.in( "mediate", "r="+ r );
		
		// if we've matched we must have understood/recognised
		r.answer( "ok" ).response( Response.N_OK );

		Iterator<Intention> ai = this.iterator();
		while (ai.hasNext()) {
			Intention in = ai.next();
			r = in.type() == Intention.N_ALLOP
					? Engine.interp( in, r )
					: in.mediate( r ); // thenFinally, think, do, say...
		}
		audit.out();
		return r;
	}
	public String toStringIndented() {
		StringBuilder intents = new StringBuilder();
		int sz = size();
		if (sz == 1)
			intents.append( ", "+ get( 0 ));
		else if (sz > 1) {
			int line = 0;
			for (Intention in : this)
				intents.append((line++ == 0 ? ":" : ";") + "\n" + Audit.indent() +"    "+ in);
		}
		return intents.toString();
}	}
