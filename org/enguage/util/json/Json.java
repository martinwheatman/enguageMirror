package org.enguage.util.json;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.enguage.util.audit.Audit;
import org.enguage.util.token.Token;
import org.enguage.util.token.TokenStream;

public class Json {
	
	private static final String NAME = "Json";
	private static final Audit audit = new Audit( NAME );
	
	public Json() {}
	public Json( String json ){
		try {
			load( json.getBytes( Token.byteEncoding ));
		} catch( UnsupportedEncodingException x ) {
			// object will be 'empty' only if UTF-8 is deprecated!
	}	}
	
	private static enum Type {String,Number,Object,Array,True,False,Null};
	
	class Value {
		public Value( String s ) {
			values.add( s );
			char t = s.charAt( 0 );
			switch (t) {
			case '"' : type = Type.String; break; 
			case '[' : type = Type.Array ; break; 
			case '{' : type = Type.Object; break; 
			case 't' : type = Type.True  ; break; 
			case 'f' : type = Type.False ; break; 
			case '-' : type = Type.Number; break; 
			default : 
				type = Character.isDigit( t ) 
						? Type.Number : Type.Null;
		}	}
		
		private final Type   type;
		public  final Type   type() {return type;}
		ArrayList<String> values = new ArrayList<String>();;
		Json object;
		
		public String toString() {
			switch (type) {
			case String : return ""+values.get( 0 );
			case Array  : return ""+values;
			case Object : return object.toString();
			case True   : return "true";
			case False  : return "false";
			default : return "null";
	}	}	}

	class NameValue {
		public NameValue( String n, Value v) {
			name = n; value = v;
		}
		private final String name;
		public  final String name() {return name;}
		
		private final Value value;
		public  final Value value() {return value;}
		
		public String toString() {return name +":"+ value;}
		public String toString(int n) {
			return (n==0?"":", ") + toString();
	}	}
	
	class Values extends ArrayList<NameValue> {
		static final long serialVersionUID = 0;
		public  boolean append( NameValue v ) {
			if (v == null) return false;
			audit.debug( "Adding: "+ v );
			values.add( v );
			return true;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int n = 0;
			for (NameValue v : values)
				sb.append( v.toString( n++ ));
			return sb.toString();
	}	}
	
	// ---
	private Values  values = new Values();
	
	// --- Location
	private int  row = 1;
	public  int  row() {return row;}
	
	private int  col = 1;
	public  int  col() {return col;}
	
	// -- LOAD...
	private String getName( TokenStream ts ) {
		return ts.expectDqString() ? ts.getString() : null;
	}
	private Value getValue( TokenStream ts ) {
		Value rc = null;
		if (ts.hasNext())
				rc = new Value( ts.getString());
		return rc;

	}
	private NameValue getNameValue( TokenStream ts ) {
		audit.in( "getNameValPair", "<value> [{ ',' <value> } | '}' ]" );
		String name;
		Value value;
		NameValue v = null != (name = getName( ts ))
					&& ts.expectLiteral( ":" )
					&& null != (value = getValue( ts ))
						? new NameValue( name, value )
						: null;
		audit.out( v );
		return v;
	}
	private boolean doObject( TokenStream ts ){
		audit.in("doObject", "'{' [ \"<str>\" ':' <value> | ',' ] '}'" );
		boolean rc = ts.expectLiteral( "{" );
		if (!ts.doLiteral( "}" )) { // starts '}' & we're done!
			while (values.append( getNameValue( ts ))
					&& ts.doLiteral( "," ));
			rc = ts.expectLiteral( "}" );
		}
		return audit.out( rc );
	}
	private void load( byte[] s ) {
		TokenStream ts = new TokenStream(s);
		if (!doObject(ts))
			audit.error( "Load failed." );
		else if (ts.hasNext())
			audit.error( "Extra data "+ ts.getString() +" found at end of JSON object." );
	}
	// --- LOAD.
	
//	public static Json json = new Json();
//	public static Strings interpret( Strings args) {
//		if (!args.isEmpty()) {
//			String cmd = args.remove(0);
//			if (cmd.equals("add")) {
//				if (!args.isEmpty()) {
//					String name = args.remove(0);
//					if (!args.isEmpty()) {
//						String value = args.remove(0);
//						json.values( json.new Value( name, value ));
//						Audit.log( "JSON is "+ json +"\n");
//					}
//				}
//			}
//		}
//		return new Strings( "ok" );
//	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "{" );
		sb.append( values.toString() );
		sb.append( "}" );
		return sb.toString();
	}
	
	// test code

	private static void test( String s) {
		Audit.title( s );
		Json j = new Json( s );
		Audit.log( "JSON is "+ j );
	}
	public static void main( String[] args ){
//		Audit.on();
		test( "{}" ); // ok!
		test( "X}" );
		test( "{X" );
		test( "{}X" );
		test( "{\n"
			+ "  \"martin\" : \"john\",\n"
			+ "  \"john\"   x \"wheatman\"\n"
			+ "}"
		);
		test( "{\n"
			+ "  \"martin\" : \"john\",\n"
			+ "  \"john\"   : \"wheatman\"\n"
			+ "}"
		);
//		interpret( new Strings( "add fred blogs" ));
}	}
