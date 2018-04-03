package org.enguage.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class Tags extends ArrayList<Tag> {
	
	static final         long serialVersionUID = 0;
	static private       Audit           audit = new Audit( "Tags", false );
	
	public Tags() { super(); }
	public Tags( Strings words ) {
		// "if X do Y" -> [ <x prefix=["if"]/>, <y prefix=["do"] postfix="."/> ]
		Tag t = new Tag();
		for ( String word : words ) {
			if (Strings.isUpperCaseWithHyphens( word ) && !word.equals( "I" )) { // TODO: remove "I"
				Strings arr = new Strings( word, '-' ); // should at least be array of 1 element!
				int  j = 0, asz = arr.size();
				for (String subWord : arr) {
					subWord = subWord.toLowerCase( Locale.getDefault());
					if ( asz > ++j ) // 
						t.append( subWord, subWord ); // non-last words in array
					else
						t.name( subWord ); // last word in array
				}
				add( t );
				t = new Tag();
			} else
				t.prefix( word );
		}
		if (!t.isEmpty()) add( t );
	}
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
	public String toXml() { return toXml( new Indent( "   " )); }
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
	}
	public String toLine() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			Tag t = ti.next();
			str += ( " "+t.prefix().toString()+" <"+t.name() +" "+ t.attributes().toString() +"/> "+t.postfix());
		}
		return str;
	}
	
	// --- test code...
	public static void main(String args[]) {
		Audit.allOn();
		audit.tracing = true;
		Tags t = new Tags();
		t.add( new Tag( "what is ", "X" ).append( Tag.numeric, Tag.numeric ) );
}	}
