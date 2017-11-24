package org.enguage.object;

import java.io.File;

import org.enguage.util.Fs;

public class Ospace {
	public static final String root = Fs.root + File.separator + "yagadi.com"; //+"sofa";
	public static String charsAndInt( String s, int n ) { return s +"."+ n; }
	static public String location() {
		return (
			Fs.location().equals("") ?
				root :
				Fs.location()
			) + File.separator;
	}
}
