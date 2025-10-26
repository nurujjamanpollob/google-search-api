package com.eazeeditor.searchengineapi;

import com.google.api.services.customsearch.v1.CustomSearchAPI;
import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nurujjamanpollob
 * Custom Search Client to interact with Google Custom Search API.
 */
public class CustomSearchClient {
    
    private final String apiKey;
    private final String searchEngineId;
    
    public CustomSearchClient(String apiKey, String searchEngineId) {
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
    }
    
    public Search executeSearch(String query) {
        // use Custom Search API to perform search with the given query
        // and return the Search results
        
        try {
            com.google.api.client.http.HttpTransport httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
            com.google.api.client.json.JsonFactory jsonFactory = new com.google.api.client.json.gson.GsonFactory();
            
            CustomSearchAPI customSearchAPI = new CustomSearchAPI.Builder(httpTransport, jsonFactory, null)
                    .setApplicationName("EazeEditorSearchEngine")
                    .build();
            
            CustomSearchAPI.Cse.List request = customSearchAPI.cse().list();
            request.setKey(apiKey);
            request.setCx(searchEngineId);
            request.setQ(query);
            
            return request.execute();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            
        }
        return null; // return null in case of error
    }

    /**
     * Extract links from the search results.
     */
    public List<String> extractLinksFromSearchResults(Search searchResult) {
        List<String> links = new ArrayList<>();
        if (searchResult.getItems() != null) {
            for (Result item : searchResult.getItems()) {
                if (item.getLink() != null) {
                    links.add(item.getLink());
                }
            }
        }
        return links;
    }

    /**
     * Extract useful content from the search results by filtering out unwanted elements.
     * Useful for getting main text content from web pages.
     */
    public List<String> extractUsefulContentFromSearchResults(Search searchResult) {
        List<String> contentList = new ArrayList<>();
        if (searchResult.getItems() != null) {
            for (Result item : searchResult.getItems()) {
                String link = item.getLink();
                if (link != null) {
                    try {
                        Document doc = Jsoup.connect(link).get();
                        Elements contentElements = doc.select("body *:not(script):not(style):not(footer):not(header):not(video):not(img):not(ad):not(div):not(span):not(p)");
                        for (Element element : contentElements) {
                            String text = element.text();
                            if (!text.isEmpty()) {
                                contentList.add(text);
                            }
                        }
                    } catch (IOException e) {
                        // Handle errors gracefully
                        e.printStackTrace();
                    }
                }
            }
        }
        return contentList;
    }

    /**
     * Extract filtered content from the search results by removing unwanted elements.
     */
    public List<String> extractFilteredContentFromSearchResults(Search searchResult) {
        List<String> filteredContent = new ArrayList<>();
        if (searchResult.getItems() != null) {
            for (Result item : searchResult.getItems()) {
                String link = item.getLink();
                if (link != null) {
                    try {
                        Document doc = Jsoup.connect(link).get();
                        // Filter out headers, footers, ads, videos, images, etc.
                        Elements contentElements = doc.select("body *:not(script):not(style):not(footer):not(header):not(video):not(img):not(ad):not(div):not(span):not(p):not(a):not(ul):not(ol):not(li):not(table):not(tr):not(td):not(th)");
                        for (Element element : contentElements) {
                            String text = element.text();
                            if (!text.isEmpty()) {
                                filteredContent.add(text);
                            }
                        }
                    } catch (IOException e) {
                        // Handle errors gracefully
                        e.printStackTrace();
                    }
                }
            }
        }
        return filteredContent;
    }

}