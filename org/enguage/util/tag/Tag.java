package org.enguage.util.tag;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.util.Audit;
import org.enguage.util.Indent;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.vehicle.Plural;

public class Tag {
	//private static Audit audit = new Audit( "Tag" );
	
	public static final String emptyPrefix = "";
	
	private Strings prefix = new Strings();
	public  Strings prefix() { return prefix; }
	public  Tag     prefix( Strings s ) { prefix = s; return this; }
	public  Tag     prefix( String str ) { prefix.append( str ); return this; }

	public Strings postfixAsStrings;
	public Strings postfixAsStrings() { return new Strings( postfix ); }
	public String  postfix = "";
	public String  postfix(  ) { return postfix; }
	public Tag     postfix( String str ) { postfix = str; return this; }

	private String name = "";
	public  String name() { return name; }
	public  Tag    name( String nm ) { if (null != nm) name = nm; return this; }
	
	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Tag        attributes( Attribute a ) {
		attrs.add( a );
		return this;
	}
	public  Tag        attributes( Attributes as ) {
		attrs = as;
		attrs.nchars( as.nchars());
		return this;
	}
	public  String     attribute( String name ) { return attrs.value( name ); }
	public  Tag        append( String name, String value ) { attributes( new Attribute( name, value )); return this; }
	public  Tag        prepend( String name, String value ) { return add( 0, name, value );}  // 0 == add at 0th index!
	public  Tag        add( int posn, String name, String value ) {
		attrs.add( posn, new Attribute( name, value ));
		return this;
	}
	public  Tag        remove( int nth ) { attrs.remove( nth ); return this; }
	public  Tag        remove( String name ) { attrs.remove( name ); return this; }
	public  Tag        replace( String name, String value ) {
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

	public boolean isEmpty() { return name.equals("") && prefix().size() == 0; }

	Tags content = new Tags();
	public  Tags content() {return content;}
	public  Tag  content( Tags ta ) { content = ta; return this; }
	public  Tag  removeContent( int n ) { return content.remove( n ); }
	public  Tag  content( int n, Tag t ) { content.add( n, t ); return this; }
	public  Tag  content( Tag child ) {
		if (null != child && !child.isEmpty()) {
			//type = START;
			content.add( child );
		}
		return this;
	}
	// --
	public boolean equals( Tag pattern ) {
		return  attributes().size() < pattern.attributes().size()   ||
		       (content().size()==0 && pattern.content().size()!=0) ||
			   (content().size()!=0 && pattern.content().size()==0) ?
			false
			:  Plural.singular(  prefix.toString()).equals( Plural.singular( pattern.prefix().toString()))
			&& Plural.singular( postfix()).equals( Plural.singular( pattern.postfix()))
			&& attributes().matches( pattern.attributes()) // not exact!!!
			&& content().equals( pattern.content());

	}
	public boolean matches( Tag pattern ) {
		return ( attributes().size() < pattern.attributes().size()  ||
                (content().size()==0 && pattern.content().size()!=0)  ) ?
			false 
		    :  Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
			&& postfix().equals( pattern.postfix())
			&& attributes().matches( pattern.attributes())
			&&    content().matches( pattern.content());
	}
	public boolean matchesContent( Tag pattern ) {
		return  (content().size()==0 && pattern.content().size()!=0) || 
			    (content().size()!=0 && pattern.content().size()==0)   ?
			false
			:	Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
				&& postfix().equals( pattern.postfix())
				&& content().matches( pattern.content());
	}

	// -- constructors...
	public Tag() {}
	public Tag( ListIterator<String> si ) { this( next( si )); }

	public Tag( String pre, String nm ) {
		this();
		prefix( new Strings( pre )).name( nm );
	}
	public Tag( String pre, String nm, String post ) {
		this( pre, nm );
		postfix( post );
	}
	public Tag( Tag orig ) {
		this( orig.prefix().toString(), orig.name(), orig.postfix());
		attributes( new Attributes( orig.attributes()));
		content( orig.content());
	}
	// -- fromXML
	static public Tag next( ListIterator<String> si ) {
		Tag t = new Tag();
		t.prefix( Strings.copyUntil( si, "<" ));
		if (si.hasNext() ) {
			String nxt = si.next();
			if (nxt.equals( "/" )) {
				si.next(); // consume name
				si.next(); // consume ">" -- found END </tag> 
			} else {
				t.name( nxt ); // will be "/" on end list
				t.attributes( new Attributes( si ));
				nxt = si.next(); 
				if (nxt.equals( "/" )) // consumed ">" ?
					si.next(); // no, consume ">" -- found STANDALONE <tag/>
				else {  // look for children....
					Tag tmp;
					while (si.hasNext() && !(tmp = next( si )).name().equals( t.name ))
						t.content( tmp );
		}	}	}
		return t;
	}
	// -- toString
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
				  )	)		  )		)
				+ postfix;
		indent.decr();
		return s;
	}
	public String toString() {
		return prefix().toString() + (name.equals( "" ) ? "" :
			("<"+ name +" "+ attrs.toString()+  // attributes doesn't have preceding space
			(0 == content().size() ? "/>" : ( ">"+ content.toString() + "</"+ name +">" ))))
			+ postfix;
	}
	public String toText() {
		return prefix().toString()
			+ (prefix().toString()==null||prefix().toString().equals("") ? "":" ")
			+ (name.equals( "" ) ? "" :
				( name.toUpperCase( Locale.getDefault() ) +" "+  // attributes has preceding space
					(0 == content().size() ? "" : content.toText() )))
			+ postfix;
	}
	public String toLine() {
		return prefix().toString()
				+ (0 == content().size() ? "" : content.toText() )
				+ postfix ;
	}
	// --
	public Tag findByName( String nm ) {
		Tag rc = null;
		if (name().equals( nm ))
			rc = this; // found
		else {
			ArrayList<Tag> ta = content();
			for (int i=0; i<ta.size() && null == rc; ++i) // find first child
				rc = ta.get( i ).findByName( nm );
		}
		return rc;
	}
	// -- test code
	public static void main( String argv[]) {
		Audit.allOn();
		Strings s = new Strings(
			"a \n"+
			"<xml type='xml'>b\n"+
			" <config \n"+
			"   CLASSPATH=\"/home/martin/ws/Enguage/bin\"\n"+
			"   DNU=\"I do not understand\" >c\n" +	
            "   <concepts>d\n"+
			"     <concept id=\"colloquia\"      op=\"load\"/>e\n"+
			"     <concept id=\"needs\"          op=\"load\"/>f\n"+
			"     <concept id=\"engine\"         op=\"load\"/>g\n"+
			"   </concepts>h\n"+
	        "   </config>i\n"+
	        "</xml>"        );
		Tag t = new Tag( s.listIterator() );
		Audit.log( "tag:"+ t.toXml());
}	}
