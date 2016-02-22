import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Scrapper {
    private static final Logger logger = LoggerFactory.getLogger(Scrapper.class);

    public static void main(String[] args) {
        WebClient webClient = new WebClient();
        final HtmlPage page;
        try {
            page = webClient.getPage("http://htmlunit.sourceforge.net");
            String pageAsText = page.asText();

            logger.info("Fist letters of the page: " + pageAsText.substring(0, 10));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
