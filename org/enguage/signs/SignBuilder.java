package org.enguage.signs;

import java.util.Iterator;
import java.util.ListIterator;

import org.enguage.signs.interpretant.Intention;
import org.enguage.signs.interpretant.Intentions;
import org.enguage.signs.symbol.pattern.Frags;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class SignBuilder {

	Strings strs;
	Frags frags;
	Intentions intentions = new Intentions();

	public SignBuilder( Strings sa ) {
		strs = new Strings( sa );
	}

	private boolean isQuoted(String s) {
		if (null==s||s.length()==0) return false;
		char ch = s.charAt(0);
		return ch == '"' || ch == '\'';
	}
	private int getType(Strings sa) {
		int rc = Intention.undef;
		
		int len = sa.size();
		String one = len>0?sa.get(0):"";
		String two = len>1?sa.get(1):"";
		String thr = len>2?sa.get(2):"";
		
		boolean neg = false;
		if (one.equals( "if" ) && thr.equals( "," )) {
			neg = two.equals( "not" );
			one = len>3?sa.get(3):"";
			two = len>4?sa.get(4):"";
		}
		
		if (isQuoted( two )) {
			if (one.equals("perform"))
				rc = !neg ? Intention.thenDo  : Intention.elseDo;
			else  if (one.equals("run"))
				rc = !neg ? Intention.thenRun : Intention.elseRun;
		}
		
		if (rc == Intention.undef)
			rc = !neg ? Intention.thenThink : Intention.elseThink;
		
		return rc;
	}
	private Sign doIntentions(Iterator<String> si, Sign sign) {
		Strings sa = new Strings();
		while (si.hasNext()) {
			String s = si.next();
			if (s.equals( ";" )) {
				int type = getType( sa );
				sign.append( new Intention( type, sa ));
				sa = new Strings();
			} else
				sa.append( s );
		}
		if (!sa.isEmpty()) {
			int type = getType( sa );
			sign.append( new Intention( type, sa ));
		}
		return sign;
	}
	private Sign doPattern(Iterator<String> si) {
		if (si.hasNext()) {
			String s = si.next();
			frags = new Frags( Strings.trim( s, '"' ));
			if (si.hasNext()) {
				s = si.next();
				if ((s.equals(",") || s.equals(":"))
					&& si.hasNext())
				{
					Sign sign = new Sign();
					sign.pattern( frags );
					doIntentions( si, sign );
					return sign;
				}
			}
		}
		return null;
	}
	public Sign toSign() {
		ListIterator<String> si = strs.listIterator();
		if (si.hasNext()) {
			String s = si.next();
			if (s.equals("On"))
				return doPattern( si );
			else
				si.previous();
		}
		return null;
	}
	// ---
	public static void test( String utterance ) {
		Strings sa = new Strings( utterance );
		SignBuilder builder  = new SignBuilder( sa );
		Sign    sign     = builder.toSign();
		Audit.LOG( "Utterance is "+ utterance );
		
		if (sign != null)
			Audit.LOG( "sign("+ sign.intentions().size() +"): "+ sign.toString() );
		else
			Audit.LOG( "mediate: "+ utterance );
	}
	public static void main( String[] args ) {
		test( "On \"i need QUOTED-THINGS\":\n"
				+"\tadd THINGS to my needs list;\n"
				+"\tperform \"do this\";\n"
				+"\trun \"ls -l\";\n"
				+"\treply \"ok, you need ...\""
		);
		test( "On \"hello\": reply \"hello to you too\"" );
		test( "what do i need" );
}	}
