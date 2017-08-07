package com.yagadi.enguage.vehicle;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.yagadi.enguage.util.Audit;

public class Question {
	
	static  Audit audit = new Audit( "Question" );
	
	private String   prompt = "? ";
	public  String   prompt() { return prompt; }
	public  Question prompt( String p ) { prompt = p; return this; }

	private String   question = "why do birds suddenly appear";
	public  String   question() { return question; }
	public  Question question( String q ) { question = q; return this; }

	static private String primedAnswer = null;
	static public  String primedAnswer() { return primedAnswer;}
	static public  void   primedAnswer( String a ) {primedAnswer = a;}
	static public  void   logPrimedAns() { if (primedAnswer != null) audit.LOG( "> "+ primedAnswer );}

	public Question( String q ) { question( q ); }
	
	static private String getLine( String defaultLine ) {
		String line = null;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ));
		try {
			while (line == null || line.equals( "\n" ))
				line = br.readLine();
		} catch (java.io.IOException e ) {
			audit.ERROR( "IO exception in Question.getLine( default );" );
		}
		return line != null ? line : defaultLine;
	}
	
	public String ask() { return ask( null ); }
	public String ask( String answer ){
		audit.LOG( question() + prompt());
		return answer != null ? answer : getLine( Reply.dnu());
	}
	public static void main( String args[] ){
		Question q = new Question( "why do birds suddenly appear" );
		audit.log( q.ask( "they long to be close to you!" ));
		q = new Question( "is it safe" );
		audit.log( q.ask( "yes, it is"));
		audit.log( q.ask( "no, it isn't"));
		audit.log( q.ask() );
		audit.log( q.ask() );
}	}