package org.enguage.util;

import java.io.PrintStream;

public class Indent {
	private String indent = "|  ";
	
	public Indent() {}
	public Indent( String str ) { indent = str; }
	
	private String indentStr = "";
	public  void setIndentStr() {
		indentStr = "";
		for (int i=0; i < level; i++)
			indentStr += indent;
	}
	private int  level = 0;
	public  void reset() { level=0; setIndentStr();}
	public  void incr() { level++; setIndentStr();}
	public  void decr() { if (level>0) level--; setIndentStr(); }
	
	public  void print( PrintStream out ) { out.append( indentStr ); }
	public  String toString() { return indentStr; }
	public static void main(String args[]) {
		Indent indent = new Indent();
		System.out.println(indent.toString() +"a");
		indent.incr();
		System.out.println(indent.toString() +"b");
		indent.incr();
		System.out.println(indent.toString() +"c");
		indent.incr();
		System.out.println(indent.toString() +"d");
		indent.decr();
		System.out.println(indent.toString() +"e");
		indent.decr();
		System.out.println(indent.toString() +"f");
		indent.decr();
		System.out.println(indent.toString() +"g");
	}
}
