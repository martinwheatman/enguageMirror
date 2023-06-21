package org.enguage.util.tag;

import java.util.ArrayList;
import java.util.Iterator;

import org.enguage.util.Indentation;

public class Tags extends ArrayList<Tag> {
	
	static final         long serialVersionUID = 0;
	
	public Tags() {}
	public Tags(TokenStream ts) {
		Tag tag;
		while (ts.hasNext()) {
			add( tag = new Tag( ts ));
			if (tag.name().equals(""))
				break;
	}	}
	
	public boolean equals( Tags ta ) {
		if (ta == null || size() != ta.size())
			return false;
		else {
			Iterator<Tag> it = iterator(), tait = ta.iterator();
			while (it.hasNext())
				if (!it.next().equals( tait.next() ))
					return false;
		}
		return true;
	}
	public boolean matches( Tags patterns ) {
		if (patterns.size() == 0) return true; // ALL = "" 
		if (size() < patterns.size()) return false;
		Iterator<Tag> it = iterator(),
				pit = patterns.iterator();
		while (it.hasNext()) // ordered by patterns
			if (!it.next().matches( pit.next() ))
				return false;
		return true;
	}

	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toXml( Indentation indent ) {
		String oldName = "";
		StringBuilder str  = new StringBuilder();
		str.append( "\n" + indent.toString());
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			Tag t = ti.next();
			str.append( (t.name().equals( oldName ) ? "\n"+indent.toString() : "") + t.toXml( indent ));
			oldName = t.name();
		}
		return str.toString();
	}
	public String toString() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toString();
			if (ti.hasNext()) str += " ";
		}
		return str;
	}
	public String toText() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			str += ti.next().toText();
			if (ti.hasNext()) str += " ";
		}
		return str;
}	}
