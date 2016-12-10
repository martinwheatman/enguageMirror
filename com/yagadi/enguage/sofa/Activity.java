package com.yagadi.enguage.sofa;

import java.io.IOException;

public class Activity {

	public Activity () {};
	public void exec( String cmd ) {
		Runtime r = Runtime.getRuntime();
		try {
			r.exec( cmd );
		} catch (IOException e) {
			System.err.println("please ignore me");
	}	}
	
	public static void main( String[] args ) {
		new Activity().exec( "this is a command which will fail" );
}	}
