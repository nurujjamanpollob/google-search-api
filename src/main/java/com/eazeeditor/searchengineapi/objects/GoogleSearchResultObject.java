package com.eazeeditor.searchengineapi.objects;

import javadev.stringcollections.textreplacor.io.json.JSONObjectUtility;

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

    /**
     * Overrides the toString method to provide a JSON representation of the GoogleSearchResultObject.
     * @return a JSON string representing the object
     */
    @Override
    public String toString() {
        return """
                {
                    "title": "%s",
                    "link": "%s",
                    "description": "%s"
                }
                """.formatted(JSONObjectUtility.escapeSpecialCharacters(getTitle()), JSONObjectUtility.escapeSpecialCharacters(getLink()), JSONObjectUtility.escapeSpecialCharacters(getDescription()));
    }
}
