package com.smartfinance.service;

import com.smartfinance.Models.RiskProfile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InvestmentService {

    private static final int MAX_RECOMMENDATIONS = 5;
    private static final int MAX_TRENDING = 15;
    private static final String DEFAULT_HISTORY_PERIOD = "1M";
    private static final double TRADING_DAYS_PER_YEAR = 252.0;

    public static class StockSuggestion {
        public String companyName;
        public String symbol;
        public double price;
        public double percentChange;
        public double volatility;
        public double momentum;
        public double suitabilityScore;
        public RiskProfile recommendedFor;

        public StockSuggestion(String companyName, String symbol, double price, double percentChange) {
            this.companyName = companyName;
            this.symbol = symbol;
            this.price = price;
            this.percentChange = percentChange;
            this.volatility = Double.NaN;
            this.momentum = Double.NaN;
            this.suitabilityScore = 0.0;
            this.recommendedFor = null;
        }

        public void calculateSuitability(RiskProfile profile) {
            if (Double.isFinite(volatility) && Double.isFinite(momentum)) {
                this.suitabilityScore = profile.calculateSuitabilityScore(volatility, momentum);
                this.recommendedFor = profile;
            }
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - ₹%.2f [%+.2f%%] Vol:%.2f Mom:%.2f Score:%.0f",
                    companyName, symbol, price, percentChange * 100,
                    volatility, momentum, suitabilityScore);
        }
    }

    private final APIClient apiClient;
    private final Map<String, JSONObject> quoteCache = new HashMap<>();
    private final Map<String, List<Double>> priceCache = new HashMap<>();

    public InvestmentService() {
        this.apiClient = new APIClient();
    }

    // --------------------------- STOCK SUGGESTIONS ---------------------------

    /**
     * Get personalized stock suggestions based on risk profile
     */
    public List<StockSuggestion> getStockSuggestions(RiskProfile risk) {
        System.out.println("Fetching suggestions for " + risk + " profile...");

        List<StockSuggestion> candidates = new ArrayList<>();

        try {
            JSONArray data = fetchYahooTopMovers();
            if (data.isEmpty()) {
                System.out.println("No live data available, using fallback.");
                return createFallbackSuggestions(risk);
            }

            candidates = enrichStockDataWithAnalytics(data, risk);

        } catch (Exception e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
            return createFallbackSuggestions(risk);
        }

        // Filter and rank by suitability score
        List<StockSuggestion> suitable = candidates.stream()
                .filter(s -> s.suitabilityScore >= 40.0)
                .sorted(Comparator.comparingDouble((StockSuggestion s) -> s.suitabilityScore).reversed())
                .limit(MAX_RECOMMENDATIONS)
                .collect(Collectors.toList());

        if (suitable.isEmpty()) {
            System.out.println("No suitable matches found, using fallback.");
            return createFallbackSuggestions(risk);
        }

        System.out.println("Found " + suitable.size() + " suitable suggestions.");
        return suitable;
    }

    private List<StockSuggestion> enrichStockDataWithAnalytics(JSONArray data, RiskProfile risk) {
        List<StockSuggestion> enriched = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject stock = data.optJSONObject(i);
            if (stock == null) continue;

            StockSuggestion suggestion = mapYahooEntryToStockSuggestion(stock);
            if (suggestion == null) continue;

            // Calculate analytics
            suggestion.volatility = calculateAnnualizedVolatility(suggestion.symbol);
            suggestion.momentum = calculateMomentum(suggestion.symbol);

            // Score against risk profile
            suggestion.calculateSuitability(risk);

            if (suggestion.suitabilityScore > 0) {
                enriched.add(suggestion);
            }
        }

        return enriched;
    }

    // --------------------------- TRENDING STOCKS ---------------------------

    public List<StockSuggestion> getTrendingStocks() {
        List<StockSuggestion> trending = new ArrayList<>();

        try {
            JSONArray data = fetchYahooTopMovers();

            for (int i = 0; i < data.length() && trending.size() < MAX_TRENDING; i++) {
                JSONObject stock = data.optJSONObject(i);
                if (stock == null) continue;

                StockSuggestion suggestion = mapYahooEntryToStockSuggestion(stock);
                if (suggestion == null) continue;

                suggestion.volatility = calculateAnnualizedVolatility(suggestion.symbol);
                suggestion.momentum = calculateMomentum(suggestion.symbol);

                // Score against all profiles to find best match
                double bestScore = 0;
                RiskProfile bestProfile = null;
                for (RiskProfile profile : RiskProfile.values()) {
                    double score = profile.calculateSuitabilityScore(suggestion.volatility, suggestion.momentum);
                    if (score > bestScore) {
                        bestScore = score;
                        bestProfile = profile;
                    }
                }

                suggestion.suitabilityScore = bestScore;
                suggestion.recommendedFor = bestProfile;
                trending.add(suggestion);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch trending stocks: " + e.getMessage());
            trending.addAll(createFallbackSuggestions(RiskProfile.MODERATE));
        }

        return trending.stream()
                .sorted(Comparator.comparingDouble((StockSuggestion s) -> Math.abs(s.percentChange)).reversed())
                .collect(Collectors.toList());
    }

    // --------------------------- INDIVIDUAL STOCK ---------------------------

    public StockSuggestion getStockDetails(String symbol) {
        try {
            JSONObject quote = fetchYahooQuote(symbol);
            StockSuggestion suggestion = mapYahooEntryToStockSuggestion(quote);

            if (suggestion == null) {
                return findFallbackSuggestion(symbol);
            }

            suggestion.volatility = calculateAnnualizedVolatility(symbol);
            suggestion.momentum = calculateMomentum(symbol);

            // Find best matching risk profile
            double bestScore = 0;
            RiskProfile bestProfile = null;
            for (RiskProfile profile : RiskProfile.values()) {
                double score = profile.calculateSuitabilityScore(suggestion.volatility, suggestion.momentum);
                if (score > bestScore) {
                    bestScore = score;
                    bestProfile = profile;
                }
            }

            suggestion.suitabilityScore = bestScore;
            suggestion.recommendedFor = bestProfile;

            return suggestion;

        } catch (Exception e) {
            System.err.println("Failed to fetch details for " + symbol + ": " + e.getMessage());
            return findFallbackSuggestion(symbol);
        }
    }

    // --------------------------- ANALYTICS ---------------------------

    private double calculateAnnualizedVolatility(String symbol) {
        List<Double> prices = getHistoricalPrices(symbol, DEFAULT_HISTORY_PERIOD);
        if (prices.size() < 2) return Double.NaN;

        List<Double> logReturns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1);
            double curr = prices.get(i);
            if (prev > 0 && curr > 0) {
                logReturns.add(Math.log(curr / prev));
            }
        }

        if (logReturns.size() < 2) return Double.NaN;

        double mean = logReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = logReturns.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / (logReturns.size() - 1);

        return Math.sqrt(variance) * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    private double calculateMomentum(String symbol) {
        List<Double> prices = getHistoricalPrices(symbol, DEFAULT_HISTORY_PERIOD);
        if (prices.size() < 2) return Double.NaN;

        double oldest = prices.get(0);
        double latest = prices.get(prices.size() - 1);
        return (latest - oldest) / oldest;
    }

    public List<Double> getHistoricalPrices(String symbol, String period) {
        String cacheKey = symbol + "_" + period;
        if (priceCache.containsKey(cacheKey)) {
            return priceCache.get(cacheKey);
        }

        try {
            JSONArray closes = fetchYahooHistoricalSeries(symbol, mapPeriodToRange(period), "1d");
            List<Double> prices = new ArrayList<>();

            for (int i = 0; i < closes.length(); i++) {
                double val = closes.optDouble(i, Double.NaN);
                if (Double.isFinite(val)) {
                    prices.add(val);
                }
            }

            if (prices.size() >= 2) {
                priceCache.put(cacheKey, prices);
                return prices;
            } else {
                throw new IOException("Insufficient price data for " + symbol);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch historical prices for " + symbol + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // --------------------------- YAHOO FINANCE FETCH ---------------------------

    private JSONArray fetchYahooTopMovers() throws IOException, InterruptedException {
        // Indian stocks watchlist
        List<String> watchlist = List.of(
                "RELIANCE.NS", "TCS.NS", "INFY.NS", "HDFCBANK.NS", "ITC.NS",
                "HINDUNILVR.NS", "BHARTIARTL.NS", "SBIN.NS", "ICICIBANK.NS", "LT.NS"
        );

        JSONArray combined = new JSONArray();
        Set<String> seen = new LinkedHashSet<>();

        for (String symbol : watchlist) {
            try {
                JSONObject quote = fetchYahooQuote(symbol);
                if (quote == null) continue;

                String normalized = quote.optString("symbol", symbol).toUpperCase(Locale.ROOT);
                if (seen.add(normalized)) {
                    combined.put(quote);
                }
            } catch (Exception e) {
                System.err.println("Skipping " + symbol + ": " + e.getMessage());
            }
        }

        return combined;
    }

    private JSONObject fetchYahooQuote(String symbol) throws IOException, InterruptedException {
        if (symbol == null || symbol.isBlank()) {
            throw new IOException("Symbol is required");
        }

        if (quoteCache.containsKey(symbol)) {
            return quoteCache.get(symbol);
        }

        JSONObject chart = requestYahooFinanceChart(symbol, "1mo", "1d");
        JSONArray results = chart.optJSONArray("result");

        if (results == null || results.isEmpty()) {
            throw new IOException("No data returned for " + symbol);
        }

        JSONObject firstResult = results.optJSONObject(0);
        JSONObject meta = firstResult.optJSONObject("meta");
        JSONArray closes = extractCloseSeries(firstResult);

        double lastClose = extractLastClose(closes);
        double firstClose = extractFirstClose(closes);

        JSONObject normalized = new JSONObject();
        normalized.put("symbol", meta.optString("symbol", symbol));
        normalized.put("name", meta.optString("longName", meta.optString("symbol", symbol)));
        normalized.put("price", lastClose);
        normalized.put("change_percentage", computeChangePercent(firstClose, lastClose));
        normalized.put("closeSeries", closes);

        quoteCache.put(symbol, normalized);
        return normalized;
    }

    private JSONObject requestYahooFinanceChart(String symbol, String range, String interval)
            throws IOException, InterruptedException {
        String response = apiClient.getYahooFinanceData(symbol, range, interval);
        JSONObject root = new JSONObject(response);
        JSONObject chart = root.optJSONObject("chart");

        if (chart == null) {
            throw new IOException("Invalid Yahoo Finance response");
        }

        return chart;
    }

    private JSONArray extractCloseSeries(JSONObject chartResult) {
        if (chartResult == null) return new JSONArray();

        JSONObject indicators = chartResult.optJSONObject("indicators");
        if (indicators == null) return new JSONArray();

        JSONArray quotes = indicators.optJSONArray("quote");
        if (quotes == null || quotes.isEmpty()) return new JSONArray();

        JSONObject firstQuote = quotes.optJSONObject(0);
        if (firstQuote == null) return new JSONArray();

        JSONArray closes = firstQuote.optJSONArray("close");
        if (closes == null) return new JSONArray();

        JSONArray sanitized = new JSONArray();
        for (int i = 0; i < closes.length(); i++) {
            double value = closes.optDouble(i, Double.NaN);
            if (Double.isFinite(value)) {
                sanitized.put(value);
            }
        }
        return sanitized;
    }

    private JSONArray fetchYahooHistoricalSeries(String symbol, String range, String interval)
            throws IOException, InterruptedException {
        JSONObject chart = requestYahooFinanceChart(symbol, range, interval);
        JSONArray results = chart.optJSONArray("result");

        if (results == null || results.isEmpty()) {
            throw new IOException("No historical data for " + symbol);
        }

        return extractCloseSeries(results.optJSONObject(0));
    }

    // --------------------------- UTILS ---------------------------

    private double extractFirstClose(JSONArray closes) {
        return closes != null && closes.length() > 0 ? closes.optDouble(0, Double.NaN) : Double.NaN;
    }

    private double extractLastClose(JSONArray closes) {
        return closes != null && closes.length() > 0
                ? closes.optDouble(closes.length() - 1, Double.NaN)
                : Double.NaN;
    }

    private double computeChangePercent(double first, double last) {
        if (!Double.isFinite(first) || !Double.isFinite(last) || first == 0) {
            return Double.NaN;
        }
        return (last - first) / first;
    }

    private StockSuggestion mapYahooEntryToStockSuggestion(JSONObject stock) {
        if (stock == null) return null;

        String symbol = stock.optString("symbol", "").trim();
        if (symbol.isEmpty()) return null;

        double price = stock.optDouble("price", Double.NaN);
        if (!Double.isFinite(price)) return null;

        double percentChange = stock.optDouble("change_percentage", 0.0);
        String name = stock.optString("name", symbol);

        return new StockSuggestion(name, symbol.toUpperCase(Locale.ROOT), price, percentChange);
    }

    private String mapPeriodToRange(String period) {
        String normalized = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1m" -> "1mo";
            case "3m" -> "3mo";
            case "6m" -> "6mo";
            case "1y" -> "1y";
            case "5y" -> "5y";
            case "max" -> "max";
            default -> "1mo";
        };
    }

    // --------------------------- FALLBACK ---------------------------

    private List<StockSuggestion> createFallbackSuggestions(RiskProfile risk) {
        List<StockSuggestion> defaults = new ArrayList<>();

        switch (risk) {
            case CONSERVATIVE -> {
                defaults.add(createFallbackStock("HDFC Bank Ltd", "HDFCBANK.NS", 1550.00, 0.0045, 0.18, 0.08));
                defaults.add(createFallbackStock("Infosys Ltd", "INFY.NS", 1405.00, 0.0030, 0.20, 0.06));
                defaults.add(createFallbackStock("ITC Ltd", "ITC.NS", 440.00, 0.0025, 0.15, 0.05));
                defaults.add(createFallbackStock("HUL", "HINDUNILVR.NS", 2450.00, 0.0028, 0.16, 0.07));
                defaults.add(createFallbackStock("Nestle India", "NESTLEIND.NS", 2350.00, 0.0032, 0.17, 0.06));
            }
            case MODERATE -> {
                defaults.add(createFallbackStock("Reliance Industries", "RELIANCE.NS", 2435.00, 0.0065, 0.30, 0.12));
                defaults.add(createFallbackStock("TCS", "TCS.NS", 3550.00, 0.0055, 0.25, 0.10));
                defaults.add(createFallbackStock("Larsen & Toubro", "LT.NS", 3330.00, 0.0075, 0.32, 0.14));
                defaults.add(createFallbackStock("ICICI Bank", "ICICIBANK.NS", 950.00, 0.0068, 0.28, 0.11));
                defaults.add(createFallbackStock("Bharti Airtel", "BHARTIARTL.NS", 1280.00, 0.0070, 0.29, 0.13));
            }
            case AGGRESSIVE -> {
                defaults.add(createFallbackStock("Adani Enterprises", "ADANIENT.NS", 2800.00, 0.0125, 0.45, 0.20));
                defaults.add(createFallbackStock("Tata Motors", "TATAMOTORS.NS", 720.00, 0.0105, 0.42, 0.18));
                defaults.add(createFallbackStock("SBI", "SBIN.NS", 570.00, 0.0095, 0.38, 0.16));
                defaults.add(createFallbackStock("Bajaj Finance", "BAJFINANCE.NS", 6850.00, 0.0115, 0.40, 0.19));
                defaults.add(createFallbackStock("Zomato", "ZOMATO.NS", 145.00, 0.0135, 0.48, 0.22));
            }
        }

        // Calculate suitability scores for fallback stocks
        for (StockSuggestion stock : defaults) {
            stock.calculateSuitability(risk);
        }

        return defaults;
    }

    private StockSuggestion createFallbackStock(String name, String symbol, double price,
                                                double percentChange, double volatility, double momentum) {
        StockSuggestion stock = new StockSuggestion(name, symbol, price, percentChange);
        stock.volatility = volatility;
        stock.momentum = momentum;
        return stock;
    }

    private StockSuggestion findFallbackSuggestion(String symbol) {
        if (symbol == null || symbol.isBlank()) return null;
        String normalized = symbol.trim().toUpperCase(Locale.ROOT);

        for (RiskProfile profile : RiskProfile.values()) {
            for (StockSuggestion s : createFallbackSuggestions(profile)) {
                if (normalized.equalsIgnoreCase(s.symbol)) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Clear all caches - useful for refreshing data
     */
    public void clearCache() {
        quoteCache.clear();
        priceCache.clear();
        System.out.println("Cache cleared successfully.");
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Cache Stats - Quotes: %d, Prices: %d",
                quoteCache.size(), priceCache.size());
    }
}