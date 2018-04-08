package com.example.priyanka.stockwatch;

import java.io.Serializable;

/**
 * Created by priya on 03-03-2018.
 */
public class stocks implements Serializable {
        private String stockSymbol;
        private String companyName;
        private String tradePrice; // up and down arrows
        private String priceChangeAmount;
        private String priceChangePercentage;

        public static int cnt = 1;

        public stocks(String stockSymbol, String companyName, String tradePrice, String priceChangeAmount, String priceChangePercentage){
            this.stockSymbol = stockSymbol;
            this.companyName = companyName;
            this.tradePrice = tradePrice;
            this.priceChangeAmount = priceChangeAmount;
            this.priceChangePercentage = priceChangePercentage;
            cnt++;
        }

        public String getStockSymbol(){
            return stockSymbol;
        }
        public String getCompanyName(){
            return companyName;
        }
        public String getTradePrice(){
            return tradePrice;
        }
        public String getPriceChangeValue(){
            return priceChangeAmount + "("+priceChangePercentage+"%)";
        }
//        public double getPriceChangePercentage(){
//            return priceChangePercentage;
//        }
}