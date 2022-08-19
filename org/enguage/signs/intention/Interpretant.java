package org.enguage.signs.intention;

import java.util.ArrayList;

public class Interpretant extends ArrayList<Intention> {
	static final long serialVersionUID = 0L;

	public  Interpretant append(        Intention in ){ add(    in ); return this;}
	public  Interpretant insert( int i, Intention in ){ add( i==-1 ? 0 : i, in ); return this;}

	// used in autopoiesis:
	public  Interpretant appendIntention( int typ, String val ) {
		add( new Intention(typ,val));
		return this;
	}
	public  Interpretant tailPrepend(    Intention in ){add( size(), in );return this;}

}
