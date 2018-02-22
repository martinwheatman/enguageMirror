package org.enguage.object.space;

import java.io.File;

import org.enguage.util.Fs;

public class Ospace {
	public static String root() {
		return Fs.root() + File.separator + "enguage.org"; //+"sofa";
	}
	public static String charsAndInt( String s, int n ) { return s +"."+ n; }
	static public String location() {
		String loc = (Fs.location().equals("") ?
				root() :
				Fs.location()
			) + File.separator;
		new File( loc ).mkdirs();
		return loc;
	}
}
