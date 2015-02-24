package com.corp.topo.copyhtml;

import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Fanthomo on 23. 2. 2015.
 */
public class ContentGetter {

    HttpClient httpclient;

    public ContentGetter(){
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,3000); // 3s max for connection
        HttpConnectionParams.setSoTimeout(httpParameters, 4000); // 4s max to get data
        this.httpclient = new DefaultHttpClient(httpParameters); // Create HTTP Client
    }

    public String getContent(String address){
        HttpGet httpget = new HttpGet(address); // Set the action you want to do
        String resString = null;
        HttpResponse response; // Executeit
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent(); // Create an InputStream with the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) // Read line by line
                sb.append(line + "\n");

            resString = sb.toString(); // Result is here

            is.close(); // Close the stream
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (resString!=null) return resString;
        else return "404: Page not Found.";

    }

}
