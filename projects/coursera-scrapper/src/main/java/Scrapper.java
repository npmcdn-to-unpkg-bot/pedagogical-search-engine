import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import htmlunit.Stubborn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Scrapper {
    private static final Logger logger = LoggerFactory.getLogger(Scrapper.class);

    public static void main(String[] args) {
        // Settup the client
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
        webClient.getOptions().setRedirectEnabled(true);

        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        webClient.getCookieManager().setCookiesEnabled(false);

        final String domain = "https://www.coursera.org";
        final String startUrl = domain + "/browse/?languages=en";
        try {
            final HtmlPage startPage = webClient.getPage(startUrl);

            final List<String> categories = Arrays.asList(
                    "Arts and Humanities",
                    "Business",
                    "Computer Science");

            final String category = categories.get(0);
            try {
                final HtmlAnchor categoryLink = startPage.getAnchorByText(category);
                final String categoryPath = categoryLink.getHrefAttribute();

                logger.info("Opening: " + categoryPath);
                final HtmlPage categoryPage = categoryLink.click();
                for(final HtmlButton seeAllBtn:
                        (List<HtmlButton>) categoryPage.getByXPath("//button[./text() = \"See All\"]")) {
                    final HtmlAnchor seeAllLink = (HtmlAnchor) seeAllBtn.getParentNode();
                    final String listPath = seeAllLink.getHrefAttribute();

                    logger.info("Opening: " + listPath);
                    final HtmlPage listPage = seeAllLink.click();
                    final List<DomElement> subCatTitleElements = (List<DomElement>) listPage.getByXPath("//h1");
                    final int nbSubCategoriesTitles = subCatTitleElements.size();
                    if(nbSubCategoriesTitles == 1) {
                        final String subCategory = subCatTitleElements.get(0).asText();
                        for(final HtmlAnchor courseLink:
                                (List<HtmlAnchor>) listPage.getByXPath("//a[@class = \"rc-OfferingCard\"]")) {
                            final String coursePath = courseLink.getHrefAttribute();
                            logger.info("Opening: " + coursePath);
                            final HtmlPage coursePage = courseLink.click();

                            final DomElement courseTitleElements = Stubborn.getExactlyOneByXPath(
                                    coursePage,
                                    "//h1",
                                    20);

                            if(courseTitleElements != null) {
                                final String title = courseTitleElements.asText();
                                logger.info("Course title: " + title);

                                // todo: write the file
                            } else {
                                logger.error(String.format("Cannot find 1 course title! " +
                                        "(cat: %s, sub-cat: %s, path: %s)",
                                        category,
                                        subCategory,
                                        coursePath));
                                coursePage.save(new File("error"));
                            }
                        }
                    } else {
                        logger.error(String.format("%d sub-category(ies) found! " +
                                "(cat: %s, path: %s)",
                                nbSubCategoriesTitles, category, listPath));
                    }
                }

            } catch (ElementNotFoundException e) {
                logger.error(String.format("Cannot open category %s. Error-msg: %s",
                        category, e.getMessage()));
            }
        } catch (IOException e) {
            logger.error(String.format("Cannot open main-page %s. Error-msg: %s",
                    startUrl, e.getMessage()));
        } finally {
            logger.info("Ending the program");
            webClient.close();
        }
    }
}
