package com.smartfinance.Controller.Client;

import com.smartfinance.Models.RiskProfile;
import com.smartfinance.service.InvestmentService;
import com.smartfinance.service.InvestmentService.StockSuggestion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class InvestmentController implements Initializable {

    @FXML
    private ChoiceBox<String> investment_type_choicebox;
    @FXML
    private VBox recommendations_container;

    private InvestmentService investmentService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        investmentService = new InvestmentService();

        // ✅ Populate risk profiles
        investment_type_choicebox.getItems().addAll(
                Arrays.stream(RiskProfile.values())
                        .map(Enum::name)
                        .toList()
        );

        if (!investment_type_choicebox.getItems().isEmpty()) {
            investment_type_choicebox.setValue(investment_type_choicebox.getItems().get(0));
            loadRecommendations(RiskProfile.valueOf(investment_type_choicebox.getValue()));
        }

        // ✅ Listener for profile change
        investment_type_choicebox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadRecommendations(RiskProfile.valueOf(newValue));
            }
        });
    }

    private void loadRecommendations(RiskProfile riskProfile) {
        recommendations_container.getChildren().clear();
        List<StockSuggestion> suggestions = investmentService.getStockSuggestions(riskProfile);
        for (StockSuggestion sug : suggestions) {
            recommendations_container.getChildren().add(createStockContainer(sug));
        }
    }

    private VBox createStockContainer(StockSuggestion sug) {
        VBox container = new VBox(8);
        container.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-padding: 14;
            -fx-background-radius: 10;
            -fx-border-color: #dee2e6;
            -fx-border-radius: 10;
            -fx-border-width: 1;
        """);
        container.setPrefWidth(440);  // ✅ Wider container
        container.setPrefHeight(260); // ✅ Taller container

        Text nameText = new Text(sug.companyName);
        nameText.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-fill: #2c3e50;");

        Text priceText = new Text("Price: ₹" + String.format("%.2f", sug.price));
        priceText.setStyle("-fx-font-size: 11px; -fx-fill: #6c757d;");

        Text changeText = new Text("Change: " + String.format("%.2f", sug.percentChange) + "%");
        String color = sug.percentChange >= 0 ? "#28a745" : "#dc3545";
        changeText.setStyle("-fx-font-size: 11px; -fx-fill: " + color + ";");

        // ✅ Chart Configuration
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Smaller tick label font
        Font tickFont = Font.font(Font.getDefault().getFamily(), 7);
        xAxis.setTickLabelFont(tickFont);
        yAxis.setTickLabelFont(tickFont);

        // Axis labels
        xAxis.setLabel("Time");
        yAxis.setLabel("Price (₹)");

        // ✅ Style axis labels via CSS
        xAxis.setStyle("-fx-font-size: 9px;");
        yAxis.setStyle("-fx-font-size: 9px;");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setPrefWidth(420);  // ✅ Wider chart
        chart.setPrefHeight(170); // ✅ Taller chart
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        List<Double> prices = investmentService.getHistoricalPrices(sug.symbol, "1m");

        try {
            if (!prices.isEmpty()) {
                double minPrice = prices.stream().min(Double::compare).orElse(0.0);
                double maxPrice = prices.stream().max(Double::compare).orElse(100.0);
                yAxis.setLowerBound(minPrice * 0.95);
                yAxis.setUpperBound(maxPrice * 1.05);

                for (int i = 0; i < prices.size(); i++) {
                    series.getData().add(new XYChart.Data<>(i, prices.get(i)));
                }

                chart.getData().add(series);
                String lineColor = sug.percentChange >= 0 ? "green" : "red";
                series.getNode().setStyle("-fx-stroke: " + lineColor + "; -fx-stroke-width: 2px;");

                container.getChildren().addAll(nameText, priceText, changeText, chart);
            } else {
                TextArea dataArea = new TextArea("No historical data available.");
                dataArea.setPrefWidth(300);
                dataArea.setPrefHeight(90);
                dataArea.setEditable(false);
                dataArea.setStyle("-fx-control-inner-background: #f8f9fa; -fx-font-size: 10px;");
                container.getChildren().addAll(nameText, priceText, changeText, dataArea);
            }
        } catch (Exception e) {
            StringBuilder data = new StringBuilder("Historical Prices:\n");
            for (int i = 0; i < prices.size(); i++) {
                data.append(String.format("Day %d: %.2f\n", i + 1, prices.get(i)));
            }
            TextArea dataArea = new TextArea(data.toString());
            dataArea.setPrefWidth(300);
            dataArea.setPrefHeight(90);
            dataArea.setEditable(false);
            dataArea.setStyle("-fx-control-inner-background: #f8f9fa; -fx-font-size: 10px;");
            container.getChildren().addAll(nameText, priceText, changeText, dataArea);
        }

        return container;
    }
}
