package utb.fai;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

class ParserCallback extends HTMLEditorKit.ParserCallback {

    URI pageURI;
    int depth = 0, maxDepth = 5;
    HashSet<URI> visitedURIs;
    LinkedList<URIinfo> foundURIs;
    int debugLevel = 0;
    private final Map<String, Integer> wordCountMap = new HashMap<>();

    ParserCallback(HashSet<URI> visitedURIs, LinkedList<URIinfo> foundURIs) {
        this.foundURIs = foundURIs;
        this.visitedURIs = visitedURIs;
    }

    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        handleStartTag(t, a, pos);
    }


    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        URI uri;
        String href = null;
        if (debugLevel > 1) {
            System.err.println("handleStartTag: " + t + ", pos=" + pos + ", attribs=" + a);
        }
        if (depth <= maxDepth) {
            if (t == HTML.Tag.A) {
                href = (String) a.getAttribute(HTML.Attribute.HREF);
            } else if (t == HTML.Tag.FRAME) {
                href = (String) a.getAttribute(HTML.Attribute.SRC);
            }
        }
        if (href != null) {
            try {
                uri = pageURI.resolve(href);
                if (!uri.isOpaque() && !visitedURIs.contains(uri)) {
                    visitedURIs.add(uri);
                    foundURIs.add(new URIinfo(uri, depth + 1));
                    if (debugLevel > 0) {
                        System.err.println("Adding URI: " + uri);
                    }
                }
            } catch (Exception e) {
                System.err.println("Invalid URI found: " + href);
                e.printStackTrace();
            }
        }
    }

    public void handleText(char[] data, int pos) {

        String[] words = String.valueOf(data).trim().split("[\\s]+");


        for(String word : words) {
            if (!word.isEmpty()) {
                word = word.toLowerCase();
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
            }
        }

    }

    public List<Map.Entry<String, Integer>> getSortedWordCount() {
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(wordCountMap.entrySet());
        sortedList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return sortedList;
    }

    public void printWordCount(int count) {
        List<Map.Entry<String, Integer>> sortedList = getSortedWordCount();
        for (int i = 0; i < count && i < sortedList.size(); i++) {
            System.out.println(sortedList.get(i).getKey() + ";" + sortedList.get(i).getValue());
        }
    }
    public Map<String, Integer> getWordFrequency() {
        return wordCountMap;
    }

}