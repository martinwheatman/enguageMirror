package org.enguage.util;

import java.util.ArrayList;
import java.util.Iterator;

public class Tags extends ArrayList<Tag> {
	
	static final         long serialVersionUID = 0;
	
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
		if (patterns == null || size() < patterns.size()) return false;
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
	public String toXml( Indent indent ) {
		String oldName = "";
		String str  = "\n"+indent.toString();
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			Tag t = ti.next();
			str += (t.name().equals( oldName ) ? "\n"+indent.toString() : "") + t.toXml( indent );
			oldName = t.name();
		}
		return str;
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
