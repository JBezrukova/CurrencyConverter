package com.example.bezru.converter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class GettingValuesAsyncTask extends android.os.AsyncTask<Void, Void, List<String>> {

    static InputStream is = null;
    static JSONObject jObj;


    @Override
    protected List<String> doInBackground(Void... voids) {

        String myURlToGetCurrencyList = "https://free.currencyconverterapi.com/api/v6/currencies";
        List<String> listOfCurrency = null;
        try {
            URL url = new URL(myURlToGetCurrencyList);
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
            jObj = new JSONObject(jsonString);
            JSONObject array = jObj.getJSONObject("results");
            JSONArray names = array.names();
            if (array != null) {
                listOfCurrency = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject((String) names.get(i));
                    listOfCurrency.add(jsonObject.getString("id"));
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listOfCurrency;
    }

}
