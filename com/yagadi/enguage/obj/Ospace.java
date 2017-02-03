package com.yagadi.enguage.obj;

import java.io.File;

import com.yagadi.enguage.util.Fs;

public class Ospace {
	public static final String root = Fs.root + File.separator + "yagadi.com"; //+"sofa";
	public static String charsAndInt( String s, int n ) { return s +"."+ n; }
}
