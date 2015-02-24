package com.corp.topo.copyhtml;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ContentViewer extends Activity {

    public final String CopyHTML_content = "com.corp.topo.copyhtml.content";
    public final String CopyHTML_content_address = "com.corp.topo.copyhtml.content_address";

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = this.getIntent();
        intent.getStringExtra(CopyHTML_content);
        setContentView(R.layout.activity_content_viewer);
        ((TextView)findViewById(R.id.addressTextView)).setText(intent.getStringExtra(CopyHTML_content_address));
        ((TextView)findViewById(R.id.contentTextView)).setText(intent.getStringExtra(CopyHTML_content));
    }



}
