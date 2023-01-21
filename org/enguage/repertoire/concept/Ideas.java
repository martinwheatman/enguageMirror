package org.enguage.repertoire.concept;

import org.enguage.repertoire.Repertoire;
import org.enguage.signs.objects.Variable;
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
			Names.add( name );
			rc = Repertoire.signs.saveAs(
								Repertoire.AUTOPOIETIC,
								name
				 ) ? Response.success() : Response.failure();

		} else if (cmd.equals( "delete" )) {
			String concept = cmds.toString( Strings.UNDERSC );
			audit.debug( "Deleting "+ concept +" concept");
			Names.remove( concept );
			Load.delete( concept );
			Repertoire.signs.remove( concept );
			
		} else if (cmd.equals( "load" )) {
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			for (String file : cmds)
				Load.load( file );
			 
		} else if (cmd.equals( "unload" )) {
			for (String file : cmds)
				Autoload.unload( file );
		/*
		 *else if (cmd.equals( "reload" )) 
		 *	Strings files = cmds.copyAfter( 0 )
		 *	for(int i=0; i<files.size(); i++) Concept.unload( files.get( i ))
		 *	for(int i=0; i<files.size(); i++) Concept.load( files.get( i ))
		 */
		} else if (cmd.equals( "show" )) {
			
			if (1==cmds.size()) {
				String option = cmds.get( 0 );
				if (option.equals( "auto" )) {
					Repertoire.autop.show();
				} else if (   option.equals( "sign" )
				           || option.equals( "user" )) {
					Repertoire.signs.show();
				} else if (cmds.get( 0 ).equals( Repertoire.AUTOPOIETIC )) {
					Repertoire.signs.show(Repertoire.AUTOPOIETIC);
				} else if (option.equals( Repertoire.ALLOP )) {
					Repertoire.allop.show();
				} else if (option.equals( "all" )) {
					Repertoire.autop.show();
					Repertoire.allop.show();
					Repertoire.signs.show();
				} else if (option.equals( "variable" )) {
					Variable.interpret( new Strings( "show" ));
				} else {
					audit.error( "option: "+ option +" doesn't match anything" );
					rc = Response.failure();
				}
			} else {
				Repertoire.signs.show();
			}
		} else {
			rc = Response.failure();
		} 		
		return rc;
}	}
