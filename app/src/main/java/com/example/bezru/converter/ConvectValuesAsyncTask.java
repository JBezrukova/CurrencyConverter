package com.example.bezru.converter;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;

public class ConvectValuesAsyncTask extends AsyncTask<String, Void, String> {

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(String... strings) {
        String from = strings[0];
        String to = strings[1];
        Double value;
        String stringUrl = String.format("https://free.currencyconverterapi.com/api/v6/convert?q=%s_%s&compact=ultra", from, to);
        String result = null;
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            String jsonString = sb.toString();
            JSONObject jObj = new JSONObject(jsonString);
            value = Double.valueOf(jObj.getString(String.valueOf(jObj.names().get(0))));
            result = new DecimalFormat("#0.0000").format(value);

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
