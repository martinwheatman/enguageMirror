package org.enguage.objects.space;

import java.io.File;

import org.enguage.objects.space.Entity;
import org.enguage.objects.space.EntityShell;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

class EntityShell extends Shell {
	EntityShell() { super( "Entity" );}
	public Strings interpret( Strings argv ) { return Entity.interpret( argv ); }
}

public class Entity {
	static private Audit audit = new Audit( "Entity" );
	
	public static String name( String entity, String rw ) {
		return Overlay.fsname( entity, rw );
	}
	public static boolean exists( String name ) {
		//audit.in( "exists", Overlay.fsname( name, Overlay.MODE_READ ));
		audit.debug( "NAME="+ name( name, Overlay.MODE_READ ));
		//return audit.out( Filesystem.exists( name( name, Overlay.MODE_READ )));
		return Fs.exists( name( name, Overlay.MODE_READ ));
	}

	public static String deleteName( String name ) {
		if (isDeleteName( name )) return name;
		File f = new File( name );
		return f.getParent() +"/!"+ f.getName();
	}
	public static String nonDeleteName( String name ) {
		if (!isDeleteName( name )) return name;
		File f = new File( name );
		return f.getParent() +"/"+ f.getName().substring( 1 );
	}
	public static boolean isDeleteName( String name ) {
		return new File( name ).getName().charAt( 0 ) == '!';
	}
	
	public static boolean create( String name ) {
		//audit.in( "create", "name='"+ name +"'" );
		boolean rc = Fs.create( name( name, Overlay.MODE_WRITE ));
		//return audit.out( rc );
		return rc;
	}
	
	// really should be in a corresponding Component.c module!
	public static boolean createComponent( Strings a ) {
		boolean rc = false;
		String name = "";
		for (int i=0, sz=a.size(); i<sz; i++) { // ignore all initial unsuccessful creates
			name += a.get( i );
			rc = Fs.create( name );
			name += "/";
		}
		return rc;	
	}
	public static boolean delete( String name ) {
		boolean rc = true;
		String readName  = Overlay.fsname( name, Overlay.MODE_READ );
		if (Fs.exists( readName )) {
			String writeName = Overlay.fsname( name, Overlay.MODE_WRITE ),
			       dname = deleteName( writeName );
			if (!Fs.destroy( writeName )) {
				// haven't managed to remove top overlay entity -- either not empty or not there
				rc = Fs.exists( writeName ) ?
					Fs.rename( writeName, dname ) : // ...it is there, so rename it!
					Fs.create( dname ); //...not there, so put in a placeholder!
			} else if (Fs.exists( readName )) // successfully removed entity but prev version still exists...
				rc = Fs.create( dname );
		}
		return rc;
	}
	public static boolean ignore( String name ) {
		boolean status = false;
		String actual = Overlay.fsname( name, Overlay.MODE_READ ),
		       potential = Overlay.fsname( name, Overlay.MODE_WRITE ),
		       ignored = deleteName( potential );
		if (Fs.exists( actual ))
			if (Fs.exists( potential )) 
				status = Fs.rename( potential, ignored );
			else
				status = Fs.create( ignored );
		return status;
	}
	
	public static boolean restore( String entity ) {
		boolean status = false;
		String restored = Overlay.fsname( entity, Overlay.MODE_WRITE ),
				ignored = deleteName( restored );
		if (!exists( entity ))
			status = Fs.rename( ignored, restored );
		return status;
	}
	
	static public Strings interpret( Strings argv ) {
		// N.B. argv[ 0 ]="create", argv[ 1 ]="martin wheatman"
		Strings rc = Shell.Fail;
		if (argv.size() > 1) {
			String cmd = argv.remove( 0 ),
			       ent = argv.remove( 0 );
			if (cmd.equals( "create" ))
				rc = create( ent ) ? Shell.Success : Shell.Fail;
			else if (cmd.equals( "component" ))
				rc = createComponent( argv )? Shell.Success : Shell.Fail;
			else if (cmd.equals( "delete" ))
				rc = delete( ent ) ? Shell.Success : Shell.Fail;
			else if (cmd.equals( "exists" ))
				rc = exists( ent ) ? Shell.Success : Shell.Fail;
			else if (cmd.equals( "ignore" ))
				rc = ignore( ent ) ? Shell.Success : Shell.Fail;
			else if (cmd.equals( "restore" ))
				rc = restore( ent ) ? Shell.Success : Shell.Fail;
			else
				System.err.println(
						"Usage: entity [create|exists|ignore|delete] <entityName>\n"+
						"Given: entity "+ argv.toString( Strings.SPACED ));
		}
		return rc;
	}
	
	public static void main (String args []) {
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else
			new EntityShell().run();
}	}
