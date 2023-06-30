package com.yagadi;

import org.enguage.util.strings.Strings;

public class Enguage implements Runnable {

    private static org.enguage.Enguage enguage = null;
    public  static void                enguage( org.enguage.Enguage e ) {enguage = e;}

    private String reply = "";
    private final String utterance;

    public Enguage( String u ) {
        utterance = u;
    }

    public void run() {
        if (enguage != null)
            reply = enguage.mediate( utterance );
    }

    public String toString() {
        return reply;
    }
}
