package com.example.ecoroute.interfaces

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion


val reverseSearchCallback = object : SearchCallback {
    override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
    }

    override fun onError(e: Exception) {
    }
}
