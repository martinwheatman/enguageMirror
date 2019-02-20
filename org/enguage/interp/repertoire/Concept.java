package org.enguage.interp.repertoire;

/*import android.app.Activity;*/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.enguage.Enguage;
import org.enguage.interp.intention.Intention;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Ospace;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.vehicle.Plural;

public class Concept {
	static public final String LOADING = "CONCEPT";
	static public boolean load( String name ) {
		boolean wasLoaded   = false,
		        wasSilenced = false,
		        wasAloud    = Enguage.shell().isAloud();
		
		Variable.set( LOADING, name );
		
		// silence inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend();
			Enguage.shell().aloudIs( false );
		}
		
		Intention.concept( name );
		
		// ...add content from file/asset...
		try {
			String fname = "concepts"+ File.separator + name + ".txt";
			/*Activity a = (Activity) Enguage.e.context();*/
			InputStream is =
					/*a == null ?*/
							new FileInputStream( Ospace.location() + fname )
					/*      : a.getAssets().open( fname )*/;
			Enguage.shell().interpret( is );
			is.close();
			
			wasLoaded = true;
			
		} catch (IOException e1) {}
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			Enguage.shell().aloudIs( wasAloud );
		}
		
		Variable.unset( LOADING );
		return wasLoaded;
	}
	public static String convertContent( String s, String from, String to ) {
		Strings ss = new Strings();
		char[] buffer = s.toCharArray();
		int  i = 0, sz = buffer.length;
		while (i<sz) {
			StringBuilder ws = new StringBuilder( Strings.MAXWORD );
			while (i < sz && !('a' <= buffer[ i ] && 'z' >= buffer[ i ]))
				ws.append( buffer[ i++ ]);
			ss.append( ws.toString() );
			
			StringBuilder word = new StringBuilder( Strings.MAXWORD );
			while (i < sz && 'a' <= buffer[ i ] && 'z' >= buffer[ i ])
				word.append( buffer[ i++ ]);
			
			String str = word.toString();
			
			// convert str
			if (str.equals( from ))
				ss.add( to );
			else if (str.equals( Plural.plural( from )))
				ss.add( Plural.plural( to ));
			else if (str.equals( Plural.singular( from )))
				ss.add( Plural.singular( to ));
			else
				ss.add( str );
		}
		return ss.toString( Strings.CONCAT );
	}
	static private boolean attempt( String from, String to, String fromFn, String toFn ) {
		String in  = Ospace.location() + "concepts"+ File.separator + fromFn + ".txt",
			   out = Ospace.location() + "concepts"+ File.separator + toFn   + ".txt",
		       str = Fs.stringFromFile( in );
		return !str.equals( "" ) && Fs.stringToFile( out, convertContent( str, from, to ));
	}
	static public boolean copy( String from, String to ) {
		boolean rc = false;
		if (rc = attempt( from, to, from, to ))
			Concepts.name( to );                          // need to want
		else if (rc = attempt( from, to, from+"+"+Plural.plural( from ), to+"+"+Plural.plural( to ))) 
			Concepts.name( to+"+"+Plural.plural( to ));   // need+needs to want+wants
		else if (rc = attempt( from, to, Plural.plural( from )+"+"+from, Plural.plural( to )+"+"+to ))
			Concepts.name( Plural.plural( to )+"+"+to );  // needs+need to wants+wants
		return rc; 
}	}
