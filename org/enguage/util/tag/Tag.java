package org.enguage.util.tag;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.sign.symbol.config.Plural;
import org.enguage.util.Audit;
import org.enguage.util.Indentation;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.sys.Fs;

public class Tag {
	public  static final int              ID = 13553; // #tag ;-)
	private static final String         NAME = "Tag";
	private static final Audit         audit = new Audit( NAME );
	public  static final String EMPTY_PREFIX = "";
	
	private        final String     name;
	public         final String     name() {return name;}
	
	private        final String     prefix;
	public         final String     prefix() {return prefix;}
	
	private        final Attributes attributes;
	public         final Attributes attributes() {return attributes;}
	
	private        final Tags       children;
	public         final Tags       content() {return children;}
	
	// -- constructors...
	public Tag( Tag orig ) {
		name = orig.name();
		prefix = orig.prefix();
		children = orig.content(); // no Tags copy constructor
		attributes = new Attributes( orig.attributes());
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
		prefix = pref.toString();
		attributes = a;
		children = cont;
	}
	
	// ************************************************************************
	private String doPrefix( TokenStream ts ) {
		StringBuilder prefixBuff = new StringBuilder();
		
		Token token = null;
		while (ts.hasNext()) {
			token = ts.getNext();
			if (token.string().equals( "<" )) {
				//ts.putNext( token ); read over "<"
				break;
			} else
				prefixBuff.append( token.toString());
		}
		if (token != null)
			prefixBuff.append( token.space() ); // add "<"'s whitespace to prefix!
		
		return prefixBuff.toString();
	}
	
	public Tag( TokenStream ts ) {
		String     p = doPrefix( ts );
		String     n = "";
		Attributes a = new Attributes();
		Tags       c = new Tags();
		
		if (ts.hasNext()) {
			Token token = ts.getNext();
			
			if (token.string().equals( "/" )) {
				ts.getNext(); // consume name -- found END </tag>
				ts.getNext(); // consume  ">" -- found END </tag>
				// return an empty tag... (prefix already done!)

			} else {
				n = token.string();
				a = new Attributes( ts );
				
				token = ts.getNext();
				if (token.string().equals( "/" )) // consumed ">" ?
					ts.getNext(); // no, consume ">" -- found STANDALONE <tag/>
				
				else  // do children....
					c = new Tags( ts );
		}	}
		
		name = n;
		prefix = p;
		attributes = a;
		children = c;
	}
	// ************************************************************************
	// ************************************************************************
	
	public boolean isEmpty() {return name.equals("") && prefix().isEmpty();}
	
	public Attribute matchedAttr( String val ) {
		return new Attribute(
				name,
				Attribute.getValue( // prevents X="x='val'"
					name.equals("unit") ? Plural.singular( val ) : val
				));
	}
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
	private static Indentation indent = new Indentation( "  " );
	public String toXml() { return toXml( indent );}
	public String toXml( Indentation indent ) {
		int sz = children.size();
		String contentSep = sz > 0? ("\n" + indent.toString()) : sz == 1 ? " " : "";
		indent.incr();
		String attrSep = sz > 0  ? "\n  "+indent.toString() : " " ;
		String s = prefix()
				+ (name.equals( "" ) ? "" :
					("<"+ name
							+ attributes.toString( attrSep )
							+ (0 == content().size() ? 
									"/>" :
									( ">"
									+ children.toXml( indent )
									+ contentSep
									+ "</"+ name +">"
				  )	)		  )		);
		indent.decr();
		return s;
	}
	public String toString() {
		return prefix().toString() + (name.equals( "" ) ? "" :
			"<"+ name +" "+ attributes.toString()+  // attrs doesn't have preceding space
			(content().isEmpty() ?
					"/>" : ( ">"+ children.toString() + "</"+ name +">" )));
	}
	public String toText() {
		return prefix().toString()
			+ (prefix().toString()==null||prefix().toString().equals("") ? "":" ")
			+ (name.equals( "" ) ? "" :
				( name.toUpperCase( Locale.getDefault() ) +" "+
					(content().isEmpty() ? "" : children.toText() )));
	}
	public String toLine() {
		return prefix().toString()
				+ (content().isEmpty() ? "" : children.toText() );
	}
	
	// ************************************************************************
	// ************************************************************************
	public void accumulateByName( String nm, Tags ts ) {
		if (name.equals( nm ))
			ts.add( this ); // found
		
		for (Tag t : content())
			t.accumulateByName( nm, ts );
	}
	public Tags findAllByName( String nm ) {
		Tags rc = new Tags();
		this.accumulateByName( nm, rc );
		return rc;
	}
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
	// ************************************************************************
	public static Strings interpret( Strings args ) {
		audit.in( "interpret", "args="+ args );
		Strings rc = new Strings( "sorry" ); // Shell.Fail;
		String cmd = args.remove( 0 );
		
		if (cmd.equals( "test" ))
			rc = new Strings( "tag test success" );
		
		else if (cmd.equals( "filter" )) {
			
			if (!args.isEmpty()) {
				
				Strings testStrings = new Strings();
				String tagName = args.remove( 0 ); // remove "file" or "span"
				if (tagName.equals( "file" ) && !args.isEmpty()) {
					
					String testFileName = Strings.trim( args.remove( 0 ), '"' );
					testStrings = new Strings( Fs.stringFromFile( testFileName ) );
					if (!args.isEmpty())
						tagName = args.remove( 0 ); // remove "file"
				}

				if (!args.isEmpty()) {
					String attrName = args.remove( 0 );
				
					if (!args.isEmpty()) {
						String attrValue = args.remove( 0 );
					
						rc      = new Strings();
						Tag t   = new Tag( args.isEmpty() ? testStrings.listIterator() : args.listIterator() );
						Tags ts = t.findAllByName( tagName );
						if (!ts.isEmpty())
							for (Tag tag : ts) 
								for (Tag child : tag.content()) 
									for (Attribute at : child.attributes())
										if (attrName.equals(  at.name()  ) &&
											attrValue.equals( at.value() )    )
											rc.add( child.content().toString() );
						if (rc.isEmpty())
							rc = new Strings( "sorry" );
					}
				}
			}
		}
		audit.out( rc );
		return rc;
	}
	// ************************************************************************
	public static void main( String argv[]) {
		String s = //new Strings(
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
			;
		//);
		
		TokenStream ts = new TokenStream(s.getBytes());
		Tag tag = new Tag( ts );
		Audit.log( "tag:"+ tag.toXml());
		Tags tags = tag.findAllByName("concept");
		for (Tag child : tags) {
			Audit.log( "Tag:" + child.name());
			Audit.incr();
			for (Attribute at : child.attributes())
				Audit.log(at.name() +"<==>"+ at.value());

			Audit.decr();
		}
}	}
