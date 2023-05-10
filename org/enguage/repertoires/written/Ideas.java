package org.enguage.repertoires.written;

import org.enguage.repertoires.Repertoires;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Ideas {
	private static final Audit audit = new Audit( "Ideas" );
	public  static final int      ID = 4186513; // Strings.hash( "Ideas" )

	public static Strings interpret( Strings cmds ) {
		
		Strings rc = Response.success();
		String cmd = cmds.remove( 0 );
		
		if (cmd.equals( "saveAs" )) {
			String name = cmds.toString( Strings.UNDERSC );
			audit.debug( "Saving concepts as "+ name );
			Concept.add( name );
			rc = Repertoires.signs().saveAs(
								Repertoires.USER_DEFINED,
								name
				 ) ? Response.success() : Response.failure();

		} else if (cmd.equals( "delete" )) {
			String concept = cmds.toString( Strings.UNDERSC );
			audit.debug( "Deleting "+ concept +" concept");
			Concept.remove( concept );
			Load.delete( concept );
			Repertoires.signs().remove( concept );
			
		} else if (cmd.equals( "load" )) {
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			for (String file : cmds)
				Load.load( file );
			 
		} else if (cmd.equals( "unload" )) {
			for (String file : cmds)
				Autoload.unloadNamed( file );
		/*
		 *else if (cmd.equals( "reload" )) 
		 *	Strings files = cmds.copyAfter( 0 )
		 *	for(int i=0; i<files.size(); i++) Concept.unload( files.get( i ))
		 *	for(int i=0; i<files.size(); i++) Concept.load( files.get( i ))
		 */
		} else {
			rc = Response.failure();
		} 		
		return rc;
}	}
