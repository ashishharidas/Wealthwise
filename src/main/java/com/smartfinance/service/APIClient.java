package com.smartfinance.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import java.nio.channels.UnresolvedAddressException;

public class APIClient {
    private static final String BASE_URL = "https://stock.indianapi.in";
    private static final String API_KEY = "sk-live-Izif4SjUfYWDjviQKV1f9PdzbqSwwfzdiyQplGFT"; // User's API key
    private static final String ALPHA_VANTAGE_BASE_URL = "https://www.alphavantage.co/query";
    private static final String ALPHA_VANTAGE_API_KEY = System.getenv("ALPHA_VANTAGE_API_KEY");

    private final HttpClient httpClient;

    public APIClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private HttpRequest buildRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("x-api-key", API_KEY)
                .header("Content-Type", "application/json")
                .build();
    }

    private HttpRequest buildAlphaVantageRequest(String function) {
        String url = ALPHA_VANTAGE_BASE_URL + "?function=" + function;
        if (ALPHA_VANTAGE_API_KEY != null && !ALPHA_VANTAGE_API_KEY.isEmpty()) {
            url += "&apikey=" + ALPHA_VANTAGE_API_KEY;
        }
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
    }

    private String sendRequest(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = buildRequest(endpoint);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new IOException("API call to " + endpoint + " failed with status " + response.statusCode());
        } catch (UnresolvedAddressException ex) {
            throw new IOException("Unable to resolve host '" + BASE_URL + "'. Check network connectivity or the configured API URL.", ex);
        }
    }

    private String sendAlphaVantageRequest(String function) throws IOException, InterruptedException {
        if (ALPHA_VANTAGE_API_KEY == null || ALPHA_VANTAGE_API_KEY.isBlank()) {
            throw new IOException("Alpha Vantage API key not configured. Set ALPHA_VANTAGE_API_KEY environment variable.");
        }
        HttpRequest request = buildAlphaVantageRequest(function);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new IOException("Alpha Vantage call to function " + function + " failed with status " + response.statusCode());
    }

    public String getAlphaVantageTopGainersLosers() throws IOException, InterruptedException {
        return sendAlphaVantageRequest("TOP_GAINERS_LOSERS");
    }

    public String getTrendingStocks() throws IOException, InterruptedException {
        return sendRequest("/trending");
    }

    public String getNSEMostActive() throws IOException, InterruptedException {
        return sendRequest("/NSE_most_active");
    }

    public String getStockDetails(String symbol) throws IOException, InterruptedException {
        return sendRequest("/stock?symbol=" + symbol);
    }

    public String getHistoricalData(String symbol, String period) throws IOException, InterruptedException {
        // Assuming period like "1M", "3M", etc.
        return sendRequest("/historical_data?symbol=" + symbol + "&period=" + period);
    }
}