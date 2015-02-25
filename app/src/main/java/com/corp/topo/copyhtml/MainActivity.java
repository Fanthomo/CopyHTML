package com.corp.topo.copyhtml;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

    public static final String CopyHTML_content = "com.corp.topo.copyhtml.content";
    public static final String CopyHTML_content_address = "com.corp.topo.copyhtml.content_address";

    private ListView listView; //view pre zoznam adries
    private ArrayList<String> listOfValues; // zoznam ktory uchovava adresy
    private ArrayList<String> contentContainer; // uchovava obsah webstranky
    private int index = 0;
    private ContentGetter contentGetter; // zabezpecuje stiahnutie obsahu zadanej webstranky
    private boolean downloaded = false; // indikuje, ci stiahnutie prebehlo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializacia zoznamov, widgetov a pohladov
        listView = (ListView) findViewById(R.id.list);
        Button addAddrBtn = (Button) findViewById(R.id.addAddressButton);
        Button downloadContBtn = (Button) findViewById(R.id.downloadButton);
        listOfValues = new ArrayList<String>();
        updateListView(); // pridame obsah adries do ListView
        // osetrenie stlacenia tlacidiel
        addAddrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(); //zobrazi dialog pre zadanie adresy
            }
        });
        downloadContBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadContent(); //volame metodu pre stiahnutie obsahu stranok
            }
        });
    }

    // vytvori sa adapter s aktualnymi hodnotami a priradi sa do Listu
    private void updateListView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_row1,R.id.text1, listOfValues);
        listView.setAdapter(adapter);
    }

    // po vyhodnoteni dialogu sa ako vstupny parameter tejto metody posle adresa z dialogu
    private void addItem(String item) {
        //kontrola, ci retazey nie je prazdny
        if (!item.equals("")) {
            item = addressValidate(item); //pridanie prefixu http:// ak je to porebne
            index++;
            listOfValues.add(item);
            Toast.makeText(this, "Pridane: " + item, Toast.LENGTH_SHORT).show();
            updateListView();
            downloaded = false;
        } else {
            Toast.makeText(this, "Nezadal si adresu", Toast.LENGTH_SHORT).show();
        }
    }
        // metoda pre vytvorenie a spracovanie dialogu pre zadanie adresy
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
    // validacna metoda pre adresu
    private String addressValidate (String address){
        if (address.startsWith("http://")){
            return address;
        } else {
            return "http://" + address;
        }
    }

    private void downloadContent(){
        contentGetter = new ContentGetter();
        contentContainer = new ArrayList<String>();
        for (final String item: listOfValues){
            //int i = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    contentContainer.add(contentGetter.getContent(item));
                }
            });
            //contentContainer.add("Obsah stranky s indexom " );
            Toast.makeText(this, "Downloaded: " + item, Toast.LENGTH_SHORT).show();
        }
        downloaded = true;
        setListener();
        Toast.makeText(this, "OK - " + !contentContainer.isEmpty(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Data nie su stiahnute...", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, ContentViewer.class);
            intent.putExtra(CopyHTML_content, contentContainer.get(position));
            intent.putExtra(CopyHTML_content_address, address);
            startActivity(intent);
        }
    }



}


