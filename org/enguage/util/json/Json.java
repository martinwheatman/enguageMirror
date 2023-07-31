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
	public Json( String j ){
		try {
			load( j.getBytes( Token.byteEncoding ));
		} catch( UnsupportedEncodingException x ) {
			// object will be 'empty' only if UTF-8 is deprecated!
	}	}
	
	class Value {
		private final String name;
		public  final String name() {return name;}
		
		private final String value;
		public  final String value() {return value;}
		
		public Value( String n, String v) {
			name = n; value = v;
		}
		public String toString() {return name +":"+ value;}
		public String toString(int n) {
			return (n==0?"":", ") + toString();
	}	}
	
	class Values extends ArrayList<Value> {
		static final long serialVersionUID = 0;
		public  boolean append( Value v ) {
			if (v == null) return false;
			audit.debug( "Adding: "+ v );
			values.add( v );
			return true;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int n = 0;
			for (Value v : values)
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
	private String doName( TokenStream ts ) {
		return ts.expectDqString() ? ts.getNext().string() : null;
	}
	private String doValue( TokenStream ts ) {
		return ts.hasNext() ? ts.getNext().string() : null;
	}
	private Value doKvPair( TokenStream ts ) {
		audit.in( "doKvPair", "<value> [{ ',' <value> } | '}' ]" );
		String name, value;
		Value v = null != (name = doName( ts ))
					&& ts.expectLiteral( ":" )
					&& null != (value = doValue( ts ))
						? new Value( name, value )
						: null;
		audit.out( v );
		return v;
	}
	private boolean doValues( TokenStream ts ) {
		audit.in( "doValues", "<value> [{ ',' <value> } | '}' ]" );
		boolean rc = true;
		if (!ts.parseLiteral( "}" )) { // starts '}' & we're done!
			while (values.append( doKvPair( ts ))
					&& ts.doLiteral( "," ));
			rc = ts.parseLiteral( "}" );
		}
		return audit.out( rc );
	}
	private boolean doObject( TokenStream ts ){
		audit.in("doObject", "{ <values> }" );
		boolean rc =   ts.expectLiteral( "{" )
					&& doValues( ts )
					&& ts.expectLiteral( "}" );
		return audit.out( rc );
	}
	private void load( byte[] s ) {
		TokenStream ts = new TokenStream(s);
		if (!doObject(ts))
			audit.error(
					"Load failed at: line="+ ts.row()
					+", col="+ ts.col()
			);
		else if (ts.hasNext())
			audit.error( "extra data found at end of JSON object" );
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
	private static void test( String s) {test(s,0,0);}

	private static void test( String s, int row, int col) {
		Audit.title( s );
		Json j = new Json( s );
		if (j.values.size() > 0 && row == 0 & col == 0)
			Audit.log( "JSON is "+ j );
		else
			audit.error( "error at "+ j.col() +"/"+ j.row() );
	}
	public static void main( String[] args ){
		test( "{}" ); // ok!
		test( "X}", 1, 1 );
		test( "{X", 1, 2 );
		test( "{}X", 1, 3 );
//		audit.debugging( true );
//		audit.tracing( true );
		test( "{\n"
			+ "  \"martin\" : \"john\",\n"
			+ "  \"john\"   x \"wheatman\"\n"
			+ "}"
		);
		//test( "{ martin : 'wheatman' }" );
		
//		audit.debugging( false );
		test( "{\n"
			+ "  \"martin\" : \"john\",\n"
			+ "  \"john\"   : \"wheatman\"\n"
			+ "}"
		);
//		test( "{\n\n \"X\"\n}\n", 3, 2 );
//		interpret( new Strings( "add fred blogs" ));
}	}
