package com.example.priyanka.stockwatch;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.tv.TvView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by priya on 03-03-2018.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHandler";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    // DB Name
    private static final String DATABASE_NAME = "StockDB";

    // DB Table Name
    private static final String TABLE_NAME = "StockWatchTable";

    ///DB Columns
    private static final String SYMBOL = "symbol";
    private static final String COMPANY = "compName";
    private static final String LISTE = "ListingExchange";
    private static final String TPRICE = "tPrice";
    private static final String PRICEAMT = "changeAmt";
    private static final String PRICEPERC = "changePerc";
    // DB Table Create Code
//    private static final String SQL_CREATE_TABLE =
//            "CREATE TABLE " + TABLE_NAME + " (" +
//                    SYMBOL + " TEXT not null unique," +
//                    COMPANY + " TEXT not null, " +
//                    TPRICE + " TEXT not null, " +
//                    PRICEAMT + " TEXT not null, " +
//                    PRICEPERC + " TEXT not null)";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    SYMBOL + " TEXT not null unique," +
                    COMPANY + " TEXT not null)";

    private SQLiteDatabase database;
    private static final String DATABASE_ALTER_TABLE_FOR_V2 = "ALTER TABLE "
            + TABLE_NAME + " ADD COLUMN " + LISTE + " TEXT not null";

    private MainActivity mainActivity;

    public DatabaseHandler(MainActivity context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mainActivity = context;
        database = getWritableDatabase(); // Inherited from SQLiteOpenHelper
        Log.d(TAG, "DatabaseHandler: C'tor DONE");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // onCreate is only called is the DB does not exist
        Log.d(TAG, "onCreate: Making New DB");
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2) {
            db.execSQL(DATABASE_ALTER_TABLE_FOR_V2);
        }
    }

    public ArrayList<String[]> loadStocks() {

        // Load books - return ArrayList of loaded books
        Log.d(TAG, "loadStocks: START");
        ArrayList<String[]> tempstocksArrayList = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,  // The table to query
                new String[]{SYMBOL, COMPANY}, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null); // The sort order

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                String s[]= new String[2];
                s[0] = cursor.getString(0);
                s[1] = cursor.getString(1);
//                stocks b = new stocks(symbol, compName, Double.parseDouble(tprice), Double.parseDouble(priceamt), Double.parseDouble(priceperc));
                tempstocksArrayList.add(s);
                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.d(TAG, "loadStocks: DONE");
        return tempstocksArrayList;
    }

    public void addStock(stocks stocks) {
        ContentValues values = new ContentValues();
        values.put(SYMBOL, stocks.getStockSymbol());
        values.put(COMPANY, stocks.getCompanyName());
        deleteStock(stocks.getStockSymbol());
        long key = database.insert(TABLE_NAME, null, values);
        Log.d(TAG, "addStock: " + key);
    }

//    public void updateStock(stocks stocks) {
//        ContentValues values = new ContentValues();
//
//        values.put(SYMBOL, stocks.getStockSymbol());
//        values.put(COMPANY, stocks.getCompanyName());
//        values.put(TPRICE, stocks.getTradePrice());
//        values.put(PRICEAMT, stocks.getPriceChangeAmount());
//        values.put(PRICEPERC, stocks.getPriceChangePercentage());
//
//        long key = database.update(TABLE_NAME, values, SYMBOL + " = ?", new String[]{stocks.getStockSymbol()});
//        Log.d(TAG, "updateStock: " + key);
//    }

    public void deleteStock(String symbol) {
        Log.d(TAG, "deleteStock: " + symbol);
        int cnt = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[]{symbol});
        Log.d(TAG, "deleteStock: " + cnt);
    }

    public void dumpDbToLog() {
        Log.d(TAG, "dumpDbToLog: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        Cursor cursor = database.rawQuery("select * from " + TABLE_NAME, null);
        Log.d(TAG, "cursor: "+ cursor );
        Log.d(TAG, "count: "+ cursor.getCount() );
        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String compName = cursor.getString(1);
                Log.d(TAG, "dumpDbToLog: " +
                        String.format("%s %-18s", SYMBOL + ":", symbol) +
                        String.format("%s %-18s", COMPANY + ":", compName));
                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.d(TAG, "dumpDbToLog: ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

    public void shutDown() {
        database.close();
    }
}
