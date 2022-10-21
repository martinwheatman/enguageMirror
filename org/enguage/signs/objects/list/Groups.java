package org.enguage.signs.objects.list;

import java.util.ArrayList;

import org.enguage.signs.symbol.reply.Reply;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Groups {
	static private Audit audit = new Audit( "Group" );
	
	static private String group="";
	static public  void   group( String by ) { group=by; }
	static public  String group() { return group; }
	
	// ========================================================================
	static class Group {
		public Group( String nm ) { name = nm; }
		
		private String  name = new String();
		public  String  name() { return name; }
		
		private Strings items = new Strings();
		public  Group   item( String itm ) {
			items.add( itm );
			return this;
		}
		public String toString() {
			return	items.toString( Reply.andListFormat())
					+ (name.equals( "" ) ? "" : " "+ name);
	}	}
	// ========================================================================
	
	private ArrayList<Group> groups = new ArrayList<Group>();
	
	public  void add( String name, String item ) {
		boolean added = false;
		for (Group g : groups)
			if (g.name().equals( name )) {
				g.item( item );
				added = true;
				break;
			}
		if (!added)
			groups.add( new Group( name ).item( item ));
	}
	public  void add( String name, Strings items ) {
		boolean added = false;
		for (Group g : groups)
			if (g.name().equals( name )) {
				g.item( items.toString() );
				added = true;
				break;
			}
		if (!added)
			groups.add( new Group( name ).item( items.toString() ));
	}
	public String toString() {
		String str = "";
		int i=0;
		for (Group g : groups)
			str += (i++==0 ? "" : "; and, ") + g.toString();
		return str;
	}
	// ------------------------------------------------------------------------
	public static void main( String args []) {
		Groups g = new Groups();
		// format=QUANTITY,UNIT of,,LOCATOR LOCATION
		// "group by LOCATION" -- this is done in engine, and is confirmed in Item.toString()
		// if format snippet contains variable, call this the group, the rest, items.
		g.add( "from Sainsburys",      new Strings( "a/cup of/coffee", '/' )); // subrc?
		g.add( "from Sainsburys",      new Strings( "a biscuit" ));
		g.add( "from the dairy aisle", new Strings( "milk" ));
		g.add( "from the dairy aisle", "cheese" );
		g.add( "from the dairy aisle", "eggs" );
		g.add( "",                     "toothpaste" );
		audit.title( "Groups" );
		Audit.log( g.toString() +"." );
}	}
