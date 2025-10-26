package com.eazeeditor.searchengineapi.website;

import com.eazeeditor.searchengineapi.CustomSearchClient;
import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Search;
import javadev.stringcollections.textreplacor.console.ColoredConsoleOutput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author nurujjamanpollob
 * Service to filter and extract useful content from search results.
 */
public class ContentFilterService {

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
     */
    public List<String> extractUsefulContentFromSearchResults(Search searchResult) {
        List<String> contentList = new ArrayList<>();
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