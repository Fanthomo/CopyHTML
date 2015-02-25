package com.corp.topo.copyhtml;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;


public class MainActivity extends Activity {

    public static final String CopyHTML_content = "com.corp.topo.copyhtml.content";
    public static final String CopyHTML_content_address = "com.corp.topo.copyhtml.content_address";
    private static final String DEBUG_TAG = "CopyHTML";

    private ListView listView; //view pre zoznam adries
    private ArrayList<String> listOfValues; // zoznam ktory uchovava adresy
    private ArrayList<String> contentContainer; // uchovava obsah webstranky
    private int index = 0;
    private ContentGetter contentGetter; // zabezpecuje stiahnutie obsahu zadanej webstranky
    private boolean downloaded = false; // indikuje, ci stiahnutie prebehlo

    @Override
    protected void onPause(){
        super.onPause();
    }

    protected void onResume(){
        super.onResume();
    }

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
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    downloadContent(); //volame metodu pre stiahnutie obsahu stranok
                } else {
                    Toast.makeText(MainActivity.this, "Pripojenie k sieti nie je dostupne.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // vytvori sa adapter s aktualnymi hodnotami a priradi sa do Listu
    private void updateListView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_row1,R.id.text1, listOfValues);
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
        contentContainer = new ArrayList<>();
        for (final String item: listOfValues){
            new DownloadWebpageTask().execute(item);
        }
        downloaded = true;
        setListener();
        //Toast.makeText(this, "OK - " + !contentContainer.isEmpty(), Toast.LENGTH_SHORT).show();
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

    //-----------podtrieda------------------------------------------------------------------

    private class DownloadWebpageTask extends AsyncTask<String, Void, String>{

        private String address;

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            address = urls[0];
            try {

                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            contentContainer.add(result);
            Toast.makeText(MainActivity.this, "Downloaded: " + address, Toast.LENGTH_SHORT).show();
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 250000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string

                return readIt(is, len);

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            CharBuffer buff = null;
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

    }



}


