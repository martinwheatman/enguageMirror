package com.yagadi.enguage.expression;

import com.yagadi.enguage.util.Strings;

public class Fmt {
	/** Fmt:
	 * e.g. ["Ok", ",", "you", "need", "..."]
	 */
	private boolean v = false;
	public  boolean variable() {return v;}
	
	private Strings ormat = new Strings();
	public  Strings ormat() {return ormat;}
	public  void    ormat(Strings s) {
		ormat = s;
		if (ormat.contains("...")) v = true;
	}
}
