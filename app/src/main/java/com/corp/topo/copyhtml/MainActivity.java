package com.corp.topo.copyhtml;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends Activity {

    public static final String CopyHTML_content = "com.corp.topo.copyhtml.content";
    public static final String CopyHTML_content_address = "com.corp.topo.copyhtml.content_address";
    private static final String DEBUG_TAG = "CopyHTML";

    private ListView listView; //view pre zoznam adries
    private ArrayList<String> listOfValues; // zoznam ktory uchovava adresy
    private ArrayList<String> contentContainer; // uchovava obsah webstranky
    private boolean downloaded = false; // indikuje, ci stiahnutie prebehlo

    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //inicializacia zoznamov, widgetov a pohladov...
        listView = (ListView) findViewById(R.id.list);
        Button addAddrBtn = (Button) findViewById(R.id.addAddressButton);
        Button downloadContBtn = (Button) findViewById(R.id.downloadButton);
        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        listOfValues = new ArrayList<>();
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
    //osetrenie zmeny konfiguracie
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // vytvori sa adapter s aktualnymi hodnotami a priradi sa do Listu
    private void updateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_row1, R.id.text1, listOfValues);
        listView.setAdapter(adapter);
    }

    // po vyhodnoteni dialogu sa ako vstupny parameter tejto metody posle adresa z dialogu
    private void addItem(String item) {
        //kontrola, ci retazec nie je prazdny
        if (!item.equals("")) {
            item = addressValidate(item); //pridanie prefixu http:// ak je to porebne
            listOfValues.add(item);
            Toast.makeText(this, "Pridane: " + item, Toast.LENGTH_SHORT).show();
            updateListView();
            downloaded = false; //nastavi indikator stiahnutia na false
        } else {
            Toast.makeText(this, "Nezadal si adresu", Toast.LENGTH_SHORT).show();
        }
    }

    // metoda pre vytvorenie a spracovanie dialogu pre zadanie adresy
    void showDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Zadaj adresu");
        final EditText input = new EditText(this);
        b.setView(input);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

    // validacna metoda pre adresu - prida prefix http
    private String addressValidate(String address) {
        if (address.startsWith("http://")) {
            return address;
        } else {
            return "http://" + address;
        }
    }

    //metoda pre stiahnutie zdrojových kodov stranok
    private void downloadContent() {
        contentContainer = new ArrayList<>();
        for (final String item : listOfValues) {
            // na pozadi sa vykona stiahnutie jednotlivych stranok
            String islast = (listOfValues.get(listOfValues.size() - 1).equals(item)) ? "true" : "false"; // je posledny?
            new DownloadWebpageTask().execute(item, islast);
        }
        setListener();
    }

    // nastavime listener pre kliknutie na adresu pre zobrazenie obsahu
    private void setListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showContent(((TextView) view).getText().toString(), position);
            }
        });
    }

    //otvorime novu aktivitu pre zobrazenie obsahu
    //ako vstup je nazov adresy a pozicia v zozname adries
    private void showContent(String address, int position) {
        if (!downloaded) {
            Toast.makeText(this, "Data nie su stiahnute...", Toast.LENGTH_SHORT).show();
        } else {
            bar.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, ContentViewer.class);
            intent.putExtra(CopyHTML_content, contentContainer.get(position));
            intent.putExtra(CopyHTML_content_address, address);

            startActivity(intent);
            bar.setVisibility(View.GONE);
        }
    }

    //-----------------------------------------------------------------------------
    // tato trieda zabezpecuje stiahnutie obsahu adresy na pozadi
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        private String address;
        private String islast;

        @Override
        protected String doInBackground(String... urls) {

            // params pochadzaju z execute() call: params[0] je url,
            address = urls[0];
            islast = urls[1];

            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPreExecute() {
            //zobrazenie progress baru
            bar.setVisibility(View.VISIBLE);
        }

        // onPostExecute zobrazi/vlozi do zoznamu vysledok z AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            contentContainer.add(result);
            if (islast.equals("true")) {
                downloaded = true; //nastavim indikator stiahnutia
                bar.setVisibility(View.INVISIBLE);
            }
            Toast.makeText(MainActivity.this, "Downloaded: " + address, Toast.LENGTH_SHORT).show();
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // obmedzenie iba na prvych 25 000 ziskanych znakov
            int len = 25000;

            //nadviazanie spojenia
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // spustenie spojenia
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convertujeme InputStream do string
                return readIt(is, len);

                // ubezpecime sa, ze je InputStream ukonceny / zavrety
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // cita InputStream a convertuje ho na  String.
        public String readIt(InputStream stream, int len) throws IOException {
            Reader reader;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

    }


}


