package org.enguage.repertoires.written;

import java.util.ArrayList;
import java.util.List;

import org.enguage.repertoires.Repertoires;
import org.enguage.signs.Sign;
import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Context;

public class AtpRpt {
	
	private static final Audit audit = new Audit( "AtpRpt" );
	private static final String atpConnector = "then , ";
	
	private AtpRpt() {}
	
	public  static Sign[] signs() {return signs;}
	private static final Sign[] signs = {
		// 3 x 3 signs (think/do/say * start/subseq/infelicit) + 1 "finally"
		new Sign()
			.pattern( "On QUOTED-X, PHRASE-Y" )
			.append( new Intention( Intention.atpRptCre, Intention.thenThink, "X Y" )),
			
		new Sign()
			.pattern( "On QUOTED-X, perform QUOTED-Y" )
			.append( new Intention( Intention.atpRptCre, Intention.thenDo, "X Y" )),
			
		new Sign()
			.pattern( "On QUOTED-X, reply QUOTED-Y" )
			.append( new Intention( Intention.atpRptCre, Intention.thenReply, "X Y" )),
			
		new Sign()
			.pattern( atpConnector + "PHRASE-Y" )
			.append( new Intention( Intention.atpRptApp, Intention.thenThink, "Y" )),
			
		new Sign()
			.pattern( atpConnector + "perform QUOTED-Y" ) // ----
			.append( new Intention( Intention.atpRptApp, Intention.thenDo, "Y" )),
			
		new Sign()
			.pattern( atpConnector + "reply QUOTED-Y" )
			.append( new Intention( Intention.atpRptApp, Intention.thenReply, "Y" )),
			
		new Sign()
			.pattern( atpConnector + "if not, PHRASE-Y" )
			.append( new Intention( Intention.atpRptApp, Intention.elseThink, "Y" )),
			
		new Sign()
			.pattern( atpConnector + "if not, perform QUOTED-Y" ) // ---
			.append( new Intention( Intention.atpRptApp, Intention.elseDo, "Y" )),
					
		new Sign()
			.pattern( atpConnector + "if not, reply QUOTED-Y" )
			.append( new Intention( Intention.atpRptApp, Intention.elseReply, "Y")),
			
		new Sign()
			.pattern( atpConnector + "if not, say so" )
			.append( new Intention( Intention.atpRptApp, Intention.elseReply, "\"\"")),
			
		//	Added 3 new signs for the running of applications external to enguage...
		new Sign()
			.pattern( "On QUOTED-X, run QUOTED-Y" )
			.append( new Intention( Intention.atpRptCre, Intention.thenRun, "X Y" )),
		
		new Sign()
			.pattern( atpConnector + "run QUOTED-Y" )
			.append( new Intention( Intention.atpRptApp, Intention.thenRun, "Y" )),
	
		new Sign()
			.pattern( atpConnector + "if not, run QUOTED-Y" )
			.append( new Intention( Intention.atpRptApp, Intention.elseRun, "Y" ))
	};
	
	public static Reply interp( Intention in, Reply r ) {
		r.answer( Response.yesStr()); // bland default reply to stop debug output look worrying
		
		if (in.type() == Intention.atpRptCre) {
			Strings sa      = Context.deref( new Strings( in.values() ));
			String  pattern = sa.remove( 0 );
			String  val     = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
			
			Repertoires.signs.insert(
					Sign.latest(
						new Sign()
							.pattern( Strings.trim( pattern, Strings.DOUBLE_QUOTE ))
							.concept( Intention.concept() )
							.append( new Intention( in.id(), val ))
			)		);
			
		} else if (in.type() == Intention.atpRptApp) {
			if (null == Sign.latest())
				audit.error( "adding to sign before creation" );
			else {
				Strings sa = Context.deref( new Strings( in.values() ));
				String val = Strings.trim( sa.remove( 0 ), Strings.DOUBLE_QUOTE );
				
				Sign.latest().append( new Intention( in.id(), val ));
		}	}
		
		return r;
	}

	private static void buildList( int rcSz, Strings primary, String connector, ArrayList<Strings>rc ) {
		for (int i=0; i<rcSz; i++) {
			Strings tmp = new Strings();
			if (i==0)
				tmp.addAll( primary );
			else
				tmp.add( 0, connector );
			tmp.add( "," );
			tmp.addAll( rc.get( i ));
			rc.set( i, tmp );
	}	}

	private static String getConnector( int rcSz, ArrayList<Strings> rc ) {
		// remove connector from last in list
		String connector = "then"; // default: this is the only one used!
		Strings lastList = rc.get( rcSz-1 );
		if (lastList.size() > 2 && lastList.get( 1 ).equals( "," )) {
			connector = lastList.remove( 0 ); // remove connector
			lastList.remove( 0 );           // remove ","
			rc.set( rcSz-1, lastList );     // replace this last item
		}
		return connector;
	}

	public static List<Strings> expandSemicolonList( Strings sentence ) {
		/*  "on one: do two; do three; and, do four." =>
		 *  [ "on one, do two.", "and on one, do three.", "and on one, do four." ]
		 *  "and" many be replace by, for example "or", "then", "also"
		 */
		ArrayList<Strings> rc = new ArrayList<>();
		
		// create a primary and a list of secondaries
		boolean isOnPattern = true;
		Strings onPattern   = new Strings();    // On "a B c" (:)
		Strings intention   = new Strings (); // if so, do something[;|.]
		for (String s : sentence) 
			if (s.equals(":")) {
				isOnPattern = false;
			} else if (s.equals(";")) {
				rc.add( intention );
				intention = new Strings();
			} else if (isOnPattern) {
				onPattern.add( s );
			} else {
				intention.add( s );
			}
		if (!intention.isEmpty()) rc.add( intention );
		
		// if we've found no semi-colon separated list...
		if (rc.isEmpty()) {
			// ...just pass back the original list
			rc.add( sentence );
			
		} else {
			int rcSz = rc.size();
			String connector = getConnector(rcSz, rc);
			
			// re-construct list, adding-in pattern, ...
			// ...and the connector on subsequent lists
			buildList( rcSz, onPattern, connector, rc );
		}
		return rc;
	}	}
