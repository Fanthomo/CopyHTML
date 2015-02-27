package com.corp.topo.copyhtml;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ContentViewer extends Activity {



    Intent intent;
    ProgressBar bar;
    TextView addrtext;
    TextView conttext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = this.getIntent(); //ziskanie intentu od volajucej aktivity(rodica)
        setContentView(R.layout.activity_content_viewer);
        bar = (ProgressBar)findViewById(R.id.progressBar2);
        addrtext = ((TextView)findViewById(R.id.addressTextView));
        conttext = ((TextView)findViewById(R.id.contentTextView));
        new loadContent().execute(); //nacitame obsah na pozadi
    }

    private class loadContent  extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            // data ziskane z EXTRA zobrazime
            addrtext.setText(intent.getStringExtra(MainActivity.CopyHTML_content_address));
            conttext.setText(intent.getStringExtra(MainActivity.CopyHTML_content));
            return null;
        }

        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
            addrtext.setVisibility(View.INVISIBLE);
            conttext.setVisibility(View.INVISIBLE);

        }

        @Override
        protected void onPostExecute(String result) {
            bar.setVisibility(View.INVISIBLE);
            addrtext.setVisibility(View.VISIBLE);
            conttext.setVisibility(View.VISIBLE);
        }
    }



}
