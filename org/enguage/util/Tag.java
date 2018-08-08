package org.enguage.util;

/*import android.app.Activity;
 *import android.content.res.AssetManager;
 *import java.io.FileInputStream;
 *import java.io.IOException;
 *import java.io.InputStream;
 */
import java.util.ArrayList;
import java.util.Locale;

import org.enguage.vehicle.Plural;

public class Tag {
	private static Audit audit = new Audit( "Tag" );
	
	public static final int NULL   = 0;
	public static final int ATOMIC = 1;
	public static final int START  = 2;
	public static final int END    = 3;
	
	public static final String emptyPrefix = "";

	private int  type = NULL;
	private int  type() { return type; }
	private void type( int t ) { if (t>=NULL && t<=END) type = t; }
	
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
	public  String     attribute( String name ) { return attrs.get( name ); }
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
				Attribute.expandValues( // prevents X="x='val'"
					name.equals("unit") ? Plural.singular( val ) : val
				).toString( Strings.SPACED ) );
	}

	public boolean isEmpty() { return name.equals("") && prefix().size() == 0; }

	Tags content = new Tags();
	public  Tags content() {return content;}
	public  Tag  content( Tags ta ) { content = ta; return this; }
	public  Tag  removeContent( int n ) { return content.remove( n ); }
	public  Tag  content( int n, Tag t ) { content.add( n, t ); return this; }
	public  Tag  content( Tag child ) {
		if (null != child && !child.isEmpty()) {
			type = START;
			content.add( child );
		}
		return this;
	}
	// --
	public boolean equals( Tag pattern ) {
		boolean rc;
		if (attributes().size() < pattern.attributes().size()) { // not exact!!!
			rc = false;
		} else if ((content().size()==0 && pattern.content().size()!=0) 
				|| (content().size()!=0 && pattern.content().size()==0)) {
			rc =  false;
		} else {
			rc =   Plural.singular(  prefix.toString()).equals( Plural.singular( pattern.prefix().toString()))
				&& Plural.singular( postfix()).equals( Plural.singular( pattern.postfix()))
				&& attributes().matches( pattern.attributes()) // not exact!!!
				&& content().equals( pattern.content());
		}
		return rc;
	}
	public boolean matches( Tag pattern ) {
		//if (audit.tracing) audit.in( "matches", "this="+ toXml() +", pattern="+ pattern.toXml() );
		boolean rc;
		if (attributes().size() < pattern.attributes().size()) {
			//audit.debug( "Too many attrs -> false" );
			rc = false;
		} else if ((content().size()==0 && pattern.content().size()!=0) ){
			//	|| (content().size()!=0 && pattern.content().size()==0)) {
			//audit.audit("one content empty -> false");
			rc =  false;
		} else {
			//audit.debug( "Checking also attrs:"+ attributes().toString() +":with:"+ pattern.attributes().toString() );
			rc =   Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
				&& postfix().equals( pattern.postfix())
				&& attributes().matches( pattern.attributes())
				&&    content().matches( pattern.content());
			//audit.debug("full check returns: "+ rc );
		}
		//if (audit.tracing) audit.out( rc );
		return rc;
	}
	public boolean matchesContent( Tag pattern ) {
		boolean rc;
		if (   (content().size()==0 && pattern.content().size()!=0) 
			|| (content().size()!=0 && pattern.content().size()==0)) {
			rc =  false;
		} else {
			rc =   Plural.singular( prefix().toString()).contains( Plural.singular( pattern.prefix().toString()))
					&& postfix().equals( pattern.postfix())
					&& content().matches( pattern.content());
		}
		return rc;
	}

	// -- tag from string ctor
	private Tag doPreamble() {
		int i = 0;
		String preamble = "";
		while (i < postfix().length() && '<' != postfix().charAt( i )) 
			preamble += postfix().charAt( i++ );
		prefix( new Strings( preamble ));
		if (i < postfix().length()) {
			i++; // read over terminator
			postfix( postfix().substring( i )); // ...save rest for later!
		} else
			postfix( "" );
		return this;
	}
	private Tag doName() {
		int i = 0;
		while(i < postfix().length() && Character.isWhitespace( postfix().charAt( i ))) i++; //read over space following '<'
		if (i < postfix().length() && '/' == postfix().charAt( i )) {
			i++;
			type( END );
		}
		if (i < postfix().length()) {
			String name = "";
			while (i < postfix().length() && Character.isLetterOrDigit( postfix().charAt( i ))) name += postfix().charAt( i++ );
			name( name );
			
			if (i < postfix().length()) postfix( postfix().substring( i )); // ...save rest for later!
		} else
			name( "" ).postfix( "" );
		return this;
	}
	private Tag doAttrs() {
		attributes( new Attributes( postfix() ));
		int i = attributes().nchars();
		if (i < postfix().length()) {
			type( '/' == postfix().charAt( i ) ? ATOMIC : START );
			while (i < postfix().length() && '>' != postfix().charAt( i )) i++; // read to tag end
			if (i < postfix().length()) i++; // should be at '>' -- read over it
		}
		postfix( postfix().substring( i )); // save rest for later
		return this;
	}
	private Tag doChildren() {
		if ( null != postfix() && !postfix().equals("") ) {
			Tag child = new Tag( postfix());
			while( NULL != child.type()) {
				// move child remained to tag...
				postfix( child.postfix());
				// ...add child, sans remainder, to tag content
				content( child.postfix( "" ));
				child = new Tag( postfix());
			}
			postfix( child.postfix() ).content( child.postfix( "" ));
		}
		return this;
	}
	private Tag doEnd() {
		name( "" ).type( NULL );
		int i = 0;
		while (i < postfix().length() && '>' != postfix().charAt( i )) i++; // read over tag end
		postfix( i == postfix().length() ? "" : postfix().substring( ++i ));
		return this;
	}
	// -- constructors...
	public Tag() {}
	public Tag( String cpp ) { // tagFromString()
		this();
		postfix( cpp );
		doPreamble().doName();
		if (type() == END) 
			doEnd();
		else {
			doAttrs();
			if (type() == START) doChildren();
	}	}
	// -- tag from string: DONE
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

	private static Indent indent = new Indent( "  " );
	public String toXml() { return toXml( indent );}
	public String toXml( Indent indent ) {
		int sz = content.size();
		String contentSep = sz > 0? ("\n" + indent.toString()) : sz == 1 ? " " : "";
		indent.incr();
		String    attrSep = sz > 0  ? "\n  "+indent.toString() : " " ;
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
	/*
	 * public static Tag fromAsset( String fname, Activity ctx ) {
	 * 	audit.in( "fromAsset", fname );
	 * 	Tag t = null;
	 * 	AssetManager am = ctx.getAssets();
	 * 	try {
	 * 		InputStream is = am.open( fname );
	 * 		t = new Tag( Fs.stringFromStream( is ));
	 * 		is.close();
	 * 	} catch (IOException e) {
	 * 		audit.ERROR( "no tag found in asset "+ fname );
	 * 	}
	 * 	audit.out();
	 * 	return t;
	 *}
	*/
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
		audit.tracing = true;
		Tag orig = new Tag("prefix ", "util", "posstfix").append("sofa", "show").append("attr","one");
		orig.content( new Tag().prefix( new Strings( "show" )).name("sub"));
		orig.content( new Tag().prefix( new Strings( "fred") ));
		Tag t = new Tag( "<xml>\n"+
			" <config \n"+
			"   CLASSPATH=\"/home/martin/ws/Enguage/bin\"\n"+
			"   DNU=\"I do not understand\" >\n" +	
            "   <concepts>\n"+
			"     <concept id=\"colloquia\"      op=\"load\"/>\n"+
			"     <concept id=\"needs\"          op=\"load\"/>\n"+
			"     <concept id=\"engine\"         op=\"load\"/>\n"+
			"   </concepts>\n"+
	        "   </config>\n"+
	        "   </xml>"        );
		audit.log( "tag:"+ t.toXml());
}	}
