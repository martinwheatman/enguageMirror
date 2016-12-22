package com.yagadi.enguage.expression;

import com.yagadi.enguage.util.Strings;

public class Ans {
	public Strings nswer = new Strings();
	
	private boolean appending = false;
	public  boolean isAppending() { return appending; }
	public  void    appendingIs( boolean b ) { appending = b; }

}
