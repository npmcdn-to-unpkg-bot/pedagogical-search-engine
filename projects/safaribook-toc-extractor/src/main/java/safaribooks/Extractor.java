package safaribooks;

import org.json.JSONArray;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toc.Utils;
import tree.Node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Extractor {
    public static final Logger logger = LoggerFactory.getLogger(Extractor.class);

    private Document doc = null;

    public Extractor(Document d) {
        this.doc = d;
    }

    public Node<String> getToc() {
        Node rootNode = null;
        Elements tocDocs = this.doc.getElementsByClass("toc_book");
        if(tocDocs.size() == 1) {
            Element tocDoc = tocDocs.get(0);
            ArrayList<toc.FlatEntry<String>> tocEntries = new ArrayList<toc.FlatEntry<String>>();
            for (Element entryEl : tocDoc.children()) {
                tocEntries.add(new safaribooks.FlatEntry(entryEl));
            }
            rootNode = Utils.expandFlatEntries(tocEntries);
        }

        // Add top attributes
        // Page-url
        String rootUrl = "http://proquest.safaribooksonline.com";

        // metadatalist
        Element metaList = this.doc.getElementsByClass("metadatalist").first();

        // book_title
        String title = metaList.getElementsByClass("book_title").text();
        rootNode.attributes.put("title", title);
        logger.info(String.format("Title: %s", title));

        // book author(s)
        String authorsStr = metaList.getElementsByTag("li").get(1).text();
        int byPosition = authorsStr.indexOf("By: ");
        if(byPosition != -1) {
            authorsStr = authorsStr.substring(byPosition + 3, authorsStr.length());
        }
        List<String> authors = new ArrayList<String>();
        for(String authorStr: authorsStr.split(";")) {
            authors.add(authorStr.trim());
        }
        rootNode.attributes.put("authors", new JSONArray(authors.toArray()));
        logger.info(String.format("Authors: %s", StringUtil.join(authors, ", ")));

        // Publisher
        String publisher = metaList.getElementsByAttributeValue("itemprop", "publisher").text();
        rootNode.attributes.put("publisher", publisher);
        logger.info(String.format("Publisher: %s", publisher));

        // Publication Date
        String dateStr = metaList.getElementsByAttributeValue("itemprop", "datePublished").text();
        DateFormat format = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
        Date dateObj = null;
        try {
            dateObj = format.parse(dateStr);
            DateFormat normFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String date = normFormat.format(dateObj);
            rootNode.attributes.put("date", date);
            logger.info(String.format("Date: %s", date));
        } catch (ParseException e) {
            logger.error("Cannot parse date: " + e.getMessage());
        }

        // book_cover
        String srcPath = this.doc.getElementsByClass("book_cover").attr("src");
        rootNode.attributes.put("cover-img", srcPath);
        logger.info(String.format("Img: %s", srcPath));

        return rootNode;
    }
}
