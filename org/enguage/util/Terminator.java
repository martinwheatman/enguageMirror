package org.enguage.util;

public class Terminator {
	
	private Terminator() {}
	
	private static Strings terminators = new Strings( ". ? !" );
	public  static void    terminators( Strings a ){ terminators = a; }
	public  static Strings terminators() { return terminators; }
	
	public  static boolean isTerminator(   String s ) {
		return terminators().contains( s );
	}
	private static boolean isTerminated(   Strings a ) {
		boolean rc = false;
		if (null != a) {
			int last = a.size() - 1;
			if (last > -1) rc = isTerminator( a.get( last ));
		}
		return rc;
	}
	public static Strings stripTerminator( Strings a ) {
		
		// ["what", "do", "i", "need."] => [ ... , "need", "."]
		a = a.normalise();
		
		if (isTerminated( a ))
			a.remove( a.size() - 1 );
		return a;
	}
	public static String stripTerminator( String s ) {
		Strings a = new Strings( s );
		a = stripTerminator( a );
		return a.toString();
	}
	public static Strings addTerminator( Strings a, String terminator ) {
		a.add( terminators.contains( terminator ) ? terminator : terminators.get( 0 ));
		return a;
	}
	public static Strings addTerminator( Strings a ) {
		return addTerminator( a, terminators.get( 0 ));
}	}
