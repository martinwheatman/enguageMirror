package org.enguage.sign.symbol.reply;

import org.enguage.sign.Config;
import org.enguage.util.strings.Strings;

public class Response {
	
	public enum Type {
		E_DNU( -5 ), // DO NOT UNDERSTAND
		E_UDU( -4 ), // user does not understand
		E_DNK( -3 ), // NOT KNOWN -- init
		E_SOZ( -2 ), // SORRY -- -ve
		E_NO(  -1 ), // FALSE -- -ve
		E_OK(   0 ), // TRUE  -- +ve
		E_CHS(  1 ); // narrative verdict - meh!
		
		private Type( int i ) {value = i;}
		
		private int value;
		public  int value() {return value;}
	}
	
	private Type type = Type.E_DNU;
	public  Type type() {return type;}
	public  void type( Type t ) {type = t;}
	
	public  static boolean  felicitous( Type t ) {return Type.E_OK == t || Type.E_CHS == t;}
	
	public  static Type     stringsToResponseType( Strings uttr ) {
		     if (uttr.begins( Config.yes()     )) return Type.E_OK;
		else if (uttr.begins( Config.success() )) return Type.E_OK;
		else if (uttr.begins( Config.failure() )) return Type.E_SOZ;
		else if (uttr.begins( Config.dnu()     )) return Type.E_DNU;
		else if (uttr.begins( Config.udu()     )) return Type.E_UDU;
		else if (uttr.begins( Config.no()      )) return Type.E_NO;
		else if (uttr.begins( Config.dnk()     )) return Type.E_DNK;
		else return Type.E_CHS;
}	}
