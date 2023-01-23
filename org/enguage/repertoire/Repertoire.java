package org.enguage.repertoire;

import java.util.TreeSet;

import org.enguage.repertoire.concept.Autoload;
import org.enguage.repertoire.concept.Load;
import org.enguage.signs.Signs;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.Utterance;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Repertoire {
	
	private Repertoire() {}
	
	public  static final String NAME = "repertoire";
	public  static final int      ID = 216434732;
	private static final Audit audit = new Audit( NAME );
	
	public  static final String           LOC = "rpt";

	public  static final String     ALLOP_STR = "engine";
	public  static final String     AUTOP_STR = "autopoiesis";
	public  static final String   AUTOPOIETIC = "OTF"; // repertoire name for signs created on-the-fly
	// TODO: create a method to comb users for signs created on-the-fly, 
	//       and to save them under (append them to) a concept file
	// with the name of the value of the pattern?  Autoload() needs to load
	// these pattern files. Theses need to be sought first(?):
	// i_need_X.txt is searched before need+needs.txt, because is it the users /will/
	// n.B. X_means_X.txt - X is just a placeholder.
	
	/* This class maintains three repertoire groups - signs, autop and allopoetic
	 * Each, well signs, contains signs from all runtime loaded repertoires and
	 * all autoloaded repertoires. Perhaps runtime loaded repertoires could go 
	 * in engine?
	 */
	public    static final Signs signs = new Signs( "user"  );
	protected static final Signs autop = new Signs( "autop" ).add( Autopoiesis.signs );
	protected static final Signs allop = new Signs( "allop" ).add( Engine.commands );
	
	/* A persistent Induction is used in the repertoire.
	 */
	private static final String FALSE = Boolean.toString( false );
	private static final String  TRUE = Boolean.toString( true  );
	
	private static Variable transformation = new Variable( "transformation", FALSE );
	public  static  boolean  transformation() {
		return transformation.get().equalsIgnoreCase( TRUE );
	}
	public static  boolean transformation( boolean b ) {
		transformation.set( b ? TRUE : FALSE );
		return b;
	}

	//
	// Repertoire Management -- above
	// *********************************************************** 

	// entry point for Enguage, re-entry point for Intention
	public static Reply mediate( Utterance u ) {
		//audit.in( "mediate", "utterance="+ u )
		// Ordering of repertoire:
		// 1. check through autop first, at startup
		// 2. during runtime, do user signs first
		Reply r = autop.mediate( u );
		if (Response.DNU == r.response()) {
			
			if (!transformation()) {
				Autoload.load( u.representamen() ); // unloaded up in Enguage.interpret()
				
				/* At this point we need to rebuild utterance with the (auto)loaded concept,
				 * with any colloquialisms it may have loaded...
				 * Needs to be expanded in case we've expanded any parameters (e.g. whatever)
				 */
				u = new Utterance( u.expanded() );
			}
			r = signs.mediate( u );
			
			if (Response.DNU == r.response())
				r = allop.mediate( u );
		}
		//audit.out( r )
		return r;
	}
	
	public static Strings interpret( Strings cmds ) {
		audit.in( "interpret", "cmds="+ cmds );
		Strings rc = Response.failure();
		if (!cmds.isEmpty()) {
			String cmd = cmds.remove( 0 );
			
			if (cmd.equals("show")) {
				rc = Response.success();
				
				String name = cmds.remove( 0 );
				if (name.equals("signs") ||
					name.equals("user"))
					
					signs.show();
					
				else if (name.equals( ALLOP_STR ))
					allop.show();
					
				else if (name.equals("autop"))
					autop.show();
					
				else if (name.equals( "all" )) {
					autop.show();
					allop.show();
					signs.show();
					
				} else
					rc = Response.failure();
				
			} else if (cmd.equals( "variable" )) {
				Variable.interpret( new Strings( "show" ));
				
			} else if (cmd.equals( "list" )) {
				//Strings reps = Enguage.e.signs.toIdList()
				/* This becomes less important as the interesting stuff becomes auto loaded 
				 * Don't want to list all repertoires once the repertoire base begins to grow?
				 * May want to ask "is there a repertoire for needs" ?
				 */
				rc = new Strings( "loaded repertoires include "+ new Strings( (TreeSet<String>)Load.loaded()).toString( Reply.andListFormat() ));
			}
		}
		audit.out();
		return rc;
}	}
