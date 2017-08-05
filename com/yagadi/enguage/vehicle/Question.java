package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;

public class Question {
	
	static  Audit audit = new Audit( "Question" );
	
	private String   prompt = "? ";
	public  String   prompt() { return prompt; }
	public  Question prompt( String p ) { prompt = p; return this; }

	private String   question = "why do birds suddenly appear";
	public  String   question() { return question; }
	public  Question question( String q ) { question = q; return this; }

	public Question( String q ) { question( q ); }
	
	public String ask() { return ask( null ); }
	public String ask( String answer ){
		audit.LOG( question() + prompt());
		return answer != null ? answer : Shell.getLine( Reply.dnu());
	}
	public static void main( String args[] ){
		Question q = new Question( "why do birds suddenly appear" );
		audit.log( q.ask( "they long to be close to you!" ));
}	}