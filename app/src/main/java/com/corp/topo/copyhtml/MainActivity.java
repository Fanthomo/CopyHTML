package com.corp.topo.copyhtml;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {

    public final String CopyHTML_content = "com.corp.topo.copyhtml.content";
    public final String CopyHTML_content_address = "com.corp.topo.copyhtml.content_address";

    private ListView listView;
    private Button addAddrBtn;
    private Button downloadContBtn;
    private ArrayList<String> listOfValues;
    private ArrayList<String> contentContainer;
    private int index = 0;
    private ContentGetter contentGetter;
    private boolean downloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        addAddrBtn = (Button) findViewById(R.id.addAddressButton);
        downloadContBtn = (Button) findViewById(R.id.downloadButton);
        listOfValues = new ArrayList<String>();
        updateListView();
        addAddrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        downloadContBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadContent();
            }
        });
    }

    private void updateListView(){
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.simple_row1,R.id.text1, listOfValues);
        listView.setAdapter(adapter);

    }

    private void addItem(String item) {
        if (!item.equals("")) {
            item = addressValidate(item);
            index++;
            listOfValues.add(item);
            Toast.makeText(this, "Pridane: " + item, Toast.LENGTH_SHORT).show();
            updateListView();
            downloaded = false;
        } else {
            Toast.makeText(this, "Nezadal si adresu", Toast.LENGTH_SHORT).show();
        }
    }

        void showDialog(){
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Zadaj adresu");
            final EditText input = new EditText(this);
            b.setView(input);
            b.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {

                    String result = input.getText().toString();
                    if (result != null) {
                        addItem(result);
                    }
                }
            });
            b.setNegativeButton("CANCEL", null);
            b.create().show();
        }

    private String addressValidate (String address){
        if (address.startsWith("http://")){
            return address;
        } else {
            String tmp = new String("http://" + address);
            return tmp;
        }
    }

    private void downloadContent(){
        contentGetter = new ContentGetter();
        for (final String item: listOfValues){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    contentContainer.add(contentGetter.getContent(item.toString()));
                }
            });
            Toast.makeText(this, "Downloaded: " + item, Toast.LENGTH_SHORT).show();
        }
        downloaded = true;
        addItem(contentContainer.get(0));
        setListener();

    }

    private void setListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showContent(((TextView)view).getText().toString(),position);
            }
        });
    }

    private void showContent(String address, int position){
        if (!downloaded) {
            Toast.makeText(this, "Data nie su stiahnute...", Toast.LENGTH_SHORT);
        }
        else {
            Intent intent = new Intent(this, ContentViewer.class);
            intent.putExtra(CopyHTML_content, contentContainer.get(position));
            intent.putExtra(CopyHTML_content_address, address);
            startActivity(intent);
        }
    }



}


