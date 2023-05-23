package org.enguage.signs.symbol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;

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
	static public  void   primedAnswer( String a ) { primedAnswer = null==a || a.equals("") ? null : a;}
	static public  void   logPrimedAns() { if (primedAnswer != null) Audit.LOG( "> "+ primedAnswer );}

	public Question( String q ) { question( q ); }

	private String getLine( String defaultLine ) {
		String line = null;
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ));
		try {
			while (line == null || line.equals( "\n" ))
				line = Shell.stripTerminator( br.readLine() );
		} catch (java.io.IOException e ) {
			audit.error( "IO exception in Question.getLine( default );" );
		}
		return line != null ? line : defaultLine;
	}
	
	public String ask() {
		Audit.LOG( question() + prompt());
		Question.logPrimedAns();
		return primedAnswer != null ? primedAnswer() : getLine( Response.dnuStr());
	}
	
	// helper functions...
	static public Strings extractPotentialAnswers( Strings utterance ) {
		audit.in( "extractPotentialAnswers", utterance.toString() );
		Strings answers = new Strings();
		Strings answer = new Strings();
		boolean recording = false;
		Iterator<String> qi = utterance.iterator();
		while (qi.hasNext()) {
			String tmp = qi.next();
			if (tmp.equals( "answering" )) // should be first word!
				recording = true;
			else if (recording && tmp.equals( "," )) { // should be last token on answers
				recording = false;
				if (answer.size() > 0) {  // sanity check
					answers.add( answer.toString() );
					answer = new Strings();
				}
			} else if (recording && tmp.equals( "or" )) {
				if (answer.size() > 0) { // sanity check
					answers.add( answer.toString() );
					answer = new Strings();
				}
			} else if (recording) // add anything else to the potential answers
				answer.add( tmp );
		}
		// recording should be false!
		return audit.out( answers );
	}
	
	// -- test code
	public static void main( String args[] ){
		
		Question q = new Question( "why do birds suddenly appear" );
		primedAnswer( "they long to be close to you!" );
		audit.log( q.ask());
		
		q = new Question( "is it safe" );
		primedAnswer( "yes, it is" );
		audit.log( q.ask( ));
		
		primedAnswer("no, it isn't");
		audit.log( q.ask( ));
		
		primedAnswer( null );
		audit.log( q.ask() );
		audit.log( q.ask() );
}	}