package org.enguage.util;

import org.enguage.util.Pent;

public class Pent implements Comparable<Pent> {
	/* This class describes a path entity, which in most cases will just be a name
	 * BUT sometime may include whether it is a directory, and a type.
	 */
	private String  name;
	public  String  name()  { return name; }
	
	// value is managed in the application code
	private String  value;
	public  String  value() { return value; }
	
	public  boolean type()  { return type; }
	private boolean type;

	public Pent( String nm ) { this( nm, "", false ); }
	public Pent( String nm, String val, boolean typ ) {
		name = nm;
		value = val;
		type = typ;
	}
	
	@Override
	public int compareTo(Pent another) { return  name().compareTo( another.name()); }

	// ------
	static public int maxNameLength( Pent[] pa ) {
		int max=0, tmp;
		for (Pent p : pa )
			if (max < (tmp = p.name().length()))
				max = tmp;
		return max;
	}
	
	public static void main( String args[] ) {
		Pent p = new Pent( "hello", "world", true );
		System.out.println( "test is "+ p.toString() );
}	}
