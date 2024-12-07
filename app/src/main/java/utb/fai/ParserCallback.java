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

/**
 * Třída ParserCallback je používána parserem DocumentParser,
 * je implementován přímo v JDK a umí parsovat HTML do verze 3.0.
 * Při parsování (analýze) HTML stránky volá tento parser
 * jednotlivé metody třídy ParserCallback, co nám umožuje
 * provádět s částmi HTML stránky naše vlastní akce.
 * 
 * @author Tomá Dulík
 */
class ParserCallback extends HTMLEditorKit.ParserCallback {
    private final HashMap<String, Integer> wordCountMap = new HashMap<>();
    private URI pageURI;
    int Depth = 0;
    int maxDepth = 5;
    HashSet<URI> visitedURIs;
    LinkedList<URIinfo> foundURIs;
    int debugLevel = 0;

    ParserCallback(HashSet<URI> visitedURIs, LinkedList<URIinfo> foundURIs) {
        this.foundURIs = foundURIs;
        this.visitedURIs = visitedURIs;
    }

    /**
     * metoda handleSimpleTag se volá např. u značky <FRAME>
     */
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        handleStartTag(t, a, pos);
    }

    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        URI uri;
        String href = null;
        if (debugLevel > 1){
            System.err.println("handleStartTag: " + t + ", pos=" + pos + ", attribs=" + a);
        }
        if (depth <= maxDepth) {
            if (t == HTML.Tag.A) {
                href = (String) a.getAttribute(HTML.Attribute.HREF);
            } else if (t == HTML.Tag.FRAME) {
                href = (String) a.getAttribute(HTML.Attribute.SRC);
            }
        }
        if (href != null)
            try {
                uri = pageURI.resolve(href);
                if (!uri.isOpaque() && !visitedURIs.contains(uri)) {
                    visitedURIs.add(uri);
                    foundURIs.add(new URIinfo(uri, depth + 1));
                    if (debugLevel > 0) {
                        System.err.println("Adding URI: " + uri.toString());
                    }
                }
            } catch (Exception e) {
                System.err.println("Nalezeno nekorektní URI: " + href);
                e.printStackTrace();
            }

    }

    /******************************************************************
     * V metodě handleText bude probíhat veškerá činnost, související se
     * zjiováním četnosti slov v textovém obsahu HTML stránek.
     * IMPLEMENTACE TÉTO METODY JE V TÉTO ÚLOZE VAŠÍM ÚKOLEM !!!!
     * Možný postup:
     * Ve třídě Parser (klidně v její metodě main) si vytvořte vyhledávací tabulku
     * =instanci třídy HashMap<String,Integer> nebo TreeMap<String,Integer>.
     * Do této tabulky si ukládejte dvojice klíč-data, kde
     * klíčem jsou jednotlivá slova z textového obsahu HTML stránek,
     * data typu Integer bude dosavadní počet výskytu daného slova v
     * HTML stránkách.
     *******************************************************************/
    public void handleText(char[] data, int position) {

        String[] words = Strting.valueOf(data).trim().split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                word = word.toLowerCase();
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
            }
        }
        public List<Map.Entry<String, Integer>> getSortedWordCount() {
            List<Map.Entry<String, Integer>> sortedWordCount = new ArrayList<>(wordCountMap.entrySet());
            sortedWordCount.sort(o1, o2) ->o2.getValue().compareTo(o1.getValue()));
            return sortedWordCount;
        }

        public void printWordCount(int count) {
            List<Map.Entry<String, Integer>> sortedWordCount = getSortedWordCount();
            for (int i =0; i<count && i < sortedWordCount.size(); i++) {
                System.out.println(sortedWordCount.get().getKey() + ";" + sortedWordCount.get().getValue());
            }
        }
        public Map<String, Integer> getWordFrequency() {
            return wordCountMap;
            }
         }
}
