package org.enguage.util.tag;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.signs.symbol.config.Plural;
import org.enguage.util.Audit;
import org.enguage.util.Indent;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;

public class Tag {
	public static final String EMPTY_PREFIX = "";
	
	public final String name;
	
	// -- constructors...
	public Tag( String n ) {name = n;}
	public Tag( Tag orig ) {
		this( orig.name );
		prefix( orig.prefix() );
		attributes( new Attributes( orig.attributes()));
		content( orig.content());
	}
	public Tag( ListIterator<String> si ) {
		Strings pref = Strings.copyUntil( si, "<" );
		String  nm = "";
		Attributes a = new Attributes();
		Tags    cont = new Tags();
		if (si.hasNext() ) {
			String nxt = si.next();
			if (nxt.equals( "/" )) {
				si.next(); // consume name + ">" -- found END </tag>
				si.next();
			} else {
				nm = nxt;
				a = new Attributes( si );
				nxt = si.next();
				if (nxt.equals( "/" )) // consumed ">" ?
					si.next(); // no, consume ">" -- found STANDALONE <tag/>
				else {  // look for children....
					Tag tmp;
					while (si.hasNext()) {
						cont.add( tmp = new Tag( si ));
						if (tmp.name.equals("")) //.name().equals( t.name ))
							break;
		}	}	}	}
		name = nm;
		prefix( pref ).attributes( a ).content( cont );
	}
	// ************************************************************************
	
	private Strings prefix = new Strings();
	public  Strings prefix() {return prefix;}
	public  Tag     prefix( Strings strs ) {prefix = strs; return this;}
	public  Tag     prefixAdd( String s ) { prefix.append( s ); return this;}

	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Tag        attributes( Attribute a ) {attrs.add( a ); return this;}
	public  Tag        attributes( Attributes as ) {
		if (null != as) {
			attrs = as;
			attrs.nchars( as.nchars());
		}
		return this;
	}
	public  String attribute( String name ) { return attrs.value( name ); }
	public  Tag    append( String name, String value ) { attributes( new Attribute( name, value )); return this; }
	public  Tag    prepend( String name, String value ) { return add( 0, name, value );} 
	public  Tag    add( int posn, String name, String value ) {
		attrs.add( posn, new Attribute( name, value ));
		return this;
	}
	public  Tag    remove( int nth ) { attrs.remove( nth ); return this; }
	public  Tag    remove( String name ) { attrs.remove( name ); return this; }
	public  Tag    replace( String name, String value ) {
		attrs.remove( name );
		attrs.add( new Attribute( name, value ));
		return this;
	}
	
	public Attribute matchedAttr( String val ) {
		return new Attribute(
				name,
				Attribute.getValue( // prevents X="x='val'"
					name.equals("unit") ? Plural.singular( val ) : val
				));
	}

	public boolean isEmpty() {return name.equals("") && prefix().isEmpty();}

	Tags content = new Tags();
	public  Tags content() {return content;}
	public  Tag  content( Tags ta ) {if (null!=ta) content = ta; return this;}
	public  Tag  removeContent( int n ) {return content.remove( n );}
	public  Tag  content( int n, Tag t ) {content.add( n, t ); return this;}
	public  Tag  content( Tag child ) {
		if (null != child && !child.isEmpty())
			content.add( child );
		return this;
	}
	// --
	public boolean equals( Tag pattern ) {
		return  Plural.singular(  prefix.toString()).equals( Plural.singular( pattern.prefix().toString()))
			&& attributes().matches( pattern.attributes()) // not exact!!!
			&& content().equals( pattern.content());

	}
	public boolean matches( Tag pattern ) {
		return Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
			&& attributes().matches( pattern.attributes())
			&&    content().matches( pattern.content());
	}
	public boolean matchesContent( Tag pattern ) {
		return Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
				&& content().matches( pattern.content());
	}

	// *** toString ***********************************************************
	private static Indent indent = new Indent( "  " );
	public String toXml() { return toXml( indent );}
	public String toXml( Indent indent ) {
		int sz = content.size();
		String contentSep = sz > 0? ("\n" + indent.toString()) : sz == 1 ? " " : "";
		indent.incr();
		String attrSep = sz > 0  ? "\n  "+indent.toString() : " " ;
		String s = prefix().toString( Strings.OUTERSP )
				+ (name.equals( "" ) ? "" :
					("<"+ name
							+ attrs.toString( attrSep )
							+ (0 == content().size() ? 
									"/>" :
									( ">"
									+ content.toXml( indent )
									+ contentSep
									+ "</"+ name +">"
				  )	)		  )		);
		indent.decr();
		return s;
	}
	public String toString() {
		return prefix().toString() + (name.equals( "" ) ? "" :
			"<"+ name +" "+ attrs.toString()+  // attrs doesn't have preceding space
			(content().isEmpty() ?
					"/>" : ( ">"+ content.toString() + "</"+ name +">" )));
	}
	public String toText() {
		return prefix().toString()
			+ (prefix().toString()==null||prefix().toString().equals("") ? "":" ")
			+ (name.equals( "" ) ? "" :
				( name.toUpperCase( Locale.getDefault() ) +" "+
					(content().isEmpty() ? "" : content.toText() )));
	}
	public String toLine() {
		return prefix().toString()
				+ (content().isEmpty() ? "" : content.toText() );
	}
	// ************************************************************************
	public Tag findByName( String nm ) {
		Tag rc = null;
		if (name.equals( nm ))
			rc = this; // found
		else {
			ArrayList<Tag> ta = content();
			for (int i=0; i<ta.size() && null == rc; ++i) // find first child
				rc = ta.get( i ).findByName( nm );
		}
		return rc;
	}
	// ************************************************************************
	public static void main( String argv[]) {
		Strings s = new Strings(
			"a<xml type='xml'>b\n"+
			" <config \n"+
			"   CLASSPATH=\"/home/martin/ws/Enguage/bin\"\n"+
			"   DNU=\"I do not understand\" >c\n" +	
            "   <concepts>d\n"+
			"     <concept id=\"colloquia\" op=\"load\"/>e\n"+
			"     <concept id=\"needs\"     op=\"load\"/>f\n"+
			"     <concept id=\"engine\"    op=\"load\"/>g\n"+
			"   </concepts>h\n"+
			"   </config>i\n"+
			"</xml> j"
		);
		Tag t = new Tag( s.listIterator() );
		Audit.log( "tag:"+ t.toXml());
}	}
