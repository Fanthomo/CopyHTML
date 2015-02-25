package com.corp.topo.copyhtml;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ContentViewer extends Activity {



    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = this.getIntent();
        intent.getStringExtra(MainActivity.CopyHTML_content);
        setContentView(R.layout.activity_content_viewer);
        ((TextView)findViewById(R.id.addressTextView)).setText(intent.getStringExtra(MainActivity.CopyHTML_content_address));
        ((TextView)findViewById(R.id.contentTextView)).setText(intent.getStringExtra(MainActivity.CopyHTML_content));
    }



}
