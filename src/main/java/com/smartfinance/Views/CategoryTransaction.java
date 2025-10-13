package com.smartfinance.Views;

import com.smartfinance.Models.Model;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides a dynamic view of transaction categories. Defaults are combined with
 * categories fetched from client budgets so any newly added budget category becomes
 * immediately available across the UI.
 */
public final class CategoryTransaction {

    private static final List<String> DEFAULT_CATEGORY_SOURCE = List.of(
            "Electricity",
            "Water",
            "PhoneBill",
            "Gas",
            "Internet",
            "Food",
            "Other"
    );

    private static final ObservableList<String> DEFAULT_CATEGORIES = FXCollections.unmodifiableObservableList(
            FXCollections.observableArrayList(DEFAULT_CATEGORY_SOURCE)
    );

    private static final Model MODEL = Model.getInstance();
    private static final ObservableList<String> AVAILABLE_CATEGORIES = FXCollections.observableArrayList();

    static {
        MODEL.getBudgetCategories().addListener((ListChangeListener<String>) change -> synchronizeCategories());
        synchronizeCategories();
    }

    private CategoryTransaction() {
        // Utility class
    }

    /**
     * Returns the immutable list of built-in default categories.
     */
    public static ObservableList<String> getDefaultCategories() {
        return DEFAULT_CATEGORIES;
    }

    /**
     * Returns the merged, deduplicated, and alphabetically sorted list of categories.
     */
    public static ObservableList<String> getAvailableCategories() {
        return FXCollections.unmodifiableObservableList(AVAILABLE_CATEGORIES);
    }

    /**
     * Forces a refresh of the category list. Useful after bulk updates outside the
     * normal save/delete budget flow.
     */
    public static void refresh() {
        synchronizeCategories();
    }

    private static void synchronizeCategories() {
        Runnable updateTask = () -> {
            Set<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            List<String> merged = new ArrayList<>();

            addIfAbsent(merged, seen, "Transfer");

            for (String defaultCategory : DEFAULT_CATEGORY_SOURCE) {
                addIfAbsent(merged, seen, defaultCategory);
            }

            for (String dynamicCategory : MODEL.getBudgetCategories()) {
                addIfAbsent(merged, seen, dynamicCategory);
            }

            Collections.sort(merged, String::compareToIgnoreCase);
            AVAILABLE_CATEGORIES.setAll(merged);
        };

        if (Platform.isFxApplicationThread()) {
            updateTask.run();
        } else {
            Platform.runLater(updateTask);
        }
    }

    private static void addIfAbsent(List<String> list, Set<String> seen, String category) {
        if (category == null) {
            return;
        }
        String normalized = category.trim();
        if (normalized.isEmpty()) {
            return;
        }
        String key = normalized.toLowerCase(Locale.ROOT);
        if (seen.add(key)) {
            list.add(normalized);
        }
    }
}
