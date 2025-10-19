package com.smartfinance.service;

import com.smartfinance.Models.Model;
import com.smartfinance.Models.RiskProfile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InvestmentService {

    public static class StockSuggestion {
        public String companyName;
        public String symbol;
        public double price;
        public double percentChange;

        public StockSuggestion(String companyName, String symbol, double price, double percentChange) {
            this.companyName = companyName;
            this.symbol = symbol;
            this.price = price;
            this.percentChange = percentChange;
        }
    }
    private final APIClient apiClient;

    public InvestmentService() {
        this.apiClient = new APIClient();
    }

    public List<StockSuggestion> getStockSuggestions(RiskProfile risk) {
        List<StockSuggestion> suggestions = new ArrayList<>();
        JSONArray data = new JSONArray();

        if (risk == RiskProfile.AGGRESSIVE) {
            try {
                String responseActive = apiClient.getNSEMostActive();
                System.out.println("NSE Most Active Response: " + responseActive);
                data = new JSONArray(responseActive);
            } catch (IOException | InterruptedException | RuntimeException e) {
                System.err.println("Failed to fetch NSE most active: " + e.getMessage());
            }
        }

        if (data.length() == 0) {
            try {
                String alphaResponse = apiClient.getAlphaVantageTopGainersLosers();
                System.out.println("Alpha Vantage TOP_GAINERS_LOSERS Response: " + alphaResponse);
                JSONObject alphaJson = new JSONObject(alphaResponse);
                JSONArray topGainers = alphaJson.optJSONArray("top_gainers");
                JSONArray topLosers = alphaJson.optJSONArray("top_losers");
                JSONArray mostActivelyTraded = alphaJson.optJSONArray("most_actively_traded");
                data = buildAlphaSelection(risk, topGainers, topLosers, mostActivelyTraded);
            } catch (IOException | InterruptedException | RuntimeException alphaException) {
                System.err.println("Alpha Vantage data unavailable: " + alphaException.getMessage());
            }
        }

        if (data.length() == 0) {
            System.err.println("No live market data available; using offline suggestions for " + risk + " profile.");
            suggestions.addAll(createFallbackSuggestions(risk));
            return suggestions;
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject stock = data.getJSONObject(i);
            String companyName = stock.optString("company",
                    stock.optString("company_name",
                            stock.optString("ticker", stock.optString("symbol", "Unknown Company"))));
            String symbol = stock.optString("ticker",
                    stock.optString("symbol",
                            stock.optString("ric", "Unknown Symbol")));
            double price = stock.optDouble("price",
                    stock.optDouble("price_current",
                            stock.optDouble("last_price", 0.0)));
            double percentChange = stock.optDouble("percent_change",
                    stock.optDouble("change_percentage",
                            stock.optDouble("percent_change_1d",
                                    stock.optDouble("change", 0.0))));
            suggestions.add(new StockSuggestion(companyName, symbol, price, percentChange));
        }

        return suggestions;
    }

    private JSONArray buildAlphaSelection(RiskProfile risk, JSONArray topGainers, JSONArray topLosers, JSONArray mostActivelyTraded) {
        JSONArray selection = new JSONArray();
        switch (risk) {
            case CONSERVATIVE -> {
                appendLimited(selection, topLosers, 6);
                appendLimited(selection, topGainers, 10);
            }
            case MODERATE -> {
                appendLimited(selection, topGainers, 8);
                appendLimited(selection, topLosers, 14);
                appendLimited(selection, mostActivelyTraded, 18);
            }
            case AGGRESSIVE -> {
                appendLimited(selection, topGainers, 8);
                appendLimited(selection, mostActivelyTraded, 12);
            }
        }
        return selection;
    }

    private void appendLimited(JSONArray target, JSONArray source, int maxSize) {
        if (source == null) {
            return;
        }
        for (int i = 0; i < source.length() && target.length() < maxSize; i++) {
            target.put(source.getJSONObject(i));
        }
    }

    private List<StockSuggestion> createFallbackSuggestions(RiskProfile risk) {
        List<StockSuggestion> defaults = new ArrayList<>();
        switch (risk) {
            case CONSERVATIVE -> {
                defaults.add(new StockSuggestion("HDFC Bank Ltd", "HDFCBANK", 1550.00, 0.45));
                defaults.add(new StockSuggestion("Infosys Ltd", "INFY", 1405.00, 0.30));
                defaults.add(new StockSuggestion("ITC Ltd", "ITC", 440.00, 0.25));
            }
            case MODERATE -> {
                defaults.add(new StockSuggestion("Reliance Industries Ltd", "RELIANCE", 2435.00, 0.65));
                defaults.add(new StockSuggestion("Tata Consultancy Services", "TCS", 3550.00, 0.55));
                defaults.add(new StockSuggestion("Larsen & Toubro Ltd", "LT", 3330.00, 0.75));
            }
            case AGGRESSIVE -> {
                defaults.add(new StockSuggestion("Adani Enterprises Ltd", "ADANIENT", 2800.00, 1.25));
                defaults.add(new StockSuggestion("Tata Motors Ltd", "TATAMOTORS", 720.00, 1.05));
                defaults.add(new StockSuggestion("State Bank of India", "SBIN", 570.00, 0.95));
            }
        }
        return defaults;
    }

    public List<Double> getHistoricalPrices(String symbol, String period) {
        List<Double> prices = new ArrayList<>();
        try {
            String response = apiClient.getHistoricalData(symbol, period); // Use period parameter
            System.out.println("Historical Data Response: " + response);
            JSONObject json = new JSONObject(response);

            if (json.has("datasets")) {
                JSONArray datasets = json.getJSONArray("datasets");
                if (datasets.length() > 0) {
                    JSONObject priceDataset = datasets.getJSONObject(0); // Assuming first is price
                    JSONArray values = priceDataset.getJSONArray("values");
                    for (int i = 0; i < values.length(); i++) {
                        JSONArray point = values.getJSONArray(i);
                        prices.add(Double.parseDouble(point.getString(1)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prices;
    }
}