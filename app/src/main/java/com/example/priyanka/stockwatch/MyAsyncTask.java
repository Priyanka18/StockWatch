package com.example.priyanka.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by priya on 06-03-2018.
 */

public class MyAsyncTask extends AsyncTask<String, Void, ArrayList<String>>{
    private static final String TAG = "MyAsyncTask";
    private MainActivity mainActivity;
    private HashMap<String, String> dataMap = new HashMap<>();
    private ArrayList<String[]> symList=new ArrayList<>();
    private String str="";

    private final String dataURL = "https://api.iextrading.com/1.0/stock/";
    private final String symbolURL = "http://d.yimg.com/aq/autoc";

    public MyAsyncTask(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPostExecute(ArrayList<String> s){
        if(s.get(0).equals("Stock Symbol")) {
            parseSymbol(s);
            mainActivity.updateSymbol(symList);
        }
        else if(s.get(0).equals("Data")){
            parseJSON(s);
            mainActivity.addStock(dataMap);
        }
        else if(s.equals("noInternet")){
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setMessage("Stocks cannot be added without a network connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setMessage("Stock Symbol "+s.get(0)+" could not be found");
            builder.setTitle("Not found");
            builder.show();
        }
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        Log.d(TAG, "doInBackground");
        ArrayList<String> returnList = new ArrayList<>();
        String url="";

        if(strings[0].equals("Stock Symbol")){
            Uri.Builder buildURL = Uri.parse(symbolURL).buildUpon();
            buildURL.appendQueryParameter("region", "US");
            buildURL.appendQueryParameter("lang", "en-US");
            buildURL.appendQueryParameter("query", strings[1]);
            url = buildURL.build().toString();
            Log.d(TAG, "URL in YES: " + url);
        }
        else if(!strings[0].equals("Stock Symbol")){
            String fullDataURL = dataURL+strings[0]+"/"+"quote";
            Uri.Builder buildURL = Uri.parse(fullDataURL).buildUpon();
            url = buildURL.build().toString();
            Log.d(TAG, "URL in NO: "+ url);
        }
        StringBuilder sb = new StringBuilder();
        try {
            URL urlFinal = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlFinal.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            Log.d(TAG, "Entered Catch block");
            returnList.add(strings[1]);
            e.printStackTrace();
            return returnList;
        }
        str=sb.toString();

        if(!strings[0].equals("Stock Symbol")){
            Log.d(TAG, "Stock Symbol: ? "+strings[0]);
            returnList.add("Data");
        }
        returnList.add(strings[0]);
        returnList.add(strings[1]);
        returnList.add(str);
        Log.d(TAG, "str value into returnList"+ str);
        return returnList;
    }

    public void parseJSON(ArrayList<String> List){
        String symbol = List.get(1);
        String compName = List.get(2);
        String stringJSON = List.get(3);

        try {
            JSONObject tempJSONStock = new JSONObject(stringJSON);
            String symbolStr = tempJSONStock.getString("symbol");
            dataMap.put("SYMBOL",symbol);
            dataMap.put("COMPANY NAME",compName);
            dataMap.put("TPRICE", tempJSONStock.getString("latestPrice"));
            dataMap.put("PRICEAMT", tempJSONStock.getString("change"));
            dataMap.put("PRICEPERC", tempJSONStock.getString("changePercent"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSymbol(ArrayList<String> List){
        String symbol = List.get(1);
        String stringJSON = List.get(2);
        try {
            JSONObject result = new JSONObject(stringJSON);
            String resultString = result.getString("ResultSet");

            JSONObject resultSet = new JSONObject(resultString);
            String resultString2 = resultSet.getString("Result");

            Log.d(TAG,"Result: " + resultString2);

            JSONArray stocksJSON = new JSONArray(resultString2);

            for(int i=0; i<stocksJSON.length(); i++){
                JSONObject tempJSONStock = (JSONObject) stocksJSON.get(i);
                String s[] = new String[2];
                s[0] = tempJSONStock.getString("symbol");
                s[1] = tempJSONStock.getString("name");

                String type = tempJSONStock.getString("type");  //should be of type S - for stocks
                String period[] = s[0].split(Pattern.quote("."));
                if(type.equals("S") && period.length<2) {
                    symList.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
