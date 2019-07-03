package com.teajey.searchreminder;

import java.io.Serializable;

public class SearchQuery implements Serializable {
    private String query;
    private String searchEngine;

    SearchQuery(String query, String engine) {
        this.query = query;
        this.searchEngine = engine;
    }

    public String getQuery() {
        return query;
    }

    public String getSearchEngine() {
        return searchEngine;
    }

    public void setQuery(String q) {
        this.query = q;
    }

    public void setSearchEngine(String se) {
        this.searchEngine = se;
    }
}
