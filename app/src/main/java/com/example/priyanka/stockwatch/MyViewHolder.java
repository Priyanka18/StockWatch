package com.example.priyanka.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by priya on 20-02-2018.
 */

public class MyViewHolder extends RecyclerView.ViewHolder{
    public TextView symbol;
    public TextView companyName;
    public TextView tPrice;
    public TextView changePrice;

    public  MyViewHolder(View view){
        super(view);
        symbol = (TextView) view.findViewById(R.id.symbol);
        companyName = (TextView) view.findViewById(R.id.compName);
        tPrice = (TextView) view.findViewById(R.id.tPrice);
        changePrice = (TextView) view.findViewById(R.id.changeAmtPerc);
    }
}
