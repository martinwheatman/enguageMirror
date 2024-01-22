package org.enguage.sign.interpretant;

import org.enguage.sign.Config;
import org.enguage.util.strings.Strings;

public class Response {

	/* ------------------------------------------------------------------------
	 * Response type
	 */
	public enum Type {
		E_DNU, // DO NOT UNDERSTAND
		E_UDU, // user does not understand
		E_DNK, // NOT KNOWN -- init
		E_SOZ, // SORRY -- -ve
		E_NO,  // FALSE -- -ve
		E_OK,  // TRUE  -- +ve identical to 'yes'
		E_CHS; // narrative verdict - meh!
	}

	public static Type typeFromStrings( Strings uttr ) {
		     if (uttr.begins( Config.yes()     )) return Type.E_OK;
		else if (uttr.begins( Config.okay()    )) return Type.E_OK;
		else if (uttr.begins( Config.notOkay() )) return Type.E_SOZ;
		else if (uttr.begins( Config.dnu()     )) return Type.E_DNU;
		else if (uttr.begins( Config.udu()     )) return Type.E_UDU;
		else if (uttr.begins( Config.no()      )) return Type.E_NO;
		else if (uttr.begins( Config.dnk()     )) return Type.E_DNK;
		else return Type.E_CHS;
	}
}
