package com.example.priyanka.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    private ArrayList<String[]> stocksArrayList = new ArrayList<>();  // Main content is here
    private List<stocks> userdata = new ArrayList<>();
    private RecyclerView recyclerView; // Layout's recycler view
    private StockAdapter sAdapter; // Data to recycler view adapter
    private SwipeRefreshLayout swiper; // The SwipeRefreshLayout
    private DatabaseHandler databaseHandler;

    private static String baseURL = "http://www.marketwatch.com/investing/stock/";
    private static final int ADD_STOCK = 1;
    private static final String TAG = "MainActivity";
    private static int pos;

    String stockSymbol, companyName;
    double tradePrice, priceChangeAmount, priceChangePercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        sAdapter = new StockAdapter(userdata,this);
        recyclerView.setAdapter(sAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager myLayoutMgr = new LinearLayoutManager(this);
        myLayoutMgr.setReverseLayout(true);
        myLayoutMgr.setStackFromEnd(true);
        recyclerView.setLayoutManager(myLayoutMgr);
        swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

//        for (int i = 0; i < 10; i++) {
//            stocksArrayList.add(new stocks("AMZN","Amazon Inc",1502.85, 2.6, 0.17));
//            sAdapter.notifyDataSetChanged();
//        }

        databaseHandler = new DatabaseHandler(this);
        databaseHandler.dumpDbToLog();
        if (doNetCheck()){
            stocksArrayList = databaseHandler.loadStocks();
//            stocksArrayList.addAll(list);
            for (String [] s : stocksArrayList){
                AsyncCall(s[0], s[1]);
            }
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stocks cannot be added without a network connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
//        sAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }

    private void doRefresh() {
        Log.d(TAG, "doRefresh");
        if (doNetCheck()){
            // refresh all stocks here, get into stockArrayList??
            stocksArrayList = databaseHandler.loadStocks();
            userdata.clear();
            for (String[] s : stocksArrayList){
                AsyncCall(s[0], s[1]);
            }
//            sAdapter.notifyDataSetChanged();
            swiper.setRefreshing(false);
            Toast.makeText(this, "Stocks refreshed!", Toast.LENGTH_SHORT).show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stocks cannot be refreshed without a network connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            swiper.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menuadd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.newstock:
                SearchAndAddStock();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void AsyncCall(String symbolp,String companyNamep){
        MyAsyncTask myAsyncTask=new MyAsyncTask(this);
        myAsyncTask.execute(symbolp,companyNamep);
    }

    public void SearchAndAddStock(){
        Log.d(TAG, "SearchAndAddStock");
        if (doNetCheck()){
            // code here for dialog to search and add a stock
            // Check here for duplicate stock also -- piyer remember to test this
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final EditText et = new EditText(this);
            et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

            et.setInputType(InputType.TYPE_CLASS_TEXT);
            et.setGravity(Gravity.CENTER_HORIZONTAL);

            builder.setView(et);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // open another dialog with the search results OR directly load the stock to the view
                    String input = et.getText().toString();
                    Log.d(TAG,"Input String:" + input);
                    MyAsyncTask myAsyncTask=new MyAsyncTask(MainActivity.this);
                    myAsyncTask.execute("Stock Symbol",input);
                    Toast.makeText(MainActivity.this, "You searched for "+ et.getText(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                }
            });

            builder.setMessage("Please enter a stock symbol:");
            builder.setTitle("Stock Selection");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stocks cannot be added without a network connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        pos = recyclerView.getChildLayoutPosition(v);
        Log.d(TAG, "pos: "+ pos);
        stocks n = userdata.get(pos);

        if (doNetCheck()){
            String address = n.getStockSymbol();
            String fullURL = baseURL + address;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(fullURL));
            startActivity(i);
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You cannot open browser without a network connection");
            builder.setTitle("No Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public boolean doNetCheck(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void addStock(HashMap<String,String> dataMap){
        String symbol=dataMap.get("SYMBOL");
        if(duplicateSymbol(symbol)){    //error message in case of adding duplicate stock
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stock " +symbol+ "is already on the list");
            builder.setTitle("Duplicate Stock");
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            String compName = dataMap.get("COMPANY NAME");
            String tPrice = dataMap.get("TPRICE");
            String priceChangeAmt = dataMap.get("PRICEAMT");
            String priceChangePerc = dataMap.get("PRICEPERC");

            stocks s = new stocks(symbol, compName, tPrice, priceChangeAmt, priceChangePerc);
            userdata.add(s);
            //sort list
            Collections.sort(userdata, new Comparator<stocks>() {
                @Override
                public int compare(stocks s1, stocks s2) {
                    return (s1.getStockSymbol().compareToIgnoreCase(s2.getStockSymbol()));
                }
            });
            databaseHandler.addStock(s);
            initialView();
        }
    }

    public boolean duplicateSymbol(String sym){
        for(stocks s : userdata){
            if(s.getStockSymbol().equals(sym))
                return true;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) { // Code to delete
        final int pos = recyclerView.getChildLayoutPosition(v);
        stocks n = userdata.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteWithIndex(pos);
                Toast.makeText(MainActivity.this, "Stock Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setMessage("Are you sure you want to delete?");
        builder.setTitle("Delete");
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    public void deleteWithIndex(int i){
        if(!stocksArrayList.isEmpty()) {
            databaseHandler.deleteStock(userdata.get(i).getStockSymbol());
            userdata.remove(i);
            sAdapter.notifyDataSetChanged();
        }
    }

    public void updateSymbol(ArrayList<String[]> listData) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        int size = listData.size();
        if(size > 1){
            final ArrayAdapter<String> choice = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
            for (String[] s : listData) {
                Log.d(TAG, "Found data");
                choice.add(s[0] + "-" + s[1]);
            }
            builder.setAdapter(choice, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String s = choice.getItem(which);
                    String p[] = s.split("-", 2);
                    AsyncCall(p[0], p[1]);
                }
            });
            builder.setTitle("Make a selection");
            builder.show();
        }
        else if (size == 1){
            String[] s = listData.get(0);
            AsyncCall(s[0],s[1]);
        }
        if (size == 0){
            Toast.makeText(this,"No stocks found for the provided symbol",Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
            builder2.setMessage("No matching stocks found");
            builder2.setTitle("Symbol Not Found");
            AlertDialog dialog = builder2.create();
            dialog.show();
        }
    }

    public void initialView(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        sAdapter= new StockAdapter(userdata,this);
        recyclerView.setAdapter(sAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager myLayoutMgr = new LinearLayoutManager(this);
        myLayoutMgr.setReverseLayout(true);
        myLayoutMgr.setStackFromEnd(true);
        recyclerView.setLayoutManager(myLayoutMgr);
        swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
        sAdapter.notifyDataSetChanged();
    }
}