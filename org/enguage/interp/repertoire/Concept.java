package org.enguage.interp.repertoire;

import java.io.File;

import org.enguage.util.Audit;

public class Concept {
	static public final String    NAME = "concept";
	static private       Audit   audit = new Audit( NAME );
	
	static public void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( name( cname )),
			     newFile = new File( name( cname, "del" ));
			if (!oldFile.renameTo( newFile ))
				audit.ERROR( "renaming "+ oldFile +" to "+ newFile );
	}	}
	static public String name( String name, String ext ) {
		// would just return "concepts"/name.ext
		String fname = Concepts.LOCATION + name +"."+ ext;
		if (new File( fname ).exists())
			return fname; // found, return existing
		else {
			String[] names = new File( Concepts.LOCATION ).list();
			if (names != null) for ( String dir : names ) { // e.g. name="hello.txt"
				String[] components = dir.split( "\\." );
				if (components.length == 1 && new File( Concepts.LOCATION + components[ 0 ]+"/"+name+"."+ ext ).exists())
					return Concepts.LOCATION + components[ 0 ]+"/"+name+"."+ ext; // found, return existing component
		}	}
		return fname; // not found, new filename
	}	
	
	static public String name( String name ) {return name( name, "txt" );}
}
