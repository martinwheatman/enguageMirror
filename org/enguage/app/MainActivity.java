/*
package org.enguage.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import org.enguage.Enguage;
import org.enguage.objects.space.Overlay;
import org.enguage.interp.repertoire.Concepts;
import org.enguage.util.Audit;
import org.enguage.util.sys.Fs;
import org.enguage.util.Strings;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

	public  static final String        NAME = "MainActivity";
	private static final int REQUEST_SPEECH = 1;

	public TextToSpeech tts = null;
	private boolean ttsInitialised = false;
	private boolean vocalised() {
		AudioManager am = (AudioManager)this.getSystemService( Activity.AUDIO_SERVICE );
		return ttsInitialised && AudioManager.RINGER_MODE_NORMAL == am.getRingerMode();
	}

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		//Audit.startupDebug = true;

		// hide the action bar
		//getActionBar().hide();
		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR) {
					tts.setLanguage( Locale.getDefault() );
				}
			}
		});

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				promptSpeechInput();
			}
		});

		initEnguage();
	}

	public void onInit(int code) {
		if (TextToSpeech.SUCCESS == code) {
			ttsInitialised = true;
	}	}

	private void initEnguage() {
		if (null == Enguage.e) {
			Enguage.e = new Enguage()
					.location( this.getExternalFilesDir(null ).getPath() )
					.root( this.getExternalFilesDir(null ).getPath() )
					.context( this );
		}

		if ((null == Enguage.e.o || !Enguage.e.o.attached() ) && !Overlay.autoAttach())
			Log.e( "Ouch!",">>>>>>>> Cannot autoAttach() to object space<<<<<<" );

		try {
			Enguage.e.concepts( this.getAssets().list( "concepts" ));
		} catch (Exception e) {
			Log.e( "ERR",">>>exception in names initialisation: "+ e.toString());
		}

		// read the config in the background...
		new MainActivity.ReadConfig( this ).execute();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	private void promptSpeechInput() {

		Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
				        Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				        getString(R.string.speech_prompt));

		try {
			startActivityForResult( intent, REQUEST_SPEECH );
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(),
					       getString(R.string.speech_not_supported),
					       Toast.LENGTH_SHORT).show();
	}	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQUEST_SPEECH:
				if (resultCode == RESULT_OK && null != data) {
					ArrayList<String> saidArray =
							data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
					String said = saidArray.get( 0 );
					Log.i( ">>>>>>>>>>UTTERANCE>>> ", said);
					if (Audit.runtimeDebug)
						Toast.makeText( getApplicationContext(), said, Toast.LENGTH_SHORT ).show();

					// interpret what is said...
					// ...in case of config failure, repeat what was said
					String truText = Enguage.e.interpret( new Strings( said )),
						   toSpeak = truText.equals( Enguage.DNU ) ? said : truText;

					if (toSpeak.equals( "I don't understand." ))
						toSpeak += (" " + said);

					Toast.makeText( getApplicationContext(), truText, Toast.LENGTH_SHORT ).show();
					Log.i ( ">>>>>>>>>>>REPLY>>> ", toSpeak );
					if (null != tts) {
						tts.stop();
						tts.speak( toSpeak, TextToSpeech.QUEUE_FLUSH, null );
				}	}
				break;

			// case REQUEST_LANGUAGE:
			//	if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
			//		// success, create the TTS instance
			//		tts = new TextToSpeech(this, this);
			//		tts.setLanguage( Locale.getDefault() );
			//	} else {
			//		// missing data? install it!
			//		Intent installIntent = new Intent();
			//		installIntent.setAction( TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA );
			//		startActivity( installIntent );
			//	}
			//	break;
	}	}

	@Override
	public void onPause() {
		if (null != tts) tts.stop();
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		Enguage.e = null;
		if (null != tts) tts.shutdown();
		super.onDestroy();
	}
	// --------
	private class ReadConfig extends AsyncTask<Void, Void, Void> {
		private static final String NAME = "BKG";

		private MainActivity ctx = null;
		public ReadConfig( MainActivity a ) {
			super();
			ctx = a;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i( NAME, "Start" );
		}
		@Override
		protected void onPostExecute( Void result ) {
			super.onPostExecute( result );
			Log.i( NAME, "Done" );
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			AssetManager am = ctx.getAssets();
			try {
				InputStream is = am.open( "config.xml" );
				Enguage.loadConfig( Fs.stringFromStream( is ));
				is.close();
			} catch (Exception e) {
				Log.e( NAME, "doInBackground() failed: "+ e.toString() );
			}
			return null;
}	}	}
*/
