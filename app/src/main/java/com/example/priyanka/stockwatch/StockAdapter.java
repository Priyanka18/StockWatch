package com.example.priyanka.stockwatch;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.SortedMap;

/**
 * Created by priya on 03-03-2018.
 */

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private static final String TAG = "StockAdapter";
    private List<stocks> StockList;
    private MainActivity mainAct;

    public StockAdapter(List<stocks> sList, MainActivity ma) {
        this.StockList = sList;
        mainAct = ma;
    }

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_stockitems, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int pos) {
        stocks stocks = StockList.get(pos);
        String temp = stocks.getPriceChangeValue();
        holder.symbol.setText(stocks.getStockSymbol());
        holder.companyName.setText(stocks.getCompanyName());
        holder.tPrice.setText(stocks.getTradePrice());
        holder.changePrice.setText(temp);
//        holder.priceChangePercentage.setText(Double.toString(stocks.getPriceChangePercentage()) + "%");

        //something for the icons
//        holder.stockSymbol.setTextColor(mainAct.getResources().getColor(R.color.stockUp));

        if(temp.charAt(0)=='-') {
            holder.symbol.setTextColor(mainAct.getResources().getColor(R.color.stockDown));
            holder.companyName.setTextColor(mainAct.getResources().getColor(R.color.stockDown));
            holder.tPrice.setTextColor(mainAct.getResources().getColor(R.color.stockDown));
            holder.changePrice.setTextColor(mainAct.getResources().getColor(R.color.stockDown));
            Drawable draw=mainAct.getResources().getDrawable(R.drawable.down);
            draw.setBounds(0,0,40,30);
            holder.changePrice.setCompoundDrawables(draw,null,null,null);
        }
        else if (temp.charAt(0)=='+'){
            holder.symbol.setTextColor(mainAct.getResources().getColor(R.color.stockUp));
            holder.companyName.setTextColor(mainAct.getResources().getColor(R.color.stockUp));
            holder.tPrice.setTextColor(mainAct.getResources().getColor(R.color.stockUp));
            holder.changePrice.setTextColor(mainAct.getResources().getColor(R.color.stockUp));
            Drawable draw=mainAct.getResources().getDrawable(R.drawable.up);
            draw.setBounds(0,0,40,30);
            holder.changePrice.setCompoundDrawables(draw,null,null,null);
        }
    }

    @Override
    public int getItemCount() {
        return StockList.size();
    }
}