package com.yagadi.enguage.interpretant;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.object.Attribute;
import com.yagadi.enguage.object.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Fs;
import com.yagadi.enguage.util.Indent;
import com.yagadi.enguage.util.Number;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.vehicle.Language;
import com.yagadi.enguage.vehicle.Plural;

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

	public static final String quoted = "quoted";
	public static final String quotedPrefix = quoted.toUpperCase( Locale.ENGLISH ) + "-";
	public static final String plural = Plural.NAME; // "plural";
	public static final String pluralPrefix = plural.toUpperCase(Locale.ENGLISH) + "-";
	public static final String numeric = "numeric";
	public static final String numericPrefix = numeric.toUpperCase( Locale.ENGLISH ) + "-";
	public static final String singular = "singular";
	public static final String singularPrefix = singular.toUpperCase( Locale.ENGLISH ) + "-";
	public static final String phrase = "phrase";
	public static final String abstr  = "abstract";
	
	
	public Strings prefixAsStrings = new Strings();
	public Strings prefixAsStrings() { return prefixAsStrings; }
	public String  prefix = emptyPrefix;
	public String  prefix(  ) { return prefix; }
	public Tag     prefix( String str ) {
		// set this shortcut..
		prefixAsStrings = new Strings( str );
		
		// replace any leading whitespace with a single space
		boolean whitespaceNeeded = str.length() > 0
				&& Character.isWhitespace( str.charAt( 0 ));
		if (whitespaceNeeded)
			while ( str.length() > 0 && Character.isWhitespace( str.charAt( 0 ))) 
				str = str.substring( 1 );
		
		prefix = (whitespaceNeeded ? " " : "") + str;
		return this;
	}

	public Strings postfixAsStrings;
	public Strings postfixAsStrings() { return new Strings( postfix ); }
	public String  postfix = "";
	public String  postfix(  ) { return postfix; }
	public Tag     postfix( String str ) { postfix = str; return this; }

	private String name = "";
	public  String name() { return name; }
	public  Tag    name( String nm ) { if (null != nm) name = nm; return this; }

	private boolean isNumeric = false;
	public  boolean isNumeric() { return isNumeric; }
	public  Tag     numericIs( boolean nm ) { isNumeric = nm; return this; }

	private boolean isQuoted = false;
	public  boolean quoted() { return isQuoted;}
	
	private boolean isPlural = false;
	public  boolean pluraled() { return isPlural; }
	
	private boolean phrased = false;
	public  boolean phrased() { return phrased; }
	
	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Tag        attributes( Attribute a ) {
		if (a.name().equals( phrase ))
			phrased = true;
		else if (a.name().equals( plural ))
			isPlural = true;
		else if (a.name().equals( quoted ))
			isQuoted = true;
		else if (a.name().equals( numeric ))
			numericIs( true );
		else
			attrs.add( a );
		return this;
	}
	public  Tag        attributes( Attributes as ) {
		for( Attribute a : as)
			attributes( a ); // add each individually
		// Propagate num chars read on creation...
		attrs.nchars( as.nchars());
		return this;
	}
	public  String     attribute( String name ) { return attrs.get( name ); }
	public  Tag        attribute( String name, String value ) {attributes( new Attribute( name, value )); return this; }
	// ordering of attributes relevant to Autopoiesis
	public  Tag        append( String name, String value ) { attributes( new Attribute( name, value )); return this; }
	public  Tag        prepend( String name, String value ) {
		if (name.equals( phrase ))
			phrased = true;
		else
			attrs.add( 0, new Attribute( name, value ));
		return this;
	} // 0 == add at 0th index!
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

	
	public boolean isNull() { return name.equals("") && prefix.equals( emptyPrefix ); }

	public boolean invalid( ListIterator<String> ui  ) {
		boolean rc = false;
		if (ui.hasNext()) {
			String candidate = ui.next();
			rc = (  quoted() && !Language.isQuoted( candidate ))
			  || (pluraled() && !Plural.isPlural(   candidate ))
			  // Useful for preventing class "class" being created...
			  //|| (attribute( Tag.abstr ).equals( Tag.abstr ) && // Archaic? Refactor anyway!
				//		candidate.equalsIgnoreCase( name()    )   )
						;
			if (ui.hasPrevious()) ui.previous();
		}
		return rc;
	}
	
	Tags content = new Tags();
	public  Tags content() {return content;}
	public  Tag  content( Tags ta ) { content = ta; return this; }
	public  Tag  removeContent( int n ) { return content.remove( n ); }
	public  Tag  content( int n, Tag t ) { content.add( n, t ); return this; }
	public  Tag  content( Tag child ) {
		if (null != child && !child.isNull()) {
			type = START;
			content.add( child );
		}
		return this;
	}
	// --
	public Tag update( Attributes as ) {
		/*
		 * this contains some item specific code :(
		 */
		audit.in( "update", as.toString() );
		for (Attribute a : as) {
			String value = a.value(),
					name = a.name();
			if (name.equals( "quantity" )) {
				/*
				 * should getNumber() and combine number from value.
				 */
				Strings vs = new Strings( value );
				if (vs.size() == 2) {
					/*
					 * combine magnitude - replace if both absolute, add if relative etc.
					 * Should be in Number? Should deal with "1 more" + "2 more" = "3 more"
					 */
					String firstVal = vs.get( 0 ), secondVal = vs.get( 1 );
					if (secondVal.equals( Number.MORE ) || secondVal.equals( Number.FEWER )) {
						int oldInt = 0, newInt = 0;
						try {
							oldInt = Integer.valueOf( attribute( name ));
						} catch (Exception e) {} // fail silently, oldInt = 0
						//audit.debug( "oldInt is "+ oldInt );
						try {
							newInt = Integer.valueOf( firstVal );
							//audit.debug( "newInt is "+ newInt );
							/*
							 * What the ...?
							 */
							value = Integer.toString( oldInt + (secondVal.equals( Number.MORE ) ? newInt : -newInt));
							//audit.debug( "value  is "+ value );
							
						} catch (Exception e) {} // fail silently, newInt = 0;
				}	}
			//} else if (name.equals( "unit" )) {
			}
			replace( a.name(), value );
		}
		audit.out();
		return this;
	}
	public int remove( Tag pattern ) {
		//audit.traceIn( "remove", pattern.toString());
		int rc = 0;
		Iterator<Tag> i = content().iterator();
		while (i.hasNext()) {
			Tag t = i.next();
			audit.debug( "checking against: "+ t.toString() );
			if (t.equals( pattern )) {
				i.remove();
				rc++;
		}	}
		//audit.traceut( rc );
		return rc;
	}
	public int removeMatches( Tag pattern ) {
		int rc = 0;
		Iterator<Tag> i = content().iterator();
		while (i.hasNext()) {
			Tag t = i.next();
			if (t.matches( pattern )) {
				i.remove();
				rc++;
		}	}
		return rc;
	}
	public boolean equals( Tag pattern ) {
		//if (audit.tracing) audit.in( "equals", "this="+ toXml() +", with="+ pattern.toXml());
		boolean rc;
		if (attributes().size() < pattern.attributes().size()) { // not exact!!!
			//audit.audit( " T:Size issue:"+ attributes().size() +":with:"+ pattern.attributes().size() +":");
			rc = false;
		} else if ((content().size()==0 && pattern.content().size()!=0) 
				|| (content().size()!=0 && pattern.content().size()==0)) {
			//audit.audit(" T:one content empty -> false");
			rc =  false;
		} else {
			//audit.audit( " T:Checking also attrs:"+ attributes().toString() +":with:"+ pattern.attributes().toString() +":" );
			//audit.audit( " T:Checking also prefx:"+ prefix() +":with:"+ pattern.prefix() +":" );
			rc =   Plural.singular(  prefix()).equals( Plural.singular( pattern.prefix()))
				&& Plural.singular( postfix()).equals( Plural.singular( pattern.postfix()))
				&& attributes().matches( pattern.attributes()) // not exact!!!
				&& content().equals( pattern.content());
			//audit.audit( " T:full check returns: "+ rc );
		}
		//if (audit.tracing) audit.out( rc );
		return rc;
	}
	/* <fred attr1="a" attr2="b">fred<content/> bill<content/></fred>.(
	//		<fred attr2="b"/> -> true
	// )
	 * 
	 */
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
			rc =   Plural.singular( prefix()).contains( Plural.singular( pattern.prefix()))
				&& postfix().equals( pattern.postfix())
				&& attributes().matches( pattern.attributes())
				&&    content().matches( pattern.content());
			//audit.debug("full check returns: "+ rc );
		}
		//if (audit.tracing) audit.out( rc );
		return rc;
	}
	public boolean matchesContent( Tag pattern ) {
		//audit.traceIn( "matches", "this="+ toString() +", pattern="+ pattern.toString() );
		boolean rc;
		if (   (content().size()==0 && pattern.content().size()!=0) 
			|| (content().size()!=0 && pattern.content().size()==0)) {
			//audit.audit("one content empty -> false");
			rc =  false;
		} else {
			//audit.debug( "Checking also attrs:"+ attributes().toString() +":with:"+ pattern.attributes().toString() );
			rc =   Plural.singular( prefix()).contains( Plural.singular( pattern.prefix()))
					&& postfix().equals( pattern.postfix())
					&& content().matches( pattern.content());
			//audit.debug("full check returns: "+ rc );
		}
		//audit.traceut( rc );
		return rc;
	}

	// -- tag from string ctor
	private Tag doPreamble() {
		int i = 0;
		String preamble = emptyPrefix;
		while (i < postfix().length() && '<' != postfix().charAt( i )) 
			preamble += postfix().charAt( i++ );
		prefix( preamble );
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
		//audit.log( "read "+ i +"chars in "+ attributes().toString() );
		//if (i>0) i++;
		if (i < postfix().length()) {
			//audit.log( "char at i="+ postfix().charAt( i ) );
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
		prefix( pre ).name( nm );
	}
	public Tag( String pre, String nm, String post ) {
		this( pre, nm );
		postfix( post );
	}
	public Tag( Tag orig ) {
		this( orig.prefix(), orig.name(), orig.postfix());
		attributes( new Attributes( orig.attributes()));
		content( orig.content());
	}

	private static Indent indent = new Indent( "  " );
	public String toXml() { return toXml( 0 );}
	public String toXml( int level ) {
		indent.incr();
		String s = prefix + (name.equals( "" ) ? "" :
			("<"+ name + attrs.toString()+ // attributes has preceding space
			(0 == content().size() ? "/>" : ( ">"+ content.toXml( level==0 ? 0 : level-1 ) + "</"+ name +">" ) )))
			+ postfix;
		indent.decr();
		return s;
	}
	public String toString() {
		return prefix + (name.equals( "" ) ? "" :
			("<"+ name + attrs.toString()+ // attributes has preceding space
			(0 == content().size() ? "/>" : ( ">"+ content.toString() + "</"+ name +">" ))))
			+ postfix;
	}
	public String toText() {
		return prefix
			+ (prefix==null||prefix.equals("") ? "":" ")
			+ (name.equals( "" ) ? "" :
				( name.toUpperCase( Locale.getDefault() ) +" "+  // attributes has preceding space
					(0 == content().size() ? "" : content.toText() )))
			+ postfix;
	}
	public String toLine() {
		return prefix
				+ (0 == content().size() ? "" : content.toText() )
				+ postfix ;
	}
	
	public static Tag fromFile( String fname ) {
		Tag t = null;
		try {
			t = new Tag( Fs.stringFromStream( new FileInputStream( fname )) );
		} catch( IOException e ) {
			audit.ERROR( "no tag found in file "+ fname );
		}
		return t;
	}
	/*
	public static Tag fromAsset( String fname, Activity ctx ) {
		//audit.traceIn( "fromAsset", fname );
		Tag t = null;
		AssetManager am = ctx.getAssets();
		try {
			InputStream is = am.open( fname );
			t = new Tag( Filesystem.stringFromStream( is ));
			is.close();
		} catch (IOException e) {
			audit.ERROR( "no tag found in asset "+ fname );
		}
		//audit.traceut();
		return t;
	}
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
		Strings a = new Strings( argv );
		int argc = argv.length;
		Tag orig = new Tag("prefix ", "util", "posstfix").append("sofa", "show").append("attr","one");
		orig.content( new Tag().prefix(" show ").name("sub"));
		orig.content( new Tag().prefix( " fred " ));
		Tag t = new Tag( orig );
		//audit.log( "orig was:"+ orig.toString());
		//audit.audit( "copy: "+ t.toString());
		//t = new Tag( orig.toString());
		//audit.audit( "copy2: "+ t.toString());
		//Tag pattern = new Tag("prefix <util attr='one'/>posstfix");
		//audit.audit( "patt: "+ pattern.toString());
		//audit.audit( "orig "+ (orig.matchesContent( pattern )?"DOES":"does NOT") +" (and should) match pattern" );
		
		//t = new Tag("<test id='123' quantity='5 * 3 more'/>");
		//orig.update( t.attributes() );
		//audit.log( "orig is now:"+ orig.toString());
		
		t = new Tag( "<xml>\n"+
			" <config \n"+
			"   CLASSPATH=\"/home/martin/ws/Enguage/bin\"\n"+
			"   DNU=\"I do not understand\" >\n" +	
            "   <concepts>\n"+
			"     <concept id=\"colloquia\"      op=\"load\"/>\n"+
			"     <concept id=\"engine\"         op=\"load\"/>\n"+
			"   </concepts>\n"+
	        "   </config>\n"+
	        "   </xml>"        );
		audit.log( "tag:"+ t.toXml());
		
		if (argc > 0) {
			audit.log( "Comparing "+ t.toString() +", with ["+ a.toString( Strings.DQCSV ) +"]");
			Attributes attr = t.content().matchValues( a );
			audit.log( (null == attr ? "NOT " : "("+ attr.toString() +")" ) + "Matched" );
}	}	}
