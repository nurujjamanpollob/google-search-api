package com.eazeeditor.searchengineapi.objects;

public class GoogleSearchResultObject {

    private final String title;
    private final String link;
    private final String description;

    public GoogleSearchResultObject(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }
    public String getLink() {
        return link;
    }
    public String getDescription() {
        return description;
    }
}
