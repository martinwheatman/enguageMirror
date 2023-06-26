package org.enguage.util.audit;

import java.io.PrintStream;

public class Indentation {
	private String indentValue = "|  ";
	private int   indentLength = indentValue.length();
	
	public Indentation() {}
	public Indentation( String str ) {
		indentValue = str;
		indentLength = indentValue.length();
	}
	
	private String indentStr = "";
	private int    indentLvl = 0;
	
	public  void reset() {indentLvl=0; indentStr = "";}
	public  void incr() {indentLvl++; indentStr += indentValue;}
	public  void decr() {
		if (indentLvl>0) indentLvl--;
		indentStr = indentStr.substring(0,indentLvl*indentLength);
	}
	
	public  void print( PrintStream out ) {out.print( indentStr );}
	public  String toString() {return indentStr;}
	public static void main(String[] args) {
		Indentation indent = new Indentation();
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
