package com.yagadi.enguage.sofa;

import java.io.File;

import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

class EntityShell extends Shell {
	EntityShell() { super( "Entity" );}
	public String interpret( Strings argv ) { return Entity.interpret( argv ); }
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
		return Filesystem.exists( name( name, Overlay.MODE_READ ));
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
		boolean rc = Filesystem.create( name( name, Overlay.MODE_WRITE ));
		//return audit.out( rc );
		return rc;
	}
	
	// really should be in a corresponding Component.c module!
	public static boolean createComponent( Strings a ) {
		boolean rc = false;
		String name = "";
		for (int i=0, sz=a.size(); i<sz; i++) { // ignore all initial unsuccessful creates
			name += a.get( i );
			rc = Filesystem.create( name );
			name += "/";
		}
		return rc;	
	}
	public static boolean delete( String name ) {
		boolean rc = true;
		String readName  = Overlay.fsname( name, Overlay.MODE_READ );
		if (Filesystem.exists( readName )) {
			String writeName = Overlay.fsname( name, Overlay.MODE_WRITE ),
			       dname = deleteName( writeName );
			if (!Filesystem.destroy( writeName )) {
				// haven't managed to remove top overlay entity -- either not empty or not there
				rc = Filesystem.exists( writeName ) ?
					Filesystem.rename( writeName, dname ) : // ...it is there, so rename it!
					Filesystem.create( dname ); //...not there, so put in a placeholder!
			} else if (Filesystem.exists( readName )) // successfully removed entity but prev version still exists...
				rc = Filesystem.create( dname );
		}
		return rc;
	}
	public static boolean ignore( String name ) {
		boolean status = false;
		String actual = Overlay.fsname( name, Overlay.MODE_READ ),
		       potential = Overlay.fsname( name, Overlay.MODE_WRITE ),
		       ignored = deleteName( potential );
		if (Filesystem.exists( actual ))
			if (Filesystem.exists( potential )) 
				status = Filesystem.rename( potential, ignored );
			else
				status = Filesystem.create( ignored );
		return status;
	}
	
	public static boolean restore( String entity ) {
		boolean status = false;
		String restored = Overlay.fsname( entity, Overlay.MODE_WRITE ),
				ignored = deleteName( restored );
		if (!exists( entity ))
			status = Filesystem.rename( ignored, restored );
		return status;
	}
	
	static public String interpret( Strings argv ) {
		// N.B. argv[ 0 ]="create", argv[ 1 ]="martin wheatman"
		String rc = Shell.FAIL;
		if (argv.size() > 0) {
			String cmd = argv.get( 0 );
			//if (argv.size() == 1) {
			//	if (argv.size() == 2 && cmd.equals("list"))
			//		rc = list() ? Shell.SUCCESS : Shell.FAIL;
			//	}
			//} else {
				String ent = argv.get( 1 );
				if (argv.size() == 2 && cmd.equals("create"))
					rc = create( ent )? Shell.SUCCESS : Shell.FAIL;
				else if (argv.size() >= 3 && cmd.equals("component"))
					rc = createComponent( argv.copyAfter( 1 ))? Shell.SUCCESS : Shell.FAIL;
				else if (argv.size() == 2 && cmd.equals("delete"))
					rc = delete( ent)? Shell.SUCCESS : Shell.FAIL;
				else if (argv.size() == 2 && cmd.equals("exists"))
					rc = exists( ent)? Shell.SUCCESS : Shell.FAIL;
				else if (argv.size() == 2 && cmd.equals("ignore"))
					rc = ignore( ent)? Shell.SUCCESS : Shell.FAIL;
				else if (argv.size() == 2 && cmd.equals("restore"))
					rc = restore( ent)? Shell.SUCCESS : Shell.FAIL;
				else
					System.err.println(
							"Usage: entity [create|exists|ignore|delete] <entityName>\n"+
							"Given: entity "+ argv.toString( Strings.SPACED ));
		}	//}
		return rc;
	}
	
	public static void main (String args []) {
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else
			new EntityShell().run();
}	}
