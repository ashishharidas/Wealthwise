package com.smartfinance.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class APIClient {
    private static final String BASE_URL = "https://yfapi.net";
    private static final String API_KEY = "ppByrs2xVz4DoG2eoSmWq4b3fKo0ir709mHRpRuv";
    private static final int TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient;

    public APIClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // ======== Yahoo Finance API Methods (yfapi.net) ========

    /**
     * Fetches stock chart data from yfapi.net
     * @param symbol Stock symbol (e.g., "RELIANCE.NS")
     * @param range Time range (e.g., "1mo", "3mo", "1y")
     * @param interval Data interval (e.g., "1d", "1wk")
     * @return JSON response as String
     */
    public String getYahooFinanceData(String symbol, String range, String interval)
            throws IOException, InterruptedException {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }

        String encodedSymbol = URLEncoder.encode(symbol.trim(), StandardCharsets.UTF_8);
        String endpoint = String.format("/v8/finance/chart/%s?range=%s&interval=%s",
                encodedSymbol, range, interval);

        return sendRequest(endpoint);
    }

    /**
     * Fetches real-time quote for a stock symbol
     * @param symbol Stock symbol
     * @return JSON response as String
     */
    public String getYahooQuote(String symbol) throws IOException, InterruptedException {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }

        String encodedSymbol = URLEncoder.encode(symbol.trim(), StandardCharsets.UTF_8);
        String endpoint = String.format("/v6/finance/quote?symbols=%s", encodedSymbol);

        return sendRequest(endpoint);
    }

    /**
     * Fetches market summary data
     * @return JSON response as String
     */
    public String getMarketSummary() throws IOException, InterruptedException {
        return sendRequest("/v6/finance/quote/marketSummary");
    }

    /**
     * Fetches trending stocks for a region
     * @param region Region code (e.g., "US", "IN")
     * @return JSON response as String
     */
    public String getTrendingStocks(String region) throws IOException, InterruptedException {
        String endpoint = String.format("/v1/finance/trending/%s", region);
        return sendRequest(endpoint);
    }

    /**
     * Fetches multiple stock quotes in one request
     * @param symbols Array of stock symbols
     * @return JSON response as String
     */
    public String getMultipleQuotes(String[] symbols) throws IOException, InterruptedException {
        if (symbols == null || symbols.length == 0) {
            throw new IllegalArgumentException("At least one symbol is required");
        }

        String symbolsParam = String.join(",", symbols);
        String encodedSymbols = URLEncoder.encode(symbolsParam, StandardCharsets.UTF_8);
        String endpoint = String.format("/v6/finance/quote?symbols=%s", encodedSymbols);

        return sendRequest(endpoint);
    }

    /**
     * Fetches stock statistics (PE ratio, market cap, etc.)
     * @param symbol Stock symbol
     * @return JSON response as String
     */
    public String getStockStatistics(String symbol) throws IOException, InterruptedException {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }

        String encodedSymbol = URLEncoder.encode(symbol.trim(), StandardCharsets.UTF_8);
        String endpoint = String.format("/v11/finance/quoteSummary/%s?modules=defaultKeyStatistics,financialData",
                encodedSymbol);

        return sendRequest(endpoint);
    }

    // ======== Shared Utility Methods ========

    private HttpRequest buildRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("x-api-key", API_KEY)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .GET()
                .build();
    }

    private String executeRequest(HttpRequest request) throws IOException, InterruptedException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return response.body();
            } else if (statusCode == 401 || statusCode == 403) {
                throw new IOException("Authentication failed. Please check your API key.");
            } else if (statusCode == 404) {
                throw new IOException("Symbol not found or endpoint does not exist: " + request.uri());
            } else if (statusCode == 429) {
                throw new IOException("Rate limit exceeded. Please try again later or upgrade your API plan.");
            } else if (statusCode >= 500) {
                throw new IOException("yfapi.net service error (status " + statusCode + "). Please try again later.");
            } else {
                throw new IOException("API call failed with status " + statusCode + ": " + response.body());
            }
        } catch (UnresolvedAddressException ex) {
            throw new IOException("Unable to resolve host 'yfapi.net'. Please check your internet connection.", ex);
        } catch (java.net.http.HttpTimeoutException ex) {
            throw new IOException("Request timed out after " + TIMEOUT_SECONDS + " seconds. The API may be slow or unavailable.", ex);
        } catch (java.net.ConnectException ex) {
            throw new IOException("Failed to connect to yfapi.net. Please check your internet connection.", ex);
        }
    }

    private String sendRequest(String endpoint) throws IOException, InterruptedException {
        return executeRequest(buildRequest(endpoint));
    }

    /**
     * Test connection to yfapi.net API
     * @return true if connection is successful and API key is valid
     */
    public boolean testConnection() {
        try {
            System.out.println("Testing connection to yfapi.net...");
            getMarketSummary();
            System.out.println("✓ Connection successful!");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get API usage information (if supported by yfapi.net)
     * @return JSON response with usage stats
     */
    public String getApiUsage() throws IOException, InterruptedException {
        return sendRequest("/usage");
    }
}