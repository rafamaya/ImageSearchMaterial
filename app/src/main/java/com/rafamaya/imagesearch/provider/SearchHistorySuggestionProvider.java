package com.rafamaya.imagesearch.provider;

import android.content.SearchRecentSuggestionsProvider;

public class SearchHistorySuggestionProvider extends SearchRecentSuggestionsProvider {
	
    public final static String AUTHORITY = "com.rafamaya.imagesearch.provider.SearchHistorySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchHistorySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}