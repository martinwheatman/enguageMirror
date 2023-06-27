package org.enguage.util.tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Pattern;

import org.enguage.sign.symbol.config.Plural;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.audit.Indentation;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.token.Token;
import org.enguage.util.token.TokenStream;

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
	public         final Tags       children() {return children;}
	
	// -- constructors...
	public Tag( Tag orig ) {
		name = orig.name();
		prefix = orig.prefix();
		children = orig.children(); // no Tags copy constructor
		attributes = new Attributes( orig.attributes());
	}
	public Tag( ListIterator<String> si ) {
		Strings pref = Strings.copyUntil( si, "<" );
		String    nm = "";
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
		String     n = "";
		String     p = doPrefix( ts );
		Attributes a = new Attributes();
		Tags       c = new Tags();
		
		if (ts.hasNext()) {
			Token nameToken = ts.getNext();
			
			if (nameToken.string().equals( "/" )) {
				ts.getNext(); // consume name -- found END </tag>
				ts.getNext(); // consume  ">" -- found END </tag>
				// return an empty tag... (prefix already done!)

			} else {
				
				// consume comments - anomaly in syntax - read over comment?
				if (nameToken.string().equals( "!" )) {
					nameToken = ts.getNext(); // DOCTYPE or first '-'
					if (nameToken.string().equals( "DOCTYPE" )) {
						ts.type( ts.getNext().string()); // consume type -- e.g. 'html'
						ts.getNext();                    // consume ">"
						
					} else { // read over <!-- comment -->
						ts.getNext(); // second '-'
						while (true) {
							if (ts.getNext().string().equals( "-" ) &&
								ts.getNext().string().equals( "-" ) &&
								ts.getNext().string().equals( ">" ))
								break;
					}	}
					p = doPrefix( ts );
					nameToken = ts.getNext();
				}
				// consume comments
					
				n = nameToken.string();
				a = new Attributes( ts );
				
				nameToken = ts.getNext();
				if (nameToken.string().equals( "/" )) // consumed ">" ?
					ts.getNext(); // no, consume ">" -- found STANDALONE <tag/>
				
				else if (ts.type().equals( "html" ) && n.equals( "meta" ))
					; // anomaly in syntax - HTML meta tags have no children AND no "/>" ending!
				
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
			&& children().equals( pattern.children());
	}
	public boolean matches( Tag pattern ) {
		return Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
			&& attributes().matches( pattern.attributes())
			&&    children().matches( pattern.children());
	}
	public boolean matchesContent( Tag pattern ) {
		return Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
				&& children().matches( pattern.children());
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
							+ (0 == children().size() ? 
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
			(children().isEmpty() ?
					"/>" : ( ">"+ children.toString() + "</"+ name +">" )));
	}
	public String toText() {
		return prefix().toString()
			+ (prefix().toString()==null||prefix().toString().equals("") ? "":" ")
			+ (name.equals( "" ) ? "" :
				( name.toUpperCase( Locale.getDefault() ) +" "+
					(children().isEmpty() ? "" : children.toText() )));
	}
	public String toLine() {
		return prefix().toString()
				+ (children().isEmpty() ? "" : children.toText() );
	}
	
	// ************************************************************************
	// ************************************************************************
	public void accumulateByName( String nm, Tags ts ) {
		if (name.equals( nm ))
			ts.add( this ); // found
		
		for (Tag t : children())
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
			ArrayList<Tag> ta = children();
			for (int i=0; i<ta.size() && null == rc; ++i) // find first child
				rc = ta.get( i ).findByName( nm );
		}
		return rc;
	}
	
	// ************************************************************************
	// ************************************************************************
	private static final Strings months = new Strings(
			"January  February March "
			+"April   May      June "
			+"July    August   September "
			+"October November December"
	);
	private static Pattern pattern = Pattern.compile("\\d+");

	private static boolean isDate( String str ) {
		Strings sa = new Strings( str );
		return (sa.size() > 1 &&
				(months.contains( sa.get( 1 )) || // 26 April 1923
				 pattern.matcher( sa.get( sa.size()-1 )).matches())); // c. 878-879
	}
	private static String getDate( Tag tag ) {
		if (isDate( tag.prefix().trim())) return tag.prefix().trim(); 
		if (!tag.children().isEmpty())
			for (Tag child : tag.children())
				if (isDate( child.prefix().trim())) return child.prefix().trim();
		return "Unknown";
	}
	
	public static Strings interpret( Strings args ) {
		audit.in( "interpret", "args="+ args );
		Strings rc = new Strings( "sorry" ); // Shell.Fail;
		String cmd = args.remove( 0 );
		String name = args.remove( 0 );
		
		if (cmd.equals( "filter" ) && !args.isEmpty()) {
				
			String fName = args.toString( Strings.CONCAT );
			try (TokenStream ts = new TokenStream( new File( fName ))) {
				Tag  doc = new Tag( ts );
				Tags tags = doc.findAllByName( "tr" );
				for (Tag t : tags) { // process each row
					
					if (!t.children().isEmpty()) {
						ListIterator<Tag> ri = t.children().listIterator();
						if (ri.hasNext()) {
							Tag cell = ri.next();
							
							if (cell.name().equals( "th" )
								&& !cell.children().isEmpty()
								&& cell.children().get(0)
										.prefix().trim().equalsIgnoreCase( name ))
							{
								cell = ri.next();
								if (cell.name().equals( "td" ))
									rc = new Strings( getDate( cell ));
							}
				}	}	}
				
			} catch (FileNotFoundException fnf) {
				Audit.log("interpret().filter: File not found: " + fName);
			}
		} else 
			audit.error( "usage: filter ..." );

		audit.out( rc );
		return rc;
	}
	
	// ************************************************************************
	// Test code...
	//
	private static void treePrint( Tag t ) {
		ListIterator<Tag> ch = t.children().listIterator();
		if ( !t.name().equals( "" )) {
			Audit.log( t.name() +" : "+ (t.children().isEmpty() ? "":t.children().get(0).prefix().trim() ));
			
			Audit.incr();
			while (ch.hasNext()) 
				treePrint( ch.next());
			
			Audit.decr();
	}	}
	public static void main( String argv[]) {
		if (argv.length > 0) {
			treePrint(
				new Tag(
					new TokenStream(
						Fs.stringFromFile( argv[ 0 ]).getBytes()
			)	)	);
		} else {
			audit.debugging( true );
			audit.tracing( true );
			String[] command = {"filter", "born", "selftest/wiki/Queen_Elizabeth_The_Second"};
			Audit.log( interpret( new Strings( command )));
		}
}	}
