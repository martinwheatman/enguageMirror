package com.yagadi.enguage.sofa;

import java.io.File;

import com.yagadi.enguage.util.Filesystem;

public class Ospace {
	public static final String root = Filesystem.root + File.separator + "yagadi.com"; //+"sofa";
	public static String charsAndInt( String s, int n ) { return s +"."+ n; }
}
